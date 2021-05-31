import {action, observable} from "mobx";
import {ChartStatusEnum} from "$models/ChartModel";

export class LoadingStore {
    @observable public chartStatusMap: Map<string, ChartStatusEnum> = new Map();

    @observable public policyLoading: ChartStatusEnum = ChartStatusEnum.Init;
    @observable public statusLoading: boolean = false;

    @action
    public register(uniqueId: string) {
        this.add(uniqueId);
    }

    @action
    public reRegister(uniqueId: string) {
        this.add(uniqueId);
    }

    public add(uniqueId: string) {
        this.chartStatusMap.set(uniqueId, ChartStatusEnum.Init);
    }

    @action
    public unRegister(uniqueId: string) {
        this.chartStatusMap.delete(uniqueId);
    }

    @action
    public setChartStatus(uniqueId: string, chartStatusEnum: ChartStatusEnum) {
        this.chartStatusMap.set(uniqueId, chartStatusEnum);
    }
}
