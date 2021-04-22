import React from "react";
import moment from "moment";
import {reaction} from "mobx";
import ChartJS from "chart.js";
import {Theme} from "$constants/Theme";
import {autobind} from "core-decorators";
import {cloneDeep, get, uniq} from "lodash";
import {isEmpty, setStyle} from "$utils/Util";
import {UnitModelEnum} from "$models/UnitModel";
import {DataFormatter} from "$utils/DataFormatter";
import StoreManager from "../../store/StoreManager";
import {ChartLegend, LegendValue} from "./ChartLegend";
import * as ChartDataConvert from "$utils/ChartDataConvert";
import {convertColor, isNoData, isSeriesChart} from "$utils/ChartDataConvert";
import {ChartStatus, ChartStatusEnum} from "$models/ChartModel";
import {AlertOrChange, AlertOrChangeEvent} from "$models/HolmesModel";
import {CANVAS_CHART_CONFIG, getVisibleInterval, getyAxesConfig} from "./ChartConfig";

import "chartjs-plugin-zoom";
import "chartjs-plugin-annotation";

const R = require("ramda");
const classNames = require("classnames");

interface SimpleCanvasChartProps {
    chart?: any;
    series?: any;
    height?: number; // 图表高度
    uniqueId: string;
    click?: any;
    legendHeight?: number;
}

interface SimpleCanvasChartState {
}

export default class SimpleCanvasChart extends React.Component<SimpleCanvasChartProps, SimpleCanvasChartState> {
    private eventCallbacks: Map<string, any> = new Map();

    private MinHeight = "280px";
    private MaxHeight = "480px";

    private chartContainer: any = {};
    private width: number = 0;
    private height: number = 0;

    private chartDiv;
    private chartRef;
    private crosshair;
    private legendRef;
    private chartStatus: ChartStatus;

    private chartObj = null; // 用于存储当前 chart 对象，用于控制点击效果，不需要响应式

    private theme: string;

    private series: any = {};

    private config;
    private click;

    private chartConfig;

    private disposer;
    private disposer2;

    // chart zoom for time range selection
    private drag: boolean = false;
    private isPointerDown: boolean = false;
    private selectedStart: number = 0;
    private selectedEnd: number = 0;

    constructor(props: SimpleCanvasChartProps) {
        super(props);

        const {uniqueId, chart, click} = props;
        StoreManager.loadingStore.register(uniqueId);
        this.config = cloneDeep(chart.config);
        this.click = click;
        this.theme = StoreManager.userStore.getTheme();
        this.disposer2 = reaction(
            () => StoreManager.userStore.user,
            () => {
                const theme = StoreManager.userStore.getTheme();
                if (theme) {
                    this.applyThemeToChart(Theme[theme]);
                }
            });
        this.addWatch(uniqueId);
    }

    addWatch(uniqueId: string) {
        this.disposer = reaction(
            () => StoreManager.loadingStore.chartStatusMap.get(uniqueId),
            chartStatus => {
                if (isEmpty(chartStatus)) {
                    return;
                }
                if (chartStatus === ChartStatusEnum.Loading) {
                    return;
                }
                let tempStatus = new ChartStatus();
                tempStatus.status = chartStatus;
                this.chartStatus = tempStatus;
                this.series = this.props.series;
                this.drawLegend();
            },
            {
                delay: 100
            }
        );
    }

    applyThemeToChart(theme: string) {
        if (this.theme != theme) {
            // Apply the theme
            this.theme = theme;
            // chart.js cannot modify options, so first destroy then create new chart
            this.destroy();
            this.draw();
        }
    }

    // componentDidMount() {
    //     this.chartStatus = ChartStatusEnum.Loaded;
    //     this.buildChartConfig();
    //     this.chartDiv.className = "emonitor-chart-wrapper";
    //     if (this.chartObj) {
    //         this.setData(this.chartObj);
    //         this.chartObj.update();
    //         this.drawPlotLine();
    //     } else {
    //         this.createChart();
    //     }
    //     this.drawLegend();
    // }

