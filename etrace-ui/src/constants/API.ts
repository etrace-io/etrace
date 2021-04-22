// API 相关
import {ENV} from "./Env";
import {get} from "lodash";

interface APIGateway {
    monitor: string;
    dog: string;
    root: string;
    esm: string;
    trace_monitor: string;
    env: ENV;
}

const API_DAILY: APIGateway = {
    monitor: "https://monitor-api.daily.elenet.me",
    dog: "https://monitor-api.daily.elenet.me",
    root: "https://monitor-api.daily.elenet.me",
    esm: "https://monitor-api.daily.elenet.me",
    trace_monitor: "https://monitor-api.daily.elenet.me",
    env: ENV.DAILY
};

const API_PROD: APIGateway = {
    monitor: "https://etrace-gw.ele.me/monitor",
    dog: "https://etrace-gw.ele.me/dog",
    root: "https://etrace-gw.ele.me/holmes",
    esm: "https://etrace-gw.ele.me/esm",
    trace_monitor: "https://trace-monitor.elenet.me",
    env: ENV.PROD
};

// 用于本地测试，线上的环境变量由线上构建器在部署阶段提供
export const API = {
    [ENV.PROD]: API_PROD,
    [ENV.DAILY]: API_DAILY,
};

const currEnv: ENV = get(window, "CONFIG.ENV", ENV.PROD).toLocaleLowerCase();
const currApi: APIGateway = get(window, "CONFIG.MONITOR")
    // 当前为线上版本
    ? {
        monitor: get(window, "CONFIG.MONITOR"),
        dog: get(window, "CONFIG.DOG"),
        root: get(window, "CONFIG.ROOT"),
        esm: get(window, "CONFIG.ESM"),
        trace_monitor: get(window, "CONFIG.TRACEMONITOR"),
        env: currEnv,
    }
    // 本地开发
    : API[currEnv];

// tslint:disable-next-line:no-console
console.log("当前 API 接口详情：", currApi);
// tslint:disable-next-line:no-console
console.log("%c 当前 EMonitor 环境 %c ".concat(currEnv, " %c"), "background:#328cf0 ; margin: 6px 0; padding: 3px 1px; border-radius: 3px 0 0 3px;  color: #fff", "background:#e94708 ; padding: 3px 1px; border-radius: 0 3px 3px 0;  color: #fff", "background:transparent");

export function getApiByEnv(env: ENV) {
    switch (env) {
        case ENV.DAILY:
            return API_DAILY;
        case ENV.PROD:
            return API_PROD;
        default:
            console.warn("can't find api settings for profile: ", env, ". use default Test profile");
            return API_PROD;
    }
}
// export const CURR_API = currApi;
export const CURR_API = currApi;
