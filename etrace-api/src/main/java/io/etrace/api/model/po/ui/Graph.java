package io.etrace.api.model.po.ui;

import io.etrace.api.model.po.BasePersistentObject;
import io.etrace.api.model.vo.graph.Relation;
import io.etrace.api.util.JpaConverterJson;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Convert;
import javax.persistence.Entity;
import java.util.List;

@Data
@Entity(name = "graph")
@EqualsAndHashCode(callSuper = true)
public class Graph extends BasePersistentObject {

    private Object layout;
    private Object config;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> nodeIds;
    @Convert(converter = JpaConverterJson.class)
    private List<Node> nodes;
    @Convert(converter = JpaConverterJson.class)
    private List<Relation> relations;
}
