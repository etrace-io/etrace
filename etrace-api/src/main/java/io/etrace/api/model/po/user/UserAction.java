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
import io.etrace.api.model.vo.ui.DashboardVO;
import io.etrace.api.util.JpaConverterJson;
import io.etrace.api.util.LongListTypeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Convert;
import javax.persistence.Entity;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class UserAction extends BasePersistentObject {

    private String userEmail;

    @Convert(converter = LongListTypeConverter.class)
    private List<Long> favoriteBoardIds;

    @Convert(converter = LongListTypeConverter.class)
    private List<Long> favoriteAppIds;

    @Convert(converter = LongListTypeConverter.class)
    private List<Long> favoriteApps;

    @Convert(converter = LongListTypeConverter.class)
    private List<Long> favoriteGraphIds;

    @Convert(converter = LongListTypeConverter.class)
    private List<Long> favoriteNodeIds;

    @Convert(converter = LongListTypeConverter.class)
    private List<Long> viewBoardIds;
    @Convert(converter = JpaConverterJson.class)
    private List<DashboardVO> viewBoards;
    @Convert(converter = JpaConverterJson.class)
    private List<DashboardVO> favoriteBoards;

    @Convert(converter = LongListTypeConverter.class)
    private List<Long> viewNodeIds;
    @Convert(converter = LongListTypeConverter.class)
    private List<Long> viewGraphIds;

}
