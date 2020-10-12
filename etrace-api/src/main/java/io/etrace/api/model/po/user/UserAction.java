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

package io.etrace.api.model.po.user;

import io.etrace.api.model.po.BasePersistentObject;
import io.etrace.api.model.po.ui.Dashboard;
import io.etrace.api.model.po.ui.DashboardApp;
import io.etrace.api.model.po.ui.Graph;
import io.etrace.api.model.po.ui.Node;
import io.etrace.api.model.po.yellowpage.SearchList;
import io.etrace.api.model.po.yellowpage.SearchRecord;
import io.etrace.api.util.JpaConverterJson;
import lombok.Data;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.List;

@Data
@Entity
public class UserAction extends BasePersistentObject {

    private String userEmail;

    @Convert(converter = JpaConverterJson.class)
    private List<Long> favoriteBoardIds;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> viewBoardIds;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> favoriteApps;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> favoriteNodeIds;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> viewNodeIds;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> favoriteGraphIds;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> viewGraphIds;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> favoriteRecordIds;
    @Convert(converter = JpaConverterJson.class)
    private List<Long> favoriteListIds;

    @Transient
    private List<Dashboard> viewBoards;

    @Transient
    private List<Dashboard> favoriteBoards;
    @Transient
    private List<DashboardApp> apps;
    @Transient
    private List<Node> favoriteNodes;
    @Transient
    private List<Node> viewNodes;
    @Transient
    private List<Graph> favoriteGraphs;
    @Transient
    private List<Graph> viewGraphs;

    @Transient
    private List<SearchRecord> favoriteRecords;
    @Transient
    private List<SearchList> favoriteLists;

}
