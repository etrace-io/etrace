import {debounce} from "lodash";
import {CURR_API} from "$constants/API";
import {createBrowserHistory} from "history";
import {SystemKit, ToolKit} from "$utils/Util";
import {EMONITOR_URL, ENV} from "$constants/Env";
import {APP_BASE_PATHNAME, APP_BASE_URL, LOGIN_BACK_URL_PARAM} from "$constants/index";
import StoreManager from "$store/StoreManager";
import * as message from "$utils/message";

export default {
    redirectToLogin: debounce(redirectToLogin, 500),
    redirectToSSO,
    redirectToOtherOrigin,
    isTest,
    isProd,
    getCurrEnv,
};

export const browserHistory = createBrowserHistory({
    basename: APP_BASE_PATHNAME,
});

/**
 * 跳转至登录页面
 * @param origin 需要登录后跳转回的页面路径
 * @param targetEnv 目标环境，指定则跳转至指定环境 Monitor 登录页
 */
function redirectToLogin(origin?: string, targetEnv?: ENV) {
    origin = origin.replace(APP_BASE_PATHNAME, "");

    const targetURL = origin && origin.indexOf(LOGIN_BACK_URL_PARAM) > -1
        ? APP_BASE_URL + origin
        : ToolKit.paramsToURLSearch({
            [LOGIN_BACK_URL_PARAM]: APP_BASE_URL + origin
        }, "/login");

    // 其他环境跳转
    if (targetEnv && getCurrEnv() !== targetEnv) {
        switch (targetEnv) {
            case ENV.TEST:
                window.open(EMONITOR_URL[ENV.TEST] + targetURL);
                break;
            case ENV.PROD:
            default:
                window.open(EMONITOR_URL[ENV.PROD] + targetURL);
        }
    } else {
        browserHistory.push(targetURL);
    }
}

/**
 * 跳转至 SSO
 * @param env 目标环境
 */
function redirectToSSO0(env?: ENV) {
    const {search} = browserHistory.location;
    const params = new URLSearchParams(search);
    if (params.get("debug")) {
        // todo: should remove debug mode
        console.warn("todo: should remove debug mode in ");
        return;
    }

    const targetEnv = env || getCurrEnv();
    const backUrl = APP_BASE_URL + (params.get(LOGIN_BACK_URL_PARAM) || "");
    switch (targetEnv) {
        case ENV.TEST:
            window.location.href = `https://sso-test/#/?from=${encodeURIComponent(backUrl)}`;
            break;
        case ENV.PROD:
        default:
            window.location.href = `https://sso-prod/auth/entry?from=${encodeURIComponent(backUrl)}`;
    }
}

/**
 * 跳转至 SSO 登录页
 * @param env 目标环境
 */
function redirectToSSO(env?: ENV) {
    const {search} = browserHistory.location;
    const urlParams = new URLSearchParams(search);
    if (urlParams.get("debug")) {
        // todo: should remove debug mode
        console.warn("todo: should remove debug mode in ");
        return;
    }

    const backUrl = urlParams.get(LOGIN_BACK_URL_PARAM) || APP_BASE_URL;

    const targetProfile = env || SystemKit.getCurrEnv();
    switch (targetProfile) {
        case ENV.TEST:
            window.location.href = `https://localhost:8080/test-ssoLogin.htm?BACK_URL=${encodeURIComponent(backUrl)}`;
            break;
        case ENV.PROD:
        default:
            window.location.href = `https://localhost:8080/prod-ssoLogin.htm?BACK_URL=${encodeURIComponent(backUrl)}`;
            break;
    }
}

function redirectToOtherOrigin(origin: string): string {
    let {location: {pathname, search}} = window;

    if (pathname.indexOf("/board/view/") >= 0 || pathname.indexOf("/board/edit/") >= 0) {
        const temp = new URLSearchParams(search);
        const board = StoreManager.boardStore.getImmutableBoard();
        if (!board || !board.globalId) {
            message.messageHandler("warning", "无法找到看板 Global ID，无法跳转");
            return;
        }
        temp.append("globalId", board.globalId);
        search = "?" + temp.toString();
    } else if (pathname.indexOf("/board/explorer/edit/") >= 0) {
        const temp = new URLSearchParams(search);
        const chart = StoreManager.editChartStore.getChart();
        if (!chart || !chart.globalId) {
            message.messageHandler("warning", "无法找到指标 Global ID，无法跳转");
            return;
        }
        temp.append("globalId", chart.globalId);
        search = "?" + temp.toString();
    } else if (pathname.indexOf("/dashboard/graph/view") >= 0 || pathname.indexOf("/dashboard/graph/edit") >= 0) {
        const temp = new URLSearchParams(search);
        const graph = StoreManager.graphStore.graph;
        if (!graph || !graph.globalId) {
            message.messageHandler("warning", "无法找到看板 Global ID，无法跳转");
            return;
        }
        temp.append("globalId", graph.globalId);
        search = "?" + temp.toString();
    }

    return origin + pathname + search;
}

/**
 * 判断当前环境是否为 Test
 */
function isTest() {
    return getCurrEnv() === ENV.TEST;
}

/**
 * 判断当前环境是否为 Prod
 */
function isProd() {
    return getCurrEnv() === ENV.PROD;
}

/**
 * 获取当前环境
 */
function getCurrEnv(): ENV {
    switch (CURR_API.env) {
        case ENV.TEST:
            return ENV.TEST;
        case ENV.PROD:
        default:
            return ENV.PROD;
    }
}
