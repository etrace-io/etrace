import * as API from "../utils/api";
import * as notification from "../utils/notification";
import {handleError} from "$utils/notification";
import {CURR_API} from "$constants/API";

export async function searchByGroup(path: string, params: object) {
    let url = CURR_API.monitor + "/" + path;
    let message = "获取App";
    try {
        let resp = await API.Get(url, params);
        let dataAppList: Array<any> = resp.data;
        if (!dataAppList) {
            return [];
        }
        return dataAppList;
    } catch (err) {
        handleError(err, message);
    }
}

export async function get(dashboardAppId: number) {
    let url = CURR_API.monitor + "/dashboard/app/" + dashboardAppId;
    let message = "获取App信息";
    try {
        let resp = await API.Get(url);
        let dataApp: object = resp.data;
        if (!dataApp) {
            return {};
        }
        return dataApp;
    } catch (err) {
        handleError(err, message);
    }
}

export async function search(board: object) {
    let url = CURR_API.monitor + "/dashboard/app";
    let message = "获取App";
    try {
        let resp = await API.Get(url, board);
        let appList: Array<any> = resp.data;
        if (!appList) {
            return [];
        }
        return appList;
    } catch (err) {
        handleError(err, message);
    }
}

export async function searchBusiness(board: object) {
    let url = CURR_API.monitor + "/dashboard/app/business";
    let message = "获取关键业务App";
    try {
        let resp = await API.Get(url, board);
        let appList: Array<any> = resp.data;
        if (!appList) {
            return [];
        }
        return appList;
    } catch (err) {
        handleError(err, message);
    }
}

export async function update(dataApp: object) {
    let url = CURR_API.monitor + "/dashboard/app";
    let message = "更新App信息";
    try {
        let resp = await API.Put(url, dataApp);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function updateBusinessOrder(firstId: number, secondId: number) {
    let url = CURR_API.monitor + "/dashboard/app/order";
    let message = "更新App信息";
    try {
        let resp = await API.Get(url + "?firstId=" + firstId + "&secondId=" + secondId);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function create(dataApp: object) {
    let url = CURR_API.monitor + "/dashboard/app";
    let message = "新建App信息";
    try {
        let resp = await API.Post(url, dataApp);
        notification.httpCodeHandler(resp, url, message);
        return resp.data;
    } catch (err) {
        handleError(err, message);
    }
}
