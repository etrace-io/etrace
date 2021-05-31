export enum UnitModelEnum {
    None = "none",
    Short = "short",
    Milliseconds = "milliseconds(ms)",
    Nanoseconds = "nanoseconds(ns)",
    Seconds = "seconds(s)",
    Bytes = "bytes",
    Bits = "bits",
    BytesSec = "bytes/sec",
    BitsSec = "bits/sec",
    OpsSec = "ops/sec",
    Percent = "percent(0-100)",
    Percent0_0 = "percent(0.0-1.0)",
    Microseconds = "microseconds(us)",
}

export class UnitModel {

    public static models: Array<UnitModel> = [
        new UnitModel(UnitModelEnum.None),
        new UnitModel(UnitModelEnum.Short),
        new UnitModel(UnitModelEnum.Milliseconds),
        new UnitModel(UnitModelEnum.Nanoseconds),
        new UnitModel(UnitModelEnum.Microseconds),
        new UnitModel(UnitModelEnum.Bytes),
        new UnitModel(UnitModelEnum.Seconds),
        new UnitModel(UnitModelEnum.Bits),
        new UnitModel(UnitModelEnum.BytesSec),
        new UnitModel(UnitModelEnum.Percent),
        new UnitModel(UnitModelEnum.Percent0_0),
        new UnitModel(UnitModelEnum.BitsSec),
        new UnitModel(UnitModelEnum.OpsSec),
    ];

    public static default: UnitModel = new UnitModel(UnitModelEnum.Bytes);

    text: string;
    modelEnum: UnitModelEnum;

    constructor(modelEnum: UnitModelEnum, text?: string) {
        this.modelEnum = modelEnum;
        this.text = text ? text : modelEnum.toString();
    }
}
//
// export function BytesFormat(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformBytes(this.value, decimals);
//     };
// }
//
// export function BitsFormat(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformBits(this.value, decimals);
//     };
// }
//
// export function PercentFormat(decimals: number = 2): UnitModel {
//     return function () {
//         return DataFormatter.transformPercent(this.value, decimals);
//     };
// }
//
// export function Percent0_0Format(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformPercent0_0(this.value, decimals);
//     };
// }
//
// export function BytesSecFormat(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformBytesSec(this.value, decimals);
//     };
// }
//
// export function BitsSecFormat(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformBitsSec(this.value, decimals);
//     };
// }
//
// export function NanosecondsFormat(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformNanoSeconds(this.value, decimals);
//     };
// }
//
// export function MillisecondsFormat(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformMilliseconds(this.value, decimals);
//     };
// }
//
// export function MicrosecondsFormat(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformMicroseconds(this.value, decimals);
//     };
// }
//
// export function SecondsFormat(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformSeconds(this.value, decimals);
//     };
// }
//
// export function NoneFormat(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformNone(this.value, decimals);
//     };
// }
//
// export function OpsSecFormat(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformOpsSec(this.value, decimals);
//     };
// }
//
// export function ShortFormat(decimals: number = 2) {
//     return function () {
//         return DataFormatter.transformShort(this.value, decimals);
//     };
// }
//
// export function findUnitModel(tp: UnitModelEnum): UnitModel {
//     for (let model of UnitModel.models) {
//         if (model.modelEnum == tp) {
//             return model;
//         }
//     }
//     return null;
// }
//
// // export function findUnitFormatter(text: string, decimals: number = 2): UnitModel {
// //     for (let model of UnitModel.models) {
// //         if (text == "percent") {
// //             return PercentFormat(decimals);
// //         }
// //         if (model.text == text) {
// //             switch (model.modelEnum) {
// //                 case UnitModelEnum.Nanoseconds:
// //                     return this.NanosecondsFormat(decimals);
// //                 case UnitModelEnum.Milliseconds:
// //                     return this.MillisecondsFormat(decimals);
// //                 case UnitModelEnum.Seconds:
// //                     return this.SecondsFormat(decimals);
// //                 case UnitModelEnum.Bytes:
// //                     return this.BytesFormat(decimals);
// //                 case UnitModelEnum.Bits:
// //                     return this.BitsFormat(decimals);
// //                 case UnitModelEnum.Percent:
// //                     return this.PercentFormat(decimals);
// //                 case UnitModelEnum.Percent0_0:
// //                     return this.Percent0_0Format(decimals);
// //                 case UnitModelEnum.BytesSec:
// //                     return this.BytesSecFormat(decimals);
// //                 case UnitModelEnum.BitsSec:
// //                     return this.BitsSecFormat(decimals);
// //                 case UnitModelEnum.None:
// //                     return this.NoneFormat(decimals);
// //                 case UnitModelEnum.Short:
// //                     return this.ShortFormat(decimals);
// //                 case UnitModelEnum.OpsSec:
// //                     return this.OpsSecFormat(decimals);
// //                 case UnitModelEnum.Microseconds:
// //                     return this.MicrosecondsFormat(decimals);
// //                 default:
// //                     return this.NoneFormat(decimals);
// //             }
// //         }
// //     }
// //     return this.NoneFormat(decimals);
// // }
//
export const UNITS: any = {
    colOne: UnitModel.models
};