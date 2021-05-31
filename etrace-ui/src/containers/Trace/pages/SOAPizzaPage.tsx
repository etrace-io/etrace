import {reaction} from "mobx";
import React, {useEffect} from "react";
import {Target} from "$models/ChartModel";
import StoreManager from "$store/StoreManager";
import {UnitModelEnum} from "$models/UnitModel";
import {EMonitorSection} from "$components/EMonitorLayout";
import MetricStatList from "$components/StatList/MetricStatList";
import {APP_ID} from "$constants/index";
import EMonitorChart from "$components/EMonitorChart/EMonitorChart";

const SOAPizzaStatList: Target = {
    entity: "app_metric",
    fields: ["t_sum(count)"],
    groupBy: ["pizza"],
    functions: [{defaultParams: ["-1d"], name: "timeShift"}],
    measurement: "soa-trace",
    prefix: "web.httpizza",
    variate: ["appid"],
    tagFilters: [{
        key: "appid",
        op: "=",
        value: ["xxxxxx"]
    }],
    statListUnit: UnitModelEnum.Short,
};

const SOAPizzaPage: React.FC = props => {
    const {urlParamStore} = StoreManager;

    useEffect(() => {
        urlParamStore.changeURLParams({appid: urlParamStore.getValue(APP_ID)});

        const disposer = reaction(
            () => urlParamStore.getValue(APP_ID),
            () => {
                urlParamStore.changeURLParams({appid: urlParamStore.getValue(APP_ID)});
                urlParamStore.forceChange();
            }
        );

        return () => disposer();
    }, []);

    return (
        <EMonitorSection fullscreen={true} mode="horizontal">
            <EMonitorSection.Item width="30%">
                <MetricStatList
                    target={SOAPizzaStatList}
                    inputPlaceholder="Input pizza app..."
                />
            </EMonitorSection.Item>
            <EMonitorSection.Item scroll={true}>
                <EMonitorChart
                    globalId="application_pizza_count"
                    span={24}
                />
            </EMonitorSection.Item>
        </EMonitorSection>
    );
};

export default SOAPizzaPage;
