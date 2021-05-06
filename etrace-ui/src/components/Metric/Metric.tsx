import {get} from "lodash";
import {reaction} from "mobx";
import {useUnmount} from "ahooks";
import classNames from "classnames";
import screenfull from "screenfull";
import {observer} from "mobx-react";
import ChartFields from "./ChartFields";
import Key from "../../utils/Shortcut/key";
import MetricAnalyze from "./MetricAnalyze";
import Shortcut from "../../utils/Shortcut";
import StoreManager from "$store/StoreManager";
import {DataFormatter} from "$utils/DataFormatter";
import MetricChartStatus from "./MetricChartStatus";
import LazyLoad, {forceCheck} from "react-lazyload";
import TableChart from "$components/Chart/TableChart";
import CanvasChart from "$components/Chart/CanvasChart";
import {uniqueId as generateUUID} from "../../utils/Util";
import React, {useEffect, useRef, useState} from "react";
import {Button, Card, Popover, Space, Tooltip} from "antd";
import Statistic from "$components/StatisticCard/Statistic";
import ChartTooltip from "../Chart/ChartTooltip/ChartTooltip";
import {ChartStatus, ChartStatusEnum, ChartTypeEnum} from "$models/ChartModel";
import ChartEditConfig, {getConfigValue} from "../../containers/Board/Explorer/ChartEditConfig";

import {
    AreaChartOutlined,
    BarChartOutlined,
    DotChartOutlined,
    EditOutlined,
    ExclamationOutlined,
    FileTextOutlined,
    LineChartOutlined,
    PieChartOutlined,
    QuestionOutlined,
    RadarChartOutlined,
    TableOutlined
} from "@ant-design/icons/lib";

import "./Metric.less";

const ReactMarkdown = require("react-markdown/with-html");

export interface MetricShortcut {
    keys: Key[];
    onMatch: (matched: { keys: Key[], native: Event }) => void;
}

interface MetricProps {
    chart?: any;
    uniqueId?: string;
    className?: string;
    style?: React.CSSProperties;
    title?: React.ReactNode;
    extraLinks?: React.ReactNode;
    height?: number;
    awaitLoad?: boolean;
    hideFields?: boolean;
    editFunction?: () => void;
    shortcuts?: MetricShortcut[];
    overlay?: React.ReactNode; // 遮罩层
}

const Metric: React.FC<MetricProps> = props => {
    const {chartStore, urlParamStore} = StoreManager;
    const {title, chart, uniqueId, height, shortcuts, awaitLoad, overlay} = props;
    const {className, style} = props;
    const uuid = useRef(uniqueId || generateUUID());

    const chartContainer = useRef<HTMLDivElement>();
    const fullscreenTarget = useRef();
    // 卸载时候移除
    useUnmount(() => StoreManager.chartStore.unRegister(uuid.current));
    const [showPrivateTooltip, setShowPrivateTooltip] = useState(false);

    useEffect(() => {
        const disposer = reaction(
            () => urlParamStore.forceChanged,
            () => {
                forceCheck();
            });

        return () => disposer();
    }, []);

    useEffect(() => {
        const s = screenfull as screenfull.Screenfull;
        const cb = (e) => {
            setShowPrivateTooltip(s.isFullscreen);
            // setIsFullScreen(s.isFullscreen);
            fullscreenTarget.current = s.isFullscreen ? e.target : null;
        };
        s.on("change", cb);

        return () => s.off("change", cb);
    }, []);

    const interval = chartStore.chartIntervalMap.get(uuid.current);
    const showInterval = getConfigValue<boolean>(
        ChartEditConfig.display.title.showInterval,
        get(chart, "config", {})
    );

    const chartHeight = height || 280;
    const chartType = get(chart, "config.type", "");

    const extraShortcuts = shortcuts && shortcuts.map((shortcut, index) => (
        <Shortcut
            key={index}
            target={chartContainer.current}
            keys={shortcut.keys}
            onMatch={shortcut.onMatch}
        />
    ));

    const chartTitle = title || chart.title;
    const displayTitle = showInterval && interval
        ? <span>{chartTitle} {"(" + DataFormatter.transformFormat(interval) + ")"}</span>
        : chartTitle;
    const cardTitle = (
        <MetricChartTitle
            uniqueId={uuid.current}
            title={displayTitle}
            tooltip={get(chart, "title", "").length > 18 && chart.title}
            type={get(chart, "config.type", ChartTypeEnum.Line)}
        />
    );

    const chartContent = chartType === ChartTypeEnum.Text
        ? <Statistic uniqueId={uuid.current} chart={chart} height={chartHeight}/>
        : chartType === ChartTypeEnum.Table
        ? <TableChart uniqueId={uuid.current} chart={chart} height={chartHeight}/>
        : <CanvasChart uniqueId={uuid.current} chart={chart} height={chartHeight} awaitLoad={awaitLoad}/>;

    const metricCardCls = classNames("metric-container", className, {
        "show-overlay": overlay,
    });

    return (
        <div ref={chartContainer}>
            <Card
                size="small"
                title={cardTitle}
                style={style}
                extra={<MetricExtra {...props} uniqueId={uuid.current}/>}
                key={uuid.current}
                className={metricCardCls}
            >
                <LazyLoad
                    once={true}
                    height={chartHeight}
                    debounce={100}
                    overflow={true}
                    resize={true}
                >
                    {/* loading status */}
                    <MetricChartStatus
                        style={{position: "absolute"}}
                        uniqueId={uuid.current}
                        focus={ChartStatusEnum.Loading}
                    />

                    {/* 锁定 crosshair 线快捷键 */}
                    <Shortcut
                        target={chartContainer.current}
                        keys={["l"]}
                        onMatch={() => StoreManager.chartEventStore.toggleLockCrosshair()}
                    />

                    {/* 其他快捷键 */}
                    {extraShortcuts}

                    {/* 图表显示区域 */}
                    {chartContent && <div className="metric-content">{chartContent}</div>}

                    {/* overlay 区域 */}
                    {overlay && <div className="metric-overlay">{overlay}</div>}

                    {/* innerChartTooltip，全屏模式下开启 */}
                </LazyLoad>
            </Card>
            {showPrivateTooltip && (
                <ChartTooltip
                    fixed={true}
                    uuid={uuid.current}
                    getBoundaryContainer={() => fullscreenTarget.current}
                />
            )}
        </div>
    );
};

