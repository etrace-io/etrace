package io.etrace.api.repository;

import io.etrace.api.model.po.ui.Node;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeMapper extends CrudRepository<Node, Long> {

    /**
     * 更新Node中的所有ChartIds
     *
     * @param node
     */
    void updateChartIds(Node node);
}
