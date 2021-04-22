import * as API from "../utils/api";
import {handleError} from "../utils/notification";
import * as LinDBService from "./LinDBService";
import {CURR_API} from "$constants/API";

export async function getUser(ssoToken: string, moziToken: string) {
    const url = `${CURR_API.monitor}/user/info?token=${ssoToken || ""}&moziToken=${moziToken || ""}`;
    let resp = await API.Get(url);
    return resp.data;
}

export async function getMoziToken(code: string, redirectUri: string) {
    const url = `${CURR_API.monitor}/user/getMoziToken?code=${code}&redirectUri=${redirectUri}`;
    return API.Get(url);
}

export async function getConsoleAppIds(appId: string): Promise<Array<string>> {
    let url = CURR_API.monitor + "/app/appId?appId=" + appId;
    let message = "获取AppId";
    try {
        let resp = await API.Get(url);
        let appIds: any = resp.data;
        if (!appIds) {
            return [];
        }
        return appIds;
    } catch (err) {
        handleError(err, message);
    }
    return [];
}

export async function getSopushProject(project: string) {
    return await LinDBService.showTagValue("fe", "etrace.dashboard.sopush_request", "project", project);
}

