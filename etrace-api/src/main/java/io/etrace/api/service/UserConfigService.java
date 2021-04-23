package io.etrace.api.service;

import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserConfigPO;
import io.etrace.api.repository.UserConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserConfigService {

    @Autowired
    private UserConfigMapper userConfigMapper;

    public UserConfigPO findUserConfig(ETraceUser user) {
        // 游客不允许改配置
        if (user.isAnonymousUser()) {
            return new UserConfigPO();
        }
        return userConfigMapper.findByUserEmail(user.getEmail());
    }

    public UserConfigPO createOrUpdate(UserConfigPO userConfig, ETraceUser user) {
        UserConfigPO config = userConfigMapper.findByUserEmail(userConfig.getUserEmail());
        if (null == config) {
            userConfig.setUserEmail(user.getEmail());
            return userConfigMapper.save(userConfig);
        } else {
            config.setConfig(userConfig.getConfig());
            return userConfigMapper.save(config);
        }
    }
}
