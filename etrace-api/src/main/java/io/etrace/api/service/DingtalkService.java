package io.etrace.api.service;

import io.etrace.api.model.po.ui.ApiToken;
import io.etrace.api.model.po.ui.ApplyTokenLog;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.thirdparty.dingtalk.At;
import io.etrace.api.thirdparty.dingtalk.DingtalkResponse;
import io.etrace.api.thirdparty.dingtalk.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Dingding openapi: https://open-doc.dingtalk.com/docs/doc.htm
 */
@Service
@Primary
public class DingtalkService implements NotifyService {

    private static final String URL_PREFIX = "https://oapi.dingtalk.com/robot/send?access_token=";
    private static final Logger LOGGER = LoggerFactory.getLogger(DingtalkService.class);
    @Autowired
    RestTemplate restTemplate;
    @Value("${dingtalk.token:mock_token}")
    String token;
    @Value("${dingtalk.mainToken:mock_token}")
    String mainToken;
    @Autowired
    UserService userService;

    public DingtalkResponse sendTextMessageToSupportGroup(String message, String userPhone) {
        TextMessage textMessage = new TextMessage(message, At.build(userPhone));
        return restTemplate.postForObject(URL_PREFIX + token, textMessage, DingtalkResponse.class);
    }

    public DingtalkResponse sendTextMessageToDevGroup(String message) {
        TextMessage textMessage = new TextMessage(message, null);
        return restTemplate.postForObject(URL_PREFIX + mainToken, textMessage, DingtalkResponse.class);
    }

    @Override
    public void notifyApiTokenRequest(ETraceUser user, ApplyTokenLog applyTokenLog) {
        String msg = String.format("[%s]提交了Open API使用审批，请尽快前往审批！", user.getUsername());
        sendTextMessageToDevGroup(msg);
    }

    @Override
    public void notifyApiTokenApproved(ETraceUser user, ApiToken apiToken) {
        String userMsg = String.format("%s，您提交的Open API使用申请， 已批准通过。", user.getUsername());
        sendTextMessageToSupportGroup(userMsg, user.getEmail());
    }
}
