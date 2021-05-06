import {ResultSetModel} from "../models/ResultSetModel";
import {ResultModel} from "../models/ResultModel";
import {isEmpty} from "./Util";
import {GroupModel} from "../models/GroupModel";
import {ConvertFunctionEnum, ConvertFunctionModel} from "./ConvertFunction";
import {Chart, ChartStatus, ChartStatusEnum} from "../models/ChartModel";
import {get} from "lodash";
import {LegendValue} from "../components/Chart/ChartLegend";
import ChartJS from "chart.js";
import {default as ChartEditConfig, getConfigValue} from "../containers/Board/Explorer/ChartEditConfig";
import moment, {DurationInputArg2} from "moment";

const R = require("ramda");
const Color = ChartJS.helpers.color;

export function getColor(idx: number) {
    const colors = ["#7EB26D", "#EAB839", "#6ED0E0", "#EF843C", "#E24D42", "#1F78C1", "#BA43A9", "#705DA0", "#508642", "#CCA300", "#447EBC", "#C15C17", "#890F02", "#0A437C", "#6D1F62", "#584477", "#70DBED", "#F9BA8F", "#F29191", "#82B5D8", "#E5A8E2", "#AEA2E0", "#629E51", "#E5AC0E", "#64B0C8", "#E0752D", "#BF1B00", "#0A50A1", "#962D82", "#614D93", "#9AC48A", "#F2C96D", "#65C5DB", "#F9934E", "#EA6460", "#5195CE", "#D683CE", "#806EB7", "#3F6833", "#967302", "#2F575E", "#99440A", "#58140C", "#052B51", "#511749", "#3F2B5B"]; // tslint:disable-line
    return colors[idx % colors.length];
}

export function isNoData(chartStatus: ChartStatus) {
    if (!chartStatus) {
        return false;
    }
    return [ChartStatusEnum.Loaded, ChartStatusEnum.UnLimit, ChartStatusEnum.Loading].indexOf(chartStatus.status) < 0;
}

export function convertColor(color: any, defaultValue: string) {
    if (color) {
        if (typeof color === "string") {
            return color;
        } else {
            return "rgba(" + color.r + "," + color.g + "," + color.b + "," + color.a + ")";
        }
    } else {
        return defaultValue;
    }
}

export function buildCanvas(resultSets: Array<ResultSetModel>, chart: Chart, selectedSeries: Array<string>,
                            uniqueId: string, initializeSeriesCache: Map<string, any>, indexStart?: number) {
    if (!resultSets) {
        return {};
    }
    const type: string = get(chart, "config.type", "line");
    switch (type) {
        case "radar":
        case "pie" :
            return buildPieReport(resultSets, type, chart, indexStart);
        default:
            return buildCanvasReport(resultSets, type, chart, selectedSeries, initializeSeriesCache, uniqueId, indexStart);
    }
}

export function convertChartType(type: string) {
    if (type === "area") {
        return "line";
    } else if (type === "column") {
        return "bar";
    }
    return type;
}

export function isSeriesChart(config: any) {
    const type = get(config, "type", "line");
    return type !== "pie" && type !== "radar";
}

