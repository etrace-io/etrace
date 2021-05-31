import {AlertOrChange, AlertOrChangeEvent, EventStatus, EventStatusEnum} from "../models/HolmesModel";
import {action, observable, reaction} from "mobx";
import StoreManager from "./StoreManager";
import {isEmpty, ObjectToMap} from "$utils/Util";
import {TimePickerModel} from "$models/TimePickerModel";
import {UserStore} from "./UserStore";
import {get} from "lodash";
import {TagFilter, Target} from "$models/ChartModel";
import {searchMetricsSimple} from "$services/LinDBService";
import {ResultSetModel} from "$models/ResultSetModel";

export class EventStore {
    userStore: UserStore;
    changeEvents: Map<string, Array<AlertOrChangeEvent>> = new Map();
    alertEvents: Map<string, Array<AlertOrChangeEvent>> = new Map();
    // key => EventStatus
    eventsStatus: Map<string, EventStatus> = new Map();

    @observable public changed: boolean = false;
    @observable public changeEventDisplay: boolean;
    @observable public alertEventDisplay: boolean;
    @observable public allChangeEvents: Array<any> = [];
    @observable public allAlertEvents: Array<any> = [];

    @observable totalAlertCount = 0;
    @observable totalChangeCount = 0;

    constructor(userStore: UserStore) {
        this.userStore = userStore;
        reaction(
            () => get(this.userStore.user, "userConfig.config", null),
            xx => {
                this.changeEventDisplay = get(this.userStore.user, "userConfig.config.showPublish", false);
                this.alertEventDisplay = get(this.userStore.user, "userConfig.config.showAlert", false);
            });
    }

    @action
    public register(key: string, eventStatus: EventStatus) {
        if (this.eventsStatus.has(key)) {
            return;
        }
        this.changeEvents.delete(key);
        this.alertEvents.delete(key);
        this.eventsStatus.set(key, eventStatus);
    }

    @action
    public clearAll() {
        this.changeEvents.clear();
        this.alertEvents.clear();
        this.eventsStatus.clear();
        this.allAlertEvents = [];
        this.allChangeEvents = [];

        this.changeEventDisplay = get(this.userStore.user, "userConfig.config.showPublish", false);
        this.alertEventDisplay = get(this.userStore.user, "userConfig.config.showAlert", false);
    }

    @action
    public loadAll() {
        // 避免每个 chart都立刻执行该查询
        setTimeout(() => this.loadAll0(), 0);
    }

    public loadAll0() {
        let appIds: Array<string> = [];
        this.eventsStatus.forEach((v: EventStatus, key: string) => {
            if (v.status != EventStatusEnum.Loading) {
                appIds.push(key);
                v.status = EventStatusEnum.Loading;
            }
        });

        if (appIds.length > 0) {
            const selectedTime: TimePickerModel = StoreManager.urlParamStore.getSelectedTime();
            Promise.all([
                searchMetricsSimple(this.buildAlertTarget(appIds, selectedTime.fromString, selectedTime.toString)),
                searchMetricsSimple(this.buildChangeTarget(appIds, selectedTime.fromString, selectedTime.toString))
            ]).then((data: Array<Array<ResultSetModel>>) => {
                const alert = data[0][0];
                const change = data[1][0];

                let totalAlertCount = 0, totalChangeCount = 0;
                alert.results && alert.results.groups && alert.results.groups.forEach(group => {
                    if (group && group.group && group.fields) {
                        let alertEvents: Array<AlertOrChangeEvent> = [];
                        let start = alert.results.startTime;
                        let fields = ObjectToMap(group.fields).get("count");
                        let appId = ObjectToMap(group.group).get("appId");
                        fields.forEach(field => {
                            if (field) {
                                alertEvents.push({
                                    appId: appId,
                                    // 这个时间是arch.holmes收到数据，并再打点的时间。
                                    // 查询时用该时间查询。arch.holmes已做了会查询出实际时间事件的逻辑
                                    timestamp: start,
                                    interval: alert.results.interval,
                                    _type: AlertOrChange.ALERT
                                });
                                totalAlertCount += field;
                            }
                            start = start + alert.results.interval;
                        });
                        this.alertEvents.set(appId, alertEvents);
                    }
                });
                change.results && change.results.groups && change.results.groups.forEach(group => { if (group && group.group && group.fields) {
                        let changeEvents: Array<AlertOrChangeEvent> = [];
                        let start = alert.results.startTime;
                        let fields = ObjectToMap(group.fields).get("count");
                        let appId = ObjectToMap(group.group).get("appId");
                        fields.forEach(field => {
                            if (field) {
                                changeEvents.push({
                                    appId: appId,
                                    // 这个时间是arch.holmes收到数据，并再打点的时间。
                                    // 查询时用该时间查询。arch.holmes已做了会查询出实际时间事件的逻辑
                                    timestamp: start,
                                    interval: alert.results.interval,
                                    _type: AlertOrChange.CHANGE
                                });
                                totalChangeCount += field;
                            }
                            start = start + alert.results.interval;
                        });
                        this.changeEvents.set(appId, changeEvents);
                    }
                });
                this.getAllAlertEvents();
                this.getAllChangeEvents();
                this.totalAlertCount = totalAlertCount;
                this.totalChangeCount = totalChangeCount;

                this.changed = !this.changed;
                appIds.forEach(appId => {
                    const event = this.eventsStatus.get(appId);
                    if (event) {
                        event.status = EventStatusEnum.Loaded;
                    }
                });
            });
        }
    }

    @action
    public getAllChangeEvents() {
        const result = [];
        this.changeEvents.forEach((v: Array<AlertOrChangeEvent>, k) => {
            if (!isEmpty(v)) {
                v.map(item => result.push(item));
            }
        });
        this.allChangeEvents = result;
    }

    @action
    public getAllAlertEvents() {
        const result = [];
        this.alertEvents.forEach((v: Array<AlertOrChangeEvent>, k) => {
            if (!isEmpty(v)) {
                v.map(item => result.push(item));
            }
        });
        this.allAlertEvents = result;
    }

    public getChangeEvents(key: string): Array<AlertOrChangeEvent> {
        const result = this.changeEvents.get(key);
        if (isEmpty(result)) {
            return [];
        }
        return result;
    }

    public getAlertEvents(key: string): Array<AlertOrChangeEvent> {
        const result = this.alertEvents.get(key);
        if (isEmpty(result)) {
            return [];
        }
        return result;
    }

    private buildAlertTarget(appIds: Array<string>, from: string, to: string): Target {
        const filter: TagFilter = {
            key: "appId",
            op: "=",
            value: appIds
        };
        return {
            entity: "app_metric",
            measurement: "alert",
            prefix: "arch.etracealertconsole",
            groupBy: ["appId"],
            fields: ["count"],
            tagFilters: [filter],
            from: from,
            to: to,
            functions: [{
                modelEnum: "interval", name: "interval",
                params: [{name: "interval", type: "string", display: false, width: 41}],
                defaultParams: ["1m"]
            }],
        };
    }

    private buildChangeTarget(appIds: Array<string>, from: string, to: string): Target {
        const filter: TagFilter = {
            key: "appId",
            op: "=",
            value: appIds
        };
        return {
            entity: "app_metric",
            measurement: "change",
            prefix: "arch.etracealertconsole",
            groupBy: ["appId"],
            fields: ["count"],
            tagFilters: [filter],
            from: from,
            to: to,
            functions: [{
                modelEnum: "interval", name: "interval",
                params: [{name: "interval", type: "string", display: false, width: 41}],
                defaultParams: ["1m"]
            }],
        };
    }
}
