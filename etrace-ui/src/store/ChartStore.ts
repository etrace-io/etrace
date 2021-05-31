import {action, observable, reaction, toJS} from "mobx";
import {get} from "lodash";
import * as LinDBService from "../services/LinDBService";
import {Chart, ChartStatus, ChartStatusEnum, ComputeTarget, Target} from "$models/ChartModel";
import {isEmpty} from "$utils/Util";
import * as ChartDataConvert from "../utils/ChartDataConvert";
import {calLineSequence, convertChartType, getColor, getLineNum, getMetricDisplay} from "$utils/ChartDataConvert";
import StoreManager from "./StoreManager";
import {EventStatusEnum} from "$models/HolmesModel";
import {OrderByStore} from "./OrderByStore";
import {URLParamStore} from "./URLParamStore";
import {MetricVariate} from "$models/BoardModel";
import {LegendValue} from "$components/Chart/ChartLegend";
import ChartJS from "chart.js";
import {default as ChartEditConfig, getConfigValue} from "../containers/Board/Explorer/ChartEditConfig";
import {SimpleJsonService} from "$services/SimpleJsonService";
import {ResultSetModel} from "$models/ResultSetModel";
import PrometheusService from "$services/PrometheusService";
import TargetKit from "$utils/UtilKit/TargetKit";

const R = require("ramda");
const Color = ChartJS.helpers.color;

export class ChartStore {
    orderByStore: OrderByStore;
    urlParamStore: URLParamStore;

    // 针对 report 页面去构建 target
    isReport: boolean = false;
    reportRule: Array<MetricVariate>;

    charts: Map<string, Chart> = new Map(); // for chart config
    seriesCache: Map<string, any> = new Map(); // for chart series data
    initializeSeriesCache: Map<string, any> = new Map(); // for chart series data
    chartErrorMap: Map<string, string> = new Map();
    chartTrackingMap: Map<string, Chart> = new Map(); // 主要跟踪Chart target和url params构建完的target对象是否有变理
    selectedSeries: Map<string, Array<string>> = new Map(); // for chart series selected
    awaitLoads: Map<String, boolean> = new Map();

    @observable public chartStatusMap: Map<string, ChartStatus> = new Map();
    @observable public chartIntervalMap: Map<string, number> = new Map();

    constructor(orderByStore: OrderByStore, urlParamStore: URLParamStore) {
        this.orderByStore = orderByStore;
        this.urlParamStore = urlParamStore;

        // listen url params if changed
        reaction(
            () => this.urlParamStore.changed,
            () => {
                this.load();
            });

        reaction(
            () => this.urlParamStore.forceChanged,
            () => {
                this.load(true);
            });
    }

    public load(forceLoad?: boolean) {
        setTimeout(
            () => {
                this.charts.forEach((v: ChartStatus, uniqueId: string) => {
                    this.loadChartData(uniqueId, forceLoad);
                });
            },
            0
        );
    }

    @action
    public register(uniqueId: string, chart: Chart, awaitLoad?: boolean) {
        if (typeof awaitLoad != "undefined") {
            this.awaitLoads.set(uniqueId, awaitLoad);
        }
        if (chart) {
            // for react component register too many times, when state change
            if (this.charts.has(uniqueId)) {
                return;
            }
            this.add(uniqueId, chart);
        }
    }

    @action
    public resetAwaitLoad(uniqueId: string) {
        this.awaitLoads.set(uniqueId, false);
    }

    @action
    public reRegister(uniqueId: string, chart: Chart) {
        if (chart) {
            this.add(uniqueId, chart);
        }
    }

    public add(uniqueId: string, chart: Chart) {
        this.charts.set(uniqueId, chart);
        // copy一份chart数据放在tracking中
        this.chartTrackingMap.set(uniqueId, R.clone(chart));
        this.chartStatusMap.set(uniqueId, {status: ChartStatusEnum.Init});
        if (!isEmpty(chart.targets)) {
            // record all prefix to query change/event
            chart.targets.map(value => {
                // if (value.display === false) {
                //     return;
                // }
                if (!isEmpty(value.prefix)) {
                    let prefix = value.prefix;
                    if (value.prefixVariate) {
                        prefix = StoreManager.urlParamStore.getValue(value.prefixVariate);
                    }
                    StoreManager.eventStore.register(prefix, {status: EventStatusEnum.Init});
                }
            });
        }
        this.loadChartData(uniqueId);
    }

