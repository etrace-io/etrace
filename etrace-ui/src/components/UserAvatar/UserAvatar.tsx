import useUser from "$hooks/useUser";
import {Link} from "react-router-dom";
import React, {useState} from "react";
import {Avatar, Dropdown, Menu} from "antd";
import {SystemKit, UserKit} from "$utils/Util";
import SettingDrawer from "$components/PersonalSetting/SettingDrawer";
import {CoffeeOutlined, LoginOutlined, SettingOutlined} from "@ant-design/icons/lib";

import "./UserAvatar.less";

const UserAvatar: React.FC = props => {
    const user = useUser();

    const [collapse, setCollapse] = useState<boolean>(false);

    const login = () => {
        SystemKit.redirectToLogin(window.location.pathname + window.location.search);
    };

    const isLogin = !UserKit.isVisitor(user);

    const settingMenu = isLogin
        ? <Menu>
            <Menu.ItemGroup title={`${user.psnname}，你好`}>
                <Menu.Item
                    key="setting"
                    onClick={() => setCollapse(true)}
                    icon={<SettingOutlined />}
                >设置
                </Menu.Item>
                <Menu.Item key="token" icon={<CoffeeOutlined />}>
                    <Link to="/token/apply">Open API</Link>
                </Menu.Item>
            </Menu.ItemGroup>
        </Menu>
        : <Menu><Menu.Item key="login" onClick={login}><LoginOutlined />登录</Menu.Item></Menu>;

    const avatar = isLogin
        ? <Avatar
            className="emonitor-avatar"
            src={"//faas.ele.me/api/user/avatar/" + user.aliEmail}
            alt={user.psnname}
        />
        : <Avatar className="emonitor-avatar">{user.psnname}</Avatar>;

    return (
        <div className="emonitor-avatar-container">
            <Dropdown overlay={settingMenu} placement="bottomRight">
                {avatar}
            </Dropdown>

            <SettingDrawer collapse={collapse} onClose={() => setCollapse(false)} />
        </div>
    );
};

export default UserAvatar;
