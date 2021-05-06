import {reaction} from "mobx";
import {observer} from "mobx-react";
import classNames from "classnames";
import useUser from "$hooks/useUser";
import {UserKit} from "$utils/Util";
import {MenuItem} from "$models/Menu";
import {Layout, Menu, Space} from "antd";
import {getPrefixCls} from "$utils/Theme";
import StoreManager from "$store/StoreManager";
import IconFont from "$components/Base/IconFont";
import React, {useEffect, useState} from "react";
import {Link, useLocation} from "react-router-dom";
import EMonitorLogo from "$components/Base/EMonitorLogo";
import {LocalStorageUtil} from "$utils/LocalStorageUtil";

interface EMonitorHeaderProps {
    className?: string;
    logo?: React.ReactNode;
    home?: string; // 点击 Logo 跳转的地址
    title?: React.ReactNode; // Menu 前展示的内容
    menu?: MenuItem[] | null | false;
    selectedKeys?: string[];
    background?: boolean;   // 是否需要背景，除非指定 false，否则默认有背景
}

const Header: React.FC<EMonitorHeaderProps> = props => {
    const {
        className,
        menu,
        selectedKeys,
        logo,
        home,
        title,
        children,
        background,
    } = props;

    const user = useUser();

    const prefixCls = getPrefixCls("header");
    const classString = classNames(prefixCls, className, {
        "no-bg": background === false,
    });

    const generateMenuItem = (list: MenuItem[]) => {
        return list.map(item => {
            const { history: watch, url, isAdmin, label } = item;

            if (item.isExternal) {
                return <Menu.Item key={url}>
                    <a href={item.url} target="_blank" rel="noopener noreferrer">
                        <span>{item.label}</span>
                        <IconFont type="icon-caozuo-wailian" style={{fontSize: 12, opacity: 0.6, margin: "0 0 0 3px"}}/>
                    </a>
                </Menu.Item>;
            }

            const menuItem = item.children
                ? <Menu.SubMenu key={label} title={label}>{generateMenuItem(item.children)}</Menu.SubMenu>
                : <Menu.Item key={url}>{watch
                    ? <HistoryLink link={url} text={label} watch={watch.watch} storageKey={watch.storageKey} />
                    : <Link to={url}>{label}</Link>
                }</Menu.Item>;

            return ((isAdmin && UserKit.isAdmin(user)) || !isAdmin) ? menuItem : null;
        });
    };

    return (
        <Layout.Header className={classString}>
            {logo !== false && (logo || <EMonitorLogo height={46} link={home} />)}

            {title}

            {menu && (
                <Menu className="emonitor-header__menu" mode="horizontal" selectedKeys={selectedKeys}>
                    {generateMenuItem(menu)}
                </Menu>
            )}

            {children && <Space align="center" className="emonitor-header__functions">{children}</Space>}
        </Layout.Header>
    );
};

interface HistoryLinkProps {
    storageKey: string;
    link: string;
    text: string;
    watch: string;
}

const HistoryLink: React.FC<HistoryLinkProps> = observer(props => {
    const { watch, link, storageKey, text } = props;
    const history = LocalStorageUtil.getStringValues(storageKey);
    const defaultURL = history.length > 0 ? `${link}?${watch}=${history[ 0 ]}` : link;

    const [currLink, setCurrLink] = useState(defaultURL);

    const location = useLocation();

    useEffect(() => {
        return reaction(
            () => StoreManager.urlParamStore.getValue(watch),
            watchValue => {
                if (location.pathname.indexOf(link) === 0 && watchValue) {
                    setCurrLink(`${link}?${watch}=${watchValue}`);
                }
            });
    }, [watch]);

    return <Link to={currLink}>{text}</Link>;
});

export default Header;
