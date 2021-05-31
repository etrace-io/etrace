import React from "react";
import {Card} from "antd";
import {UnitModelEnum} from "$models/UnitModel";
import {COUNT, RT} from "../../app/EMonitorApp";
import {MetricVariate} from "$models/BoardModel";
import {Target, Targets} from "$models/ChartModel";
import MetricStatList from "$components/StatList/MetricStatList";
import {EMonitorGallery, EMonitorSection} from "$components/EMonitorLayout";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {default as EditableChart} from "$components/EMonitorChart/Chart/EditableChart/EditableChart";
import {APP_ID} from "$constants/index";

const STATUS: MetricVariate = new MetricVariate(
    "状态",
    "status",
    "application",
    "redis_time",
    null,
    null,
    APP_ID
);
const EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "redis_time",
    null,
    null,
    APP_ID
);
const HOST: MetricVariate = new MetricVariate(
    "Host",
    "hostName",
    "application",
    "redis_time",
    null,
    null,
    APP_ID
);

const RedisAvgStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerSum)/t_sum(timerCount)"],
    groupBy: ["redis", "command", "hostName"],
    groupByKeys: ["redisName", "command"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.redis_time",
    measurementVars: ["appId"],
    variate: ["ezone", "status", "hostName"],
    statListUnit: UnitModelEnum.Milliseconds
};

const RedisCountStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerCount)"],
    groupBy: ["redis", "command", "hostName"],
    groupByKeys: ["redisName", "command"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.redis_time",
    measurementVars: ["appId"],
    variate: ["ezone", "status", "hostName"],
    statListUnit: UnitModelEnum.Short
};

const TraceRedisPage: React.FC = props => {
    const variates = [EZONE, STATUS, HOST];
    const statList: Targets = {
        "avg": {label: RT, target: RedisAvgStatList},
        "count": {label: COUNT, target: RedisCountStatList}
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
                        inputPlaceholder="Input Redis Name..."
                        keepStatList={true}
                    />
                </EMonitorSection.Item>

                <EMonitorSection.Item scroll={true}>
                    <EMonitorGallery>
                        <EditableChart
                            globalId="application_redis_time"
                            span={24}
                            metricType="timer"
                            prefixKey={APP_ID}
                        />
                        <EditableChart
                            globalId="application_redis_count"
                            span={24}
                            metricType="timer"
                            prefixKey={APP_ID}
                        />
                        <EditableChart
                            globalId="application_redis_hit_rate"
                            span={24}
                            prefixKey={APP_ID}
                        />
                        <EditableChart
                            globalId="application_redis_package"
                            span={24}
                            metricType="payload"
                            prefixKey={APP_ID}
                        />
                    </EMonitorGallery>
                </EMonitorSection.Item>
            </EMonitorSection>
        </EMonitorSection>
    );
};

export default TraceRedisPage;
