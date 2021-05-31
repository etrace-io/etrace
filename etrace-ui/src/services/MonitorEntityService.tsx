import * as API from "../utils/api";
import * as notification from "../utils/notification";
import {handleError} from "$utils/notification";
import {ReactNode} from "react";
import {SystemKit} from "$utils/Util";
import {CURR_API} from "$constants/API";

export interface MonitorEntity {
    id: number;
    name: string;
    status: string;
    type: string;
    code: string;
    parentId: number;
    datasourceId: number;
    datasource?: any;
    database: string;
    metaLink: string;
    config: string;
    metaUrl: string;
    metaType: string;
    metaPlaceholder: string;
    value?: string;
    label?: ReactNode;
    disabled?: boolean;
    description?: string;
    children?: MonitorEntity[];
    aliasCode?: string;
}

export async function fecth(params?: any, throwError: boolean = false) {
    let url = CURR_API.monitor + "/entity/0/children";
    let message = "获取监控项";
    try {
        let resp = await API.Get(url, params);
        let entityList: Array<any> = resp.data;
        if (!entityList) {
            return [];
        }
        if (resp.status === 401) {
            SystemKit.redirectToLogin(window.location.pathname + window.location.search);
        }
        return entityList;
    } catch (err) {
        if (throwError) {
            throw err;
        } else {
            handleError(err, message);
        }
    }
}

export async function queryEntityByType(type: string, throwError: boolean = false) {
    let url = CURR_API.monitor + "/entity?type=" + type;
    let message = "根据type获取监控项";
    try {
        let resp = await API.Get(url);
        let entityList: Array<any> = resp.data;
        if (!entityList) {
            return [];
        }
        if (resp.status === 401) {
            SystemKit.redirectToLogin(window.location.pathname + window.location.search);
        }
        return entityList;
    } catch (err) {
        if (throwError) {
            throw err;
        } else {
            handleError(err, message);
        }
    }
}

export async function save(entity: MonitorEntity) {
    let url = CURR_API.monitor + "/entity";
    let message = "更新监控项";
    try {
        let resp;
        if (entity.id) {
            message = "更新监控项";
            resp = await API.Put(url, entity);
        } else {
            message = "添加监控项";
            resp = await API.Post(url, entity);
        }
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}
