import {GetAndParseAsArray} from "$utils/api";
import {OrderOrShipInfo, SimpleRequestIdAndRpcId} from "../models/SearchModel";
import {CURR_API} from "$constants/API";

export class SearchApiService {
    public static async queryOrderId(orderId: string): Promise<Array<OrderOrShipInfo>> {
        let url = CURR_API.monitor + "/proxy/order?cluster=all&type=1&id=" + orderId;
        return GetAndParseAsArray(url, OrderOrShipInfo, null, e => {
            console.warn("Fail to query order for ", orderId);
        });
    }

    public static async queryShipId(shipId: string): Promise<Array<OrderOrShipInfo>> {
        let url = CURR_API.monitor + "/proxy/order?cluster=all&type=2&id=" + shipId;
        return GetAndParseAsArray(url, OrderOrShipInfo, null, e => {
            console.warn("Fail to query ship for ", shipId);
        });
    }

    public static async queryEagleEyeToTrace(eagleeyeid: string): Promise<Array<SimpleRequestIdAndRpcId>> {
        let url = CURR_API.monitor + "/proxy/eagleeyeId2traceId?eagleeyeId=" + eagleeyeid;
        return GetAndParseAsArray(url, SimpleRequestIdAndRpcId, null, e => {
            console.warn("Fail to query order for ", eagleeyeid);
        });
    }
}