function buildCanvasReport(resultSets: Array<ResultSetModel>, type: string, chart: Chart, selectedSeries: Array<string>,
                           initializeSeriesCache: Map<string, any>, uniqueId: string, indexStart?: number) {
    const nullValue = getConfigValue<string>(ChartEditConfig.display.series.nullAsZero, chart.config);
    const dashStyle = getConfigValue<string>(ChartEditConfig.display.series.dashStyle, chart.config);
    const opacity = getConfigValue<number>(ChartEditConfig.display.series.fillOpacity, chart.config) * 0.1;
    const showRight = get(chart, "config.config.rightyAxis.visible", false);
    const rightSeries = showRight ? get(chart, "config.config.rightyAxis.series", []) : [];
    let nullAsZone: boolean = true;
    if (!nullValue) {
        nullAsZone = true;
    } else {
        nullAsZone = nullValue.indexOf("zone") >= 0;
    }

    const connected = nullValue === "connectNulls";
    let reportData = [];
    let timeData = [];
    let idx = indexStart ? indexStart : 0;
    let minInterval = 0;
    let leftYAxesMaxValue = 0;
    let rightYAxesMaxValue = 0;
    const chartType = convertChartType(type);
    let computes: Map<string, Array<Array<string>>> = new Map<string, Array<Array<string>>>();
    if (resultSets) {
        resultSets.forEach(rs => {
            const interval = get(rs, "results.interval", 0);
            if ((minInterval === 0 || interval < minInterval) && interval != 0) {
                minInterval = interval;
            }
        });
        if (minInterval > 0) {
            for (let m = 0; m < resultSets.length; m++) {
                let resultSet: ResultSetModel = resultSets[m];
                let functions = resultSet.functions;
                let seriesDisplay = getDisplay(functions);
                let lineNum = getLineNum(functions);
                let computeArrays = getSeriesComputes(functions);
                if (resultSet) {
                    const metricType = resultSet.metricType;
                    const tagFilters = resultSet.tagFilters;
                    const monitorItem = resultSet.name;
                    let result: ResultModel = resultSet.results;
                    if (result) {
                        let display = getMetricDisplay(functions);
                        if (!display) {
                            continue;
                        }

                        let tagKey = getTagKey(functions);
                        let timeShift = getTimeShiftInterval(functions);
                        let countPs = getCountPS(functions);
                        let startTime = result.startTime;
                        let interval = result.interval;
                        const fixNull = interval / minInterval;
                        const measurementName = result.measurementName;
                        let groupModels: Array<GroupModel> = result.groups;
                        for (let j = 0; j < groupModels.length; j++) {
                            let groupModel = groupModels[j];
                            let groups: Map<string, string> = groupModel.group;

                            let group: string = "";
                            for (let key of Object.keys(groups)) {
                                group += groups[`${key}`] + "/";
                            }
                            if (group != "") {
                                group = group.substring(0, group.length - 1);
                            }
                            let fields: Map<string, Array<number>> = groupModel.fields;
                            if (fields) {
                                for (let key of Object.keys(fields)) {
                                    if (reportData && reportData.length > 100) {
                                        continue;
                                    }
                                    let values: Array<number> = fields[`${key}`];
                                    let data: any = {};
                                    let name = "";
                                    if (group == "") {
                                        name = key;
                                    } else {
                                        name = group;
                                    }
                                    data.rawName = name;

                                    name = getGroupName(name, functions);
                                    if (tagKey) {
                                        data.label = name + " " + tagKey;
                                    } else {
                                        data.label = name;
                                    }
                                    data.name = data.label;

                                    const isLeftYAxes = rightSeries.indexOf(data.label) < 0;
                                    data.yAxisID = isLeftYAxes ? "yAxes-left" : "yAxes-right";

                                    if (selectedSeries) {
                                        data.hidden = selectedSeries.indexOf(data.name) < 0;
                                    }
                                    data.type = chartType;
                                    const color = getColor(idx);
                                    data.borderColor = color;
                                    if (type === "area") {
                                        data.backgroundColor = Color(color).alpha(opacity).rgbString();
                                    }
                                    if (chartType === "bar") {
                                        data.backgroundColor = color;
                                    }

                                    idx++;
                                    data.data = [];

                                    if (timeShift) {
                                        data.borderDash = dashStyle.split(",");
                                    }

                                    data.metric = {
                                        tags: groups,
                                        interval: interval,
                                        timeShift: timeShift,
                                        functions: functions,
                                        name: measurementName.replace(monitorItem + ".", ""),
                                        fullName: measurementName,
                                        field: key,
                                        type: metricType,
                                        tagFilters: tagFilters,
                                        monitorItem: monitorItem
                                    };

                                    let total = 0;
                                    let count = 0;
                                    let max = null;
                                    let min = null;
                                    let current = null;
                                    const putLabels = (timeData.length == 0 || timeData.length < values.length);
                                    if (putLabels) {
                                        timeData = [];
                                    }
                                    for (let k = 0; k < values.length; k++) {
                                        let value: number = values[k];
                                        let time = startTime + k * interval;
                                        if (timeShift) {
                                            time = time - timeShift;
                                        }
                                        if (putLabels) {
                                            timeData.push(time);
                                            // for (let f = 1; f < fixNull; f++) {
                                            //     data.data.push(null);
                                            // }
                                        }

                                        // 计算（不参与 null as zero 的计算）
                                        if (value !== null) {
                                            count++;
                                        }

                                        if (nullAsZone && isEmpty(value)) {
                                            value = 0;
                                        }
                                        if (value && countPs) {
                                            value = value * 1000 / interval;
                                        }
                                        if (value) {
                                            value = Math.floor(value * 1000) / 1000;
                                        }
                                        data.data.push(value);
                                        for (let f = 1; f < fixNull; f++) {
                                            data.data.push(null);
                                        }

                                        // 计算（不参与 null as zero 的计算）
                                        if (value !== null) {
                                            total += value;
                                            current = value;
                                        }

                                        if (max === null || value > max) {
                                            max = value;
                                        }
                                        if (min === null || value < min) {
                                            min = value;
                                        }
                                        if (seriesDisplay) {
                                            if (!data.hidden && isLeftYAxes && value > leftYAxesMaxValue) {
                                                leftYAxesMaxValue = value;
                                            } else if (!data.hidden && !isLeftYAxes && value > rightYAxesMaxValue) {
                                                rightYAxesMaxValue = value;
                                            }
                                        }
                                    }

                                    data.spanGaps = connected || fixNull > 1;

                                    data.value = {
                                        [LegendValue.TOTAL]: total,
                                        [LegendValue.MAX]: max === null ? 0 : max,
                                        [LegendValue.MIN]: min === null ? 0 : min,
                                        [LegendValue.AVG]: count > 0 ? total / count : 0,
                                        [LegendValue.CURRENT]: current === null ? 0 : current
                                    };
                                    data.seriesDisplay = seriesDisplay;
                                    data.lineNum = lineNum;
                                    data.dataType = 0;
                                    reportData.push(data);
                                }
                            }
                        }
                    }
                }
                computes.set(lineNum, computeArrays);
            }
        }
    }

    if (timeData.length === 0) {
        return {};
    }
    let pointCount = timeData.length;
    const labels = buildChartLabels(timeData);

    initializeSeriesCache.set(uniqueId, {
        datasets: R.clone(reportData),
        times: timeData,
        labels: labels,
        interval: minInterval,
        leftMax: leftYAxesMaxValue,
        rightMax: rightYAxesMaxValue
    });

    // todo
    if (1 != 1) {
        let tempReports = [];
        let reports: Map<string, any> = new Map<string, any>();
        reportData.forEach(report => {
            let label = report.label;
            let lineNum = report.lineNum;
            reports.set(label + "***" + lineNum, report);
        });
        let left = 0, right = 0;
        let lines: Map<string, string> = new Map<string, string>();

        computes.forEach((computeArrays: Array<Array<string>>, lineNum: string) => {
            for (let i = 0; i < computeArrays.length; i++) {
                let compute = computeArrays[i];
                let data = buildFuncSeries(compute, pointCount, reports, lineNum);
                if (data) {
                    const isLeftYAxes = rightSeries.indexOf(data.label) < 0;
                    data.yAxisID = isLeftYAxes ? "yAxes-left" : "yAxes-right";
                    lines.set(lineNum, lineNum);
                    if (selectedSeries) {
                        data.hidden = selectedSeries.indexOf(data.name) < 0;
                    }
                    let value = data.value[`max`];
                    if (!data.hidden && isLeftYAxes && value > left) {
                        left = value;
                    } else if (!data.hidden && !isLeftYAxes && value > right) {
                        right = value;
                    }
                    tempReports.push(data);
                }
            }
        });
        reportData.forEach(report => {
            let lineNum = report.lineNum;
            if (!lines.get(lineNum)) {
                const isLeftYAxes = rightSeries.indexOf(report.label) < 0;
                report.yAxisID = isLeftYAxes ? "yAxes-left" : "yAxes-right";
                if (selectedSeries) {
                    report.hidden = selectedSeries.indexOf(report.name) < 0;
                }
                let value = report.value[`max`];
                if (!report.hidden && isLeftYAxes && value > left) {
                    left = value;
                } else if (!report.hidden && !isLeftYAxes && value > right) {
                    right = value;
                }
                tempReports.push(report);
            }

        });
        tempReports.forEach(((value, index) => {
            const color = getColor(index);
            if (type === "area") {
                value.backgroundColor = Color(color).alpha(opacity).rgbString();
            }
            if (chartType === "bar") {
                value.backgroundColor = color;
            }
            value.borderColor = color;
        }));
        leftYAxesMaxValue = left;
        rightYAxesMaxValue = right;
        reportData = tempReports;
    }
    return {
        datasets: reportData,
        times: timeData,
        labels: labels,
        interval: minInterval,
        leftMax: leftYAxesMaxValue,
        rightMax: rightYAxesMaxValue
    };
}

