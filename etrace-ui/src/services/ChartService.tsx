import * as API from "$utils/api";
import {Get, GetAndParse} from "$utils/api";
import * as notification from "$utils/notification";
import {handleError} from "$utils/notification";
import * as msg from "$utils/message";
import {Chart, ChartInfo} from "$models/ChartModel";
import {CURR_API} from "$constants/API";
import {MonitorHttp} from "$services/http";

export async function search(chart: object) {
    let url = CURR_API.monitor + "/chart";
    let message = "搜索指标信息";
    try {
        let resp = await API.Get(url, chart);
        let charts = resp.data;
        return charts;
    } catch (err) {
        handleError(err, message);
    }
}

export async function get(chartId: number): Promise<Chart> {
    let url = CURR_API.monitor + "/chart/" + chartId;
    let message = "获取指标信息";
    try {
        let resp = await API.Get(url);
        let chart: Chart = resp.data;
        if (!chart) {
            return null;
        }
        return chart;
    } catch (err) {
        handleError(err, message);
    }
}

export async function getChart(chartId: number): Promise<Chart> {
    let url = CURR_API.monitor + "/chart/" + chartId;
    let message = "获取指标信息";
    try {
        return GetAndParse(url, Chart);
    } catch (err) {
        handleError(err, message);
    }
}

export async function validateBoardGlobalId(globalId: string): Promise<boolean> {
    let url = CURR_API.monitor + "/dashboard/checkGlobalId?globalId=" + globalId;
    let ok = await Get(url);
    return ok.data;
}

export async function validateChartGlobalId(globalId: string): Promise<boolean> {
    let url = CURR_API.monitor + "/chart/checkGlobalId?globalId=" + globalId;
    let ok = await Get(url);
    return ok.data;
}

export async function searchByGroup(chart: object, monitorUrl?: string) {
    let url = (monitorUrl ? monitorUrl : CURR_API.monitor) + "/chart";
    let message = "获取指标信息";
    try {
        let resp = await API.Get(url, chart);
        let chartList: Array<any> = resp.data;
        if (!chartList) {
            return [];
        }
        return chartList;
    } catch (err) {
        handleError(err, message);
    }
}

export async function deleteChartById(chartId: number) {
    let url = CURR_API.monitor + "/chart/" + chartId;
    let message = "废弃指标";
    try {
        let resp = await API.Delete(url);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function rollbackChartById(chartId: number) {
    let url = CURR_API.monitor + "/chart/" + chartId + "?status=Active";
    let message = "启用指标";
    try {
        let resp = await API.Delete(url);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function sync(chart: Chart, monitorUrl: string) {
    let url = monitorUrl + "/chart/sync";
    let message = "同步指标信息";
    try {
        let resp = await API.Put(url, chart);
        notification.httpCodeHandler(resp, url, message);
        return resp.data;
    } catch (err) {
        handleError(err, message);
        throw err;
    }
}

export async function save(chart: Chart, monitorUrl?: string, forceToCreate?: boolean) {
    let url = (monitorUrl ? monitorUrl : CURR_API.monitor) + "/chart";
    let message = "更新指标信息";
    try {
        let resp;
        if (forceToCreate) {
            message = "强制新建指标信息";
            resp = await API.Post(url, chart);
        } else if (chart.id) {
            message = "更新指标信息";
            resp = await API.Put(url, chart);
        } else {
            message = "新建指标信息";
            resp = await API.Post(url, chart);
        }
        notification.httpCodeHandler(resp, url, message);
        return resp.data;
    } catch (err) {
        handleError(err, message);
    }
}

export async function analyze(config: any) {
    const url = `${CURR_API.monitor}/analyze`;
    let message = "获取指标分析结果";
    try {
        let resp = await API.Put(url, config);
        msg.httpCodeHandler(resp, url, message);
        return resp.data;
    } catch (err) {
        handleError(err, message);
        throw err;
    }
}

function fetchChartByGroup(groupBy: Object, baseURL?: string) {
    const url = baseURL ? `${baseURL}/chart` : "/chart";
    return MonitorHttp.get<{
        results: ChartInfo[];
        total: number;
    }>(url, { params: groupBy });
}

function fetchChartByGlobalId(globalId: string) {
    return fetchChartByGroup({
        globalId,
        status: "Active",
    }).then(res => res.results.length > 0
        ? res.results.find(chart => chart.globalId === globalId)
        : null
    );
}

function fetchChartById(id: string) {
    return MonitorHttp.get(`/chart/${id}`);
}

export default {
    fetchChartById,
    fetchChartByGlobalId,
};
