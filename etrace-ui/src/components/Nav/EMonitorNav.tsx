import {reaction} from "mobx";
import {observer} from "mobx-react";
import {MenuItem} from "$models/Menu";
import StoreManager from "$store/StoreManager";
import HelpLink from "$components/Nav/HelpLink";
import React, {useEffect, useState} from "react";
import NavAvatar from "$components/UserAvatar/UserAvatar";
import {useHistory, useLocation} from "react-router-dom";
import EventTimeLine from "$components/Nav/EventTimeLine";
import {EMonitorHeader} from "$components/EMonitorLayout";
import EnvSwitcher from "$components/EnvSwitcher/EnvSwitcher";
import {EMONITOR_ROUTER, GLOBAL_SEARCH_PAGE} from "$constants/Route";
import GlobalSearchBox from "$components/GlobalSearchBox/GlobalSearchBox";

import "./EMonitorNav.less";
import {SEARCH_KEY} from "$constants/index";

interface EMonitorNavProps {
    search?: boolean;       // 是否需要全局搜索功能，除非指定 false，否则默认有搜索功能
    background?: boolean;   // 是否需要背景，除非指定 false，否则默认有背景
    menu?: MenuItem[] | null | false; // 自定义 menu
    logo?: React.ReactNode;
}

const EMonitorNav: React.FC<EMonitorNavProps> = props => {
    const { search, background, logo, menu = EMONITOR_ROUTER, children } = props;
    const { eventStore } = StoreManager;

    const [showEvent, setShowEvent] = useState<boolean>(false);

    useEffect(() => {
        const disposer = reaction(
            () => [eventStore.allChangeEvents, eventStore.allAlertEvents],
            ([change, alert]) => setShowEvent(change.length > 0 || alert.length > 0)
        );
        return () => disposer();
    }, []);

    const location = useLocation();
    const history = useHistory();
    const selectedKeys = location.pathname
        .split("/")
        .map((v, idx, arr) => v ? arr.slice(0, idx + 1).join("/") : "/");

    const handleSearch = value => {
        if (!value) {
            return;
        }
        const url = `${GLOBAL_SEARCH_PAGE}?${SEARCH_KEY}=${value}`;
        history.push(url);
    };

    return (
        <EMonitorHeader
            logo={logo}
            background={background}
            home="/"
            menu={menu}
            selectedKeys={selectedKeys}
            title={children}
        >
            {/* 全局搜索 */}
            {search !== false && <GlobalSearchBox
              className="emonitor-nav-search-box"
              size="middle"
              focusClass="focused"
              dropdownStyle={{ width: "400px" }}
              needLink={true}
              dropdownMatchSelectWidth={false}
              onSelect={handleSearch}
            />}

            {/* 报警、变更信息 */}
            {showEvent && <EventTimeLine />}

            {/* 环境切换 */}
            <EnvSwitcher />

            {/* 帮助文档 */}
            <HelpLink />

            {/* 头像及下拉菜单 */}
            <NavAvatar />
        </EMonitorHeader>
    );
};

export default observer(EMonitorNav);