    componentWillReceiveProps(nextProps: Readonly<SimpleCanvasChartProps>, nextContext: any): void {
        if (this.chartObj
            && (!R.equals(get(nextProps.chart, "config.config", null), this.config)
                || !R.equals(get(nextProps.chart, "config.type", "line"), get(this.config, "type", "line")))) {

            this.destroy();

            const legendChanged = !R.equals(get(this.config, "config.legend", null), get(nextProps.chart, "config.config.legend", null))
                || get(this.config, "config.unit", null) !== get(nextProps.chart, "config.config.unit", null);

            this.config = cloneDeep(nextProps.chart.config);

            if (legendChanged) {
                this.drawLegend();
            } else {
                // if legend modify changed, legend will trigger chart draw
                this.buildChartConfig();
                this.draw();
            }
        }
    }

    componentWillUnmount() {
        if (this.disposer) {
            this.disposer();
        }
        if (this.disposer2) {
            this.disposer2();
        }
        this.destroy();
    }

    @autobind
    buildChartConfig() {
        if (this.chartConfig) {
            return;
        }
        const config = this.config;
        const chartConfig = Object.assign(cloneDeep(CANVAS_CHART_CONFIG), {config});
        const that = this;
        const chartType = get(config, "type", "line");
        const type = ChartDataConvert.convertChartType(chartType);
        const seriesChart = ChartDataConvert.isSeriesChart(config);
        const fontColor = this.theme === Theme.Dark ? "#E0E0E3" : "#666";
        const lineColor = this.theme === Theme.Dark ? "#707073" : "#d2d2d2";
        const unit = get(config, "config.unit", null);
        const decimals = get(config, "config.yAxis.decimals", 2);
        const stacked = get(config, "config.plotOptions.series.stacking", null);

        if (type === "radar") {
            chartConfig.options.scale = {
                ticks: {display: false, beginAtZero: true},
                gridLines: {color: lineColor, lineWidth: 0.2},
                angleLines: {color: lineColor, lineWidth: 0.2},
                pointLabels: {fontColor: fontColor, fontSize: 11}
            };
        } else {
            delete chartConfig.options.scale;
        }

        // set chart type
        chartConfig.type = type;
        // set tooltip options
        chartConfig.options.tooltips.enabled = !seriesChart;
        chartConfig.options.tooltips.mode = seriesChart ? "dataset" : "nearest";
        if (seriesChart) {
            chartConfig.options.tooltips.callbacks = {};
        } else {
            chartConfig.options.tooltips.callbacks = {
                label: function (tooltipItem: any, data: any) {
                    const idx = tooltipItem.index;
                    const val = data.datasets[tooltipItem.datasetIndex].data[idx];
                    let total = 0;
                    data.datasets[tooltipItem.datasetIndex].data.forEach(v => total += v);
                    const value = DataFormatter.tooltipFormatter(unit, val, decimals);
                    return data.labels[idx] + ": " + value + " (" + DataFormatter.tooltipFormatter(UnitModelEnum.Percent0_0, val / total) + ")";
                }
            };
            // disable zoom for not series chart
            chartConfig.options.zoom.enabled = false;
            chartConfig.options.zoom.drag = false;
        }
        // set xAxes options
        chartConfig.options.scales.xAxes[0].display = seriesChart;
        chartConfig.options.scales.xAxes[0].ticks.fontColor = fontColor;
        chartConfig.options.scales.xAxes[0].gridLines.color = lineColor;
        chartConfig.options.scales.xAxes[0].stacked = !!stacked;

        // set elements options
        const markerEnabled = type === "scatter" || type === "radar" || get(config, "config.plotOptions.series.marker.enabled", false);
        const radius = get(config, "config.plotOptions.series.marker.radius", 2);
        chartConfig.options.elements.line.fill = chartType === "area" || type === "radar";
        chartConfig.options.elements.point.radius = markerEnabled ? radius : 0;
        chartConfig.options.elements.point.hoverRadius = markerEnabled ? radius + 2 : 5;
        chartConfig.options.showLines = type !== "scatter";
        chartConfig.options.elements.line.borderWidth = get(config, "config.plotOptions.series.lineWidth", 1);

        // set yAxes options
        chartConfig.options.scales.yAxes[0] = getyAxesConfig(this.theme, config, chartConfig.options.scales.yAxes[0], "");
        chartConfig.options.scales.yAxes[1] = getyAxesConfig(this.theme, config, chartConfig.options.scales.yAxes[1], "right");

        chartConfig.options.scales.yAxes[0].gridLines.color = lineColor;
        chartConfig.options.scales.yAxes[1].gridLines.color = lineColor;

        // set threshold options
        this.setThresholdAnnotation(seriesChart, config, chartConfig);

        // set hover options
        chartConfig.options.hover.onHover = function (event: any) {
            if (!that.chartStatus || that.chartStatus.status === ChartStatusEnum.Loaded || that.chartStatus.status === ChartStatusEnum.UnLimit) {
                const activePoints = that.chartObj.getElementAtEvent(event);
                let isPointer = false;
                if (!activePoints || activePoints.length <= 0) {
                    isPointer = false;
                } else {
                    const chart = that.chartObj;
                    const point = activePoints[0];
                    const metric = get(chart.options.getSeries()[point._datasetIndex], "metric", null);
                    const metricType = get(metric, "type", null);

                    if (metricType) {
                        isPointer = true;
                    }
                }
                if (that.click) {
                    isPointer = true;
                }

                that.chartRef.style.cursor = isPointer || !seriesChart ? "pointer" : "crosshair";
            }
        };
        // set raw config ref
        chartConfig.options.rawConfig = config;
        // set series click
        chartConfig.options.seriesClick = this.seriesClick;
        chartConfig.options.uniqueId = this.props.uniqueId;
        chartConfig.options.isSeriesChart = seriesChart;
        chartConfig.options.isShowLegend = function () {
            // radar chart disable legend
            return get(that.config, "type", "line") != "radar" && get(that.config, "config.legend.show", true);
        };
        chartConfig.options.getSeries = function () {
            let tempSeries: Array<any> = get(that.series, !seriesChart ? "items" : "datasets", []);
            let series = [];
            tempSeries.forEach(value => {
                let display = value.seriesDisplay;
                if (display == undefined || display == true) {
                    series.push(value);
                }
            });
            return series;
        };
        this.chartConfig = chartConfig;
    }

