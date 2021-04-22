import * as API from "../utils/api";
import {User, UserConfig} from "$models/User";
import {handleError} from "$utils/notification";
import * as messageUtil from "../utils/message";
import {CURR_API} from "$constants/API";

export async function search(keyword: string) {
    let url = CURR_API.monitor + "/user?keyword=" + keyword;
    let message = "获取用户";
    try {
        let resp = await API.Get(url);
        let userList: Array<User> = resp.data;
        if (!userList) {
            return [];
        }
        return userList;
    } catch (err) {
        handleError(err, message);
    }
}

export async function saveUserConfig(userConfig: UserConfig) {
    let url = CURR_API.monitor + "/user-config";
    let message = "保存用户配置信息";
    try {
        let resp = await API.Post(url, {config: userConfig});
        messageUtil.httpCodeHandler(resp, url, message);
        return resp;
    } catch (err) {
        handleError(err, message);
    }
}
