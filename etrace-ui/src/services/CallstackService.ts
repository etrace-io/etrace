import {Callstack, FullSql} from "../models/CallstackModel";
import {Get, GetAndParse, GetAndParseAsArray, Post} from "../utils/api";
import {isEmpty, MapToObject} from "../utils/Util";
import * as notification from "../utils/notification";
import {duration} from "../utils/notification";
import {CURR_API} from "$constants/API";

export class CallstackService {

    public static async queryRequestId(requestId: string, cluster?: string, errorHandler?: any): Promise<Callstack> {
        // 修复 "^" 这个字符导致后端api无法处理的问题
        requestId = requestId.replace("^", "%5E");
        let url = CURR_API.monitor + "/proxy/callstack?messageId=" + requestId;
        if (!isEmpty(cluster)) {
            url += "&cluster=" + cluster;
        }
        return GetAndParse(url, Callstack, null, errorHandler);
    }

    public static loadFullSql(sqlId: string, dalGroup: string, errorHandler?: any): Promise<Array<FullSql>> {
        let url = CURR_API.monitor + "/api/legacy/transfer/dalSql?sqlId=" + sqlId;
        return GetAndParseAsArray(url, FullSql, null, errorHandler);
    }

    public static loadConsumers(msgId: string): Promise<any> {
        let url = CURR_API.monitor + "/proxy/queue?msgId=" + msgId;
        let message = "获取Consumer";
        try {
            let resp: any = Get(url);
            return resp;
        } catch (err) {
            notification.errorHandler({message: message, description: err.message, duration: duration});
        }
    }

    public static transferRedis(redisIp: Array<string>) {
        let url = CURR_API.monitor + "/api/legacy/transfer/redis";
        const data = {type: "redis", param: redisIp};
        return Post(url, data);
    }

    public static sampling(metricName: string, tags: Map<string, string>, metricType: string,
                           timestamp: number, interval: number) {
        let url = CURR_API.monitor + "/proxy/sampling?cluster=all";
        const data = {
            metricName: metricName,
            timestamp: timestamp,
            metricType: metricType,
            tags: MapToObject(tags),
            interval: interval
        };
        return Post(url, data);
    }
}
