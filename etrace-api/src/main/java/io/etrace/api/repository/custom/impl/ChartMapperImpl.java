package io.etrace.api.repository.custom.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.etrace.api.convert.ChartConvert;
import io.etrace.api.model.po.ui.QChartPO;
import io.etrace.api.model.po.ui.QDepartmentPO;
import io.etrace.api.model.vo.Chart;
import io.etrace.api.model.vo.Department;
import io.etrace.api.repository.custom.ChartMapperCustom;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChartMapperImpl implements ChartMapperCustom {

    @PersistenceContext
    private EntityManager entityManager;

    //    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @PostConstruct
    public void init() {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public int count(String title, String globalId, Long departmentId, Long productLineId, String user, String status) {
        List<com.querydsl.core.types.Predicate> predicateList = buildQueryCondition(title, globalId, departmentId,
            productLineId, user, status);
        QChartPO qChartPO = QChartPO.chartPO;
        return jpaQueryFactory
            .select(qChartPO.id.count())
            .from(qChartPO)
            .where(predicateList.toArray(new Predicate[] {}))
            .fetchOne().intValue();
    }

    @Override
    public List<Chart> search(String title, String globalId, Long departmentId, Long productLineId, String user,
                              int start, int pageSize, String status) {
        List<com.querydsl.core.types.Predicate> predicateList = buildQueryCondition(title, globalId, departmentId,
            productLineId, user, status);
        QChartPO qChartPO = QChartPO.chartPO;
        QDepartmentPO qDepartmentPO = QDepartmentPO.departmentPO;
        QDepartmentPO qDepartmentPO2 = QDepartmentPO.departmentPO;
        return jpaQueryFactory
            .select(qChartPO)
            .from(qChartPO)
            .leftJoin(qDepartmentPO).on(qChartPO.departmentId.eq(qDepartmentPO.id))
            .leftJoin(qDepartmentPO2).on(qChartPO.productLineId.eq(qDepartmentPO2.id))
            .where(predicateList.toArray(new Predicate[] {}))
            .orderBy(qChartPO.id.desc())
            .offset(start)
            .limit(pageSize)
            .fetch().stream().map(chartPO -> ChartConvert.convertToVO(chartPO)).collect(Collectors.toList());
    }

    @Override
    public int countChartCountByDepartment(Long departmentId, String status, String user) {
        return count(null, null, departmentId, null, user, status);
    }

    @Override
    public int countChartCountByProductline(Long productLineId, String status, String user) {
        return count(null, null, null, productLineId, user, status);
    }

    @Override
    public List<Department> findChartGroupByDepartment(String status, String user) {
        List<com.querydsl.core.types.Predicate> predicateList = buildQueryCondition(null, null, null, null, user,
            status);
        QChartPO qChartPO = QChartPO.chartPO;
        QDepartmentPO departmentPO = QDepartmentPO.departmentPO;
        return jpaQueryFactory
            .select(
                qChartPO.departmentId,
                qChartPO.departmentId.count(),
                departmentPO.name)
            .from(qChartPO)
            .groupBy(qChartPO.departmentId)
            .where(
                predicateList.toArray(new com.querydsl.core.types.Predicate[] {})
            ).leftJoin(departmentPO)
            .on(qChartPO.departmentId.eq(departmentPO.id)).fetch().stream().map(tuple -> {
                Department department = new Department();
                department.setId(tuple.get(qChartPO.departmentId));
                department.setDepartmentName(tuple.get(departmentPO.name));
                department.setDepartmentId(department.getId());
                department.setCount(tuple.get(qChartPO.departmentId.count()).intValue());
                return department;
            }).collect(Collectors.toList());
    }

    @Override
    public List<Department> findChartGroupByProductLine(Long departmentId, String status, String user) {

        List<com.querydsl.core.types.Predicate> predicateList = buildQueryCondition(null, null, departmentId, null,
            user, status);
        QChartPO qChartPO = QChartPO.chartPO;
        QDepartmentPO departmentPO = QDepartmentPO.departmentPO;
        return jpaQueryFactory
            .select(
                qChartPO.productLineId,
                qChartPO.productLineId.count(),
                departmentPO.name)
            .from(qChartPO)
            .groupBy(qChartPO.productLineId)
            .where(
                predicateList.toArray(new com.querydsl.core.types.Predicate[] {})
            ).leftJoin(departmentPO)
            .on(qChartPO.productLineId.eq(departmentPO.id)).fetch().stream().map(tuple -> {
                Department department = new Department();
                department.setId(tuple.get(qChartPO.productLineId));
                department.setDepartmentName(tuple.get(departmentPO.name));
                department.setDepartmentId(department.getId());
                department.setCount(tuple.get(qChartPO.productLineId.count()).intValue());
                return department;
            }).collect(Collectors.toList());
    }

    private List<Predicate> buildQueryCondition(String title, String globalId, Long departmentId, Long productLineId,
                                                String user, String status) {
        List<com.querydsl.core.types.Predicate> predicateList = new ArrayList<>();
        QChartPO qChartPO = QChartPO.chartPO;
        if (!StringUtils.isEmpty(title)) {
            predicateList.add(qChartPO.title.contains(title));
        }
        if (!StringUtils.isEmpty(globalId)) {
            predicateList.add(qChartPO.globalId.eq(globalId));
        }
        if (null != departmentId) {
            predicateList.add(qChartPO.departmentId.eq(departmentId));
        }
        if (null != productLineId) {
            predicateList.add(qChartPO.productLineId.eq(productLineId));
        }
        if (!StringUtils.isEmpty(user)) {
            predicateList.add(qChartPO.createdBy.eq(user).or(qChartPO.updatedBy.eq(user)));
        }
        if (!StringUtils.isEmpty(status)) {
            predicateList.add(qChartPO.status.eq(status));
        }
        return predicateList;
    }
}
