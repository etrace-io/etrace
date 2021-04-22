import {action, observable, reaction, runInAction, toJS} from "mobx";
import {
    Callstack,
    KnownMessageName,
    KnownMessageType,
    MessageClassType,
    RequestIdAndTime,
    Sampling,
    Transaction
} from "$models/CallstackModel";
import {CallstackService} from "$services/CallstackService";
import {get} from "lodash";
import {isEmpty, TargetKit} from "../utils/Util";
import {ChartEventStore} from "./ChartEventStore";
import {TagFilter} from "$models/ChartModel";
import {ConvertFunctionEnum} from "$utils/ConvertFunction";

export class CallstackStore {
    chartEventStore: ChartEventStore;
    @observable public selectedRequestId: string;
    @observable public samplings: Array<Sampling> = [];
    @observable public samplingsAsRequestIdAndTime: Array<RequestIdAndTime> = [];
    @observable public isStartToQuerySampling: boolean = false;
    @observable public showConsumers: boolean = false;
    @observable public samplingData: { metricName: string, timestamp: number, tags: Map<string, string>, interval: number } = null;
    @observable public metricType: string;
    @observable public consumers: Array<any> = [];
    @observable public consumersLoading: boolean = false;
    @observable public consumersEmptyText: string = "NoData";
    public callstackShowHead: boolean = true;
    public isThreadDump: boolean = false;
    public threadDumpData: string;

    @observable public callstack: Callstack;
    @observable private cantFindSampling: boolean = false;
    @observable private cantFindCallstack: boolean = false;

    constructor(chartEventStore: ChartEventStore) {
        this.chartEventStore = chartEventStore;
        reaction(
            // add null check to pass jest test.
            () => this.chartEventStore.seriesClickEvent,
            event => {
                const {metric, index} = event;
                if (!metric) { return; }
                // 以下 `?? get` 为兼容老版 CanvasChart
                const metricType = metric.metricType ?? get(metric, "type", null);
                // const idx = get(event, "index", -1);
                // const metricType = metric.;
                // const metricType = get(metric, "type", null);

                if (metricType) {
                    const tags = metric.tags || {};
                    const tagFilters = metric.tagFilters || [];
                    const mergesTags = new Map();

                    tagFilters.forEach((tagFilter: TagFilter) => {
                        if (!isEmpty(tagFilter.value) && tagFilter.op === "=") {
                            mergesTags.set(tagFilter.key, tagFilter.value);
                        }
                    });

                    Object.keys(tags).forEach(key => {
                        mergesTags.set(key, tags[key]);
                    });

                    const metricName = metric.results?.measurementName ?? get(metric, "fullName", null);
                    const timeShift = TargetKit.resolveTimeShit(
                        TargetKit.getValueFromFunctions(metric.functions, ConvertFunctionEnum.TIME_SHIFT)
                    ) ?? 0;
                    const interval = metric.results?.interval ?? get(metric, "interval", null);

                    console.warn(
                        metricName, ", tags from series: ", toJS(metric.tags), ", tag from filter: ", toJS(tags),
                        ", metric.tagFilters: ", toJS(metric.tagFilters), ", mergesTags: ", toJS(mergesTags),
                        ", ts: ", index,
                        ", metric type: ", metricType,
                        ", interval: ", interval,
                        "time shift: " + timeShift);

                    this.metricType = metricType;
                    this.startToQuerySampling();
                    const timestamp = get(event, "timestamp", 0) + timeShift;
                    CallstackService.sampling(metricName, mergesTags, metricType, timestamp, interval).then(xx => {
                        if (!isEmpty(xx) && xx.data && xx.data.length > 0) {
                            this.setSamplingsAndLoadFirstRequestId(xx.data);
                        } else {
                            this.setCantFindSampling({
                                metricName: metricName,
                                timestamp: timestamp,
                                tags: mergesTags,
                                interval,
                            });
                        }
                    }).catch(error => {
                        this.setCantFindSampling({
                            metricName: metricName,
                            timestamp: timestamp,
                            tags: mergesTags,
                            interval,
                        });
                    });
                }
            });
    }

