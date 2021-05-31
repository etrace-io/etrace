import Chart from "chart.js";
import ReactDOM from "react-dom";
import classNames from "classnames";
import {useDebounceFn} from "ahooks";
import {ChartKit, DOMKit} from "$utils/Util";
import TooltipList from "$components/EMonitorChart/Tooltip/TooltipList";
import ToolTipToolBar from "$components/EMonitorChart/Tooltip/TooltipToolBar";
import {getTooltipPositionAndSize} from "$components/EMonitorChart/Metrics/utils";
import ChartEditConfig, {getConfigValue} from "$containers/Board/Explorer/ChartEditConfig";
import React, {useCallback, useEffect, useMemo, useRef, useState} from "react";

import {
    ChartInfo,
    ChartSeriesInfo,
    ChartTypeEnum,
    EMonitorChartVisualData,
    TOOLTIP_POSITION,
    TooltipSort,
    TooltipSortBy,
    TooltipSortMethod
} from "$models/ChartModel";

import "./ChartTooltip.less";

// 宏
export const TOOLTIP_SPACING = 10;      // Tooltip 与边界的距离
export const TOOLTIP_MIN_HEIGHT = 100;  // Chart Tooltip 最小高度
export const CROSSHAIR_SPACING = 20;    // 位于左右两侧时，鼠标与 tooltip 边界的间隙

