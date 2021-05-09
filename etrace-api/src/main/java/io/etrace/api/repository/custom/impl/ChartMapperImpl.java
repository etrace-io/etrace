package io.etrace.api.repository.custom.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.etrace.api.model.vo.ui.ChartVO;
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
    public List<ChartVO> search(String title, String globalId, Long departmentId, Long productLineId, String user,
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
