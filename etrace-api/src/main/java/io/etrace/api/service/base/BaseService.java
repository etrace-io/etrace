package io.etrace.api.service.base;

import io.etrace.api.consts.HistoryLogTypeEnum;
import io.etrace.api.exception.BadRequestException;
import io.etrace.api.exception.UserForbiddenException;
import io.etrace.api.model.po.BaseItem;
import io.etrace.api.model.po.ui.HistoryLog;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.UserAction;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.service.HistoryLogService;
import io.etrace.api.service.UserActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

public abstract class BaseService<VO extends BaseItem, T extends BaseItem> implements SyncMetricConfigService<VO>,
    FavoriteAndViewInterface {

    private final UserActionService.Callback callback;
    private final CrudRepository<T, Long> crudRepository;
    @Autowired
    protected HistoryLogService historyLogService;
    @Autowired
    protected UserActionService userActionService;

    public BaseService(CrudRepository<T, Long> crudRepository, UserActionService.Callback callback) {
        this.callback = callback;
        this.crudRepository = crudRepository;
    }

    public long create(T t) {
        if (StringUtils.isEmpty(t.getGlobalId())) {
            throw new RuntimeException("global id must not be null");
        }
        if (null == t.getAdminVisible()) {
            t.setAdminVisible(Boolean.FALSE);
        }
        crudRepository.save(t);
        return t.getId();
    }

    public void update(T t, ETraceUser user) throws BadRequestException, UserForbiddenException {
        if (t != null) {
            crudRepository.save(t);
            createHistoryLog(t, user, null, true);
        }
    }

    public Optional<T> findById(long id) {
        return crudRepository.findById(id);
    }

    public abstract T findById(long id, ETraceUser user);

    public Iterable<T> findByIds(List<Long> ids) {
        return crudRepository.findAllById(ids);
    }

    public abstract List<VO> findByIds(String title, List<Long> ids);

    public abstract <S extends T> void syncSonMetricConfig(S t, ETraceUser user);

    public void delete(long id, ETraceUser user) throws UserForbiddenException {
        createHistoryLog(crudRepository.findById(id).orElse(null), user, null, true);
        crudRepository.deleteById(id);
    }

    protected void createHistoryLog(T t, ETraceUser user, HistoryLogTypeEnum historyLogTypeEnum, boolean checkAdmin)
        throws UserForbiddenException {
        // if the dashboard is set to be editable by the administrator, verify that the current user have a role of
        // admin
        if (checkAdmin && t != null && Boolean.TRUE.equals(t.getAdminVisible()) && !user.isAdmin()) {
            throw new UserForbiddenException("no permission,the dashboard is set to the administrator to update！");
        }
        HistoryLog historyLog = new HistoryLog();
        historyLog.setHistory(t);
        historyLog.setCreatedBy(user.getUsername());
        historyLog.setUpdatedBy(user.getUsername());
        historyLog.setType(historyLogTypeEnum.name());
        historyLog.setHistoryId(t != null ? t.getId() : -1);
        historyLogService.create(historyLog);
    }

    public abstract SearchResult<VO> search(String title, String globalId, Integer pageNum, Integer pageSize,
                                            String user, String status);

    public void modelIsStar(ETraceUser user, List<VO> models) {
        if (user == null) {
            return;
        }
        UserAction userAction = userActionService.findFavoriteByUser(user);
        if (userAction != null) {
            List<Long> favorites = callback.get(userAction, false);
            if (favorites != null && !favorites.isEmpty()) {
                for (VO model : models) {
                    if (favorites.contains(model.getId())) {
                        model.setIsStar(true);
                    }
                }
            }
        }

    }

    //@Override
    public void syncMetricConfig(@NotNull T t, ETraceUser user) throws UserForbiddenException {
        //if (null == t) {
        //    throw new RuntimeException("the dashboard must not be null");
        //}
        //t.setUpdatedBy(user.getUsername());
        //t.setCreatedBy(user.getUsername());
        //
        //T oldT = findByGlobalId(t.getGlobalId());
        //if (null != oldT && Boolean.TRUE.equals(oldT.getAdminVisible()) && !user.isAdmin()) {
        //    throw new UserForbiddenException("no permission,the dashboard is set to the administrator to update！");
        //}
        //syncSonMetricConfig(t, user);
        //if (null == oldT) {
        //    //t.setFavoriteCount(0L);
        //    //t.setViewCount(0L);
        //    if (null == t.getAdminVisible()) {
        //        t.setAdminVisible(Boolean.FALSE);
        //    }
        //    create(t);
        //} else {
        //    t.setId(oldT.getId());
        //    createHistoryLog(oldT, user, null, false);
        //    try {
        //        update(t, t, user);
        //    } catch (BadRequestException e) {
        //        throw new RuntimeException("sync error!");
        //    }
        //}
    }
}
