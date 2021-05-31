import {get} from "lodash";
import {reaction} from "mobx";
import Moment from "react-moment";
import {Button, Input} from "antd";
import classNames from "classnames";
import {DOMKit} from "$utils/Util";
import StoreManager from "$store/StoreManager";
import {useDebounceFn, useUpdate} from "ahooks";
import IconFont from "$components/Base/IconFont";
import {DataFormatter} from "$utils/DataFormatter";
import {SearchOutlined} from "@ant-design/icons/lib";
import React, {useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState} from "react";
import ChartEditConfig, {getConfigValue} from "$containers/Board/Explorer/ChartEditConfig";

import "./ChartTooltip.less";

enum SORT {
    DECREASING = "Decreasing",
    INCREASING = "Increasing",
}

enum SORT_BY {
    NAME = "name",
    VALUE = "value",
}

const OPPOSITE_SORT = {
    [SORT.DECREASING]: SORT.INCREASING,
    [SORT.INCREASING]: SORT.DECREASING,
};

enum TOOLTIP_POSITION {
    TOP = "top",
    RIGHT = "right",
    BOTTOM = "bottom",
    LEFT = "left",
}

// 宏
const TOOLTIP_SPACING = 10;             // Tooltip 与边界的距离
const TOOLTIP_MIN_HEIGHT = 100;   // Chart Tooltip 最小高度
const CROSSHAIR_SPACING = 20;      // 位于左右两侧时，鼠标与 tooltip 边界的间隙

const setContentMaxSize = (el: HTMLElement, size: { height?: number, width?: number } = {}) => {
    if (!el) { return; }

    const {height, width} = size;
    height && (el.style.maxHeight = height + "px");
    width && (el.style.maxWidth = width + "px");
};

interface SortMethod {
    by: SORT_BY;
    order: SORT;
}

