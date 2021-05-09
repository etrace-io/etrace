package io.etrace.api.filter;

import com.google.common.collect.Sets;
import io.etrace.agent.Trace;
import io.etrace.api.exception.UnauthorizedException;
import io.etrace.api.model.RateLimitRequest;
import io.etrace.api.model.po.ui.ApiToken;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.ratelimit.BaseRateLimitService;
import io.etrace.api.service.ApiTokenService;
import io.etrace.api.service.UserService;
import io.etrace.common.constant.Constants;
import io.etrace.common.message.trace.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ApiTokenFilter extends GenericFilterBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiTokenFilter.class);
    // todo:
    private static final Set<String> whiteSet = Sets.newHashSet();
    private static final Set<Pattern> urlPatterns = new LinkedHashSet();
    private final ApiTokenService apiTokenService;
    private final UserService userService;
    private final BaseRateLimitService baseRateLimitService;


    public ApiTokenFilter(ApiTokenService apiTokenService, UserService userService,
                          BaseRateLimitService baseRateLimitService) {
        this.apiTokenService = apiTokenService;
        this.userService = userService;
        this.baseRateLimitService = baseRateLimitService;
    }

    public void initPathPattern(String pattern) {
        try {
            String[] patterns = pattern.split(";");
            for (int i = 0; i < patterns.length; ++i) {
                String temp = patterns[i];
                if (!StringUtils.isEmpty(temp)) {
                    urlPatterns.add(Pattern.compile(temp.trim()));
                }
            }
        } catch (Exception e) {
            LOGGER.error("初始化pattern error", e);
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        //if request is white list, do next chain
        //check 是否是api用户
        try {
            ETraceUser ETraceUser = getQueryUser(request);
            ETraceUser.setIsApiUser(Boolean.TRUE);
            if (!checkAccess(ETraceUser.getEmail())) {
                sendErrorResponse(res, "request too often", 429);
                return;
            }
            logCounterWithUserInfo(request.getRequestURI(), ETraceUser);
            String path = getRequestURI(request.getRequestURI());
            logRequestPayload(path, ETraceUser.getUsername());
            Trace.logEvent("Open-Api", ETraceUser.getUsername(), Constants.SUCCESS,
                    String.format("%s %s %s", request.getMethod(), request.getRequestURL().toString(),
                            request.getQueryString()), null
            );
        } catch (Exception e) {
            String responseReason;
            LOGGER.error("openApi 鉴权失败", e);
            if (e instanceof UnauthorizedException) {
                responseReason = e.getMessage();
            } else {
                responseReason = "service internal error";
            }
            logCounterWithException(request.getRequestURI());
            sendErrorResponse(res, responseReason, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chain.doFilter(request, response);
        return;
    }

    private void logCounterWithUserInfo(String path, ETraceUser ETraceUser) {
        Trace.newCounter("api_request")
                .addTag("request_type", "OK")
                .addTag("psnname", ETraceUser.getUsername())
                .once();
    }

    private void logCounterWithException(String path) {
        Trace.newCounter("api_request")
                //                .addTag("path", path)
                .addTag("request_type", "Exception")
                .once();
    }

    private ETraceUser getQueryUser(HttpServletRequest request) throws UnauthorizedException {
        String cid = request.getHeader("Emonitor-ID");
        String encode = request.getHeader("Emonitor-Authorization-Code");
        if (StringUtils.isEmpty(cid)) {
            throw new UnauthorizedException("Emonitor-ID is empty: [" + cid + "]");
        }
        if (StringUtils.isEmpty(encode)) {
            throw new UnauthorizedException("Emonitor-Authorization-Code is empty: [" + encode + "]");
        }
        ApiToken apiToken = apiTokenService.findByCid(cid);
        if (null == apiToken) {
            throw new UnauthorizedException("无效的Emonitor-ID: [" + cid + "]");
        }
        //  check 加密是否匹配
        String gen;
        long tm = (System.currentTimeMillis() / 1000) / 30;
        try {
            gen = genToken(apiToken.getToken(), tm);
            if (!encode.equals(gen)) {
                // 两次check
                String recheck = genToken(apiToken.getToken(), tm - 1);
                if (!encode.equals(recheck)) {
                    throw new UnauthorizedException("invalid token!");
                }
            }
        } catch (Exception e) {
            LOGGER.error("验证签名异常", e);
            throw new UnauthorizedException("validate token error!");
        }
        ETraceUser ETraceUser = userService.findByUserEmail(apiToken.getUserEmail());
        if (ETraceUser == null) {
            throw new UnauthorizedException(
                    "can't find the user info for User Email [" + apiToken.getUserEmail() + "]");
        }
        return ETraceUser;

    }

    private void sendErrorResponse(ServletResponse res, String reason, int httpStatus) throws IOException {
        HttpServletResponse response = (HttpServletResponse) res;
        // don't continue the chain
        response.setStatus(httpStatus);
        response.getWriter().print("error reason: " + reason);
        response.flushBuffer();
    }

    private String genToken(String secret, long tm) throws NoSuchAlgorithmException, InvalidKeyException {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(tm);
        buffer.flip();
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(keySpec);
        byte[] hm = mac.doFinal(buffer.array());
        int offset = hm[hm.length - 1] & 0x0F;
        byte[] truncatedHash = Arrays.copyOfRange(hm, offset, offset + 4);
        ByteBuffer buf = ByteBuffer.allocate(truncatedHash.length);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.put(truncatedHash);
        buf.flip();
        long hash = buf.getInt();
        hash &= 0x7FFFFFFF;
        hash %= 1000000;
        return String.format("%06d", hash);
    }

    private boolean checkAccess(String user) {
        if (StringUtils.isEmpty(user)) {
            return false;
        }
        if (whiteSet.contains(user)) {
            return true;
        }
        RateLimitRequest rateLimitrequest = new RateLimitRequest();
        rateLimitrequest.setAcquireNum(1);
        rateLimitrequest.setKey(user);
        Transaction transaction = Trace.newTransaction("RateLimit", "OpenApi");
        boolean allowed = baseRateLimitService.isAllowed(rateLimitrequest);
        transaction.setStatus(allowed ? Constants.SUCCESS : Constants.FAILURE);
        transaction.complete();
        return allowed;
    }

    private String getRequestURI(String requestURI) {
        if (urlPatterns.size() == 0) {
            return "unknown-url";
        } else {
            Iterator<Pattern> iterator = urlPatterns.iterator();
            while (iterator.hasNext()) {
                Pattern pattern = iterator.next();
                try {
                    if (pattern.matcher(requestURI).find()) {
                        return pattern.pattern();
                    }
                } catch (Exception e) {
                    LOGGER.error("解析requestURI error", e);
                }
            }
            return "unknown-url";
        }
    }

    private void logRequestPayload(String path, String user) {
        Trace.newCounter("OpenApiURL").addTag("path", path).addTag("user", user).once();
    }
}
