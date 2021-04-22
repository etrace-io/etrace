import {GetAndParseAsArray} from "$utils/api";
import {AlertEventVO, ChangeEventVO, RequestIdInfo} from "$models/HolmesModel";
import {CURR_API} from "$constants/API";

export class HolmesApiService {

    public static async getAlertEventsByAppId(appId: string, from: number, to: number, page?: number, pageSize?: number): Promise<Array<AlertEventVO>> {
        let url = CURR_API.root + "/events/alert?from=" + from + "&to=" + to + "&appId=" + appId;
        if (page) {
            url += "&page=" + page;
        }
        if (pageSize) {
            url += "&pageSize=" + pageSize;
        }
        return GetAndParseAsArray(url, AlertEventVO, null, e => {
            console.warn("Fail to query changeEvent for ", appId, " from: ", from, ", to: ", to);
        });
    }

    public static async getSortedRequestIdInfo(requestId: string, timestamp: number, size: number, currentPage: number): Promise<Array<RequestIdInfo>> {
        let url = CURR_API.monitor + "/proxy/rpcId?requestId=" + encodeURIComponent(requestId) + "&timestamp=" + timestamp + "&pageSize=" + size + "&currentPage=" + currentPage;
        return GetAndParseAsArray(url, RequestIdInfo);
    }

    public static async getChangeEventsByAppId(appId: string, from: number, to: number, page?: number, pageSize?: number): Promise<Array<ChangeEventVO>> {
        let url = CURR_API.root + "/events/change?from=" + from + "&to=" + to + "&appId=" + appId;
        if (page) {
            url += "&page=" + page;
        }
        if (pageSize) {
            url += "&pageSize=" + pageSize;
        }
        return GetAndParseAsArray(url, ChangeEventVO, null, e => {
            console.warn("Fail to query changeEvent for ", appId, " from: ", from, ", to: ", to);
        });
    }
}
