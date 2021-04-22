/**
 * 用于 Chart 信息处理、图表绘制
 * 输入 Chart，输出对应的 Target
 */
import get from "lodash/get";
import cloneDeep from "lodash/cloneDeep";
import {ChartKit, SeriesKit, TargetKit, ToolKit} from "$utils/Util";
import {
    ChartInfo,
    ChartTypeEnum,
    EMonitorChartVisualData,
    EMonitorMetricComputeTarget,
    EMonitorMetricDataSet,
    EMonitorMetricTarget,
    EMonitorSeriesData,
    HiddenSeriesMap,
} from "$models/ChartModel";
import union from "lodash/union";
import {ChartConfiguration, ChartData, ChartOptions, ChartTooltipItem, PointStyle} from "chart.js";
import EMonitorChartConfig, {
    EMONITOR_CHART_DEFAULT_OPTIONS,
    EMONITOR_CHART_LEFT_YAXIS_ID,
    EMONITOR_CHART_PLUGIN_CROSSHAIR,
    EMONITOR_CHART_RIGHT_YAXIS_ID
} from "$components/EMonitorChart/EMonitorChartConfig";
import {LegendValue} from "$components/Chart/ChartLegend";
import {default as ChartEditConfig, getConfigValue} from "$containers/Board/Explorer/ChartEditConfig";
import {ConvertFunctionEnum} from "$utils/ConvertFunction";
import {DataFormatter} from "$utils/DataFormatter";
import {UnitModelEnum} from "$models/UnitModel";
import CrosshairPlugin from "$components/EMonitorChart/Plugins/ChartCrosshair";

export default {
    extractAndResolveTargetsFromChart,
    getComputeTargets,
    getChartType,
    getChartJSType,
    createChartDataSource,
    createChartConfig,
    calcDataSuggestMax,
    isSeriesChart,
};

type ChartTarget = EMonitorMetricTarget;

/**
 * 处理并提取 Chart 中的 targets
 * @param chart Chart 配置信息
 */
function extractAndResolveTargetsFromChart(chart: ChartInfo): ChartTarget[] | null {
    if (!chart?.targets) {
        return null;
    }

    // 校验指标合法
    // TODO: 检查指标配置不全
    const pendingTargets: ChartTarget[] = new Proxy([], {
        set(obj: ChartTarget[], key: number | string, target: ChartTarget) {
            if (key === "length" || TargetKit.checkTargetValid(target as ChartTarget)) {
                return Reflect.set(obj, key, target);
            } else {
                console.warn("当前指标不合法：%s", target);
                return true;
            }
        }
    });

    // 处理指标中所有 Target
    chart.targets?.forEach((target, index) => {
        if (target.isAnalyze) { return; }

        // 记录行号信息，以防 Sunfire 和 LinDB 指标分别计算时丢失
        target.lineIndex = index;

        // 处理 Sunfire 数据源
        if (TargetKit.isSimpleJsonTarget(target)) {
            pendingTargets.push(TargetKit.resolveSunfireTarget(target));
            return;
        }

        console.log("TargetKit.isPrometheusTarget(target)", TargetKit.isPrometheusTarget(target));
        // todo: process prometheus datasource
        if (TargetKit.isPrometheusTarget(target)) {
            pendingTargets.push(TargetKit.resolvePrometheusTarget(target));
            return;
        }

        // 处理 LinDB 数据源
        pendingTargets.push(TargetKit.resolveLinDBTarget(target, chart));
    });

    return pendingTargets.filter(Boolean);
}

/**
 * 获取所有 Series 及坐标轴需要的需数据
 */
