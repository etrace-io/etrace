import React from "react";
import {Menu} from "antd";
import Icon from "@ant-design/icons";
import {Link, withRouter} from "react-router-dom";
import {RouteComponentProps} from "react-router";

const SubMenu = Menu.SubMenu;

interface NavMenuProps  extends RouteComponentProps<any> {
    menuData: any;
    Authorized?: any;
    isMobile?: boolean;
    onCollapse?: any;
    logo?: any;
    collapsed?: any;
    mode: any;
    openKeys?: Array<string>;
}

interface NavMenuState {
    openKeys: any;
    menus: any;
}

class NavMenu extends React.Component<NavMenuProps, NavMenuState> {
    // menus;

    constructor(props: NavMenuProps) {
        super(props);
        // this.menus = this.props.menuData;
        this.state = {
            menus: props.menuData,
            openKeys: null,
        };
        // todo
        // this.getDefaultKeys(props);
    }

    componentWillReceiveProps(nextProps: any) {
        this.setState(
            {
                menus: nextProps.menuData,
                openKeys: this.getDefaultCollapsedSubMenus(nextProps),
            }
        );
    }

    getDefaultKeys = (props: any) => {
        let keys = [];
        props.menuData.forEach(e => {
            if (e.children) {
                keys.push(e.name);
            }
        });
        return keys;
    };

    getDefaultCollapsedSubMenus(props: any) {
        const {location: {pathname}} = props || this.props;
        const snippets = pathname.split("/").slice(1, -1);
        const currentPathSnippets = snippets.map((item, index) => {
            const arr = snippets.filter((_, i) => i <= index);
            return arr.join("/");
        });
        let currentMenuSelectedKeys = [];
        currentPathSnippets.forEach((item) => {
            currentMenuSelectedKeys = currentMenuSelectedKeys.concat(this.getSelectedMenuKeys(item));
        });
        if (currentMenuSelectedKeys.length === 0) {
            return [""];
        }
        return currentMenuSelectedKeys;
    }

// Allow menu.js config icon as string or ReactNode
//   icon: 'setting',
//   icon: 'http://demo.com/icon.png',
//   icon: <Icon type="setting" />,
    getIcon(icon: any) {
        if (typeof icon === "string" && icon.indexOf("http") === 0) {
            return <img src={icon} alt="icon"/>;
        }
        if (typeof icon === "string") {
            return <Icon type={icon}/>;
        }
        return icon;
    }

    getNavMenuItems(menusData: any) {
        if (!menusData) {
            return [];
        }
        return menusData.map((item) => {
            if (!item.name || item.hideInMenu) {
                return null;
            }
            const ItemDom = this.getSubMenuOrItem(item);
            return this.checkPermissionItem(item.authority, ItemDom);
        });
    }

    // permission to check
    checkPermissionItem(authority: any, ItemDom: any) {
        if (this.props.Authorized && this.props.Authorized.check) {
            const {check} = this.props.Authorized;
            return check(
                authority,
                ItemDom
            );
        }
        return ItemDom;
    }

    /**
     * get SubMenu or Item
     */
    getSubMenuOrItem(item: any) {
        if (item.children && item.children.some(child => child.name)) {
            return (
                <SubMenu
                    className="monitor-sub-menu"
                    title={
                        item.icon ? (
                            <span>{this.getIcon(item.icon)}<span>{item.name}</span></span>
                        ) : item.name
                    }
                    key={item.name}
                    disabled={item.disabled}
                >
                    {this.getNavMenuItems(item.children)}
                </SubMenu>
            );
        } else {
            return ([
                (
                    <Menu.Item
                        key={item.key || item.path}
                        className="monitor-menu"
                        disabled={item.disabled}
                    >
                        {this.getMenuItemPath(item)}
                    </Menu.Item>)
            ]);
        }
    }

    // conversion Path
    // 转化路径
    conversionPath(path: string) {
        if (path && path.indexOf("http") === 0) {
            return path;
        } else {
            return `/${path || ""}`.replace(/\/+/g, "/");
        }
    }

    /**
     * 判断是否是http链接.返回 Link 或 a
     * Judge whether it is http link.return a or Link
     * @memberof SiderMenu
     */
    getMenuItemPath(item: any) {
        const itemPath = this.conversionPath(item.path);
        const icon = this.getIcon(item.icon);
        const {target, name} = item;
        // Is it a http link
        if (/^https?:\/\//.test(itemPath)) {
            return (
                <a href={item.params ? itemPath + item.params : itemPath} target={target}>
                    {icon}<span>{name}</span>
                </a>
            );
        }
        return (
            <Link
                to={item.params ? itemPath + item.params : itemPath}
                target={target}
                replace={itemPath === this.props.location.pathname}
                onClick={this.props.isMobile ? () => {
                    this.props.onCollapse(true);
                } : undefined}
            >
                {icon}<span>{name}</span>
            </Link>
        );
    }

    /**
     * 获得菜单子节点
     * @memberof SiderMenu
     */
    getNavMenuItem(menusData: any) {
        if (!menusData) {
            return [];
        }
        return menusData.map((item) => {
            if (!item.name || item.hideInMenu) {
                return null;
            }
            const ItemDom = this.getSubMenuOrItem(item);
            return this.checkPermissionItem(item.authority, ItemDom);
        });
    }

    getFlatMenuKeys(menus: any) {
        let keys = [];
        menus.forEach((item) => {
            if (item.children) {
                keys.push(item.path);
                keys = keys.concat(this.getFlatMenuKeys(item.children));
            } else {
                keys.push(item.path);
            }
        });
        return keys;
    }

    getSelectedMenuKeys(path: any) {
        const flatMenuKeys = this.getFlatMenuKeys(this.state.menus);
        if (flatMenuKeys.indexOf(path.replace(/^\//, "")) > -1) {
            return [path.replace(/^\//, "")];
        }
        if (flatMenuKeys.indexOf(path.replace(/^\//, "").replace(/\/$/, "")) > -1) {
            return [path.replace(/^\//, "").replace(/\/$/, "")];
        }
        return flatMenuKeys.filter((item) => {
            const itemRegExpStr = `^${item.replace(/:[\w-]+/g, "[\\w-]+")}$`;
            const itemRegExp = new RegExp(itemRegExpStr);
            return itemRegExp.test(path.replace(/^\//, "").replace(/\/$/, ""));
        });
    }

    render() {
        const {location: {pathname}, menuData} = this.props;
        let selectedKeys = this.getSelectedMenuKeys(pathname);
        const defaultOpenKeys = menuData ? menuData.filter(i => i.children).map(i => i.name) : [];
        return (
            <Menu
                mode={this.props.mode}
                selectedKeys={selectedKeys}
                style={{width: "100%"}}
                defaultOpenKeys={defaultOpenKeys}
            >
                {this.getNavMenuItems(this.state.menus)}
            </Menu>
        );
    }
}

export default withRouter(NavMenu);
