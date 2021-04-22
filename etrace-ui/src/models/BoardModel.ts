import {Chart} from "./ChartModel";

export class Board {
    id?: number;
    globalId?: string;
    title?: string;
    description?: string;
    status?: string;
    productLine?: number;
    department?: number;
    departmentId?: number;
    productLineId?: number;
    productLineName?: string;
    departmentName?: string;
    favoriteCount?: number;
    viewCount?: number;
    layout: Array<Layout>;
    config?: BoardConfig;
    chartIds?: Array<number>;
    charts?: Array<Chart>;
    updatedAt?: number;
    createdAt?: number;
    createdBy?: string;
    updatedBy?: string;
    star?: boolean;

    adminVisible?: boolean;

    constructor() {
        this.layout = [];
    }
}

export interface Layout {
    // 当前折叠状态
    show?: boolean;
    title?: string;
    panels: Array<Panel>;
    titleShow?: boolean;
    chartHeight?: number;
    // 默认折叠当前行
    defaultFold?: boolean;
}

export interface Panel {
    chartId?: number;
    span?: number;
    chart?: Chart;
    globalId?: string;
}

export class BoardConfig {
    refresh?: string;
    time?: any;
    variates?: Array<Variate>;
    links?: Array<Link>;
}

export class Link {
    target?: string;
    title?: string;
    url?: string;
    time_range?: boolean;
    variable_value?: boolean;
}

export enum VariateType {
    HTTP = "http",
    ENUM = "enum",
    METRIC = "metric",
    TARGET = "target"
}

export declare type Variate = MetricVariate | EnumVariate | HttpVariate | TargetVariate;

export class MetricVariate {
    // the name shown on UI: 变量显示的名称
    label: string;
    // 该变量控制的tag字段, 看板唯一
    name: string;

    entity: string;
    measurement: string;
    prefix?: string;
    prefixKey?: string; // 从url params中取值，替换默认的prefix

    current?: Array<string>;

    type: VariateType = VariateType.METRIC;

    // 是否仅可单选
    onlySingleSelect: boolean;

    // "级联选择"功能：它依赖的其他tagKeys。若某tag未选择value，则忽略该tag；若已选择，则此MetricVariate只会查询筛选后的值。
    relatedTagKeys: Array<string> = [];

    constructor(label: string, name: string, entity: string, measurement: string, relatedTagKeys?: Array<string>, prefix?: string, prefixKey?: string) {
        this.label = label;
        this.name = name;
        this.entity = entity;
        this.measurement = measurement;
        this.prefix = prefix;
        this.type = VariateType.METRIC;
        if (relatedTagKeys) {
            this.relatedTagKeys = relatedTagKeys;
        } else {
            this.relatedTagKeys = [];
        }

        this.prefixKey = prefixKey;
    }
}

export class TargetVariate {
    label: string;
    name: string;
    entity: string;
    prefix?: string;
    measurement: string;
    groupBy: Array<string>;
    fields: Array<string>;
    variateKey: string;
    type: VariateType = VariateType.TARGET;
    current?: Array<string>;

    // 是否仅可单选
    onlySingleSelect: boolean;

    constructor(label: string, name: string, entity: string, measurement: string, variateKey: string, groupBy: Array<string>, fields: Array<string>, prefix?: string) {
        this.label = label;
        this.name = name;
        this.entity = entity;
        this.prefix = prefix;
        this.measurement = measurement;
        this.groupBy = groupBy;
        this.fields = fields;
        this.variateKey = variateKey;
        this.type = VariateType.TARGET;
    }
}

export class EnumVariate {
    label: string;
    name: string;

    lists: string;

    current?: Array<string>;

    type: VariateType;

    // 是否仅可单选
    onlySingleSelect: boolean;

    constructor(label: string, name: string, lists: string) {
        this.label = label;
        this.name = name;
        this.lists = lists;
        this.type = VariateType.ENUM;
    }
}

export class HttpVariate {
    label: string;
    name: string;

    query: string;
    current?: Array<string>;

    type: VariateType;

    // 是否仅可单选
    onlySingleSelect: boolean;

    constructor(label: string, name: string, query: string) {
        this.label = label;
        this.name = name;
        this.query = query;
        this.type = VariateType.HTTP;
    }
}
