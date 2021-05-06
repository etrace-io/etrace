import prettyFormat from "pretty-format";

const uuid = require("react-native-uuid");

export class Util {
    public static debug(obj: never, msg?: string) {
        console.log(msg, prettyFormat(obj));  // tslint:disable-line
    }
}

export function uniqueId() {
    return uuid.v4();
}

export function isEmpty(value: any) {
    if (!value) {
        return true;
    }
    if (value instanceof Number) {
        return false;
    }
    if (value instanceof Object) {
        return Object.keys(value).length === 0;
    }
    return value.length === 0;
}

export function calcWidth(value: any) {
    if (isEmpty(value)) {
        return 0;
    }
    let length = 0;
    if (value instanceof Array) {
        value.forEach(v => {
            length += v.length * 8 + 50;
        });
    } else if (value instanceof String) {
        length += value.length;
    }
    length += 10;
    return length;
}

export function setStyle(target: any, style: object) {
    if (!target || !target.style) {
        return;
    }
    Object.keys(style).forEach(key => {
        target.style[key] = style[key];
    });
}

export const MapToObject = (aMap => {
    const obj = {};
    if (aMap) {
        aMap.forEach((v, k) => {
            obj[k] = v;
        });
    }
    return obj;
});

export const ObjectToMap = (obj => {
    const mp = new Map();
    Object.keys(obj).forEach(k => {
        mp.set(k, obj[k]);
    });
    return mp;
});

export function mapObjectToUrlParamsStr(params: any) {
    if (!params) {
        return "";
    }
    return Object.keys(params).map(key => {
        const param = params[key];
        if (param instanceof Array) {
            return param.map(i => `${key}=${i}`).join("&");
        } else {
            return `${key}=${param}`;
        }
    }).join("&");
}

export {default as DOMKit} from "./UtilKit/DOMKit";
export {default as UserKit} from "./UtilKit/UserKit";
export {default as ToolKit} from "./UtilKit/ToolKit";
export {default as QueryKit} from "./UtilKit/QueryKit";
export {default as ChartKit} from "./UtilKit/ChartKit";
export {default as TargetKit} from "./UtilKit/TargetKit";
export {default as MetricKit} from "./UtilKit/MetricKit";
export {default as SeriesKit} from "./UtilKit/SeriesKit";
export {default as CookieKit} from "./UtilKit/CookieKit";
export {default as SystemKit} from "./UtilKit/SystemKit";
export {default as LocalStorageKit} from "./UtilKit/LocalStorageKit";