    @action
    public unRegister(uniqueId: string) {
        this.charts.delete(uniqueId);
        this.seriesCache.delete(uniqueId);
        this.initializeSeriesCache.delete(uniqueId);
        this.chartTrackingMap.delete(uniqueId);
        this.selectedSeries.delete(uniqueId);
        this.chartErrorMap.delete(uniqueId);
        this.awaitLoads.delete(uniqueId);

        this.chartStatusMap.delete(uniqueId);
    }

    public getChart(uniqueId: string) {
        const chart: Chart = this.charts.get(uniqueId);
        if (isEmpty(chart)) {
            return {};
        }
        return toJS(chart);
    }

    @action
    public setChartStatus(uniqueId: string, chartStatus: ChartStatus) {
        this.chartStatusMap.set(uniqueId, toJS(chartStatus));
    }

    @action
    public setChartErrorStatus(uniqueId: string, error: string) {
        this.chartErrorMap.set(uniqueId, error);
    }

    @action
    public reComputeMetrics(uniqueId: string, chart: Chart) {
        this.charts.set(uniqueId, chart);
        this.chartTrackingMap.set(uniqueId, R.clone(chart));
        this.chartStatusMap.set(uniqueId, {status: ChartStatusEnum.Init});
        // let chartStatus: ChartStatus = this.chartStatusMap.get(uniqueId);
        // if (chartStatus && (chartStatus.status == ChartStatusEnum.Loading || chartStatus.status == ChartStatusEnum.Init)) {
        //     return;
        // }
        this.reBuildChartData(uniqueId);
    }

    public reBuildChartData(uniqueId: string) {
        let chartStatus: ChartStatus = this.chartStatusMap.get(uniqueId);
        const chart = this.charts.get(uniqueId);

        // 需要请求的全新target对象
        const targets = [];
        get(chart, "config.computes", []).forEach((value: ComputeTarget) => {
            if (!ComputeTarget.valid(value)) {
                return;
            }
            targets.push(value);
        });

        if (!chartStatus) {
            return;
        }

        if (targets.length > 0) {
            chartStatus.status = ChartStatusEnum.Loading;
            this.setChartStatus(uniqueId, chartStatus);
            let canvas: any = this.seriesCache.get(uniqueId);
            this.buildChartData(canvas, targets, chart, uniqueId);
            const limit = this.orderByStore.limit || 50;
            if (canvas && canvas.datasets) {
                if (this.datasetLength(canvas.datasets) >= limit) {
                    chartStatus.status = ChartStatusEnum.UnLimit;
                } else {
                    chartStatus.status = ChartStatusEnum.Loaded;
                }
            }
            this.seriesCache.set(uniqueId, canvas);
            chartStatus.status = ChartStatusEnum.Loaded;
            this.setChartStatus(uniqueId, chartStatus);
        } else {
            chartStatus.status = ChartStatusEnum.Loaded;
            this.setChartStatus(uniqueId, chartStatus);
        }
    }

