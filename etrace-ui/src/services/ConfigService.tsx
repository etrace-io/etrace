import * as API from "../utils/api";
import * as notification from "../utils/notification";
import {handleError} from "$utils/notification";
import {CURR_API} from "$constants/API";

export async function search(config: object) {
    let url = CURR_API.monitor + "/config";
    let message = "获取配置数据";
    try {
        let resp = await API.Get(url, config);
        let appList: Array<any> = resp.data;
        if (!appList) {
            return [];
        }
        return appList;
    } catch (err) {
        handleError(err, message);
    }
}

export async function update(config: object) {
    let url = CURR_API.monitor + "/config";
    let message = "更新配置数据";
    try {
        let resp = await API.Put(url, config);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function create(config: any) {
    let url = CURR_API.monitor + "/config";
    let message = "更新配置数据";
    try {
        let resp;
        if (config.id) {
            message = "更新配置数据";
            resp = await API.Put(url, config);
        } else {
            message = "添加配置数据";
            resp = await API.Post(url, config);
        }
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function deleteConfig(id: number) {
    let url = CURR_API.monitor + "/config/" + id;
    let message = "新建配置数据";
    try {
        let resp = await API.Delete(url);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}