function createChartDataSource(
    chart: ChartInfo,
    datasetList: EMonitorMetricDataSet[],
    maxSeries?: number,
): EMonitorChartVisualData {
    if (!chart || !datasetList?.length) { return null; }

    // 寻找所有数据集中最大时间范围
    // 基础此基础来填充数据
    const computedTargets = ChartKit.getComputeTargets(chart);

    let timeScale: number[] = []; // 所有数据源上的时间刻度
    datasetList?.forEach(dataset => {
        if (!dataset.results) { return; }
        const {interval, startTime, endTime} = dataset.results;
        // 这里需要判断参与显示的 target 的时间间隔
        // 或不显示，但是参与了计算指标的计算的 target
        // 不论指标「是否展示」，都能显示合理的时间间隔
        // 环比排除指标不考虑
        if (
            SeriesKit.shouldProcessDataset(dataset, computedTargets) &&
            !TargetKit.getValueFromFunctions(dataset.functions, ConvertFunctionEnum.TIME_SHIFT)
        ) {
            timeScale = union(timeScale, ToolKit.getTimeScaleByRange(startTime, endTime, interval));
        }
    });
    // 时间顺序排序
    timeScale.sort((a, b) => a - b);

    const minInterval = Math.min.apply(null, datasetList?.map(dataset => dataset.results?.interval ?? Infinity));

    if (minInterval <= 0 || minInterval === Infinity) {
        return;
    }

    // 获取所有 Series
    const seriesList = SeriesKit.createSeriesData(chart, datasetList, timeScale, maxSeries);
    // 获取所有计算指标
    const computedSeriesList = SeriesKit.createComputedSeriesData(chart, seriesList, timeScale);

    return {
        times: timeScale,
        interval: minInterval,
        datasets: [
            ...seriesList,
            ...computedSeriesList,
        ],
    };
}

/**
 * 创建绘图配置
 * @param chart
 * @param dataSource
 * @param hiddenSeries 需要隐藏的 Series
 */
function createChartConfig(
    chart: ChartInfo,
    dataSource: EMonitorChartVisualData,
    hiddenSeries?: HiddenSeriesMap,
): ChartConfiguration {
    if (!chart || !dataSource) { return null; }

    switch (getChartType(chart)) {
        case ChartTypeEnum.Pie:
        case ChartTypeEnum.Radar:
            return createProportionChartConfig(chart, dataSource);
        case ChartTypeEnum.Line:
        case ChartTypeEnum.Area:
        case ChartTypeEnum.Column:
        case ChartTypeEnum.Scatter:
            return createSeriesChartConfig(chart, dataSource, hiddenSeries);
        default:
            return null;
    }
}

/**
 * 线性图表绘制配置
 */
function createSeriesChartConfig(
    chart: ChartInfo,
    dataSource: EMonitorChartVisualData,
    hiddenSeries?: HiddenSeriesMap,
): ChartConfiguration {
    const type = getChartJSType(chart);
    const options = creatChartOptions(chart, dataSource, hiddenSeries);

    // 配置 Plugins
    const plugins = [
        CrosshairPlugin,
    ];
    options.plugins = {
        crosshair: EMONITOR_CHART_PLUGIN_CROSSHAIR,
    };

    // 获取 Chart 相关配置
    const chartConfig = chart.config;
    const dashStyle = getConfigValue(ChartEditConfig.display.series.dashStyle, chartConfig);
    const borderDash = dashStyle.split(",").map(Number);

    const data: ChartData = {
        labels: ToolKit.formatChartTimeLabel(dataSource.times),
        datasets: dataSource.datasets
            .filter(dataset => dataset.display)
            .map((dataset, datasetIndex) => {
                // 获取指标相关配置
                const {metric: {functions}} = dataset;
                const {spanGaps, yAxisID, label} = dataset;
                const timeShift = TargetKit.getValueFromFunctions(functions, ConvertFunctionEnum.TIME_SHIFT);

                return {
                    // hidden: false,
                    data: dataset.data,
                    hidden: label in hiddenSeries,
                    label,
                    yAxisID,
                    spanGaps,
                    borderColor: ctx => SeriesKit.getSeriesColor(ctx.datasetIndex),
                    backgroundColor: ctx => SeriesKit.getSeriesColor(ctx.datasetIndex, 0.2),
                    borderDash: timeShift ? borderDash : null,
                };
            })
    };

    return { type, data, options, plugins };
}

