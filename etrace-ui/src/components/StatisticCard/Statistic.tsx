import {reaction} from "mobx";
import React from "react";
import {Tooltip} from "antd";
import StatisticNumber from "./Number";
import {autobind} from "core-decorators";
import {FormatConfig, valueType} from "./Utils";
import {get, isEqual} from "lodash";
import StoreManager from "../../store/StoreManager";
import {DataFormatter} from "../../utils/DataFormatter";
import {Chart, ChartStatusEnum} from "../../models/ChartModel";
import ChartEditConfig, {getConfigValue} from "../../containers/Board/Explorer/ChartEditConfig";
import moment from "moment";
import {CaretDownOutlined, CaretUpOutlined, MinusOutlined} from "@ant-design/icons/lib";

const classNames = require("classnames");

export interface StatisticProps extends FormatConfig {
    chart?: Chart;      // 对应 Chart 配置
    height?: number;    // 图表高度
    uniqueId?: string;  // Chart 对应唯一 ID

    value?: valueType;                                          // 数值内容
    className?: string;                                         // 组件 Class
    style?: React.CSSProperties;                                // 组件样式
    valueStyle?: React.CSSProperties;                           // 数值样式
    prefix?: React.ReactNode;                                   // 数值前缀
    suffix?: React.ReactNode;                                   // 数值尾缀
    valueTitle?: React.ReactNode;                               // 数值上方标题
    valueFooter?: React.ReactNode;                              // 数值下方内容
    valueRender?: (node: React.ReactNode) => React.ReactNode;   // 数值渲染函数
}

interface StatisticState {
    currData: any;
    loading: boolean;
}

export default class Statistic extends React.Component<StatisticProps, StatisticState> {
    private static MAX_HEIGHT: number = 480;
    private static MIN_HEIGHT: number = 280;
    private disposer: any;

    // 获取当前高度
    static getHeight(defaultHeight: number) {
        return StoreManager.boardStore.chart
            ? Math.min(Statistic.MAX_HEIGHT, Math.max(window.innerHeight - 210, Statistic.MIN_HEIGHT))
            : defaultHeight;
    }

    constructor(props: StatisticProps) {
        super(props);

        const {uniqueId, chart} = props;
        // 监听 Chart 加载状态
        this.addListenerOfChartStatus(uniqueId);

        this.state = {
            currData: null,
            loading: false,
        };

        StoreManager.chartStore.register(uniqueId, chart);
    }

    shouldComponentUpdate(nextProps: Readonly<StatisticProps>, nextState: Readonly<StatisticState>, nextContext: any): boolean {
        return !(isEqual(this.props.chart, nextProps.chart) && isEqual(this.state.currData, nextState.currData));
    }

    componentWillUnmount(): void {
        if (this.disposer) {
            this.disposer();
        }
    }

    /**
     * 获取环比内容
     * @return React.ReactNode[] 环比内容元素
     */
    getTimeShiftFooter(datasets: any[]) {
        let current: number;

        const items = datasets.map(dataset => {
            const value = get(dataset, "data[0]");
            const timeshift = get(dataset, "metric.timeShift");

            if (!timeshift) {
                current = value;
            }

            return {
                value,
                timeshift,
            };
        });

        return items.map((item, index) => {
            if (!item.timeshift) {
                return null;
            }

            const title = Math.abs(item.timeshift) === 7 * 24 * 60 * 60 * 1000 ? "同比" : "环比";

            return this.renderComparison(index, title, current, item.value, item.timeshift);
        });
    }

    /**
     * 渲染环比 Node
     */
    @autobind
    renderComparison(key: any, title: string, current: number, history: number, timeshift: number) {
        // 获取 timeshift 精度
        const {chart} = this.props;
        const chartConfig = get(chart, "config", {});
        const precision = getConfigValue<number>(ChartEditConfig.display.text.timeshiftPrecision, chartConfig);

        // 获取对比的环比目标日期
        const now = moment();
        const historyDay = now.subtract(Math.abs(timeshift));
        const weekday = ["", "一", "二", "三", "四", "五", "六", "日"][historyDay.isoWeekday()];
        const date = historyDay.format("YYYY.M.D") + `(周${weekday})`;

        const action = current > history ? "增加" : "下降";
        const ratio = Math.abs((current - history) / history).toFixed(precision);

        // 鼠标 Hover 时 Tip
        const tip = `对比 ${date} 同一时间段` + (current === history ? "无变化" : `${action} ${ratio}%`);
        const TrendIcon = current > history
            ? <CaretUpOutlined />
            : (current === history ? <MinusOutlined /> : <CaretDownOutlined />);
        const ratioCls = current > history ? "kpi-rise" : (current === history ? "" : "kpi-drop");

        return (
            <Tooltip title={tip} key={key} overlayStyle={{maxWidth: "unset", fontSize: "12px"}}>
                <div>
                    {title}
                    <span className={classNames("e-monitor-statistic-timeshift", ratioCls)}>
                        {TrendIcon} {ratio}%
                </span>
                </div>
            </Tooltip>
        );
    }