    @action
    public clearSamplings() {
        this.samplings = [];
        this.samplingsAsRequestIdAndTime = [];
        this.isStartToQuerySampling = false;
        this.cantFindCallstack = false;
        this.clearCallstack();
    }

    @action
    public clearConsumers() {
        this.consumers = [];
        this.showConsumers = false;
    }

    @action
    public clearCallstack() {
        this.callstack = null;
        this.cantFindSampling = false;
        this.cantFindCallstack = false;
        this.samplingData = null;
        this.callstackShowHead = true;
    }

    public getCantFindSampling() {
        return this.cantFindSampling;
    }

    public getConsumers() {
        return toJS(this.consumers);
    }

    public getCantFindCallstack() {
        return this.cantFindCallstack;
    }

    public setCantFindCallstack() {
        this.cantFindCallstack = true;
    }

    @action
    public setCantFindSampling(samplingData: { metricName: string, timestamp: number, tags: Map<string, string>, interval: number }) {
        this.cantFindSampling = true;
        this.samplingData = samplingData;
    }

    public getCallstack() {
        return this.callstack;
    }

    public setCallstack(cs: Callstack) {
        this.callstack = cs;
    }

    public startToQuerySampling() {
        this.isStartToQuerySampling = true;
    }

    public loadConsumers(msgId: string) {
        this.consumersLoading = true;
        CallstackService.loadConsumers(msgId).then(data => {
            if (data) {
                runInAction(() => {
                    this.consumersLoading = false;
                    this.consumers = data.data;
                });
            }
        }).catch(err => {
            this.consumersLoading = false;
            this.consumersEmptyText = err.response.data.toString();
        });
    }

    @action
    public loadCallstack(requestId: string, idc?: string) {
        this.cantFindCallstack = false;
        this.callstack = null;
        this.selectedRequestId = requestId;
        CallstackService.queryRequestId(requestId, idc, err => {
            this.cantFindCallstack = true;
        }).then(data => {
            if (data) {
                runInAction(() => {
                    this.cantFindCallstack = false;
                    this.callstack = data;
                    this.processThreadDump();
                });
            }
        }).catch(e => {
            this.cantFindCallstack = true;
            alert(e);
        });
    }

    public processThreadDump() {
        let message: Transaction = this.callstack.message as Transaction;
        if (message._type == MessageClassType.TRANSACTION && message.type == KnownMessageType.System && (message.name == KnownMessageName.Status || message.name == KnownMessageName.ThreadDump)) {
            let child = message.children[0];
            if (child._type == MessageClassType.HEARTBEAT && (child.type == KnownMessageType.Heartbeat || child.type == KnownMessageType.threaddump)) {
                this.isThreadDump = true;
                if (child.data) {
                    this.threadDumpData = child.data;
                }
            }
        }
    }

    public getSamplings(): Array<Sampling> {
        return this.samplings;
    }

    public setSamplingsAndLoadFirstRequestId(samplings: Array<Sampling>) {
        this.samplings = samplings.filter(value => value.value != "null");
        if (this.samplings != null && this.samplings instanceof Array) {
            this.samplingsAsRequestIdAndTime = this.samplings.filter(sampling => sampling != null).map(sampling => {
                if (sampling.value) {
                    return new RequestIdAndTime(sampling.value, 0, sampling.idc);
                } else {
                    return new RequestIdAndTime(sampling.max, sampling.maxValue, sampling.idc);
                }
            }).sort((a, b) => b.time - a.time);
            if (this.samplingsAsRequestIdAndTime.length) {
                this.selectedRequestId = this.samplingsAsRequestIdAndTime[0].requestId;
                this.loadCallstack(this.selectedRequestId, this.samplingsAsRequestIdAndTime[0].idc);
            } else {
                this.samplingsAsRequestIdAndTime = [];
            }
        } else {
            this.samplingsAsRequestIdAndTime = [];
        }
    }
}
