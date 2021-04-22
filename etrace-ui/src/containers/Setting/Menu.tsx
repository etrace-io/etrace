import {formatter} from "$utils/menu";
import React from "react";
import {DatabaseOutlined, EyeOutlined, PartitionOutlined, SettingOutlined, UserOutlined} from "@ant-design/icons";

const menuData = [
    {
        name: "数据源",
        icon: <DatabaseOutlined />,
        path: "datasource"
    },
    {
        name: "监控项",
        icon: <EyeOutlined />,
        path: "entity"
    },
    {
        name: "用户管理",
        icon: <UserOutlined />,
        path: "user-role"
    },
    {
        name: "配置",
        icon: <SettingOutlined />,
        path: "config"
    },
    {
        name: "Proxy",
        icon: <PartitionOutlined />,
        path: "proxy"
    },
];

export const getMenuData = function () {
    return formatter(menuData, "setting/", null);
};
