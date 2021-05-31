import React from "react";
import {Card, Space} from "antd";
import {UnitModelEnum} from "$models/UnitModel";
import {COUNT, RT} from "../../app/EMonitorApp";
import {MetricVariate} from "$models/BoardModel";
import {Target, Targets} from "$models/ChartModel";
import MetricStatList from "$components/StatList/MetricStatList";
import {EMonitorGallery, EMonitorSection} from "$components/EMonitorLayout";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {APP_ID} from "$constants/index";
import EMonitorChart from "$components/EMonitorChart/EMonitorChart";

const STATUS: MetricVariate = new MetricVariate(
    "状态",
    "status",
    "application",
    "url",
    null,
    null,
    APP_ID
);
const EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "url",
    null,
    null,
    APP_ID
);

const urlAvgStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerSum)/t_sum(timerCount)"],
    groupBy: ["url"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.url",
    measurementVars: ["appId"],
    variate: ["ezone", "status"],
    statListUnit: UnitModelEnum.Milliseconds,
};

const urlCountStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerCount)"],
    groupBy: ["url"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.url",
    measurementVars: ["appId"],
    variate: ["ezone", "status"],
    statListUnit: UnitModelEnum.Short,
};

const TraceURLPage: React.FC = props => {
    const variates = [EZONE, STATUS];
    const statList: Targets = {
        "avg": {label: RT, target: urlAvgStatList},
        "count": {label: COUNT, target: urlCountStatList}
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
                        inputPlaceholder="Input url Name..."
                    />
                </EMonitorSection.Item>
                <EMonitorSection.Item scroll={true}>
                    <EMonitorGallery>
                        <EMonitorChart
                            globalId="application_url_avg"
                            span={24}
                            prefixVariate={APP_ID}
                            metricType="timer"
                        />
                        <EMonitorChart
                            globalId="application_url_count"
                            span={24}
                            prefixVariate={APP_ID}
                            metricType="timer"
                        />
                    </EMonitorGallery>
                </EMonitorSection.Item>
            </EMonitorSection>
        </EMonitorSection>
    );
};

export default TraceURLPage;
