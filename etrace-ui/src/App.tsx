import {message} from "antd";
import {UserContext} from "./Context";
import {Route} from "react-router-dom";
import React, {useEffect} from "react";
import useLogin from "$hooks/useLogin";
import {changeThemeTo} from "$utils/Theme";
import {LocalStorageKit} from "$utils/Util";
import StoreManager from "$store/StoreManager";
import {BOARD_APP_VIEW} from "$constants/Route";
import AuthRoute from "$components/Auth/AuthRoute";
import {EMONITOR_DARK_THEME_TIP} from "$constants/index";
import EMonitorMeta from "$components/Base/EMonitorMeta";
import {ConsumerModal} from "$components/Callstack/Consumers";
import ChartTooltip from "$components/Chart/ChartTooltip/ChartTooltip";
import EventsPlotLineModal from "$components/Callstack/EventsPlotLineModal";
import SamplingModal from "$components/Callstack/SamplingModal/SamplingModal";
import {FullScreenBoardView} from "$components/FullViewBoard/FullViewBoard";

/* Page */
import GlobalSearch from "$containers/GlobalSearchPage/GlobalSearchPage";
import ShareBoard from "$containers/Board/ShareBoard";
import YellowPages from "$containers/YellowPages/YellowPages";
import LoginErrorPage from "$containers/Login/LoginErrorPage";
import YpRecordListPage from "$containers/YellowPages/YPRecordListPage";
import YpRecordInfoPage from "$containers/YellowPages/YPRecordInfoPage";
import YpSearchResultPage from "$containers/YellowPages/YPSearchResultPage";

// App
import EMonitorApp from "$containers/app/EMonitorApp";
import BoardApp from "$containers/app/BoardApp/BoardApp";

const MonitorApp: React.FC = props => {

    // 登录
    const {
        loading, user: currUser, error, authorized, backURL,
    } = useLogin(user => {
        // 获取用户信息成功回调
        if (user) {
            StoreManager.monitorEntityStore.loadEntities().then();
            StoreManager.userStore.setUser(user);
            if (
                StoreManager.userStore.getTheme() === "Dark" &&
                !LocalStorageKit.getValue(EMONITOR_DARK_THEME_TIP)
            ) {
                message.warn("黑色主题开发中，请等待后续更新~", 5);
                LocalStorageKit.setValue(EMONITOR_DARK_THEME_TIP, "true");
            }
            changeThemeTo("Light");
        }
    });

    // 用于移除 Loading 页面
    useEffect(() => {
        if (!loading) {
            const loadingPage = document.getElementById("PageLoading");
            loadingPage && loadingPage.parentElement.removeChild(loadingPage);
        }
    }, [loading]);

    if (loading) {
        return null;
    }

    if (error) {
        return <LoginErrorPage error={error}/>;
    }

    return (
        <UserContext.Provider value={currUser}>
            <EMonitorMeta/>

            <AuthRoute authorized={authorized} back={backURL}>
                {/* 各大应用（可作为单独页面运行）*/}

                {/* 看板应用 */}
                <Route path={`${BOARD_APP_VIEW}/:dataAppId`} component={BoardApp}/>
                {/* 全屏看板 */}
                <Route path="/purity/board/:globalId" component={FullScreenBoardView}/>
                {/* 全局搜索结果页 */}
                <Route path="/query" exact={false} component={GlobalSearch}/>
                {/* 外嵌看板 */}
                <Route path="/board/share" exact={false} component={ShareBoard}/>

                {/* TODO: 黄页功能 */}
                <Route path="/yellow-pages" exact={true} component={YellowPages}/>
                <Route path="/yellow-pages/record/:recordId" exact={true} component={YpRecordInfoPage}/>
                <Route path="/yellow-pages/list/:listId" exact={true} component={YpRecordListPage}/>
                <Route path="/yellow-pages/search" exact={true} component={YpSearchResultPage}/>

                {/* EMonitor 主应用 */}
                <Route path="/" exact={false} component={EMonitorApp}/>
            </AuthRoute>

            <SamplingModal/>
            <ConsumerModal/>
            <EventsPlotLineModal/>

            <ChartTooltip/>
        </UserContext.Provider>
    );
};

export default MonitorApp;