import {Col} from "antd";
import {QueryKit} from "$utils/Util";
import useChart from "$hooks/useChart";
import React, {useCallback} from "react";
import cloneDeep from "lodash/cloneDeep";
import useMetrics from "$hooks/useMetrics";
import {ChartInfo} from "$models/ChartModel";
import ChartCard from "$components/EMonitorChart/Chart/ChartCard";
import EMonitorMetric from "$components/EMonitorChart/EMonitorMetric";

export interface EMonitorChartProps {
    type?: "card" | "default";
    span?: number;
    editable?: boolean;
    // 用于采样
    metricType?: string;
    // 监听 URL 中对应的字段，用于设置监控项
    prefixVariate?: string;
    // 图表高度（默认 280）
    height?: number;
    // 隐藏右上角额外功能栏
    hideExtraFunc?: boolean;
    // chart 属性优先级最高，有调用方进行 chart 数据的获取
    // 若存在 chart，则会忽略 chartId 和 globalId
    chart?: ChartInfo;
    chartId?: string;
    globalId?: string;
    // 饼图点击事件
    onPieChartClick?(tags: any): void;
}

const EMonitorChart: React.FC<EMonitorChartProps> = props => {
    const {chartId, globalId, chart: originChart, onPieChartClick} = props;
    const {span, type = "card", height, hideExtraFunc} = props;
    const {metricType, prefixVariate} = props;

    // 获取 Chart 信息
    const {chart, isLoading: chartLoading} = useChart({
        id: chartId,
        globalId,
        chart: originChart,
        // enabled: observerChartEnable ? false : undefined,
        metricType,
        prefixVariate,
    });

    // 获取 Metrics
    const {metrics, status: metricStatus, isLoading: metricsLoading} = useMetrics(chart);

    const handleTargetFieldsChange = useCallback((targetIndex: number, fields: string[]) => {
        if (!chart?.targets) { return; }

        const cloneChart: ChartInfo = cloneDeep(chart);
        cloneChart.targets[targetIndex].fields = fields;
        QueryKit.reloadChartData(chart.globalId, cloneChart);
    }, [chart]);

    // const chartDataSource = chart || chartInfo;

    let chartComponent = (
        <EMonitorMetric
            key="metric"
            height={height}
            chart={chart}
            metrics={metrics}
            status={metricStatus}
            onPieChartClick={onPieChartClick}
        />
    );

    if (type === "card") {
        chartComponent = (
            <ChartCard
                dataSource={chart}
                hideExtra={hideExtraFunc}
                metricStatus={metricStatus}
                loading={chartLoading || metricsLoading}
                onFieldChange={handleTargetFieldsChange}
            >
                {chartComponent}
            </ChartCard>
        );
    }

    if (span) {
        chartComponent = <Col span={span}>{chartComponent}</Col>;
    }

    return chartComponent;
};

export default EMonitorChart;
