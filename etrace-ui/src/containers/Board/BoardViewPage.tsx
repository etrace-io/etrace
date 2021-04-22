import useBoard from "$hooks/useBoard";
import React, {useEffect, useRef, useState} from "react";
import StoreManager from "$store/StoreManager";
import * as notification from "$utils/notification";
import * as BoardService from "$services/BoardService";
import * as ChartService from "$services/ChartService";
import {useHistory, useParams} from "react-router-dom";
import Exception from "../../components/Exception/Exception";
import {BOARD_VIEW, FULL_SCREEN_CHART} from "$constants/Route";

import "./ViewBoard.css";
import {reaction} from "mobx";
import {Chart} from "$models/ChartModel";
import BoardView from "$components/Board/BoardView";
import {BoardToolBarProps} from "$components/Board/BoardPanel/BoardToolBar";

interface BoardViewPageProps extends Omit<BoardToolBarProps, "board"> {
    hideToolsBar?: boolean;
}

const BoardViewPage: React.FC<BoardViewPageProps> = props => {
    const {hideToolsBar, ...toolBarOptions} = props;
    const {boardStore, eventStore, urlParamStore} = StoreManager;

    const history = useHistory();
    const {boardId} = useParams();
    const board = useBoard();
    const currBoardId = useRef<number>();
    // const [board, setBoard] = useState<Board>(null);
    const [chart, setChart] = useState<Chart>(null);

    useEffect(() => {
        if (boardId) {
            setBoardIdFromUrl(boardId);
        } else {
            currBoardId.current = boardStore.board.id;
        }

        return () => {
            boardStore.unRegisterBoard();
            eventStore.clearAll();
        };
    }, [boardId]);

    useEffect(() => {
        const disposer = reaction(
            () => boardStore.chart,
            (c) => {
                setChart(c);
            }
        );
        return () => { disposer(); };
    }, []);

    const setBoardIdFromUrl = (id) => {
        // 切换看板时，删除报警事件 / 变更事件
        eventStore.clearAll();
        const globalId = urlParamStore.getValue("globalId");

        if (globalId) {
            BoardService.search({globalId, status: "Active"}).then((results: any) => {
                if (results.results.length > 0) {
                    history.replace({pathname: `${BOARD_VIEW}/${results.results[0].id}`});
                } else {
                    setBoardId(id);
                    notification.warningHandler({
                        message: "未在该环境找到 GlobalId 对应的配置",
                        description: "建议使用「同步」功能，从其他环境同步配置到当前环境", duration: 10
                    });
                }
            });
        } else {
            setBoardId(id);
            checkFullScreenChart();
        }
    };

    const setBoardId = id => {
        currBoardId.current = id;
        boardStore.setBoardId(id);
    };

    const checkFullScreenChart = () => {
        const fullScreenChart = urlParamStore.getValue(FULL_SCREEN_CHART);
        if (!fullScreenChart) {
            return;
        }
        // 仅在无 `globalId` 的场景，处理 `fullScreenChart`
        boardStore.setBoardId(currBoardId.current);
        ChartService.get(+fullScreenChart).then(fullViewChart);
    };

    const fullViewChart = (target: any) => {
        boardStore.setChart(target);
        urlParamStore.changeURLParams({ [ FULL_SCREEN_CHART ]: target?.id });
    };

    // const {board, chart} = boardStore;

    const error = boardStore.error;
    if (error) {
        return (<Exception title={error.status} desc={error.description}/>);
    }

    return (
        <BoardView
            {...toolBarOptions}
            board={board}
            hideToolsBar={hideToolsBar}
            singleChart={chart}
        />
    );
};

export default React.memo(BoardViewPage);
