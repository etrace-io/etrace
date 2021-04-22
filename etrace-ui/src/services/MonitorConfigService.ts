import * as API from "../utils/api";
import {handleError} from "$utils/notification";
import {CURR_API} from "$constants/API";

export async function getAll() {
    let url = CURR_API.trace_monitor + "/config";
    let message = "获取监控配置";
    try {
        let resp = await API.Get(url);
        let configList: Array<any> = resp.data;
        if (!configList) {
            return [];
        }
        return configList;
    } catch (err) {
        handleError(err, message);
    }
}

export async function getAlertInfo() {
    let url = CURR_API.trace_monitor + "/alertInfo";
    let message = "获取监控通知配置";
    try {
        let resp = await API.Get(url);
        let notices: Array<any> = resp.data;
        if (!notices) {
            return [];
        }
        return notices;
    } catch (err) {
        handleError(err, message);
    }
}
