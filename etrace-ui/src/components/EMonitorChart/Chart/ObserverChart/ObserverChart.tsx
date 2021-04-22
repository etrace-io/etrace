import React from "react";
import useSearchParams from "$hooks/useSearchParams";
import EMonitorChart, {EMonitorChartProps} from "$components/EMonitorChart/EMonitorChart";

interface ObserverChartProps extends EMonitorChartProps {
    // 监听对应（URL）参数，存在则显示
    dependencyKeys: string[] | string;
}

const ObserverChart: React.FC<ObserverChartProps> = props => {
    const {dependencyKeys, ...chartProps} = props;
    // 获取依赖对应的 value
    const dependencyValues = useSearchParams(dependencyKeys);

    const showChart = Object.keys(dependencyValues).length > 0;

    return showChart ? <EMonitorChart {...chartProps}/> : null;
};

export default ObserverChart;
