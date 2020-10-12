package io.etrace.api.service;

import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserConfig;
import io.etrace.api.repository.UserConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserConfigService {

    @Autowired
    private UserConfigMapper userConfigMapper;

    public UserConfig findUserConfig(ETraceUser user) {
        // 游客不允许改配置
        if (user.isAnonymousUser()) {
            return new UserConfig();
        }
        return userConfigMapper.findByUserEmail(user.getEmail());
    }

    public UserConfig createOrUpdate(UserConfig userConfig, ETraceUser user) {
        UserConfig config = userConfigMapper.findByUserEmail(userConfig.getUserEmail());
        if (null == config) {
            userConfig.setUserEmail(user.getEmail());
            return userConfigMapper.save(userConfig);
        } else {
            config.setConfig(userConfig.getConfig());
            return userConfigMapper.save(config);
        }
    }
}
