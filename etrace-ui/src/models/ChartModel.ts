import {UnitModelEnum} from "./UnitModel";
import React from "react";
import {isEmpty, ObjectToMap} from "../utils/Util";
import {AlertEventVO, ChangeEventVO} from "./HolmesModel";
import {ResultSetModel} from "./ResultSetModel";
import {ConvertFunctionModel} from "$utils/ConvertFunction";

export class Chart {
    id?: number;
    globalId?: string;
    loading?: boolean = false;
    title?: string;
    description?: string;
    config?: any;
    from?: string;
    to?: string;
    timeShift?: string;
    targets?: Array<Target>;
    series?: Array<ResultSetModel>;
    status?: string;
    departmentId?: number;
    productLineId?: number;
    createdBy?: string;
    updatedBy?: string;
    updatedAt?: number;
    events?: Array<ChangeEventVO | AlertEventVO>;
    adminVisible?: boolean;
    staticChart?: boolean;
}

// the Model from backend server.
export class MetricBean {
    entity: string;
    prefix?: string;
    measurement?: string;
    groupBy?: Array<string>;
    fields?: Array<string>;
    tagFilters?: Array<TagFilter>;
    functions?: Array<any>;

    from?: string;
    to?: string;

    orderBy?: string;
    limit?: string;

    prefixVariate?: string;
}

export enum ChartTypeEnum {
    Line = "line",
    Area = "area",
    Pie = "pie",
    Column = "column",
    Scatter = "scatter",
    Radar = "radar",
    Text = "text",
    Table = "table",
}

export class ComputeTarget {
    compute?: string;
    alias?: string;
    functions?: Array<any>;
    display?: boolean;

