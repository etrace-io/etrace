import {ChartOptions, ChartXAxe, ChartYAxe} from "chart.js";
import {default as ChartEditConfig, getConfigValue} from "$containers/Board/Explorer/ChartEditConfig";
import {ChartInfo} from "$models/ChartModel";
import {ChartKit} from "$utils/Util";
import {DataFormatter} from "$utils/DataFormatter";

export default {
    getChartStyle,
    getYAxisConfig,
    getXAxisConfig,
};

export const EMONITOR_CHART_LEFT_YAXIS_ID = "yAxis-left";
export const EMONITOR_CHART_RIGHT_YAXIS_ID = "yAxis-right";
export const EMONITOR_CHART_BOTTOM_XAXIS_ID = "xAxis-bottom";

// 颜色相关
export const EMONITOR_CHART_FONT_COLOR_LIGHT = "#666";
export const EMONITOR_CHART_GRID_COLOR_LIGHT = "#d2d2d2";

// 底部横坐标轴
export const EMONITOR_CHART_BOTTOM_X_AXIS: ChartXAxe = {

};

// 默认 Chart 配置项
export const EMONITOR_CHART_DEFAULT_OPTIONS: ChartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    responsiveAnimationDuration: 0, // animation duration after a resize
    // 不显示 Legend，使用自定义 Legend
    legend: {
        display: false
    },
    elements: {
        line: {
            // 禁用贝塞尔曲线
            tension: 0,
        },
        // 点样式
        point: {
            radius: 0,
            hoverRadius: 5,
            // pointStyle: undefined
        },
        arc: {
            borderWidth: 0
        }
    },
    // 在点上移动
    hover: {
        animationDuration: 0, // duration of animations when hovering an item
        mode: "index",
        intersect: false,
    },
    animation: {
        duration: 0, // general animation time
    },
    // zoom: {
    //     enabled: true,
    //     drag: true,
    //     mode: "x",
    //     limits: {
    //         max: 10,
    //         min: 0.5
    //     }
    // },
    // annotation: {
    //     events: ["click"],
    //     annotations: []
    // },

    // scales: {
    //     // 坐标轴在 ChartKit 中设置
    //     xAxes: [],
    //     yAxes: [],
    // },

    // scale: undefined,

    // rawConfig: undefined,
    // seriesClick: undefined,
    // uniqueId: undefined,
    // isSeriesChart: undefined,
    // isShowLegend: undefined,
    // getSeries: undefined
};

// 默认 Chart 样式配置
export function getChartStyle() {
    return {
        fontColor: EMONITOR_CHART_FONT_COLOR_LIGHT,
        gridColor: EMONITOR_CHART_GRID_COLOR_LIGHT,
        fontSize: 10,
        gridLineWidth: 0.3,
    };
}

