import {UnitModel, UnitModelEnum} from "$models/UnitModel";

// https://github.com/ben-ng/convert-units#readme
const convert = require("convert-units");

export class DataFormatter {

    public static formatterByUnit(unit: UnitModelEnum, point: any, decimals?: number): string {
        switch (unit) {
            case UnitModelEnum.Nanoseconds:
                return this.transformNanoSeconds(point, decimals);
            case UnitModelEnum.Milliseconds:
                return this.transformMilliseconds(point, decimals);
            case UnitModelEnum.Microseconds:
                return this.transformMicroseconds(point, decimals);
            case UnitModelEnum.Seconds:
                return this.transformSeconds(point, decimals);
            case UnitModelEnum.Bytes:
                return this.transformBytes(point, decimals);
            case UnitModelEnum.Bits:
                return this.transformBits(point, decimals);
            case UnitModelEnum.Percent:
                return this.transformPercent(point, decimals);
            case UnitModelEnum.Percent0_0:
                return this.transformPercent0_0(point, decimals);
            case UnitModelEnum.BytesSec:
                return this.transformBytesSec(point, decimals);
            case UnitModelEnum.BitsSec:
                return this.transformBitsSec(point, decimals);
            case UnitModelEnum.None:
                return this.transformNone(point, decimals);
            case UnitModelEnum.Short:
                return this.transformShort(point, decimals);
            case UnitModelEnum.OpsSec:
                return this.transformOpsSec(point, decimals);
            default:
                return this.transformNone(point, decimals);
        }
    }

    public static tooltipFormatter(text: string, point: any, decimals: number = 2): string {
        let model = this.findUnitModel(text);
        if (!model) {
            return this.transformNone(point, decimals);
        }
        return this.formatterByUnit(model.modelEnum, point, decimals);
    }

    public static findUnitModel(text: string): UnitModel {
        if (text == "percent") {
            return new UnitModel(UnitModelEnum.Percent);
        }
        for (let model of UnitModel.models) {
            if (model.text == text) {
                return model;
            }
        }
        return null;
    }

    public static transformShort(input: number, decimals: number = 2): string {
        if (input > 1000 * 1000 * 1000 * 1000 * 1000) {
            return (input / (1000 * 1000 * 1000 * 1000 * 1000)).toFixed(decimals) + "P";
        } else if (input > 1000 * 1000 * 1000 * 1000) {
            return (input / (1000 * 1000 * 1000 * 1000)).toFixed(decimals) + "T";
        } else if (input > 1000 * 1000 * 1000) {
            return (input / (1000 * 1000 * 1000)).toFixed(decimals) + "B";
        } else if (input > 1000 * 1000) {
            return (input / (1000 * 1000)).toFixed(decimals) + "M";
        } else if (input > 1000) {
            return (input / 1000).toFixed(decimals) + "K";
        } else if (!input) {
            return "0";
        } else {
            return input.toString();
        }
    }

    public static transformOpsSec(input: number, decimals: number = 2): string {
        if (input > 1000 * 1000 * 1000 * 1000 * 1000) {
            return (input / (1000 * 1000 * 1000 * 1000 * 1000)).toFixed(decimals) + "Pops";
        } else if (input > 1000 * 1000 * 1000 * 1000) {
            return (input / (1000 * 1000 * 1000 * 1000)).toFixed(decimals) + "Tops";
        } else if (input > 1000 * 1000 * 1000) {
            return (input / (1000 * 1000 * 1000)).toFixed(decimals) + "Bops";
        } else if (input > 1000 * 1000) {
            return (input / (1000 * 1000)).toFixed(decimals) + "Mops";
        } else if (input > 1000) {
            return (input / 1000).toFixed(decimals) + "Kops";
        } else if (!input) {
            return "0";
        } else {
            return input.toString() + "ops";
        }
    }

    public static transformBytes(input: number, decimals?: number): string {
        if (!input) {
            return "0";
        }
        const best = convert(input).from("B").toBest();
        const value = convert(input).from("B").to(best.unit);
        if (decimals !== undefined) {
            return value.toFixed(decimals) + best.unit;
        } else {
            return Math.floor(value * 100) / 100 + best.unit;
        }
    }

    public static transformPercent(input: number, decimals?: number, percent0?: boolean): string {
        if (!input) {
            return "0%";
        } else {
            if (decimals !== undefined) {
                return input.toFixed(decimals).toString() + "%";
            } else {
                return input.toFixed(2).toString() + "%";
            }
        }
    }

    public static transformPercent0_0(input: number, decimals?: number): string {
        if (!input) {
            return "0%";
        } else {
            input = input * 100;
            if (decimals !== undefined) {
                return input.toFixed(decimals).toString() + "%";
            } else {
                return input.toFixed(2).toString() + "%";
            }
        }
    }

    public static transformPercentNumber(input: number, decimals?: number): number {
        if (!input) {
            return 0;
        } else {
            if (decimals !== undefined) {
                return Number.parseFloat(input.toFixed(decimals));
            } else {
                return Number.parseFloat(input.toFixed(5));
            }
        }
    }

