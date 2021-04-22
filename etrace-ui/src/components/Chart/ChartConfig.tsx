import {get} from "lodash";
import ChartJS from "chart.js";
import {ChartStatusEnum} from "$models/ChartModel";
import {DataFormatter} from "$utils/DataFormatter";
import * as ChartDataConvert from "$utils/ChartDataConvert";
import {isNoData} from "$utils/ChartDataConvert";
import StoreManager from "../../store/StoreManager";
import {default as ChartEditConfig, getConfigValue} from "../../containers/Board/Explorer/ChartEditConfig";
import {Theme} from "$constants/Theme";

ChartJS.defaults.global.elements.line.borderWidth = 0;
ChartJS.defaults.global.elements.line.cubicInterpolationMode = "monotone";

export const INTERVALS = [10000, 30000, 60 * 1000, 2 * 60 * 1000, 5 * 60 * 1000, 10 * 60 * 1000, 15 * 60 * 1000, 30 * 60 * 1000];
export const EXTRA_PLOTLINES = "extraPlotLines";

export function getVisibleInterval(times: any, interval: number, width: number) {
    const ticksCount = times.length;
    const perTickPX = width / (ticksCount - 1);
    let ticksPerVisibleTick = parseInt("" + 100 / perTickPX);
    const t = ticksPerVisibleTick * interval;
    let result = interval;
    if (t < 60 * 60 * 1000) {
        INTERVALS.forEach(item => {
            if (t / item < 0.6) {
                return;
            } else {
                result = item;
            }
        });
    } else {
        result = t - t % (60 * 60 * 1000);
    }
    return result;
}

ChartJS.plugins.register({
    afterUpdate: function (chart: any) {
        const xAxes = chart.scales["xAxes-bottom"];
        const tickOffset = get(xAxes, "options.ticks.tickOffset", null);
        const display = get(xAxes, "options.display", false);
        if (display && tickOffset) {
            const width = get(chart, "scales.xAxes-bottom.width", 0);
            const interval = get(chart, "config.options.interval", 0);
            const times = get(chart, "config.options.times", []);
            const visibleInterval = getVisibleInterval(times, interval, width);

            xAxes.draw = function () {
                const xScale = chart.scales["xAxes-bottom"];
                const helpers = ChartJS.helpers;

                const tickFontColor = helpers.getValueOrDefault(xScale.options.ticks.fontColor, ChartJS.defaults.global.defaultFontColor);
                const tickFontSize = helpers.getValueOrDefault(xScale.options.ticks.fontSize, ChartJS.defaults.global.defaultFontSize);
                const tickFontStyle = helpers.getValueOrDefault(xScale.options.ticks.fontStyle, ChartJS.defaults.global.defaultFontStyle);
                const tickFontFamily = helpers.getValueOrDefault(xScale.options.ticks.fontFamily, ChartJS.defaults.global.defaultFontFamily);
                const tickLabelFont = helpers.fontString(tickFontSize, tickFontStyle, tickFontFamily);
                const tl = xScale.options.gridLines.tickMarkLength;

                const isRotated = xScale.labelRotation !== 0;
                const yTickStart = xScale.top;
                const yTickEnd = xScale.top + tl;
                const chartArea = chart.chartArea;

                helpers.each(
                    xScale.ticks,
                    function (label: any, index: any) {
                        if (times[index] % visibleInterval != 0) {
                            return;
                        }

                        // copy of chart.js code
                        let xLineValue = this.getPixelForTick(index);
                        const xLabelValue = this.getPixelForTick(index, this.options.gridLines.offsetGridLines);

                        if (this.options.gridLines.display) {
                            this.ctx.lineWidth = this.options.gridLines.lineWidth;
                            this.ctx.strokeStyle = this.options.gridLines.color;

                            xLineValue += helpers.aliasPixel(this.ctx.lineWidth);

                            // Draw the label area
                            this.ctx.beginPath();

                            if (this.options.gridLines.drawTicks) {
                                this.ctx.moveTo(xLineValue, yTickStart);
                                this.ctx.lineTo(xLineValue, yTickEnd);
                            }

                            // Draw the chart area
                            if (this.options.gridLines.drawOnChartArea) {
                                this.ctx.moveTo(xLineValue, chartArea.top);
                                this.ctx.lineTo(xLineValue, chartArea.bottom);
                            }

                            // Need to stroke in the loop because we are potentially changing line widths & colours
                            this.ctx.stroke();
                        }

                        if (this.options.ticks.display) {
                            this.ctx.save();
                            this.ctx.translate(xLabelValue + this.options.ticks.labelOffset, (isRotated) ? this.top + 12 : this.options.position === "top" ? this.bottom - tl : this.top + tl);
                            this.ctx.rotate(helpers.toRadians(this.labelRotation) * -1);
                            this.ctx.font = tickLabelFont;
                            this.ctx.textAlign = (isRotated) ? "right" : "center";
                            this.ctx.textBaseline = (isRotated) ? "middle" : this.options.position === "top" ? "bottom" : "top";
                            this.ctx.fillStyle = tickFontColor;
                            this.ctx.fillText(label, 0, 0);
                            this.ctx.restore();
                        }
                    },
                    xScale);
            };
        }
    },
    afterDatasetsDraw: function (chart: any) {
        const {locked, value} = StoreManager.chartEventStore.CrosshairInfo;
        const ctx = chart.chart.ctx;
        if (locked) {
            drawLockedCrosshair(ctx, chart, value);
        }
        const lines = get(chart, `config.config.${EXTRA_PLOTLINES}`, []);
        if (lines.length > 0) {
            for (let line of lines) {
                drawPlotLine(ctx, chart, line);
            }
        }
    },
    afterDraw: function (chart: any) {
        const status = get(chart, "options.status", null);

        const ctx = chart.chart.ctx;
        // const type = get(chart, "config.type", null);

        if (isNoData(status)) {
            chart.clear();
            const width = chart.chart.width;
            const height = chart.chart.height;
            let text = "";
            let color = get(chart, "options.scales.yAxes[0].ticks.fontColor", null);
            ctx.textAlign = "center";
            ctx.textBaseline = "middle";
            switch (status.status) {
                case ChartStatusEnum.NoData:
                    text = "No data to display";
                    break;
                case ChartStatusEnum.BadRequest:
                    text = "Invalid Configuration";
                    color = "#D27613";
                    break;
                case ChartStatusEnum.LoadError:
                    color = "#F56C6C";
                    ctx.fillStyle = color;
                    ctx.fillText(status.msg, width / 2, (height / 2) + 20);
                    text = "Internal Server Error";
                    break;
                default:
                    break;
            }
            ctx.font = "13px Arial";
            ctx.fillStyle = color;
            ctx.fillText(text, width / 2, height / 2);
            ctx.restore();
        } else if (chart.options.isSeriesChart) {
            const chartArea = chart.chartArea;

            ctx.beginPath();
            ctx.lineWidth = 1;
            ctx.strokeStyle = get(chart, "options.scales.yAxes[0].gridLines.color", null);
            ctx.moveTo(chartArea.left, chartArea.bottom);
            ctx.lineTo(chartArea.right, chartArea.bottom);
            ctx.stroke();
        }
    }
});