export function buildChartLabels(timeData: number[]) {
    const labels = [];
    const start = new Date(timeData[0]);
    const end = new Date(timeData[timeData.length - 1]);
    const range = timeData[timeData.length - 1] - timeData[0];
    const showTimeLabel = start.getDate() !== end.getDate() || start.getMonth() !== end.getMonth() || start.getFullYear() !== end.getFullYear();
    for (let i = 0; i < timeData.length; i++) {
        const time = timeData[i];
        const dateTime = moment(time);
        if (showTimeLabel) {
            labels.push(dateTime.format("MM/DD HH:mm"));
        } else if (range > 5 * 60 * 1000) {
            labels.push(dateTime.format("HH:mm"));
        } else {
            labels.push(dateTime.format("HH:mm:ss"));
        }
    }
    return labels;
}

function buildFuncSeries(computeMeta: Array<string>, pointCount: number, reports: Map<string, any>, lineNum: string) {
    let compute = computeMeta[0];
    let name = computeMeta[1];
    let n = compute.split("${").length - 1;
    if (n > 0) {
        let total = 0;
        let max = null;
        let min = null;
        let count = 0;
        let current = null;
        let data: any = {};
        data.data = [];
        data.rawName = name;
        data.label = name;
        data.name = name;
        for (let index = 0; index < pointCount; index++) {
            let post = 0;
            let targetCompute = compute;
            for (let i = 0; i < n; i++) {

                let prefix = compute.indexOf("${", post);
                let postfix = compute.indexOf("}", post);
                let temp = compute.substring(prefix, postfix + 1);
                let key = compute.substring(prefix + 2, postfix) + "***" + lineNum;
                let report: any = reports.get(key);
                if (!report) {
                    return null;
                }
                let datas: Array<number> = report.data;
                let num = datas[index];
                if (!num) {
                    num = 0;
                }
                data.type = report.type;
                data.borderColor = report.borderColor;
                data.backgroundColor = report.backgroundColor;
                data.dataType = 0;
                data.spanGaps = report.spanGaps;
                data.yAxisID = report.yAxisID;
                data.lineNum = lineNum;
                data.metric = report.metric;
                data.value = report.value;
                data.seriesDisplay = report.seriesDisplay;
                targetCompute = targetCompute.replace(temp, num + "");
                post = postfix + 1;
            }
            let value = (new Function("return " + targetCompute))();
            if (value) {
                value = Math.floor(value * 1000) / 1000;
            } else {
                value = 0;
            }
            total += value;
            current = value;
            if (max === null || value > max) {
                max = value;
            }
            if (min === null || value < min) {
                min = value;
            }
            count++;
            data.data.push(value);
        }
        data.value = {
            [LegendValue.TOTAL]: total,
            [LegendValue.MAX]: max === null ? 0 : max,
            [LegendValue.MIN]: min === null ? 0 : min,
            [LegendValue.AVG]: count > 0 ? total / count : 0,
            [LegendValue.CURRENT]: current === null ? 0 : current
        };
        return data;
    }
    return null;
}

