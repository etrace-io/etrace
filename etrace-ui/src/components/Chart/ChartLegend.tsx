import React from "react";
import {autobind} from "core-decorators";
import {Input} from "antd";
import {get} from "lodash";
import {DataFormatter} from "../../utils/DataFormatter";
import {UnitModelEnum} from "../../models/UnitModel";
import StoreManager from "../../store/StoreManager";
import * as ChartDataConvert from "../../utils/ChartDataConvert";
import {default as ChartEditConfig, getConfigValue} from "../../containers/Board/Explorer/ChartEditConfig";

const classNames = require("classnames");

interface ChartLegendProps {
    reflow?: any;
    maxHeight?: any;
}

interface ChartLegendStates {
}

export enum LegendValue {
    MAX = "max",
    MIN = "min",
    AVG = "avg",
    TOTAL = "total",
    CURRENT = "current"
}

export class ChartLegend extends React.Component<ChartLegendProps, ChartLegendStates> {
    private VALUE_INDEX = [LegendValue.MIN, LegendValue.MAX, LegendValue.AVG, LegendValue.CURRENT, LegendValue.TOTAL]; // 顺序
    private divRef;
    private searchInput;

    private chartConfig;

    private isOrder = false; // 默认不排序
    private currSortCol = ""; // 默认排序列为 Total
    private isDesc = true; // 默认降序
    private rendered = false;
    private firstDraw = true;
    private MaxHeight = "60px";

    /**
     * 计算 Legend 中的 Value 并加上单位
     * @param value 需要换算的值
     * @param type 当前图标 y 轴单位
     * @param decimals 保留的小数位数
     */
    static _calcLegendValue(value: number, display: string, type: UnitModelEnum, decimals: number, interval: number) {
        value = +value.toFixed(decimals);
        if (interval && display == LegendValue.TOTAL) {
            value = value * interval / 1000;
        }
        return DataFormatter.formatterByUnit(type, value, decimals);
    }

    constructor(props: ChartLegendProps) {
        super(props);
        this.state = {};
    }

    @autobind
    draw(chartConfig: any, reflow: boolean = true) {
        this.chartConfig = chartConfig;

        if (this.firstDraw) {
            const order = get(this.chartConfig, "config.config.legend.sort.order", "");
            const rule = get(this.chartConfig, "config.config.legend.sort.rule", "");
            this.isOrder = order.length > 0;
            this.isDesc = order === "desc";
            this.currSortCol = rule;
            this.firstDraw = false;
        }

        if (reflow) {
            this.rendered = false;
        }
        this.forceUpdate();
    }

    componentDidUpdate(prevProps: Readonly<ChartLegendProps>, prevState: Readonly<ChartLegendStates>): void {
        if (this.divRef && !this.rendered && prevProps.reflow) {
            this.reSizeChart();
        }
        this.rendered = true;
    }

    @autobind
    reSizeChart() {
        if (!this.divRef || !this.props.reflow) {
            return;
        }
        this.props.reflow();
    }

    /**
     * 获取 Legend 的 Table 表头
     * @param legendInfo 配置项中的 legend.info
     */
    @autobind
    getLegendHeader(legendInfo: any) {
        const isSeriesChart = this.chartConfig.options.isSeriesChart;
        return this.VALUE_INDEX.map(value => {
            let headerClass = classNames({
                "emonitor-chart-legend-th-content": true,
                [value]: true,
                "order": this.currSortCol === value && this.isOrder,
                "desc": this.currSortCol === value && this.isDesc
            });
            if (legendInfo && legendInfo[value]) {
                return (
                    <span
                        key={value}
                        className={headerClass}
                        onClick={() => this.handleLegendHeaderClick(value)}
                    >
                        {value[0].toUpperCase() + value.substr(1).toLowerCase()}
                    </span>
                );
            } else {
                return null;
            }
        }).filter((value) => (value && !isSeriesChart && value.key == "total") || isSeriesChart);
    }

    /**
     * 获取各个 Legend 的 value DOM 集合
     * @param display 需要显示的 value 的 name 集合
     * @param item 当前 legend
     * @param decimals 需要 value 保留的小数位数
     * @param unitType 单位
     */
    getLegendValue(display: Array<string>, item: any, decimals: number, unitType: UnitModelEnum) {
        let interval;
        if (item.metric) {
            let functions = item.metric.functions;
            let isCountPS = ChartDataConvert.getCountPS(functions);
            if (isCountPS) {
                interval = item.metric.interval;
            }
        }
        return display.map(value => {
            return (
                <span key={value} className={`emonitor-chart-legend-value ${value}`}>
                    <span className="emonitor-chart-legend-value-label">
                        {value[0].toUpperCase() + value.substr(1)}:
                    </span>
                    {ChartLegend._calcLegendValue(item.value[value], value, unitType, decimals, interval)}
                </span>
            );
        });
    }