const ChartTooltip: React.FC<{
    fixed?: boolean;    // 全屏时 Tooltip 的位置需要 fixed 控制（全屏条件下置为 true）
    uuid?: string;      // 当存在多个 Tooltip 的时候，控制显示的 uuid
    getBoundaryContainer?: () => HTMLElement;
}> = props => {
    const {getBoundaryContainer, fixed, uuid} = props;

    // const wrapper = useRef<HTMLDivElement>();
    const container = useRef<HTMLDivElement>(); // tooltip 容器
    const kick = useRef<HTMLDivElement>();      // tooltip 三角形指向图标
    const timer = useRef<number>();             // 用于 tooltip hover 离开后的消失时间
    const targetChart = useRef<HTMLElement>();  // 保存鼠标所在 tooltip
    const forceUpdate = useUpdate();

    const [event, setEvent] = useState<any>();

    const configSort = getConfigValue(
        ChartEditConfig.display.tooltip.sort,
        get(event, "chart.options.rawConfig", {}),
        SORT.DECREASING
    ) as SORT;

    const [visible, setVisible] = useState<boolean>(false);
    const [search, setSearch] = useState<string>("");
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [currSort, setCurrSort] = useState<SortMethod>();
    const [updateManually, setUpdateManually] = useState<boolean>(false); // 手动更新

    const {run: updateRectInfo} = useDebounceFn(() => {
        setUpdateManually(v => !v);
    }, {wait: 500});

    const scrollParent = useMemo(
        () => targetChart.current && DOMKit.findScrollParent(targetChart.current),
        [targetChart.current]
    );

    /* 可视区域 */
    const boundaryRect = useMemo(() => {
        const popupContainer = getBoundaryContainer && getBoundaryContainer();
        return popupContainer
            ? popupContainer.getBoundingClientRect()
            : document.body.getBoundingClientRect();
    }, [getBoundaryContainer, updateManually]);

    const chartCanvas: HTMLCanvasElement = get(event, "chart.canvas", null);

    /* Chart Canvas 对应 Rect */
    const chartCanvasRect = useMemo(
        () => chartCanvas && chartCanvas.getBoundingClientRect(),
        [chartCanvas, updateManually]
    );

    const setTimer = () => {
        if (timer.current) { return; }
        timer.current = +setTimeout(() => {
            setVisible(false);
        }, 200);
    };

    const clearTimer = () => {
        // 确保在 setTimer 之后调用
        setTimeout(() => {
            clearTimeout(timer.current);
            timer.current = null;
        }, 0);
    };

    /**
     * 添加鼠标移动监听
     */
    useEffect(() => {
        const disposer = [
            reaction(
                () => StoreManager.chartEventStore.pointMouseOverEvent,
                e => {
                    const uniqueId = get(e, "chart.options.uniqueId", null);
                    if (uuid && uniqueId !== uuid) { return; }
                    // 禁止移动
                    const ne = e.nativeEvent;
                    if (ne.metaKey || ne.altKey || ne.shiftKey || ne.ctrlKey) { return; }
                    // if (get(e, "chart.options.uniqueId", null) !== uniqueId) { return; }

                    setVisible(true);
                    setEvent(e);

                    targetChart.current = ne.target;

                    if (timer.current) {
                        clearTimer();
                    }
                }),
            reaction(
                () => StoreManager.chartEventStore.pointMouseOutEvent,
                () => {
                    setTimer();
                })
        ];

        return () => {
            disposer.forEach(d => d());
        };
    }, []);

    /**
     * 监听窗口尺寸变化
     */
    useEffect(() => {
        // const _container = getBoundaryContainer ? getBoundaryContainer() : document.body;
        window.addEventListener("resize", updateRectInfo);

        return () => {
            window.removeEventListener("resize", updateRectInfo);
        };
    }, []);

    /**
     * 监听滚动
     */
    useEffect(() => {
        if (!scrollParent) { return; }
        scrollParent.addEventListener("scroll", updateRectInfo);

        return () => {
            scrollParent.removeEventListener("scroll", updateRectInfo);
        };
    }, [scrollParent]);

    /**
     * 构建 Tooltip 数据源
     */
    useEffect(() => {
        if (!event) { return; }
        const series = event.chart.options.getSeries();
        const idx = get(event, "index", -1);

        if (configSort && !currSort) {
            setCurrSort({
                by: SORT_BY.VALUE,
                order: [SORT.DECREASING, SORT.INCREASING].indexOf(configSort) > -1 ? configSort : SORT.DECREASING
            });
        }

        if (!series || idx < 0) { return; }

        const itemList = [];
        const showZero = getConfigValue<boolean>(
            ChartEditConfig.display.tooltip.showZero,
            get(event, "chart.options.rawConfig", {})
        );

        series.forEach(item => {
            const value = item.data[idx];
            if (value === null || value === undefined) {
                return;
            }
            if (showZero || (!showZero && value > 0)) {
                itemList.push({
                    color: item.borderColor,
                    name: get(item, "name", ""),
                    hidden: item.hidden,
                    value: value
                });
            }
        });

        const displayItemList = itemList.filter(item => item.name.toLowerCase().indexOf(search.toLowerCase()) > -1);

        if (currSort) {
            displayItemList.sort((a, b) => {
                if (currSort.by === SORT_BY.NAME) {
                    return currSort.order === SORT.DECREASING
                        ? b.name.localeCompare(a.name)
                        : a.name.localeCompare(b.name);
                } else {
                    return currSort.order === SORT.DECREASING
                        ? b.value - a.value
                        : a.value - b.value;
                }
            });
        }

        setDataSource(displayItemList);
    }, [configSort, currSort, event, search]);

    // useWhyDidYouUpdate('tooltip', {configSort, currSort, event, search});

    /**
     * 判定 Tooltip 所处位置（上、右、下、左）
     * 然后交给 layoutTooltip 处理定位
     */
    const offsetLeft = event ? event.left : null;

    useLayoutEffect(() => {
        // console.log("?", {offsetLeft, curr: container.current})
        if (!visible || !container.current || !chartCanvas) { return; }

        // 当前点鼠标对于边界的 offsetX 值
        const mouseClientX = offsetLeft + chartCanvasRect.left - boundaryRect.left;

        // 上方可视区域
        const topAreaHeight = chartCanvasRect.top - boundaryRect.top;
        // 下方可视区域
        const bottomAreaHeight = boundaryRect.bottom - chartCanvasRect.bottom;
        // 左侧可视区域
        const leftAreaWidth = mouseClientX - CROSSHAIR_SPACING;
        // 右侧可视区域
        const rightAreaWidth = boundaryRect.width - mouseClientX - CROSSHAIR_SPACING;

        // 当前 Tooltip 高度
        const tooltipHeight = container.current.clientHeight;
        // 当前 Tooltip 宽度
        const tooltipWidth = container.current.clientWidth;

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

        setContentMaxSize(container.current, maxSize);

        // 设置位置
        // let tooltipPos;
        // setTimeout(() => {
        // }, 0);
        layoutTooltip(mouseClientX, targetPosition, maxSize);

    }, [dataSource]);

    /**
     * 设置 Tooltip 位置
     * @param mouseX 相对于边界（boundary）的 offsetX
     * @param position 目标位置（上、右、下、左）
     * @param maxSize ToolTip 最大尺寸
     */
    const layoutTooltip = (mouseX: number, position: TOOLTIP_POSITION, maxSize: { height: number, width: number }) => {
        // 当前 Tooltip 高度
        const tooltipHeight = container.current.offsetHeight;
        // 当前 Tooltip 宽度
        const tooltipWidth = container.current.offsetWidth;

        // console.log({mouseX, position, tooltipHeight, tooltipWidth});
        // console.log(maxSize);

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

        // debugger;

        // console.log(kickPosition);
        // console.log(containerPosition);

        DOMKit.setPosition(kick.current, kickPosition);
        DOMKit.setPosition(container.current, containerPosition);
    };

    const handleSort = useCallback((type: SORT_BY, order: SORT) => {
        setCurrSort({
            by: type,
            order,
        });
    }, []);

    if (!event) { return null; }

    const {chart} = event;

    const hideTooltip = (
        !chartCanvas ||
        !visible ||
        !chart.options.isSeriesChart ||
        !event.value ||
        (dataSource.length === 0 && search.length === 0)
    );

    const handleSeriesClick = (e: any, name: string) => {
        chart.options.seriesClick(e, name, true);
        forceUpdate();
    };

    const tooltipCls = classNames("chart-tooltip", {
        // "in-chart": tooltipInChart,
        fixed,
        hide: hideTooltip,
    });

    return (
        <div
            key="chart-tooltip"
            className={tooltipCls}
            // ref={wrapper}
            onMouseEnter={clearTimer}
            onMouseLeave={setTimer}
            // className="tooltip-container"
            ref={container}
        >
            <div ref={kick} className="tooltip-top-kick" />
            <div className="tooltip-title">
                <Moment format="YYYY-MM-DD HH:mm:ss">{event.value}</Moment>

                <ChartToolTipToolBar
                    sort={currSort}
                    visible={dataSource.length > 1}
                    onSearch={setSearch}
                    onSort={handleSort}
                />
            </div>

            <div className="tooltip-content-wrapper">
                <ChartTooltipContent
                    dataSource={dataSource}
                    chart={chart}
                    onClick={handleSeriesClick}
                />
            </div>
        </div>
    );

    // return (
    //     <div
    //         className={tooltipCls}
    //         ref={wrapper}
    //         onMouseEnter={clearTimer}
    //         onMouseLeave={setTimer}
    //     >
    //
    //     </div>
    // );
};

