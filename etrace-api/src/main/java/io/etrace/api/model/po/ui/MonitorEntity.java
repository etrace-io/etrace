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

import io.etrace.api.model.po.BasePersistentObject;
import io.etrace.api.util.JpaConverterJson;
import io.etrace.common.constant.Status;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)

@Entity
public class MonitorEntity extends BasePersistentObject {
    private long parentId;
    private String type;
    private String name;
    private String code;
    private Long datasourceId;

    @Transient
    private MetricDataSource datasource;

    @Column(name = "`database`")
    private String database;
    private String metaUrl;
    private String metaLink;
    private String config;
    private String metaPlaceholder;
    @Convert(converter = JpaConverterJson.class)
    private List<MonitorEntity> children;
    private String status = Status.Active.name();
}