    @autobind
    setThresholdAnnotation(seriesChart: boolean, config: any, chartConfig: any) {
        const yMin = get(config, "config.yAxis.plotBands[0].from", 0);
        const yMax = get(config, "config.yAxis.plotBands[0].to", 0);

        if (seriesChart && yMax > yMin && yMax !== 0) {
            chartConfig.options.annotation.annotations.push({
                type: "box",
                drawTime: "beforeDatasetsDraw",
                yScaleID: "yAxes-left",
                borderWidth: 0.1, // cannot set 0
                yMax: yMax,
                yMin: yMin,
                backgroundColor: convertColor(get(config, "config.yAxis.plotBands[0].fromColor", null), "rgba(252, 255, 197, 0.5)")
            });
            chartConfig.options.annotation.annotations.push({
                type: "box",
                drawTime: "beforeDatasetsDraw",
                yScaleID: "yAxes-left",
                borderWidth: 0.1, // cannot set 0
                yMin: yMax,
                backgroundColor: convertColor(get(config, "config.yAxis.plotBands[0].toColor", null), "rgba(249, 233, 233, 0.5)")
            });
        }
    }

    @autobind
    createChart() {
        this.setData(this.chartConfig);
        this.chartRef.width = this.width;
        this.chartRef.height = this.height;
        this.chartObj = new ChartJS(this.chartRef, this.chartConfig);

        this.addEventListener();

        if (isSeriesChart(this.config)) {
            if (this.crosshair) {
                this.chartObj.crosshair = this.crosshair;
            }
        }
    }

