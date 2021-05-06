package io.etrace.api.repository.custom.impl;//package io.etrace.api.repository.custom.impl;
//
//import com.querydsl.core.Tuple;
//import com.querydsl.core.types.Predicate;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import io.etrace.api.model.po.ui.MonitorEntity;
//import io.etrace.api.model.po.ui.QMetricDataSourcePO;
//import io.etrace.api.model.po.ui.QMonitorEntity;
//import io.etrace.api.repository.custom.MonitorEntityMapperCustom;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//public class MonitorEntityMapperImpl implements MonitorEntityMapperCustom {
//
//    @Autowired
//    private JPAQueryFactory jpaQueryFactory;
//
//    @Override
//    public List<MonitorEntity> findAllByParentId(long parentId) {
//        QMonitorEntity monitorEntity = QMonitorEntity.monitorEntity;
//        List<Predicate> predicateList = new ArrayList<>();
//
//        predicateList.add(monitorEntity.parentId.eq(parentId));
//        QMetricDataSourcePO qMetricDataSourcePO = QMetricDataSourcePO.metricDataSourcePO;
//        List<Tuple> fetch = jpaQueryFactory
//                .select(monitorEntity, qMetricDataSourcePO)
//                .from(monitorEntity)
//                .leftJoin(qMetricDataSourcePO).on(monitorEntity.datasourceId.eq(qMetricDataSourcePO.id))
//                .where(predicateList.toArray(new Predicate[]{}))
//                .fetch();
//
//        System.out.println(fetch);
//
//        return null;
//    }
//
//    @Override
//    public List<MonitorEntity> findByTypeAndStatus(String type, String status) {
//        return null;
//    }
//
//    @Override
//    public Optional<MonitorEntity> findByCode(String code) {
//        return Optional.empty();
//    }
//}
