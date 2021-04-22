import {action, observable} from "mobx";
import {cloneDeep} from "lodash";
import {EMonitorMetricDataSet} from "$models/ChartModel";

interface SeriesClickEvent {
    metric: EMonitorMetricDataSet;
    index: number;
    timestamp: number;
    uniqueId: string;
}

export class ChartEventStore {
    @observable public pieClickEvent: any = null;
    @observable public seriesClickEvent: SeriesClickEvent = null;
    @observable public plotLineClickEvent: any = null;
    @observable public pointMouseOverEvent: any = null;
    @observable public pointMouseOutEvent: any = null;

    @observable public CrosshairInfo: {locked: boolean, value: number} = {locked: false, value: 0};

    @observable public resizeEvent: any = null;

    @action
    public seriesClick(e: SeriesClickEvent) {

        console.log("seriesClick", e );

        this.seriesClickEvent = e;
    }

    @action
    public pieClick(e: any) {
        this.pieClickEvent = e;
    }

    @action
    public plotLineClick(event: any) {
        // clone event, fix click same event, just first time trigger mobx observe
        this.plotLineClickEvent = cloneDeep(event);
    }

    @action
    public pointMouseOver(e: any) {
        this.pointMouseOverEvent = e;
    }

    @action
    public pointMouseOut(e: any) {
        this.pointMouseOutEvent = e;
    }

    @action
    public toggleLockCrosshair() {
        const currState = this.CrosshairInfo.locked;

        if (!currState && this.pointMouseOverEvent) {
            const {value} = this.pointMouseOverEvent;
            this.CrosshairInfo.value = value;
        }

        this.CrosshairInfo.locked = (this.pointMouseOverEvent && this.pointMouseOverEvent.value)
            ? !currState
            : currState;
    }

    @action
    public resetCrosshair() {
        this.CrosshairInfo = {locked: false, value: 0};
    }
}
