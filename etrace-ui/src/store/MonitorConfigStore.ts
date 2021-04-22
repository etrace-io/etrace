import {action, observable, reaction, toJS} from "mobx";
import {
    AlertChannels,
    HBaseConfig,
    HBaseNoticeConfig,
    HealthConfig,
    HealthNoticeConfig,
    KafkaConfig,
    KafkaNoticeConfig,
    LinDbConfig,
    LinDbNoticeConfig,
    MonitorAlertInfoModel,
    MonitorConfigModel,
    NormalConfig,
    NormalNoticeConfig,
    NoticeConfig,
    ZookeeperConfig,
    ZookeeperNoticeConfig
} from "$models/TraceMonitorModel";
import * as MonitorConfigService from "../services/MonitorConfigService";

const R = require("ramda");

export class MonitorConfigStore {
    @observable public configs: Array<MonitorConfigModel> = [];
    @observable public alertInfos: Array<MonitorAlertInfoModel> = [];
    @observable public alerts: Map<number, MonitorAlertInfoModel> = new Map<number, MonitorAlertInfoModel>();
    @observable public tab: string = "kafka";
    @observable public tabName: string = "Kafka监控";
    @observable public subTab: string = "监控配置";
    @observable public noticeConfig: NoticeConfig = new NoticeConfig();
    @observable public monitorConfigModel: MonitorConfigModel = new MonitorConfigModel();
    @observable public change: boolean = false;

    constructor() {
        this.getCommonConfig();
        this.getNoticeCommonConfig();
        reaction(
            () => this.change,
            xxx => {
                this.getAllConfigs();
                this.getAllAlertInfos();
            });
    }

    public init() {
        this.getCommonConfig();
        this.getNoticeCommonConfig();
    }

    public reset() {
        this.configs = [];
        this.alertInfos = [];
        this.alerts = new Map<number, MonitorAlertInfoModel>();
        this.tab = "kafka";
        this.tabName = "Kafka监控";
        this.subTab = "监控配置";
        this.noticeConfig = new NoticeConfig();
        this.monitorConfigModel = new MonitorConfigModel();
    }

    @action
    async getAllConfigs() {
        this.configs = [];
        MonitorConfigService.getAll().then((datas: Array<any>) => {
            if (datas) {
                let configs: Array<MonitorConfigModel> = [];
                datas.forEach(data => {
                    let config: MonitorConfigModel = new MonitorConfigModel();
                    config.id = data.id;
                    config.ezone = data.ezone;
                    config.type = data.type;
                    config.value = JSON.parse(data.value);
                    configs.push(config);
                });
                this.configs = configs;
            }

        });
    }

    @action
    setChange() {
        this.change = !this.change;
    }

    getDisplayTab(): string {
        return this.tab;
    }

    setDisplayTab(tab: string) {
        if (this.tab != tab) {
            this.tab = tab;
            if (tab == "kafka") {
                this.tabName = "Kafka监控";
            } else if (tab == "hbase") {
                this.tabName = "HBase监控";
            } else if (tab == "zookeeper") {
                this.tabName = "Zookeeper监控";
            } else if (tab == "health") {
                this.tabName = "健康监控";
            } else if (tab == "normal") {
                this.tabName = "通用指标监控";
            } else {
                this.tabName = "Lindb集群";
            }
            this.getCommonConfig();
            this.getNoticeCommonConfig();
        }
    }

    getSubTab(): string {
        return this.subTab;
    }

    setSubTab(subTab: string) {
        if (this.subTab != subTab) {
            this.subTab = subTab;
            this.getNoticeCommonConfig();
        }
    }

    @action
    async getAllAlertInfos() {
        this.alertInfos = [];
        this.alerts.clear();
        MonitorConfigService.getAlertInfo().then((datas: Array<MonitorAlertInfoModel>) => {
            if (datas) {
                let configs: Array<MonitorAlertInfoModel> = [];
                datas.forEach((data: MonitorAlertInfoModel) => {
                    configs.push(data);
                    this.alerts.set(data.id, data);
                });
                this.alertInfos = configs;
            }

        });
    }