function buildPieReport(resultSets: Array<any>, type: string, chart: Chart, indexStart?: number) {
    const reportData: any = {};
    let index: number = 1;
    let sum = 0;
    let idx = indexStart ? indexStart : 0;
    const data = [];
    const backgroundColor = [];
    const borderColor = [];
    const labels = [];
    const items = [];
    for (let m = 0; m < resultSets.length; m++) {
        let resultSet: ResultSetModel = resultSets[m];
        let functions: Array<ConvertFunctionModel> = resultSet.functions;
        let seriesDisplay = getDisplay(functions);
        if (resultSet) {
            let result: ResultModel = resultSet.results;
            if (result) {
                let display = getMetricDisplay(functions);
                if (!display) {
                    continue;
                }
                let tagKey = getTagKey(functions);
                let groupModels: Array<GroupModel> = result.groups;
                for (let j = 0; j < groupModels.length; j++) {
                    let groupModel = groupModels[j];
                    let groups: Map<string, string> = groupModel.group;
                    let group: string = "";
                    for (let key of Object.keys(groups)) {
                        group += groups[`${key}`] + "/";
                    }
                    if (group != "") {
                        group = group.substring(0, group.length - 1);
                    }
                    let fields: Map<string, Array<number>> = groupModel.fields;
                    if (fields) {
                        for (let key of Object.keys(fields)) {
                            let values: Array<number> = fields[`${key}`];
                            let sumValue: number = 0;
                            for (let k = 0; k < values.length; k++) {
                                let value: number = values[k];
                                if (value) {
                                    sumValue += value;
                                    sum += value;
                                }
                            }
                            let name = "";
                            if (group == "") {
                                name = key + " " + index;
                                index++;
                            } else {
                                name = group;
                            }
                            let rawName = name;
                            name = getGroupName(name, functions);
                            if (tagKey) {
                                name = name + " " + tagKey;
                            }
                            data.push(sumValue);
                            const color = getColor(idx);
                            borderColor.push(color);
                            if (type === "radar") {
                                backgroundColor.push(Color(color).alpha(0.4).rgbString());
                            } else {
                                backgroundColor.push(color);
                            }

                            labels.push(name);

                            items.push({
                                rawName: rawName,
                                name: name,
                                borderColor: color,
                                seriesDisplay: seriesDisplay,
                                value: {total: sumValue},
                                groups: groups
                            });
                            idx++;
                        }
                    }
                }
            }
        }
    }

    if (!isEmpty(data)) {
        reportData.datasets = [{
            data: data,
            backgroundColor: backgroundColor,
            borderColor: borderColor,
            pointBackgroundColor: borderColor
        }];
        reportData.labels = labels;
        reportData.items = items;
    }
    return reportData;
}

