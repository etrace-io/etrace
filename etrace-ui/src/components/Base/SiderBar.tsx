import React from "react";
import useUser from "$hooks/useUser";
import {MenuItem} from "$models/Menu";
import {Layout, Menu, Tooltip} from "antd";
import {ToolKit, UserKit} from "$utils/Util";
import IconFont from "$components/Base/IconFont";
import {Link, useLocation} from "react-router-dom";

export type MenuParams = {[key: string]: string[] | string | number};
export type MenuParamsFn = (url: string) => MenuParams;

const SiderBar: React.FC<{
    menu: MenuItem[];
    className?: string;
    menuParams?: MenuParams | MenuParamsFn;
}> = props => {
    const {menu, menuParams, className} = props;

    const handleSiderCollapse = (collapsed: boolean) => {
        const event = new UIEvent("resize");
        window.dispatchEvent(event);
    };

    return (
        <Layout.Sider
            theme="light"
            className={className}
            breakpoint="xl"
            collapsible={true}
            onCollapse={handleSiderCollapse}
            // collapsedWidth={60}
        >
            <PageSiderMenu menu={menu} params={menuParams}/>
        </Layout.Sider>
    );
};

const PageSiderMenu: React.FC<{
    menu?: MenuItem[];
    params?: MenuParams | MenuParamsFn;
}> = props => {
    const { menu, params } = props;

    const location = useLocation();
    const user = useUser();

    const selectedKeys = location.pathname
        .split("/")
        .map((v, idx, arr) => v ? arr.slice(0, idx + 1).join("/") : "/");

    // const externalTag = <img style={{float: "right", margin: "10px 0", opacity: 0.6}} width={22} height={22} src={EMONITOR_LOGO_LIGHT} alt="EMonitor" title="跳转老版 EMonitor"/>;
    const externalTag = <IconFont type="icon-caozuo-wailian" style={{fontSize: 12, opacity: 0.6, float: "right", margin: "15px 0"}}/>;

    const renderMenuItem = (list: MenuItem[]) => list.map(item => {
        if (item.isAdmin && !UserKit.isAdmin(user)) { return null; }

        return item.children
            ? <Menu.SubMenu key={item.url || item.label} icon={item.icon} title={item.label}>
                {renderMenuItem(item.children)}
            </Menu.SubMenu>
            : <Menu.Item key={item.url} icon={item.icon}>
                {item.isExternal
                    ? <Tooltip title="External Link" color="blue" placement="right"><a href={ToolKit.paramsToURLSearch(params, item.url)} target="_blank" rel="noopener noreferrer"><span>{item.label}</span>{externalTag}</a></Tooltip>
                    : <Link to={ToolKit.paramsToURLSearch(typeof params === "function" ? params(item.url) : params, item.url)}><span>{item.label}</span></Link>
                }
            </Menu.Item>;
    });

    const openKey = menu.map(i => i.url || i.label);

    return (
        <Menu
            mode="inline"
            defaultOpenKeys={openKey}
            selectedKeys={selectedKeys}
        >
            {renderMenuItem(menu)}
        </Menu>
    );
};

export default SiderBar;
