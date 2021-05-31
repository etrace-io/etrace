import React from "react";
import {Input} from "antd";
import classNames from "classnames";
import {ToolKit} from "$utils/Util";
import {ChartInfo, LegendSort, LegendValue} from "$models/ChartModel";
import {LegendValueIndex} from "$components/EMonitorChart/Legend/ChartLegend";
import {default as ChartEditConfig, getConfigValue} from "$containers/Board/Explorer/ChartEditConfig";

// 表头点击排序逻辑的顺序，点击第一次（包含切换列）：降序；点击第二次：升序；点击第三次：不排序。
const TABLE_HEADER_CLICK_ORDER = [
    LegendSort.NONE,
    LegendSort.DECREASING,
    LegendSort.INCREASING,
];

const LegendTableHeader: React.FC<{
    chart: ChartInfo,
    sort: LegendSort,
    sortBy: LegendValue,
    showValue: LegendValue[]; // 需要展示的 value
    onSearch: (value: string) => void;
    onSort: (sort: LegendSort, sortBy: LegendValue) => void;
}> = props => {
    const {chart, onSearch, sortBy, sort, onSort, showValue} = props;

    const chartConfig = chart.config;
    const showSearch = getConfigValue(ChartEditConfig.legend.config.showSearch, chartConfig); // 搜索框

    const handleSort = (col: LegendValue) => {
        const currStep = sortBy !== col
            ? 0 // 新的列
            : TABLE_HEADER_CLICK_ORDER.indexOf(sort);

        onSort(
            TABLE_HEADER_CLICK_ORDER[(currStep + 1) % 3], // 下一步
            col,
        );
    };

    return (
        <div className="chart-legend-table-header">
            {/* 搜索框占位 */}
            <span className="chart-legend-th-content no-pointer">
                {showSearch ? (
                    <Input
                        className="emonitor-chart-legend-search"
                        size="small"
                        placeholder="请输入名称以过滤"
                        onChange={(e) => onSearch && onSearch(e.target.value)}
                    />
                ) : <span className="legend-name-col">Name</span>}
            </span>

            {/* 数值展示 Title */}
            {LegendValueIndex.map(key => {
                const value = getConfigValue<string>(ChartEditConfig.legend.value[key], chartConfig);
                if (!showValue.includes(key) || !value) { return null; }

                const headerClass = classNames("chart-legend-th-content", {
                    order: sortBy === key && sort !== LegendSort.NONE,
                    desc: sortBy === key && sort === LegendSort.DECREASING,
                });

                return <span key={key} className={headerClass} onClick={() => handleSort(key)}>
                    <span>{ToolKit.firstUpperCase(key)}</span>
                </span>;
            })}
        </div>
    );
};

export default LegendTableHeader;
