import {reaction} from "mobx";
import LazyLoad from "react-lazyload";
import StoreManager from "$store/StoreManager";
import {CHART_DEFAULT_HEIGHT} from "$constants/index";
import React, {useEffect, useMemo, useState} from "react";
import EmptyTips from "$components/Base/EmptyTips/EmptyTips";
import ChartMetric from "$components/EMonitorChart/Metrics/ChartMetric";
import {ChartInfo, EMonitorMetricDataSet, MetricStatus} from "$models/ChartModel";

/**
 * Chart 图表部分
 *
 * 渲染引擎：Chart.js
 * 渲染方式：Canvas
 */
const EMonitorMetric: React.FC<{
    chart?: ChartInfo;
    height?: number;
    status?: MetricStatus;
    metrics?: EMonitorMetricDataSet[];
    onPieChartClick?(tags: any): void;
}> = props => {
    const {chart, metrics, status, onPieChartClick, height = CHART_DEFAULT_HEIGHT} = props;

    const [maxSeries, setMaxSeries] = useState<number>();

    // 监听最大显示条数变化
    useEffect(() => {
        const disposer = reaction(
            () => StoreManager.orderByStore.limit,
            limit => {
                setMaxSeries(limit);
            },
            {fireImmediately: true},
        );
        return () => disposer();
    }, []);

    const content = useMemo(() => {
        if (status === MetricStatus.NO_DATA) {
            return <EmptyTips tips="暂无图表数据"/>;
        }

        if (chart && metrics) {
            return <ChartMetric
                chart={chart}
                metrics={metrics}
                maxSeries={maxSeries}
                onPieChartClick={onPieChartClick}
            />;
        }

        return null;
    }, [chart, maxSeries, metrics, onPieChartClick, status]);

    // TODO：文本和表格类型数据集展示

    return (
        <LazyLoad
            once={true}
            height={height}
            debounce={100}
            overflow={true}
            resize={true}
        >
            <div style={{height}}>{content}</div>
        </LazyLoad>
    );
};

export default EMonitorMetric;
