import React from "react";
import {Card, Space} from "antd";
import {MetricVariate} from "$models/BoardModel";
import MetricStatList from "$components/StatList/MetricStatList";
import {Target, Targets} from "$models/ChartModel";
import {UnitModelEnum} from "$models/UnitModel";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {COUNT, RT} from "../../app/EMonitorApp";
import {EMonitorGallery, EMonitorSection} from "$components/EMonitorLayout";
import usePieChartClickObserver from "$hooks/usePieChartClickObserver";
import {APP_ID} from "$constants/index";
import EMonitorChart from "$components/EMonitorChart/EMonitorChart";

const PROVIDER_EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "soa_dependency",
    null,
    null,
    APP_ID
);
const PROVIDER_STATUS: MetricVariate = new MetricVariate(
    "状态",
    "result",
    "application",
    "soa_dependency",
    null,
    null,
    APP_ID
);
const PROVIDER_METHOD: MetricVariate = new MetricVariate(
    "接口",
    "method",
    "application",
    "soa_dependency",
    null,
    null,
    APP_ID
);

const SOAProviderAvgStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerSum)/t_sum(timerCount)"],
    groupBy: ["serviceApp", "serviceMethod"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.soa_dependency",
    measurementVars: ["appId"],
    variate: ["ezone", "result", "method"],
    statListUnit: UnitModelEnum.Milliseconds,
};

const SOAProviderCountStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerCount)"],
    groupBy: ["serviceApp", "serviceMethod"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.soa_dependency",
    measurementVars: ["appId"],
    variate: ["ezone", "result", "method"],
    statListUnit: UnitModelEnum.Short,
};

const SOADependencyPage: React.FC = props => {
    usePieChartClickObserver();

    const variates = [PROVIDER_EZONE, PROVIDER_STATUS, PROVIDER_METHOD];
    const statList: Targets = {
        "avg": {label: RT, target: SOAProviderAvgStatList},
        "count": {label: COUNT, target: SOAProviderCountStatList}
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
                        inputPlaceholder="Input Service App or Service Method Name..."
                    />
                </EMonitorSection.Item>

                <EMonitorSection.Item scroll={true}>
                    <EMonitorGallery>
                        <EMonitorChart
                            globalId="bc873e05-acb2-411e-8485-a1715fee655f"
                            span={24}
                            prefixVariate={APP_ID}
                            metricType="timer"
                        />
                        <EMonitorChart
                            globalId="53f542ef-1e62-4b08-80dc-cc576dec6857"
                            span={24}
                            prefixVariate={APP_ID}
                            metricType="timer"
                        />
                        <EMonitorChart
                            globalId="6d6a8198-aa49-4872-a607-3540c4a9cb7f"
                            span={24}
                            prefixVariate={APP_ID}
                        />
                    </EMonitorGallery>
                </EMonitorSection.Item>
            </EMonitorSection>
        </EMonitorSection>
    );
};

export default SOADependencyPage;