    @autobind
    clearAnnotations(chart: any) {
        if (chart.options.annotation) {
            chart.options.annotation.annotations = [];
            const seriesChart = ChartDataConvert.isSeriesChart(this.config);
            this.setThresholdAnnotation(seriesChart, this.config, chart);
        }
    }

    @autobind
    addAnnotation(chart: any, label: string, event: AlertOrChangeEvent) {
        const annotation = {
            scaleID: "xAxes-bottom",
            type: "line",
            mode: "vertical",
            value: label,
            borderColor: event._type == AlertOrChange.ALERT ? "#F56C6C" : "#27c594",
            borderWidth: 2, // cannot set 0
            drawTime: "afterDatasetsDraw",
        };
        chart.options.annotation.annotations.push(annotation);
    }

    @autobind
    buildPlotLine(chartConfig: any) {
        // 1.draw all plot line
        this.clearAnnotations(chartConfig);

        //
    }

    @autobind
    drawPlotLine() {
        if (!this.chartObj) {
            return;
        }

        this.buildPlotLine(this.chartObj);

        this.chartObj.update();
    }

    @autobind
    drawLegend() {
        this.buildChartConfig();

        const legendIsRight = this.isRight();
        if (legendIsRight) {
            this.chartDiv.className = "emonitor-chart-wrapper legend-to-right";
        } else {
            this.chartDiv.className = "emonitor-chart-wrapper";
        }
        this.legendRef.draw(this.chartConfig);
    }

    @autobind
    resize(): boolean {
        if (!this.chartDiv) {
            return;
        }

        const legendIsRight = this.isRight();

        let diff = false;
        if (this.width != this.chartDiv.clientWidth - this.legendRef.divRef.clientWidth ||
            this.height != this.chartDiv.clientHeight - this.legendRef.divRef.clientHeight) {
            if (legendIsRight) {
                this.width = this.chartDiv.clientWidth - this.legendRef.divRef.clientWidth;
                this.height = this.chartDiv.clientHeight;
            } else {
                this.height = this.chartDiv.clientHeight - this.legendRef.divRef.clientHeight;
                this.width = this.chartDiv.clientWidth;
            }
            diff = true;
        }

        // if chart area changed, reset chart container
        if (diff) {
            // 如果是retina(window.devicePixelRatio=2)，画的图是实际大小的2倍，需要注意
            // 其实不需要关注 ratio, chart.js 已经坐过相关处理
            // 真正的高宽设置应该在 new Chart(canvas, config) 前对 canvas 做高宽设置
            // 因为其内部是对 canvas 和其 container 的宽度作比较, 取小的一个
            // 如果没有设置 canvas, 默认 300*150

            this.chartContainer.chartWidth = this.chartRef.width;
            this.chartContainer.containerWidth = this.chartDiv.clientWidth;
            this.chartContainer.rect = this.chartRef.getBoundingClientRect();
        }

        return diff;
    }

    @autobind
    reflow() {
        this.draw(this.resize());
    }

    @autobind
    draw(resize: boolean = false) {
        if (this.chartObj) {
            this.setData(this.chartObj);
            this.chartObj.update();
            if (resize) {
                this.chartObj.resize();
            }
        } else {
            this.createChart();
        }
    }

    @autobind
    setData(chart: any) {
        const seriesChart = this.chartConfig.options.isSeriesChart;
        const labels = this.series.labels || [];
        if (seriesChart && labels.length > 0) {
            const times = get(this.series, "times", []);
            const interval = get(this.series, "interval", 10000);
            const visibleInterval = getVisibleInterval(times, interval, this.width);

            const perTickPX = this.width / (times.length - 1);
            let idx = 0;
            let firstIdx = -1;
            times.map((item: any, index: number) => {
                if (item % visibleInterval == 0) {
                    if (firstIdx === -1) {
                        firstIdx = index;
                    }
                    idx = index;
                }
            });
            if ((labels.length - idx) * perTickPX > 15) {
                labels[labels.length - 1] = "";
            }
            if (firstIdx * perTickPX < 20) {
                labels[firstIdx] = "";
            }
        }

        chart.options.status = this.chartStatus;
        chart.options.interval = this.series.interval;
        chart.options.times = this.series.times;
        chart.data.labels = labels;
        chart.data.datasets = this.series.datasets ? this.getShowSeries(this.series.datasets) : [];

        chart.options.scales.yAxes[0].ticks.suggestedMax = this.series.leftMax * 1.05;
        chart.options.scales.yAxes[1].ticks.suggestedMax = this.series.rightMax * 1.05;

        this.buildPlotLine(chart);
    }

