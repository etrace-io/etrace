/**
 * 移植自 AbelHeinsbroek 的 https://www.npmjs.com/package/chartjs-plugin-crosshair
 */

import ChartJS, {ChartPoint} from "chart.js";
import {DOMKit} from "$utils/Util";

type Plugin = ChartJS.PluginServiceGlobalRegistration & ChartJS.PluginServiceRegistrationOptions;

interface Chart extends ChartJS {
    id?: number;
    crosshair?: any;
    panZoom?: (chart: Chart, increment: number) => void;
    scales?: any[];
    controller?: any;
    active?: any[];
}

declare module "chart.js" {
    interface PluginServiceGlobalRegistration {
        [name: string]: any;
    }
    interface ChartDataSets {
        interpolatedValue?: any;
    }
}

const helpers = ChartJS.helpers;

const defaultOptions = {
    line: {
        color: "#F66",
        width: 1,
        dashPattern: []
    },
    sync: {
        enabled: true,
        group: 1,
        suppressTooltips: false
    },
    zoom: {
        enabled: true,
        zoomboxBackgroundColor: "rgba(66,133,244,0.2)",
        zoomboxBorderColor: "#48F",
        zoomButtonText: "Reset Zoom",
        zoomButtonClass: "reset-zoom",
    },
    snap: {
        enabled: false,
    },
    callbacks: {
        beforeZoom: function(start: number, end: number) {
            return true;
        },
        afterZoom: function(start: number, end: number) {
            // ...
        }
    }
};