    public static valid(target: ComputeTarget): boolean {
        let compute = target.compute;

        if (isEmpty(target.compute)) {
            return false;
        }
        let keys: Array<number> = [];
        let flag = ComputeTarget.findSpecial(compute, keys);
        if (!flag) {
            return false;
        }
        for (let i = 0; i < keys[0]; i++) {
            compute = compute.replace(/\([+\-*/]/, "");
        }
        for (let i = 0; i < keys[1]; i++) {
            compute = compute.replace("(", "");
        }
        for (let i = 0; i < keys[2]; i++) {
            compute = compute.replace(")", "");
        }
        let n = compute.search(/^((\$\{[A-Z]+\})|[0-9]*)+([+\-*/]{1}((\$\{[A-Z]+\}){1}|[0-9]+){1})*$/);
        if (n < 0) {
            return false;
        }
        return true;
    }

    public static findSpecial(compute: string, keys: Array<number>) {
        let n = compute.split(/\(/).length - 1;
        let m = compute.split(/\)/).length - 1;
        if (n != m) {
            return false;
        }
        let j = compute.split(/\([+\-*/]/).length - 1;
        if (j > 0) {
            n = n - j;
        }
        keys.push(j, n, m);
        return true;
    }

    constructor() {
        this.display = true;
    }
}

export class Target extends MetricBean {
    // Target 类型
    _type?: EMonitorMetricTargetType;

    display?: boolean;
    prefixRequired?: boolean;

    variate?: Array<string>;
    measurementVars?: Array<string>;
    functions?: Array<any>;
    from?: string;
    to?: string;
    // target列表中的编号
    lineIndex?: number;

    // for callstack sampling. should be 'counter', 'timer', 'gauge' etc.
    // if metricType not set, chart click won't trigger sampling.
    metricType?: string;

    // use by StatList to format unit of the value
    statListUnit?: UnitModelEnum;

    // StatList中，默认以groupBy作为list key
    // DalSql的场景，需要展示返回中新增的groupBy的'originSql'
    // 特此新增该字段
    groupByKeys?: Array<string>;

    // 设置是否用固定的查询时间范围，因为一些gauge类型的数据，如果按列表显示时根据需要使用
    fixedTimeRange?: boolean;

    // 用于分析的配置指标
    isAnalyze?: boolean;

    // check target is valid
    public static valid(target: Target): boolean {
        if (target.isAnalyze) {
            return true;
        }
        if (isEmpty(target.measurement)
            || isEmpty(target.fields)) {
            return false;
        }
        return true;
    }

    constructor() {
        super();
        this.prefixRequired = false;
        this.display = true;
    }
}

export interface Targets {
    // default: string;
    [propName: string]: {
        label: string | React.ReactNode;
        target: Target;
    };
}

export class TagFilter {
    display?: boolean = true;
    key?: string;
    op?: string;
    value?: Array<string>;
}

export class StatListItem {
    key: string;
    value: number;
    showRatio: boolean;
    tags: Map<string, string>;
    historyValue?: number;
    timeShift?: number;

    constructor(key: string, value: number, tags: any, showRatio: boolean, timeShift?: number) {
        this.key = key;
        if (showRatio) {
            if (timeShift) {
                this.historyValue = value;
                this.timeShift = timeShift;
                this.value = 0;
            } else {
                this.value = value;
            }
        } else {
            this.value = value;
        }

        this.tags = ObjectToMap(tags);

        this.showRatio = showRatio;
    }
}

export class StatItem {
    measurement: string;
    values: Map<string, number[]>;
    tags: Map<string, string>;
    key: string;
    interval?: number;

    constructor(measurement: string, values: any, tags: any, interval: number) {
        this.measurement = measurement;
        this.key = measurement;
        this.interval = interval;
        if (!isEmpty(tags)) {
            this.key += JSON.stringify(tags);
        }

        this.values = new Map();
        Object.keys(values).forEach(k => {
            this.values.set(k, values[k]);
        });
        this.tags = new Map();
        Object.keys(tags).forEach(k => {
            this.tags.set(k, tags[k]);
        });
    }
}

export const ValueType = {
    COUNT: "count",
    GAUGE: "gauge",
};

Object.freeze(ValueType);

export class StatItemSingle {
    measurement: string;
    values: Map<string, number>;
    tags: Map<string, string>;
    interval?: number;

    constructor(measurement: string, values: any, tags: any, interval: number) {
        this.measurement = measurement;
        this.interval = interval;

        this.values = new Map();
        Object.keys(values).forEach(k => {
            let vals: Array<number> = values[k];
            let result = 0;
            switch (k) {
                case ValueType.COUNT:
                    vals.forEach((value) => {
                        if (value != null) {
                            result = result + value;
                        }
                    });
                    this.values.set(k, result);
                    break;
                case ValueType.GAUGE:
                    let reverseValue: Array<number> = vals.reverse();
                    for (let value of reverseValue) {
                        if (value != null && value > 0) {
                            result = value;
                            break;
                        }
                    }
                    this.values.set(k, result);
                    break;
                default:
                    break;
            }
        });
        this.tags = new Map();
        Object.keys(tags).forEach(k => {
            this.tags.set(k, tags[k]);
        });
    }
}

export class StatListTargets {
    targets: Array<Target>;
    loading?: boolean;
    observable?: Array<string>;
    tracing: Map<string, any>;
    staticLoad?: Array<string>;

    constructor(targets: Array<Target>, observable?: Array<string>, staticLoad?: Array<string>, loading?: boolean) {
        this.targets = targets;
        this.observable = observable;
        this.loading = loading;
        this.tracing = new Map();
        this.staticLoad = staticLoad;
        if (!isEmpty(this.observable)) {
            this.observable.forEach(v => {
                this.tracing.set(v, null);
            });
        }
    }
}

export class TabData {
    id: string;
    name?: string;
    targets?: StatListTargets;
}

export class StatListTree {
    title: string;
    key: string;
    item?: StatListItem;
    children: Array<StatListTree>;
    // father is level one, record father's value
    fatherValue?: string;

    constructor(title: string, key: string, item: StatListItem, children: Array<StatListTree>, fatherValue?: string) {
        this.title = title;
        this.key = key;
        this.item = item;
        this.children = children;
        this.fatherValue = fatherValue;
    }
}

export class ChartStatus {
    loading?: boolean;
    status: ChartStatusEnum;
    msg?: string;
    msgs?: Array<string> = [];

    constructor() {
        this.msgs = [];
    }
}

export enum ChartStatusEnum {
    Init = "init",
    Loading = "loading",
    Loaded = "loaded",
    BadRequest = "badRequest",
    NoData = "noData",
    LoadError = "loadError",
    UnMount = "unMount",
    UnLimit = "unLimit"
}

export interface ChartInfo {
    id?: number;
    updatedAt?: number;
    createdAt?: number;
    // 二级部门 ID
    departmentId?: number;
    // 四级部门 ID
    productLineId?: number;
    // 指标名称
    title?: string;
    // 当前状态（Active or Inactive）
    status?: string;
    // 全局唯一 ID
    globalId?: string;
    // 创建者名称
    createdBy?: string;
    // 更新者名称
    updatedBy?: string;
    // 描述
    description?: string;
    // 二级部门名称
    departmentName?: string;
    // 四级部门名称
    productLineName?: string;
    // 是否仅 Admin 可更改、查看
    adminVisible?: boolean;
    // 指标配置
    config?: any;
    targets?: EMonitorMetricTarget[];
}

export enum MetricStatus {
    INIT = "init",
    EXCEEDS_LIMIT = "exceedsLimit",
    NO_DATA = "noData",
    DONE = "done",
}

/* 服务端返回 Chart 数据 */
export interface EMonitorMetric {
    // 监控类型
    entity: string;
    // 监控项
    prefix?: string;
    // 指标名
    measurement?: string;
    // Group By
    groupBy?: string[];
    // 字段
    fields?: string[];
    // 过滤条件
    tagFilters?: EMonitorMetricTagFilter[];
    // 处理函数
    functions?: Array<any>;
    // 查询时间起点
    from?: string;
    // 查询时间终点
    to?: string;
    // 排序方式
    orderBy?: string;
    // 查询条数限制
    limit?: string;
    // TODO: ？
    prefixVariate?: string;
}

/* 在 Metric 的基础上，进行相应的赋值以支持响应 */
export interface EMonitorMetricTarget extends EMonitorMetric {
    // Target 类型
    _type?: EMonitorMetricTargetType;
    // 是否显示该配置
    display?: boolean;
    // TODO: ？
    prefixRequired?: boolean;
    // 监听的「变量名」列表
    variate?: string[];
    // 「指标名」中的变量，用于替换指标名中的「变量标识」
    measurementVars?: string[];

    // target列表中的编号
    lineIndex?: number;

    // 在采样中，应该为 `counter`、`timer`、`gauge` 等等
    // 如果没有设置，点击图标中的线不会触发采样
    metricType?: string;

    // 用于 StatList 中的单位
    statListUnit?: UnitModelEnum;

    // StatList 中，默认以 groupBy 作为 list key
    // DalSql 的场景，需要展示返回中新增的 groupBy 的 `originSql`
    groupByKeys?: Array<string>;

    // 设置是否用固定的查询时间范围，因为一些 `gauge` 类型的数据，如果按列表显示时根据需要使用
    fixedTimeRange?: boolean;

    // 用于分析的配置指标
    isAnalyze?: boolean;
}

export interface EMonitorMetricComputeTarget {
    // 表达式
    compute?: string;
    // 别名
    alias?: string;
    // 函数
    functions?: Array<any>;
    // 是否展示
    display?: boolean;
}

export interface EMonitorMetricTagFilter {
    display?: boolean;
    key?: string;
    op?: string;
    value?: string[];
}

export enum EMonitorMetricTargetType {
    SUNFIRE = "sunfire",
    LINDB = "lindb",
    PROMETHEUS = "prometheus",
}

// 数据源类型声明
// 通用指标 Target，其他数据源需转为以下类型
export interface EMonitorMetricDataSet {
    // 指标名
    name?: string;
    errorMsg?: string;
    queryType?: string;
    results?: EMonitorMetricDataSetBody | null;
    // Chart 对应配置的函数
    functions?: ConvertFunctionModel[];
    // 查询带上的 tagFilters
    tagFilters?: EMonitorMetricTagFilter[];
    // 额外添加字段，在 SeriesKit 中处理时带上
    // 为了方便在某些场景（如 Series click 的时候）
    // 获取 Series 所在的 Metric 信息
    metricType?: string;
    field?: string;
    tags?: {
        [group: string]: string;
    };
}

export interface EMonitorMetricDataSetBody {
    // 数据开始时间
    startTime?: number;
    // 数据结束时间
    endTime?: number;
    // 数据时间间隔
    interval?: number;
    // 时间范围内打点数
    pointCount?: number;
    // 指标名
    measurementName?: string;
    // 数据内容
    groups?: EMonitorMetricDataPoint[];
    data?: any;
}

export interface EMonitorMetricDataPoint {
    group?: {
        [group: string]: string;
    };
    fields?: {
        [field: string]: number[];
    };
}

// 额外数据源
export interface SunfireMetricDataSet {
    // target 前存在编号
    target?: string;
    datapoints?: [number, number][];
}

export interface PrometheusDataSet {
    status: string;
    data: {
        resultType: string;
        result: PrometheusData[]
    };
}

export interface PrometheusData {
    metric: {
        // __name__: string;
        // tags...
        [tags: string]: string;
    };
    values: [number, number][];
}

// 可视化数据源，用于 ChartJS
export interface EMonitorChartVisualData {
    // 所有数据点
    datasets: EMonitorSeriesData[];
    // 横坐标刻度
    times?: number[];
    // labels: any;
    // 整体时间隔（最小时间间隔）
    interval: any;
    // 左侧中坐标最高点
    // leftMax: any;
    // 右侧纵坐标最高点
    // rightMax: any;
}

// 可视化数据源的数据集 item（每条线）
export type EMonitorSeriesMetric = EMonitorMetricDataSet | EMonitorMetricComputeTarget;
export interface EMonitorSeriesData {
    // 线条颜色
    // borderColor: string;
    // 面积图和饼图需要背景色
    // backgroundColor?: string;
    // 线条样式（用于环比）
    // borderDash?: string[];
    // 数据点
    data: number[];
    // 指标类型：普通指标 or 计算指标
    targetType: EMonitorSeriesDataType;
    // 对应的 metric 的信息
    // TODO: 具体
    metric: EMonitorSeriesMetric;
    // 对应 metric 的行数
    lineNum: string;
    // 原数据线条名称
    name?: string;
    // 线条名称（经过 Alias 处理后的名称）
    label: string;
    // 对应的 item 是否显示（配置中设置）
    display: boolean;
    // 是否隐藏（点击图例后的隐藏、显示）
    // hidden?: boolean;
    // 是否在无数据或空数据的点之间绘制线条;
    // 如果为 `false`，则数据为 NaN 的点将在这条线中产生一个 break；
    spanGaps: boolean;
    // Chart 类型
    type: string;
    yAxisID: string;
    value: {
        total?: number;
        max?: number;
        min?: number;
        avg?: number;
        current?: number;
    };
}

export enum EMonitorSeriesDataType {
    DEFAULT = "default",
    COMPUTED = "computed", // 计算指标
}

export type HiddenSeriesMap = { [label: string]: number };

// 用于表示 Series 信息
export interface ChartSeriesInfo {
    label: string;
    // 在数据集中的索引
    datasetIndex: number;
    // 对应点的真实值（Tooltip 使用）
    _value?: number;
    // 计算值
    computedValue?: {
        total?: number;
        max?: number;
        min?: number;
        avg?: number;
        current?: number;
    };
    // 被搜索过滤后折叠
    collapse?: boolean;
    // 是否为右侧 Legend
    isRightAxis?: boolean;
}

/* Legend 相关 */

export enum LegendValue {
    MAX = "max",
    MIN = "min",
    AVG = "avg",
    TOTAL = "total",
    CURRENT = "current"
}

export enum LegendSort {
    DECREASING = "Decreasing",
    INCREASING = "Increasing",
    NONE = "None", // 恢复原样
}

/* Tooltip 相关 */
export interface TooltipEvent {
    // 当前鼠标坐标
    index: number;
    // 对应的值
    value: number;
    left: number;
    top: number;
    bottom: number;
    nativeEvent: Event;
    // chart: that.chartObj,
    // chartContainer: that.chartContainer,
    // nativeEvent: e
}

export enum TooltipSort {
    DECREASING = "Decreasing",
    INCREASING = "Increasing",
}

export enum TooltipSortBy {
    NAME = "name",
    VALUE = "value",
}

export interface TooltipSortMethod {
    by: TooltipSortBy;
    order: TooltipSort;
}

export enum TOOLTIP_POSITION {
    TOP = "top",
    RIGHT = "right",
    BOTTOM = "bottom",
    LEFT = "left",
}
