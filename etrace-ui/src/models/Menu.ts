import React from "react";

export interface MenuItem {
    label?: string;
    url?: string;
    isAdmin?: boolean;
    children?: MenuItem[];
    icon?: React.ReactNode;
    history?: { watch: string, storageKey: string }; // 使用 Storage 保存的历史记录拼凑 URL
    isExternal?: boolean;
}
