import React, {useCallback} from "react";
import {Card} from "antd";
import {Target} from "$models/ChartModel";
import {UnitModelEnum} from "$models/UnitModel";
import {MetricVariate} from "$models/BoardModel";
import MetricStatList from "$components/StatList/MetricStatList";
import {EMonitorGallery, EMonitorSection} from "$components/EMonitorLayout";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {APP_ID} from "$constants/index";
import EMonitorChart from "$components/EMonitorChart/EMonitorChart";
import StoreManager from "$store/StoreManager";
import ObserverChart from "$components/EMonitorChart/Chart/ObserverChart/ObserverChart";

const STATUS: MetricVariate = new MetricVariate(
    "状态",
    "status",
    "application",
    "event",
    null,
    null,
    APP_ID
);
const EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "event",
    null,
    null,
    APP_ID
);

const variates = [STATUS, EZONE];
const statList: Target = {
    entity: "application",
    fields: ["t_sum(count)"],
    groupBy: ["type", "name"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.event",
    measurementVars: ["appId"],
    variate: ["ezone", "status"],
    statListUnit: UnitModelEnum.Short,
    tagFilters: [{op: "!=", key: "type", value: ["Reboot"], display: true}]
};

const EventPage: React.FC = props => {
    const handlePieChartClick = useCallback(tags => {
        StoreManager.urlParamStore.changeURLParams(tags);
    }, []);

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
                        target={statList}
                        inputPlaceholder="Input Type or Name..."
                    />
                </EMonitorSection.Item>
                <EMonitorSection.Item scroll={true}>
                    <EMonitorGallery>
                        <EMonitorChart
                            globalId="application_event_count"
                            span={24}
                            prefixVariate={APP_ID}
                            metricType="counter"
                        />
                        <EMonitorChart
                            globalId="application_event_host"
                            span={24}
                            prefixVariate={APP_ID}
                            onPieChartClick={handlePieChartClick}
                        />
                        <ObserverChart
                            globalId="application_event_count_pre_host"
                            span={24}
                            prefixVariate={APP_ID}
                            metricType="counter"
                            dependencyKeys="hostName"
                        />
                    </EMonitorGallery>
                </EMonitorSection.Item>
            </EMonitorSection>
        </EMonitorSection>
    );
};

export default EventPage;