export function getTimeShiftInterval(functions: Array<ConvertFunctionModel>) {
    if (functions) {
        for (let func of functions) {
            if (func.modelEnum == ConvertFunctionEnum.TIME_SHIFT) {
                return timeRange(func.defaultParams[0]);
            }
        }
    }
    return 0;
}

function getSeriesComputes(functions: Array<ConvertFunctionModel>): Array<Array<string>> {
    let computes: Array<Array<string>> = [];
    if (functions) {
        for (let func of functions) {
            if (func.modelEnum == ConvertFunctionEnum.COMPUTE) {
                computes.push([func.defaultParams[0], func.defaultParams[1]]);
            }
        }
    }
    return computes;
}

export function getCountPS(functions: Array<ConvertFunctionModel>) {
    if (functions) {
        for (let func of functions) {
            if (func.modelEnum == ConvertFunctionEnum.COUNT_PS) {
                return true;
            }
        }
    }
    return false;
}

export function getMetricDisplay(functions: Array<ConvertFunctionModel>) {
    if (functions) {
        for (let func of functions) {
            if (func.name == "display") {
                return func.defaultParams[0];
            }
        }
    }
    return true;
}

export function getLineNum(functions: Array<ConvertFunctionModel>) {
    if (functions) {
        for (let func of functions) {
            if (func.name == "line_flag") {
                return func.defaultParams[0];
            }
        }
    }
    return "A";
}

