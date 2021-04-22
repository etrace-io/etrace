import {action, observable, toJS} from "mobx";
import * as ChartService from "../services/ChartService";
import * as LinDBService from "../services/LinDBService";
import {Chart, ChartTypeEnum, ComputeTarget, Target} from "$models/ChartModel";
import StoreManager from "./StoreManager";
import {cloneDeep} from "lodash";
import {uniqueId} from "$utils/Util";
import {PageSwitchStore} from "./PageSwitchStore";

const R = require("ramda");

export class EditChartStore {
    public chartUniqueId = "xxxxxxxxx9999999999999";
    public pageSwitchStore;
    public isEditing: boolean = false;
    public timer: any;

    @observable chart: Chart = EditChartStore.newChart();

    public static newChart() {
        const chart: Chart = new Chart();
        chart.id = -1;
        chart.globalId = uniqueId();
        chart.config = {type: ChartTypeEnum.Line};
        chart.targets = [];
        return R.clone(chart);
    }

    constructor(pageSwitchStore: PageSwitchStore) {
        this.pageSwitchStore = pageSwitchStore;
    }

    @action
    public setChart(chart: Chart, needLoadData?: boolean) {
        this.chart = chart;
        if (needLoadData) {
            StoreManager.chartStore.reRegister(this.chartUniqueId, this.chart);
            // StoreManager.chartStore.reRegister(this.chartUniqueId, cloneDeep(this.chart));
        }
    }

    @action
    public setChartChange(chart: Chart, needLoadData?: boolean) {
        this.pageSwitchStore.setPromptSwitch(true);
        this.setChart(chart, needLoadData);
    }

    @action
    public setTarget(index: number, target: Target) {
        this.pageSwitchStore.setPromptSwitch(true);
        // target valid no load data
        if (!Target.valid(target) && !Target.valid(this.chart.targets[index])) {
            this.chart.targets[index] = target;
            return;
        }
        if (!R.equals(this.chart.targets[index], target)) {
            this.chart.targets[index] = target;
            StoreManager.chartStore.reRegister(this.chartUniqueId, this.chart);
            // QueryKit.refetchMetrics(this.chart.globalId);
        }
    }

    @action
    public setComputeTarget(index: number, target: ComputeTarget) {
        this.pageSwitchStore.setPromptSwitch(true);
        // compute target valid no load data
        let flag = false;
        if (!R.equals(this.chart.config.computes[index], target)) {
            flag = true;
        }
        this.chart.config.computes[index] = target;
        if (!ComputeTarget.valid(target) && !ComputeTarget.valid(this.chart.config.computes[index])) {
            return;
        }
        if (flag) {
            if (this.timer) {
                clearInterval(this.timer);
            }
            this.timer = setInterval(() => {
                clearInterval(this.timer);
                StoreManager.chartStore.reComputeMetrics(this.chartUniqueId, cloneDeep(this.chart));
            }, 500);
        }
    }

    @action
    public setAdminVisible(adminVisible: boolean) {
        this.pageSwitchStore.setPromptSwitch(true);
        this.chart.adminVisible = adminVisible;
    }

    public getChart(): Chart {
        return toJS(this.chart);
    }

    @action
    public async loadChart(chartId: number) {
        const chart = await ChartService.get(chartId);
        this.setChart(chart, true);
    }

    /**
     * 用于新建一个 Chart 副本，舍弃一些不需要的字段（体现在 newChart 中）
     * @param chartId
     * @param defaultCategory
     */
    @action
    public async loadCopyChart(chartId: number, defaultCategory: Array<number>) {
        const chart = await ChartService.get(chartId);
        const copyChart: Chart = EditChartStore.newChart();
        copyChart.config = chart.config;
        copyChart.targets = chart.targets;
        if (defaultCategory.length === 2) {
            [copyChart.departmentId, copyChart.productLineId] = defaultCategory;
        } else {
            copyChart.departmentId = chart.departmentId;
            copyChart.productLineId = chart.productLineId;
        }
        copyChart.title = chart.title;
        copyChart.description = chart.description;
        this.pageSwitchStore.setPromptSwitch(true);
        this.setChart(copyChart, true);
    }

