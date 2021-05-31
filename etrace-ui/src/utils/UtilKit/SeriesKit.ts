/**
 * Series 相关处理：创建、更新等n
 */
import ChartJS from "chart.js";
import {
    ChartInfo,
    EMonitorMetricComputeTarget,
    EMonitorMetricDataSet,
    EMonitorSeriesData,
    EMonitorSeriesDataType
} from "$models/ChartModel";
import {ChartKit, MetricKit, TargetKit} from "$utils/Util";
import {default as ChartEditConfig, getConfigValue} from "$containers/Board/Explorer/ChartEditConfig";
import {ConvertFunctionEnum} from "$utils/ConvertFunction";
import {LegendValue} from "$components/Chart/ChartLegend";
import groupBy from "lodash/groupBy";
import {
    EMONITOR_CHART_LEFT_YAXIS_ID,
    EMONITOR_CHART_RIGHT_YAXIS_ID
} from "$components/EMonitorChart/EMonitorChartConfig";

const Mexp = require("math-expression-evaluator");
const Color = ChartJS.helpers.color;

export default {
    createSeriesData,
    createComputedSeriesData,
    getSeriesColor,
    shouldProcessDataset,
};

/**
 * 处理 Group 内容
 * 每个 Group 下的 field 就是一条线
 */
function createSeriesData(
    chart: ChartInfo,
    datasetList: EMonitorMetricDataSet[],
    timeScale: number[],
    maxSeries?: number,
): EMonitorSeriesData[] {
    if (!chart || !datasetList?.length) { return []; }

    const seriesList: EMonitorSeriesData[] = [];
    /* 获取指标配置 */
    const chartConfig = chart.config;
    // const chartType = ChartKit.getChartType(chart);
    const chartJSType = ChartKit.getChartJSType(chart);
    const timeLength = timeScale.length;
    const timeScaleMap = MetricKit.mapTimeScale(timeScale); // 时间戳 -> 所在位置，方便在 O(1) 下找到对应的坐标

    // 空值处理
    const nullValue = getConfigValue(ChartEditConfig.display.series.nullAsZero, chart.config);
    const nullAsZero = nullValue?.includes("zone") ?? true; // 空值作为零
    const connectNulls = nullValue?.includes("connectNulls") ?? false; // 链接空值

    // 右侧坐标
    const rightAxisSeries = getConfigValue(ChartEditConfig.axis.rightYAxis.series, chartConfig);

    /* 处理请求结果 */

    // 遍历每个数据源的结果集（一对多）
    datasetList?.forEach(dataset => {
        // 每个结果集就是一条线
        const {functions, metricType, tagFilters, name, results} = dataset;

        // TODO: 和 getMetricDisplay 的区别
        // 如果 getMetricDisplay 为 false 需要 return
        // if (!display) { return; }

        const display = TargetKit.getValueFromFunctions(functions, ConvertFunctionEnum.TARGET_DISPLAY) ?? true;
        const line = TargetKit.getValueFromFunctions(functions, ConvertFunctionEnum.LINE_FLAG) ?? "A";
        const countPS = !!TargetKit.getCountPS(functions);
        const timeShift = TargetKit.getValueFromFunctions(functions, ConvertFunctionEnum.TIME_SHIFT);

        const resolveTimeShiftOffset = TargetKit.resolveTimeShit(timeShift);

        const {groups, startTime, interval, measurementName} = results;

        // 处理 Group 内容
        for (const model of groups) {
            const {group, fields} = model;
            const groupName = Object.values(group).join("/");

            // 遍历字段
            for (const field of Object.keys(fields)) {
                if (maxSeries && seriesList.length >= maxSeries) {
                    break;
                }
                const values = fields[field];
                const needFillNull = values.length !== timeLength; // 需要填充为 null 的位置

                const seriesName = groupName || field;

                // 显示名称，带上 alias、timeshift
                const seriesLabel = [
                    TargetKit.getAliasNameByFunctions(seriesName, functions),
                    timeShift
                ].filter(Boolean).join(" ");

                const isLeftYAxis = rightAxisSeries.indexOf(seriesLabel) === -1;

                // TODO: 点击图例后的隐藏
                // if (selectedSeries) {
                //     data.hidden = selectedSeries.indexOf(data.name) < 0;
                // }

                // 处理并 push 数据（保证 values 长度一致）
                const valueData = new Array(timeLength).fill(null);
                let total = 0;
                let count = 0; // 参与 avg 计算的点
                let max = -Infinity;
                let min = Infinity;
                let current = null;

                values.forEach((value, idx) => {
                    if (value !== null) { count++; }
                    total += value ?? 0;
                    current = value ?? current;
                    max = Math.max(max, value ?? -Infinity);
                    min = Math.min(min, value ?? Infinity);

                    // 获取当前 value 对应的时间及在刻度上的坐标
                    const currValueTime = startTime + idx * interval - (resolveTimeShiftOffset ?? 0);
                    const currValueIndexOnTimeScale = timeScaleMap[currValueTime];

                    if (nullAsZero && value === null) {
                        valueData[currValueIndexOnTimeScale] = 0;
                        return;
                    }
                    if (countPS) {
                        valueData[currValueIndexOnTimeScale] = value * 1000 / interval;
                        return;
                    }
                    valueData[currValueIndexOnTimeScale] = Math.floor(value * 1000) / 1000;
                });

                const {results: {groups: originGroups, ...metricResult}, ...originMetric} = dataset;

                // 移除 results 中的 groups，节省空间
                const metric: EMonitorMetricDataSet = {
                    metricType: chart.targets[0]?.metricType,
                    field,
                    tags: group,
                    results: metricResult,
                    ...originMetric,
                };

                // Object.assign({
                // }, dataset);

                const seriesData: EMonitorSeriesData = {
                    targetType: EMonitorSeriesDataType.DEFAULT,
                    display,
                    data: valueData,
                    name: seriesName,
                    label: seriesLabel,
                    yAxisID: isLeftYAxis ? EMONITOR_CHART_LEFT_YAXIS_ID : EMONITOR_CHART_RIGHT_YAXIS_ID,
                    spanGaps: connectNulls || needFillNull,
                    type: chartJSType,
                    lineNum: line,
                    // 设置 Metric，添加请求中的 MetricType 方便后续根据 metric 判断采样类型
                    metric,
                    value: {
                        [LegendValue.TOTAL]: countPS ? (total * 1000 / interval) : total,
                        [LegendValue.MAX]: max === -Infinity ? 0 : (countPS ? (max * 1000 / interval) : max),
                        [LegendValue.MIN]: min === Infinity ? 0 : (countPS ? (min * 1000 / interval) : min),
                        [LegendValue.AVG]: count > 0 ? total / count : 0,
                        [LegendValue.CURRENT]: current === null ? 0 : (countPS ? (current * 1000 / interval) : current)
                    },
                };

                // 保存当前线数据
                seriesList.push(seriesData);
            }
        }
    });

    return seriesList;
}

