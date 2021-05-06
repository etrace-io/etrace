package io.etrace.api.repository.custom.impl;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.etrace.api.convert.DataSourceConvert;
import io.etrace.api.model.po.ui.QMetricDataSourcePO;
import io.etrace.api.model.vo.DataSource;
import io.etrace.api.repository.custom.DataSourceMapperCustom;
import io.etrace.common.constant.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataSourceMapperImpl implements DataSourceMapperCustom {

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Override
    public int count(String type, String name, String status) {
        List<Predicate> predicateList = buildQueryCondition(type, name, status);
        QMetricDataSourcePO qMetricDataSourcePO = QMetricDataSourcePO.metricDataSourcePO;
        return jpaQueryFactory
            .select(qMetricDataSourcePO.id.count())
            .from(qMetricDataSourcePO)
            .where(predicateList.toArray(predicateList.toArray(new Predicate[0])))
            .fetchOne().intValue();
    }

    @Override
    public List<DataSource> search(String type, String name, String status, int start, int pageSize) {
        List<Predicate> predicateList = buildQueryCondition(type, name, status);
        QMetricDataSourcePO qMetricDataSourcePO = QMetricDataSourcePO.metricDataSourcePO;
        return jpaQueryFactory
            .select(qMetricDataSourcePO)
            .from(qMetricDataSourcePO)
            .where(predicateList.toArray(predicateList.toArray(new Predicate[0])))
            .offset(start)
            .limit(pageSize)
            .fetch().stream().map(dataSourcePO -> DataSourceConvert.convertToVO(dataSourcePO)).collect(
                Collectors.toList());
    }

    private List<Predicate> buildQueryCondition(String type, String name, String status) {
        List<com.querydsl.core.types.Predicate> predicateList = new ArrayList<>();
        QMetricDataSourcePO qMetricDataSourcePO = QMetricDataSourcePO.metricDataSourcePO;

        if (!StringUtils.isEmpty(type)) {
            predicateList.add(qMetricDataSourcePO.type.eq(type));
        }
        if (!StringUtils.isEmpty(status) && Status.forName(status) != null) {
            predicateList.add(qMetricDataSourcePO.status.eq(Status.forName(status)));
        }
        if (!StringUtils.isEmpty(name)) {
            predicateList.add(qMetricDataSourcePO.name.contains(name));
        }
        return predicateList;
    }
}
