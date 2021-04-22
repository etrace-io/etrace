import classNames from "classnames";
import {ChartKit} from "$utils/Util";
import React, {useMemo, useState} from "react";
import {UnitModelEnum} from "$models/UnitModel";
import {DataFormatter} from "$utils/DataFormatter";
import LegendSeries from "$components/EMonitorChart/Legend/LegendSeries";
import LegendTableHeader from "$components/EMonitorChart/Legend/LegendTableHeader";
import {default as ChartEditConfig, getConfigValue} from "$containers/Board/Explorer/ChartEditConfig";
import {
    ChartInfo,
    ChartSeriesInfo,
    ChartTypeEnum,
    EMonitorChartVisualData,
    LegendSort,
    LegendValue
} from "$models/ChartModel";

import "./ChartLegend.less";

// 表格展示取值列顺序
export const LegendValueIndex = [LegendValue.MIN, LegendValue.MAX, LegendValue.AVG, LegendValue.CURRENT, LegendValue.TOTAL];

const ChartLegend: React.FC<{
    chart: ChartInfo,
    dataSource: EMonitorChartVisualData,
    maxHeight?: number,
    selectedKeys?: string[],
    onSelect?(legend: ChartSeriesInfo, nativeEvent: React.MouseEvent<HTMLDivElement>): void,
}> = props => {
    const {dataSource, chart, maxHeight = 60, onSelect, selectedKeys} = props;
    const chartConfig = chart.config;
    const chartType = ChartKit.getChartType(chart);
    const isSeriesChart = ChartKit.isSeriesChart(chart);
    const datasets = dataSource?.datasets ?? [];
    const rightSeries = getConfigValue(ChartEditConfig.axis.rightYAxis.series, chartConfig) ?? [];

    const [searchValue, setSearchValue] = useState<string>();
    const [sort, setSort] = useState<LegendSort>(null);
    const [sortBy, setSortBy] = useState(null);

    // 所有图例数据
    const allLegendItems = useMemo<ChartSeriesInfo[]>(() => datasets.map((dataset, index): ChartSeriesInfo => {
        return {
            label: dataset.label,
            computedValue: dataset.value,
            datasetIndex: index,
            isRightAxis: rightSeries.includes(dataset.label),
            // 搜索过滤
            collapse: searchValue
                ? !(new RegExp(searchValue.replace(/([.*+?^=!:${}()|[\]/\\])/g, "\\$&"), "i")).test(dataset.label)
                : false,
        };
    }), [datasets, searchValue]);

    const areaChartTotalCount = useMemo(
        () => isSeriesChart ? 0 : allLegendItems.reduce((a, b) => a + b.computedValue.total, 0),
        [isSeriesChart, allLegendItems]
    );

    // 用于展示的数据
    const legendList = useMemo<ChartSeriesInfo[]>(() => {
        if (!sort || sort === LegendSort.NONE) { return allLegendItems; }
        // 开始排序
        const renderItems = allLegendItems.slice();
        return renderItems.sort((a, b) =>
            sort === LegendSort.DECREASING
                ? b.computedValue[sortBy] - a.computedValue[sortBy]
                : a.computedValue[sortBy] - b.computedValue[sortBy]
        );
    }, [allLegendItems, sort, sortBy]);

    const showLegend =
        chartType !== ChartTypeEnum.Radar &&
        getConfigValue(ChartEditConfig.legend.config.display, chartConfig);

    if (!showLegend) {
        return null;
    }

    // 获取配置
    const asTable = getConfigValue(ChartEditConfig.legend.config.asTable, chartConfig); // 表格显示
    const width = getConfigValue(ChartEditConfig.legend.config.width, chartConfig); // 「右侧显示」时宽度
    const layout = getConfigValue(ChartEditConfig.legend.config.layout, chartConfig);
    const toRight = getConfigValue(ChartEditConfig.legend.config.toRight, chartConfig);
    const decimals = getConfigValue(ChartEditConfig.legend.value.decimals, chartConfig);
    const unit = getConfigValue(ChartEditConfig.axis.leftYAxis.unit, chartConfig) as UnitModelEnum;
    const rightUnit = getConfigValue(ChartEditConfig.axis.rightYAxis.unit, chartConfig) as UnitModelEnum;
    const valueConfig = getConfigValue(ChartEditConfig.legend.value, chartConfig);

    const showValue = isSeriesChart // 当前展示的
        ? LegendValueIndex.filter(key => !!valueConfig?.[key])
        : valueConfig?.[LegendValue.TOTAL] ? [LegendValue.TOTAL] : []; // 面积图只支持 total;

    const generateLegendSeries = (series: ChartSeriesInfo) => (
        <LegendSeries
            key={series.label}
            dataSource={series}
            table={asTable}
            showValue={showValue}
            decimals={decimals}
            unit={series.isRightAxis ? rightUnit : unit}
            onClick={onSelect}
            suffix={handleLegendSuffix}
            selected={!selectedKeys?.length ? true : selectedKeys?.indexOf(series.label) > -1}
        />
    );

    const handleLegendSort = (targetSort: LegendSort, targetSortBy: LegendValue) => {
        setSort(targetSort);
        setSortBy(targetSortBy);
    };

    const handleLegendSuffix = (legend: ChartSeriesInfo) => {
        if (isSeriesChart) {
            return null;
        }
        return `（${
            DataFormatter.tooltipFormatter(
                UnitModelEnum.Percent0_0, 
                legend.computedValue[LegendValue.TOTAL] / areaChartTotalCount
            )
        }）`;
    };

    const legendClassString = classNames("emonitor-chart-legend-container", {
        "as-table": asTable,
        "to-right": toRight,
        "align-center": !toRight && layout === "center",
        "align-right": !toRight && layout === "right",
    });

    const legendContentClassString = classNames("chart-legend-content", {
        table: asTable,
        active: selectedKeys?.length
    });

    const legendStyle: React.CSSProperties = {
        flexBasis: (toRight && width && legendList.length > 0) ? width : null,
        maxHeight: toRight ? null : maxHeight,
    };

    return (
        <div className={legendClassString} style={legendStyle}>
            <div className={legendContentClassString}>
                {/* 如果 as table, 渲染表头 */}
                {asTable && legendList.length > 0 && (
                    <LegendTableHeader
                        chart={chart}
                        sort={sort}
                        sortBy={sortBy}
                        showValue={showValue}
                        onSearch={setSearchValue}
                        onSort={handleLegendSort}
                    />
                )}

                {/* 「表格显示」和「右边显示」不分离左右侧 Legend */}
                {(toRight || asTable || rightSeries.length === 0)
                    ? legendList.map(generateLegendSeries)
                    : <div className="chart-legend-split-layout">
                        <div className="chart-legend__split-left">{legendList.filter(i => !i.isRightAxis).map(generateLegendSeries)}</div>
                        <div className="chart-legend__split-right">{legendList.filter(i => i.isRightAxis).map(generateLegendSeries)}</div>
                    </div>
                }
            </div>
        </div>
    );
};

export default ChartLegend;
