/**
 * 参数 `params` 用于在 NavMenu 中的 MenuItem 的 URL path 后加上参数（可以不传）
 * 如传进来的 `params` 为 { appId: "zeus.eos", test: “hello” } 对象
 * 则 `result.params` 为 `?appId=zeus.eos&test=hello`，否则为 `null`
 * 需要注意的是 `params` 会清空值为 null | undefined | false 这类 falsy 值对应的键值对，如 { appId: "test", test: null } 结果仅为 `?appId=test`
 */
import {isEmpty} from "./Util";

function formatter(data: Array<any>, parentPath: string = "", parentAuthority: any, params?: Object): Array<any> {
    return data.map((item) => {
        const result = {
            ...item,
            path: `${parentPath}${item.path}`,
            authority: item.authority || parentAuthority,
            params: params
                ? getSearch(params)
                : null
        };
        if (item.children) {
            result.children = formatter(item.children, `${parentPath}${item.path}/`, item.authority, params);
        }
        return result;
    });
}

function getSearch(params: Object) {
    return "?" + Object
        .keys(params)
        .map(param => {
            const values = params[param];
            if (isEmpty(values)) {
                return null;
            }
            if (Array.isArray(values)) {
                return values.map(v => `${param}=${v}`).join("&");
            } else {
                return `${param}=${values}`;
            }
        })
        .filter(i => i)
        .join("&");
}

export {formatter};