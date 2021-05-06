import React from "react";
import {Card, Space} from "antd";
import {UnitModelEnum} from "$models/UnitModel";
import {COUNT, RT} from "../../app/EMonitorApp";
import {MetricVariate} from "$models/BoardModel";
import {Target, Targets} from "$models/ChartModel";
import MetricStatList from "$components/StatList/MetricStatList";
import {EMonitorGallery, EMonitorSection} from "$components/EMonitorLayout";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {APP_ID, SOA_CONSUMER_AVG_GLOBAL_ID, SOA_CONSUMER_STATUS_GLOBAL_ID} from "$constants/index";
import EMonitorChart from "$components/EMonitorChart/EMonitorChart";

const CONSUMER_STATUS: MetricVariate = new MetricVariate(
    "状态",
    "result",
    "application",
    "soa_consumer",
    null,
    null,
    APP_ID
);
const CONSUMER_EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "soa_consumer",
    null,
    null,
    APP_ID
);
const CONSUMER_CLUSTER: MetricVariate = new MetricVariate(
    "Cluster",
    "cluster",
    "application",
    "soa_consumer",
    null,
    null,
    APP_ID
);

const SOAConsumerAvgStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerSum)/t_sum(timerCount)"],
    groupBy: ["method"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.soa_consumer",
    measurementVars: ["appId"],
    variate: ["ezone", "result", "cluster"],
    statListUnit: UnitModelEnum.Milliseconds,
};

const SOAConsumerCountStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerCount)"],
    groupBy: ["method"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.soa_consumer",
    measurementVars: ["appId"],
    variate: ["ezone", "result", "cluster"],
    statListUnit: UnitModelEnum.Short,
};

const SOAConsumerPage: React.FC = props => {
    const variates = [CONSUMER_EZONE, CONSUMER_STATUS, CONSUMER_CLUSTER];
    const statList: Targets = {
        "avg": {label: RT, target: SOAConsumerAvgStatList},
        "count": {label: COUNT, target: SOAConsumerCountStatList}
    };

    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item>
                <Card size="small">
                    <Space size={16}>
                        <MultiVariateSelect variates={variates}/>
                    </Space>
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
                        <EMonitorChart
                            globalId={SOA_CONSUMER_AVG_GLOBAL_ID}
                            span={24}
                            prefixVariate={APP_ID}
                            metricType="timer"
                        />
                        <EMonitorChart
                            globalId={SOA_CONSUMER_STATUS_GLOBAL_ID}
                            span={24}
                            prefixVariate={APP_ID}
                            metricType="timer"
                        />
                        <EMonitorChart
                            globalId="application_soa_consumer_provider"
                            span={24}
                            prefixVariate={APP_ID}
                            metricType="timer"
                        />
                        <EMonitorChart
                            globalId="application_soa_consumer_success_rate"
                            span={24}
                            prefixVariate={APP_ID}
                        />
                    </EMonitorGallery>
                </EMonitorSection.Item>
            </EMonitorSection>
        </EMonitorSection>
    );
};

export default SOAConsumerPage;
