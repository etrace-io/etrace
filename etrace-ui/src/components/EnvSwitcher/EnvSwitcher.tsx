import React from "react";
import classNames from "classnames";
import {SystemKit, ToolKit} from "$utils/Util";
import {Dropdown, Menu} from "antd";
import {CURR_API} from "$constants/API";
import {EMONITOR_URL, ENV_COLOR, SUPPORT_ENV} from "$constants/Env";

import "./EnvSwitcher.less";

const EnvSwitcher: React.FC = props => {
    const handleEnvChange = (e: any) => {
        const url = EMONITOR_URL[e.key];
        const target = SystemKit.redirectToOtherOrigin(url);
        if (target) {
            window.open(target, "_blank");
        }
    };

    const envMenu = <Menu onClick={handleEnvChange}>
        {SUPPORT_ENV.map(env => (
            <Menu.Item key={env} style={{color: ENV_COLOR[env]}}>{ToolKit.firstUpperCase(env)}</Menu.Item>
        ))}
    </Menu>;

    const classString = classNames("env-switcher", CURR_API.env.toLowerCase());

    return <Dropdown overlay={envMenu}>
        <div className={classString}>{CURR_API.env}</div>
    </Dropdown>;
};

export default EnvSwitcher;
