import get from "lodash/get";

/* 项目整体「组件间」间隙 */
export const SPACE_BETWEEN = 4;

/**
 * 项目部署路径
 * - 顶级目录如 demo.etrace.io；
 * - 部署在一级目录下如：demo.etrace.io/monitor-ui/
 */
export const APP_BASE_PATHNAME = get(window, "CONFIG.PATHNAME", "/").trim().replace(/\/$/, "");
export const APP_BASE_URL = window.location.origin + APP_BASE_PATHNAME;

/* URL Key */
export const LOGIN_BACK_URL_PARAM = "BACK_URL";
export const SSO_TOKEN = "SSO_TOKEN";
export const SEARCH_TYPE = "c";
export const SEARCH_KEY = "s";
export const EZONE = "ezone";

/* Storage Key */
export const STORAGE_KEY_APP_ID = "APPID_KEY";
export const STORAGE_KEY_GLOBAL_SEARCH = "GLOBAL_SEARCH_KEY";
export const EMONITOR_DARK_THEME_TIP = "EMONITOR_DARK_THEME_TIP"; // 临时使用，用于提示暂不支持黑色主题

/* Cookies Key */
export const SSO_TOKEN_COOKIE_KEY = "MONITOR_SSO_TOKEN";
export const SSO_TOKEN_COOKIE_KEY_TEST = "MONITOR_SSO_TOKEN_TEST";

/* Global ID */
export const SOA_PROVIDER_AVG_GLOBAL_ID = "application_soa_provider_avg";
export const SOA_PROVIDER_STATUS_GLOBAL_ID = "application_soa_provider_status";
export const SOA_PROVIDER_RATE_GLOBAL_ID = "application_soa_provider_success_rate";

export const SOA_CONSUMER_AVG_GLOBAL_ID = "application_soa_consumer_avg";
export const SOA_CONSUMER_STATUS_GLOBAL_ID = "application_soa_consumer_status";

/* DataSource Type */
export const LinDB = "LinDB";
export const SimpleJSON = "SimpleJSON";
export const DATASOURCE_TYPE = [LinDB, SimpleJSON];

/* 数据源 */
export const INTERFACE = "interface";
export const APP_ID = "appId";
export const HOST = "host";

/* 静态资源 */
export const EMONITOR_LOGO_DARK = "../public/images/EMONITOR_LOGO_DARK.png";
export const EMONITOR_LOGO_LIGHT = "../public/images/EMONITOR_LOGO_LIGHT.png";
export const EMPTY_BACKGROUND = "../public/images/EMPTY_BACKGROUND.svg";
export const ERROR_IMAGE = "../public/images/ERROR_IMAGE.svg";
export const NO_CHART_DATA = "https://iconfont.alicdn.com/t/8c1051a6-6ba9-4285-80b2-35ce168dcafe.png";

/* 颜色 */
export const EMONITOR_RED = "#ff4d4f";

/* 其他常量 */
export const QUERY_KEY_CHART = "chart"; // 在 react-query 中标识请求
export const QUERY_KEY_METRIC = "metric";
export const MAX_SERIES_COUNT = 50; // 默认展示数据条数
export const CHART_DEFAULT_HEIGHT = 280; // 默认图表区域（canvas）高度

/* api address */
export const API_URL_TEST = "https://etrace-test/api/monitor";
export const API_URL_PROD = "https://etrace-prod/api/monitor";

export const SIMPLE_JSON_URL_PROD = "https://etrace-prod/api/simplejson";
export const PROMETHEUS_URL_TEST = "https://etrace-test/api/prometheus";
export const PROMETHEUS_URL_PROD = "https://etrace-prod/api/prometheus";

/* etrace address */
export const MONITOR_URL_PROD = "https://demo.etrace.io";
export const MONITOR_URL_TEST = "https://test-demo.etrace.io";
