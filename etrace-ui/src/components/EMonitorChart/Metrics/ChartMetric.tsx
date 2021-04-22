import React, {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {ChartInfo, ChartSeriesInfo, ChartTypeEnum, EMonitorMetricDataSet, HiddenSeriesMap} from "$models/ChartModel";
import {ChartKit, ToolKit} from "$utils/Util";
import Chart, {ChartConfiguration} from "chart.js";

import ChartLegend from "$components/EMonitorChart/Legend/ChartLegend";
import {getSelectedSeries} from "$components/EMonitorChart/Metrics/utils";
import {default as ChartEditConfig, getConfigValue} from "$containers/Board/Explorer/ChartEditConfig";
import classNames from "classnames";
import StoreManager from "$store/StoreManager";
import ChartTooltip from "$components/EMonitorChart/Tooltip/ChartTooltip";
import {MAX_SERIES_COUNT} from "$constants/index";

import "./ChartMetric.less";

// import "chartjs-plugin-crosshair";

/**
 * Chart 类型指标渲染
 */
const ChartMetric: React.FC<{
    chart: ChartInfo;
    metrics: EMonitorMetricDataSet[];
    maxSeries?: number; // 最大显示条数
    onPieChartClick?(tags: any): void;
}> = props => {
    const {chart: chartInfo, metrics, onPieChartClick, maxSeries = MAX_SERIES_COUNT} = props;
    const isSeriesChart = ChartKit.isSeriesChart(chartInfo);
    const chartConfig = chartInfo?.config;

    const chartjsInstance = useRef<Chart>(null);
    // Chart 级缓存数据
    const hiddenSeriesMap = useRef<HiddenSeriesMap>({}); // 当前隐藏的 Series
    const canvas = useRef<HTMLCanvasElement>(null);

    const [showTooltip, setShowTooltip] = useState(false);
    // 遇到可点击节点，修改样式
    const [hoverPointer, setHoverPointer] = useState(false);

    // 拿到 Metrics 后进行数据处理（时间、label 等相关计算）
    const chartData = useMemo(
        () => {
            return ChartKit.createChartDataSource(chartInfo, metrics, maxSeries);
        },
        [chartInfo, metrics]
    );

    // 根据处理后的数据，生成对应 ChartJS 配置
    const chartJSConfig = useMemo<ChartConfiguration>(
        () => {
            const hiddenMap = hiddenSeriesMap.current;
            return ChartKit.createChartConfig(chartInfo, chartData, hiddenMap);
        },
        [chartData]
    );

    const [selectedSeries, setSelectedSeries] = useState<string[]>();

    // console.log({metrics})

    // console.log({chartInfo, metrics, chartData, chartJSConfig})

    // 根据配置对 Chart 进行更新
    useEffect(() => {
        renderChartWithConfig(chartJSConfig);
    }, [chartJSConfig, metrics]);

    // 相关销毁函数
    useEffect(() => {
        return () => {
            // 移除监听器
            Object.keys(chartCanvasEventListener).forEach(key => {
                const handler = chartCanvasEventListener[key];
                if (Array.isArray(handler)) {
                    handler.forEach(listener => {
                        canvas.current?.removeEventListener(key, listener);
                    });
                } else {
                    canvas.current?.removeEventListener(key, handler);
                }
            });
            const inst = chartjsInstance.current;
            if (inst) { inst.destroy(); }
        };
    }, []);

    /**
     * Series 点击回调句柄
     */
    const handleSeriesSelected = useCallback((series: ChartSeriesInfo, nativeEvent: React.MouseEvent<HTMLDivElement>) => {
        const chartInst = chartjsInstance.current;
        if (!chartInst) { return; }

        // 是否配合功能键，只调整对应 Series
        const withMetaKeyPressed =
            nativeEvent.shiftKey ||
            nativeEvent.metaKey ||
            nativeEvent.ctrlKey ||
            nativeEvent.altKey;

        const {selectedList, hiddenMap} = getSelectedSeries(
            series,
            chartInst.data,
            selectedSeries,
            hiddenSeriesMap.current,
            isSeriesChart,
            withMetaKeyPressed,
        );

        // 控制渲染效果
        if (isSeriesChart) {
            chartInst.data.datasets.forEach((dataset, index) => {
                const meta = chartInst.getDatasetMeta(index);
                meta.hidden = hiddenMap[dataset.label] > -1;
            });
        } else {
            chartInst.getDatasetMeta(0).data.forEach((item, index) => {
                const areaChartLabel = chartInst.data.labels[index] as string;
                item.hidden = hiddenMap[areaChartLabel] > -1;
            });
        }

        // 计算 SuggestMax
        const {leftYAxisMaxValue, rightYAxisMaxValue} = ChartKit.calcDataSuggestMax(chartData.datasets, hiddenMap);
        chartInst.options.scales.yAxes[0].ticks.suggestedMax = leftYAxisMaxValue * 1.02;
        chartInst.options.scales.yAxes[1].ticks.suggestedMax = rightYAxisMaxValue * 1.02;

        // 控制左右坐标轴隐藏
        const rightSeries = getConfigValue(ChartEditConfig.axis.rightYAxis.series, chartConfig) ?? [];
        if (rightSeries.length > 0) {
            if (selectedList.length === 0) {
                chartInst.options.scales.yAxes[0].display = true;
                chartInst.options.scales.yAxes[1].display = true;
            } else {
                const selectedRightSeries = selectedList.filter(item => rightSeries.includes(item));
                chartInst.options.scales.yAxes[0].display = selectedList.length - selectedRightSeries.length > 0;
                chartInst.options.scales.yAxes[1].display = selectedRightSeries.length > 0;
            }
        }

        // 更新选中目标 Series
        setSelectedSeries(selectedList);
        hiddenSeriesMap.current = hiddenMap;

        // 更新
        chartInst.update();
    }, [chartConfig, chartData?.datasets, isSeriesChart, selectedSeries]);

    // Series 点击事件（SeriesLink、采样）
    const handleChartPointClick = (e: MouseEvent) => {
        const chartInst = chartjsInstance.current;
        if (!isSeriesChart || !chartInst) { return; }

        const point = chartInst.getElementAtEvent(e)[0];
        if (!point) { return; }

        // @ts-ignore
        const index = point._index;
        // @ts-ignore
        const datasetIndex = point._datasetIndex;

        const metric = chartData.datasets[datasetIndex].metric as EMonitorMetricDataSet;
        if (!metric.name) { return; }

        StoreManager.chartEventStore.seriesClick({
            metric,
            index,
            timestamp: chartData.times[index],
            uniqueId: chartInfo.globalId,
        });
    };

    // 饼图区域点击事件
    const handlePieChartAreaClick = (e: MouseEvent) => {
        const chartInst = chartjsInstance.current;
        const chartType = ChartKit.getChartType(chartInfo);
        if (!onPieChartClick || chartType !== ChartTypeEnum.Pie || !chartInst) { return; }

        const clickArea = chartInst.getElementAtEvent(e);
        // @ts-ignore
        const index = clickArea?.[0]?._index;

        if (index >= 0) {
            const tags = (chartData.datasets[index].metric as EMonitorMetricDataSet).tags;
            onPieChartClick(tags);
            // StoreManager.chartEventStore.pieClick({
            //     item: {groups: },
            // });
        }
    };

    // 鼠标移动过程中，遇到可点击改变鼠标样式
    const handleChartMousemove = (e: MouseEvent) => {
        const chartInst = chartjsInstance.current;
        if (!chartInst) {
            setHoverPointer(false);
            return;
        }

        const point = chartInst.getElementAtEvent(e)[0];
        if (!point) {
            setHoverPointer(false);
            return;
        }

        if (!isSeriesChart) {
            setHoverPointer(!!point);
            // !hoverPointer && setHoverPointer(true);
            return;
        }

        // @ts-ignore
        const datasetIndex = point._datasetIndex;

        const metric = chartData.datasets[datasetIndex]?.metric as EMonitorMetricDataSet;
        if (!metric) { return; }

        const {metricType} = metric; // 是否支持采样
        const seriesLink = getConfigValue(ChartEditConfig.link.series, chartConfig); // 是否需要跳转

        const isPointer = !!(seriesLink || metricType);

        setHoverPointer(isPointer);
    };

    // 用于保存 Chart 所绑定的监听器，数组则表示该事件绑定多个监听器
    const chartCanvasEventListener = {
        click: [handleChartPointClick, handlePieChartAreaClick],
        mousemove: handleChartMousemove,
    };

    useEffect(() => {
        // 添加监听器
        Object.keys(chartCanvasEventListener).forEach(key => {
            const handler = chartCanvasEventListener[key];
            if (Array.isArray(handler)) {
                handler.forEach(listener => {
                    canvas.current.addEventListener(key, listener);
                });
            } else {
                canvas.current.addEventListener(key, handler);
            }
        });
        return () => {
            // 移除监听器
            Object.keys(chartCanvasEventListener).forEach(key => {
                const handler = chartCanvasEventListener[key];
                if (Array.isArray(handler)) {
                    handler.forEach(listener => {
                        canvas.current.removeEventListener(key, listener);
                    });
                } else {
                    canvas.current.removeEventListener(key, handler);
                }
            });
        };
    }, [metrics]);

    // 渲染图表配置（mount or update）
    const renderChartWithConfig = (config: ChartConfiguration) => {
        if (!canvas.current || !config) { return; }

        // 插件配置
        if (isSeriesChart) {
            config.options.plugins.crosshair = Object.assign({}, config.options.plugins.crosshair, {
                callbacks: {
                    beforeZoom: handleTimeRangeChange
                },
            });
        }

        if (!chartjsInstance.current) {
            // mount
            const ctx = canvas.current.getContext("2d");

            // 新建 ChartJS 实例
            chartjsInstance.current = new Chart(ctx, config);

            // 触发更新，传递 chartjsInstance
            setShowTooltip(true);

            // 在 useEffect 中添加监听器
        } else {
            // update
            chartjsInstance.current.config.type = config.type;
            chartjsInstance.current.data = config.data;
            chartjsInstance.current.options = config.options;

            chartjsInstance.current.update();
        }
    };

    if (!chartInfo) { return null; }

    const legendToRight = getConfigValue(ChartEditConfig.legend.config.toRight, chartConfig);

    const handleTimeRangeChange = (startIdx: number, endIdx: number) => {
        // const valueCount = chartjsInstance.current.data.labels.length;
        const startTime = chartData.times[Math.max(startIdx, 0)];
        const endTime = chartData.times[Math.min(endIdx, chartData.times.length - 1)];
        if (startTime >= endTime) { return; }

        // 修改 URL 时间
        ToolKit.changeURLTime(startTime, endTime);

        // 不再继续执行
        return false;
    };

    const chartMetricClassString = classNames("chart-metric-container", {
        "legend-to-right": legendToRight,
        "chart-cursor-pointer": hoverPointer,
    });

    return (
        <div className={chartMetricClassString}>
            <div className="chart-canvas-wrapper">
                <canvas ref={canvas}/>
            </div>

            {/* 图例 */}
            <ChartLegend
                maxHeight={null}
                chart={chartInfo}
                dataSource={chartData}
                selectedKeys={selectedSeries}
                onSelect={handleSeriesSelected}
            />

            {/* Tooltip */}
            {isSeriesChart && (
                <ChartTooltip
                    chart={chartInfo}
                    dataSource={chartData}
                    chartjsInst={chartjsInstance.current}
                    selectedKeys={selectedSeries}
                    onSelect={handleSeriesSelected}
                />
            )}
            {/*<div ref={ref => this.crosshair = ref} className="emonitor-chart-crosshair"/>*/}
        </div>
    );
};

export default ChartMetric;
