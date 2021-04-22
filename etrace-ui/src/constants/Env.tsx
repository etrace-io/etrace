import get from "lodash/get";

export enum ENV {
    PROD = "prod",
    DAILY = "daily",
    LEGACY_PROD = "legacyProd",
    LEGACY_DAILY = "legacyDaily",
}

// 当前支持的环境，方便快速上/下线
export const SUPPORT_ENV: ENV[] = [
    ENV.PROD,
    ENV.DAILY,
];

export const ENV_COLOR = {
    [ENV.PROD]: "#ff4d4f",
    [ENV.DAILY]: "#409EFF",
};

export const EMONITOR_URL = {
    [ENV.PROD]: get(window, "CONFIG.URL_PROD", "https://monitor.faas.ele.me"),
    [ENV.DAILY]: get(window, "CONFIG.URL_DAILY", "https://monitor.daily.elenet.me"),
    [ENV.LEGACY_PROD]: get(window, "CONFIG.URL_LEGACY", "https://monitor-legacy.faas.elenet.me"),
    [ENV.LEGACY_DAILY]: get(window, "CONFIG.URL_LEGACY_DAILY", "https://monitor-legacy.faas.daily.elenet.me"),
};
