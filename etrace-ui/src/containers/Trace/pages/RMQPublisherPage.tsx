import React from "react";
import {Card} from "antd";
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
    "rmq_publish",
    null,
    null,
    APP_ID
);

const EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "rmq_publish",
    null,
    null,
    APP_ID
);

const VHOST: MetricVariate = new MetricVariate(
    "VHOST",
    "vhost",
    "application",
    "rmq_publish",
    null,
    null,
    APP_ID
);

const PublishAvgStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerSum)/t_sum(timerCount)"],
    groupBy: ["exchange", "routingkey"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.rmq_publish",
    measurementVars: ["appId"],
    variate: ["ezone", "status", "vhost"],
    statListUnit: UnitModelEnum.Milliseconds
};

const PublishCountStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerCount)"],
    groupBy: ["exchange", "routingkey"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.rmq_publish",
    measurementVars: ["appId"],
    variate: ["ezone", "status", "vhost"],
    statListUnit: UnitModelEnum.Short
};

const RMQPublisherPage: React.FC = props => {
    const variates = [EZONE, STATUS, VHOST];
    const statList: Targets = {
        "avg": {label: RT, target: PublishAvgStatList},
        "count": {label: COUNT, target: PublishCountStatList}
    };

    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item>
                <Card size="small">
                    <MultiVariateSelect variates={variates}/>
                </Card>
            </EMonitorSection.Item>

            <EMonitorSection fullscreen={true} mode="horizontal">
                <EMonitorSection.Item width="30%">
                    <MetricStatList
                        targets={statList}
                        inputPlaceholder="Input Exchange or RoutingKey..."
                    />
                </EMonitorSection.Item>

                <EMonitorSection.Item scroll={true}>
                    <EMonitorGallery>
                        <EditableChart
                            globalId="application_rmq_publish_time_avg"
                            span={24}
                            metricType="timer"
                            prefixKey={APP_ID}
                        />
                        <EditableChart
                            globalId="application_rmq_publish_count"
                            span={24}
                            metricType="timer"
                            prefixKey={APP_ID}
                        />
                        <EditableChart
                            globalId="application_rmq_publish_consumer"
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

export default RMQPublisherPage;
