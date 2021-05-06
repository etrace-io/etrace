import get from "lodash/get";
import React, {useRef} from "react";
import {Board} from "$models/BoardModel";
import {Chart} from "$models/ChartModel";
import EMonitorMeta from "$components/Base/EMonitorMeta";
import {EMonitorSection} from "$components/EMonitorLayout";
import BoardPanel from "$components/Board/BoardPanel/BoardPanel";
import BoardChartSingleView from "$components/Board/BoardPanel/BoardChartSingleView";
import BoardToolBar, {BoardToolBarProps} from "$components/Board/BoardPanel/BoardToolBar";

interface BoardViewProps extends BoardToolBarProps {
    board: Board;
    singleChart?: Chart;
    hideToolsBar?: boolean;
}

const BoardView: React.FC<BoardViewProps> = props => {
    const {board, hideToolsBar, singleChart, ...toolBarOptions} = props;

    const container = useRef<HTMLDivElement>();

    return (
        <EMonitorSection fullscreen={true} ref={container} className="board-panel">
            <EMonitorMeta title={get(board, "title", "")}/>

            {/* 工具栏 */}
            {!hideToolsBar && (
                <EMonitorSection.Item>
                    <BoardToolBar {...toolBarOptions} board={board} fullScreenTarget={container}/>
                </EMonitorSection.Item>
            )}

            <EMonitorSection scroll={true}>
                {singleChart
                    ? <BoardChartSingleView chart={singleChart}/> // 渲染
                    : <BoardPanel board={board}/>}
            </EMonitorSection>

        </EMonitorSection>
    );
};

export default React.memo(BoardView);
