import React from "react";
import {Card, Space} from "antd";
import {UnitModelEnum} from "$models/UnitModel";
import {COUNT, RT} from "../../app/EMonitorApp";
import {MetricVariate} from "$models/BoardModel";
import {Target, Targets} from "$models/ChartModel";
import MetricStatList from "$components/StatList/MetricStatList";
import EditableChart from "$components/EMonitorChart/Chart/EditableChart/EditableChart";
import {EMonitorGallery, EMonitorSection} from "$components/EMonitorLayout";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {APP_ID} from "$constants/index";

const STATUS: MetricVariate = new MetricVariate(
    "状态",
    "status",
    "application",
    "rmq_consume",
    null,
    null,
    APP_ID
);

const EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "rmq_consume",
    null,
    null,
    APP_ID
);

const VHOST: MetricVariate = new MetricVariate(
    "VHOST",
    "vhost",
    "application",
    "rmq_consume",
    null,
    null,
    APP_ID
);

const RMQConsumeAvgStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerSum)/t_sum(timerCount)"],
    groupBy: ["queue", "routingkey"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.rmq_consume",
    measurementVars: ["appId"],
    variate: ["ezone", "status", "vhost"],
    statListUnit: UnitModelEnum.Milliseconds
};

const RMQConsumeCountStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerCount)"],
    groupBy: ["queue", "routingkey"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.rmq_consume",
    measurementVars: ["appId"],
    variate: ["ezone", "status", "vhost"],
    statListUnit: UnitModelEnum.Short
};

const RMQConsumerPage: React.FC = props => {
    const variates = [EZONE, STATUS, VHOST];

    const statList: Targets = {
        "avg": {label: RT, target: RMQConsumeAvgStatList},
        "count": {label: COUNT, target: RMQConsumeCountStatList}
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
                <EMonitorSection.Item>
                    <MetricStatList
                        targets={statList}
                        inputPlaceholder="Input Queue or RoutingKey..."
                    />
                </EMonitorSection.Item>

                <EMonitorSection.Item scroll={true}>
                    <EMonitorGallery>
                        <EditableChart
                            globalId="application_consumer_time"
                            span={24}
                            metricType="timer"
                            prefixKey={APP_ID}
                        />
                        <EditableChart
                            globalId="application_consumer_count"
                            span={24}
                            metricType="timer"
                            prefixKey={APP_ID}
                        />
                        <EditableChart
                            globalId="application_consumer_producer"
                            span={24}
                            metricType="timer"
                            prefixKey={APP_ID}
                        />
                    </EMonitorGallery>
                </EMonitorSection.Item>
            </EMonitorSection>
        </EMonitorSection>
    );
};

export default RMQConsumerPage;
