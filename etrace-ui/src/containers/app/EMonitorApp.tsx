import React, {ReactNode} from "react";
import {observer} from "mobx-react";
import {Prompt} from "react-router";
import StoreManager from "$store/StoreManager";
import IconFont from "$components/Base/IconFont";
import EMonitorNav from "$components/Nav/EMonitorNav";
import LandingPage from "$containers/LandingPage/LandingPage";
import {EMonitorContainer} from "$components/EMonitorLayout";
import ChartSingleView from "$containers/chart/ChartSingleView";
import CallstackPage from "$components/Callstack/CallstackPage";
import {Redirect, Route, Switch, useHistory} from "react-router-dom";
import useWatchChartSeriesClick from "$hooks/useWatchChartSeriesClick";

/* Entrance */
import BoardEntrance from "$containers/Board/BoardEntrance";
import TraceEntrance from "$containers/Trace/TraceEntrance";
import SearchEntrance from "$containers/TraceSearch/SearchEntrance";
import SettingEntrance from "$containers/Setting/SettingEntrance";
import OpenAPIEntrance from "$containers/OpenAPI/OpenAPIEntrance";
import {PageSwitchStore} from "$store/PageSwitchStore";

export const RT = (<IconFont type="icon-pingjunxiangyingshijian"/>);
export const COUNT = (<IconFont type="icon-fangwencishu"/>);
export const SIZE = (<IconFont type="icon-size"/>);

export function buildAppIdNotFoundTooltipTitle(appId: string): ReactNode {
    return <>未在<a href="http://eapp.tools.elenet.me" target="_blank" rel="noopener noreferrer"> EApp 系统</a>上找到<b> [{appId}]</b><br/>请走<a href="http://estart.tools.elenet.me" target="_blank" rel="noopener noreferrer">申请流程</a>，之后会定期同步。</>;
}

const EMonitorApp: React.FC = props => {
    const {pageSwitchStore} = StoreManager;

    useWatchChartSeriesClick();

    const history = useHistory();

    // 判断是否 sso 登录
    const {search} = window.location;
    const params = new URLSearchParams(search);
    const state = params.get("state");
    const code = params.get("code");
    const redirect = state === "true" ? "/" : state;

    const isHome = history.location.pathname === "/";

    const nav = isHome
        ? <EMonitorNav menu={false} search={false} logo={false} background={false}/>
        : <EMonitorNav />;

    return (
        <EMonitorContainer header={nav} fullscreen={true} headerFixed={isHome}>
            <Prompt message={PageSwitchStore.MSG_LEAVE_WITHOUT_SAVE} when={pageSwitchStore.promptSwitch}/>
            <Switch>
                {/* 子模块 */}

                {/* 看板 */}
                <Route path="/board" exact={false} component={BoardEntrance}/>
                {/* 应用 */}
                <Route path="/trace" exact={false} component={TraceEntrance}/>
                {/* 搜索 */}
                <Route path="/search" exact={false} component={SearchEntrance}/>
                {/* 设置 */}
                <Route path="/setting" exact={false} component={SettingEntrance}/>
                {/* OpenAPI */}
                <Route path="/token" exact={false} component={OpenAPIEntrance}/>

                {/* 单页功能 */}

                {/* Chart 查看（通过 URL 分享） */}
                <Route path="/chart/:chartId" component={ChartSingleView}/>
                {/* 链路查询页面 */}
                <Route path="/requestId/:requestId" exact={true} component={CallstackPage}/>
                {/* 墨子跳转 */}
                {code && state && redirect && <Redirect to={redirect}/>}

                {/* 欢迎页 */}
                <Route path="/" exact={true} component={LandingPage}/>
                <Redirect to="/"/>
            </Switch>
        </EMonitorContainer>
    );
};

export default observer(EMonitorApp);