/**
 * 根据 Metric 的数据结果，处理计算指标的数据集
 */
function createComputedSeriesData(
    chart: ChartInfo,
    metricsDatasetList: EMonitorSeriesData[],
    timeScale: number[],
): EMonitorSeriesData[] {
    if (!chart || !metricsDatasetList?.length) { return []; }

    const computedSeriesList: EMonitorSeriesData[] = [];

    const computedTargets = ChartKit.getComputeTargets(chart);
    const existingDataset = groupBy(metricsDatasetList, "lineNum");

    const chartConfig = chart.config;
    const chartJSType = ChartKit.getChartJSType(chart);

    // 获取 Chart 配置
    const nullValue = getConfigValue(ChartEditConfig.display.series.nullAsZero, chartConfig);
    const nullAsZero = nullValue?.includes("zone") ?? true; // 空值作为零
    const connectNulls = nullValue?.includes("connectNulls") ?? false; // 链接空值

    const rightAxisSeries = getConfigValue(ChartEditConfig.axis.rightYAxis.series, chartConfig);

    // 遍历所有计算指标
    computedTargets?.forEach(target => {
        if (!TargetKit.checkComputeTargetValid(target)) {
            return;
        }
        const {display, alias, compute} = target;

        if (!display) {
            return;
        }

        // 提取表达式中的变量
        const variateList = TargetKit.extractVariateFromComputedTargetExpression(compute);

        // 获取计算指标所有成员变量的排列组合
        const pendingComputedMetrics = combineComputedTargetMember(
            existingDataset,
            variateList,
        );

        pendingComputedMetrics.forEach((metricMap, computedMetricsIndex) => {
            // 处理 value，这里能够保证所有 metric 的 values 已经对齐，长度一致
            const computedValues = new Array(metricMap[variateList[0]].data.length)
                .fill(null)
                .map((_: any, index: number) => {
                    const targetValues = variateList.map(member => metricMap[member].data[index]);

                    if (targetValues.includes(null) && !nullAsZero) {
                        return null;
                    }

                    const expression = variateList.reduce((prevExpression, memberVariate) => {
                        return prevExpression.replace(
                            new RegExp(`\\$\{${memberVariate}}`, "g"),
                            (metricMap[memberVariate].data[index] ?? 0) + "",
                        );
                    }, compute);

                    const calculateResult = Mexp.eval(expression);

                    return Math.floor(calculateResult * 1000) / 1000;
                });

            // 处理计算值
            let total = 0;
            let count = 0; // 参与 avg 计算的点
            let max = -Infinity;
            let min = Infinity;
            let current = null;
            computedValues.forEach((value, idx) => {
                if (value !== null) { count++; }
                total += value ?? 0;
                current = value ?? current;
                max = Math.max(max, value ?? -Infinity);
                min = Math.min(min, value ?? Infinity);
            });

            // 名称后缀（多个指标加上 index）
            const namePostfix = pendingComputedMetrics.length > 1 ? (" - " + computedMetricsIndex) : "";

            const seriesName = variateList
                .map(member => metricMap[member].name)
                .join(" × ")
                .concat(namePostfix);

            // 添加 alias
            const seriesLabel = alias.concat(namePostfix);

            // 坐标位置
            const isLeftYAxis = rightAxisSeries.indexOf(seriesLabel) === -1;

            computedSeriesList.push({
                targetType: EMonitorSeriesDataType.COMPUTED,
                display,
                data: computedValues,
                name: seriesName,
                label: seriesLabel,
                lineNum: "COMPUTED_" + TargetKit.targetIndexToCode(computedMetricsIndex),
                spanGaps: connectNulls,
                yAxisID: isLeftYAxis ? EMONITOR_CHART_LEFT_YAXIS_ID : EMONITOR_CHART_RIGHT_YAXIS_ID,
                type: chartJSType,
                metric: target,
                value: {
                    [LegendValue.TOTAL]: total,
                    [LegendValue.MAX]: max === -Infinity ? 0 : max,
                    [LegendValue.MIN]: min === Infinity ? 0 : min,
                    [LegendValue.AVG]: count > 0 ? total / count : 0,
                    [LegendValue.CURRENT]: current === null ? 0 : current
                },
            });
        });
    });

    return computedSeriesList;
}

