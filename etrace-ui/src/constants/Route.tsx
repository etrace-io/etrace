import React from "react";
import {MenuItem} from "$models/Menu";

import {
    ApartmentOutlined,
    ApiOutlined,
    AppstoreOutlined,
    CalendarOutlined,
    ClusterOutlined,
    DashboardOutlined,
    DatabaseOutlined,
    DotChartOutlined,
    ExclamationCircleOutlined,
    FileTextOutlined,
    HddOutlined,
    LaptopOutlined,
    LinkOutlined,
    MonitorOutlined,
    PartitionOutlined,
    ProfileOutlined,
    SearchOutlined,
    SettingOutlined,
    TeamOutlined
} from "@ant-design/icons";
import {APP_ID, STORAGE_KEY_APP_ID} from "$constants/index";

/**
 * Path 相关
 */
export const GLOBAL_SEARCH_PAGE = "/query";

export const BOARD_VIEW = "/board/view"; // `/board/view/:boardId`
export const BOARD_EDIT = "/board/edit"; // `/board/edit/:boardId`
export const METRIC_EDIT = "/board/explorer/edit"; // `/board/explorer/edit/:chartId`
export const BOARD_APP_VIEW = "/app"; // `/app/:dataAppId`
export const BOARD_APP_EDIT = "/board/app/edit"; // `/board/app/edit/:boardAppId`

export const CHART_SINGLE_VIEW = "/chart"; // `/chart/:chartId`

/* Search Params */
export const FULL_SCREEN_CHART = "fullScreenChart"; // 需要全屏展示的 Chart ID

/**
 * 应用级路由（一般为顶部导航）
 */
// EMonitor 导航
export const EMONITOR_ROUTER: MenuItem[] = [
    {
        label: "看板",
        url: "/board",
        icon: <DashboardOutlined/>,
    },
    {
        label: "应用",
        url: "/trace",
        icon: <AppstoreOutlined/>,
        history: {watch: APP_ID, storageKey: STORAGE_KEY_APP_ID},
    },
    {
        label: "服务器",
        url: "/system",
        icon: <ClusterOutlined/>,
    },
    {
        label: "搜索",
        url: "/search",
        icon: <SearchOutlined/>,
    },
    {
        label: "设置",
        url: "/setting",
        isAdmin: true,
        icon: <SettingOutlined/>,
    },
    {
        isExternal: true,
        label: "大盘",
        url: `/dashboard`,
        icon: <ApartmentOutlined/>,
    },
];

// App 导航（首页导航）
export const APP_ROUTER: MenuItem[] = [({
    label: "黄页",
    url: "/yellow-pages",
    icon: <CalendarOutlined/>,
} as MenuItem)].concat(EMONITOR_ROUTER);

/**
 * 侧栏菜单
 */
// 「看板」侧栏
export const BOARD_SIDER: MenuItem[] = [
    {label: "指标查询", url: "/board/explorer", icon: <SearchOutlined/>},
    {label: "指标管理", url: "/board/chart", icon: <DotChartOutlined/>},
    {label: "所有看板", url: "/board/list", icon: <AppstoreOutlined/>},
    {label: "看板应用", url: "/board/app", icon: <LaptopOutlined/>},
];

// 「应用」侧栏
export const TRACE_SIDER: MenuItem[] = [
    {label: "Overview", url: "/trace/overview", icon: <AppstoreOutlined/>},
    {label: "Transaction", url: "/trace/transaction", icon: <ProfileOutlined/>},
    {label: "Event", url: "/trace/event", icon: <FileTextOutlined/>},
    {label: "Exception", url: "/trace/exception", icon: <ExclamationCircleOutlined/>},
    {
        label: "SOA", icon: <ApiOutlined/>, children: [
            {label: "Provider", url: "/trace/soa/provider", icon: <ApiOutlined/>},
            {label: "Consumer", url: "/trace/soa/consumer", icon: <ApiOutlined/>},
        ]
    },
    {label: "URL", url: "/trace/url", icon: <LinkOutlined/>},
    {label: "JVM", url: "/trace/jvm", icon: <HddOutlined/>},
];

// 「搜索」侧栏
export const SEARCH_SIDER: MenuItem[] = [
    {label: "链路搜索", url: "/search/request", icon: <PartitionOutlined/>},
    {label: "订单搜索", url: "/search/order", icon: <PartitionOutlined/>},
];

// 「设置」侧栏
export const SETTING_SIDER: MenuItem[] = [
    {label: "数据源", url: "/setting/datasource", icon: <DatabaseOutlined/>},
    {label: "监控项", url: "/setting/entity", icon: <MonitorOutlined/>},
];

// 「Open API」侧栏
export const OPENAPI_SIDER: MenuItem[] = [
    {label: "申请与查看", url: "/token/apply", icon: <DatabaseOutlined/>},
    {isAdmin: true, label: "管理", url: "/token/manage", icon: <TeamOutlined/>},
];
