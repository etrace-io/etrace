import {action, observable, reaction, toJS} from "mobx";
import * as LinDBService from "../services/LinDBService";
import {StatItem, StatListTargets, TagFilter, Target} from "$models/ChartModel";
import {isEmpty} from "$utils/Util";
import {URLParamStore} from "./URLParamStore";
import {get} from "lodash";
import {handleError} from "$utils/notification";

const R = require("ramda");

export class StatListStore {
    urlParamStore: URLParamStore;

    public targets: Map<string, StatListTargets> = new Map();
    @observable statListMap: Map<string, Array<StatItem>> = new Map();
    @observable loading: boolean = false;

    constructor(urlParamStore: URLParamStore) {
        this.urlParamStore = urlParamStore;

        reaction(
            () => urlParamStore.changed,
            () => {
                this.load();
            });
        reaction(
            () => urlParamStore.forceChanged,
            () => {
                this.load(true);
            });
    }

    public init() {
        this.targets = new Map();
        this.statListMap = new Map();
    }

    public load(needLoad?: boolean) {
        this.targets.forEach((v: StatListTargets, k: string) => {
            this.loadData(k, v, needLoad);
        });
    }

    public register(id: string, statListTargets: StatListTargets) {
        this.targets.set(id, statListTargets);
    }

    @action
    public remove(id: string) {
        this.targets.delete(id);
        this.statListMap.delete(id);
    }

    @action
    public setLoading(loading: boolean) {
        this.loading = loading;
    }

    public getLoading(): boolean {
        return this.loading;
    }

    @action
    public async loadData(id: string, statListTargets: StatListTargets, load?: boolean) {
        if (!statListTargets || isEmpty(statListTargets.targets)) {
            console.warn("stat list target is null.");
            return;
        }
        if (statListTargets.loading) {
            return;
        }
        let needLoad = load;

        const tracingVars: Map<string, any> = statListTargets.tracing;
        const staticLoad: Array<string> = statListTargets.staticLoad;
        tracingVars.forEach((v: any, k: string) => {
            const tagVar = this.urlParamStore.getValues(k);
            if (!R.equals(v, tagVar)) {
                needLoad = true;
            }
            if (!staticLoad || staticLoad.indexOf(k) < 0) {
                statListTargets.tracing.set(k, tagVar);
            }

        });
        if (!needLoad) {
            return;
        }
        const targets: Array<Target> = [];
        statListTargets.loading = true;

        statListTargets.targets.forEach((value: Target) => {
            const target: Target = new Target();

            // init
            target.entity = value.entity;
            target.prefix = value.prefix;
            target.fields = value.fields;
            target.groupBy = value.groupBy;
            target.orderBy = value.orderBy;

            // setup selected time
            if (!target.fixedTimeRange && this.urlParamStore.getSelectedTime()) {
                const selectedTime = this.urlParamStore.getSelectedTime();
                target.from = selectedTime.fromString;
                target.to = selectedTime.toString;
            }

            let tagFilters: Array<TagFilter> = value.tagFilters;
            if (!tagFilters) {
                tagFilters = [];
            }

            // setup measurement
            if (value.measurementVars) {
                value.measurementVars.forEach(measurementVar => {
                    target.measurement = value.measurement.replace("${" + measurementVar + "}", this.urlParamStore.getValue(measurementVar));
                });
            } else {
                target.measurement = value.measurement;
            }

            // setup variate
            if (value.variate) {
                value.variate.forEach(variate => {
                    const values = this.urlParamStore.getValues(variate);
                    if (values) {
                        const tag: TagFilter = new TagFilter();
                        tag.key = variate;
                        tag.op = "=";
                        tag.value = values;
                        if (tagFilters[`${variate}`]) {
                            tagFilters[`${variate}`].push(values);
                        } else {
                            tagFilters.push(tag);
                        }
                    }
                });
            }
            target.tagFilters = tagFilters;

            // target is invalid
            if (isEmpty(target.measurement) || isEmpty(target.fields)) {
                return;
            }

            targets.push(target);
        });
        // this.statListMap.delete(id);
        if (targets.length > 0) {
            LinDBService.searchMetrics(targets).then((resultSets) => {
                const statList: Array<StatItem> = LinDBService.buildStatItemListReport(resultSets);
                this.statListMap.set(id, statList);
            }).catch((err) => {
                handleError(err, "获取LinDB数据");
            });
        } else {
            this.statListMap.set(id, []);
        }

        statListTargets.loading = false;
    }

    public getStatList(id: string): Array<StatItem> {
        const result = this.statListMap.get(id);
        if (isEmpty(result)) {
            return [];
        }
        return toJS(result);
    }

    public async loadStatListData(target: Target) {
        let showRatio = false;
        get(target, "functions", []).forEach(fun => {
            if (fun.name === "timeShift") {
                showRatio = true;
            }
        });
        return await LinDBService.searchMetricsList([R.clone(target)], showRatio);
    }
}