const CrosshairPlugin: Plugin = {
    id: "crosshair",
    afterInit(chart: Chart, options?: any) {
        if (chart.config.options.scales.xAxes.length === 0) {
            return;
        }

        const xScaleType = chart.config.options.scales.xAxes[0].type;

        if (
            xScaleType !== "linear" &&
            xScaleType !== "time" &&
            xScaleType !== "category" &&
            xScaleType !== "logarithmic"
        ) {
            return;
        }

        if (chart.options.plugins.crosshair === undefined) {
            chart.options.plugins.crosshair = defaultOptions;
        }

        // @ts-ignore
        chart.crosshair = {
            enabled: false,
            x: null,
            originalData: [],
            originalXRange: {},
            dragStarted: false,
            dragStartX: null,
            dragEndX: null,
            suppressTooltips: false,
            reset: () => this.resetZoom(chart, false, false),
        };

        const syncEnabled = this.getOption(chart, "sync", "enabled");
        if (syncEnabled) {
            chart.crosshair.syncEventHandler = e => this.handleSyncEvent(chart, e);

            chart.crosshair.resetZoomEventHandler = e => {
                const syncGroup = this.getOption(chart, "sync", "group");

                if (e.chartId !== chart.id && e.syncGroup === syncGroup) {
                    this.resetZoom(chart, true);
                }
            };

            window.addEventListener("sync-event", chart.crosshair.syncEventHandler);
            window.addEventListener("reset-zoom-event", chart.crosshair.resetZoomEventHandler);
        }

        chart.panZoom = this.panZoom.bind(this, chart);
    },
    afterEvent(chart: Chart, e: any, options?: any) {
        if (chart.config.options.scales.xAxes.length === 0) {
            return;
        }

        if (!DOMKit.isInViewPort(chart.canvas)) {
            return;
        }

        const xScaleType = chart.config.options.scales.xAxes[0].type;

        if (xScaleType !== "linear" && xScaleType !== "time" && xScaleType !== "category" && xScaleType !== "logarithmic") {
            return;
        }

        const xScale = this.getXScale(chart);

        if (!xScale) {
            return;
        }

        // fix for Safari
        const buttons = e.native.type === "mouseup"
            ? 0
            : (e.native.buttons === undefined ? e.native.which : e.native.buttons);

        const syncEnabled = this.getOption(chart, "sync", "enabled");
        const syncGroup = this.getOption(chart, "sync", "group");

        // fire event for all other linked charts
        if (!e.stop && syncEnabled && e.type !== "click") {
            const event = new CustomEvent("sync-event");
            const xValue = xScale.getValueForPixel(e.x);
            const valueCount = chart.data.labels.length;
            Object.assign(event, {
                chartId: chart.id,
                syncGroup: syncGroup,
                original: e,
                xValue,
                xRatio: xValue / valueCount,
            });
            window.dispatchEvent(event);
        }

        // suppress tooltips for linked charts
        const suppressTooltips = this.getOption(chart, "sync", "suppressTooltips");

        chart.crosshair.suppressTooltips = e.stop && suppressTooltips;

        chart.crosshair.enabled =
            e.type !== "mouseout" &&
            e.x >= chart.chartArea.left &&
            e.x <= chart.chartArea.right;

        if (!chart.crosshair.enabled) {
            if (e.x > xScale.getPixelForValue(xScale.max)) {
                chart.update();
            }
            return true;
        }

        // handle drag to zoom
        const zoomEnabled = this.getOption(chart, "zoom", "enabled");

        if (buttons === 1 && !chart.crosshair.dragStarted && zoomEnabled) {
            chart.crosshair.dragStartX = e.x;
            chart.crosshair.dragStarted = true;
        }

        // handle drag to zoom
        if (chart.crosshair.dragStarted && buttons === 0) {
            chart.crosshair.dragStarted = false;

            if (e.native?.target) {
                const start = xScale.getValueForPixel(chart.crosshair.dragStartX);
                const end = xScale.getValueForPixel(chart.crosshair.x);

                if (Math.abs(chart.crosshair.dragStartX - chart.crosshair.x) > 1) {
                    this.doZoom(chart, start, end);
                }
                chart.update();
            }
        }

        chart.crosshair.x = e.x;

        // @ts-ignore
        chart.draw();
    },
    afterDraw(chart: Chart, easing: Chart.Easing, options?: any) {
        if (!chart.crosshair.enabled) {
            return;
        }
        if (!DOMKit.isInViewPort(chart.canvas)) {
            return;
        }

        if (chart.crosshair.dragStarted) {
            this.drawZoombox(chart);
        } else {
            this.drawTraceLine(chart);
            // this.interpolateValues(chart);
            // this.drawTracePoints(chart);
        }

        return true;
    },
    destroy(chart: Chart) {
        const syncEnabled = this.getOption(chart, "sync", "enabled");
        if (syncEnabled) {
            window.removeEventListener("sync-event", chart.crosshair.syncEventHandler);
            window.removeEventListener("reset-zoom-event", chart.crosshair.resetZoomEventHandler);
        }
    },
    resetZoom(chart: Chart) {
        const stop = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;
        const update = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;

        if (update) {
            for (let datasetIndex = 0; datasetIndex < chart.data.datasets.length; datasetIndex++) {
                const dataset = chart.data.datasets[datasetIndex];
                dataset.data = chart.crosshair.originalData.shift(0);
            }

            const range = chart.options.scales.xAxes[0].time ? "time" : "ticks";

            // reset original xRange
            if (chart.crosshair.originalXRange.min) {
                chart.options.scales.xAxes[0][range].min = chart.crosshair.originalXRange.min;
                chart.crosshair.originalXRange.min = null;
            } else {
                delete chart.options.scales.xAxes[0][range].min;
            }
            if (chart.crosshair.originalXRange.max) {
                chart.options.scales.xAxes[0][range].max = chart.crosshair.originalXRange.max;
                chart.crosshair.originalXRange.max = null;
            } else {
                delete chart.options.scales.xAxes[0][range].max;
            }
        }

        if (chart.crosshair.button && chart.crosshair.button.parentNode) {
            chart.crosshair.button.parentNode.removeChild(chart.crosshair.button);
            chart.crosshair.button = false;
        }

        const syncEnabled = this.getOption(chart, "sync", "enabled");

        if (!stop && update && syncEnabled) {

            const syncGroup = this.getOption(chart, "sync", "group");

            const event = new CustomEvent("reset-zoom-event");
            Object.assign(event, {
                chartId: chart.id,
                syncGroup: syncGroup,
            });
            window.dispatchEvent(event);
        }
        if (update) {
            const prevAnimation = chart.options.animation;
            chart.options.animation.duration = 0;
            chart.update();
            chart.options.animation = prevAnimation;
        }
    },
    doZoom(chart: Chart, start: number, end: number) {
        // swap start/end if user dragged from right to left
        if (start > end) {
            [start, end] = [end, start];
        }

        // notify delegate
        const beforeZoomCallback = helpers.getValueOrDefault(
            chart.options.plugins.crosshair.callbacks
                ? chart.options.plugins.crosshair.callbacks.beforeZoom
                : undefined,
            defaultOptions.callbacks.beforeZoom
        );

        if (!beforeZoomCallback(start, end)) {
            return false;
        }

        if (chart.options.scales.xAxes[0].type === "time") {
            if (chart.options.scales.xAxes[0].time.min && chart.crosshair.originalData.length === 0) {
                chart.crosshair.originalXRange.min = chart.options.scales.xAxes[0].time.min;
            }
            if (chart.options.scales.xAxes[0].time.max && chart.crosshair.originalData.length === 0) {
                chart.crosshair.originalXRange.max = chart.options.scales.xAxes[0].time.max;
            }
        } else {
            if (chart.options.scales.xAxes[0].ticks.min && chart.crosshair.originalData.length === undefined) {
                chart.crosshair.originalXRange.min = chart.options.scales.xAxes[0].ticks.min;
            }
            if (chart.options.scales.xAxes[0].ticks.max && chart.crosshair.originalData.length === undefined) {
                chart.crosshair.originalXRange.max = chart.options.scales.xAxes[0].ticks.max;
            }
        }

        // if (!chart.crosshair.button) {
        //     // add restore zoom button
        //     const button = document.createElement("button");
        //
        //     const buttonText = this.getOption(chart, "zoom", "zoomButtonText");
        //     const buttonClass = this.getOption(chart, "zoom", "zoomButtonClass");
        //
        //     const buttonLabel = document.createTextNode(buttonText);
        //     button.appendChild(buttonLabel);
        //     button.className = buttonClass;
        //     button.addEventListener("click", () => this.resetZoom(chart));
        //     chart.canvas.parentNode.appendChild(button);
        //     chart.crosshair.button = button;
        // }

        // set axis scale
        if (chart.options.scales.xAxes[0].time) {
            chart.options.scales.xAxes[0].time.min = start.toString();
            chart.options.scales.xAxes[0].time.max = end.toString();
        } else {
            chart.options.scales.xAxes[0].ticks.min = start;
            chart.options.scales.xAxes[0].ticks.max = end;
        }

        // make a copy of the original data for later restoration
        const storeOriginals = chart.crosshair.originalData.length === 0;
        // filter dataset

        for (let datasetIndex = 0; datasetIndex < chart.data.datasets.length; datasetIndex++) {

            const newData = [];

            let index = 0;
            let started = false;
            let stop = false;
            if (storeOriginals) {
                chart.crosshair.originalData[datasetIndex] = chart.data.datasets[datasetIndex].data;
            }

            const sourceDataset = chart.crosshair.originalData[datasetIndex];

            for (let oldDataIndex = 0; oldDataIndex < sourceDataset.length; oldDataIndex++) {

                const oldData = sourceDataset[oldDataIndex];
                const oldDataX = this.getXScale(chart).getRightValue(oldData);

                // append one value outside of bounds
                if (oldDataX >= start && !started && index > 0) {
                    newData.push(sourceDataset[index - 1]);
                    started = true;
                }
                if (oldDataX >= start && oldDataX <= end) {
                    newData.push(oldData);
                }
                if (oldDataX > end && !stop && index < sourceDataset.length) {
                    newData.push(oldData);
                    stop = true;
                }
                index += 1;
            }

            chart.data.datasets[datasetIndex].data = newData;
        }

        chart.crosshair.start = start;
        chart.crosshair.end = end;

        if (storeOriginals) {
            const xAxes = this.getXScale(chart);
            chart.crosshair.min = xAxes.min;
            chart.crosshair.max = xAxes.max;
        }

        chart.update();

        const afterZoomCallback = this.getOption(chart, "callbacks", "afterZoom");

        afterZoomCallback(start, end);
    },
    drawZoombox(chart: Chart) {
        const yScale = this.getYScale(chart);

        const borderColor = this.getOption(chart, "zoom", "zoomboxBorderColor");
        const fillColor = this.getOption(chart, "zoom", "zoomboxBackgroundColor");

        chart.ctx.beginPath();
        chart.ctx.rect(
            chart.crosshair.dragStartX,
            yScale.getPixelForValue(yScale.max),
            chart.crosshair.x - chart.crosshair.dragStartX,
            yScale.getPixelForValue(yScale.min) - yScale.getPixelForValue(yScale.max)
        );
        chart.ctx.lineWidth = 1;
        chart.ctx.strokeStyle = borderColor;
        chart.ctx.fillStyle = fillColor;
        chart.ctx.fill();
        chart.ctx.fillStyle = "";
        chart.ctx.stroke();
        chart.ctx.closePath();
    },
    // 绘制 Crosshair
    drawTraceLine(chart: Chart) {
        const yScale = this.getYScale(chart);

        const lineWidth = this.getOption(chart, "line", "width");
        const color = this.getOption(chart, "line", "color");
        const dashPattern = this.getOption(chart, "line", "dashPattern");
        const snapEnabled = this.getOption(chart, "snap", "enabled");

        let lineX = chart.crosshair.x;
        const isHoverIntersectOff = chart.config.options.hover.intersect === false;

        if (snapEnabled && isHoverIntersectOff && chart.active.length) {
            lineX = chart.active[0]._view.x;
        }

        chart.ctx.beginPath();
        chart.ctx.setLineDash(dashPattern);
        chart.ctx.moveTo(lineX, yScale.getPixelForValue(yScale.max));
        chart.ctx.lineWidth = lineWidth;
        chart.ctx.strokeStyle = color;
        chart.ctx.lineTo(lineX, yScale.getPixelForValue(yScale.min));
        chart.ctx.stroke();
        chart.ctx.setLineDash([]);
    },
    drawTracePoints(chart: Chart) {
        for (let chartIndex = 0; chartIndex < chart.data.datasets.length; chartIndex++) {
            const dataset = chart.data.datasets[chartIndex];
            const meta = chart.getDatasetMeta(chartIndex);
            const yScale = chart.scales[meta.yAxisID];

            // @ts-ignore
            if (meta.hidden || !dataset.interpolate) {
                continue;
            }

            chart.ctx.beginPath();
            chart.ctx.arc(
                chart.crosshair.x,
                yScale.getPixelForValue(dataset.interpolatedValue),
                3,
                0,
                2 * Math.PI,
                false
            );
            chart.ctx.fillStyle = "white";
            chart.ctx.lineWidth = 2;
            chart.ctx.strokeStyle = dataset.borderColor.toString();
            chart.ctx.fill();
            chart.ctx.stroke();
        }
    },
    interpolateValues(chart: Chart) {
        for (let chartIndex = 0; chartIndex < chart.data.datasets.length; chartIndex++) {
            const dataset = chart.data.datasets[chartIndex];
            const meta = chart.getDatasetMeta(chartIndex);

            const xScale = chart.scales[meta.xAxisID];
            const xValue = xScale.getValueForPixel(chart.crosshair.x);

            // @ts-ignore
            if (meta.hidden || !dataset.interpolate) {
                continue;
            }

            const data = dataset.data as ChartPoint[];
            const index = data.findIndex(o => o.x >= xValue);

            const prev = data[index - 1];
            const next = data[index];

            if (chart.data.datasets[chartIndex].steppedLine && prev) {
                dataset.interpolatedValue = prev.y;
            } else if (prev && next) {
                const slope = (+next.y - +prev.y) / (+next.x - +prev.x);
                dataset.interpolatedValue = +prev.y + (xValue - +prev.x) * slope;
            } else {
                dataset.interpolatedValue = NaN;
            }
        }
    },
    panZoom(chart: Chart, increment: number) {
        if (chart.crosshair.originalData.length === 0) {
            return;
        }
        const diff = chart.crosshair.end - chart.crosshair.start;
        const min = chart.crosshair.min;
        const max = chart.crosshair.max;
        if (increment < 0) { // left
            chart.crosshair.start = Math.max(chart.crosshair.start + increment, min);
            chart.crosshair.end = chart.crosshair.start === min ? min + diff : chart.crosshair.end + increment;
        } else { // right
            chart.crosshair.end = Math.min(chart.crosshair.end + increment, chart.crosshair.max);
            chart.crosshair.start = chart.crosshair.end === max ? max - diff : chart.crosshair.start + increment;
        }

        this.doZoom(chart, chart.crosshair.start, chart.crosshair.end);
    },
    getOption(chart: Chart, category: any, name: any) {
        return helpers.getValueOrDefault(
            chart.options.plugins.crosshair[category]
                ? chart.options.plugins.crosshair[category][name]
                : undefined,
            defaultOptions[category][name]
        );
    },
    getXScale(chart: Chart) {
        return chart.data.datasets.length ? chart.scales[chart.getDatasetMeta(0).xAxisID] : null;
    },
    getYScale(chart: Chart) {
        return chart.scales[chart.getDatasetMeta(0).yAxisID];
    },
    handleSyncEvent(chart: Chart, e: any) {
        const syncGroup = this.getOption(chart, "sync", "group");

        // stop if the sync event was fired from this chart
        if (e.chartId === chart.id) {
            return;
        }

        // stop if the sync event was fired from a different group
        if (e.syncGroup !== syncGroup) {
            return;
        }

        const xScale = this.getXScale(chart);

        if (!xScale) {
            return;
        }

        // Safari fix
        const buttons = e.original.type === "mouseup"
            ? 0
            : (e.original.native.buttons === undefined
                ? e.original.native.which
                : e.original.native.buttons
            );

        const valueCount = chart.data.labels.length;
        const realXValue = Math.round(e.xRatio * valueCount);

        const newEvent = {
            type: e.original.type,
            chart: chart,
            // TODO 转 e.xValue 为真实的 value（根据位置的百分比）
            x: xScale.getPixelForValue(realXValue),
            // x: xScale.getPixelForValue(e.xValue),
            y: e.original.y,
            native: {
                buttons: buttons
            },
            stop: true
        };
        chart.controller.eventHandler(newEvent);
    },
};

export default CrosshairPlugin;
