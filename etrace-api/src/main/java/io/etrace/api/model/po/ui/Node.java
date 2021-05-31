package io.etrace.api.model.po.ui;

import io.etrace.api.model.po.BaseItem;
import io.etrace.api.model.vo.graph.NodeType;
import io.etrace.api.model.vo.ui.ChartVO;
import io.etrace.api.util.JpaConverterJson;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Convert;
import javax.persistence.Entity;
import java.util.List;
import java.util.Map;

@Data
@Entity(name = "node")
@EqualsAndHashCode(callSuper = true)
public class Node extends BaseItem {

    public static final String GROUP_NODE_TYPE = "type";
    public static final String GROUP_NODE_APPID = "appId";
    public static final String GROUP_NODE_NAME = "nodeName";

    private NodeType nodeType;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> chartIds;
    @Convert(converter = JpaConverterJson.class)
    private List<ChartPO> charts;
    private Object layout;
    private Object config;

    private String title;

    /**
     * for AppNode
     */
    private String appId;

    /**
     * for GroupNode
     */
    @Convert(converter = JpaConverterJson.class)
    private List<String> groupBy;
    @Convert(converter = JpaConverterJson.class)
    private Map<String, Object> singleNodeConfig;

}
