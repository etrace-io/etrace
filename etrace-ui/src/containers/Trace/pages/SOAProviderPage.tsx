import StoreManager from "$store/StoreManager";
import {Card, Checkbox, Row, Space} from "antd";
import {UnitModelEnum} from "$models/UnitModel";
import {COUNT, RT} from "../../app/EMonitorApp";
import {MetricVariate} from "$models/BoardModel";
import React, {useCallback, useEffect, useState} from "react";
import {Target, Targets} from "$models/ChartModel";
import MetricStatList from "$components/StatList/MetricStatList";
import {EMonitorGallery, EMonitorSection} from "$components/EMonitorLayout";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {
    APP_ID,
    SOA_PROVIDER_AVG_GLOBAL_ID,
    SOA_PROVIDER_RATE_GLOBAL_ID,
    SOA_PROVIDER_STATUS_GLOBAL_ID
} from "$constants/index";
import ObserverChart from "$components/EMonitorChart/Chart/ObserverChart/ObserverChart";
import EMonitorChart from "$components/EMonitorChart/EMonitorChart";

const PROVIDER_EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "soa_provider",
    null,
    null,
    APP_ID);
const PROVIDER_STATUS: MetricVariate = new MetricVariate(
    "状态",
    "result",
    "application",
    "soa_provider",
    null,
    null,
    APP_ID
);
const PROVIDER_CLUSTER: MetricVariate = new MetricVariate(
    "Cluster",
    "cluster",
    "application",
    "soa_provider_host",
    null,
    null,
    APP_ID
);
const PROVIDER_HOST: MetricVariate = new MetricVariate(
    "Host",
    "hostName",
    "application",
    "soa_provider_host",
    ["ezone", "cluster", "result"],
    null,
    APP_ID
);

const SOAProviderAvgStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerSum)/t_sum(timerCount)"],
    groupBy: ["method"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.soa_provider_host",
    measurementVars: ["appId"],
    variate: ["ezone", "result", "cluster", "hostName"],
    statListUnit: UnitModelEnum.Milliseconds,
};

const SOAProviderCountStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerCount)"],
    groupBy: ["method"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.soa_provider_host",
    measurementVars: ["appId"],
    variate: ["ezone", "result", "cluster", "hostName"],
    statListUnit: UnitModelEnum.Short,
};

const SOAProviderPage: React.FC = props => {
    const [singleHost, setSingleHost] = useState<boolean>(() =>
        StoreManager.urlParamStore.getValue("hostName") !== null
    );
    // usePieChartClickObserver();

    useEffect(() => {
        if (!singleHost) { // 清除选择后的数据
            StoreManager.urlParamStore.changeURLParams({}, ["cluster", "hostName"]);
        }
    }, [singleHost]);

    const handlePieChartClick = useCallback(tags => {
        StoreManager.urlParamStore.changeURLParams(tags);
    }, []);

    const variates = singleHost
        ? [PROVIDER_EZONE, PROVIDER_STATUS, PROVIDER_CLUSTER, PROVIDER_HOST]
        : [PROVIDER_EZONE, PROVIDER_STATUS, PROVIDER_CLUSTER];

    const statList: Targets = {
        "avg": {label: RT, target: SOAProviderAvgStatList},
        "count": {label: COUNT, target: SOAProviderCountStatList}
    };

    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item>
                <Card size="small">
                    <Row justify="space-between" align="middle">
                        <Space size={16}>
                            <MultiVariateSelect variates={variates}/>
                        </Space>

                        <Checkbox checked={singleHost} onChange={e => setSingleHost(e.target.checked)}>
                            切换单机维度查看
                        </Checkbox>
                    </Row>
                </Card>
            </EMonitorSection.Item>

            <EMonitorSection fullscreen={true} mode="horizontal">
                <EMonitorSection.Item width="30%">
                    <MetricStatList
                        targets={statList}
                        inputPlaceholder="Input Method Name..."
                    />
                </EMonitorSection.Item>

                <EMonitorSection.Item scroll={true}>
                    <EMonitorGallery>
                        {singleHost ? (<>
                            <EMonitorChart
                                key="application_soa_provider_avg_by_host"
                                globalId="application_soa_provider_avg_by_host"
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="timer"
                            />
                            <EMonitorChart
                                key="application_soa_provider_qps_by_host"
                                globalId="application_soa_provider_qps_by_host"
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="timer"
                            />
                        </>) : (<>
                            {/*<EditableChart*/}
                            {/*    key={SOA_PROVIDER_AVG_GLOBAL_ID}*/}
                            {/*    globalId={SOA_PROVIDER_AVG_GLOBAL_ID}*/}
                            {/*    span={24}*/}
                            {/*    prefixKey={APP_ID}*/}
                            {/*    metricType="timer"*/}
                            {/*/>*/}
                            <EMonitorChart
                                key={SOA_PROVIDER_AVG_GLOBAL_ID}
                                globalId={SOA_PROVIDER_AVG_GLOBAL_ID}
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="timer"
                            />
                            <EMonitorChart
                                key={SOA_PROVIDER_STATUS_GLOBAL_ID}
                                globalId={SOA_PROVIDER_STATUS_GLOBAL_ID}
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="timer"
                            />
                            <EMonitorChart
                                key={SOA_PROVIDER_RATE_GLOBAL_ID}
                                globalId={SOA_PROVIDER_RATE_GLOBAL_ID}
                                span={24}
                                prefixVariate={APP_ID}
                            />
                            <EMonitorChart
                                key="application_soa_provider_cluster"
                                globalId="application_soa_provider_cluster"
                                span={24}
                                prefixVariate={APP_ID}
                                onPieChartClick={handlePieChartClick}
                            />
                            <EMonitorChart
                                key="application_soa_provider_count"
                                globalId="application_soa_provider_count"
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="timer"
                            />
                            <ObserverChart
                                key="application_soa_provider_cluster_as_lines"
                                globalId="application_soa_provider_cluster_as_lines"
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="timer"
                                dependencyKeys="cluster"
                            />
                            <ObserverChart
                                key="application_soa_provider_cluster_as_count"
                                globalId="application_soa_provider_cluster_as_count"
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="timer"
                                dependencyKeys="cluster"
                            />
                            <ObserverChart
                                key="application_soa_provider_host"
                                globalId="application_soa_provider_host"
                                span={24}
                                prefixVariate={APP_ID}
                                dependencyKeys="cluster"
                                onPieChartClick={handlePieChartClick}
                            />
                            <ObserverChart
                                key="application_soa_provider_time_pre_host"
                                globalId="application_soa_provider_time_pre_host"
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="timer"
                                dependencyKeys="hostName"
                            />
                            <ObserverChart
                                key="application_soa_provider_count_pre_host"
                                globalId="application_soa_provider_count_pre_host"
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="timer"
                                dependencyKeys="hostName"
                            />
                        </>)}
                    </EMonitorGallery>
                </EMonitorSection.Item>
            </EMonitorSection>
        </EMonitorSection>
    );
};

export default SOAProviderPage;
