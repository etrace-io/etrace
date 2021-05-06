import React from "react";
import {observer} from "mobx-react";
import StoreManager from "../../store/StoreManager";
import EMonitorChart from "$components/EMonitorChart/EMonitorChart";

interface ObserverChartProps {
    globalId: string;
    span: number;
    prefixKey: string;
    metricType?: string;
    observerKeys: string[];
}
interface ObserverChartState {}

@observer
export default class ObserverChart extends React.Component<ObserverChartProps, ObserverChartState> {
    render() {
        const { observerKeys, globalId, span, prefixKey, metricType } = this.props;
        if (!observerKeys) {
            return null;
        }
        const values = observerKeys.map(key => StoreManager.urlParamStore.getValue(key)).filter(item => item);
        if (!values || values.length != observerKeys.length) {
            return null;
        }
        return (
            <EMonitorChart
                globalId={globalId}
                span={span}
                metricType={metricType}
                prefixVariate={prefixKey}
            />
        );
    }
}
