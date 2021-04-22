import {Get} from "$utils/api";
import {CURR_API} from "$constants/API";

export class HistoryApiService {
    public static async queryHistory(type: string, id: number, pageNum: number = 1, pageSize: number = 10): Promise<any> {
        let url = CURR_API.monitor + "/history/list";
        return Get(url, {type: type, id: id, pageSize: pageSize, pageNum: pageNum});
    }

    public static async queryHistoryDetail(type: string, id: number): Promise<any> {
        let url = CURR_API.monitor + "/history/detail";
        return Get(url, {type: type, id: id});
    }
}
