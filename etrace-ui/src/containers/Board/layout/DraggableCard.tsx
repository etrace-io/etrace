import {DeleteOutlined, EditOutlined, EyeOutlined} from "@ant-design/icons/lib";
import React from "react";
import {Button, Divider, Popconfirm, Popover, Select, Space, Tag, Tooltip} from "antd";
import {DragSource, DropTarget} from "react-dnd";
import {autobind} from "core-decorators";
import {Link} from "react-router-dom";
import ChartPreview from "$components/Chart/ChartPreview";

import "./DraggableCard.less";

let previous = null;
const cardSource = {
    beginDrag(props: any, monitor: any, component: any) {
        const {item, rowIdx, colIdx, type} = props;
        props.setDragItem(item, rowIdx);
        previous = null;
        return {item, rowIdx, colIdx, type};
    },
    endDrag(props: any, monitor: any) {
        previous = null;
        props.setDragItem(null, null);
    },
    canDrag(props: any, monitor: any) {
        return props.item != null;
    }
};

const cardTarget = {
    drop(props: any, monitor: any) {
        const dragItem = monitor.getItem();
        const type = dragItem.type;
        const {moveCard, rowIdx, colIdx, item} = props;

        if (type == "raw") {
            moveCard(type, rowIdx, colIdx);
        } else if (type !== "placeholder") {
            if (dragItem.item.chartId != item.chartId
                && (
                    previous == null ||
                    (previous.rowIdx != rowIdx && previous.colIdx != colIdx)
                )) {
                moveCard(type, rowIdx, colIdx);
                previous = props;
            }
        }
    }
};

interface CardComponentProps {
    setDragItem: any;
    rowIdx: any;
    moveCard?: any;
    connectDropTarget?: any;
    connectDragSource?: any;
    dragItem?: any;
    colIdx?: any;
    item?: any;
    findChart?: any;
    type?: any;
    form?: any;
    selecting: boolean;
    moveOut?: any;
    changeSpan?: any;
    removeChart?: any;
}

interface CardComponentStatus {
    settingModal?: boolean;
    viewChart?: boolean;
}

class CardComponent extends React.Component<CardComponentProps, CardComponentStatus> {
    constructor(props: CardComponentProps) {
        super(props);
        this.state = {
            settingModal: false,
            viewChart: false,
        };
    }

    getSpanOption() {
        let option = [];
        for (let i = 1; i <= 24; i++) {
            option.push((<Select.Option key={"" + i} value={i}>{i}</Select.Option>));
        }
        return (option);
    }

    @autobind
    chartModalOk() {
        this.setState({
            viewChart: false
        });
    }

    render() {
        const {
            connectDropTarget, connectDragSource, colIdx, rowIdx, item, findChart, type,
            selecting, changeSpan, removeChart
        } = this.props;
        let chart: any = {};
        if (type == "ref" && item) {
            chart = findChart(item.chartId, item.globalId);
        } else if (type == "raw") {
            chart = item;
        } else {
            chart = item;
        }
        if (!chart) {
            return null;
        }
        const popoverContent = chart.title
            ? (
                <React.Fragment>
                    <Space>
                        {!selecting && chart.title && (<>
                            <Tooltip key="span" title="选择占据格数，一行 24 格">
                                <Select
                                    size="small"
                                    dropdownMatchSelectWidth={false}
                                    value={item && item.span ? item.span : 8}
                                    onSelect={(value) => changeSpan(rowIdx, colIdx, value)}
                                >
                                    {this.getSpanOption()}
                                </Select>
                            </Tooltip>
                            <Button key="50%" size="small" onClick={() => changeSpan(rowIdx, colIdx, 12)}>50%</Button>
                            <Button key="100%" size="small" onClick={() => changeSpan(rowIdx, colIdx, 24)}>100%</Button>
                            <Divider key="vertical" type="vertical" />
                        </>)}

                        {chart.title && (
                            <Popconfirm
                                key={chart.title}
                                onConfirm={() => removeChart(chart.globalId, colIdx, rowIdx)}
                                okText="Yes"
                                cancelText="No"
                                title={"确定移除指标(" + chart.title + ")吗？"}
                            >
                                <Button size="small" type="primary" danger={true} icon={<DeleteOutlined />}/>
                            </Popconfirm>
                        )}
                        <Link to={"/board/explorer/edit/" + chart.id} target="_blank">
                            <Button size="small" icon={<EditOutlined />} type="primary"/>
                        </Link>
                        <Button size="small" icon={<EyeOutlined />} onClick={() => this.setState({viewChart: true})}/>
                    </Space>
                </React.Fragment>
            )
            : (<span>可拖拽指左侧指标到这里</span>);

        return connectDragSource(connectDropTarget(
            <div>
                {chart && (
                    <Popover
                        placement="top"
                        content={popoverContent}
                    >
                        <div className="draggable-card-item">
                            <div
                                style={{
                                    overflow: "hidden",
                                    display: "-webkit-inline-box",
                                    WebkitLineClamp: 1,
                                    WebkitBoxOrient: "vertical"
                                }}
                            >
                                {chart.title}
                                {/* 标识废弃指标 */}
                                {(chart.title && chart.status !== "Active") && (
                                    <Tag color="#ff4d4f" style={{marginLeft: 8}}>已废弃</Tag>
                                )}
                            </div>
                        </div>
                    </Popover>
                )}
                {this.state.viewChart && (
                    <ChartPreview chart={chart} visible={this.state.viewChart} onOk={this.chartModalOk}/>
                )}
            </div>
        ));
    }
}

export default DropTarget("card", cardTarget, connect => ({
    connectDropTarget: connect.dropTarget(),
}))(
    DragSource("card", cardSource, (connect, monitor) => ({
        connectDragSource: connect.dragSource(),
        isDragging: monitor.isDragging(),
    }))(CardComponent)
);
