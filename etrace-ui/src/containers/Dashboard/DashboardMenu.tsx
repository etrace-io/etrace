import {formatter} from "../../utils/menu";
import React from "react";
import {AppstoreOutlined} from "@ant-design/icons/lib";

export const alertMenuData = [
    {
        name: "所有大盘",
        icon: <AppstoreOutlined />,
        path: "graph",
    },
    {
        name: "所有节点",
        icon: <AppstoreOutlined />,
        path: "node",
    },
];

export const getDashboardMenuData = function (params: Object) {
    return formatter(alertMenuData, "dashboard/", null, params);
};
