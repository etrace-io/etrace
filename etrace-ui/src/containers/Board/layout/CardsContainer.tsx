import {DeleteOutlined, SettingOutlined} from "@ant-design/icons/lib";
import React from "react";

import CardComponent from "./DraggableCard";
import {Draggable} from "react-beautiful-dnd";
import {Button, Card, Col, Row, Tooltip} from "antd";

const ButtonGroup = Button.Group;

interface CardsContainerProps {
    item: any;
    rowIdx: any;
    moveCard: any;
    moveOut: any;
    editRow: any;
    moveRow: any;
    dragItem: any;
    setDragItem: any;
    findChart: any;
    connectDropTarget?: any;
    connectDragSource?: any;
    form?: any;
    changeSpan?: any;
    removeChart?: any;
}

interface CardsContainerStatus {
}

export default class CardsContainer extends React.Component<CardsContainerProps, CardsContainerStatus> {
    constructor(props: CardsContainerProps) {
        super(props);
        this.state = {
            settingModal: false,
        };
    }

    // 渲染其中指标
    renderCards() {
        const {item, rowIdx, moveCard, dragItem, setDragItem, changeSpan, removeChart} = this.props;
        if (!item.panels) {
            item.panels = [];
        }

        // 渲染已放置的（可拖拽）指标
        const items = item.panels.map((card, i) => {
            return (
                <Col key={card.globalId} span={card.span}>
                    <CardComponent
                        moveCard={moveCard}
                        rowIdx={rowIdx}
                        colIdx={i}
                        item={card}
                        type="ref"
                        selecting={false}
                        findChart={this.props.findChart}
                        dragItem={dragItem}
                        setDragItem={setDragItem}
                        changeSpan={changeSpan}
                        removeChart={removeChart}
                    />
                </Col>
            );
        });

        // 添加新的可放置区域
        items.push(
            <Col span={8} key={-1}>
                <CardComponent
                    moveCard={moveCard}
                    rowIdx={rowIdx}
                    colIdx={-1}
                    type="placeholder"
                    item={{}}
                    selecting={false}
                    findChart={this.props.findChart}
                    dragItem={dragItem}
                    setDragItem={setDragItem}
                    removeChart={removeChart}
                />
            </Col>
        );

        // 布局
        return items;
    }

    render() {
        const {item, rowIdx, moveOut, editRow} = this.props;
        return (
            <Draggable key={rowIdx} draggableId={"CardsContainer-" + rowIdx} index={rowIdx}>
                {(innerProvided, innerSnapshot) => (
                    <div
                        ref={innerProvided.innerRef}
                        {...innerProvided.draggableProps}
                        className="draggable-card-container"
                    >
                        <Card
                            size="small"
                            title={(
                                <div {...innerProvided.dragHandleProps}>{item.title || "新的行"}</div>
                            )}
                            extra={
                                <ButtonGroup size="small">
                                    <Tooltip title="编辑行">
                                        <Button
                                            icon={<SettingOutlined />}
                                            key="edit"
                                            onClick={() => editRow(rowIdx)}
                                        />
                                    </Tooltip>
                                    <Tooltip title="删除行">
                                        <Button
                                            danger={true}
                                            icon={<DeleteOutlined />}
                                            key="delete"
                                            onClick={() => moveOut(rowIdx)}
                                        />
                                    </Tooltip>
                                </ButtonGroup>
                            }
                        >
                            <Row gutter={6}>
                                {this.renderCards()}
                            </Row>
                        </Card>
                    </div>
                )}
            </Draggable>
        );
    }
}
