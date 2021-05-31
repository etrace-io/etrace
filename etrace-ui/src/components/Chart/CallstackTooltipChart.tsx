import {get} from "lodash";
import React from "react";
import {observable} from "mobx";
import {observer} from "mobx-react";
import {autobind} from "core-decorators";
import ChartCard from "../../containers/Trace/ChartCard";
import * as ChartService from "../../services/ChartService";
import {Callstack, KnownMessageType, Message, MessageClassType} from "$models/CallstackModel";
import {Popover, Row} from "antd";
import {TIME_FORMAT} from "$models/TimePickerModel";
import {TagFilter} from "$models/ChartModel";
import moment from "moment";
import {AreaChartOutlined} from "@ant-design/icons/lib";
import {
    SOA_CONSUMER_AVG_GLOBAL_ID,
    SOA_CONSUMER_STATUS_GLOBAL_ID,
    SOA_PROVIDER_AVG_GLOBAL_ID,
    SOA_PROVIDER_RATE_GLOBAL_ID,
    SOA_PROVIDER_STATUS_GLOBAL_ID
} from "$constants/index";

interface CallstackTooltipChartProps {
    message?: Message;
    callstack?: Callstack;
    showType: string;
}

interface CallstackTooltipChartState {
}

class ChartResult {
    chart: any;
    globalId: string;
    uniqueId: string;
}

class GlobalIdAndExtraTags {
    globalId: string;
    prefix: string;
    extraTags: Map<string, Array<string>>;

    constructor(globalId: string, prefix?: string, extraTags?: Map<string, Array<string>>) {
        this.globalId = globalId;
        this.prefix = prefix;
        if (extraTags == null) {
            this.extraTags = new Map();
        } else {
            this.extraTags = extraTags;
        }
    }
}

@observer
export default class CallstackTooltipChart extends React.Component<CallstackTooltipChartProps, CallstackTooltipChartState> {

    public static SHOW_TRANSACTION_INFO = "show_transaction_info";
    public static SHOW_MACHINE_INFO = "show_machine_info";

    public static globalIdCache: Map<string, any> = new Map();

    disposer;
    userStore;

    @observable results: Array<ChartResult> = [];
    @observable loading: boolean = true;

    constructor(props: CallstackTooltipChartProps) {
        super(props);
        this.state = {};
    }

    componentDidMount() {
        let globalIdAndExtraTagsArray: Array<GlobalIdAndExtraTags> = null;
        switch (this.props.showType) {
            case CallstackTooltipChart.SHOW_TRANSACTION_INFO: {
                globalIdAndExtraTagsArray = this.findGlobalIdForTransaction(this.props.message);
                break;
            }
            case CallstackTooltipChart.SHOW_MACHINE_INFO: {
                globalIdAndExtraTagsArray = this.findGlobalIdForMachine(this.props.callstack);
                break;
            }
            default:
        }

        if (globalIdAndExtraTagsArray != null) {
            let timestamp: number = this.props.callstack ? this.props.callstack.message.timestamp : this.props.message.timestamp;
            this.loadChartConfig(globalIdAndExtraTagsArray, timestamp);
        }
    }

    loadChartConfig(globalIdAndExtraTagsArray: Array<GlobalIdAndExtraTags>, timestamp: number) {
        globalIdAndExtraTagsArray.forEach(globalIdAndExtraTags => {
            let globalId = globalIdAndExtraTags.globalId;
            if (CallstackTooltipChart.globalIdCache.has(globalId)) {
                this.results.push({
                    chart: this.handleResult(
                        CallstackTooltipChart.globalIdCache.get(globalId),
                        globalIdAndExtraTags.prefix, globalIdAndExtraTags.extraTags, timestamp),
                    globalId: globalId,
                    uniqueId: this.buildUniqueId()
                });
            } else {
                ChartService
                    .searchByGroup({
                        status: "Active",
                        globalId: globalIdAndExtraTags.globalId,
                    })
                    .then((charts: any) => {
                        let rawChart = get(charts, "results", []).length > 0
                            ? charts.results.filter(chart => chart.globalId === globalId)[0]
                            : null;

                        CallstackTooltipChart.globalIdCache.set(globalId, rawChart);
                        let chartResult = this.handleResult(rawChart, globalIdAndExtraTags.prefix, globalIdAndExtraTags.extraTags, timestamp);

                        this.results.push({
                            chart: chartResult,
                            globalId: globalId,
                            uniqueId: this.buildUniqueId()
                        });

                        this.loading = false;
                    });
            }
        });
    }

    render() {
        return (
            <Popover content={this.buildChartCards(this.results)} arrowPointAtCenter={true} mouseEnterDelay={0.5}>
                <AreaChartOutlined style={{padding: "0 3px", verticalAlign: "middle", marginLeft: 5, cursor: "pointer"}}/>
            </Popover>
        );
    }

