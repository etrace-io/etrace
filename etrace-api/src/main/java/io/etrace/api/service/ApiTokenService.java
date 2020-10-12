package io.etrace.api.service;

import io.etrace.api.consts.ApiAccessStatus;
import io.etrace.api.consts.ApplyTokenAuditStatus;
import io.etrace.api.consts.TokenStatus;
import io.etrace.api.exception.UnauthorizedException;
import io.etrace.api.model.po.ui.ApiToken;
import io.etrace.api.model.po.ui.ApplyTokenLog;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.repository.ApiTokenMapper;
import io.etrace.api.util.AESUtil;
import io.etrace.api.util.MD5Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ApiTokenService {

    // todo: this should be configurable
    private static final String AESKEY = "monitor-api-api-token-key";
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiTokenService.class);
    /**
     * 默认访问模式为全部都允许
     */
    private final String currentStatus = ApiAccessStatus.none.name();
    @Autowired
    private ApiTokenMapper apiTokenMapper;
    @Autowired
    private ApplyTokenLogService applyTokenLogService;
    // todo: get spring profiles
    private String profiles;

    public String getTokenProfile() {
        return this.profiles;
    }

    @Cacheable(value = "token-cid", key = "#cid")
    public ApiToken findByCid(String cid) throws UnauthorizedException {
        ApiToken apiToken = apiTokenMapper.findByCid(cid);
        checkAccessLevel(apiToken);
        return apiToken;
    }

    public Iterable<ApiToken> findAll() {
        return apiTokenMapper.findAll();
    }

    public ApiToken create(ApiToken apiToken) throws Exception {
        if (null == apiToken || StringUtils.isEmpty(apiToken.getUserEmail())) {
            throw new IllegalArgumentException("illegal param");
        }
        // 根据用户生成token以及cid
        //先查询是否已经生成过api token
        ApiToken tokenFromDB = apiTokenMapper.findByUserEmail(apiToken.getUserEmail());
        if (null != tokenFromDB) {
            throw new DuplicateKeyException("user already have the token！");
        }
        String envAndEmail = apiToken.getUserEmail() + profiles;
        String encode = MD5Encoder.encode(envAndEmail);
        String cid = AESUtil.aesEncrypt(AESKEY, encode);
        String token = new String(Base64Utils.encode(AESUtil.aesEncrypt(AESKEY, envAndEmail).getBytes()));
        apiToken.setCid(cid);
        apiToken.setToken(token);
        apiToken.setStatus(TokenStatus.Active);
        return apiTokenMapper.save(apiToken);
    }

    //protected void update(ApiToken apiToken) {
    //
    //    apiTokenMapper.save(apiToken);
    //}

    public void updateApiTokenStatus(Long id, TokenStatus status, String updatedBy) {
        Optional<ApiToken> token = apiTokenMapper.findById(id);
        token.ifPresent(t -> {
            t.setStatus(status);
            t.setUpdatedBy(updatedBy);
            apiTokenMapper.save(t);
        });
    }

    public Optional<ApiToken> tryToFindTokenOrApplyRecord(ETraceUser user) {
        ApiToken apiToken = apiTokenMapper.findByUserEmail(user.getEmail());
        if (null == apiToken) {
            // 再查看是否有申请记录。
            List<ApplyTokenLog> tokenLogList = applyTokenLogService.findByParam(null, null, user.getEmail());
            if (tokenLogList.stream().anyMatch(t -> ApplyTokenAuditStatus.REFUSED != t.getAuditStatus())) {
                apiToken = new ApiToken();
                apiToken.setCreatedBy(user.getUsername());
                apiToken.setUpdatedBy(user.getUsername());
                apiToken.setUserEmail(user.getEmail());
                apiToken.setStatus(TokenStatus.WAIT_AUDIT);
            }
        }
        return Optional.ofNullable(apiToken);
    }

    private void checkAccessLevel(ApiToken apiToken) throws UnauthorizedException {
        if (ApiAccessStatus.none.name().equals(currentStatus)) {
            return;
        }
        if (ApiAccessStatus.white.name().equals(currentStatus)) {
            if (!apiToken.getIsAlwaysAccess()) {
                throw new UnauthorizedException("限制访问！");
            }
        }
        if (ApiAccessStatus.forbidden_all.name().equals(currentStatus)) {
            throw new UnauthorizedException("禁止所有请求");
        }
        if (!TokenStatus.Active.equals(apiToken.getStatus())) {
            throw new UnauthorizedException("token不可用");
        }
    }

}
