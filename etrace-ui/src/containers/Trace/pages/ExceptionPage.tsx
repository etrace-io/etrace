import {Target} from "$models/ChartModel";
import StoreManager from "$store/StoreManager";
import {Card, Checkbox, Row, Space} from "antd";
import {UnitModelEnum} from "$models/UnitModel";
import {MetricVariate} from "$models/BoardModel";
import React, {useCallback, useEffect, useState} from "react";
import MetricStatList from "$components/StatList/MetricStatList";
import usePieChartClickObserver from "$hooks/usePieChartClickObserver";
import {EMonitorGallery, EMonitorSection} from "$components/EMonitorLayout";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {APP_ID} from "$constants/index";
import EMonitorChart from "$components/EMonitorChart/EMonitorChart";
import ObserverChart from "$components/EMonitorChart/Chart/ObserverChart/ObserverChart";

const EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "exception",
    null,
    null,
    APP_ID
);
const CLUSTER: MetricVariate = new MetricVariate(
    "Cluster",
    "cluster",
    "application",
    "exception",
    null,
    null,
    APP_ID
);
const HOST: MetricVariate = new  MetricVariate(
    "Host",
    "hostName",
    "application",
    "exception",
    ["ezone", "cluster"],
    null,
    APP_ID
);

const ExceptionPage: React.FC = props => {
    const {urlParamStore} = StoreManager;
    const [singleHost, setSingleHost] = useState<boolean>(() => urlParamStore.getValue("hostName") != null);
    usePieChartClickObserver();

    useEffect(() => {
        if (!singleHost) { // 清除选择后的数据
            StoreManager.urlParamStore.changeURLParams({}, ["cluster", "hostName"]);
        }
    }, [singleHost]);

    const handlePieChartClick = useCallback(tags => {
        StoreManager.urlParamStore.changeURLParams(tags);
    }, []);

    const variates = singleHost
        ? [EZONE, CLUSTER, HOST]
        : [EZONE, CLUSTER];

    const statList: Target = {
        entity: "application",
        fields: ["t_sum(count)"],
        groupBy: ["type", "name"],
        functions: [{defaultParams: ["-1d"], name: "timeShift"}],
        measurement: "${appId}.exception",
        measurementVars: ["appId"],
        variate: ["ezone", "cluster", "hostName"],
        statListUnit: UnitModelEnum.Short,
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
                        target={statList}
                        inputPlaceholder="Input Exception Name..."
                    />
                </EMonitorSection.Item>

                <EMonitorSection.Item scroll={true}>
                    <EMonitorGallery>
                        {singleHost ? (<>
                            <EMonitorChart
                                key="application_exception_pre_host"
                                globalId="application_exception_pre_host"
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="counter"
                            />
                        </>) : (<>
                            <EMonitorChart
                                key="application_exception_count"
                                globalId="application_exception_count"
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="counter"
                            />
                            <EMonitorChart
                                key="8613dbe3-5faa-4fd5-a302-f5051ca1a734"
                                globalId="8613dbe3-5faa-4fd5-a302-f5051ca1a734"
                                span={24}
                                prefixVariate={APP_ID}
                                onPieChartClick={handlePieChartClick}
                            />
                            <ObserverChart
                                key="8185c82f-1caf-45c6-9c97-794a3aed8350"
                                globalId="8185c82f-1caf-45c6-9c97-794a3aed8350"
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="counter"
                                dependencyKeys="method"
                            />
                            <EMonitorChart
                                key="application_exception_host"
                                globalId="application_exception_host"
                                span={24}
                                prefixVariate={APP_ID}
                                onPieChartClick={handlePieChartClick}
                            />
                            <ObserverChart
                                key="application_exception_pre_host"
                                globalId="application_exception_pre_host"
                                span={24}
                                prefixVariate={APP_ID}
                                metricType="counter"
                                dependencyKeys="hostName"
                            />
                        </>)}
                    </EMonitorGallery>
                </EMonitorSection.Item>
            </EMonitorSection>
        </EMonitorSection>
    );
};

export default ExceptionPage;
