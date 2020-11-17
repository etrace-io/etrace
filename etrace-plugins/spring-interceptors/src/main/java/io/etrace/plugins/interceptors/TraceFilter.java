package io.etrace.plugins.interceptors;

import com.google.common.base.Strings;
import io.etrace.agent.Trace;
import io.etrace.common.constant.Constants;
import io.etrace.common.message.trace.Message;
import io.etrace.common.message.trace.Transaction;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class TraceFilter implements Filter {
    private static Set<Pattern> urlPatterns = new LinkedHashSet<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String pattern = filterConfig.getInitParameter("trace-url");
        if (pattern != null) {
            try {
                String[] patterns = pattern.split(";");
                for (String temp : patterns) {
                    if (!Strings.isNullOrEmpty(temp)) {
                        urlPatterns.add(Pattern.compile(temp.trim()));
                    }
                }
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        String type = Constants.URL;
        Transaction t = Trace.newTransaction(type, getRequestURI(req));
        try {
            try {
                logRequestPayload(req, type);
                t.setStatus(Message.SUCCESS);
            } catch (Throwable ignore) {
                t.setStatus(ignore);
            }
            chain.doFilter(request, response);
        } catch (Error e) {
            Trace.logError(e);
            t.setStatus(e);
            throw e;
        } finally {
            t.complete();
        }
    }

    private String getRequestURI(HttpServletRequest req) {
        String requestURI = req.getRequestURI();

        if (urlPatterns.size() == 0) {
            return "unknown-url";
        } else {
            for (Pattern pattern : urlPatterns) {
                try {
                    if (pattern.matcher(requestURI).find()) {
                        return pattern.pattern();
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
            return "unknown-url";
        }
    }

    protected void logRequestPayload(HttpServletRequest req, String type) {
        StringBuilder sb = new StringBuilder(256);

        sb.append(req.getScheme().toUpperCase()).append('/');
        sb.append(req.getMethod()).append(' ').append(req.getRequestURI());

        String qs = req.getQueryString();

        if (qs != null) {
            sb.append('?').append(qs);
        }
        Trace.logEvent(type, type + ".Method", Message.SUCCESS, sb.toString(), null);
    }
}
