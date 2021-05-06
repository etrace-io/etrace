import {CURR_API} from "$constants/API";
import axios, {AxiosRequestConfig} from "axios";
import {QueryClient} from "react-query";

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            refetchOnWindowFocus: false,
        }
    }
});

interface AxiosInstance {
    request<T = any> (config: AxiosRequestConfig): Promise<T>;
    get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>;
    delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>;
    head<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>;
    post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>;
    put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>;
    patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T>;
}

function createAxiosInstance(config?: AxiosRequestConfig): AxiosInstance {
    const http = axios.create(createRequestConfig(config));
    http.interceptors.response.use(response => {
        return response.data;
    });
    return http;
}

function createRequestConfig(config?: AxiosRequestConfig): AxiosRequestConfig {
    return Object.assign({
        withCredentials: true,
    }, config);
}

export const Http = createAxiosInstance();
export const MonitorHttp = createAxiosInstance({
    baseURL: CURR_API.monitor
});
