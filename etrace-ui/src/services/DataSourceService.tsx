import * as API from "../utils/api";
import * as notification from "../utils/notification";
import {handleError} from "$utils/notification";
import {CURR_API} from "$constants/API";

export interface DataSource {
    id: number;
    name: string;
    status: boolean;
    type: string;
    config: any;
    updatedAt: number;
}

export async function search(dataSource: object) {
    let url = CURR_API.monitor + "/datasource";
    let message = "获取数据源";
    try {
        let resp = await API.Get(url, dataSource);
        let datasourceList: Array<any> = resp.data;
        if (!datasourceList) {
            return [];
        }
        return datasourceList;
    } catch (err) {
        handleError(err, message);
    }
}

export async function save(dataSource: DataSource) {
    let url = CURR_API.monitor + "/datasource";
    let message = "更新数据源";
    try {
        let resp;
        if (dataSource.id) {
            message = "更新数据源";
            resp = await API.Put(url, dataSource);
        } else {
            message = "添加数据源";
            resp = await API.Post(url, dataSource);
        }
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function deleteSource(id: number) {
    let url = CURR_API.monitor + "/datasource/" + id;
    let message = "更新数据源状态";
    try {
        let resp = await API.Delete(url, {status: "Inactive"});
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function fetch() {
    let url = CURR_API.monitor + "/datasource/all";
    let message = "获取数据源";
    try {
        let resp = await API.Get(url);
        let datasourceList: Array<any> = resp.data;
        if (!datasourceList) {
            return [];
        }
        return datasourceList;
    } catch (err) {
        handleError(err, message);
    }
}
