import {Delete, GetAndParseAsArray, Post, Put} from "$utils/api";
import {JsonObject, JsonProperty} from "json2typescript";
import {CURR_API} from "$constants/API";

@JsonObject("ProxyConfig")
export class ProxyConfig {
    @JsonProperty("id", Number, true)
    id: number = undefined;
    @JsonProperty("updatedAt", Number, true)
    updatedAt: number = undefined;
    @JsonProperty("createdAt", Number, true)
    createdAt: number = undefined;
    @JsonProperty()
    serverName: string = undefined;
    @JsonProperty()
    proxyPath: string = undefined;
    @JsonProperty()
    path: string = undefined;
    @JsonProperty()
    clusters: string = undefined;
    @JsonProperty("rule", String, true)
    rule: string = undefined;
    @JsonProperty()
    status: string = undefined;
    @JsonProperty()
    type: number = undefined;
}

export class ProxyService {

    public static save(pc: ProxyConfig) {
        let url = CURR_API.monitor + "/proxyConfig";
        return Post(url, pc);
    }

    public static update(pc: ProxyConfig) {
        let url = CURR_API.monitor + "/proxyConfig";
        return Put(url, pc);
    }

    public static delete(id: number) {
        let url = CURR_API.monitor + "/proxyConfig/" + id;
        return Delete(url, {status: "Inactive"});
    }

    public static loadAll(): Promise<ProxyConfig[]> {
        let url = CURR_API.monitor + "/proxyConfig";
        return GetAndParseAsArray(url, ProxyConfig, null);
    }
}