    public static transformBits(input: number, decimals: number = 2): string {
        if (input > 1024 * 1024 * 1024 * 1024 * 1024) {
            return (input / (1024 * 1024 * 1024 * 1024 * 1024)).toFixed(decimals) + "Pb";
        } else if (input > 1024 * 1024 * 1024 * 1024) {
            return (input / (1024 * 1024 * 1024 * 1024)).toFixed(decimals) + "Tb";
        } else if (input > 1024 * 1024 * 1024) {
            return (input / (1024 * 1024 * 1024)).toFixed(decimals) + "Gb";
        } else if (input > 1024 * 1024) {
            return (input / (1024 * 1024)).toFixed(decimals) + "Mb";
        } else if (input > 1024) {
            return (input / 1024).toFixed(decimals) + "Kb";
        } else if (!input) {
            return "0";
        } else {
            return input.toString() + "b";
        }
    }

    public static transformBytesSec(input: number, decimals: number = 2): string {
        if (input > 1024 * 1024 * 1024 * 1024 * 1024) {
            return (input / (1024 * 1024 * 1024 * 1024 * 1024)).toFixed(decimals) + "PBps";
        } else if (input > 1024 * 1024 * 1024 * 1024) {
            return (input / (1024 * 1024 * 1024 * 1024)).toFixed(decimals) + "TBps";
        } else if (input > 1024 * 1024 * 1024) {
            return (input / (1024 * 1024 * 1024)).toFixed(decimals) + "GBps";
        } else if (input > 1024 * 1024) {
            return (input / (1024 * 1024)).toFixed(decimals) + "MBps";
        } else if (input >= 1024) {
            return (input / 1024).toFixed(decimals) + "KBps";
        } else if (!input) {
            return "0Bps";
        } else {
            return input.toString() + "Bps";
        }
    }

    public static transformNone(input: number, decimals: number = 2): string {
        if (!input) {
            return "0";
        } else {
            if (Number.isInteger(input)) {
                return input.toString() + "";
            } else {
                return input.toFixed(decimals).toString() + "";
            }
        }
    }

    public static transformBitsSec(input: number, decimals: number = 2): string {
        if (input > 1024 * 1024 * 1024 * 1024 * 1024) {
            return (input / (1024 * 1024 * 1024 * 1024 * 1024)).toFixed(decimals) + "Pbps";
        } else if (input > 1024 * 1024 * 1024 * 1024) {
            return (input / (1024 * 1024 * 1024 * 1024)).toFixed(decimals) + "Tbps";
        } else if (input > 1024 * 1024 * 1024) {
            return (input / (1024 * 1024 * 1024)).toFixed(decimals) + "Gbps";
        } else if (input > 1024 * 1024) {
            return (input / (1024 * 1024)).toFixed(decimals) + "Mbps";
        } else if (input > 1024) {
            return (input / 1024).toFixed(decimals) + "Kbps";
        } else if (!input) {
            return "0bps";
        } else {
            return input.toString() + "bps";
        }
    }

    public static transformNanoSeconds(input: number, decimals?: number): string {
        if (!input) {
            return "0";
        }
        const best = convert(input).from("ns").toBest();
        const value = convert(input).from("ns").to(best.unit);
        if (decimals !== undefined) {
            return value.toFixed(decimals) + best.unit;
        } else {
            return Math.floor(value * 100) / 100 + best.unit;
        }
    }

    public static transformMilliseconds(input: number, decimals: number = 2): string {
        if (input > 24 * 3600 * 1000) {
            return (input / (24 * 3600 * 1000)).toFixed(decimals) + "d";
        } else if (input > 3600 * 1000) {
            return (input / (3600 * 1000)).toFixed(decimals) + "h";
        } else if (input > 60 * 1000) {
            return (input / 60000).toFixed(decimals) + "m";
        } else if (input >= 1000) {
            return (input / 1000).toFixed(decimals) + "s";
        } else if (!input) {
            return "0ms";
        } else {
            return input + "ms";
        }
    }

    public static transformMicroseconds(input: number, decimals: number = 2): string {
        if (input > 24 * 3600 * 1000 * 1000) {
            return (input / (24 * 3600 * 1000 * 1000)).toFixed(decimals) + "d";
        } else if (input > 60 * 60 * 1000 * 1000) {
            return (input / (60 * 60 * 1000 * 1000)).toFixed(decimals) + "h";
        } else if (input >= 60 * 1000 * 1000) {
            return (input / 60000000).toFixed(decimals) + "m";
        } else if (input >= 1000 * 1000) {
            return (input / 1000000).toFixed(decimals) + "s";
        } else if (input >= 1000) {
            return (input / 1000).toFixed(decimals) + "ms";
        } else if (!input) {
            return "0us";
        } else {
            return input + "us";
        }
    }

    public static transformSeconds(input: number, decimals: number = 2): string {
        if (input > 365 * 24 * 3600) {
            return (input / (365 * 24 * 3600)).toFixed(decimals) + " year";
        } else if (input > 24 * 3600) {
            return (input / (24 * 3600)).toFixed(decimals) + " day";
        } else if (input > 3600) {
            return (input / (3600)).toFixed(decimals) + " hour";
        } else if (input >= 60) {
            return (input / 60).toFixed(decimals) + " minute";
        } else if (!input) {
            return "0 second";
        } else {
            return input + " second";
        }
    }

    public static transformScale(dataMax: number, base: number) {
        if (!dataMax) {
            dataMax = 0;
        }
        let positions: Array<number> = [0];
        let tick = 0;
        for (; Math.pow(base, tick - 1) < dataMax; tick++) {
            positions.push(Math.pow(base, tick));
        }
        return positions;
    }

    public static transformFormat(interval: number) {
        let second = interval / 1000;
        if (second < 60) {
            return second + "s";
        } else {
            return (second / 60) + "m";
        }
    }

}