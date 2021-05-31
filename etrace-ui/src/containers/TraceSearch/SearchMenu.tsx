import React from "react";
import {formatter} from "$utils/menu";
import {BizMenuIcon} from "$components/Icon/BizIcon";

const menuData = [
    {
        name: "链路搜索",
        icon: <BizMenuIcon type={"icon-zprofileridominatortree"}/>,
        path: "request"
    },
    {
        name: "订单搜索",
        icon: <BizMenuIcon type={"icon-dingdanchaxun"}/>,
        path: "order"
    }
];

export const getSearchMenuData = function () {
    return formatter(menuData, "search/", null);
};
