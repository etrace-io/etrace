package io.etrace.api.repository.custom.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.etrace.api.convert.DashboardAppConvert;
import io.etrace.api.model.po.ui.DashboardAppPO;
import io.etrace.api.model.po.ui.QDashboardAppPO;
import io.etrace.api.model.po.ui.QDepartmentPO;
import io.etrace.api.model.vo.DashboardApp;
import io.etrace.api.model.vo.Department;
import io.etrace.api.repository.custom.DashboardAppMapperCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardAppMapperImpl implements DashboardAppMapperCustom {

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Override
    public List<DashboardApp> findByIds(String title, List<Long> ids) {

        List<com.querydsl.core.types.Predicate> predicateList = buildQueryCondition(ids, title, null, null, null, null,
            null);
        QDashboardAppPO qDashboardAppPO = QDashboardAppPO.dashboardAppPO;
        List<DashboardAppPO> dashboardAppPOS = jpaQueryFactory.select(qDashboardAppPO).from(qDashboardAppPO).where(
            predicateList.toArray(predicateList.toArray(new Predicate[0]))).fetch();
        return DashboardAppConvert.convertToVOs(dashboardAppPOS);
    }

    @Override
    public int count(String title, Long departmentId, Long productLineId, String user, Boolean critical) {
        QDashboardAppPO qDashboardAppPO = QDashboardAppPO.dashboardAppPO;
        List<Predicate> predicateList = buildQueryCondition(null, title, departmentId, productLineId, user, null,
            critical);
        return jpaQueryFactory
            .select(qDashboardAppPO.id.count())
            .from(qDashboardAppPO)
            .where(predicateList.toArray(predicateList.toArray(new Predicate[0])))
            .fetchOne().intValue();
    }

    @Override
    public List<DashboardApp> search(String title, Long departmentId, Long productLineId, String user, Boolean critical,
                                     int start, int pageSize) {
        List<Predicate> predicateList = buildQueryCondition(null, title, departmentId, productLineId, user, null,
            critical);
        QDashboardAppPO qDashboardAppPO = QDashboardAppPO.dashboardAppPO;
        return jpaQueryFactory
            .select(qDashboardAppPO)
            .from(qDashboardAppPO)
            .where(predicateList.toArray(predicateList.toArray(new Predicate[0])))
            .orderBy(qDashboardAppPO.id.desc())
            .offset(start)
            .limit(pageSize)
            .fetch()
            .stream()
            .map(DashboardAppPO -> DashboardAppConvert.convertToVO(DashboardAppPO))
            .collect(Collectors.toList());
    }

    @Override
    public List<DashboardApp> settingSearch(String title, Boolean critical) {
        List<com.querydsl.core.types.Predicate> predicateList = buildQueryCondition(null, title, null, null, null, null,
            critical);
        QDashboardAppPO qDashboardAppPO = QDashboardAppPO.dashboardAppPO;
        List<DashboardAppPO> dashboardAppPOS = jpaQueryFactory.select(qDashboardAppPO).from(qDashboardAppPO).where(
            predicateList.toArray(predicateList.toArray(new Predicate[0]))).fetch();
        return DashboardAppConvert.convertToVOs(dashboardAppPOS);
    }

    @Override
    public int countDashboardAppCountByDepartment(Long departmentId, String status, String user,
                                                  List<Long> dashboardAppIdList) {
        QDashboardAppPO qDashboardAppPO = QDashboardAppPO.dashboardAppPO;
        List<Predicate> predicateList = buildQueryCondition(dashboardAppIdList, null, departmentId, null, user, status,
            null);
        return jpaQueryFactory
            .select(qDashboardAppPO.id.count())
            .from(qDashboardAppPO)
            .where(predicateList.toArray(predicateList.toArray(new Predicate[0])))
            .fetchOne().intValue();

    }

    @Override
    public int countDashboardAppCountByProductline(Long productLineId, String status, String user,
                                                   List<Long> dashboardAppIdList) {
        QDashboardAppPO qDashboardAppPO = QDashboardAppPO.dashboardAppPO;
        List<Predicate> predicateList = buildQueryCondition(dashboardAppIdList, null, null, productLineId, user, status,
            null);
        return jpaQueryFactory
            .select(qDashboardAppPO.id.count())
            .from(qDashboardAppPO)
            .where(predicateList.toArray(predicateList.toArray(new Predicate[0])))
            .fetchOne().intValue();
    }

    @Override
    public List<Department> findDashboardAppGroupByDepartment(String status, String user, List<Long> favoriteApps) {
        List<com.querydsl.core.types.Predicate> predicateList = buildQueryCondition(favoriteApps, null, null, null,
            user, status, null);
        QDashboardAppPO qDashboardAppPO = QDashboardAppPO.dashboardAppPO;
        QDepartmentPO departmentPO = QDepartmentPO.departmentPO;
        return jpaQueryFactory.select(qDashboardAppPO.departmentId,
            qDashboardAppPO.departmentId.count(),
            departmentPO.name)
            .from(qDashboardAppPO)
            .groupBy(qDashboardAppPO.departmentId)
            .where(
                predicateList.toArray(new com.querydsl.core.types.Predicate[] {})
            ).leftJoin(departmentPO)
            .on(qDashboardAppPO.departmentId.eq(departmentPO.id)).fetch().stream().map(tuple -> {
                Department department = new Department();
                department.setId(tuple.get(qDashboardAppPO.departmentId));
                department.setDepartmentName(tuple.get(departmentPO.name));
                department.setDepartmentId(department.getId());
                department.setCount(tuple.get(qDashboardAppPO.departmentId.count()).intValue());
                return department;
            }).collect(Collectors.toList());
    }

    @Override
    public List<Department> findDashboardAppGroupByProductLine(Long departmentId, String status, String user,
                                                               List<Long> favoriteApps) {
        List<com.querydsl.core.types.Predicate> predicateList = buildQueryCondition(favoriteApps, null, departmentId,
            null, user, status, null);
        QDashboardAppPO qDashboardAppPO = QDashboardAppPO.dashboardAppPO;
        QDepartmentPO departmentPO = QDepartmentPO.departmentPO;
        return jpaQueryFactory.select(qDashboardAppPO.productLineId,
            qDashboardAppPO.productLineId.count(),
            departmentPO.name)
            .from(qDashboardAppPO)
            .groupBy(qDashboardAppPO.productLineId)
            .where(
                predicateList.toArray(new com.querydsl.core.types.Predicate[] {})
            ).leftJoin(departmentPO)
            .on(qDashboardAppPO.productLineId.eq(departmentPO.id)).fetch().stream().map(tuple -> {
                Department department = new Department();
                department.setId(tuple.get(qDashboardAppPO.productLineId));
                department.setDepartmentName(tuple.get(departmentPO.name));
                department.setDepartmentId(department.getId());
                department.setCount(tuple.get(qDashboardAppPO.productLineId.count()).intValue());
                return department;
            }).collect(Collectors.toList());
    }

    private List<Predicate> buildQueryCondition(List<Long> idList, String title, Long departmentId, Long productLineId,
                                                String user, String status, Boolean critical) {
        List<com.querydsl.core.types.Predicate> predicateList = new ArrayList<>();
        QDashboardAppPO qDashboardAppPO = QDashboardAppPO.dashboardAppPO;
        if (!CollectionUtils.isEmpty(idList)) {
            predicateList.add(qDashboardAppPO.id.in(idList));
        }
        if (!StringUtils.isEmpty(title)) {
            predicateList.add(qDashboardAppPO.title.contains(title));
        }
        if (null != critical) {
            predicateList.add(qDashboardAppPO.critical.eq(critical));
        }
        if (null != departmentId) {
            predicateList.add(qDashboardAppPO.departmentId.eq(departmentId));
        }
        if (null != productLineId) {
            predicateList.add(qDashboardAppPO.productLineId.eq(productLineId));
        }
        if (!StringUtils.isEmpty(user)) {
            predicateList.add(qDashboardAppPO.createdBy.eq(user).or(qDashboardAppPO.updatedBy.eq(user)));
        }
        if (!StringUtils.isEmpty(status)) {
            predicateList.add(qDashboardAppPO.status.eq(status));
        }
        return predicateList;
    }
}