    /**
     * Legend 表头点击事件，控制排序
     * @param value 当前点击的表头的列的名称，如 total, avg, min, max, current
     */
    @autobind
    handleLegendHeaderClick(value: LegendValue) {
        if (this.currSortCol !== value || (!this.isDesc && !this.isOrder)) { // 第一次点击，重置为降序
            this.isOrder = true;
            this.isDesc = true;
            if (this.currSortCol !== value) {
                this.currSortCol = value;
            }
        } else if (!this.isDesc && this.isOrder) {
            // 第三次点击，无需排序
            this.isOrder = false;
        } else {
            // 第二次点击，置为升序
            this.isDesc = false;
        }

        this.forceUpdate();

        if (StoreManager.editChartStore.isEditing) {
            setTimeout(
                () => {
                    StoreManager.editChartStore.mergeChartConfig({
                        config: {
                            legend: {
                                sort: {
                                    rule: this.currSortCol,
                                    order: this.isOrder ? (this.isDesc ? "desc" : "asc") : ""
                                }
                            }
                        }
                    });
                },
                0
            );
        }
    }

    /**
     * 根据当前排序列以及是否排序返回其升降序 items
     */
    @autobind
    doOrderItems(items: Array<any>) {
        if (this.isOrder) {
            return items.slice().sort((a, b) => {
                return this.isDesc
                    ? b.value[this.currSortCol] - a.value[this.currSortCol]
                    : a.value[this.currSortCol] - b.value[this.currSortCol];
            });
        } else {
            return items;
        }
    }

    @autobind
    doItemSearch(series: Array<any>) {
        series.forEach(item => {
            if (this.searchInput && this.searchInput.length > 0) {
                const reg = new RegExp(this.searchInput.replace(/([.*+?^=!:${}()|[\]/\\])/g, "\\$&"), "i");
                item.collapse = !reg.test(item.name);
            } else {
                item.collapse = false;
            }
        });

        return series;
    }

    /**
     * 控制对应 Series 是否显示
     * @param e 原生事件对象
     * @param name 点击的 Legend 的 series name
     */
    handleLegendClick(e: any, name: string) {
        this.forceUpdate();

        if (this.chartConfig.options.seriesClick) {
            this.chartConfig.options.seriesClick(e, name);
        }
    }

    @autobind
    handleSeriesSearch(value: string) {
        this.searchInput = value;
        this.forceUpdate();
    }

    @autobind
    renderItems(items: Array<any>, displayValue: Array<any>, selectedSeries: Array<string>, decimals: number, unit: UnitModelEnum, filterFn?: any) {
        const seriesChart = ChartDataConvert.isSeriesChart(this.chartConfig);
        let count = 0;
        if (items && !seriesChart) {
            items.forEach((item: any) => {
                let value = item.value;
                count += value.total;
            });
        }
        return items && items.map((item: any, index: number) => {
            const name = item.name; // 对应的 series 名称
            if (filterFn && filterFn(name)) {
                return null;
            }
            const seriesClass = classNames({
                "emonitor-chart-legend-series": true,
                "selected": !selectedSeries || selectedSeries.indexOf(name) >= 0,
                "hidden": item.collapse
            });
            return (
                // 这里每个 series 为一个 table-row, 其子元素为各个 table-cell
                <div
                    className={seriesClass}
                    key={index}
                    onClick={(e) => this.handleLegendClick(e, name)}
                >
                    {/* Legend Alias */}
                    <span className="emonitor-chart-legend-alias">
                        <div className="emonitor-chart-legend-icon-wrapper">
                            <div
                                className="emonitor-chart-legend-icon point"
                                style={{backgroundColor: item.borderColor}}
                            />
                        </div>
                        {name}
                        {
                            !seriesChart && (" (" + DataFormatter.tooltipFormatter(UnitModelEnum.Percent0_0, item.value[LegendValue.TOTAL] / count) + ")")
                        }
                        </span>
                    {/* 控制需要显示的 Value */}
                    {this.getLegendValue(displayValue, item, decimals, unit)}
                </div>
            );
        });
    }

