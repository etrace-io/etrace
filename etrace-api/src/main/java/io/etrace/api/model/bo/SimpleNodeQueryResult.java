package io.etrace.api.model.bo;

import io.etrace.api.model.MetricResult;
import io.etrace.api.model.vo.graph.NodeType;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SimpleNodeQueryResult {

    private Long id;
    private String title;
    private NodeType nodeType;
    private List<MetricResult> results;
    private Map<String, String> group;
}
