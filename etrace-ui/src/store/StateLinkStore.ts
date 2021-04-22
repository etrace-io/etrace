import {observable} from "mobx";
import {StatListItem} from "$models/ChartModel";

export class StateLinkStore {
    @observable public statListData: Array<StatListItem> = [];
}