    @action
    public mergeMonitorConfig(config: any) {
        let oldModel = this.monitorConfigModel;
        this.monitorConfigModel = R.mergeDeepRight(oldModel, config);
    }

    @action
    public mergeMonitorValueConfig(configKey: string, config: any) {
        let monitorConfigModel = toJS(this.monitorConfigModel);
        let value = monitorConfigModel.value;
        value[`${configKey}`] = config;
        this.monitorConfigModel = R.mergeDeepRight(this.monitorConfigModel, monitorConfigModel);
        this.monitorConfigModel.type = this.tab;
    }

    @action
    public mergeNoticeConfig(config: any) {
        let oldModel = this.noticeConfig;
        this.noticeConfig = R.mergeDeepRight(oldModel, config);
    }

    @action
    public mergeNoticeChannelConfig(config: boolean) {
        let channel = new AlertChannels();
        // let monitorChannels: Array<AlertChannels> = this.noticeConfig.monitorChannels;
        this.noticeConfig.monitorChannels.forEach(alertChannel => {
            channel.channelInfo = config;
            channel.id = alertChannel.id;
            channel.userId = alertChannel.userId;
            channel.monitorInfoId = alertChannel.monitorInfoId;
        });
        this.noticeConfig.monitorChannels = [];
        this.noticeConfig.monitorChannels.push(channel);
        let oldModel = this.noticeConfig;
        let change = {isChange: config};
        this.noticeConfig = R.mergeDeepRight(oldModel, change);
    }

    @action
    public mergeNoticeInfoConfig(configKey: string, config: any) {
        let noticeConfig = toJS(this.noticeConfig);
        let info = noticeConfig.info;
        info[`${configKey}`] = config;
        this.noticeConfig = R.mergeDeepRight(this.noticeConfig, noticeConfig);
        this.noticeConfig.monitorType = this.tab;
    }

    @action
    getCommonConfig(record?: MonitorConfigModel) {
        let common;
        // this.monitorConfigModel = new MonitorConfigModel();
        if (record) {
            this.monitorConfigModel.type = record.type;
            this.monitorConfigModel.ezone = record.ezone;
            this.monitorConfigModel.id = record.id;
        }
        switch (this.tab) {
            case "kafka":
                if (record) {
                    let kafkaConfig: KafkaConfig = record.value;
                    common = new KafkaConfig(kafkaConfig.host, kafkaConfig.zookeeper, kafkaConfig.port, kafkaConfig.jmxPort, kafkaConfig.cluster);
                } else {
                    common = new KafkaConfig(null, null, null, null, null);
                }
                break;
            case "hbase":
                if (record) {
                    let hbaseConfig: HBaseConfig = record.value;
                    common = new HBaseConfig(hbaseConfig.host, hbaseConfig.port);
                } else {
                    common = new HBaseConfig(null, null);
                }
                break;
            case "health":
                if (record) {
                    let healthConfig: HealthConfig = record.value;
                    common = new HealthConfig(healthConfig.path, healthConfig.name);
                } else {
                    common = new HealthConfig(null, null);
                }
                break;
            case "lindb":
                if (record) {
                    let linDbConfig: LinDbConfig = record.value;
                    common = new LinDbConfig(linDbConfig.servers, linDbConfig.cluster);
                } else {
                    common = new LinDbConfig(null, null);
                }
                break;
            case "normal":
                if (record) {
                    let normalConfig: NormalConfig = record.value;
                    common = new NormalConfig(normalConfig.path, normalConfig.name, normalConfig.port);
                } else {
                    common = new NormalConfig(null, null, null);
                }
                break;
            case "zookeeper":
                if (record) {
                    let zookeeperConfig: ZookeeperConfig = record.value;
                    common = new ZookeeperConfig(zookeeperConfig.host, zookeeperConfig.port, zookeeperConfig.cluster);
                } else {
                    common = new ZookeeperConfig(null, null, null);
                }
                break;
            default:
                if (record) {
                    let kafkaConfig: KafkaConfig = record.value;
                    common = new KafkaConfig(kafkaConfig.host, kafkaConfig.zookeeper, kafkaConfig.port, kafkaConfig.jmxPort, kafkaConfig.cluster);
                } else {
                    common = new KafkaConfig(null, null, null, null, null);
                }
                break;
        }
        this.monitorConfigModel.value = common;
    }