/**
 * 比例图绘制，如饼图、雷达图
 */
function createProportionChartConfig(chart: ChartInfo, dataSource: EMonitorChartVisualData): ChartConfiguration {
    const type = getChartJSType(chart);
    const options = creatChartOptions(chart, dataSource);

    const labels = [];
    const values = [];

    dataSource.datasets.forEach(dataset => {
        labels.push(dataset.label);
        values.push(dataset.value.total);
    });

    const data: ChartData = {
        labels,
        datasets: [{
            data: values,
            // borderColor: ctx => SeriesKit.getSeriesColor(ctx.datasetIndex),
            backgroundColor: values.map((_, idx) => SeriesKit.getSeriesColor(idx)),
        }],
    };

    return { type, data, options };
}

/**
 * 创建图表配置
 */
function creatChartOptions(
    chart: ChartInfo,
    dataSource: EMonitorChartVisualData,
    hiddenSeries?: HiddenSeriesMap,
): ChartOptions {
    const options = cloneDeep(EMONITOR_CHART_DEFAULT_OPTIONS);
    const chartType = getChartType(chart);
    // const chartJSType = getChartJSType(chart);
    const chartConfig = chart.config;

    // 判断是否为线条图
    const isSeries = isSeriesChart(chart);
    const unit = getConfigValue(ChartEditConfig.axis.leftYAxis.unit, chartConfig);
    const decimals = getConfigValue(ChartEditConfig.axis.leftYAxis.decimals, chartConfig);

    /* 设置数据 Tooltip 交互方式 */
    options.tooltips = {
        // Pie or Radar 图开启自带 Tooltip
        enabled: !isSeries,
        // @ts-ignore
        mode: isSeries ? "interpolation" : "nearest",
        // intersect 表示触发 tooltip 的方式
        // false 表示在整个纵向方向上都能都获取数据索引，
        // 而非强制将鼠标移动到数据点上
        intersect: !isSeries,
    };

    // 非线性图配置
    if (!isSeries) {
        options.tooltips.callbacks = {
            label(tooltipItem: ChartTooltipItem, data: ChartData) {
                const values = data.datasets[tooltipItem.datasetIndex].data as number[];
                const value = values[tooltipItem.index];
                const total = values.reduce((a, b) => a + b, 0);
                return `${data.labels[tooltipItem.index]}: ${DataFormatter.tooltipFormatter(unit, value, decimals)}（${DataFormatter.tooltipFormatter(UnitModelEnum.Percent0_0, value / total)}）`;
            },
        };
        // // disable zoom for not series chart
        // chartConfig.options.zoom.enabled = false;
        // chartConfig.options.zoom.drag = false;
    }

    /* 配置坐标轴 */
    options.scales = {
        xAxes: [
            EMonitorChartConfig.getXAxisConfig(chart),
        ],
        yAxes: [
            EMonitorChartConfig.getYAxisConfig("left", chart),
            EMonitorChartConfig.getYAxisConfig("right", chart),
        ]
    };

    /* 隐藏雷达图坐标轴 */
    if (chartType === ChartTypeEnum.Radar) {
        options.scale = {
            ticks: {display: false, beginAtZero: true},
        };
    }

    /* 获取左右坐标轴最大值 */
    const {leftYAxisMaxValue, rightYAxisMaxValue} = calcDataSuggestMax(dataSource.datasets, hiddenSeries);
    options.scales.yAxes[0].ticks.suggestedMax = leftYAxisMaxValue * 1.02;
    options.scales.yAxes[1].ticks.suggestedMax = rightYAxisMaxValue * 1.02;

    /* 设置显示点、点大小 */
    const markerEnabled =
        chartType === ChartTypeEnum.Scatter ||
        chartType === ChartTypeEnum.Radar ||
        getConfigValue(ChartEditConfig.display.series.showPoint, chartConfig);

    if (markerEnabled) {
        const markerRadius = getConfigValue(ChartEditConfig.display.series.markerRadius, chartConfig);
        options.elements.point.radius = markerRadius;
        options.elements.point.hoverRadius = markerRadius + 2;
    }

    /* 设置点样式 */
    const pointStyle = getConfigValue(ChartEditConfig.display.series.pointStyle, chartConfig);
    options.elements.point.pointStyle = pointStyle as PointStyle;

    /* 设置线宽度 */
    const lineWidth = getConfigValue(ChartEditConfig.display.series.lineWidth, chartConfig);
    options.elements.line.borderWidth = lineWidth;

    /* 散点图不绘制连线 */
    options.showLines = chartType !== ChartTypeEnum.Scatter;

    /* 面积、雷达图设置填充颜色 */
    options.elements.line.fill = chartType === ChartTypeEnum.Area || chartType === ChartTypeEnum.Radar;

    /* 交互事件设置 */
    // options.hover.onHover = handleOnChartDataHover(chart, dataSource);

    return options;
}

