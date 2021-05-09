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

package io.etrace.api.model.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

// MappedSuperclass https://docs.spring.io/spring-data/jpa/docs/2.3.1.RELEASE/reference/html/#dependencies
@MappedSuperclass
@Data
@EqualsAndHashCode
public abstract class BasePersistentObject implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date updatedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date createdAt;



    private String createdBy;
    private String updatedBy;

}
