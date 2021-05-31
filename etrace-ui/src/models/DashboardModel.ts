import {Target} from "./ChartModel";
import {ResultSetModel} from "./ResultSetModel";

declare type DashboardNodeType = "SimpleNode" | "AppNode" | "GroupNode";
export type DashboardNode = DashboardSimpleNode | DashboardAppNode | DashboardGroupNode;
export type DashboardCreateOrUpdateNodeForm = DashboardSimpleNodeForm | DashboardAppNodeForm | DashboardGroupNodeForm;

export enum DashboardNodeTypeEnum {
    SimpleNode = "SimpleNode",
    GroupNode = "GroupNode",
    AppNode = "AppNode",
}

/* 根据 Node Config 请求到的结果 */
export interface DashboardNodeQueryResult {
    alert?: number;
    change?: number;
    publish?: number;
    id?: number | string; // 后面会被改为 Global ID
    appId?: string;
    title?: string;
    nodeType?: string;
    results?: DashboardNodeQueryResultOfMetric[];
    group?: any; // 用于添加 TagFilter
    // index?: number; // 仅用于用于前端标记
    parentGlobalId?: string; // 由于是请求得到的，所以自身没有 Global ID，前端附加一个 Global ID 用于识别
    parentId?: string; // 由于存在 Group 后的节点无 id 的 case，所以此字段用于前端添加识别 Node ID
    nodeConfig?: DashboardNode; // 用于点击时候获取对应的 Node Config
}

export interface DashboardNodeQueryResultOfMetric {
    metricShowName?: string;
    chart?: DashboardNodeChart;
    result?: ResultSetModel;
}

/* Node 配置中的 Chart 信息 */
export interface DashboardNodeChart {
    id: number;
    updatedAt: number;
    createdAt: number;
    departmentId: number;
    productLineId: number;
    title: string;
    status: string;
    globalId: string;
    createdBy?: string;
    updatedBy?: string;
    description?: string;
    adminVisible: boolean;
    config?: any;
    targets?: DashboardNodeChartTarget[];
}

export class DashboardNodeChartTarget extends Target {
    tagKeys?: string[];
}

/* Node Base Type */
export interface DashboardBaseNode {
    id?: number;
    updatedAt?: number;
    createdAt?: number;
    viewCount?: number;
    departmentId?: number;
    favoriteCount?: number;
    productLineId?: number;
    title?: string;
    status?: string;
    globalId?: string;
    createdBy?: string;
    updatedBy?: string;
    description?: string;
    star?: boolean;
    adminVisible?: boolean;
    chartIds?: number[];
    nodeType?: DashboardNodeType;
    charts?: DashboardNodeChart[];
    config?: any;
}

export interface DashboardSimpleNode extends DashboardBaseNode {
}

export interface DashboardAppNode extends DashboardBaseNode {
    group?: { [k in string]: any };
    appId?: string;
}

export interface DashboardGroupNode extends DashboardBaseNode {
    group?: { [k in string]: any };
    groupBy?: string[];
    singleNodeConfig?: any;
}

export interface DashboardGraphRelation {
    to?: number | string;
    from?: number | string;
    source?: number | string;
    target?: number | string;
}

/* Dashboard Graph Model */
export interface DashboardGraph {
    id: number;
    updatedAt: number;
    createdAt: number;
    viewCount: number;
    departmentId: number;
    productLineId: number;
    favoriteCount: number;
    title: string;
    status: string;
    globalId: string;
    createdBy: string;
    updatedBy: string;
    description: string;
    star?: boolean;
    adminVisible: boolean;
    nodeIds: number[];
    config?: any;
    layout?: any;
    nodes: DashboardNode[];
    // relations: DashboardGraphRelation[];
}

export interface DashboardGraphList {
    results: DashboardGraph[];
    total: number;
}

export interface DashboardNodeList {
    results: DashboardNode[];
    total: number;
}

export interface RenderNodeData {
    id: number | string;
    title: string;
    class?: string;
    contents?: RenderNodeDataContent[][];
    nodeInfo: DashboardNodeQueryResult;
    metrics?: DashboardNodeQueryResultOfMetric[];
    size?: [number, number];
    width?: number;
    height?: number;
    originId?: number;
}

export interface RenderNodeDataContent {
    name?: string;
    chart?: any;
    max?: number;
    min?: number;
    avg?: number;
    total?: number;
    current?: number;
}

/* 表单相关 */
export interface DashboardCreateOrUpdateNodeBaseForm {
    id?: number;
    globalId?: string;
    nodeType: DashboardNodeType;
    title: string;
    description?: string;
    chartIds?: number[];
}

export interface DashboardSimpleNodeForm extends DashboardCreateOrUpdateNodeBaseForm {
}

export interface DashboardAppNodeForm extends DashboardCreateOrUpdateNodeBaseForm {
    appId?: string;
}

export interface DashboardGroupNodeForm extends DashboardCreateOrUpdateNodeBaseForm {
    groupBy?: string[];
    singleNodeConfig?: DashboardGroupNodeConfig;
}

export interface DashboardGroupNodeConfig {
    type: DashboardNodeType;
    appId?: string;
    nodeName?: string;
}

// 创建 or 更新大盘表单
export interface DashboardCreateOrUpdateGraphForm {
    id?: number;
    star?: boolean;
    status?: string;
    description?: string;
    adminVisible?: boolean;
    title: string;
    globalId: string;
    departmentId: number;
    productLineId: number;
    nodeIds: number[];
    relations: DashboardGraphRelation[];
    config?: any;
    layout?: any;
}

export enum GraphStatus {
    Init = "init",
    Loading = "loading",
    Loaded = "loaded",
}

export enum NodeStatus {
    Loading = "loading",
    Loaded = "loaded",
}