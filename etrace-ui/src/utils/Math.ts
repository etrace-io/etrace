import {isEmpty} from "./Util";

const R = require("ramda");

export function max(vals: number[], excludeNull?: boolean) {
    let values = removeNull(vals, excludeNull);
    return R.reduce(R.max, 0, values);
}

export function mean(vals: number[], excludeNull?: boolean) {
    let values = removeNull(vals, excludeNull);
    return R.mean(values).toFixed(2);
}

export function last(vals: number[], excludeNull?: boolean) {
    let values = removeNull(vals, excludeNull);
    return R.last(values);
}

function removeNull(vals: number[], excludeNull?: boolean): number[] {
    let values = vals;
    if (excludeNull) {
        values = [];
        vals.forEach(v => {
            if (!isEmpty(v)) {
                values.push(v);
            }
        });
    }
    return values;
}