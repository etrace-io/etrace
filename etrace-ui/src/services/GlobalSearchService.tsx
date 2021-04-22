import {Get} from "$utils/api";
import {AxiosResponse} from "axios";
import {notification} from "antd";
import {MetricBean} from "$models/ChartModel";
import {SystemKit} from "$utils/Util";
import {CURR_API} from "$constants/API";

const baseUrl = CURR_API.monitor + "/search";

/**
 * 获取搜索建议
 * @param {string} key 关键字
 * @param options 附加选项
 * @return {Promise<any>} data | null
 */
export async function getSearchSuggest(key: string, options: GlobalSearchOptions = {}): Promise<SuggestResultGroup[] | null> {
    const {category, page, size} = options;

    const res = await Get(baseUrl + "/suggest", {
        key,
        category,
        page,
        size,
    });

    return checkResponse(res);
}

/**
 * 获取搜索结果
 * @param {string} key 关键字
 * @param options 附加选项
 * @return {Promise<any>} data | null
 */
export async function getSearchQuery(key: string, options: GlobalSearchOptions = {}): Promise<SuggestResultGroup[] | null> {
    const {category, page, size} = options;

    const res = await Get(baseUrl + "/query", {
        key,
        category,
        page,
        size,
    });

    return checkResponse(res);
}

function checkResponse(res: AxiosResponse): SuggestResultGroup[] | null {
    if (res) {
        const {status, data} = res;

        if (status === 200) {
            return data;
        } else if (status === 401) {
            SystemKit.redirectToLogin();
        } else {
            notification.error({
                message: data.message,
                description: `(${res.status}) ${res.config.url}`,
                duration: 5
            });
        }
    } else {
        notification.error({message: "获取搜索建议出错", description: "", duration: 5});
    }

    return null;
}

export declare type GlobalSearchOptions = {category?: string; page?: number; size?: number};

export enum SearchResultCategory {
    HOSTNAME = "HOSTNAME",
    APPID = "APPID",
    USER = "USER",
    CHART = "CHART",
    DASHBOARDAPP = "DASHBOARDAPP",
    DASHBOARD = "DASHBOARD",
    CHANGE = "CHANGE",
    ALERT = "ALERT",
    DALGROUP = "DALGROUP",
    RPCID = "RPCID",
}

export interface SuggestResultGroup {
    category: SearchResultCategory;
    label: string;
    order: number;
    contents: SuggestResultContents[];
}

export declare type SuggestResultContents =
    string |
    AppId |
    HostName |
    User |
    DashboardApp |
    Dashboard |
    Chart |
    Change |
    Alert |
    DalGroup |
    RpcId;

export interface HostName {
    id: number;
    hd: string;
    env: string;
    idc: string;
    cpu: string;
    mem: string;
    status: 0 | 1;
    appId: string;
    ezone: string;
    os_ver: string;
    region: string;
    nic0Ip: string;
    hostname: string;
    hostType: string;
    confModel: string;
    partition: string;
    use_status: string;
}

export interface AppId {
    id: number;
    departmentId: number;
    productLineId: number;
    critical: boolean;
    multiZone: boolean;
    globalZone: boolean;
    appId: string;
    appType: string;
    moduleName: string;
    moduleOwner: string;
    departmentName: string;
    productLineName: string;
}

export interface User {
    id: number;
    psncode: string;
    psnname: string;
    deptcode: string;
    deptname: string;
    fatdeptcode: string;
    fatdeptname: string;
    onedeptname: string;
    apiUser: boolean;
    isApiUser: boolean;
}

export interface DashboardApp {
    id: number;
    order: number;
    updatedAt: number;
    createdAt: number;
    departmentId: number;
    productLineId: number;
    favoriteCount: number;
    title: string;
    status: string;
    createdBy: string;
    description: string;
    departmentName: string;
    productLineName: string;
    critical: boolean;
    dashboardIds: number[];
    icon: {
        type: string;
        value: string
    };
}

export interface Dashboard {
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
    departmentName: string;
    productLineName: string;
}

export interface Chart {
    id: number;
    updatedAt: number;
    createdAt: number;
    departmentId: number;
    productLineId: number;
    title: string;
    status: string;
    globalId: string;
    updatedBy: string;
    createdBy: string;
    departmentName: string;
    productLineName: string;
    adminVisible: boolean;
    targets: MetricBean[];
}

export interface Change {
    timestamp: number;
    metricTimestamp: number;
    severity: boolean;
    isKeyPath: boolean;
    source: string;
    content: string;
    operator: string;
    department: string;
    description: string;
    eventAction: string;
    parentDepartment: string;
    appIds: string[];
}

export interface Alert {
    timestamp: number;
    metricTimestamp: number;
    payload: string;
    eventType: string;
    department: string;
    parentDepartment: string;
    appIds: string[];
}

export interface AlertPayload {
    event: {
        fields: any;
        metricName: string;
        tags: any;
        timestamp: number
    };
    appId: string;
    message: string;
    dataSource: string;
    metricName: string;
    policyId: number;
    messageId: number;
    triggeredAt: number;
}

export interface DalGroup {
    groupName: string;
    apps: DalGroupApps[];
    dbGroups: DalGroupDBgroups[];
}

export interface DalGroupApps {
    appId: string;
}

export interface DalGroupDBgroups {
    database: string;
    name: string;
}

export interface RpcId {
    hour: number;
    index: number;
    offset: number;
    blockId: number;
    ip: string;
    appId: string;
    reqId: string;
    rpcId: string;
    rpcType: string;
    rpcInfo: RpcInfo;
}

export interface RpcInfo {
    SQL: number;
    Redis: number;
    status: number;
    duration: number;
    timestamp: number;
    url: string;
    name: string;
    ezone: string;
    interface: string;
    operation: string;
    shardingkey: string;
}