interface MetricChartTypeProps {
    type?: ChartTypeEnum;
}

const MetricChartType: React.FC<MetricChartTypeProps> = props => {
    const {type} = props;

    const ChartTypeIcon = () => {
        switch (type) {
            case ChartTypeEnum.Area:
                return <AreaChartOutlined />;
            case ChartTypeEnum.Column:
                return <BarChartOutlined />;
            case ChartTypeEnum.Pie:
                return <PieChartOutlined />;
            case ChartTypeEnum.Radar:
                return <RadarChartOutlined />;
            case ChartTypeEnum.Scatter:
                return <DotChartOutlined />;
            case ChartTypeEnum.Table:
                return <TableOutlined />;
            case ChartTypeEnum.Text:
                return <FileTextOutlined />;
            case ChartTypeEnum.Line:
            default:
                return <LineChartOutlined />;
        }
    };

    const iconTooltip = {
        [ChartTypeEnum.Area]: "是面积图哦",
        [ChartTypeEnum.Column]: "是柱状图哦",
        [ChartTypeEnum.Pie]: "是饼图哦",
        [ChartTypeEnum.Radar]: "是雷达图哦",
        [ChartTypeEnum.Scatter]: "是散点图哦",
        [ChartTypeEnum.Table]: "是表图哦",
        [ChartTypeEnum.Text]: "是文本哦",
        [ChartTypeEnum.Line]: "是线图哦",
    };

    return (
        <Tooltip title={iconTooltip[type]} mouseEnterDelay={1} mouseLeaveDelay={0.2} color="blue" placement="topLeft" arrowPointAtCenter={true}>
            <span><ChartTypeIcon/></span>
        </Tooltip>
    );
};

interface MetricChartTitleProps extends MetricChartTypeProps {
    uniqueId: string;
    title: React.ReactNode;
    tooltip?: React.ReactNode;
}

const MetricChartTitle: React.FC<MetricChartTitleProps> = props => {
    const {type, title, tooltip, uniqueId} = props;

    const titleContent = tooltip
        ? <Tooltip title={tooltip} mouseEnterDelay={0.6} placement="bottom"><span>{title}</span></Tooltip>
        : title;

    return (
        <div className="metric-card-title">
            <MetricChartStatus uniqueId={uniqueId} focus={ChartStatusEnum.UnLimit}/>
            <MetricChartType type={type} />
            <span className="metric-title__content">{titleContent}</span>
        </div>
    );
};

const MetricExtra: React.FC<MetricProps> = observer(props => {
    const items = [];
    const {chart, uniqueId, editFunction, hideFields, extraLinks} = props;
    const {chartStore} = StoreManager;

    const status: ChartStatus = chartStore.chartStatusMap.get(uniqueId);
    const chartDesc = chart ? chart.description : null;
    const analyzeConfig = getConfigValue(ChartEditConfig.analyze, get(chart, "config"));

    if (analyzeConfig && Object.keys(analyzeConfig).length > 0) {
        items.push(<MetricAnalyze key="analyze" config={analyzeConfig} />);
    }

    if (chartDesc) {
        const desc = (
            <ReactMarkdown
                className="e-monitor-markdown"
                source={chartDesc}
                escapeHtml={false}
            />
        );
        items.push(
            <Popover key="desc" placement="bottom" content={desc} overlayStyle={{maxWidth: 500}}>
                <Button
                    shape="circle"
                    size="small"
                    icon={<QuestionOutlined />}
                />
            </Popover>);
    }

    if (editFunction) {
        items.push(
            <Popover key="edit" arrowPointAtCenter={true} content="显示该指标详情（仅 Admin 可见）">
                <Button
                    type="dashed"
                    shape="circle"
                    size="small"
                    icon={<EditOutlined />}
                    onClick={editFunction}
                />
            </Popover>
        );
    }

    if (status && status.msgs && status.msgs.length > 0) {
        const errMsg = status.msgs.map((msg, idx) => (
            <p key={idx}>{msg}</p>
        ));
        items.push(
            <Tooltip key="err" title={errMsg} overlayStyle={{maxWidth: "none"}}>
                <Button
                    type="dashed"
                    danger={true}
                    shape="circle"
                    size="small"
                    icon={<ExclamationOutlined />}
                />
            </Tooltip>
        );
    }

    if (status && !hideFields) {
        items.push(<ChartFields key={uniqueId} uniqueId={uniqueId} />);
    }

    if (extraLinks) {
        items.push(<React.Fragment key="extraLinks">{extraLinks}</React.Fragment>);
    }

    return <Space>{items}</Space>;
});

export default observer(Metric);
