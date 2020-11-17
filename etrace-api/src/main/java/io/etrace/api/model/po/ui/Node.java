/*
 * Copyright 2020 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.api.model.po.ui;

import io.etrace.api.model.graph.NodeType;
import io.etrace.api.model.po.BaseVisualizationObject;
import io.etrace.api.util.JpaConverterJson;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Convert;
import javax.persistence.Entity;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity(name = "node")
public class Node extends BaseVisualizationObject {

    public static final String GROUP_NODE_TYPE = "type";
    public static final String GROUP_NODE_APPID = "appId";
    public static final String GROUP_NODE_NAME = "nodeName";

    private NodeType nodeType;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> chartIds;
    @Convert(converter = JpaConverterJson.class)
    private List<Chart> charts;
    @Convert(converter = JpaConverterJson.class)

    private Object layout;
    @Convert(converter = JpaConverterJson.class)
    private Object config;

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
