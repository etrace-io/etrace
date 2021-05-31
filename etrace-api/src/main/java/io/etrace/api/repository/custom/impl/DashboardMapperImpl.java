package io.etrace.api.repository.custom.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.etrace.api.model.po.ui.QDashboardPO;
import io.etrace.api.model.vo.ui.DashboardVO;
import io.etrace.api.repository.custom.DashboardMapperCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardMapperImpl implements DashboardMapperCustom {

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Override
    public int count(String title, Long departmentId, Long productLineId, String globalId, String user, String status) {

        QDashboardPO qDashboardPO = QDashboardPO.dashboardPO;
        List<Predicate> predicateList = buildQueryCondition(null, title, globalId, departmentId, productLineId, user,
            status);
        int count = jpaQueryFactory
            .select(qDashboardPO.id.count())
            .from(qDashboardPO)
            .where(predicateList.toArray(predicateList.toArray(new Predicate[0])))
            .fetchOne().intValue();
        return count;
    }

    @Override
    public List<DashboardVO> search(String title, Long departmentId, Long productLineId, String globalId, String user,
                                    String status, int start, int pageSize) {
        QDashboardPO qDashboardPO = QDashboardPO.dashboardPO;
        List<Predicate> predicateList = buildQueryCondition(null, title, globalId, departmentId, productLineId, user,
            status);
        return jpaQueryFactory
            .select(qDashboardPO)
            .from(qDashboardPO)
            .where(predicateList.toArray(predicateList.toArray(new Predicate[0])))
            .orderBy(qDashboardPO.id.desc())
            .offset(start)
            .limit(pageSize)
            .fetch().stream().map(DashboardVO::toVO).collect(
                Collectors.toList());
    }

    @Override
    public List<DashboardVO> findByIds(String title, Long departmentId, Long productLineId, List<Long> dashboardIds) {
        QDashboardPO qDashboardPO = QDashboardPO.dashboardPO;
        List<Predicate> predicateList = buildQueryCondition(dashboardIds, title, null, departmentId, productLineId,
            null, null);

        return jpaQueryFactory
            .select(qDashboardPO)
            .from(qDashboardPO)
            .where(predicateList.toArray(predicateList.toArray(new Predicate[0])))
            .fetch().stream().map(DashboardVO::toVO).collect(
                Collectors.toList());
    }

    private List<Predicate> buildQueryCondition(List<Long> idList, String title, String globalId, Long departmentId,
                                                Long productLineId, String user, String status) {
        List<com.querydsl.core.types.Predicate> predicateList = new ArrayList<>();
        QDashboardPO qDashboardPO = QDashboardPO.dashboardPO;
        if (!CollectionUtils.isEmpty(idList)) {
            predicateList.add(qDashboardPO.id.in(idList));
        }
        if (!StringUtils.isEmpty(title)) {
            predicateList.add(qDashboardPO.title.contains(title));
        }
        if (!StringUtils.isEmpty(globalId)) {
            predicateList.add(qDashboardPO.globalId.eq(globalId));
        }
        //if (null != departmentId) {
        //    predicateList.add(qDashboardPO.departmentId.eq(departmentId));
        //}
        //if (null != productLineId) {
        //    predicateList.add(qDashboardPO.productLineId.eq(productLineId));
        //}
        if (!StringUtils.isEmpty(user)) {
            predicateList.add(qDashboardPO.createdBy.eq(user).or(qDashboardPO.updatedBy.eq(user)));
        }
        if (!StringUtils.isEmpty(status)) {
            predicateList.add(qDashboardPO.status.eq(status));
        }
        return predicateList;
    }
}