    public buildChartData(canvas: any, targets: Array<ComputeTarget>, chartInfo: Chart, uniqueId: string) {
        if (canvas && canvas.datasets) {
            const nullValue = getConfigValue<string>(ChartEditConfig.display.series.nullAsZero, chartInfo.config);
            const type: string = get(chartInfo, "config.type", "line");
            const opacity = getConfigValue<number>(ChartEditConfig.display.series.fillOpacity, chartInfo.config) * 0.1;
            const showRight = get(chartInfo, "config.config.rightyAxis.visible", false);
            const rightSeries = showRight ? get(chartInfo, "config.config.rightyAxis.series", []) : [];
            const connected = nullValue === "connectNulls";
            let leftYAxesMaxValue = 0;
            let rightYAxesMaxValue = 0;
            const chartType = convertChartType(type);
            let reportData: Array<any> = canvas.datasets;

            let dataLength = reportData.length;
            let tempReport: Array<any> = [];
            for (let i = 0; i < dataLength; i++) {
                let report = reportData[i];
                if (report.dataType != 1) {
                    tempReport.push(report);
                }
            }
            reportData = tempReport;
            let selectedSeries = this.selectedSeries.get(uniqueId);

            const timeData: Array<any> = canvas.times;
            let length = timeData.length;
            if (reportData && reportData.length > 0) {
                let datasetMap: Map<String, Array<any>> = new Map<String, Array<any>>();
                reportData.forEach((dataset: any) => {
                    let lineNum = dataset.lineNum;
                    let temp = "${" + lineNum + "}";
                    let datas: Array<any> = datasetMap.get(temp);
                    if (!datas) {
                        datas = [];
                    }
                    datas.push(dataset);
                    datasetMap.set(temp, datas);
                });
                targets.forEach((target: ComputeTarget, tarIndex: number) => {
                    const display = target.display;
                    let alias = target.alias;
                    let lineNum = calLineSequence(tarIndex);
                    let compute = target.compute;
                    let pattern = /\$\{(.*?)\}/g;
                    let charts: Map<string, number> = new Map<string, number>();
                    let renderComputeFlag = true;
                    // 100*${AA}/${AB} ->  [AA, AB]
                    let matches: Array<any> = [];
                    let matched = pattern.exec(compute);
                    while (matched != null) {
                        matches.push(matched[1]);
                        matched = pattern.exec(compute);
                    }
                    for (let i = 0; i < matches.length; i++) {
                        let id = matches[i];
                        let count = charts.get(id);
                        if (!count) {
                            count = 0;
                        }
                        count++;
                        charts.set(id, count);
                    }
                    if (renderComputeFlag) {
                        let sekCounts: Array<string> = [];
                        charts.forEach((chart, num) => {
                            let temp = "${" + num + "}";
                            let dataLines: Array<any> = datasetMap.get(temp);
                            if (dataLines) {
                                sekCounts = this.nchoosek(sekCounts, dataLines, num);
                            }
                        });
                        let variateNum = charts.size;
                        for (let sekIndex = 0; sekIndex < sekCounts.length; sekIndex++) {
                            let data: any = {};
                            let sek = sekCounts[sekIndex];
                            let seks = sek.split("_");
                            let tempSeries: Map<string, any> = new Map<string, any>();
                            let rawName = "";
                            let label = "";
                            let name = "";
                            // 直接 colorIndex = target序列+总序列好了
                            let colorIndex = 1 + sekIndex + tarIndex;
                            for (let variateIndex = 0; variateIndex < variateNum; variateIndex++) {
                                let chart = seks[2 * variateIndex];
                                let serieIndex = Number.parseInt(seks[2 * variateIndex + 1]);
                                // colorIndex = variateNum * colorIndex * ((serieIndex || 0) + 1);
                                let temp = "${" + chart + "}";
                                let datasets = datasetMap.get(temp);
                                if (datasets) {
                                    let dataset = datasets[serieIndex];
                                    tempSeries.set(chart, dataset);
                                    rawName += dataset.rawName;
                                    label += dataset.label;
                                    name += dataset.name;
                                }
                            }
                            if (alias) {
                                label = alias;
                                name = alias;
                            }
                            data.rawName = rawName;
                            if (sekCounts.length > 1) {
                                data.label = label + "_" + sekIndex;
                                data.name = name + "_" + sekIndex;
                            } else {
                                data.label = label;
                                data.name = name;
                            }

                            data.type = chartType;
                            if (selectedSeries) {
                                data.hidden = selectedSeries.indexOf(data.name) < 0;
                            }
                            const color = getColor(colorIndex);

                            data.borderColor = color;
                            if (chartType === "area") {
                                data.backgroundColor = Color(color).alpha(opacity).rgbString();
                            }
                            if (chartType === "bar") {
                                data.backgroundColor = color;
                            }

                            data.data = [];
                            data.dataType = 1;
                            data.lineNum = "compute_" + lineNum;
                            data.spanGaps = connected;
                            let total = 0;
                            let count = 0;
                            let max = null;
                            let min = null;
                            let current = null;
                            const isLeftYAxes = rightSeries.indexOf(data.label) < 0;
                            data.yAxisID = isLeftYAxes ? "yAxes-left" : "yAxes-right";
                            for (let dataIndex = 0; dataIndex < length; dataIndex++) {
                                let targetCompute = target.compute;
                                for (let i = 0; i < matches.length; i++) {
                                    let id = matches[i];
                                    let temp = "${" + id + "}";
                                    let series: any = tempSeries.get(id);
                                    let datas: Array<number> = series ? series.data : [];
                                    let num = datas[dataIndex];
                                    if (num !== null) {
                                        targetCompute = targetCompute.replace(temp, num + "");
                                    }
                                }
                                // 对于计算指标，应该根据空值处理策略来选择value算法
                                // 无效的targetCompute，表达式存在未填充的占位符
                                let value = null;

                                const isNullAsZero = nullValue === "null_as_zone";
                                if (isNullAsZero) {
                                    // 根据空值策略，将null值处理为0；
                                    targetCompute = targetCompute.replace(/\$\{(.*?)\}/g, "0");
                                    value = (new Function("return " + targetCompute))();
                                } else if (targetCompute.indexOf("$") === -1) {
                                    // 不符合null as zero机制，但不存在空值。可以进行计算；
                                    value = (new Function("return " + targetCompute))();
                                } else {
                                    // 不符合null as zero机制，同时有存在空值。保留为null
                                }

                                count++;
                                if (value !== null) {
                                    value = Math.floor(value * 1000) / 1000;

                                    total += value;
                                    if (max === null || value > max) {
                                        max = value;
                                    }
                                    if (min === null || value < min) {
                                        min = value;
                                    }
                                }
                                current = value;
                                data.data.push(value);

                                if (!data.hidden && isLeftYAxes && value > leftYAxesMaxValue) {
                                    leftYAxesMaxValue = value;
                                } else if (!data.hidden && !isLeftYAxes && value > rightYAxesMaxValue) {
                                    rightYAxesMaxValue = value;
                                }
                            }
                            data.seriesDisplay = display;
                            data.value = {
                                [LegendValue.TOTAL]: total,
                                [LegendValue.MAX]: max === null ? 0 : max,
                                [LegendValue.MIN]: min === null ? 0 : min,
                                [LegendValue.AVG]: count > 0 ? total / count : 0,
                                [LegendValue.CURRENT]: current === null ? 0 : current
                            };
                            reportData.push(data);
                        }
                    }
                });
                if (leftYAxesMaxValue > canvas.datasets.leftMax) {
                    canvas.datasets.leftMax = leftYAxesMaxValue;
                }
                if (rightYAxesMaxValue > canvas.datasets.rightMax) {
                    canvas.datasets.rightMax = rightYAxesMaxValue;
                }
                canvas.datasets = reportData;
            }
        }
    }

