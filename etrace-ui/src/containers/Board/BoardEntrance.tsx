import {observer} from "mobx-react";
import ExplorerPage from "./Explorer";
import React, {useEffect} from "react";
import EditBoardAppPage from "./EditBoardApp";
import BoardAppListPage from "./BoardAppListPage";
import StoreManager from "../../store/StoreManager";
import BoardEditPage from "./editBoard/BoardEditPage";
import EMonitorMeta from "$components/Base/EMonitorMeta";
import {EMonitorPage} from "$components/EMonitorLayout";
import {Redirect, Route, Switch} from "react-router-dom";
import BoardViewPage from "$containers/Board/BoardViewPage";
import {BarsOutlined, StarOutlined,} from "@ant-design/icons/lib";
import {BOARD_APP_EDIT, BOARD_EDIT, BOARD_SIDER, BOARD_VIEW, METRIC_EDIT} from "$constants/Route";

import ChartListPage from "$containers/Board/ChartListPage";
import BoardListPage from "$containers/Board/BoardListPage";

const BoardEntrance: React.FC = props => {
    const { userActionStore } = StoreManager;

    useEffect(() => {
        userActionStore.loadUserAction().then();
    }, []);

    const sider = BOARD_SIDER.concat([{
        label: "我的收藏",
        icon: <StarOutlined />,
        children: userActionStore.favoriteBoards.map(board => ({
            label: board.title,
            url: `${BOARD_VIEW}/${board.id}`,
        })),
    }, {
        label: "最近浏览",
        icon: <BarsOutlined />,
        children: userActionStore.viewBoards.map(board => ({
            label: board.title,
            url: `${BOARD_VIEW}/${board.id}`,
        })),
    }]);

    return (
        <EMonitorPage sider={sider}>
            <EMonitorMeta title="看板" />

            <Switch>
                <Route path="/board/list" component={BoardListPage} />
                <Route path="/board/chart" component={ChartListPage} />
                <Route path="/board/app" exact={true} component={BoardAppListPage} />
                <Route path="/board/explorer" exact={true} component={ExplorerPage} />
                <Route path={`${METRIC_EDIT}/:chartId`} component={ExplorerPage} />
                <Route path={`${BOARD_VIEW}/:boardId`} exact={true} component={BoardViewPage} />
                <Route path={`${BOARD_EDIT}/:boardId`} component={BoardEditPage} />
                <Route path={`${BOARD_APP_EDIT}/:boardAppId`} component={EditBoardAppPage} />
                <Redirect to="/board/list" />
            </Switch>
        </EMonitorPage>
    );
};

export default observer(BoardEntrance);
