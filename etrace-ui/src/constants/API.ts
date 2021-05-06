// API 相关
import {ENV} from "./Env";
import {get} from "lodash";

interface APIGateway {
    monitor: string;
    env: ENV;
}

const API_TEST: APIGateway = {
    monitor: "https://monitor-api.daily.elenet.me",
    env: ENV.TEST
};

const API_PROD: APIGateway = {
    monitor: "https://etrace-gw.ele.me/monitor",
    env: ENV.PROD
};

// 用于本地测试，线上的环境变量由线上构建器在部署阶段提供
export const API = {
    [ENV.PROD]: API_PROD,
    [ENV.TEST]: API_TEST,
};

const currEnv: ENV = get(window, "CONFIG.ENV", ENV.PROD).toLocaleLowerCase();
const currApi: APIGateway = get(window, "CONFIG.MONITOR")
    // 当前为线上版本
    ? {
        monitor: get(window, "CONFIG.MONITOR"),
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
        case ENV.TEST:
            return API_TEST;
        case ENV.PROD:
            return API_PROD;
        default:
            console.warn("can't find api settings for profile: ", env, ". use default Test profile");
            return API_TEST;
    }
}
export const CURR_API = currApi;
