import classNames from "classnames";
import {DOMKit, SeriesKit} from "$utils/Util";
import React, {useMemo, useRef} from "react";
import {DataFormatter} from "$utils/DataFormatter";
import {ChartInfo, ChartSeriesInfo} from "$models/ChartModel";
import ChartEditConfig, {getConfigValue} from "$containers/Board/Explorer/ChartEditConfig";

const TooltipList: React.FC<{
    chart: ChartInfo;
    dataSource: ChartSeriesInfo[];
    onSelect?: (tooltip: ChartSeriesInfo, nativeEvent: React.MouseEvent<HTMLDivElement>) => void;
    selectedKeys?: string[];
}> = props => {
    const {chart, dataSource, onSelect, selectedKeys} = props;
    const chartConfig = chart?.config;

    const unit = useMemo(
        () => getConfigValue(ChartEditConfig.axis.leftYAxis.unit, chartConfig),
        [chartConfig]
    );

    const decimals = useMemo(
        () => getConfigValue(ChartEditConfig.axis.leftYAxis.decimals, chartConfig),
        [chartConfig]
    );

    if (!dataSource.length) {
        return <div className="tooltip-empty-tip">无数据</div>;
    }

    return (
        <div className="tooltip-list">
            {dataSource?.map((series, index) => {
                const value = DataFormatter.tooltipFormatter(unit, series._value, decimals);
                return (
                    <TooltipSeries
                        key={series.label}
                        dataSource={series}
                        value={value}
                        selected={!selectedKeys?.length ? true : selectedKeys?.indexOf(series.label) > -1}
                        onClick={onSelect}
                    />
                );
            })}
        </div>
    );
};

const TooltipSeries: React.FC<{
    dataSource: ChartSeriesInfo;
    value: string | number;
    // unit: UnitModelEnum; // 当前图表 y 轴单位
    selected?: boolean; // 当前 series 是否选中（展示）
    onClick?: (tooltip: ChartSeriesInfo, nativeEvent: React.MouseEvent<HTMLDivElement>) => void;
}> = props => {
    const {dataSource, selected, onClick, value} = props;
    const color = SeriesKit.getSeriesColor(dataSource.datasetIndex);

    const tooltipRef = useRef<HTMLDivElement>(null);

    const handleTooltipSeriesClick = (e: React.MouseEvent<HTMLDivElement>) => {
        // 确保复制的操作
        const selection = window.getSelection();
        if (
            selection.toString() !== "" &&
            DOMKit.isParentOf(selection.anchorNode, tooltipRef.current)
        ) {
            return;
        }

        // 派发事件
        onClick && onClick(dataSource, e);
    };

    return (
        <div className={classNames("tooltip-content-list-item", { selected })} ref={tooltipRef}>
            <span className="tooltip-series-key" onClick={handleTooltipSeriesClick}>
                <i className="tooltip-series-icon" style={{backgroundColor: color}}/>
                <span className="tooltip-series-label">{dataSource.label}</span>
            </span>

            <span className="tooltip-series-value">{value}</span>
        </div>
    );
};

export default TooltipList;