    @action
    getNoticeCommonConfig(record?: MonitorAlertInfoModel) {
        let common;
        // this.noticeConfig = new NoticeConfig();
        if (record) {
            this.noticeConfig.metricName = record.metricName;
            this.noticeConfig.monitorType = record.alertType;
            this.noticeConfig.monitorName = record.alertName;
            this.noticeConfig.id = record.id;
            this.noticeConfig.alertModelId = record.alertModelId;
            this.noticeConfig.monitorChannels = record.alertChannels;
        } else {
            this.noticeConfig = new NoticeConfig();
            this.noticeConfig.metricName = this.tabName;
            this.noticeConfig.monitorType = this.tab;
        }
        switch (this.tab) {
            case "kafka":
                if (record) {
                    let kafkaNoticeConfig: KafkaNoticeConfig = record.alertModel;
                    common = new KafkaNoticeConfig(kafkaNoticeConfig.group, kafkaNoticeConfig.topic, kafkaNoticeConfig.threshold, kafkaNoticeConfig.cluster);
                } else {
                    common = new KafkaNoticeConfig(null, null, null, null);
                }
                break;
            case "hbase":
                if (record) {
                    let hbaseNoticeConfig: HBaseNoticeConfig = record.alertModel;
                    common = new HBaseNoticeConfig(hbaseNoticeConfig.writeRequestCount);
                } else {
                    common = new HBaseNoticeConfig(null);
                }
                break;
            case "health":
                if (record) {
                    let healthNoticeConfig: HealthNoticeConfig = record.alertModel;
                    common = new HealthNoticeConfig(healthNoticeConfig.path);
                } else {
                    common = new HealthNoticeConfig(null);
                }
                break;
            case "lindb":
                if (record) {
                    let linDbNoticeConfig: LinDbNoticeConfig = record.alertModel;
                    common = new LinDbNoticeConfig(linDbNoticeConfig.ezone, linDbNoticeConfig.cluster, linDbNoticeConfig.servers, linDbNoticeConfig.database, linDbNoticeConfig.ql, linDbNoticeConfig.groupCount, linDbNoticeConfig.dynamicGroup, linDbNoticeConfig.fieldsThreshold, linDbNoticeConfig.trend, linDbNoticeConfig.trendValue);    // tslint:disable-line
                } else {
                    common = new LinDbNoticeConfig(null, null, null, null, null, null, null, null, null, null);
                }
                break;
            case "normal":
                if (record) {
                    let normalNoticeConfig: NormalNoticeConfig = record.alertModel;
                    common = new NormalNoticeConfig(normalNoticeConfig.path);
                } else {
                    common = new NormalNoticeConfig(null);
                }
                break;
            case "zookeeper":
                if (record) {
                    let zookeeperNoticeConfig: ZookeeperNoticeConfig = record.alertModel;
                    common = new ZookeeperNoticeConfig(zookeeperNoticeConfig.path);
                } else {
                    common = new ZookeeperNoticeConfig(null);
                }
                break;
            default:
                if (record) {
                    let kafkaNoticeConfig: KafkaNoticeConfig = record.alertModel;
                    common = new KafkaNoticeConfig(kafkaNoticeConfig.group, kafkaNoticeConfig.topic, kafkaNoticeConfig.threshold, kafkaNoticeConfig.cluster);
                } else {
                    common = new KafkaNoticeConfig(null, null, null, null);
                }
                break;
        }
        this.noticeConfig.info = common;
    }
}