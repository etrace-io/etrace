import {ENV} from "$constants/Env";
import {SystemKit} from "$utils/Util";
import {SSO_TOKEN_COOKIE_KEY_TEST, SSO_TOKEN_COOKIE_KEY} from "$constants/index";

const Cookies = require("js-cookie");

export default {
    getSSOToken,
    setSSOToken
};

function getSSOToken(): string {
    const currEnv = SystemKit.getCurrEnv();
    const name = currEnv === ENV.TEST
        ? SSO_TOKEN_COOKIE_KEY_TEST
        : SSO_TOKEN_COOKIE_KEY;

    return Cookies.get(name);
}

/**
 * 设置 SSO Token
 * @param token SSO Token
 * @param expires 过期时间，单位：天
 */
function setSSOToken(token: string, expires?: number) {
    const currEnv = SystemKit.getCurrEnv();
    const domain = "." + window.location.hostname.split(".").slice(-2).join(".");

    const name = currEnv === ENV.TEST
        ? SSO_TOKEN_COOKIE_KEY_TEST
        : SSO_TOKEN_COOKIE_KEY;

    Cookies.set(name, token, {
        domain,
        expires: expires ? expires / 60 / 60 / 24 : 1,
        secure: true,       // 为了设置 sameSite: None
        sameSite: "None",   // 为了 iframe 能够获取 cookie
    });
}
