package io.etrace.agent.message;

import io.etrace.agent.message.manager.DefaultMessageManager;
import io.etrace.common.modal.TraceContext;

public class DefaultTraceContext implements TraceContext {
    private DefaultMessageManager.Context ctx;

    public DefaultTraceContext(DefaultMessageManager.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public DefaultMessageManager.Context getCtx() {
        return ctx;
    }
}