    @autobind
    getShowSeries(datasets: Array<any>): Array<any> {
        let series = [];
        datasets.forEach(dataset => {
            let display = dataset.seriesDisplay;
            if (display == undefined || display == true) {
                series.push(dataset);
            }
        });
        return series;
    }

    @autobind
    destroy() {
        if (this.chartObj) {
            this.chartObj.destroy();
            this.chartObj = null;
            this.chartConfig = null;
        }
        const canvas = this.chartRef;

        this.eventCallbacks.forEach((v, k) => {
            canvas.removeEventListener(k, v);
        });

        this.eventCallbacks.clear();
    }

    @autobind
    addEventListener() {
        const canvas = this.chartRef;
        const chart = this.chartObj;
        const that = this;
        const seriesChart = this.chartConfig.options.isSeriesChart;
        const chartType = this.chartConfig.type;
        this.eventCallbacks.set("click", function (event: any) {
            if (seriesChart) {
                const activePoints = chart.getElementAtEvent(event);

                if (!activePoints || activePoints.length <= 0) {
                    return;
                }
                const point = activePoints[0];
                StoreManager.chartEventStore.seriesClick({
                    metric: chart.options.getSeries()[point._datasetIndex],
                    index: point._index,
                    timestamp: that.series.times[point._index],
                    uniqueId: that.props.uniqueId
                });
                if (that.click) {
                    that.click(that.series.times[point._index]);
                }
            } else if (chartType === "pie") {
                const activePie = chart.getElementAtEvent(event);
                if (activePie && activePie.length > 0 && activePie[0]._index >= 0) {
                    const series = that.chartConfig.options.getSeries();
                    StoreManager.chartEventStore.pieClick({
                        item: series[activePie[0]._index]
                    });
                }
            }
        });
        if (seriesChart) {
            this.eventCallbacks.set("pointermove", function (e: any) {
                // 禁止移动
                if (e.metaKey || e.altKey || e.shiftKey || e.ctrlKey) {
                    return;
                }

                // 体验优化，鼠标移动到坐标轴下的时候触发 MouseOut 事件
                if (e.offsetY > get(that.chartObj, "chartArea.bottom", e.offsetY)) {
                    StoreManager.chartEventStore.pointMouseOut(e);
                    return;
                }

                const points: any = that.chartObj.getElementsAtXAxis(e);
                if (!points || points.length <= 0) {
                    return;
                }

                const currValue = points[0]._index;
                if (that.isPointerDown) {
                    that.selectedEnd = that.series.times[currValue];
                    if (Math.abs(that.selectedEnd - that.selectedStart) >= 1000) {
                        that.drag = true;
                    }
                }

                // 所以的数据都转换成秒来处理，解决不同interval时画cross hair不对的问题
                const interval = get(that.chartObj, "config.options.interval", 0) / 1000;
                const v = currValue * interval;
                for (let key of Object.keys(ChartJS.instances)) {
                    const currChart = ChartJS.instances[`${key}`];
                    const type = get(currChart, "config.type");
                    // @ts-ignore
                    const crosshair = currChart.crosshair;
                    const status = get(currChart, "options.status", null);
                    if (!currChart.chartArea || !crosshair || isNoData(status)) {
                        continue;
                    }

                    const chartArea = currChart.chartArea;

                    const width = get(currChart, "scales.xAxes-bottom.width", 0);

                    const len = get(currChart, "config.data.labels.length", 0);
                    const i = get(currChart, "config.options.interval", 0) / 1000;

                    const x = type === "bar"
                        ? (currValue * 2 + 1) * width / (len * 2) + chartArea.left
                        : v / ((len - 1) * i) * width + chartArea.left;
                    if (x > chartArea.right) {
                        continue;
                    }
                    const top = chartArea.top;
                    const bottom = chartArea.bottom;

                    setStyle(crosshair, {
                        display: "block",
                        height: bottom - top + "px",
                        transform: `translate(${x}px, ${top}px)`
                    });
                }
                if (!that.chartObj.chartArea) {
                    return;
                }
                const currChartType = get(that.chartObj, "config.type");
                const area = that.chartObj.chartArea;
                const w = area.right - area.left;
                const length = get(that.chartObj, "config.data.labels.length", 0);

                StoreManager.chartEventStore.pointMouseOver({
                    index: currValue,
                    value: that.series.times[currValue],
                    left: currChartType === "bar"
                        ? (currValue * 2 + 1) * w / (length * 2) + area.left
                        : currValue / (length - 1) * w + area.left,
                    top: area.top,
                    bottom: area.bottom,
                    chart: that.chartObj,
                    chartContainer: that.chartContainer,
                    nativeEvent: e
                });
            });

            this.eventCallbacks.set("mouseleave", function (e: any) {
                for (let key of Object.keys(ChartJS.instances)) {
                    const currChart = ChartJS.instances[`${key}`];
                    // @ts-ignore
                    const crosshair = currChart.crosshair;

                    setStyle(crosshair, {
                        display: "none"
                    });
                }
                StoreManager.chartEventStore.pointMouseOut(e);
            });

            this.eventCallbacks.set("pointerdown", function (evt: any) {
                that.isPointerDown = true;
                const points: any = chart.getElementsAtEventForMode(evt, "index", {intersect: false});
                if (points && points.length > 0) {
                    that.selectedStart = that.series.times[points[0]._index];
                }
            });

            this.eventCallbacks.set("pointerup", function (evt: any) {
                that.isPointerDown = false;
                if (that.drag) {
                    const start = Math.min(that.selectedStart, that.selectedEnd);
                    const end = Math.max(that.selectedStart, that.selectedEnd);
                    const from = moment(start).format("YYYY-MM-DD HH:mm:ss");
                    const to = moment(end).format("YYYY-MM-DD HH:mm:ss");
                    StoreManager.urlParamStore.changeURLParams({from: from, to: to});
                }

                that.drag = false;
                that.selectedStart = 0;
                that.selectedEnd = 0;
            });
        }

        this.eventCallbacks.forEach((v, k) => {
            canvas.addEventListener(k, v, {passive: true});
        });
    }

