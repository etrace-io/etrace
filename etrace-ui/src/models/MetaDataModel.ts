import {JsonObject, JsonProperty} from "json2typescript";

@JsonObject("AppDependency")
export class AppDependency {
    @JsonProperty()
    asConsumer: Array<DependencyConsumer>;
    @JsonProperty()
    asProvider: Array<DependencyProvider>;
}

@JsonObject("DependencyConsumer")
export class DependencyConsumer {
    @JsonProperty()
    appId: string;
    @JsonProperty()
    method: string;
}

@JsonObject("DependencyProvider")
export class DependencyProvider {
    @JsonProperty()
    appId: string;
    @JsonProperty()
    method: string;
}

@JsonObject("AppFramework")
export class AppFramework {
    @JsonProperty()
    framework: string;
    @JsonProperty()
    version: string;
    @JsonProperty()
    timestamp: string;
}

@JsonObject("RmqProduce")
export class RmqProduce {
    @JsonProperty("ttl")
    ttl: number = undefined;
    @JsonProperty("id")
    id: number = undefined;
    @JsonProperty("server")
    server: string = undefined;
    @JsonProperty("routing")
    routing: string = undefined;
    @JsonProperty("vhost")
    vhost: string = undefined;
    @JsonProperty("exchange")
    exchange: string = undefined;
    @JsonProperty("queue")
    queue: string = undefined;
}

@JsonObject("RmqConsume")
export class RmqConsume {
    @JsonProperty("ttl")
    ttl: number = undefined;
    @JsonProperty("id")
    id: number = undefined;
    @JsonProperty("server")
    server: string = undefined;
    @JsonProperty("routing")
    routing: string = undefined;
    @JsonProperty("vhost")
    vhost: string = undefined;
    @JsonProperty("exchange")
    exchange: string = undefined;
    @JsonProperty("queue")
    queue: string = undefined;
}

@JsonObject("SoaCall")
export class SoaCall {
    @JsonProperty("ttl")
    ttl: number = undefined;
    @JsonProperty("id")
    id: number = undefined;
    @JsonProperty("method")
    method: string = undefined;
    @JsonProperty("provider")
    provider: string = undefined;
}

@JsonObject("SoaServe")
export class SoaServe {
    @JsonProperty("ttl")
    ttl: number = undefined;
    @JsonProperty("id")
    id: number = undefined;
    @JsonProperty("method")
    method: string = undefined;
    @JsonProperty("provider")
    provider: string = undefined;
}

@JsonObject("CorvusCache")
export class CorvusCache {
    @JsonProperty("ttl")
    ttl: number = undefined;
    @JsonProperty("id")
    id: number = undefined;
    @JsonProperty("port")
    port: string = undefined;
    @JsonProperty("name")
    name: string = undefined;
}

@JsonObject("DalGroupDal")
export class DalGroupDal {
    @JsonProperty("ttl")
    ttl: number = undefined;
    @JsonProperty("id")
    id: number = undefined;
    @JsonProperty("name")
    name: string = undefined;
}

@JsonObject("MachineRunOn")
export class MachineRunOn {
    @JsonProperty("ttl")
    ttl: number = undefined;
    @JsonProperty("id")
    id: number = undefined;
    @JsonProperty("hostName")
    hostName: string = undefined;
    @JsonProperty("cluster")
    cluster: string = undefined;
    @JsonProperty("hostIp")
    hostIp: string = undefined;
    @JsonProperty("idc")
    idc: string = undefined;
    @JsonProperty("ezone")
    ezone: string = undefined;
}

// @ts-ignore
@JsonObject("AppInfo")
export class AppInfo {
    @JsonProperty("Rmq,PRODUCE", [RmqProduce], true)
    rmqProduce?: Array<RmqProduce> = undefined;

    @JsonProperty("Rmq,CONSUME", [RmqConsume], true)
    rmqConsume?: Array<RmqConsume> = undefined;

    @JsonProperty("Soa,CALL", [SoaCall], true)
    soaCall?: Array<SoaCall> = undefined;

    @JsonProperty("Soa,SERVE", [SoaServe], true)
    soaServe?: Array<SoaServe> = undefined;

    @JsonProperty("Corvus,CACHE", [CorvusCache], true)
    corvusCache?: Array<CorvusCache> = undefined;

    @JsonProperty("DalGroup,DAL", [DalGroupDal], true)
    dalGroupDal?: Array<DalGroupDal> = undefined;

    @JsonProperty("Machine,RUN_ON", [MachineRunOn], true)
    machineRunOn?: Array<MachineRunOn> = undefined;
}

export class SoaListInfo {
    ttl?: number;
    id?: number;
    method?: string;
    provider?: string;
}