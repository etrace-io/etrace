import classNames from "classnames";
import React, {useMemo, useRef} from "react";
import {DOMKit, SeriesKit, ToolKit} from "$utils/Util";
import {UnitModelEnum} from "$models/UnitModel";
import {DataFormatter} from "$utils/DataFormatter";
import {ChartSeriesInfo, LegendValue} from "$models/ChartModel";

const LegendSeries: React.FC<{
    dataSource: ChartSeriesInfo,
    showValue: LegendValue[]; // 需要展示的 value
    unit: UnitModelEnum, // 当前图表 y 轴单位
    decimals: number, // 保留的小数位数
    table?: boolean; // 表格展示
    selected?: boolean; // 当前 series 是否选中（展示）
    interval?: number, // 处理 isCountPS 情况下的 Total 计算
    suffix?: string | ((legend: ChartSeriesInfo) => string),
    onClick?: (legend: ChartSeriesInfo, nativeEvent: React.MouseEvent<HTMLDivElement>) => void;
}> = props => {
    const {dataSource, selected, showValue, unit, decimals, interval, suffix, onClick, table} = props;

    const legendRef = useRef<HTMLDivElement>(null);
    const suffixContent = useMemo(
        () => (typeof suffix === "function" ? suffix(dataSource) : suffix) ?? "",
        [suffix]
    );

    const {label, computedValue, collapse} = dataSource;
    const color = SeriesKit.getSeriesColor(dataSource.datasetIndex);

    const seriesClassString = classNames("chart-legend-series", {
        "fade": !selected,
        collapse,
        // table,
    });

    const handleLegendSeriesClick = (e: React.MouseEvent<HTMLDivElement>) => {
        // 确保复制的操作
        const selection = window.getSelection();
        if (
            selection.toString() !== "" &&
            DOMKit.isParentOf(selection.anchorNode, legendRef.current)
        ) {
            return;
        }

        // 派发事件
        onClick && onClick(dataSource, e);
    };

    return (
        // 这里每个 series 为一个 table-row, 其子元素为各个 table-cell
        <div className={seriesClassString} onClick={handleLegendSeriesClick} ref={legendRef}>
            <span className="legend-series-key">
                <i className="legend-series-icon" style={{backgroundColor: color}}/>
                <span className="legend-series-label">{label + suffixContent}</span>
            </span>

            {/* 控制需要显示的 Value */}
            {showValue?.map(key => (
                <span key={key} className="legend-series-value">
                    {table
                        ? calcLegendValue(computedValue[key], key, unit, decimals, interval)
                        : `${ToolKit.firstUpperCase(key)}: ${calcLegendValue(computedValue[key], key, unit, decimals, interval)}`
                    }
                </span>
            ))}
        </div>
    );
};

/**
 * 计算 Legend 中的 Value 并加上单位
 */
const calcLegendValue = (
    value: number, // 需要换算的值
    key: LegendValue, // 当前展示的 value 字段
    unit: UnitModelEnum, // 当前图标 y 轴单位
    decimals: number, // 保留的小数位数
    interval: number
) => {
    value = +value.toFixed(decimals);
    if (interval && key === LegendValue.TOTAL) {
        value = value * interval / 1000;
    }
    return DataFormatter.formatterByUnit(unit, value, decimals);
};

export default LegendSeries;