/**
 * 将计算指标中的成员变量对应的 Metrics 进行排列组合
 * in: {A: [series * 2], B: [series * 2], C: [series * 2]} | [A, B]
 * out: [A1B1, A1B2, A2B1, A2B2]
 */
function combineComputedTargetMember(dataSource: { [lineCode: string]: EMonitorSeriesData[] }, computedMember: string[]) {
    if (!computedMember?.length) { return []; }

    // 拿到对应的指标
    const lineCode = computedMember[0];
    // 拿到查询到的所有 Metrics
    const currMemberMetrics = dataSource[lineCode];

    if (computedMember.length === 1) {
        return currMemberMetrics?.map(metric => ({
            [lineCode]: metric,
        })) ?? [];
    }

    // 递归计算出后面的所有组合
    const prevMember = combineComputedTargetMember(dataSource, computedMember.slice(1));
    // 保存当前计算出的所有组合
    const computedTargetMetrics: { [lineCode: string]: EMonitorSeriesData }[] = [];

    currMemberMetrics?.forEach(metric => {
        prevMember.forEach(prevMetric => {
            // 新增一个组合
            computedTargetMetrics.push(Object.assign({
                [lineCode]: metric,
            }, prevMetric));
        });
    });

    return computedTargetMetrics;
}

/**
 * 判断指标是否需要参与坐标轴刻度的计算
 * 仅「需要显示」以及「参与计算指标」的数据集需要参与坐标轴刻度的计算
 * @param dataset
 * @param computedTargets
 */
function shouldProcessDataset(dataset: EMonitorMetricDataSet, computedTargets: EMonitorMetricComputeTarget[]) {
    const computedVariateList = computedTargets
        ?.map(target => TargetKit.extractVariateFromComputedTargetExpression(target.compute))
        .flat() ?? [];

    const display = TargetKit.getValueFromFunctions(dataset.functions, ConvertFunctionEnum.TARGET_DISPLAY) !== false;
    const partOfComputedTarget = computedVariateList.includes(
        TargetKit.getValueFromFunctions(dataset.functions, ConvertFunctionEnum.LINE_FLAG)
    );

    return display || partOfComputedTarget;
}

/**
 * 获取线条颜色
 */
function getSeriesColor(idx: number, alpha?: number) {
    const colors = [
        "#7EB26D", "#EAB839", "#6ED0E0", "#ef843c",
        "#E24D42", "#1F78C1", "#BA43A9", "#705DA0",
        "#508642", "#CCA300", "#447EBC", "#C15C17",
        "#890F02", "#0A437C", "#6D1F62", "#584477",
        "#70DBED", "#F9BA8F", "#F29191", "#82B5D8",
        "#E5A8E2", "#AEA2E0", "#629E51", "#E5AC0E",
        "#64B0C8", "#E0752D", "#BF1B00", "#0A50A1",
        "#962D82", "#614D93", "#9AC48A", "#F2C96D",
        "#65C5DB", "#F9934E", "#EA6460", "#5195CE",
        "#D683CE", "#806EB7", "#3F6833", "#967302",
        "#2F575E", "#99440A", "#58140C", "#052B51",
        "#511749", "#3F2B5B"
    ];
    const color = colors[idx % colors.length];
    return alpha
        ? Color(color).alpha(alpha).rgbaString()
        : color;
}
