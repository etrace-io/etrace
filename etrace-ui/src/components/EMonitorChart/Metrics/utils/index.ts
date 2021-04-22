import {ChartSeriesInfo, HiddenSeriesMap, TOOLTIP_POSITION} from "$models/ChartModel";
import {ChartData} from "chart.js";
import {CROSSHAIR_SPACING, TOOLTIP_MIN_HEIGHT, TOOLTIP_SPACING} from "$components/EMonitorChart/Tooltip/ChartTooltip";

/**
 * 返回下一次 Series 的状态
 * - selectedList 表示下一次选中的 Series 数组（空数组表示全选）
 * - hiddenMap 表示下一次隐藏的 Map（label -> index）
 */
export const getSelectedSeries = (
    target: ChartSeriesInfo,
    data: ChartData, // 需要区分 isSeries
    prevSelected: string[],
    prevHidden: HiddenSeriesMap,
    isSeries: boolean = true, // 是否为线状图
    metaKey?: boolean,
) => {
    const selectedLabel = target.label;
    const selectedDatasetIndex = target.datasetIndex;

    let currSelectedSeries = new Set<string>(prevSelected);
    let currHiddenSeries: HiddenSeriesMap = Object.assign({}, prevHidden);

    // 点击了唯一一条已经显示的 Series，全部展示
    const shouldShowAllSeries =
        currSelectedSeries.size === 1 &&
        currSelectedSeries.has(selectedLabel);

    // 逻辑区分线图和面积图
    // 线图点击 Series 只展示「一条」目标 Series
    // 面积图点击 Series 则隐藏当前 Series

    if (shouldShowAllSeries) {
        // 点击唯一显示的 Series，反向选取，展示全部
        currSelectedSeries.clear();
        currHiddenSeries = {};
    } else if (metaKey || !isSeries) {
        // 配合功能键点击，针对单一 Series 处理
        const isHidden = prevHidden[selectedLabel] > -1;

        // 状态置反
        if (isHidden) {
            delete currHiddenSeries[selectedLabel];
            currSelectedSeries.add(selectedLabel);
        } else {
            currHiddenSeries[selectedLabel] = selectedDatasetIndex;
            if (currSelectedSeries.size === 0) {
                currSelectedSeries = new Set<string>(
                    isSeries ? data.datasets.map(dataset => dataset.label) : data.labels as string[]
                );
            }
            currSelectedSeries.delete(selectedLabel);
        }
    } else {
        // 正常选取，只展示选中的 Series，其他全部隐藏
        data.datasets.forEach((dataset, index) => {
            if (dataset.label !== selectedLabel) {
                currHiddenSeries[dataset.label] = index;
            } else {
                delete currHiddenSeries[dataset.label];
            }
        });
        currSelectedSeries.clear();
        currSelectedSeries.add(selectedLabel);
    }

    return {
        selectedList: Array.from(currSelectedSeries),
        hiddenMap: currHiddenSeries,
    };
};

/**
 * 获取 Tooltip 放置时合适的方位（上下左右）及尺寸
 */
export const getTooltipPositionAndSize = (
    tooltipContainer: HTMLElement,
    offsetX: number,
    // Chart Canvas
    chartCanvasRect: DOMRect,
    // 边界元素
    boundaryRect: DOMRect,
) => {
    // 当前点鼠标对于「边界」的 offsetX 值
    const mouseClientX = offsetX + chartCanvasRect.left - boundaryRect.left;
    // 上方可视区域
    const topAreaHeight = chartCanvasRect.top - boundaryRect.top;
    // 下方可视区域
    const bottomAreaHeight = boundaryRect.bottom - chartCanvasRect.bottom;
    // 左侧可视区域
    const leftAreaWidth = mouseClientX - CROSSHAIR_SPACING;
    // 右侧可视区域
    const rightAreaWidth = boundaryRect.width - mouseClientX - CROSSHAIR_SPACING;

    // 当前 Tooltip 高度
    const tooltipHeight = tooltipContainer.clientHeight;
    // 当前 Tooltip 宽度
    const tooltipWidth = tooltipContainer.clientWidth;
    // 判断所处位置
    let targetPosition: TOOLTIP_POSITION;

    // 上下足以容纳
    if (bottomAreaHeight > TOOLTIP_MIN_HEIGHT || topAreaHeight > TOOLTIP_MIN_HEIGHT) {
        if (bottomAreaHeight - TOOLTIP_SPACING > tooltipHeight) {
            targetPosition = TOOLTIP_POSITION.BOTTOM;
        } else if (topAreaHeight - TOOLTIP_SPACING > tooltipHeight) {
            targetPosition = TOOLTIP_POSITION.TOP;
        } else {
            targetPosition = bottomAreaHeight > topAreaHeight
                ? TOOLTIP_POSITION.BOTTOM
                : TOOLTIP_POSITION.TOP;
        }
    } else {
        if (leftAreaWidth - TOOLTIP_SPACING > tooltipWidth) {
            targetPosition = TOOLTIP_POSITION.LEFT;
        } else if (rightAreaWidth - TOOLTIP_SPACING > tooltipWidth) {
            targetPosition = TOOLTIP_POSITION.RIGHT;
        } else {
            targetPosition = leftAreaWidth > rightAreaWidth
                ? TOOLTIP_POSITION.LEFT
                : TOOLTIP_POSITION.RIGHT;
        }
    }

    let maxSize;

    // 设置高度
    switch (targetPosition) {
        case TOOLTIP_POSITION.BOTTOM:
            maxSize = {
                height: bottomAreaHeight - TOOLTIP_SPACING,
                width: boundaryRect.width - TOOLTIP_SPACING * 2,
            };
            break;
        case TOOLTIP_POSITION.TOP:
            maxSize = {
                height: topAreaHeight - TOOLTIP_SPACING,
                width: boundaryRect.width - TOOLTIP_SPACING * 2,
            };
            break;
        case TOOLTIP_POSITION.LEFT:
            maxSize = {
                height: chartCanvasRect.height - TOOLTIP_SPACING * 2,
                width: leftAreaWidth - TOOLTIP_SPACING,
            };
            break;
        case TOOLTIP_POSITION.RIGHT:
            maxSize = {
                height: chartCanvasRect.height - TOOLTIP_SPACING * 2,
                width: rightAreaWidth - TOOLTIP_SPACING,
            };
            break;
        default:
            break;
    }

    const {container, kick} = calcTooltipPosition(
        tooltipContainer,
        targetPosition,
        mouseClientX,
        chartCanvasRect,
        boundaryRect,
    );

    return {
        position: { container, kick },
        size: maxSize,
        direction: targetPosition,
    };
};

