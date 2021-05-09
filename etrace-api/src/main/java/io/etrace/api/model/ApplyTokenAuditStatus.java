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

package io.etrace.api.model;

public enum ApplyTokenAuditStatus {
    /**
     * 待审核
     */
    NOT_AUDIT("NOT_AUDIT"),
    /**
     * 同意
     */
    AGREE("AGREE"),
    /**
     * 拒绝
     */
    REFUSED("REFUSED");

    public String value;

    ApplyTokenAuditStatus(String value) {
        this.value = value;
    }

}