    public nchoosek(inits: Array<string>, datasets: Array<any>, chartAt: string): Array<string> {
        let seks: Array<string> = [];
        if (!inits || inits.length == 0) {
            datasets.forEach((dataset, index) => {
                let temp = chartAt + "_" + index;
                seks.push(temp);
            });
        } else {
            inits.forEach((init, i) => {
                datasets.forEach((dataset, j) => {
                    let temp = init + "_" + chartAt + "_" + j;
                    seks.push(temp);
                });
            });
        }
        return seks;
    }

    public loadChartData(uniqueId: string, forceLoad: boolean = false) {
        let chartStatus: ChartStatus = this.chartStatusMap.get(uniqueId);
        if (chartStatus && chartStatus.status == ChartStatusEnum.Loading) {
            return;
        }

        const chart = toJS(this.charts.get(uniqueId));
        const previousChart = this.chartTrackingMap.get(uniqueId);

        const simpleJsonTarget: Array<Target> = [];
        const prometheusTarget: Array<Target> = [];
        // 需要请求的全新target对象
        const linDBTargets: Array<Target> = [];
        chartStatus.msgs = [];
        // let targets: Target[] = ;
        get(chart, "targets", []).forEach((value: Target, index: number) => {
            // if (value.display === false) {
            //     return;
            // }

            if (value.isAnalyze) {
                return;
            }
            // 记录行号信息，以防sunfire和lindb指标分别计算时丢失
            value.lineIndex = index;
            if (this.isSimpleJsonTarget(value)) {
                simpleJsonTarget.push(SimpleJsonService.buildTargetWithoutOrderBy(value));
            } else if (TargetKit.isPrometheusTarget(value)) {
                prometheusTarget.push(PrometheusService.buildTargetWithoutOrderBy(value));
            } else {
                const target: Target = LinDBService.buildTargetWithoutOrderBy(value, chart);

                const groupBys = target.groupBy;
                const fields = target.fields;
                // init
                let orderBy = target.orderBy;
                if (!orderBy) {
                    if (groupBys && groupBys.length > 0 && fields && fields.length > 0) {
                        let field: string = fields[0];
                        orderBy = this.orderKey(field);
                    }
                }
                target.orderBy = orderBy;

                // target is invalid
                if (!Target.valid(target)) {
                    let display = getMetricDisplay(target.functions);
                    if (display) {
                        let lineNum = getLineNum(target.functions);
                        if (!chartStatus.msgs) {
                            chartStatus.msgs = [];
                        }
                        chartStatus.msgs.push("Line " + lineNum + " : 指标配置不全！");
                    }
                    return;
                }
                linDBTargets.push(target);
            }
        });

        let newTargets: Array<Target> = [];
        newTargets.push(...linDBTargets);
        newTargets.push(...simpleJsonTarget);
        newTargets.push(...prometheusTarget);

        if (newTargets.length > 0) {
            let awaitLoad = this.awaitLoads.get(uniqueId);
            if (forceLoad || !R.equals(newTargets, previousChart.targets)) {
                if (!awaitLoad) {
                    chartStatus.status = ChartStatusEnum.Loading;
                    this.setChartStatus(uniqueId, chartStatus);

                    let queryArr: any[] = [];
                    if (linDBTargets.length > 0) {
                        queryArr.push(this.loadLinDBData(linDBTargets, chart, chartStatus, uniqueId));
                    }
                    if (simpleJsonTarget.length > 0) {
                        queryArr.push(this.loadSimpleJson(simpleJsonTarget, chart, chartStatus, uniqueId));
                    }
                    if (prometheusTarget.length > 0) {
                        let index = 0;
                        prometheusTarget.map(oneTarget =>
                            queryArr.push(this.loadOnePrometheus(oneTarget, chart, chartStatus, uniqueId, index++)));
                    }

                    // 如果同时存在 simplejson 和 lindb 数据源，需要对两者返回的数据进行合并；
                    Promise.all(queryArr).then(res => {
                        this.mergeAndSet(uniqueId, chartStatus, R.filter(x => !isEmpty(x), res));
                    });
                }
            }
        } else {
            this.seriesCache.delete(uniqueId);
        }

        // 设置新的target对象
        previousChart.targets = newTargets;

        // load event for chart
        StoreManager.eventStore.loadAll();
    }

