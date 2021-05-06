import * as API from "../utils/api";
import {handleError} from "../utils/notification";

export async function getMeta(url: string): Promise<Array<any>> {
    let message = "获取Meta";
    try {
        let resp = await API.Get(url);
        let meta: Array<any> = resp.data;
        if (!meta) {
            return [];
        }
        return meta;
    } catch (err) {
        handleError(err, message);
    }
}