package io.etrace.api.service;

import io.etrace.api.model.po.ui.ApiToken;
import io.etrace.api.model.po.ui.ApplyTokenLog;
import io.etrace.api.model.po.user.ETraceUser;
import org.springframework.stereotype.Service;

@Service
public class DefaultNotifyService implements NotifyService {
    @Override
    public void notifyApiTokenRequest(ETraceUser ETraceUser, ApplyTokenLog applyTokenLog) {
        // todo: implement this!
        throw new RuntimeException("==notifyApiTokenRequest== not implemented yet!");

    }

    @Override
    public void notifyApiTokenApproved(ETraceUser ETraceUser, ApiToken apiToken) {
        // todo: implement this!
        throw new RuntimeException("==notifyApiTokenApproved== not implemented yet!");

    }
}
