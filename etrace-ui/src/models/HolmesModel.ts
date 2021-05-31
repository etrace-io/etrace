import {JsonObject, JsonProperty} from "json2typescript";

export enum EventType {
    MACHINE = "MACHINE",
    ESM = "ESM"
}

export enum BusinessDomainType {
    NORMAL = "NORMAL",
    ORDER = "ORDER",
    MIDDLE = "MIDDLE",
}

export enum AppType {
    APP = "APP",
    DAL = "DAL",
    QUEUE = "QUEUE",
    URL = "URL"
}

export class EventStatus {
    status: EventStatusEnum;
}

export enum EventStatusEnum {
    Init = "init",
    Loading = "loading",
    Loaded = "loaded"
}

export enum AlertOrChange {
    ALERT, CHANGE
}

/**
 * 从lindb查出来的 alert/change 信息
 */
export class AlertOrChangeEvent {
    appId: string = undefined;
    timestamp: number = undefined;
    interval: number = undefined;
    _type: AlertOrChange;
}

@JsonObject("AlertEventVO")
export class AlertEventVO {
    @JsonProperty()
    eventType: EventType = undefined;
    @JsonProperty()
    appIds: Array<string> = undefined;
    @JsonProperty()
    timestamp: number = undefined;
    @JsonProperty("metricTimestamp", Number, true)
    metricTimestamp: number = undefined;

    @JsonProperty("department", String, true)
    department: string = undefined;

    @JsonProperty("parentDepartment", String, true)
    parentDepartment: string = undefined;

    @JsonProperty()
    payload: string = undefined;
}

@JsonObject("ChangeEventVO")
export class ChangeEventVO {
    @JsonProperty()
    source: string = undefined;
    @JsonProperty()
    appIds: Array<string> = undefined;
    @JsonProperty()
    timestamp: number = undefined;
    @JsonProperty("metricTimestamp", Number, true)
    metricTimestamp: number = undefined;
    @JsonProperty()
    content: string = undefined;
    @JsonProperty()
    description: string = undefined;
    @JsonProperty()
    isKeyPath: boolean = undefined;
    @JsonProperty()
    severity: boolean = undefined;
    @JsonProperty()
    operator: string = undefined;

    @JsonProperty("department", String, true)
    department: string = undefined;

    @JsonProperty("parentDepartment", String, true)
    parentDepartment: string = undefined;

}

export class RpcInfo {
    @JsonProperty()
    interface: string = undefined;
    @JsonProperty()
    operation: string = undefined;
    @JsonProperty()
    url: string = undefined;
    @JsonProperty()
    name: string = undefined;

    @JsonProperty()
    ezone: string = undefined;
    @JsonProperty()
    status: number = undefined;
    @JsonProperty()
    duration: string = undefined;
    @JsonProperty()
    timestamp: number;
    @JsonProperty()
    shardingkey: string = undefined;
    @JsonProperty()
    testCase: string = undefined;
}

// request Info
@JsonObject("RequestIdInfo")
export class RequestIdInfo {
    @JsonProperty()
    reqId: string = undefined;

    @JsonProperty()
    rpcId: string = undefined;

    @JsonProperty()
    hour: number = undefined;
    @JsonProperty()
    ip: string = undefined;

    @JsonProperty("rpcType", Object, true)
    rpcType: string = undefined;

    @JsonProperty()
    appId: string = undefined;

    @JsonProperty("rpcInfo", Object, true)
    rpcInfo: RpcInfo = undefined;
}
