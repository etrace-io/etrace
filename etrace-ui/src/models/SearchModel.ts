import {JsonObject, JsonProperty} from "json2typescript";

@JsonObject("OrderOrShipInfo")
export class OrderOrShipInfo {
    @JsonProperty("appId")
    appId: string = undefined;
    @JsonProperty("critical")
    critical: boolean = undefined;

    @JsonProperty("duration")
    duration: number = undefined;

    @JsonProperty("ezone")
    ezone: string = undefined;

    @JsonProperty("org")
    org: string = undefined;

    @JsonProperty("process")
    process: string = undefined;

    @JsonProperty("processDesc")
    processDesc: string = undefined;

    @JsonProperty("timestamp")
    timestamp: number = undefined;

    @JsonProperty("status")
    status: string = undefined;

    @JsonProperty("sampling")
    sampling: string = undefined;
}

@JsonObject("SimpleRequestIdAndRpcId")
export class SimpleRequestIdAndRpcId {
    @JsonProperty("requestId")
    requestId: string = undefined;
    @JsonProperty("rpcId")
    rpcId: boolean = undefined;
}
