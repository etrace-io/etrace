import React from "react";
import * as moment from "moment";
import {padStart} from "lodash";

export type valueType = number | string;
export type countdownValueType = valueType | string;

export type Formatter =
    | false
    | "number"
    | "countdown"
    | ((value: valueType, config?: FormatConfig) => React.ReactNode);

export interface FormatConfig {
    formatter?: Formatter;      // 自定义数值展示
    decimalSeparator?: string;  // 小数分隔符，默认 `.`
    groupSeparator?: string;    // 千分位标识符，默认 `,`
    precision?: number;         // 数值精度，默认无
}

export interface CountdownFormatConfig extends FormatConfig {
    format?: string;
}

// Countdown
const timeUnits: [string, number][] = [
    ["Y", 1000 * 60 * 60 * 24 * 365], // years
    ["M", 1000 * 60 * 60 * 24 * 30], // months
    ["D", 1000 * 60 * 60 * 24], // days
    ["H", 1000 * 60 * 60], // hours
    ["m", 1000 * 60], // minutes
    ["s", 1000], // seconds
    ["S", 1], // million seconds
];

function formatTimeStr(duration: number, format: string) {
    let leftDuration: number = duration;

    return timeUnits.reduce(
        (current, [name, unit]) => {
            if (current.indexOf(name) !== -1) {
                const value = Math.floor(leftDuration / unit);
                leftDuration -= value * unit;
                return current.replace(new RegExp(`${name}+`, "g"), (match: string) => {
                    const len = match.length;
                    return padStart(value.toString(), len, "0");
                });
            }
            return current;
        },
        format
    );
}

function interopDefault(m: any) {
    return m.default || m;
}

export function formatCountdown(value: countdownValueType, config: CountdownFormatConfig) {
    const {format = ""} = config;
    const target = interopDefault(moment)(value).valueOf();
    const current = interopDefault(moment)().valueOf();
    const diff = Math.max(target - current, 0);

    return formatTimeStr(diff, format);
}
