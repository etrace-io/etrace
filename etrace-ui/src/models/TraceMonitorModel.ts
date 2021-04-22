export class MonitorConfigModel {
    ezone: string;
    id: number;
    type: string;
    value: CommonConfig;
}

export class MonitorConfigDO {
    ezone: string;
    id: number;
    type: string;
    value: string;
}

export class MonitorAlertInfoModel {
    id: number;
    isNotice: boolean;
    createdAt: number;
    metricName: string;
    alertType: string;
    alertName: string;
    alertModelString: string;
    alertModel: any;
    alertModelId: number;
    alertInfo: string;
    alertChannels: Array<MonitorAlertChannel>;
}

export enum MonitorType {
    Kafka, HBase, Zookeeper, Health, Normal, Lindb
}

export class MonitorAlertChannel {
    monitorInfoId: number;
    id: number;
    channelInfo: boolean;
    userId: number;
}

export declare type  CommonConfig =
    KafkaConfig
    | HBaseConfig
    | ZookeeperConfig
    | HealthConfig
    | NormalConfig
    | LinDbConfig;

export class KafkaConfig {
    host?: string;
    zookeeper?: string;
    port?: string;
    jmxPort?: string;
    cluster?: string;
    id?: number;

    constructor(host: string, zookeeper: string, port: string, jmxPort: string, cluster: string) {
        this.host = host;
        this.zookeeper = zookeeper;
        this.port = port;
        this.jmxPort = jmxPort;
        this.cluster = cluster;
    }
}

export class HBaseConfig {
    host?: string;
    port?: string;
    id?: number;

    constructor(host: string, port: string) {
        this.host = host;
        this.port = port;
    }
}

export class ZookeeperConfig {
    host?: string;
    port?: string;
    cluster?: string;
    id?: number;

    constructor(host: string, port: string, cluster: string) {
        this.host = host;
        this.port = port;
        this.cluster = cluster;
    }
}

export class HealthConfig {
    path?: string;
    name?: string;
    id?: number;

    constructor(path: string, name: string) {
        this.path = path;
        this.name = name;
    }
}

export class NormalConfig {
    path?: string;
    name?: string;
    port?: string;
    id?: number;

    constructor(path: string, name: string, port: string) {
        this.path = path;
        this.name = name;
        this.port = port;
    }
}

export class LinDbConfig {
    servers?: string;
    cluster?: string;
    id?: number;

    constructor(servers: string, cluster: string) {
        this.servers = servers;
        this.cluster = cluster;
    }
}

export class NoticeConfig {
    id: number;
    monitorType?: string;
    monitorName?: string;
    metricName?: string;
    isChange?: boolean;
    info?: NoticeCommonInfo;
    alertModelId?: number;
    monitorChannels?: Array<AlertChannels>;

    constructor() {
        let alertChannel = new AlertChannels();
        alertChannel.channelInfo = true;
        this.monitorChannels = [alertChannel];
    }
}

export class AlertChannels {
    id: number;
    channelInfo: boolean;
    monitorInfoId: number;
    userId: number;
}

export class NoticeConfigDO {
    id: number;
    monitorType?: string;
    monitorName?: string;
    metricName?: string;
    info?: string;
    monitorChannels?: string;
    alertModelId?: number;
}

export declare type  NoticeCommonInfo =
    LinDbNoticeConfig
    | HBaseNoticeConfig
    | HealthNoticeConfig
    | ZookeeperNoticeConfig
    | NormalNoticeConfig
    | KafkaNoticeConfig;

export class LinDbNoticeConfig {
    path?: string;
    ezone?: string;
    cluster?: string;
    servers?: string;
    database?: string;
    ql?: string;
    groupCount?: string;
    dynamicGroup?: string;
    fieldsThreshold?: FieldsThreshold;
    trend?: string;
    trendValue?: string;

    constructor(ezone: string, cluster: string, servers: string, database: string, ql: string,
                groupCount: string, dynamicGroup: string, fieldsThreshold: FieldsThreshold, trend: string, trendValue: string) {
        this.ezone = ezone;
        this.cluster = cluster;
        this.servers = servers;
        this.database = database;
        this.ql = ql;
        this.groupCount = groupCount;
        this.dynamicGroup = dynamicGroup;
        this.fieldsThreshold = fieldsThreshold;
        this.trend = trend;
        this.trendValue = trendValue;
    }
}

export class FieldsThreshold {
    value: ThresholdType;
}

export class ThresholdType {
    LT: number;
    GT: number;
}

export class HBaseNoticeConfig {
    path?: string;
    writeRequestCount?: number;

    constructor(writeRequestCount: number) {
        this.writeRequestCount = writeRequestCount;
    }
}

export class HealthNoticeConfig {
    path?: string;

    constructor(path: string) {
        this.path = path;
    }
}

export class ZookeeperNoticeConfig {
    path?: string;

    constructor(path: string) {
        this.path = path;
    }
}

export class NormalNoticeConfig {
    path?: string;

    constructor(path: string) {
        this.path = path;
    }
}

export class KafkaNoticeConfig {
    path?: string;
    group?: string;
    topic?: string;
    threshold?: string;
    cluster?: string;

    constructor(group: string, topic: string, threshold: string, cluster: string) {
        this.group = group;
        this.topic = topic;
        this.threshold = threshold;
        this.cluster = cluster;
    }
}