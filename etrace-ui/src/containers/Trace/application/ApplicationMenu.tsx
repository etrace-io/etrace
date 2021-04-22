import React from "react";
import {formatter} from "../../../utils/menu";
import {LinkOutlined} from "@ant-design/icons/lib";
import {BizMenuIcon} from "../../../components/Icon/BizIcon";

export const menuData = [
    {
        name: "Overview",
        icon: <BizMenuIcon type={"icon-overview"}/>,
        path: "overview"
    },
    {
        name: "Transaction",
        icon: <BizMenuIcon type={"icon-T2"}/>,
        path: "transaction"
    },
    {
        name: "Event",
        icon: <BizMenuIcon type={"icon-e"}/>,
        path: "event"
    },
    {
        name: "Exception",
        icon: "exclamation-circle-o",
        path: "exception"
    },
    {
        name: "SOA",
        icon: <BizMenuIcon type={"icon-connect"}/>,
        path: "soa",
        children: [
            {
                name: "Provider",
                icon: <BizMenuIcon type={"icon-connect"}/>,
                path: "provider"
            },
            {
                name: "Consumer",
                icon: <BizMenuIcon type={"icon-connect"}/>,
                path: "consumer"
            },
            {
                name: "Dependency",
                icon: <BizMenuIcon type={"icon-connect"}/>,
                path: "dependency"
            },
            {
                name: "Pizza",
                icon: <BizMenuIcon type={"icon-connect"}/>,
                path: "pizza"
            }
        ]
    },
    {
        name: "URL",
        icon: <BizMenuIcon type={"icon-url1"}/>,
        path: "url"
    },
    {
        name: "JVM",
        icon: <BizMenuIcon type={"icon-JVM"}/>,
        path: "jvm"
    },
    {
        name: "Redis",
        icon: <BizMenuIcon type={"icon-Redis"}/>,
        path: "redis"
    },
    {
        name: "RMQ Publisher",
        icon: <BizMenuIcon type={"icon-icon_queue"}/>,
        path: "rmq_publish"
    },
    {
        name: "RMQ Consumer",
        icon: <BizMenuIcon type={"icon-icon_queue"}/>,
        path: "rmq_consumer"
    },
    {
        name: "Ejdbc Pool",
        icon: <LinkOutlined />,
        path: "ejdbc"
    },
    {
        name: "Database",
        icon: <BizMenuIcon type={"icon-database"}/>,
        path: "database"
    },
];

export const getApplicationMenuData = function (params: Object) {
    return formatter(menuData, "trace/", null, params);
};
