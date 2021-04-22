import React from "react";
import {Layout, Menu} from "antd";
import classNames from "classnames";
import NavAvatar from "../UserAvatar/UserAvatar";
import HelpLink from "./HelpLink";
import EnvSwitcher from "../EnvSwitcher/EnvSwitcher";
import {withRouter} from "react-router-dom";
import GlobalSearchBox from "../GlobalSearchBox/GlobalSearchBox";
import {RouteComponentProps} from "react-router";
import EMonitorLogo from "$components/Base/EMonitorLogo";
import {GLOBAL_SEARCH_PAGE} from "$constants/Route";
import {SEARCH_KEY} from "$constants/index";

const Header = Layout.Header;

interface NavWithSearchProps  extends RouteComponentProps<any> {
    className?: string;
    search?: boolean;       // 默认 true
    logo?: boolean;         // 默认 true
    background?: boolean;   // 默认 false
    searchBoxClassName?: string;
    notInMenu?: boolean;    // 默认 false
}

interface NavWithSearchState {
}

class NavWithSearch extends React.Component<NavWithSearchProps, NavWithSearchState> {
    handlerSearch = (value: string) => {
        // this.urlParamStore.changeURLParams({[SEARCH_KEY]: value}, [SEARCH_TYPE]);
        if (!value) {
            return;
        }
        const url = `${GLOBAL_SEARCH_PAGE}?${SEARCH_KEY}=${value}`;

        this.props.history.push(url);
    };

    /**
     * Search Box 回车键 press 句柄
     */
    handlerSearchPressEnter = (value: string) => {
        if (!value) {
            return;
        }
        const url = `${GLOBAL_SEARCH_PAGE}?${SEARCH_KEY}=${value}`;

        this.props.history.push(url);
    };

    render() {
        const {search, logo, background, className, searchBoxClassName, notInMenu} = this.props;

        const navClassString = classNames(
            {
                "e-monitor-header": true,
                "no-bg": !background
            },
            className,
        );

        const searchBoxClassString = classNames(
            {
                "e-monitor-menu-no-hover": true,
            },
            searchBoxClassName,
        );

        const searchBox = (
            <GlobalSearchBox
                size="middle"
                needLink={true}
                onSelect={this.handlerSearch}
            />
        );

        return (
            <Header className={navClassString}>
                {logo !== false && !notInMenu && (<EMonitorLogo height={46}/>)}
                {search !== false && notInMenu && (
                    <div className={searchBoxClassName}>
                        {logo !== false && (
                            <div className="e-monitor-nav-logo__container">
                                <EMonitorLogo height={46} link="/"/>
                            </div>
                        )}
                        {searchBox}
                    </div>
                )}

                <Menu className="e-monitor-header__menu" mode="horizontal">
                    {/* 搜索框 */}
                    {search !== false && !notInMenu && (
                        <Menu.Item className={searchBoxClassString}>
                            {searchBox}
                        </Menu.Item>
                    )}
                </Menu>

                <div className="e-monitor-header__functions">
                    {/* 环境切换 */}
                    <EnvSwitcher/>
                    {/* 帮助文档 */}
                    <HelpLink/>
                    {/* 头像及下拉菜单 */}
                    <NavAvatar/>
                </div>
            </Header>
        );
    }
}

export default withRouter(NavWithSearch);