export const LEFT_Y_AXES = {
    id: "yAxes-left",
    position: "left",
    gridLines: {
        drawTicks: false,
        lineWidth: 0.3,
        autoSkip: true,
        tickMarkLength: 0,
        zeroLineWidth: 0,
        drawBorder: false,
        // drawOnChartArea: false,
        // borderDash: [1, 1],
        color: undefined,
    },
    ticks: {
        mirror: true, // draw tick in chart area
        padding: 2,
        display: true,
        min: 0,
        fontSize: 10,
        autoSkip: true,
        tickMarkLength: 0,
        maxTicksLimit: 6
    },
};

export const RIGHT_Y_AXES = {
    id: "yAxes-right",
    position: "right",
    display: false,
    gridLines: {
        drawBorder: false,
        drawTicks: false,
        display: false,
        color: undefined,
    },
    ticks: {
        mirror: true, // draw tick in chart area
        padding: 2,
        display: false,
        min: 0,
        fontSize: 10,
        autoSkip: true,
        maxTicksLimit: 6
    },
    color: undefined,
};

export const CANVAS_CHART_CONFIG = {
    data: {},
    options: {
        responsive: true,
        maintainAspectRatio: false,
        responsiveAnimationDuration: 0, // animation duration after a resize
        legend: {
            display: false
        },
        elements: {
            line: {
                tension: 0, // disables bezier curve
                borderWidth: 1,
                fill: undefined
            },
            point: {
                radius: 0,
                hoverRadius: undefined,
                pointStyle: undefined
            },
            arc: {
                borderWidth: 0
            }
        },
        tooltips: {
            mode: "dataset", enabled: undefined,
            callbacks: undefined

        },
        hover: {
            animationDuration: 0, // duration of animations when hovering an item
            mode: "index",
            intersect: false, onHover: undefined

        },
        animation: {
            duration: 0, // general animation time
        },
        zoom: {
            enabled: true,
            drag: true,
            mode: "x",
            limits: {
                max: 10,
                min: 0.5
            }
        },
        annotation: {
            events: ["click"],
            annotations: []
        },
        scales: {
            xAxes: [{
                id: "xAxes-bottom",
                type: "category",
                gridLines: {
                    drawTicks: false,
                    lineWidth: 0.3,
                    tickMarkLength: 2,
                    // drawOnChartArea: false,
                    drawBorder: false, color: undefined

                },
                ticks: {
                    fontSize: 10,
                    maxRotation: 0, // angle in degrees
                    tickOffset: 100,
                    fontColor: undefined
                },
                display: undefined,
                stacked: undefined
            }],
            yAxes: [LEFT_Y_AXES, RIGHT_Y_AXES]
        },

        scale: undefined,
        showLines: undefined,
        rawConfig: undefined,
        seriesClick: undefined,
        uniqueId: undefined,
        isSeriesChart: undefined,
        isShowLegend: undefined,
        getSeries: undefined
    },
    type: undefined
};