    render() {
        const {currData, loading} = this.state;
        const {chart} = this.props;
        const chartConfig = get(chart, "config", {});

        const valueUnit = getConfigValue<string>(ChartEditConfig.display.text.valueUnit, chartConfig);
        const customValueTitle = getConfigValue(ChartEditConfig.display.text.valueTitle, chartConfig);
        const customSuffix = getConfigValue(ChartEditConfig.display.text.customSuffix, chartConfig);
        const customPrefix = getConfigValue(ChartEditConfig.display.text.customPrefix, chartConfig);
        const showTimeshift = getConfigValue(ChartEditConfig.display.text.showTimeshift, chartConfig);
        const valuePrecision = getConfigValue<number>(ChartEditConfig.display.text.valuePrecision, chartConfig);

        const targetGroup = get(chart, "targets[0].groupBy", []);
        // 获取环比信息，存在 GroupBy 则不存在
        const footer = showTimeshift && targetGroup.length === 0
            ? this.getTimeShiftFooter(get(currData, "datasets", []))
            : null;

        // 如果 Loading 则不渲染 prefix 等
        const {
            value,
            style,
            height,
            className,
            valueStyle,
            valueRender,
            valueFooter = !loading && footer,
            prefix = !loading && customPrefix,
            suffix = !loading && customSuffix,
            valueTitle = !loading && customValueTitle,
            ...others
        } = this.props;

        // 获取 data 中的真实数值
        const dataValue = get(currData, "datasets[0].data[0]", value || null);

        // 根据情况处理
        const processedValue = dataValue !== null
            ? DataFormatter.tooltipFormatter(
                valueUnit,
                dataValue,
                valuePrecision
            )
            : (loading ? "加载中" : "无数据");

        let valueNode: React.ReactNode = (
            <StatisticNumber
                value={targetGroup.length > 0
                    ? "暂不支持添加 GroupBy"
                    : processedValue
                }
                {...others}
            />
        );

        // 自定义 render 函数
        if (valueRender) {
            valueNode = valueRender(valueNode);
        }

        return (
            <div
                className={classNames(["e-monitor-statistic", "vertical-center"], className)}
                style={Object.assign({height: Statistic.getHeight(height)}, style)}
            >
                {/* 数值上方标题 */}
                {valueTitle && <div className="e-monitor-statistic-title">{valueTitle}</div>}

                <div style={valueStyle} className="e-monitor-statistic-content">
                    {/* 前缀 */}
                    {prefix && <span className="e-monitor-statistic-content-prefix">{prefix}</span>}

                    {/* 数值内容 */}
                    {valueNode}

                    {/* 尾缀 */}
                    {suffix && <span className="e-monitor-statistic-content-suffix">{suffix}</span>}
                </div>

                {/* 数值 footer，用于显示环比 */}
                {valueFooter && <div className="e-monitor-statistic-footer">{valueFooter}</div>}
            </div>
        );
    }

    /**
     * 添加对 Chart Loading 的监听
     */
    @autobind
    private addListenerOfChartStatus(uniqueId: string) {
        this.disposer = reaction(
            () => StoreManager.chartStore.chartStatusMap.get(uniqueId),
            chartStatus => {
                if (!chartStatus || chartStatus.status === ChartStatusEnum.Loading) {
                    this.setState({
                        loading: true
                    });
                    return;
                }

                this.setState({
                    currData: StoreManager.chartStore.seriesCache.get(uniqueId),
                    loading: false,
                });
            }
        );
    }

    /**
     * 检查是否有 TimeShift，没有的话加上环比一天和同比七天，并在 ChartStore 注册
     * @param {string} uniqueId 唯一值
     * @param {Chart} chart 对应 Chart
     */
    private checkAndRegisterChart(uniqueId: string, chart: Chart) {
        // const functions = get(chart, "targets[0].functions", []);
        //
        // let hasTimeShift = false;
        // if (functions) {
        //     for (let func of functions) {
        //         console.log(func, func.modelEnum)
        //         if (func.modelEnum === ConvertFunctionEnum.TIME_SHIFT) {
        //             hasTimeShift = true;
        //             break;
        //         }
        //     }
        // }
        //
        // if (!hasTimeShift) {
        //     const functionModel: ConvertFunctionModel = findConverFunctionModelByName(ConvertFunctionEnum.TIME_SHIFT);
        //     const model = cloneDeep(functionModel);
        //     functions.push(model);
        //
        //     set(chart, "targets[0].functions", functions);
        //
        // }
        // console.log("functions", functions, "hasTimeShift", hasTimeShift)
        // console.log(chart);
        //
        // StoreManager.chartStore.register(uniqueId, chart);
    }
}