const ChartToolTipToolBar: React.FC<{
    visible: boolean;
    sort?: SortMethod;
    onSort?: (type: SORT_BY, order: SORT) => void;
    onSearch?: (value: string) => void;
}> = React.memo(props => {
    const {visible, sort} = props;
    const {onSearch, onSort} = props;

    const [showSearch, setShowSearch] = useState<boolean>(false);

    // const [sortBy, setSortBy] = useState<SORT_BY>(SORT_BY.NAME);
    // const [sortOrder, setSortOrder] = useState<SORT>(defaultSort);

    // const [sortBy, {toggle: toggleSortBy}] = useToggle(SORT_BY.VALUE, SORT_BY.NAME);
    // const [valueSort, {toggle: toggleValueSort}] = useToggle(
    //     OPPOSITE_SORT[defaultSort] ? defaultSort : SORT.DECREASING,
    //     OPPOSITE_SORT[defaultSort] || SORT.INCREASING
    // );
    // const [nameSort, {toggle: toggleNameSort}] = useToggle(SORT.DECREASING, SORT.INCREASING);

    // useEffect(() => {
    //     // console.log(visible, defaultSort)
    //     if (visible && defaultSort) {
    //         toggleValueSort(OPPOSITE_SORT[defaultSort] ? defaultSort : SORT.DECREASING);
    //     }
    // }, [visible]);

    if (!visible) {
        return null;
    }

    const handleNameSortClick = () => {
        onSort && onSort(
            SORT_BY.NAME,
            sort.by === SORT_BY.NAME ? OPPOSITE_SORT[sort.order] : SORT.DECREASING,
        );
    };

    const handleValueSortClick = () => {
        onSort && onSort(
            SORT_BY.VALUE,
            sort.by === SORT_BY.VALUE ? OPPOSITE_SORT[sort.order] : SORT.DECREASING,
        );
    };

    return (<>
        <Button.Group style={{paddingLeft: 10}} size="small">
            {/* 搜索框显示 */}
            <Button
                icon={<SearchOutlined />}
                type={showSearch ? "primary" : "default"}
                // className={`tooltip-btn ${showSearch ? "ant-btn-primary" : ""}`}
                onClick={() => setShowSearch(v => !v)}
                // style={{padding: "0 7px"}}
            />

            {/* 字典排序按钮 */}
            <Button
                onClick={handleNameSortClick}
                type={sort.by === SORT_BY.NAME ? "primary" : "default"}
                className="tooltip-btn"
            >
                <IconFont
                    type={sort.by === SORT_BY.NAME && sort.order === SORT.DECREASING ? "icon-sort-alpha-desc" : "icon-sort-alpha-asc"}
                />
            </Button>

            {/* 值排序按钮 */}
            <Button
                onClick={handleValueSortClick}
                type={sort.by === SORT_BY.VALUE ? "primary" : "default"}
                className="tooltip-btn"
            >
                <IconFont
                    type={sort.by === SORT_BY.VALUE && sort.order === SORT.DECREASING ? "icon-sortnumericdesc" : "icon-sortnumericasc"}
                />
            </Button>
        </Button.Group>

        {showSearch && (
            <Input
                size="small"
                // value={search}
                onChange={e => onSearch && onSearch(e.target.value)}
                placeholder="输入名称以过滤"
            />
        )}
    </>);
});

const ChartTooltipContent: React.FC<{
    chart: any;
    dataSource: any[];
    onClick?: (e: any, name: string) => void;
}> = props => {
    const {chart, dataSource, onClick} = props;

    const unit = useMemo(() => get(chart, "options.rawConfig.config.unit", null), [chart]);
    const decimals = useMemo(() => get(chart, "options.rawConfig.config.yAxis.decimals", 2), [chart]);

    const handleClick = (e: any, name: string) => {
        onClick && onClick(e, name);
    };

    return (
        <div className="tooltip-content">
            {dataSource && dataSource.map((item, index) => {
                const cls = classNames("tooltip-list-item", {
                    unselected: item.hidden
                });
                return (
                    <div className={cls} key={item.color + item.name}>
                        <div className="tooltip-color">
                            <span className="tooltip-icon" style={{backgroundColor: item.color}}/>
                        </div>
                        <div className="tooltip-key" onClick={(e) => handleClick(e, item.name)}>{item.name}</div>
                        <div className="tooltip-value">
                            {DataFormatter.tooltipFormatter(unit, item.value, decimals)}
                        </div>
                    </div>
                );
            })}
        </div>
    );
};

export default ChartTooltip;