/* 图表交互事件句柄 */

// hover 更改指针样式
function handleOnChartDataHover(chart: ChartInfo, dataSource: EMonitorChartVisualData) {
    const chartConfig = chart.config;
    const {datasets} = dataSource;
    const isSeries = isSeriesChart(chart);
    const isSeriesLink = !!getConfigValue(ChartEditConfig.link.series, chartConfig);
    const defaultCursor = isSeries ? "crosshair" : "default";

    return function (e: any) {
        const el = this.getElementAtEvent?.(e);
        // console.log(el, datasets);
        const metric = datasets[el[0]?._datasetIndex]?.metric;
        const hasMetricType = metric && "metricType" in metric && !!metric.metricType;

        let isPointer = false;

        if (el && el.length > 0) {
            // 存在数据点才会更改
            if (!isSeries || hasMetricType || isSeriesLink) {
                isPointer = true;
            }
        }

        e.target && (e.target.style.cursor = isPointer ? "pointer" : defaultCursor);
    };
}

/* 工具函数 */

/**
 * 获取计算指标
 * @param chart Chart 配置信息
 */
function getComputeTargets(chart: ChartInfo): EMonitorMetricComputeTarget[] | null {
    return get(chart, "config.computes", null);
}

function getChartType(chart: ChartInfo): ChartTypeEnum {
    return get(chart, "config.type", ChartTypeEnum.Line);
}

/**
 * 计算当前数据源中所有显示的 Series 中的建议最大值
 */
function calcDataSuggestMax(datasets: EMonitorSeriesData[], hiddenMap?: HiddenSeriesMap) {
    let leftYAxisMaxValue = 0;
    let rightYAxisMaxValue = 0;

    datasets.forEach(series => {
        if (
            series.display === false ||
            hiddenMap?.[series.label] > -1
        ) {
            return;
        }

        const currMaxValue = series.value[LegendValue.MAX];

        if (series.yAxisID === EMONITOR_CHART_LEFT_YAXIS_ID && currMaxValue > leftYAxisMaxValue) {
            leftYAxisMaxValue = currMaxValue;
        }
        if (series.yAxisID === EMONITOR_CHART_RIGHT_YAXIS_ID && currMaxValue > rightYAxisMaxValue) {
            rightYAxisMaxValue = currMaxValue;
        }
    });

    return {leftYAxisMaxValue, rightYAxisMaxValue};
}

/**
 * 配置中的 Type 和 ChartJS 中的 type 可能不一致，需要转换
 */
function getChartJSType(chart: ChartInfo) {
    const type = ChartKit.getChartType(chart);
    if (type === ChartTypeEnum.Area) {
        return "line";
    } else if (type === ChartTypeEnum.Column) {
        return "bar";
    }
    return type as string;
}

function isSeriesChart(chart: ChartInfo) {
    if (!chart) { return false; }
    const chartType = getChartType(chart);
    return chartType !== ChartTypeEnum.Radar && chartType !== ChartTypeEnum.Pie;
}
