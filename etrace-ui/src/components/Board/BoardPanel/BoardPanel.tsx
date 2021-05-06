import {Link} from "react-router-dom";
import {Board} from "$models/BoardModel";
import Exception from "$components/Exception/Exception";
import React from "react";
import {EMonitorSection} from "$components/EMonitorLayout";
import {Button, Collapse} from "antd";

import {CaretRightOutlined} from "@ant-design/icons";

import "./BoardPanel.less";
import BoardRow from "$components/Board/BoardPanel/BoardRow";

/**
 * 看板视图
 */
const BoardPanel: React.FC<{
    board?: Board;
}> = props => {
    const {board} = props;

    if (!board) {
        return null;
    }

    if (
        !board.layout ||
        board.layout.length === 0 ||
        (board.layout.length === 1 && board.layout[0].panels.length === 0)
    ) {
        return (
            <Exception
                type="202"
                desc="面板没有添加指标"
                actions={(
                    <div>
                        <Button htmlType="button">
                            <Link to={`/board/edit/${board.id}`}>编辑面板</Link>
                        </Button>
                    </div>
                )}
            />
        );
    }

    return (
        <EMonitorSection>
            {/* 遍历「行」 */}
            {board.layout.map((row, index) => (
                <EMonitorSection.Item key={index}>
                    {row.titleShow
                        ? (
                            <Collapse
                                className="board-view__chart-collapse"
                                defaultActiveKey={row.defaultFold ? [] : [0]}
                                expandIcon={({isActive}) => <CaretRightOutlined rotate={isActive ? 90 : 0}/>}
                            >
                                <Collapse.Panel
                                    key={0}
                                    className="board-view__chart-collapse-panel"
                                    header={<span className="chart-collapse-panel__title">{row.title || "新的行"}</span>}
                                    extra={<span className="chart-collapse-panel__summary">点击展开 / 关闭</span>}
                                >
                                    <BoardRow panels={row.panels} height={row.chartHeight}/>
                                </Collapse.Panel>
                            </Collapse>
                        )
                        : (
                            <BoardRow panels={row.panels} height={row.chartHeight}/>
                        )}
                </EMonitorSection.Item>
            ))}
        </EMonitorSection>
    );
};

export default BoardPanel;