    public datasetLength(datasets: Array<any>): number {
        let length = 0;
        datasets.forEach(dataset => {
            if (dataset.seriesDisplay == undefined || dataset.seriesDisplay) {
                length++;
            }
        });
        return length;
    }

    public orderKey(field: string): string {
        if (isEmpty(field)) {
            return field;
        }
        if (field.indexOf(" as ") >= 0) {
            field = field.split("as")[0].trimRight();
        }
        const regex = /^(t_max|t_min|stddev|variance|t_sum|t_gauge|t_mean)\((\S|\s)*\)$/;
        if (field.match(regex)) {
            return this.orderByStore.getOrderBy(field);
        }
        let type = this.orderByStore.type;
        field = type + "(" + field + ")";
        return this.orderByStore.getOrderBy(field);
    }

    private isSimpleJsonTarget(target: Target) {
        return target.entity == "sunfire";
    }

    private mergeAndSet(uniqueId: string, chartStatus: ChartStatus, reportDatas: any[]) {
        let mergedReportData = undefined;
        if (reportDatas.length == 1) {
            mergedReportData = reportDatas[0];
        } else if (reportDatas.length !== 0) {
            // 时间范围取并集
            const startTime = Math.min(...reportDatas.map(i => i.times[0]));
            const endTime = Math.min(...reportDatas.map(i => i.times[i.times.length - 1]));
            const interval = Math.min(...reportDatas.map(i => i.interval));
            const times = R.unfold(n => n > endTime ? false : [n, n + interval], startTime);
            mergedReportData = {
                datasets: [],
                interval: interval,
                times: times,
                labels: ChartDataConvert.buildChartLabels(times),
                leftMax: Math.max(...reportDatas.map(i => i.leftMax)),
                rightMax: Math.max(...reportDatas.map(i => i.rightMax)),
            };

            // 按照新的interval进行填充
            for (let i = 0; i < reportDatas.length; i++) {
                const thisTimes = reportDatas[i].times;
                for (let j = 0; j < reportDatas[i].datasets.length; j++) {
                    // 处理因为时间间隙不对而导致的对应位置没有填充为 null
                    const thisDataSet = reportDatas[i].datasets[j];
                    let newData = R.repeat(null, times.length);
                    for (let tsIdx = 0; tsIdx < thisTimes.length; tsIdx++) {
                        const index = times.indexOf(thisTimes[tsIdx]);
                        if (index >= 0) {
                            newData[index] = thisDataSet.data[tsIdx];
                        }
                    }
                    thisDataSet.data = newData;
                    mergedReportData.datasets.push(thisDataSet);
                }
            }
        }

        this.seriesCache.set(uniqueId, mergedReportData);
        if (!isEmpty(mergedReportData) && mergedReportData.datasets) {
            const limit = this.orderByStore.limit || 50;
            if (this.datasetLength(mergedReportData.datasets) >= limit) {
                chartStatus.status = ChartStatusEnum.UnLimit;
            } else {
                chartStatus.status = ChartStatusEnum.Loaded;
            }
            this.reBuildChartData(uniqueId);
        } else {
            chartStatus.status = ChartStatusEnum.NoData;
        }
        this.setChartStatus(uniqueId, chartStatus);
    }