    @autobind
    private buildChartCards(results: Array<ChartResult>) {
        const charts = results.filter(result => result.chart != null);

        const charCards = charts.map(
            (result, index) => {
                return (
                    <ChartCard
                        span={(charts.length === 3 && index === 2) ? 24 : 12}
                        style={{padding: 2}}
                        chart={result.chart}
                        key={result.uniqueId + index}
                        uniqueId={result.uniqueId + index}
                        awaitLoad={false}
                    />
                );
            });

        return (
            <Row style={{width: charts.length > 1 ? 900 : 450}}>
                {charCards}
            </Row>
        );
    }

    private findGlobalIdForMachine(callstack: Callstack): Array<GlobalIdAndExtraTags> {
        let tags: Map<string, Array<string>> = new Map();
        tags.set("host", [callstack.hostName]);
        return [
            new GlobalIdAndExtraTags("system_pre_host_overview_cpu_usage", null, tags),
            new GlobalIdAndExtraTags("system_pre_host_overview_memory_usage", null, tags),
            new GlobalIdAndExtraTags("system_pre_host_overview_net.io_usage", null, tags),
            new GlobalIdAndExtraTags("system_pre_host_overview_disk.io_usage", null, tags)];
    }

    private findGlobalIdForTransaction(message: Message): Array<GlobalIdAndExtraTags> {
        let results: Array<GlobalIdAndExtraTags> = null;

        let tags: Map<string, Array<string>> = new Map();
        let appId: string = message.callstackRef.appId;
        if (message._type == MessageClassType.TRANSACTION) {
            switch (message.type) {
                case KnownMessageType.SOAService: {
                    tags.set("method", [message.name]);
                    results = [
                        new GlobalIdAndExtraTags(SOA_PROVIDER_STATUS_GLOBAL_ID, appId, tags),
                        new GlobalIdAndExtraTags(SOA_PROVIDER_AVG_GLOBAL_ID, appId, tags),
                        new GlobalIdAndExtraTags(SOA_PROVIDER_RATE_GLOBAL_ID, appId, tags),
                    ];
                    break;
                }
                case KnownMessageType.SOACall: {
                    tags.set("method", [message.name]);
                    results = [
                        new GlobalIdAndExtraTags(SOA_CONSUMER_STATUS_GLOBAL_ID, appId, tags),
                        new GlobalIdAndExtraTags(SOA_CONSUMER_AVG_GLOBAL_ID, appId, tags),
                    ];
                    break;
                }
                case KnownMessageType.DAL: {
                    if (message.tags) {
                        let dalGroup = message.tags.group;
                        let sqlId = message.tags.sqlId;
                        tags.set("sql", [sqlId]);
                        results = [
                            new GlobalIdAndExtraTags("dal_sql_count", dalGroup, tags),
                            new GlobalIdAndExtraTags("dal_sql_time", dalGroup, tags),
                        ];
                    }
                    break;
                }
                case KnownMessageType.RMQ_PRODUCE: {
                    if (message.tags) {
                        let routing = message.tags.routing;
                        let exchange = message.tags.exchange;
                        tags.set("exchange", [exchange]);
                        tags.set("routingkey", [routing]);
                        results = [
                            new GlobalIdAndExtraTags("application_rmq_publish_count", appId, tags),
                            new GlobalIdAndExtraTags("application_rmq_publish_time_avg", appId, tags)];
                    }
                    break;
                }
                case KnownMessageType.RMQ_CONSUME: {
                    if (message.tags) {
                        let routing = message.tags.routing;
                        let queue = message.tags.queue;
                        tags.set("queue", [queue]);
                        tags.set("routingkey", [routing]);
                        results = [
                            new GlobalIdAndExtraTags("application_consumer_count", appId, tags),
                            new GlobalIdAndExtraTags("application_consumer_time", appId, tags)
                        ];
                    }
                    break;
                }
                default:
            }
        }
        return results;
    }

    private buildUniqueId(): string {
        if (this.props.callstack) {
            let callstack = this.props.callstack ? this.props.callstack : this.props.message.callstackRef;
            return callstack.requestId + callstack.id + this.props.showType;
        } else {
            let callstack = this.props.message.callstackRef;
            return callstack.requestId + callstack.id + this.props.message.type + this.props.message.name;
        }
    }

    private handleResult(tempResult: any, prefix: string, extraTags: Map<string, Array<string>>, timestamp: number) {
        if (tempResult && Array.isArray(tempResult.targets)) {
            tempResult.targets.forEach(target => {
                // 禁用"点击采样"功能
                target.metricType = null;

                target.prefix = prefix;
                if (extraTags.size > 0) {
                    if (!target.tagFilters) {
                        target.tagFilters = [];
                    }

                    target.tagFilters = target.tagFilters.filter((tagFilter: TagFilter) => {
                        return !extraTags.has(tagFilter.key);
                    });

                    extraTags.forEach((v, k) => {
                        target.tagFilters.push(
                            {
                                op: "=",
                                key: k,
                                value: v
                            }
                        );
                    });
                }

                if (timestamp < moment().subtract(30, "minute").valueOf()) {
                    target.from = moment(timestamp).subtract(30, "minute").format(TIME_FORMAT);
                    target.to = moment(timestamp).add(30, "minute").format(TIME_FORMAT);
                }
            });
        }
        return tempResult;
    }
}
