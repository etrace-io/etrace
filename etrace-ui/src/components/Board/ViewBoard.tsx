import {Row} from "antd";
import React from "react";
import {Board} from "$models/BoardModel";
import EditableChart from "../EMonitorChart/Chart/EditableChart/EditableChart";

const classNames = require("classnames");

interface ViewBoardProps {
    board: Board;
    viewBoardClass?: any;
    awaitLoad?: boolean;
}

const ViewBoard: React.FC<ViewBoardProps> = props => {
    const {board, viewBoardClass, awaitLoad} = props;

    const clsName = classNames({
        "e-monitor-content-section": true,
        "space-between": true,
        ...viewBoardClass
    });

    return (
        <Row className={clsName}>
            {board.layout && board.layout.map((row) => (
                row.panels
                    ? row.panels.map(({globalId, span}, index) => (
                        <EditableChart
                            key={index}
                            awaitLoad={awaitLoad}
                            globalId={globalId}
                            span={span || 8}
                        />
                    ))
                    : null
            ))}
        </Row>
    );
};

export default ViewBoard;
