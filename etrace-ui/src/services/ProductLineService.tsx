import * as API from "../utils/api";
import {handleError} from "$utils/notification";
import {CURR_API} from "$constants/API";

export async function getProductLineList(departmentId: number, params: any) {
    let url = CURR_API.monitor + "/productline/" + departmentId + "/used";
    let message = "获取子部门信息";
    try {
        let resp = await API.Get(url, params);
        let productListList: Array<any> = resp.data;
        if (!productListList) {
            return [];
        }
        return productListList;
    } catch (err) {
        handleError(err, message);
    }
}
