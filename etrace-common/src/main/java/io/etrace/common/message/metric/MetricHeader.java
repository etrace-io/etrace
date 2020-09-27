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

import java.util.Objects;

@Deprecated
public class MetricHeader {

    private String topic;
    private String appId;
    private String hostIp;
    private String hostName;
    private String cluster;
    private String ezone;
    private String idc;
    private String mesosTaskId;
    private String eleapposLabel;
    private String eleapposSlaveFqdn;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

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

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getEzone() {
        return ezone;
    }

    public void setEzone(String ezone) {
        this.ezone = ezone;
    }

    public String getIdc() {
        return idc;
    }

    public void setIdc(String idc) {
        this.idc = idc;
    }

    public String getMesosTaskId() {
        return mesosTaskId;
    }

    public void setMesosTaskId(String mesosTaskId) {
        this.mesosTaskId = mesosTaskId;
    }

    public String getEleapposLabel() {
        return eleapposLabel;
    }

    public void setEleapposLabel(String eleapposLabel) {
        this.eleapposLabel = eleapposLabel;
    }

    public String getEleapposSlaveFqdn() {
        return eleapposSlaveFqdn;
    }

    public void setEleapposSlaveFqdn(String eleapposSlaveFqdn) {
        this.eleapposSlaveFqdn = eleapposSlaveFqdn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof MetricHeader)) { return false; }
        MetricHeader that = (MetricHeader)o;
        return Objects.equals(topic, that.topic) &&
            Objects.equals(appId, that.appId) &&
            Objects.equals(hostIp, that.hostIp) &&
            Objects.equals(hostName, that.hostName) &&
            Objects.equals(cluster, that.cluster) &&
            Objects.equals(ezone, that.ezone) &&
            Objects.equals(idc, that.idc) &&
            Objects.equals(mesosTaskId, that.mesosTaskId) &&
            Objects.equals(eleapposLabel, that.eleapposLabel) &&
            Objects.equals(eleapposSlaveFqdn, that.eleapposSlaveFqdn);
    }

    @Override
    public int hashCode() {
        int result = topic != null ? topic.hashCode() : 0;
        result = 31 * result + (appId != null ? appId.hashCode() : 0);
        result = 31 * result + (hostIp != null ? hostIp.hashCode() : 0);
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        result = 31 * result + (cluster != null ? cluster.hashCode() : 0);
        result = 31 * result + (ezone != null ? ezone.hashCode() : 0);
        result = 31 * result + (idc != null ? idc.hashCode() : 0);
        result = 31 * result + (mesosTaskId != null ? mesosTaskId.hashCode() : 0);
        result = 31 * result + (eleapposLabel != null ? eleapposLabel.hashCode() : 0);
        result = 31 * result + (eleapposSlaveFqdn != null ? eleapposSlaveFqdn.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MetricHeader{" +
            "topic='" + topic + '\'' +
            ", appId='" + appId + '\'' +
            ", hostIp='" + hostIp + '\'' +
            ", hostName='" + hostName + '\'' +
            ", cluster='" + cluster + '\'' +
            ", ezone='" + ezone + '\'' +
            ", idc='" + idc + '\'' +
            ", mesosTaskId='" + mesosTaskId + '\'' +
            ", eleapposLabel='" + eleapposLabel + '\'' +
            ", eleapposSlaveFqdn='" + eleapposSlaveFqdn + '\'' +
            '}';
    }

}
