package io.etrace.api.filter;

import com.google.common.base.Strings;
import io.etrace.agent.Trace;
import io.etrace.api.config.WebSecurityConfig;
import io.etrace.api.consts.UserContext;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.service.UserRoleService;
import io.etrace.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static io.etrace.api.service.UserService.VISITOR_PSNCODE;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@Deprecated
public class UserAuthenticationFilter extends GenericFilterBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthenticationFilter.class);
    private static final String UNKNOWN = "unknown";
    private final List<RequestMatcher> matcherList = new ArrayList<>();
    private final UserService userService;
    private final UserRoleService userRoleService;

    //todo: this key should be configurable
    private final String aeskey = "monitor-api-aes-key";

    private final String coffeeCookieName;

    public UserAuthenticationFilter(UserService userService, UserRoleService userRoleService, String coffeeCookieName) {
        this.userService = userService;
        this.userRoleService = userRoleService;
        for (String uri : WebSecurityConfig.AUTH_WHITELIST) {
            matcherList.add(new AntPathRequestMatcher(uri, null));
        }

        this.coffeeCookieName = coffeeCookieName;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        //if request is white list, do next chain
        if (isWhiteList(request)) {
            logCounterForWhiteList(request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }
        if (isOptionMethod(request)) {
            return;
        }
        HttpSession httpSession = request.getSession();
        Object contextFromSession = httpSession.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
        if (contextFromSession == null) {
            contextFromSession = SecurityContextHolder.createEmptyContext();
        }
        SecurityContext securityContext = (SecurityContext)contextFromSession;
        if (securityContext.getAuthentication() == null) {

            //build authentication token, if token is null auth fail
            try {
                Authentication authentication = buildAuthenticationToken(request, response);
                securityContext.setAuthentication(authentication);
            } catch (UserForbiddenException userForbiddenException) {
                logCounterForNoPermission(request.getRequestURI());
                sendErrorResponse(response, "authentication fail, maybe no permission: SC_FORBIDDEN",
                    HttpServletResponse.SC_FORBIDDEN);
                return;
            } catch (IOException iOException) {
                //maybe no permission or token auth fail
                logCounterForUnauthorized(request.getRequestURI());
                sendErrorResponse(response, "authentication fail, maybe invalid token: SC_UNAUTHORIZED",
                    HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (Exception e) {
                LOGGER.error("登录未知异常", e);
                sendUnAuthorizedResponse(response, "authentication fail, maybe invalid token");
                // 原因不明
                logCounterForUnknownUnauthorized(request.getRequestURI());
                return;
            }
        }
        httpSession.setAttribute(SPRING_SECURITY_CONTEXT_KEY, contextFromSession);

        try {
            String psncode;
            Authentication authentication = securityContext.getAuthentication();
            if (authentication.getPrincipal() instanceof UserDetails) {
                psncode = ((UserDetails)authentication.getPrincipal()).getUsername();
            } else {
                psncode = authentication.getPrincipal().toString();
            }
            logCounterWithPsncode(request.getRequestURI(), psncode);

            //if current user is not visitor，set user role info in threadLocal
            if (!VISITOR_PSNCODE.equalsIgnoreCase(psncode)) {
                Set<String> roles = userRoleService.findRolesByUser(psncode);
                ETraceUser ETraceUser = new ETraceUser();
                ETraceUser.setRoles(roles);
                UserContext.setUser(ETraceUser);
            }
        } catch (Exception e) {
            LOGGER.error("find user role exception，", e);
            logCounterPsncodeExcpetion(request.getRequestURI());
        }

        chain.doFilter(request, response);
    }

    /**
     * Is request white list
     *
     * @param request http request
     * @return true: white list
     */
    private boolean isWhiteList(HttpServletRequest request) {
        for (RequestMatcher matcher : matcherList) {
            if (matcher.matches(request)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOptionMethod(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    /**
     * Send UnAuthorized response to client
     *
     * @param res response
     */
    private void sendUnAuthorizedResponse(ServletResponse res, String reason) throws IOException {
        HttpServletResponse response = (HttpServletResponse)res;
        // don't continue the chain
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().print("UnAuthorized: " + reason);
        response.flushBuffer();
    }

    /**
     * send error response to client
     *
     * @param res        http servlet response
     * @param reason     the error message
     * @param httpStatus http status code such as:{@link javax.servlet.http.HttpServletResponse#SC_OK}
     */
    private void sendErrorResponse(ServletResponse res, String reason, int httpStatus) throws IOException {

        HttpServletResponse response = (HttpServletResponse)res;
        // don't continue the chain
        response.setStatus(httpStatus);
        response.getWriter().print("error reason: " + reason);
        response.flushBuffer();
    }

    /**
     * Get SSO token from cookie
     *
     * @param request http servlet request
     * @return sso token, null not found
     */
    private String getSSOToken(HttpServletRequest request) {
        // 特殊逻辑，钉钉内嵌h5专用逻辑，不要动
        String token = request.getHeader("ELE_MONITOR_TOKEN");
        if (null != token) {
            return token;
        }
        return getCookieValue(request, coffeeCookieName);
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        if (cookieName == null) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Build authentication token based on sso token
     */
    private Authentication buildAuthenticationToken(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        String ssoToken = getSSOToken(request);
        if (!Strings.isNullOrEmpty(ssoToken)) {

            //try {
            //    User user = userService.getUserInfoByToken(ssoToken);
            //    if (!VISITOR_PSNCODE.equalsIgnoreCase(user.getPsncode())) {
            //        //set user info in the thread
            //        Cookie cookie = setCookieForMonitorApi(user);
            //        response.addCookie(cookie);
            //    }
            //    return new UsernamePasswordAuthenticationToken(user.getPsncode(), MOCK_PASSWORD);
            //} catch (IOException e) {
            //    Trace.logError("获取用户信息失败", e);
            //     /*
            //    如遇异常（如sso错误或已过期)，校验UserContext。因为VisitorUserFilter会根据`MONITOR_API_TOKEN`设置成游客（即使sso已过期)
            //     */
            //    if (null != UserContext.getUserInfo()) {
            //        return new UsernamePasswordAuthenticationToken(UserContext.getUserInfo().getPsncode(),
            //            MOCK_PASSWORD);
            //    } else {
            //        throw e;
            //    }
            //}
        } else if (Strings.isNullOrEmpty(ssoToken)) {
            throw new IOException("no coffee token in your request cookie!");
        } else {
            throw new RuntimeException("不可能");
        }

        // todo: implement this!
        throw new RuntimeException("==buildAuthenticationToken== not implemented yet!");
    }

    private void logCounterForWhiteList(String path) {
        Trace.newCounter("normal_request")
            .addTag("request_type", "White_List")
            .addTag("psncode", UNKNOWN)
            .once();
    }

    private void logCounterForNoPermission(String path) {
        Trace.newCounter("normal_request")
            .addTag("request_type", "No_Permission")
            .addTag("psncode", UNKNOWN)
            .once();
    }

    private void logCounterForUnauthorized(String path) {
        Trace.newCounter("normal_request")
            .addTag("request_type", "Unauthorized")
            .addTag("psncode", UNKNOWN)
            .once();
    }

    private void logCounterForUnknownUnauthorized(String path) {
        Trace.newCounter("normal_request")
            .addTag("request_type", "Unauthorized_Unknown_Exception")
            .addTag("psncode", UNKNOWN)
            .once();
    }

    private void logCounterWithPsncode(String path, String psncode) {
        Trace.newCounter("normal_request")
            .addTag("request_type", "OK")
            .addTag("psncode", psncode)
            .once();
    }

    private void logCounterPsncodeExcpetion(String path) {
        Trace.newCounter("normal_request")
            .addTag("request_type", "Psncode_Exception")
            .addTag("psncode", UNKNOWN)
            .once();
    }
}
