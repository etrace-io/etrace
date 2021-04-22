import {get} from "lodash";
import * as API from "$utils/api";
import {CURR_API} from "$constants/API";
import {useLocation} from "react-router-dom";
import {LoginError, User} from "$models/User";
import {useCallback, useEffect, useRef, useState} from "react";
import {APP_BASE_PATHNAME, APP_BASE_URL, LOGIN_BACK_URL_PARAM, MOZI_SSO_TOKEN} from "$constants/index";

/**
 * 获取用户信息接口，放这里统一管理
 * @param params 参数
 */
function fetchUserInfo(params?: any) {
    const url = `${CURR_API.monitor}/user/info`;
    return API.Get<User>(url, params);
}

/**
 * 自定义登录，继续实现下面接口即可：
 * - loading：默认 true，登录成功后 false；
 * - user：默认 null，登录成功后设置用户信息；
 * - error：LoginError 类型，默认 null，登录过程中出现的错误；
 * - authorized：当前鉴权情况，用于控制是否跳转登录页；
 * - backURL：跳转登录页带上的，登录后重定向链接；
 */
export default function useLogin (
    // token: string,
    callback?: (user: User) => void,
) {
    const location = useLocation();
    const entrance = useRef<URL>(new URL(window.location.href));

    const [loading, setLoading] = useState<boolean>(true);
    const [user, setUser] = useState<User>(null);
    const [error, setError] = useState<LoginError>(null);
    const [authorized, setAuthorized] = useState<boolean>(false);

    const [backURL] = useState<string>(() => {
        const {search} = location;
        const entrancePathname = entrance.current.pathname.replace(APP_BASE_PATHNAME, "");
        const params = new URLSearchParams(search);
        const backURLPath = entrancePathname === "/login" ? "/" : entrancePathname;

        return params.get(LOGIN_BACK_URL_PARAM) ||
            (APP_BASE_URL + backURLPath + search);
    });

    useEffect(() => {
        const {search} = location;
        const params = new URLSearchParams(search);

        const SSO_TOKEN = params.get(MOZI_SSO_TOKEN);

        // if (SSO_TOKEN) {
        //     CookieKit.setMOZIToken(SSO_TOKEN);
        // }

        // 登录
        login(SSO_TOKEN);
        // `${window.location.origin}${backURLPath}${enter === "/" ? "" : encodeURIComponent(enter)}`;
    }, []);

    const login = useCallback((moziSSOToken) => {
        fetchUserInfo({
            moziSSOToken
        })
            .then(result => {
                setUser(result.data);

                if (result.data && result.data.psncode) {
                    setAuthorized(true);
                    callback(result.data);
                } else {
                    const url = get(result, "config.url");
                    setError({
                        message: "用户信息为空",
                        url
                    });

                    callback(null);
                }
            })
            .catch(err => {
                const url = get(err, "config.url");
                const message = get(err, "message");
                const status = get(err, "response.status");

                if (status === 401) {
                    setAuthorized(false);
                } else {
                    setError({ message, url, status });
                }
            })
            .finally(() => {
                setLoading(false);
            });
    }, [callback]);

    // console.log({loading, user, error, authorized, backURL})

    return {loading, user, error, authorized, backURL};
}
