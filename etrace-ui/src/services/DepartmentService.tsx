import * as API from "../utils/api";
import {handleError} from "$utils/notification";
import {CURR_API} from "$constants/API";

export async function fetch(params: any) {
    let url = CURR_API.monitor + "/department/used";
    let message = "获取部门信息";
    try {
        let resp = await API.Get(url, params);
        let departmentList: Array<any> = resp.data;
        if (departmentList) {
            return departmentList;
        }
    } catch (err) {
        handleError(err, message);
    }
    return [];
}

export async function getTreeData(params?: any) {
    let url = CURR_API.monitor + "/department/detail/used";
    let message = "获取组织关系";
    try {
        let resp = await API.Get(url, params);
        let departmentList: Array<any> = resp.data;
        if (departmentList) {
            return departmentList;
        }
    } catch (err) {
        handleError(err, message);
    }
    return [];
}

export async function getDefaultTreeData() {
    let url = CURR_API.monitor + "/department/detail";
    let message = "获取组织关系";
    try {
        let resp = await API.Get(url);
        let departmentList: Array<any> = resp.data;
        if (departmentList) {
            return departmentList;
        }
    } catch (err) {
        handleError(err, message);
    }
    return [];
}
