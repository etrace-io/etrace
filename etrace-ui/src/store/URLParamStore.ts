import {action, observable} from "mobx";
import {
    findTimePickerModelByTime,
    REFRESH,
    TIME_FROM,
    TIME_TO,
    TimePickerModel,
    TIMESHIFT
} from "$models/TimePickerModel";
import {isEmpty} from "$utils/Util";
import {forbidRefresh} from "$components/TimePicker/TimePicker";
import {notification} from "antd";
import {browserHistory} from "$utils/UtilKit/SystemKit";

const R = require("ramda");

export class URLParamStore {
    @observable public changed: boolean;
    @observable public forceChanged: boolean;
    @observable public seconds: number;
    @observable public changeList: string[];
    @observable public selectedTime: { from: string, to: string } = null;
    @observable private params: URLSearchParams;
    private timer: any;

    constructor() {
        this.applyURLChange();

        // AOP history.pushState method for update params after url changed
        const pushState = window.history.pushState;
        const replaceState = window.history.replaceState;

        window.history.pushState = (data: any, title: string, url?: string | null) => {
            Reflect.apply(pushState, window.history, [data, title, url]);
            // after url change modify store url params
            this.applyURLChange();
        };

        window.history.replaceState = (data: any, title: string, url?: string | null) => {
            Reflect.apply(replaceState, window.history, [data, title, url]);
            this.applyURLChange();
        };

        window.addEventListener("popstate", () => {
            this.applyURLChange();
        });
    }

    public getValue(key: string): string | null {
        return this.params.get(key);
    }

    public getValues(key: string): string[] {
        return this.params.getAll(key);
    }

    @action
    public forceChange() {
        this.forceChanged = !this.forceChanged;
    }

    public getTimeShift() {
        return this.params.get(TIMESHIFT);
    }

    public getTimeAutoReflesh() {
        return this.params.get(REFRESH);
    }

    public getSelectedTime(hasDefault: boolean = true): TimePickerModel | null {
        let model = hasDefault ? TimePickerModel.default : null;
        try {
            const params = this.params;
            if (params.get(TIME_FROM) && params.get(TIME_TO)) {
                let start = params.get(TIME_FROM);
                let to = params.get(TIME_TO);
                model = findTimePickerModelByTime(start, to);
            }
        } catch (err) {
            console.warn("init time data error:", err);
        }
        return model;
    }

    public changeSelectedTime(from: string, to: string) {
        const time = {from, to};
        this.selectedTime = time;
        this.changeURLParams(time);
    }

    /**
     *  Change url params
     * @param params set params
     * @param needDelete delete url params key
     * @param clearAll clean all url params, but keep time range
     * @param method push or replace
     */
    @action
    public changeURLParams(params: object, needDelete: Array<string> = [], clearAll: boolean = false, method: "push" | "replace" = "push") {
        const {pathname} = browserHistory.location;
        const {search} = window.location;
        const oldSearchParams = new URLSearchParams(search); // 不能修改，保留之前的url信息，最后再做对比
        const searchParams = clearAll ? new URLSearchParams() : new URLSearchParams(search);
        if (!clearAll) {
            for (const key of needDelete) {
                searchParams.delete(key);
            }
        } else {
            if (oldSearchParams.has(TIME_TO)) {
                searchParams.set(TIME_TO, oldSearchParams.get(TIME_TO));
            }
            if (oldSearchParams.has(TIME_FROM)) {
                searchParams.set(TIME_FROM, oldSearchParams.get(TIME_FROM));
            }
            if (oldSearchParams.has(TIMESHIFT)) {
                searchParams.set(TIMESHIFT, oldSearchParams.get(TIMESHIFT));
            }
        }

        for (let k of Object.keys(params)) {
            const v = params[`${k}`];
            if (k) {
                if (!isEmpty(v)) {
                    if (Array.isArray(v)) {
                        searchParams.delete(k);
                        v.forEach(oneValue => searchParams.append(k, oneValue));
                    } else {
                        searchParams.set(k, v);
                    }
                } else {
                    // if v == null or empty, then delete that key
                    searchParams.delete(k);
                }
            } else {
                console.warn("Something try to set Url with null, key: ", k, ", value: ", v);
            }
        }

        const newParamsStr = searchParams.toString();
        const oldParamsStr = oldSearchParams.toString();
        if (!R.equals(newParamsStr, oldParamsStr)) {
            if (browserHistory[method]) {
                browserHistory[method].call(this, {search: newParamsStr, pathname: pathname});
            }
            // browserHistory.push({search: newParamsStr, pathname: pathname});
        }
    }

    public redirectTo(url: string) {
        browserHistory.push(url);
    }

    public getSeconds(): number {
        const value = this.params.get(REFRESH);
        if (value && value != "off") {
            let interval: number = this.getRefreshTime(value);
            this.seconds = interval / 1000;
        } else {
            this.seconds = 0;
        }
        return this.seconds;
    }

    @action
    private applyURLChange(): void {
        this.params = new URLSearchParams(window.location.search);
        this.changed = !this.changed;

        this.autoRefresh();
    }

    private autoRefresh() {
        const value = this.params.get(REFRESH);
        if (!value || value == "off") {
            clearInterval(this.timer);
        } else {
            const from = this.params.get(TIME_FROM);
            const to = this.params.get(TIME_TO);
            if (from && to) {
                let model: TimePickerModel = findTimePickerModelByTime(from, to);
                if (!model.modelEnum) {
                    notification.warning({message: "自动刷新提示", description: "特定查询时间点，关闭自动刷新！"});
                    this.params.set(REFRESH, "off");
                    return;
                }
                if (forbidRefresh.indexOf(model.modelEnum) >= 0) {
                    notification.warning({message: "自动刷新提示", description: "时间选择大于等于12小时，关闭自动刷新！"});
                    this.params.set(REFRESH, "off");
                    clearInterval(this.timer);
                    return;
                }
            }
            let interval: number = this.getRefreshTime(value);
            clearInterval(this.timer);
            this.timer = setInterval(
                () => {
                    this.forceChanged = !this.forceChanged;
                },
                interval
            );
        }
    }

    private getRefreshTime(refresh: string): number {
        let length = refresh.length;
        let interval: number = Number.parseInt(refresh.slice(0, length - 1), 10);
        let type = refresh.slice(length - 1);
        if (type == "m") {
            return interval * 60 * 1000;
        }
        return interval * 1000;
    }
}
