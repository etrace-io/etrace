import React from "react";
import EMonitorLogo from "$components/Base/EMonitorLogo";
import {EMonitorHeader} from "$components/EMonitorLayout";
import UserAvatar from "$components/UserAvatar/UserAvatar";
import EnvSwitcher from "$components/EnvSwitcher/EnvSwitcher";
import {EMONITOR_LOGO_DARK, EMONITOR_LOGO_LIGHT} from "$constants/index";

const YpNav: React.FC<{
    homeLink?: string; // 首页链接
}> = props => {
    const {homeLink} = props;

    const logo = <EMonitorLogo height={46} link={homeLink || "/"} dark={EMONITOR_LOGO_DARK} light={EMONITOR_LOGO_LIGHT}/>;

    return (
        <EMonitorHeader background={false} logo={logo}>
            {/* 环境切换 */}
            <EnvSwitcher/>
            {/* 头像及下拉菜单 */}
            <UserAvatar/>
        </EMonitorHeader>
    );
};

export default YpNav;
