package io.etrace.api.controller.ui;

import io.etrace.api.controller.CurrentUser;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.BaseItem;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.service.base.BaseService;
import org.springframework.web.bind.annotation.RequestParam;

@Deprecated
public abstract class BaseController<VO extends BaseItem, T extends BaseItem> {

    private final BaseService<VO, T> baseService;

    public BaseController(BaseService<VO, T> baseService) {
        this.baseService = baseService;
    }

    public Long doCreate(T t, ETraceUser user) {
        t.setCreatedBy(user.getUsername());
        t.setUpdatedBy(user.getUsername());
        baseService.create(t);
        return t.getId();
    }

    public T doFindById(long id, ETraceUser user) {
        T t = baseService.findById(id, user);
        if (t != null) {
            createOrUpdateView(id, user);
            return t;
        }
        return null;
    }

    public void doUpdate(T t, ETraceUser user) throws BadRequestException, UserForbiddenException {
        t.setUpdatedBy(user.getUsername());
        baseService.update(t, user);
    }

    public void doDelete(long id, String status, ETraceUser user) throws Exception {
        baseService.delete(id, user);
    }

    public SearchResult<VO> doSearch(
        @RequestParam(value = "user", required = false) String user,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "globalId", required = false) String globalId,
        @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "current", defaultValue = "1") Integer pageNum,
        @RequestParam(value = "status", defaultValue = "Active") String status,
        @CurrentUser ETraceUser eTraceUser) {
        SearchResult<VO> result = baseService.search(title, globalId, pageNum, pageSize, user, status);
        if (result.getTotal() > 0) {
            baseService.modelIsStar(eTraceUser, result.getResults());
        }
        return result;
    }

    protected abstract void createOrUpdateView(long id, ETraceUser user);
}
