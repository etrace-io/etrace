import {notification} from "antd";
import {duration} from "./notification";
import {SystemKit} from "$utils/Util";
import axios, {AxiosResponse} from "axios";
import {JsonConvert} from "json2typescript";

const jsonConvert: JsonConvert = new JsonConvert();

async function GetAndParseAsArray<T>
(url: string, clazz: { new(): T; }, options?: any, errHandler?: any): Promise<T[]> {
    try {
        const resp = await axios.get(url, {
            headers: {"Content-Type": "application/json"},
            withCredentials: true,
            params: options,
        });
        _httpCodeHandler(resp, url);

        return jsonConvert.deserialize(resp.data, clazz) as T[];
    } catch (err) {
        console.warn("fail to deserialize the result of url: ", url, " error: ", err);
        if (errHandler) {
            return errHandler(err);
        }
        _errorHandler(url, err.message);
    }
}

async function GetAndParse<T>(url: string, clazz: { new(): T; }, options?: any, errHandler?: any): Promise<T> {
    try {
        const resp = await axios.get(url, {
            headers: {"Content-Type": "application/json"},
            withCredentials: true,
            params: options,
        });
        _httpCodeHandler(resp, url);

        return jsonConvert.deserialize(resp.data, clazz) as T;
    } catch (err) {
        console.warn("fail to deserialize the result of url: ", url, " error: ", err);
        if (errHandler) {
            return errHandler(err);
        }
        _errorHandler(url, err.message);
    }
}

async function Get<T extends any>(url: string, options?: any) {
    let resp = await axios.get<T>(url, {
        headers: {"Content-Type": "application/json"},
        withCredentials: true,
        params: options,
    }).then(
        (response) => {
            return response;
        }
    );
    return resp;
}

async function Post(url: string, data: any, errHandler?: any) {
    let resp = await axios.post(url, `${JSON.stringify(data)}`, {
        headers: {"Content-Type": "application/json"},
        withCredentials: true
    });
    return resp;
}

async function Put(url: string, data?: any) {
    let param: any;
    if (data) {
        param = JSON.stringify(data);
    }
    let resp = await axios.put(url, param, {
        headers: {"Content-Type": "application/json"},
        withCredentials: true
    });
    return resp;
}

async function AlertPut(url: string, data?: any) {
    let param: any;
    if (data) {
        param = JSON.stringify(data);
    }
    let resp = await axios.put(url, param, {
        headers: {"Content-Type": "application/json", "ALERT-AUTH-TOKEN": "09684ff0-bc05-11e8-8a39-c4b301bfebfd"},
        withCredentials: true
    });
    return resp;
}

async function Delete(url: string, options?: any) {
    let resp = await axios.delete(url, {
        headers: {"Content-Type": "application/json"},
        withCredentials: true,
        params: options,
    });
    return resp;
}

function _errorHandler(url: string, err: any) {
    let resp = err.response;
    if (resp) {
        notification.error({
            message: resp.status,
            description: resp.data.message,
            duration: 10
        });
    } else {
        notification.error({message: url, description: err.message, duration: duration});
    }
}

function _httpCodeHandler(resp: AxiosResponse, url: string) {
    if (!resp || (resp.status < 200 || resp.status >= 300)) {
        if (resp.status == 401) {
            SystemKit.redirectToLogin();
        }
    }
}

export {Get, Post, Put, Delete, GetAndParse, GetAndParseAsArray, AlertPut};