    public async saveChart(chart: Chart, monitorUrl?: string, forceToCreate?: boolean) {
        return await ChartService.save(chart, monitorUrl, forceToCreate);
    }

    @action
    public duplicateTarget(source: Target) {
        const t = R.clone(toJS(source));
        this.chart.targets.push(t);

        StoreManager.chartStore.reRegister(this.chartUniqueId, cloneDeep(this.chart));
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    moveTarget(type: any, index: number) {
        let targets = this.chart.targets;
        let target = null;
        switch (type) {
            case "up":
                if (index != 0) {
                    target = targets.splice(index, 1)[0];
                    targets.splice(index - 1, 0, target);
                }
                break;
            case "down":
                if (index < targets.length) {
                    target = targets.splice(index, 1)[0];
                    targets.splice(index + 1, 0, target);
                }
                break;
            default:
                break;
        }
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public deleteTarget(index: number) {
        this.chart.targets.splice(index, 1);

        StoreManager.chartStore.reRegister(this.chartUniqueId, cloneDeep(this.chart));
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public deleteComputeTarget(index: number) {
        this.chart.config.computes.splice(index, 1);

        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public newTarget(target: Target) {
        if (!target) {
            return;
        }
        if (!this.chart.targets) {
            this.chart.targets = [];
        }
        const analyzeTargetIndex = this.chart.targets.findIndex(t => t.isAnalyze);
        if (target.isAnalyze && analyzeTargetIndex > -1) {
            return;
        }
        if (analyzeTargetIndex > -1) {
            // Analyze Target 置底，否则会出现正常的 Target 显示的 LineNum 有问题
            this.chart.targets.splice(analyzeTargetIndex, 0, target);
        } else {
            this.chart.targets.push(target);
        }
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public newComputeTarget(target: ComputeTarget) {
        if (!this.chart.config) {
            this.chart.config = {};
        }
        if (!this.chart.config.computes) {
            this.chart.config.computes = [];
        }
        this.chart.config.computes.push(target);
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public mergeChartGeneral(general: any) {
        this.chart = R.mergeDeepRight(this.chart, general);
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public mergeChartConfig(config: any, forceLoadData: boolean = false) {
        let oldConfig = this.chart.config;
        this.chart.config = R.mergeDeepRight(oldConfig, config);
        if (forceLoadData) {
            StoreManager.chartStore.reRegister(this.chartUniqueId, cloneDeep(this.chart));
        }
        // QueryKit.refetchMetrics(this.chart.globalId);
        this.pageSwitchStore.setPromptSwitch(true);
    }

    public reset() {
        this.pageSwitchStore.setPromptSwitch(false);
        this.setChart(EditChartStore.newChart(), true);
    }

    public async searchMeasurementName(target: Target) {
        if (target.prefix || !target.prefixRequired) {
            return await LinDBService.searchMeasurement(target);
        }
    }

    public async showFields(target: Target) {
        let measurement = target.measurement;
        if (target.prefixRequired) {
            measurement = target.prefix + "." + measurement;
        }
        return await LinDBService.showField(target.entity, measurement);
    }

    public async showTagValues(target: Target, tagKey: string, prefix: string) {
        let measurement = target.measurement;
        if (target.prefixRequired) {
            measurement = target.prefix + "." + measurement;
        }
        return await LinDBService.showTagValue(target.entity, measurement, tagKey, prefix);
    }

    public async showTagKeys(target: Target) {
        let measurement = target.measurement;
        if (target.prefixRequired) {
            measurement = target.prefix + "." + measurement;
        }
        return await LinDBService.showTagKey(target.entity, measurement);
    }

    public async check(target: Target) {
        let measurement = target.measurement;
        if (target.prefixRequired) {
            measurement = target.prefix + "." + measurement;
        }
        return await LinDBService.showTagKey(target.entity, measurement);
    }
}