/**
 * 根据配置生成对应Y轴的配置
 * @param theme theme
 * @param config chart config
 * @param options yAxes options
 * @param yAxesType type
 */
export function getyAxesConfig(theme: string, config: any, options: any, yAxesType: string) {
    const seriesChart = ChartDataConvert.isSeriesChart(config);
    const fontColor = theme === Theme.Dark ? "#E0E0E3" : "#666";
    const lineColor = theme === Theme.Dark ? "#707073" : "#97979A";
    const unit = get(config, `config.${yAxesType}unit`, null);
    const decimals = get(config, `config.${yAxesType}yAxis.decimals`, 2);
    const visible = seriesChart && get(config, `config.${yAxesType}yAxis.visible`, yAxesType !== "right"); // left default show, right default hide
    options.display = visible;
    options.ticks.display = visible;
    const yAxisTitle = get(config, `config.${yAxesType}yAxis.title.text`, null);
    if (yAxisTitle) {
        options.scaleLabel = {
            display: true,
            fontColor: fontColor,
            labelString: yAxisTitle
        };
    } else {
        options.scaleLabel = {display: false};
    }

    const yAxisMin = get(config, `config.${yAxesType}yAxis.min`, null);
    const yAxisMax = get(config, `config.${yAxesType}yAxis.max`, null);

    options.ticks.min = yAxisMin || 0;
    if (yAxisMax) {
        options.ticks.max = yAxisMax;
    } else {
        delete options.ticks.max;
    }

    const stacked = getConfigValue<string>(ChartEditConfig.display.series.seriesStacking, config);
    options.stacked = !!stacked;

    if (unit) {
        options.ticks.callback = function (value: any, index: any, values: Array<number>) {
            if (index == 0) {
                return "";
            }
            return DataFormatter.tooltipFormatter(unit, value, decimals);
        };
    }

    options.ticks.fontColor = fontColor;
    options.gridLines.color = lineColor;

    return options;
}

function drawLockedCrosshair(ctx: CanvasRenderingContext2D, chart: any, value: number) {
    drawPlotLine(ctx, chart, value, true);
}

function drawPlotLine(ctx: CanvasRenderingContext2D, chart: any, value: number, dash: boolean = false) {
    const chartArea = chart.chartArea;

    const scale = chart.scales["xAxes-bottom"];
    const times = chart.options.times;
    if (!scale || !times) {
        return;
    }

    const oldIndex = chart.lockedCrosshairIndex;
    if ((oldIndex > 0 && value !== times[oldIndex]) || !oldIndex) {
        chart.lockedCrosshairIndex = getIndexOfArrayWithRound(times, value);
    }
    const index = chart.lockedCrosshairIndex;

    if (index === -1) {
        return;
    }

    const left = scale.getPixelForValue(undefined, index);
    if (!left) {
        return;
    }

    ctx.save();
    ctx.lineWidth = 1;
    if (dash) {
        ctx.setLineDash([3, 2]);
    }
    ctx.strokeStyle = "red";

    ctx.beginPath();
    ctx.moveTo(left, chartArea.top);
    ctx.lineTo(left, chartArea.bottom);
    ctx.stroke();
    ctx.restore();
}

function getIndexOfArrayWithRound(arr: number[], value: number, radius?: number) {
    let low = 0;
    let high = arr.length - 1;
    while (high - low > 1) {
        const mid = Math.floor((low + high) / 2);
        arr[mid] < value
            ? low = mid
            : high = mid;
    }
    const result = (value - arr[low] <= arr[high] - value) ? low : high;
    if (radius) {
        return Math.abs(arr[result] - value) <= radius ? result : -1;
    } else {
        return result;
    }
}