export function calLineSequence(tarIndex: number): string {
    // 65:A 90:Z
    if (tarIndex < 26) {
        return String.fromCharCode(tarIndex + 65);
    }
    // from AA to ZZ
    let highRank = tarIndex / 26 - 1;
    let lowRank = tarIndex % 26;
    return String.fromCharCode(highRank + 65) + String.fromCharCode(lowRank + 65);
}

function getDisplay(functions: Array<ConvertFunctionModel>) {
    if (functions) {
        for (let func of functions) {
            if (func.name == ConvertFunctionEnum.TARGET_DISPLAY) {
                let display: boolean = func.defaultParams[0];
                return display;
            }
        }
    }
    return true;
}

export function getTagKey(functions: Array<ConvertFunctionModel>) {
    if (functions) {
        for (let func of functions) {
            if (func.modelEnum == ConvertFunctionEnum.TIME_SHIFT) {
                return func.defaultParams[0];
            }
        }
    }
    return null;
}

export function getGroupName(group: string, functions: Array<ConvertFunctionModel>) {
    let temp: string = group;
    if (functions) {
        for (let func of functions) {
            if (func.modelEnum == ConvertFunctionEnum.ALIAS_REPLACE) {
                let searchS: string = func.defaultParams[0];
                let replace: string = func.defaultParams[1];
                if (!isEmpty(searchS)) {
                    const reg = new RegExp(searchS);
                    if (reg.test(temp)) {
                        temp = replace;
                    }
                }
            } else if (func.modelEnum == ConvertFunctionEnum.ALIAS) {
                temp = func.defaultParams[0];
            } else if (func.modelEnum === ConvertFunctionEnum.ALIAS_PREFIX) {
                temp = func.defaultParams[0] + temp;
            } else if (func.modelEnum === ConvertFunctionEnum.ALIAS_POSTFIX) {
                temp += func.defaultParams[0];
            } else if (func.modelEnum === ConvertFunctionEnum.ALIAS_PATTERN) {
                // {{textA}}-{{textB}}-{{textC}}
                let pattern = func.defaultParams[0];
                let patternKeys = pattern.match(/{{\w+}}/g);
                if (patternKeys && patternKeys.length) {
                    patternKeys.forEach(onePatternKey => {
                        if (onePatternKey.length > 4) {
                            let key = onePatternKey.substring(2, onePatternKey.length - 2);
                            let indexInGroupName = group.indexOf(key + "=");
                            if (indexInGroupName >= 0) {
                                let value = group.substring(indexInGroupName + key.length + 2,
                                    group.indexOf("\"", indexInGroupName + key.length + 2));
                                pattern = pattern.replace(onePatternKey, value);
                            }
                        }
                    });
                }
                temp = pattern;
            }
        }
    }
    return temp;
}

function timeRange(ratio: string): number {
    if (ratio == null) {
        return null;
    }
    let length = ratio.length;
    let type: string = ratio.slice(length - 1);
    let interval: number = Number.parseInt(ratio.slice(0, length - 1), 10);
    let start: number = moment().unix();
    let end: number = moment().subtract(interval, type as DurationInputArg2).unix();
    return (start - end) * 1000;
}
