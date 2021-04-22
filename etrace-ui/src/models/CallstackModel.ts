import {JsonObject, JsonProperty} from "json2typescript";

@JsonObject("Callstack")
export class Callstack {
    @JsonProperty()
    requestId: string = undefined;
    @JsonProperty()
    id: string = undefined;
    @JsonProperty()
    message: Message = undefined;
    @JsonProperty()
    appId: string = undefined;
    @JsonProperty()
    hostIp: string = undefined;
    @JsonProperty()
    hostName: string = undefined;
    @JsonProperty()
    instance: string = undefined;
    @JsonProperty()
    cluster: string = undefined;
    @JsonProperty()
    ezone: string = undefined;
    @JsonProperty()
    idc: string = undefined;
    mesosTaskId: string = undefined;
    @JsonProperty()
    eleapposLabel: string = undefined;
    @JsonProperty()
    eleapposSlaveFqdn: string = undefined;
}

export enum MessageClassType {
    TRANSACTION = "transaction",
    EVENT = "event",
    HEARTBEAT = "heartbeat",
}

export enum KnownMessageType {
    SOAService = "SOAService",
    SOACall = "SOACall",
    DAL = "DAL",
    SQL = "SQL",
    Redis = "Redis",
    URL = "URL",
    ETraceLink = "ETraceLink",
    threaddump = "thread-dump",
    Heartbeat = "Heartbeat",
    System = "System",
    RMQ_PRODUCE = "RMQ_PRODUCE",
    RMQ_CONSUME = "RMQ_CONSUME",

    // statsd打点的EVENT的前缀都是"metric-"，如 metric-count, metric-timer
    Statsd_Metric_Prefix = "metric-",

    // 也需忽略type=log, name=error的event
    Log = "log",
    Error = "error",
}

export enum KnownMessageName {
    Status = "Status",
    Heartbeat = "Heartbeat",
    ThreadDump = "Thread-Dump",
}

@JsonObject("Message")
export class Message {
    @JsonProperty()
        // Transaction, event
    _type: MessageClassType;
    @JsonProperty()
        // not used id;
    id: number;
    @JsonProperty()
    type: string;
    @JsonProperty()
    tags?: any;  // like `map`, but is `object`
    @JsonProperty()
    name: string;
    @JsonProperty()
    status: string;
    @JsonProperty()
    timestamp: number;

    // rpcId
    data?: string;

    // reference to its Callstack. used in table.
    callstackRef: Callstack;
    // expand or collapse
    isOpen: boolean = null;
    // fail to drill up or drill down
    isError: boolean = false;
    // is drilling or not
    isLoading?: boolean = false;

    // 决定是否显示  host等详细信息
    shouldShowDetailInfo: boolean = false;

    public static isStatusSuccess(message: Message): boolean {
        return message.status === "0";
    }
}

export class Transaction extends Message {
    duration: number;
    children: Array<Message>;
    complete: boolean;
    // public isStatusSuccess(): boolean {
    //     return this.status == "0";
    // }
}

export class Event extends Message {
    // // rpcId
    // data: string;

}

export class Heartbeat extends Message {

}

@JsonObject("FullSql")
export class FullSql {
    @JsonProperty("full_sql", String)
    fullSql: string = undefined;
    @JsonProperty("group_name", String)
    groupName: string = undefined;
    @JsonProperty("sql_id", String)
    sqlId: string = undefined;
}

export class Sampling {
    value?: string;
    maxValue?: number;
    max?: string;
    idc?: string;
}

export class RequestIdAndTime {
    requestId: string;
    time: number;
    idc: string;

    constructor(requestId: string, time: number, idc: string) {
        this.requestId = requestId;
        this.time = time;
        this.idc = idc;
    }
}