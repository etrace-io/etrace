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

import io.etrace.api.consts.TokenStatus;
import io.etrace.api.model.po.BasePersistentObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ApiToken extends BasePersistentObject {

    private String cid;
    private String token;

    @Column(unique = true, nullable = false)
    private String userEmail;

    private TokenStatus status;
    /**
     * 是否始终允许访问
     */
    @Column(length = 1)
    private Boolean isAlwaysAccess;
    private String createdBy;
    private String updatedBy;

}