    private loadOnePrometheus(oneTarget: any, chart: Chart, chartStatus: ChartStatus, uniqueId: string, indexStart?: number) {
        return PrometheusService.fetchMetricByTargets(oneTarget, indexStart).then((series: Array<ResultSetModel>) => {
            if (series) {
                let interval = 10000;
                this.chartIntervalMap.set(uniqueId, interval);
            }

            return ChartDataConvert.buildCanvas(series, chart, this.selectedSeries.get(uniqueId), uniqueId,
                this.initializeSeriesCache, indexStart);
        }).catch((error) => {
            this.handleLoadDataError(chartStatus, error, uniqueId);
        });
    }

    private loadSimpleJson(targets: any[], chart: Chart, chartStatus: ChartStatus, uniqueId: string) {
        return SimpleJsonService.searchMetrics(targets).then((series: Array<ResultSetModel>) => {
            if (series) {
                let interval = 10000;
                this.chartIntervalMap.set(uniqueId, interval);
            }

            return ChartDataConvert.buildCanvas(series, chart, this.selectedSeries.get(uniqueId), uniqueId, this.initializeSeriesCache);

        }).catch((error) => {
            this.handleLoadDataError(chartStatus, error, uniqueId);
        });
    }

    private loadLinDBData(targets: any[], chart: Chart, chartStatus: ChartStatus, uniqueId: string) {
        return LinDBService.searchMetrics(targets, chart).then(series => {
            if (series) {
                let interval = 10000;
                if (!isEmpty(targets[0].metricType)) {
                    series.forEach(item => {
                        item.metricType = targets[0].metricType;
                        item.tagFilters = targets[0].tagFilters;
                        if (item.results) {
                            interval = item.results.interval;
                        }
                    });
                } else {
                    series.forEach(item => {
                        if (item.results) {
                            interval = item.results.interval;
                        }
                    });
                }
                series.forEach(item => {
                    if (item.errorMsg && item.errorMsg !== "") {
                        let display = getMetricDisplay(item.functions);
                        if (display) {
                            let lineNum = getLineNum(item.functions);
                            if (!chartStatus.msgs) {
                                chartStatus.msgs = [];
                            }
                            chartStatus.msgs.push("Line " + lineNum + " : " + item.errorMsg);
                        }
                    }
                });
                this.chartIntervalMap.set(uniqueId, interval);
            }

            return ChartDataConvert.buildCanvas(series, chart, this.selectedSeries.get(uniqueId), uniqueId, this.initializeSeriesCache);

        }).catch((error) => {
            this.handleLoadDataError(chartStatus, error, uniqueId);
        });
    }

    private handleLoadDataError(chartStatus: ChartStatus, error: any, uniqueId: string) {
        chartStatus.status = ChartStatusEnum.LoadError;
        let resp = error.response;
        if (resp) {
            chartStatus.msg = resp.data.message;
            this.setChartErrorStatus(uniqueId, resp.data.message);
        } else {
            chartStatus.msg = "数据获取异常！";
            this.setChartErrorStatus(uniqueId, "数据获取异常！");
        }
        this.setChartStatus(uniqueId, chartStatus);
    }

}
