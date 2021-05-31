import {reaction} from "mobx";
import {useBoolean} from "ahooks";
import isEqualWith from "lodash/isEqualWith";
import StoreManager from "$store/StoreManager";
import {useCallback, useEffect, useMemo, useRef} from "react";

const isParamsEqual = (value, other) => isEqualWith(value, other, (a, b) => {
    if (Array.isArray(a) && Array.isArray(b)) {
        if (a.length !== b.length) { return false; }
        return a.every(item => b.includes(item));
    }
});

/**
 * 获取对应 keys 中的字段，获取 URL 中的 value
 * 返回值将根据 URL 的变化更新
 * @param keys 监听的 URL 中的字段列表
 */
export default function useSearchParams(keys: string[] | string) {
    const getLatestParams = useCallback(() => {
        const searchParams = new URLSearchParams(window.location.search);
        const newValues = {};
        if (Array.isArray(keys)) {
            keys.forEach(key => {
                const value = searchParams.getAll(key);
                if (value.length > 0) {
                    newValues[key] = value.length > 1 ? value : value[0];
                }
            });
        } else {
            const value = searchParams.getAll(keys);
            if (value.length > 0) {
                newValues[keys] = value.length > 1 ? value : value[0];
            }
        }

        return newValues;
    }, [keys]);

    const prevValues = useRef(null);
    // 不使用 useState 的原因：key 值变化后 state 无法在同一 render 阶段实现更新；
    const [isUpdate, {toggle: triggerUpdate}] = useBoolean(); // 手动触发重新计算

    const values = useMemo(() => {
        const newValues = getLatestParams();
        const isSame = isParamsEqual(prevValues.current, newValues);
        if (!isSame) {
            prevValues.current = newValues;
        }
        return isSame ? prevValues.current : newValues;
    }, [getLatestParams, isUpdate]);

    useEffect(() => {
        if (!keys?.length) { return; }

        const disposer = reaction(
            () => StoreManager.urlParamStore.changed,
            () => { triggerUpdate(); }
        );

        return () => disposer();
    }, [getLatestParams, keys]);

    return values;
}
