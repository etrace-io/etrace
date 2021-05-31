package io.etrace.api.service;

import io.etrace.agent.Trace;
import io.etrace.api.model.ApplyTokenAuditStatus;
import io.etrace.api.model.TokenStatus;
import io.etrace.api.model.po.ui.ApiToken;
import io.etrace.api.model.po.ui.ApplyTokenLog;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.ApplyTokenLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ApplyTokenLogService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyTokenLogService.class);
    @Autowired
    private ApplyTokenLogMapper applyTokenLogMapper;

    @Autowired
    private ApiTokenService apiTokenService;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private UserService userService;

    @Transactional
    public Long create(ApplyTokenLog applyTokenLog, ETraceUser user) {
        applyTokenLogMapper.save(applyTokenLog);
        try {
            notifyService.notifyApiTokenRequest(user, applyTokenLog);
        } catch (Exception e) {
            LOGGER.error("==create==", e);
            Trace.logError(e);
        }
        return applyTokenLog.getId();
    }

    @Transactional
    public void auditApply(ApplyTokenAuditStatus applyTokenAuditStatus, String auditOpinion, String auditUser, Long id)
        throws Exception {
        Optional<ApplyTokenLog> applyTokenLogOp = applyTokenLogMapper.findById(id);
        if (!applyTokenLogOp.isPresent()) {
            throw new Exception("not found apply for id " + id);
        }
        ApplyTokenLog applyTokenLog = applyTokenLogOp.get();

        applyTokenLog.setAuditOpinion(auditOpinion);
        applyTokenLog.setUpdatedBy(auditUser);
        applyTokenLog.setAuditStatus(applyTokenAuditStatus);
        applyTokenLogMapper.save(applyTokenLog);
        if (ApplyTokenAuditStatus.AGREE.equals(applyTokenAuditStatus)) {
            ApiToken apiToken = new ApiToken();
            apiToken.setStatus(TokenStatus.Active);
            apiToken.setUserEmail(applyTokenLog.getUserEmail());
            apiToken.setCreatedBy(auditUser);
            apiToken.setUpdatedBy(auditUser);
            apiTokenService.create(apiToken);

            try {
                ETraceUser ETraceUser = userService.findByUserEmail(apiToken.getUserEmail());
                notifyService.notifyApiTokenApproved(ETraceUser, apiToken);
            } catch (Exception e) {
                LOGGER.error("==auditApply==", e);
                Trace.logError(e);
            }
        }
    }

    public List<ApplyTokenLog> findByParam(String auditStatus, String status, String userEmail) {
        return applyTokenLogMapper.findByAuditStatusAndStatusAndUserEmail(auditStatus, status, userEmail,
            Pageable.unpaged());
    }

    @Transactional
    public void delete(Long id) {
        applyTokenLogMapper.deleteById(id);
    }

    public SearchResult<ApplyTokenLog> search(String auditStatus, String status, String userEmail, int pageNum,
                                              int pageSize) {
        SearchResult<ApplyTokenLog> searchResult = new SearchResult<>();
        int count = applyTokenLogMapper.countByAuditStatusAndStatusAndUserEmail(auditStatus, status, userEmail);
        searchResult.setTotal(count);
        if (count > 0) {
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            List<ApplyTokenLog> applyTokenLogList = applyTokenLogMapper.findByAuditStatusAndStatusAndUserEmail(
                auditStatus, status, userEmail, pageable);
            searchResult.setResults(applyTokenLogList);
        }
        return searchResult;
    }
}
