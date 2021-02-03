package io.etrace.api.controller.ui;

import io.etrace.api.consts.ApplyTokenAuditStatus;
import io.etrace.api.consts.TokenStatus;
import io.etrace.api.controller.CurrentUser;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.model.po.ui.ApiToken;
import io.etrace.api.model.po.ui.ApplyTokenLog;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.service.ApiTokenService;
import io.etrace.api.service.ApplyTokenLogService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static io.etrace.api.config.SwaggerConfig.FOR_ETRACE;
import static io.etrace.api.config.SwaggerConfig.MYSQL_DATA;

@RestController
@RequestMapping("/token/")
@Api(tags = {FOR_ETRACE, MYSQL_DATA})
public class ApiTokenController {
    @Autowired
    private ApiTokenService apiTokenService;

    @Autowired
    private ApplyTokenLogService applyTokenLogService;

    @GetMapping("/user/findToken")
    public ApiToken findOwnerToken(@CurrentUser ETraceUser user) {
        Optional<ApiToken> op = apiTokenService.tryToFindTokenOrApplyRecord(user);
        return op.orElse(null);
    }

    @PostMapping(value = "/user/apply")
    public void apply(@CurrentUser ETraceUser user) throws BadRequestException {
        try {
            // avoid duplicated application
            if (apiTokenService.tryToFindTokenOrApplyRecord(user).isPresent()) {
                throw new BadRequestException("already applied or already had one token");
            } else {
                ApplyTokenLog applyTokenLog = new ApplyTokenLog();
                applyTokenLog.setCreatedBy(user.getUsername());
                applyTokenLog.setUpdatedBy(user.getUsername());
                applyTokenLog.setStatus(TokenStatus.Inactive);
                applyTokenLog.setAuditStatus(ApplyTokenAuditStatus.NOT_AUDIT);
                applyTokenLog.setUserEmail(user.getEmail());
                applyTokenLogService.create(applyTokenLog, user);
            }
        } catch (Exception e) {
            throw new BadRequestException("apply token failed！", e);
        }
    }

    @PostMapping(value = "/admin/audit")
    public void audit(@RequestBody ApplyTokenLog applyTokenLog, @CurrentUser ETraceUser user)
        throws BadRequestException {
        try {
            applyTokenLogService.auditApply(applyTokenLog.getAuditStatus(), applyTokenLog.getAuditOpinion(),
                user.getUsername(), applyTokenLog.getId());
        } catch (Exception e) {
            throw new BadRequestException("audit apply failed！", e);
        }
    }

    @GetMapping(value = "/admin/apply/search")
    public SearchResult<ApplyTokenLog> search(ApplyTokenAuditStatus auditStatus, @RequestParam TokenStatus status,
                                              String userCode,
                                              Integer pageNum, Integer pageSize) throws BadRequestException {
        try {
            return applyTokenLogService.search(
                auditStatus != null ? auditStatus.value : "", status != null ? status.value : "",
                userCode,
                Optional.ofNullable(pageNum).orElse(1),
                Optional.ofNullable(pageSize).orElse(100));
        } catch (Exception e) {
            throw new BadRequestException("search apply fail!！", e);
        }
    }

    @GetMapping(value = "/admin/all-token")
    public Iterable<ApiToken> search() {
        return apiTokenService.findAll();
    }

    @PostMapping(value = "/admin/update-token")
    public void updateToken(@RequestBody ApiToken apiToken, @CurrentUser ETraceUser user) {
        if (apiToken.getId() != null) {
            apiTokenService.updateApiTokenStatus(apiToken.getId(), apiToken.getStatus(), user.getUsername());
        }
    }

    // todo: 这里的API 参数改了，前端需要处理
    @PostMapping(value = "/admin/create-token")
    public ApiToken adminCreateToken(@RequestParam("email") String userEmail, @CurrentUser ETraceUser user)
        throws Exception {
        ApiToken apiToken = new ApiToken();
        apiToken.setUserEmail(userEmail);
        apiToken.setStatus(TokenStatus.Active);
        apiToken.setCreatedBy(user.getUsername());
        apiToken.setUpdatedBy(user.getUsername());
        return apiTokenService.create(apiToken);
    }
}
