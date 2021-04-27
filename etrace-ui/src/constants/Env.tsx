import get from "lodash/get";

export enum ENV {
    PROD = "prod",
    TEST = "test",
}

// 当前支持的环境，方便快速上/下线
export const SUPPORT_ENV: ENV[] = [
    ENV.PROD,
    ENV.TEST,
];

export const ENV_COLOR = {
    [ENV.PROD]: "#ff4d4f",
    [ENV.TEST]: "#409EFF",
};

export const EMONITOR_URL = {
    [ENV.PROD]: get(window, "CONFIG.URL_PROD", "https://monitor.faas.ele.me"),
    [ENV.TEST]: get(window, "CONFIG.URL_DAILY", "https://monitor.daily.elenet.me"),
};
