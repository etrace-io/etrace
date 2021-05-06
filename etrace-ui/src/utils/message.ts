import React from "react";
import {message} from "antd";
import {AxiosResponse} from "axios";
import {SystemKit} from "$utils/Util";

declare type messageStatus = "success" | "error" | "info" | "warning" | "warn" | "loading";
declare type ConfigOnClose = () => void;

export const MESSAGE_DURATION = 3;

// 全局配置
message.config({
    top: 30,
    duration: MESSAGE_DURATION,
    maxCount: 3
});

export function messageHandler(type: messageStatus, content: React.ReactNode | string, duration?: number | null, onClose?: ConfigOnClose) {
    return message[type].call(null, content, duration, onClose);
}

export function httpCodeHandler(resp: AxiosResponse, url: string, msg: string) {
    if (!resp || (resp.status < 200 || resp.status >= 300)) {
        if (resp.status == 401) {
            SystemKit.redirectToLogin();
        } else {
            return messageHandler(
                "error",
                `${msg} (${resp.status + ":" + resp.data.error || (resp.data.stat && resp.data.stat.errorMsg)})`
            );
        }
    } else {
        messageHandler(
            "success",
            `${msg}成功`
        );
    }
}
