/*
 * Copyright 2019 etrace.io
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

package io.etrace.common.message.metric;

import java.util.Map;

public class MetricHeaderV1 {

    Map<String, String> extraProperties;
    private String tenant;
    private String appId;
    private String hostIp;
    private String hostName;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Map<String, String> getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(Map<String, String> extraProperties) {
        this.extraProperties = extraProperties;
    }

    @Override
    public String toString() {
        return "MetricHeaderV1{" +
            "tenant='" + tenant + '\'' +
            ", appId='" + appId + '\'' +
            ", hostIp='" + hostIp + '\'' +
            ", hostName='" + hostName + '\'' +
            ", extraProperties=" + extraProperties +
            '}';
    }
}