const ChartTooltip: React.FC<{
    chart: ChartInfo;
    chartjsInst: Chart;
    selectedKeys?: string[];
    dataSource: EMonitorChartVisualData;
    onSelect?(legend: ChartSeriesInfo, nativeEvent: React.MouseEvent<HTMLDivElement>): void;
    getBoundaryContainer?(): HTMLElement; // 设置边界元素
}> = props => {
    const {getBoundaryContainer, chart, chartjsInst, onSelect, dataSource, selectedKeys} = props;
    const chartConfig = chart.config;
    const chartCanvas = chartjsInst?.canvas;
    const chartType = ChartKit.getChartType(chart);

    const container = useRef<HTMLDivElement>();     // tooltip 容器
    const kick = useRef<HTMLDivElement>();          // tooltip 三角形指向图标
    const timer = useRef<number>(null);   // 用于 tooltip hover 离开后的消失时间

    // 获取 Chart 配置
    const configSort = getConfigValue(ChartEditConfig.display.tooltip.sort, chartConfig, TooltipSort.DECREASING) as TooltipSort;
    const showZero = getConfigValue(ChartEditConfig.display.tooltip.showZero, chartConfig);

    // Tooltip 显示 / 隐藏
    const [visible, setVisible] = useState<boolean>(false);
    // 当前搜索过滤内容
    const [search, setSearch] = useState<string>("");
    // 当前 Tooltip 内容排序方式
    const [currSort, setCurrSort] = useState<TooltipSortMethod>({
        by: TooltipSortBy.VALUE,
        order: [TooltipSort.DECREASING, TooltipSort.INCREASING].indexOf(configSort) > -1 ? configSort : TooltipSort.DECREASING
    });
    // 当前鼠标上数据位于数据源上的索引
    const [currPointIndex, setCurrPointIndex] = useState<number>(null);
    // 当前 Tooltip 需要展示的数据内容
    const [tooltipSeriesList, setTooltipSeriesTooltip] = useState<ChartSeriesInfo[]>(null);
    // 当前鼠标偏移量，用于定位 Tooltip
    const [offsetLeft, setOffsetLeft] = useState<number>(null);
    const [tooltipDirection, setTooltipDirection] = useState<TOOLTIP_POSITION>(null);
    // 手动更新 Tooltip（resize）
    const [updateManually, setUpdateManually] = useState<boolean>(false); // 手动更新
    // 显示 Toolbar 搜索框
    const [showSearch, setShowSearch] = useState<boolean>(false);

    const {run: updateRectInfo} = useDebounceFn(() => {
        setUpdateManually(v => !v);
    }, {wait: 500});

    const scrollParent = useMemo(
        () => chartCanvas && DOMKit.findScrollParent(chartCanvas),
        [chartCanvas]
    );

    /* 可视区域 */
    const boundaryRect = useMemo(() => {
        const popupContainer = getBoundaryContainer && getBoundaryContainer();
        return popupContainer
            ? popupContainer.getBoundingClientRect()
            : document.body.getBoundingClientRect();
    }, [getBoundaryContainer, updateManually]);

    /* Chart Canvas 对应 Rect */
    const chartCanvasRect = useMemo(
        () => chartCanvas && chartCanvas.getBoundingClientRect(),
        [chartCanvas, updateManually]
    );

    const removeTooltip = () => {
        if (timer.current) { return; }
        timer.current = +setTimeout(() => {
            setVisible(false);
            timer.current = null;
        }, 200);
    };

    const keepTooltip = () => {
        if (!timer.current) { return; }
        clearTimeout(timer.current);
        timer.current = null;
    };

    // 排序
    const handleTooltipListSort = useCallback((type: TooltipSortBy, order: TooltipSort) => {
        setCurrSort({
            by: type,
            order,
        });
    }, []);

    // 鼠标移动事件监听
    const handleChartMouseout = useCallback((e?: MouseEvent) => {
        removeTooltip();
    }, []);

    const handleChartMouseover = useCallback((e: MouseEvent) => {
        if (!chartjsInst) { return; }
        // 禁止移动
        if (e.metaKey || e.altKey || e.shiftKey || e.ctrlKey) { return; }

        // 体验优化，鼠标移动到坐标轴下的时候触发 MouseOut 事件
        if (e.offsetY > chartjsInst.chartArea.bottom) {
            removeTooltip();
            return;
        }

        const points = chartjsInst.getElementsAtXAxis(e);
        if (!points || points.length === 0) {
            return;
        }

        // @ts-ignore
        const currIndex = points[0]._index;
        const {chartArea} = chartjsInst;

        const chartWidth = chartArea.right - chartArea.left;
        const chartDataNum = chartjsInst.data.labels.length;

        // 计算当前 Tooltip 所在的横坐标（柱状图特殊处理，移动到所有柱子中间）
        const offsetX = chartType === ChartTypeEnum.Column
            ? (currIndex * 2 + 1) * chartWidth / (chartDataNum * 2) + chartArea.left
            : currIndex / (chartDataNum - 1) * chartWidth + chartArea.left;

        // 更新坐标值
        setOffsetLeft(offsetX);
        setCurrPointIndex(currIndex);

        // 更新数据源
        setVisible(true);
        keepTooltip();
    }, [chartType, chartjsInst]);

    // 添加 chart 鼠标监听
    useEffect(() => {
        const canvas = chartjsInst?.canvas;
        if (!canvas) { return; }

        canvas.addEventListener("mousemove", handleChartMouseover);
        canvas.addEventListener("mouseout", handleChartMouseout);

        return () => {
            canvas.removeEventListener("mousemove", handleChartMouseover);
            canvas.removeEventListener("mouseout", handleChartMouseout);
        };
    }, [chartjsInst, handleChartMouseout, handleChartMouseover]);

    // 更新 Tooltip 列表
    useEffect(() => {
        const tooltipList = dataSource?.datasets
            .map<ChartSeriesInfo>((dataset, _idx) => ({
                label: dataset.label,
                datasetIndex: _idx,
                _value: dataset.data[currPointIndex],
            }))
            .filter(series => {
                // 过滤无效值
                if (series._value === undefined || series._value === null) { return false; }
                // 过滤搜索
                if (search && !series.label.toLowerCase().includes(search.toLowerCase())) { return false; }
                // 过滤「0 值显示」
                return showZero ? true : series._value > 0;
            })
            .sort((a, b) => {
                if (currSort.by === TooltipSortBy.NAME) {
                    return currSort.order === TooltipSort.DECREASING
                        ? b.label.localeCompare(a.label)
                        : a.label.localeCompare(b.label);
                } else {
                    return currSort.order === TooltipSort.DECREASING
                        ? b._value - a._value
                        : a._value - b._value;
                }
            });

        setTooltipSeriesTooltip(tooltipList);
    }, [currPointIndex, currSort.by, currSort.order, dataSource, search, showZero]);

    // 监听窗口尺寸变化
    useEffect(() => {
        // const _container = getBoundaryContainer ? getBoundaryContainer() : document.body;
        window.addEventListener("resize", updateRectInfo);
        return () => window.removeEventListener("resize", updateRectInfo);
    }, [updateRectInfo]);

    // 监听滚动
    useEffect(() => {
        if (!scrollParent) { return; }
        scrollParent.addEventListener("scroll", updateRectInfo);
        return () => scrollParent.removeEventListener("scroll", updateRectInfo);
    }, [scrollParent, updateRectInfo]);

    // 计算 Tooltip 位置及尺寸并设置样式
    useEffect(() => {
        // console.log("?", {offsetLeft, curr: container.current})
        if (!visible || !container.current || !chartCanvas) { return; }

        // 计算对应位置及尺寸
        const {position, size, direction} = getTooltipPositionAndSize(
            container.current,
            offsetLeft,
            chartCanvasRect,
            boundaryRect,
        );

        // 设置位置
        DOMKit.setPosition(kick.current, position.kick);
        DOMKit.setPosition(container.current, position.container);

        // 设置尺寸
        const {height, width} = size;
        height && (container.current.style.maxHeight = height + "px");
        width && (container.current.style.maxWidth = width + "px");

        // 设置方位
        setTooltipDirection(direction);

    }, [boundaryRect, chartCanvas, chartCanvasRect, dataSource, offsetLeft, visible]);

    const tooltipCls = classNames("emonitor-chart-tooltip", {
        "in-chart": tooltipDirection === TOOLTIP_POSITION.LEFT || tooltipDirection === TOOLTIP_POSITION.RIGHT,
    });

    const tooltip = (
        <div
            className={tooltipCls}
            onMouseEnter={keepTooltip}
            onMouseLeave={removeTooltip}
            ref={container}
        >
            {/* Kick 角标 */}
            <div ref={kick} className="tooltip-top-kick" />

            {/* 工具栏 */}
            <div className="tooltip-title">
                <ToolTipToolBar
                    time={dataSource?.times[currPointIndex]}
                    sort={currSort}
                    search={search}
                    onSearch={setSearch}
                    onSort={handleTooltipListSort}
                    searchVisible={showSearch}
                    onSearchVisibleChange={setShowSearch}
                />
            </div>

            {/* Series 列表 */}
            <div className="tooltip-content-list">
                <TooltipList
                    dataSource={tooltipSeriesList}
                    chart={chartjsInst}
                    onSelect={onSelect}
                    selectedKeys={selectedKeys}
                />
            </div>
        </div>
    );

    if (!visible) { return null; }

    return ReactDOM.createPortal(
        tooltip,
        getBoundaryContainer ? getBoundaryContainer() : document.body
    );
};

export default React.memo(ChartTooltip);
