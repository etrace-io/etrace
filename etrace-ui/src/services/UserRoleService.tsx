import * as API from "../utils/api";
import * as notification from "../utils/notification";
import {handleError} from "$utils/notification";
import {CURR_API} from "$constants/API";

export interface UserRole {
    id: number;
    name: string;
    status: boolean;
    type: string;
    config: any;
    updatedAt: number;
}

export async function search(userRole: object) {
    let url = CURR_API.monitor + "/user/all";
    let message = "获取用户权限";
    try {
        let resp = await API.Get(url, userRole);
        let userRoleList: Array<any> = resp.data;
        if (!userRoleList) {
            return [];
        }
        return userRoleList;
    } catch (err) {
        handleError(err, message);
    }
}

export async function save(userRole: UserRole) {
    let url = CURR_API.monitor + "/user_role";
    let message = "用户权限管理";
    try {
        let resp;
        if (userRole.id) {
            message = "更新用户权限";
            resp = await API.Put(url, userRole);
        } else {
            message = "新建用户权限";
            resp = await API.Post(url, userRole);

        }
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function deleteUser(id: number) {
    let url = CURR_API.monitor + "/user_role/" + id;
    let message = "删除用户权限";
    try {
        let resp = await API.Delete(url);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }

}