/**
 * 根据所处方位及尺寸，计算 Tooltip 对应的摆放位置
 */
const calcTooltipPosition = (
    tooltipContainer: HTMLElement,
    position: TOOLTIP_POSITION,
    // 当前鼠标相对于边界（boundary）的 offsetX
    mouseX: number,
    // Chart Canvas
    chartCanvasRect: DOMRect,
    // 边界元素
    boundaryRect: DOMRect,
) => {
    // 当前 Tooltip 高度
    const tooltipHeight = tooltipContainer.offsetHeight;
    // 当前 Tooltip 宽度
    const tooltipWidth = tooltipContainer.offsetWidth;

    const verticalAreaHeight = chartCanvasRect.top - boundaryRect.top - TOOLTIP_SPACING; // 竖直方向可视区域
    const reachLeftBoundary = mouseX - tooltipWidth / 2 - TOOLTIP_SPACING < 0;
    const reachRightBoundary = mouseX + tooltipWidth / 2 + TOOLTIP_SPACING > boundaryRect.width;

    const reachBoundary = reachLeftBoundary || reachRightBoundary;

    let kickPosition, containerPosition;

    switch (position) {
        case TOOLTIP_POSITION.BOTTOM:
            kickPosition = {
                left: reachBoundary
                    ? reachRightBoundary ? tooltipWidth - (boundaryRect.width - TOOLTIP_SPACING - mouseX) : mouseX - TOOLTIP_SPACING
                    : "50%",
            };
            containerPosition = {
                left: !reachBoundary
                    ? mouseX - tooltipWidth / 2 // 移动过程中
                    : reachLeftBoundary ? TOOLTIP_SPACING : undefined,
                top: chartCanvasRect.bottom,
                right: reachRightBoundary ? TOOLTIP_SPACING : undefined,
            };
            break;
        case TOOLTIP_POSITION.TOP:
            kickPosition = {
                bottom: -15,
                left: reachBoundary
                    ? reachRightBoundary ? tooltipWidth - (boundaryRect.width - TOOLTIP_SPACING - mouseX) : mouseX - TOOLTIP_SPACING
                    : "50%",
            };
            containerPosition = {
                left: !reachBoundary
                    ? mouseX - tooltipWidth / 2
                    : reachLeftBoundary ? TOOLTIP_SPACING : undefined,
                top: chartCanvasRect.top - tooltipHeight,
                right: reachRightBoundary ? TOOLTIP_SPACING : undefined,
            };
            break;
        case TOOLTIP_POSITION.LEFT:
            kickPosition = {
                right: -15,
                top: "50%",
            };
            containerPosition = {
                left: mouseX - CROSSHAIR_SPACING - tooltipWidth,
                // left: isRight ? mouseClientX + CROSSHAIR_SPACING : mouseClientX - CROSSHAIR_SPACING - toolTipReact.width - 15,
                top: "50%",
            };
            break;
        case TOOLTIP_POSITION.RIGHT:
            kickPosition = {
                left: 2,
                top: "50%",
            };
            containerPosition = {
                left: mouseX + CROSSHAIR_SPACING,
                top: "50%",
            };
            break;
        default:
            break;
    }

    return {
        container: containerPosition,
        kick: kickPosition,
    };
};
