import {notification} from "antd";
import {Delete, Get, Post, Put} from "$utils/api";
import {messageHandler} from "$utils/message";
import {handleError} from "$utils/notification";
import {
    DashboardCreateOrUpdateGraphForm,
    DashboardCreateOrUpdateNodeForm,
    DashboardGraph,
    DashboardGraphList,
    DashboardNode,
    DashboardNodeList,
    DashboardNodeQueryResult
} from "$models/DashboardModel";
import {SystemKit} from "$utils/Util";
import {browserHistory} from "$utils/UtilKit/SystemKit";
import {CURR_API} from "$constants/API";

const monitorUrl = CURR_API.monitor;

/**
 * 获取大盘列表
 */
export async function getGraphList(page: number, pageSize: number = 24, options: any = {}, isFavorite?: boolean, envUrl?: string): Promise<DashboardGraphList> {
    const params = Object.keys(options)
        .filter(i => options[i] !== null && options[i] !== undefined && options[i] !== "")
        .map(key => `&${key}=${options[key]}`)
        .join("");

    const url = isFavorite
        ? `${envUrl || monitorUrl}/user-action/favorite/graph?current=${page}&pageSize=${pageSize}${params}`
        : `${envUrl || monitorUrl}/graph?current=${page}&pageSize=${pageSize}${params}`;
    return checkResponse(Get(url));
}

/**
 * 查询指定大盘详细信息
 */
export async function getGraphWithId(id: number): Promise<DashboardGraph> {
    const url = `${monitorUrl}/graph/${id}`;
    return checkResponse(Get(url));
}

/* 创建 or 更新大盘 */
export async function createOrUpdateGraph(data: DashboardCreateOrUpdateGraphForm) {
    const url = `${monitorUrl}/graph`;

    const isCreate = !data.id;
    const tip = isCreate ? "创建" : "更新";

    return checkResponse(
        isCreate ? Post(url, data) : Put(url, data),
        () => messageHandler("success", `${tip}成功`)
    );
}

export async function deleteGraph(id: number) {
    const url = `${monitorUrl}/graph/${id}`;
    return checkResponse(Delete(url));
}

export async function rollbackGraph(id: number) {
    const url = `${monitorUrl}/graph/${id}?status=Active`;
    return checkResponse(Delete(url));
}

export async function syncGraph(graph: any, syncUrl: string) {
    const url = `${syncUrl || monitorUrl}/graph/sync`;
    return checkResponse(Put(url, graph));
}

/* 收藏大盘 */
export async function changeGraphFavorite(id: number, targetStatus: boolean) {
    const url = `${monitorUrl}/user-action/favorite/graph/${id}`;

    const resp = targetStatus ? Put(url) : Delete(url);
    const tips = targetStatus ? "收藏" : "取消收藏";

    return checkResponse(resp, () => messageHandler("success", `${tips}成功`));
}

/**
 * 获取 Node 列表
 */
export async function getNodeList(page: number, pageSize: number = 24, options: any = {}, isFavorite?: boolean, envUrl?: string): Promise<DashboardNodeList> {
    const params = Object.keys(options)
        .filter(i => options[i] !== null && options[i] !== undefined && options[i] !== "")
        .map(key => `&${key}=${options[key]}`)
        .join("");

    const url = isFavorite
        ? `${envUrl || monitorUrl}/user-action/favorite/node?current=${page}&pageSize=${pageSize}${params}`
        : `${envUrl || monitorUrl}/node?current=${page}&pageSize=${pageSize}${params}`;
    return checkResponse(Get(url));
}

/**
 * 获取指定 Node 详细信息
 */
export async function getNodeConfigWithId(id: number | string): Promise<DashboardNode> {
    const url = `${monitorUrl}/node/${id}`;
    return checkResponse(Get(url));
}

/**
 * 根据 Node 配置查询 Node 结果
 */
export async function getNodeInfoWithConfig(config: DashboardNode): Promise<DashboardNodeQueryResult[]> {
    const url = `${monitorUrl}/node/query`;
    return checkResponse(Put(url, config));
}

/**
 * 创建 Node
 */
export async function createOrUpdateNode(data: DashboardCreateOrUpdateNodeForm) {
    const url = `${monitorUrl}/node`;

    const isCreate = !data.id;
    const tip = isCreate ? "创建" : "更新";

    return checkResponse(
        isCreate ? Post(url, data) : Put(url, data),
        () => messageHandler("success", `${tip}成功`)
    );
}

export async function deleteNode(id: number) {
    const url = `${monitorUrl}/node/${id}`;
    return checkResponse(Delete(url));
}

export async function rollbackNode(id: number) {
    const url = `${monitorUrl}/node/${id}?status=Active`;
    return checkResponse(Delete(url));
}

export async function syncNode(graph: any, syncUrl: string) {
    const url = `${syncUrl || monitorUrl}/node/sync`;
    return checkResponse(Put(url, graph));
}

/* 收藏 Node */
export async function changeNodeFavorite(id: number, targetStatus: boolean) {
    const url = `${monitorUrl}/user-action/favorite/node/${id}`;

    const resp = targetStatus ? Put(url) : Delete(url);
    const tips = targetStatus ? "收藏" : "取消收藏";

    return checkResponse(resp, () => messageHandler("success", `${tips}成功`));
}

async function checkResponse(req: Promise<any>, onFulfilled?: () => void) {
    let res;

    try {
        res = await req;
    } catch (e) {
        handleError(e, "获取大盘数据出错");
    }

    if (res) {
        const {status, data} = res;

        if (status < 300 && status >= 200) {
            if (onFulfilled) {
                onFulfilled();
            }
            return data;
        } else if (status === 401) {
            SystemKit.redirectToLogin(browserHistory.location.pathname + browserHistory.location.search);
        } else {
            notification.error({
                message: data.message,
                description: `(${res.status}) ${res.config.url}`,
                duration: 5
            });
        }
    }

    return {};
}
