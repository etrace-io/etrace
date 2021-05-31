import {GroupModel} from "./GroupModel";

export class ResultModel {
    startTime?: number;
    endTime?: number;
    interval?: number;
    pointCount?: number;
    measurementName?: string;
    groups?: Array<GroupModel>;
    data?: any;
}