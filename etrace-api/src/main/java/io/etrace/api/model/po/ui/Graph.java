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

import io.etrace.api.model.graph.Relation;
import io.etrace.api.model.po.BaseVisualizationObject;
import io.etrace.api.util.JpaConverterJson;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Convert;
import javax.persistence.Entity;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity(name = "graph")
public class Graph extends BaseVisualizationObject {

    @Convert(converter = JpaConverterJson.class)
    private Object layout;
    @Convert(converter = JpaConverterJson.class)
    private Object config;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> nodeIds;

    @Convert(converter = JpaConverterJson.class)
    private List<Node> nodes;
    @Convert(converter = JpaConverterJson.class)
    private List<Relation> relations;
}
