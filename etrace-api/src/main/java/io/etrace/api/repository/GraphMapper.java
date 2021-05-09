package io.etrace.api.repository;

import io.etrace.api.model.po.ui.Graph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GraphMapper extends CrudRepository<Graph, Long> {

    /**
     * 更新Graph中的所有NodeIds
     *
     * @param graph
     */
    void updateNodeIds(Graph graph);

}
