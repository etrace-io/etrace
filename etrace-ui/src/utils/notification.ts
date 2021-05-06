import {notification} from "antd";
import {AxiosResponse} from "axios";
import {SystemKit} from "$utils/Util";
import {ArgsProps} from "antd/lib/notification";
import {browserHistory} from "$utils/UtilKit/SystemKit";

export const duration = 10;

function errorHandler(props: ArgsProps) {
    notification.error(props);
}

function successHandler(props: ArgsProps) {
    notification.success(props);

}

function infoHandler(props: ArgsProps) {
    notification.info(props);
}

function warningHandler(props: ArgsProps) {
    notification.warning(props);
}

function httpCodeHandler(resp: AxiosResponse, url: string, message: string) {
    if (!resp || (resp.status < 200 || resp.status >= 300)) {
        if (resp.status == 401) {
            SystemKit.redirectToLogin(browserHistory.location.pathname + window.location.search);
        } else {
            return errorHandler({
                message: message,
                description: resp.status + ":" + (resp.data.error || (resp.data.stat && resp.data.stat.errorMsg) || resp.data.message)
            });
        }
    } else {
        successHandler({
            message: message,
            description: "success"
        });
    }
}

function handleError(err: any, message: string) {
    let resp = err.response;
    if (resp) {
        if (resp.status == 401) {
            SystemKit.redirectToLogin(browserHistory.location.pathname + window.location.search);
        }
        errorHandler({
            message: message + "(" + resp.status + ")",
            description: resp.data.message,
            duration: duration
        });
    } else {
        errorHandler({message: message, description: err.message, duration: duration});
    }
}

export {errorHandler, handleError, successHandler, infoHandler, warningHandler, httpCodeHandler};
