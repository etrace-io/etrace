import {Card} from "antd";
import React, {useCallback} from "react";
import {UnitModelEnum} from "$models/UnitModel";
import {COUNT, RT} from "../../app/EMonitorApp";
import {MetricVariate} from "$models/BoardModel";
import {Target, Targets} from "$models/ChartModel";
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
    "transaction",
    null,
    null,
    APP_ID
);

const EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "transaction",
    null,
    null,
    APP_ID
);

const transactionAvgStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerSum)/t_sum(timerCount)"],
    groupBy: ["type", "name"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.transaction",
    measurementVars: ["appId"],
    variate: ["ezone", "status"],
    statListUnit: UnitModelEnum.Milliseconds,
};

const transactionCountStatList: Target = {
    entity: "application",
    fields: ["t_sum(timerCount)"],
    groupBy: ["type", "name"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "${appId}.transaction",
    measurementVars: ["appId"],
    variate: ["ezone", "status"],
    statListUnit: UnitModelEnum.Short,
};

const TransactionPage: React.FC = props => {
    const variates = [EZONE, STATUS];
    const statList: Targets = {
        "avg": {label: RT, target: transactionAvgStatList},
        "count": {label: COUNT, target: transactionCountStatList}
    };

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
                        targets={statList}
                        inputPlaceholder={"Input Type or Name..."}
                    />
                </EMonitorSection.Item>
                <EMonitorSection.Item scroll={true}>
                    <EMonitorGallery>
                        <EMonitorChart
                            globalId="application_transaction_avg"
                            span={24}
                            metricType="timer"
                            prefixVariate={APP_ID}
                        />
                        <EMonitorChart
                            globalId="application_transaction_count"
                            span={24}
                            metricType="timer"
                            prefixVariate={APP_ID}
                        />
                        <EMonitorChart
                            globalId="application_transaction_host"
                            span={24}
                            prefixVariate={APP_ID}
                            onPieChartClick={handlePieChartClick}
                        />
                        <ObserverChart
                            globalId="application_transaction_avg_pre_host"
                            span={24}
                            metricType="timer"
                            prefixVariate={APP_ID}
                            dependencyKeys="hostName"
                        />
                        <ObserverChart
                            span={24}
                            globalId="application_transaction_count_pre_host"
                            metricType="timer"
                            prefixVariate={APP_ID}
                            dependencyKeys="hostName"
                        />
                    </EMonitorGallery>
                </EMonitorSection.Item>
            </EMonitorSection>
        </EMonitorSection>
    );
};

export default TransactionPage;
