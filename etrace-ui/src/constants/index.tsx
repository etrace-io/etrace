import get from "lodash/get";
import {CURR_API} from "$constants/API";
import {EMONITOR_URL, ENV} from "$constants/Env";

/* 项目整体「组件间」间隙 */
export const SPACE_BETWEEN = 4;

/**
 * 项目部署路径
 * - 顶级目录如 monitor-ui.faas.daily.elenet.me/；
 * - 部署在一级目录下如：daily.elenet.me/monitor-ui/
 */
export const APP_BASE_PATHNAME = get(window, "CONFIG.PATHNAME", "/").trim().replace(/\/$/, "");
export const LEGACY_MONITOR_URL = CURR_API.env === ENV.PROD ? EMONITOR_URL[ENV.LEGACY_PROD] : EMONITOR_URL[ENV.LEGACY_DAILY];
export const APP_BASE_URL = window.location.origin + APP_BASE_PATHNAME;

/* URL Key */
export const LOGIN_BACK_URL_PARAM = "BACK_URL";
export const MOZI_SSO_TOKEN = "SSO_TOKEN";
export const SEARCH_TYPE = "c";
export const SEARCH_KEY = "s";
export const EZONE = "ezone";

/* Storage Key */
export const STORAGE_KEY_APP_ID = "APPID_KEY";
export const STORAGE_KEY_GLOBAL_SEARCH = "GLOBAL_SEARCH_KEY";
export const EMONITOR_DARK_THEME_TIP = "EMONITOR_DARK_THEME_TIP"; // 临时使用，用于提示暂不支持黑色主题

/* Cookies Key */
export const MOZI_TOKEN_COOKIE_KEY = "MONITOR_MOZI_TOKEN";
export const DAILY_MOZI_TOKEN_COOKIE_KEY = "MONITOR_DAILY_MOZI_TOKEN";

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
export const EMONITOR_LOGO_DARK = "https://shadow.elemecdn.com/app/monitor/e-monitor-logo_dark.bdf6cfd1-3995-11e9-b6ef-ad95b6be342a.png";
export const EMONITOR_LOGO_LIGHT = "https://shadow.elemecdn.com/app/monitor/e-monitor-logo_light.bdf2d831-3995-11e9-ba1a-55bba1877129.png";
export const EMPTY_BACKGROUND = "https://shadow.elemecdn.com/app/monitor/TVYTbAXWheQpRcWDaDMu.2b596431-fef6-11ea-a28a-bf618f5b9430.svg";
export const ERROR_IMAGE = "https://shadow.elemecdn.com/app/monitor/coding_.7f26e751-63c3-11ea-a7be-8904522559fe.svg";
export const NO_CHART_DATA = "https://iconfont.alicdn.com/t/8c1051a6-6ba9-4285-80b2-35ce168dcafe.png";

/* 颜色 */
export const EMONITOR_RED = "#ff4d4f";

/* 其他常量 */
export const QUERY_KEY_CHART = "chart"; // 在 react-query 中标识请求
export const QUERY_KEY_METRIC = "metric";
export const MAX_SERIES_COUNT = 50; // 默认展示数据条数
export const CHART_DEFAULT_HEIGHT = 280; // 默认图表区域（canvas）高度