    @autobind
    isRight() {
        return get(this.config, "config.legend.toRight", false);
    }

    shouldComponentUpdate(nextProps: Readonly<SimpleCanvasChartProps>, nextState: Readonly<SimpleCanvasChartState>, nextContext: any): boolean {
        return false;
    }

    /**
     * 控制对应Series点击行为，主要控制是否显示
     * @param e 原生事件对象
     * @param name 点击的series name
     * @param drawLegend draw legend
     */
    @autobind
    seriesClick(e: any, name: string, drawLegend: boolean = false) {
        const seriesChart = this.chartConfig.options.isSeriesChart;
        const series = this.chartConfig.options.getSeries();
        const allSeriesName: string[] = series.map(item => item.name);
        let selectedSeries = StoreManager.chartStore.selectedSeries.get(this.props.uniqueId);

        const selection = window.getSelection();
        // 如果选中的是 alias
        if (selection.toString() !== "" &&
            selection.anchorNode.parentElement.classList.contains("emonitor-chart-legend-alias")
        ) {
            return;
        }

        if (e.shiftKey || e.metaKey || e.ctrlKey || e.altKey) {
            if (!selectedSeries) {
                // selectedSeries 为空是第一次加载完数据后的时候
                selectedSeries = allSeriesName;
            }

            const index = selectedSeries.indexOf(name);
            if (index > -1) {
                selectedSeries.splice(index, 1);
            } else {
                selectedSeries.push(name);
            }
        } else {
            if (!selectedSeries) {
                // selectedSeries 为空是第一次加载完数据后的时候
                selectedSeries = series.length === 1 ? [] : [name];
            } else if (uniq(selectedSeries).length === 1 && uniq(selectedSeries)[0] === name) {
                if (uniq(allSeriesName).length === 1) {
                    // 如果只有一条 series
                    selectedSeries = [];
                } else {
                    selectedSeries = allSeriesName;
                }
            } else {
                selectedSeries = [name];
            }
        }

        // set chart legend selected
        StoreManager.chartStore.selectedSeries.set(this.props.uniqueId, selectedSeries);

        if (drawLegend) {
            // this.drawLegend();
            if (this.legendRef) {
                this.legendRef.draw(this.chartConfig, false);
            }
        }

        if (!this.chartObj) {
            return;
        }
        if (seriesChart) {
            let leftMax = 0;
            let rightMax = 0;
            series.map(item => {
                item.hidden = selectedSeries.indexOf(item.name) < 0;
                if (!item.hidden && item.value[LegendValue.MAX] > leftMax && item.yAxisID === "yAxes-left") {
                    leftMax = item.value[LegendValue.MAX];
                } else if (!item.hidden && item.value[LegendValue.MAX] > rightMax && item.yAxisID === "yAxes-right") {
                    rightMax = item.value[LegendValue.MAX];
                }
            });

            this.chartObj.options.scales.yAxes[0].ticks.suggestedMax = leftMax * 1.05;
            this.chartObj.options.scales.yAxes[1].ticks.suggestedMax = rightMax * 1.05;
        } else {
            const labels = get(this.chartObj, "data.labels", []);
            const dataSets = this.chartObj.getDatasetMeta(0);
            labels.map((label: string, idx: number) => {
                dataSets.data[idx].hidden = selectedSeries.indexOf(label) < 0;
            });
        }
        this.chartObj.update();
    }

