import * as API from "../utils/api";
import * as notification from "../utils/notification";
import {handleError} from "$utils/notification";
import {CURR_API} from "$constants/API";

interface ProblemList {
    results?: Problem[];
    total?: number;
}

interface Problem {
    createdAt?: number;
    createdBy?: string;
    dataStatus?: string;
    description?: string;
    id?: number;
    solver?: string;
    status?: string;
    type?: string;
    updatedAt?: number;
}

export async function search(params: any): Promise<ProblemList> {
    let url = CURR_API.monitor + "/feedback/search";
    let message = "获取问题列表";
    let problemInfo: ProblemList = {};
    try {
        let resp = await API.Get(url, params);
        problemInfo = resp.data;
        if (!problemInfo) {
            return {};
        }
    } catch (err) {
        handleError(err, message);
    }
    return problemInfo;
}

export async function countByStatus() {
    let url = CURR_API.monitor + "/feedback/countByStatus";
    let message = "获取问题状态数量";
    let countInfo: ProblemList = {};
    try {
        let resp = await API.Get(url);
        countInfo = resp.data;
        if (!countInfo) {
            return {};
        }
    } catch (err) {
        handleError(err, message);
    }
    return countInfo;
}

export async function createOrUpdate(problem: any) {
    let url = CURR_API.monitor + "/feedback";
    let message = "创建反馈问题";
    let problemList = [];
    try {
        let resp;
        if (problem.id) {
            message = "更新反馈问题";
            resp = await API.Put(url, problem);
        } else {
            message = "创建反馈问题";
            resp = await API.Post(url, problem);
        }
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
    return problemList;
}

export async function deleteProblem(id: number) {
    let url = CURR_API.monitor + "/feedback/" + id;
    let message = "删除反馈问题";
    let problemList = [];
    try {
        let resp = await API.Delete(url);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
    return problemList;
}