    render() {
        if (!this.chartConfig) {
            return null;
        }
        const config = get(this.chartConfig, "options.rawConfig", {});
        const legendConfig = get(config, "config.legend", {});
        const legendInfo = legendConfig.info; // 包含配置的 Legend 需要显示的 value
        // const toRight = get(legendConfig, "toRight", false);
        // const max = get(legendConfig, "maxItems", 50);
        const isSeriesChart = this.chartConfig.options.isSeriesChart;
        const series = this.chartConfig.options.getSeries();
        const toRight = getConfigValue<boolean>(ChartEditConfig.legend.config.toRight, config);
        const max = getConfigValue<number>(ChartEditConfig.legend.config.maxItem, config);
        const decimals = getConfigValue<number>(ChartEditConfig.legend.value.decimals, config); // get(legendConfig, "info.decimals", 2);
        const selectedSeries = StoreManager.chartStore.selectedSeries.get(this.chartConfig.options.uniqueId);
        const show = this.chartConfig.options.isShowLegend() && (toRight || series.length <= max);

        let items = [];
        if (show) {
            items = this.doItemSearch(series);
            items = this.doOrderItems(items);
        }

        const displayValue = legendInfo ? this.VALUE_INDEX.map(key => { // 从 config 获取需要显示的 value
            return legendInfo[key] === true
                ? key
                : null;
        }).filter((value) => value && ((!isSeriesChart && value == "total") || isSeriesChart)) : [];

        let maxHeight = this.MaxHeight;
        if (this.props.maxHeight) {
            maxHeight = this.props.maxHeight;
        }
        const showRight = get(config, "config.rightyAxis.visible", false);
        const rightSeries = !toRight && showRight ? get(config, "config.rightyAxis.series", []) : [];

        const leftUnit = get(this.chartConfig, "options.rawConfig.config.unit", "");
        const rightUnit = get(this.chartConfig, "options.rawConfig.config.rightunit", "");

        // for only left y-axes
        if (rightSeries.length == 0) {
            // const asTable = legendConfig.asTable; // 是否显示为 table
            // const width = get(legendConfig, "info.width", null);
            // const search = get(legendConfig, "search", false);
            // const layout = get(legendConfig, "layout", "center");
            const asTable = getConfigValue<boolean>(ChartEditConfig.legend.config.asTable, config);
            const width = getConfigValue<number>(ChartEditConfig.legend.config.width, config);
            const search = getConfigValue<boolean>(ChartEditConfig.legend.config.showSearch, config);
            const layout = getConfigValue<any>(ChartEditConfig.legend.config.layout, config);

            let legendWrapperClass = classNames({
                "emonitor-chart-legend-wrapper": true,
                "as-table": asTable
            });
            return (
                <div
                    ref={element => this.divRef = element}
                    className={legendWrapperClass}
                    style={{
                        width: width && toRight && items.length > 0 ? width + "px" : "auto",
                        textAlign: layout,
                        maxHeight: maxHeight
                    }}
                >
                    {show && (
                        <div className="emonitor-chart-legend">
                            {/* 如果 as table, 渲染表头 */}
                            {asTable && items.length > 0 && (
                                <div className="emonitor-chart-legend-table-header">
                                    {/* 用于占据 icon 和 alias 的位置 */}
                                    <span className="emonitor-chart-legend-th-content no-pointer">
                                {search && (
                                    <Input
                                        className="emonitor-chart-legend-search"
                                        size="small"
                                        placeholder="search"
                                        onChange={(e) => this.handleSeriesSearch(e.target.value)}
                                    />
                                )}
                            </span>
                                    {/* 表头部分 */}
                                    {legendInfo && this.getLegendHeader(legendInfo)}
                                </div>
                            )}
                            {this.renderItems(items, displayValue, selectedSeries, decimals, leftUnit)}
                        </div>
                    )}
                </div>
            );
        } else {
            return (
                <div
                    ref={element => this.divRef = element}
                    className="emonitor-chart-legend-wrapper"
                    style={{width: "auto", maxHeight: maxHeight}}
                >
                    {show && (
                        <div className="emonitor-chart-legend" style={{width: "100%"}}>
                            <div style={{float: "left"}}>
                                {this.renderItems(
                                    items,
                                    displayValue,
                                    selectedSeries,
                                    decimals,
                                    leftUnit,
                                    function (name: string) {
                                        return rightSeries.indexOf(name) >= 0;
                                    })
                                }
                            </div>
                            <div style={{float: "right"}}>
                                {this.renderItems(
                                    items,
                                    displayValue,
                                    selectedSeries,
                                    decimals,
                                    rightUnit,
                                    function (name: string) {
                                        return rightSeries.indexOf(name) < 0;
                                    })
                                }
                            </div>
                        </div>
                    )}
                </div>
            );
        }
    }
}
