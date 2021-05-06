import {DataFormatter} from "../utils/DataFormatter";

export enum ScaleModelEnum {
    Linear = "linear",
    Base2 = "log(base 2)",
    Base10 = "log(base 10)",
    Base32 = "log(base 32)",
    Base1024 = "log(base 1024)"
}

export class ScaleModel {

    public static models: Array<ScaleModel> = [
        new ScaleModel(ScaleModelEnum.Linear),
        new ScaleModel(ScaleModelEnum.Base2),
        new ScaleModel(ScaleModelEnum.Base10),
        new ScaleModel(ScaleModelEnum.Base32),
        new ScaleModel(ScaleModelEnum.Base1024),
    ];

    public static default: ScaleModel = new ScaleModel(ScaleModelEnum.Linear);

    text: string;
    modelEnum: ScaleModelEnum;

    constructor(modelEnum: ScaleModelEnum, text?: string) {
        this.modelEnum = modelEnum;
        this.text = text ? text : modelEnum.toString();
    }
}

export function LinearFormat() {
    return function () {
        return this.tickPositions;
    };
}

export function Base2Format() {
    return function () {
        return DataFormatter.transformScale(this.dataMax, 2);
    };
}

export function Base10Format() {
    return function () {
        return DataFormatter.transformScale(this.dataMax, 10);
    };
}

export function Base32Format() {
    return function () {
        return DataFormatter.transformScale(this.dataMax, 32);
    };
}

export function Base1024Format() {
    return function () {
        return DataFormatter.transformScale(this.dataMax, 1024);
    };
}

export function findUnitModel(tp: ScaleModelEnum): ScaleModel {
    for (let model of ScaleModel.models) {
        if (model.modelEnum == tp) {
            return model;
        }
    }
    return null;
}

export function findScaleFormatter(text: string): number {
    for (let model of ScaleModel.models) {
        if (model.text == text) {
            switch (model.modelEnum) {
                case ScaleModelEnum.Base2:
                    return 2;
                case ScaleModelEnum.Base10:
                    return 10;
                case ScaleModelEnum.Base32:
                    return 32;
                case ScaleModelEnum.Base1024:
                    return 1024;
                default:
                    return null;
            }
        }
    }
    return null;
}

export function findScaleFormatter2(text: string): number {
    for (let model of ScaleModel.models) {
        if (model.text == text) {
            switch (model.modelEnum) {
                case ScaleModelEnum.Base2:
                    return this.Base2Format();
                case ScaleModelEnum.Base10:
                    return this.Base10Format();
                case ScaleModelEnum.Base32:
                    return this.Base32Format();
                case ScaleModelEnum.Base1024:
                    return this.Base1024Format();
                case ScaleModelEnum.Linear:
                    return this.LinearFormat();
                default:
                    return this.LinearFormat();
            }
        }
    }
    return this.LinearFormat();
}

export function log2lin(tick: number) {
    return function (num: number) {
        if (num < tick) {
            num += (tick - num) / tick;
        }
        return Math.log(num) / Math.log(tick);
    };
}

export function lin2log(tick: number) {
    return function (num: number) {
        num = Math.ceil(num);
        let result = Math.pow(tick, num);
        if (result < tick) {
            result = (tick * (result - 1)) / (tick - 1);
        }
        return result;
    };
}

export const SCALES: any = {
    scales: ScaleModel.models
};