    /**
     * chart height for chart solo view
     */
    @autobind
    getHeight() {
        let height = "280px";
        let inner = window.innerHeight - 210;
        if (StoreManager.boardStore.chart) {
            if (inner > 480) {
                height = this.MaxHeight;
            } else if (inner < 280) {
                height = this.MinHeight;
            } else {
                height = inner + "px";
            }
        } else if (this.props.height) {
            if (this.props.legendHeight) {
                height = (this.props.height + this.props.legendHeight) + "px";
            } else {
                height = this.props.height + "px";
            }
        }
        return height;
    }

    @autobind
    getChartHeight() {
        let height = "280px";
        let inner = window.innerHeight - 210;
        if (StoreManager.boardStore.chart) {
            if (inner > 480) {
                height = this.MaxHeight;
            } else if (inner < 280) {
                height = this.MinHeight;
            } else {
                height = inner + "px";
            }
        } else if (this.props.height) {
            height = this.props.height + "px";
        }
        return height;
    }

    render() {
        const height = this.getHeight();
        const chartHeight = this.getChartHeight();
        let chartWrapperClass = classNames({
            "emonitor-chart-wrapper": true,
            "legend-to-right": this.isRight(), // 是否置于右侧
        });

        return (
            <div className={chartWrapperClass} ref={element => this.chartDiv = element} style={{maxHeight: height, height: chartHeight}}>
                <div className="emonitor-chart-area" style={{height: chartHeight}}>
                    <canvas className="emonitor-chart" ref={ref => (this.chartRef = ref)}/>
                    <div ref={ref => this.crosshair = ref} className="emonitor-chart-crosshair"/>
                </div>
                <ChartLegend
                    ref={element => this.legendRef = element}
                    reflow={this.reflow}
                    maxHeight={this.props.legendHeight}
                />
            </div>
        );
    }
}
