package io.etrace.plugins.interceptors;

import io.etrace.agent.Trace;
import io.etrace.common.constant.Constants;
import io.etrace.common.message.trace.Message;
import io.etrace.common.message.trace.Transaction;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;

public class TraceInterceptor extends HandlerInterceptorAdapter {

    private ThreadLocal<Transaction> contexts = new ThreadLocal<>();

    public TraceInterceptor() {
        super();
    }

    @Override
    public boolean preHandle(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse response,
                             Object handler) throws Exception {
        String type = Constants.URL;
        Transaction t = Trace.newTransaction(type, req.getRequestURI());
        contexts.set(t);
        logRequestPayload(req, type);
        return super.preHandle(req, response, handler);
    }

    @Override
    public void afterCompletion(javax.servlet.http.HttpServletRequest request,
                                javax.servlet.http.HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
        try {
            Transaction t = contexts.get();
            if (null != t) {
                try {
                    if (null == ex) {
                        t.setStatus(Message.SUCCESS);
                    } else {
                        Trace.logError(ex);
                        t.setStatus(ex);
                    }
                } finally {
                    t.complete();
                }
            }
        } finally {
            contexts.remove();
        }

        super.afterCompletion(request, response, handler, ex);
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
