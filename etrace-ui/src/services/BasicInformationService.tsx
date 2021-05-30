import * as API from "../utils/api";
import {handleError} from "../utils/notification";
import {CURR_API} from "$constants/API";

export async function getUser(ssoToken: string, token: string) {
    const url = `${CURR_API.monitor}/user/info?token=${ssoToken || ""}&token=${token || ""}`;
    let resp = await API.Get(url);
    return resp.data;
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