// 生成纵坐标配置
export function getYAxisConfig(position: "left" | "right", chart?: ChartInfo): ChartYAxe {
    const chartConfig = chart?.config;
    // if (!chartConfig) { return null; }

    const {fontColor, gridColor, fontSize, gridLineWidth} = getChartStyle();
    const isLeft = position === "left";
    const isSeriesChart = ChartKit?.isSeriesChart(chart) ?? false;

    // 单位
    const unit = getConfigValue(
        isLeft ? ChartEditConfig.axis.leftYAxis.unit : ChartEditConfig.axis.rightYAxis.unit,
        chartConfig
    );

    // 保留小数位
    const decimals = getConfigValue(
        isLeft ? ChartEditConfig.axis.leftYAxis.decimals : ChartEditConfig.axis.rightYAxis.decimals,
        chartConfig
    );

    // 是否展示当前坐标轴
    const display = isSeriesChart && getConfigValue(
        isLeft ? ChartEditConfig.axis.leftYAxis.visible : ChartEditConfig.axis.rightYAxis.visible,
        chartConfig
    );

    // 坐标轴标题
    const title = getConfigValue(
        isLeft ? ChartEditConfig.axis.leftYAxis.title : ChartEditConfig.axis.rightYAxis.title,
        chartConfig
    );

    // 坐标轴最大值
    const max = getConfigValue(
        isLeft ? ChartEditConfig.axis.leftYAxis.max : ChartEditConfig.axis.rightYAxis.max,
        chartConfig
    );

    // 坐标轴最小值
    const min = getConfigValue(
        isLeft ? ChartEditConfig.axis.leftYAxis.min : ChartEditConfig.axis.rightYAxis.min,
        chartConfig
    );

    // 是否堆叠
    const stacked = !!getConfigValue(
        ChartEditConfig.display.series.seriesStacking,
        chartConfig
    );

    return {
        id: isLeft ? EMONITOR_CHART_LEFT_YAXIS_ID : EMONITOR_CHART_RIGHT_YAXIS_ID,
        position,
        display,
        stacked,
        ticks: {
            display,
            max: max ?? undefined,
            // 中坐标默认从 0 开始计算
            min: min ?? 0,
            fontColor,
            fontSize,
            // 纵轴刻度单位转换
            callback: unit ? (value: number | string, index: number, values: number[] | string[]) => (
                index !== 0 ? DataFormatter.tooltipFormatter(unit, value, decimals) : ""
            ) : undefined,
            // 刻度是否在图内绘制
            mirror: true,
            autoSkip: true,
            maxTicksLimit: 6,
            labelOffset: -fontSize / 2,
        },
        // 坐标轴标题
        scaleLabel: {
            display: !!title,
            fontColor,
            labelString: title,
        },
        // 图内网格线
        gridLines: {
            // drawTicks: false,
            drawBorder: true,
            color: gridColor,
            lineWidth: gridLineWidth,
            // 隐藏零刻度线
            zeroLineWidth: gridLineWidth,
            zeroLineColor: gridColor,
            // 网格线将绘制到轴区域中的长度（以像素为单位）
            tickMarkLength: 0,
        },
    };
}

// 生成横坐标配置
export function getXAxisConfig(chart?: ChartInfo): ChartXAxe {
    const {fontColor, gridColor, fontSize, gridLineWidth} = getChartStyle();
    const isSeriesChart = ChartKit?.isSeriesChart(chart) ?? false;

    const chartConfig = chart?.config;

    const stacked = !!getConfigValue(ChartEditConfig.display.series.seriesStacking, chartConfig);

    return {
        id: EMONITOR_CHART_BOTTOM_XAXIS_ID,
        type: "category",
        display: isSeriesChart,
        stacked,
        gridLines: {
            drawTicks: false,
            lineWidth: gridLineWidth,
            tickMarkLength: 2,
            drawBorder: false,
            color: gridColor,
            // 隐藏零刻度线
            zeroLineWidth: 0,
        },
        ticks: {
            fontSize,
            fontColor,
            // 刻度不旋转
            maxRotation: 0,
            // maxTicksLimit: 6,
            autoSkip: true,
            autoSkipPadding: 100,
            beginAtZero: false,
        },
    };
}

/* 插件配置 */
export const EMONITOR_CHART_PLUGIN_CROSSHAIR = {
    line: {
        color: "#ff0e0b",        // crosshair line color
        width: 0.5,             // crosshair line width
        dashPattern: [5, 2]   // crosshair line dash pattern
    },
    // sync: {
    //     enabled: true,            // enable trace line syncing with other charts
    //     group: 1,                 // chart group
    //     suppressTooltips: true   // suppress tooltips when showing a synced tracer
    // },
    zoom: {
        // button: false,
        // enabled: false,                                     // enable zooming
        // zoomboxBackgroundColor: "rgba(66,133,244,0.2)",     // background color of zoom box
        // zoomboxBorderColor: "#48F",                         // border color of zoom box
        // zoomButtonText: "Reset Zoom",                       // reset zoom button text
        // zoomButtonClass: "reset-zoom",                      // reset zoom button class
    },

    // callbacks: {
    //     beforeZoom: function(start: any, end: any) {                  // called before zoom, return false to prevent zoom
    //         console.log(start, end)
    //         return true;
    //     },
    //     afterZoom: function(start, end) {                   // called after zoom
    //     }
    // }
};
