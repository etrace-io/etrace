import {Theme} from "$constants/Theme";

export interface User {
    id?: number;
    email?: string;
    userIcon?: string;
    psncode?: string;
    psnname?: string;
    deptcode?: string;
    deptname?: string;
    aliEmail?: string;
    fatdeptcode?: string;
    fatdeptname?: string;
    onedeptcode?: string;
    onedeptname?: string;
    roles?: Array<string>;
    buDepartments?: Array<string>;
    userConfig?: UserConfigData;
}

// 键名为显示内容和保存在用户配置里的内容
export enum ConfigKey {
    showAlert = "显示报警事件",
    showPublish = "显示发布事件",
    theme = "选择主题"
}

export interface UserConfigData {
    config?: UserConfig;
}

export class UserConfig {
    showAlert?: boolean;
    showPublish?: boolean;
    theme?: Theme;
    departmentId?: number;
    productLineId?: number;

    constructor() {
        this.showAlert = true;
        this.showPublish = true;
        this.theme = Theme.Light;
    }
}

export interface LoginError {
    message?: string;
    url?: string;
    status?: number;
}
