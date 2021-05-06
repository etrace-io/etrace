import {FormInstance} from "antd/es/form";

import {toJS} from "mobx";
import {autobind} from "core-decorators";
import * as BoardService from "../../../services/BoardService";

import {Board, Layout, Panel} from "../../../models/BoardModel";

import CardComponent from "../layout/DraggableCard";
import CardsContainer from "../layout/CardsContainer";
import {SelectChartModal} from "./EditBoardSelectModalNew";
import {Button, Card, Checkbox, Col, Form, Input, InputNumber, Modal, Row} from "antd";

import {DragDropContext, Droppable} from "react-beautiful-dnd";
import {InfoCircleOutlined, PlusOutlined} from "@ant-design/icons/lib";
import StoreManager from "$store/StoreManager";
import React from "react";

const R = require("ramda");
const FormItem = Form.Item;

interface EditBoardLayoutProps {
}

interface EditBoardLayoutStatus {
    visible?: boolean;
    layout?: any;
    selectedChartModal?: boolean;
    settingModal?: boolean;
    selectedCharts?: Array<any>;
    rowIdx?: number;
}

// const EditBoardLayout: React.FC = props => {
//     const {boardStore} = StoreManager;
//
//     const [layout, setLayout] = useState<any>();
//     const [rowIdx, setRowIdx] = useState<number>();
//     const [visible, setVisible] = useState<boolean>();
//     const [settingModal, setSettingModal] = useState<boolean>();
//     const [selectedCharts, setSelectedCharts] = useState<any[]>();
//     const [selectedChartModal, setSelectedChartModal] = useState<boolean>();
// };

export default class EditBoardLayout extends React.Component<EditBoardLayoutProps, EditBoardLayoutStatus> {
    selectConfig;
    boardStore;
    chartGlobalId;
    col;
    dragItem = null;
    dragIndex = null;
    layoutForm = React.createRef<FormInstance>();

    constructor(props: any) {
        super(props);
        this.boardStore = StoreManager.boardStore;

        this.state = {
            selectedChartModal: false,
            settingModal: false,
            selectedCharts: [],
        };
    }

    renderSelectCharts() {
        if (!this.boardStore || !this.boardStore.board || !this.boardStore.board.charts) {
            return;
        }
        return this.boardStore.board.charts.map((card, i) => (
            <CardComponent
                key={i}
                moveCard={this.moveCard}
                rowIdx={-1}
                colIdx={i}
                item={card}
                type="raw"
                dragItem={this.dragItem}
                setDragItem={this.setDragItem}
                selecting={true}
                changeSpan={this.changeSpan}
                removeChart={this.deleteSelectedChart}
            />
        ));
    }

    deleteSelectedChartModal() {
        return (
            <Modal
                title="删除已选指标"
                visible={this.state.selectedChartModal}
                onCancel={() => this.setState({selectedChartModal: false})}
                onOk={this.deleteSelectedChartHandle}
                closable={false}
                style={{top: 20}}
            >
                <p><InfoCircleOutlined type="info-circle-o" style={{marginRight: "2px", color: "red"}}/>指标已经被使用，是否确定删除?</p>
            </Modal>
        );
    }

    @autobind
    deleteSelectedChartHandle() {
        const board: Board = this.boardStore.board;
        board.charts.splice(this.col, 1);
        board.chartIds = board.charts.map(item => {
            return item.id;
        });
        let layouts: Array<any> = board.layout;
        if (layouts) {
            for (let lay = 0; lay < layouts.length; lay++) {
                let layout = layouts[lay];
                if (layout.panels) {
                    let panels: Array<any> = [];
                    let length = layout.panels.length;
                    for (let index = 0; index < length; index++) {
                        if (layout.panels[index].globalId !== this.chartGlobalId) {
                            panels.push(layout.panels[index]);
                        }
                    }
                    layouts[lay].panels = panels;
                }
            }
            board.layout = layouts;
        }
        this.boardStore.setBoard(board);
        this.setState({selectedChartModal: false});
    }

    @autobind
    saveSelectedCharts(charts: Array<any>) {
        if (!charts || charts.length <= 0) {
            return;
        }

        const board: Board = this.boardStore.board;
        if (!board.charts) {
            board.charts = [];
        }
        charts.forEach(chart => {
            board.charts.push(chart);
        });
        const chartIds = board.charts.map(item => {
            return item.id;
        });
        board.chartIds = chartIds;
        this.boardStore.setBoard(board);
        this.saveBoardChartIds(board);
    }

    @autobind
    addNewRow() {
        const board: Board = this.boardStore.board;
        if (!board.layout) {
            board.layout = [];
        }
        board.layout.push({title: "", panels: []});
        console.log(board.layout);
        this.boardStore.setChangeBoard(board);
    }

    @autobind
    async saveBoardChartIds(board: any) {
        await BoardService.saveChartIds(board);
        this.setState({visible: false, selectedCharts: []});
    }

    usedSelectedChart() {
        let charts: Array<number> = [];
        const board: Board = this.boardStore.board;
        let layouts: Array<any> = board.layout;
        if (layouts) {
            for (let layout of layouts) {
                if (layout.panels) {
                    for (let panel of layout.panels) {
                        charts.push(panel.globalId);
                    }
                }
            }
        }
        return charts;
    }

    // render
    @autobind
    renderSettingModal() {
        let layout: any = this.state.layout;
        if (layout) {
            return (
                <Modal
                    style={{top: 40}}
                    closable={false}
                    title={layout.title}
                    visible={this.state.settingModal}
                    width={1000}
                    onOk={this.handleSubmit}
                    onCancel={this.handleCancel}
                >
                    <Form layout="inline" ref={this.layoutForm}>
                        <FormItem name="title" label="标题">
                            <Input placeholder="Input title"/>
                        </FormItem>
                        <FormItem name="titleShow" label="显示标题" valuePropName="checked">
                            <Checkbox/>
                        </FormItem>
                        <FormItem name="chartHeight" label="当前行内图表高度">
                            <InputNumber placeholder="默认 280" step={5} decimalSeparator="."/>
                        </FormItem>
                        <FormItem name="defaultFold" label="默认折叠当前行" valuePropName="checked">
                            <Checkbox/>
                        </FormItem>
                    </Form>
                </Modal>
            );
        }
    }

    @autobind
    handleCancel() {
        this.selectConfig = null;
        this.setState({
            settingModal: false,
        });
    }

    @autobind
    handleSubmit() {
        const form = this.layoutForm.current;
        form.validateFields().then(values => {
            const board = this.boardStore.getImmutableBoard();
            let rowIdx = this.state.rowIdx;
            const layouts: Array<any> = board.layout;
            let layout = layouts[rowIdx];
            layout.title = values.title;
            layout.chartHeight = values.chartHeight;
            layout.titleShow = values.titleShow;
            layout.defaultFold = values.defaultFold;
            this.boardStore.setChangeBoard(board);
            this.setState({settingModal: false});
        }).catch();
    }

    @autobind
    onDragEnd(result: any) {
        const {source, destination} = result;
        if (!destination) {
            return;
        }

        // 行与行之间的替换（CardContainer）
        if (destination.droppableId === "list" && source.droppableId === "list") {
            if (source.index !== destination.index) {
                const index = source.index;
                const item = this.boardStore.board.layout[index];
                this.setDragItem(item, index);
                this.moveRow(destination.index);
                this.setDragItem(null, null);
            }
        }
    }

    // 渲染当前可拖拽布局
    @autobind
    renderLayoutBoard() {
        if (!this.boardStore || !this.boardStore.board || !this.boardStore.board.layout) {
            return [];
        }

        return (
            // 提供拖拽上下文环境
            <DragDropContext onDragEnd={this.onDragEnd}>
                {/* 提供可放置区域，也就是 CardsContainer 的父容器 */}
                <Droppable droppableId="list">
                    {(provided, snapshot) => (
                        <div
                            ref={provided.innerRef}
                            style={{
                                maxHeight: 600,
                                minHeight: 100,
                                overflow: "auto"
                            }}
                        >
                            {/* 提供拖拽对象，也就是遍历渲染 CardsContainer */}
                            {this.boardStore.board.layout.map((row, index) => (
                                <CardsContainer
                                    key={index}
                                    item={row}
                                    rowIdx={index}
                                    moveOut={this.moveOut}
                                    editRow={this.editRow}
                                    moveCard={this.moveCard}
                                    moveRow={this.moveRow}
                                    setDragItem={this.setDragItem}
                                    dragItem={this.dragItem}
                                    findChart={this.findChart}
                                    changeSpan={this.changeSpan}
                                    removeChart={this.deleteSelectedLayoutChart}
                                />
                            ))}
                        </div>
                    )}
                </Droppable>
            </DragDropContext>
        );
    }

    // CardContainer 操作
    @autobind
    moveOut(rowIdx: number) {
        let board: Board = this.boardStore.board;
        const layout: Array<any> = board.layout;
        layout.splice(rowIdx, 1);
        board.layout = layout;
        this.boardStore.setChangeBoard(board);
    }

    @autobind
    editRow(rowIdx: number) {
        let board: Board = this.boardStore.board;
        const layouts: Array<any> = board.layout;
        let layout = layouts[rowIdx];
        setTimeout(() => {
            const form = this.layoutForm.current;
            form.setFieldsValue({
                title: layout.title,
                titleShow: layout.titleShow,
                defaultFold: layout.defaultFold,
                chartHeight: layout.chartHeight || 280
            });
        }, 0);
        this.setState({
            settingModal: true,
            layout: layout,
            rowIdx: rowIdx
        });
    }

    @autobind
    moveCard(type: any, hoverRow: number, hoverCol: number) {
        if (hoverRow === -1) {
            return;
        }
        const board = this.boardStore.getImmutableBoard();
        if (!board.layout) {
            board.layout = [];
        }
        if (type === "ref") {
            let dragRow = board.layout[hoverRow];
            let dragColIdx = R.findIndex(R.propEq("chartId", this.dragItem.chartId))(dragRow.panels);
            if (dragColIdx > -1) {
                dragRow.panels.splice(dragColIdx, 1);
            }
        }
        if (!board.layout[hoverRow].panels) {
            board.layout[hoverRow].panels = [];
        }
        let colIndex = hoverCol;
        if (hoverCol === -1) {
            colIndex = board.layout[hoverRow].panels.length;
        }
        if (type == "ref") {
            board.layout[hoverRow].panels.splice(colIndex, 0, this.dragItem);
        } else {
            let chartRef = {chartId: this.dragItem.id, span: 8, globalId: this.dragItem.globalId};
            board.layout[hoverRow].panels.splice(colIndex, 0, chartRef);
        }
        this.boardStore.setChangeBoard(board);
    }

    @autobind
    moveRow(destinationIndex: number) {
        let board: Board = this.boardStore.board;
        const layout = board.layout;
        const dragIndex = this.dragIndex; // layout.indexOf(this.dragItem)
        if (dragIndex === null) {
            return;
        }
        const deleted = layout.splice(dragIndex, 1);
        layout.splice(destinationIndex, 0, deleted[0]);
        board.layout = layout;
        this.boardStore.setChangeBoard(board);
    }

    @autobind
    setDragItem(item: any, index: number) {
        this.dragItem = toJS(item);
        this.dragIndex = index;
    }

    @autobind
    findChart(chartId: number, globalId: string) {
        return BoardService.findChartFromBoard(this.boardStore.board, chartId, globalId);
    }

    @autobind
    changeSpan(rowIdx: number, colIdx: number, value: number) {
        let board: Board = this.boardStore.board;
        let layout: Array<Layout> = board.layout;
        let row: any = layout[rowIdx];
        let panels: Array<Panel> = row.panels;
        let panel: Panel = panels[colIdx];
        panel.span = value;
        this.boardStore.setChangeBoard(board);
    }

    @autobind
    deleteSelectedLayoutChart(id: number, col: number, row: number) {
        if (col >= 0 && row >= 0) {
            const board: Board = this.boardStore.board;
            const layouts: Array<any> = board.layout;
            const layout = layouts[row];
            layout.panels.splice(col, 1);
            if (layout.panels.length == 0) {
                layouts.splice(row, 1);
            }
            this.boardStore.setChangeBoard(board);
        }
    }

    @autobind
    deleteSelectedChart(globalId: number, col: number, row: number) {
        if (globalId && col >= 0) {
            this.chartGlobalId = globalId;
            this.col = col;
            const board = this.boardStore.board;
            let ids: Array<number> = this.usedSelectedChart();
            if (ids.indexOf(globalId) < 0) {
                board.charts.splice(col, 1);
                board.chartIds = board.charts.map(item => {
                    return item.id;
                });
                this.boardStore.setChangeBoard(board);
                this.saveBoardChartIds(board);
            } else {
                this.setState({
                    selectedChartModal: true
                });
            }
        }
    }

    render() {
        return (
            <Row gutter={6}>
                <Col span={6}>
                    <Card
                        size="small"
                        title="已选指标"
                        bodyStyle={{
                            maxHeight: 600,
                            minHeight: 100,
                            overflow: "auto"
                        }}
                        extra={
                            <Button
                                type="primary"
                                style={{margin: "4px 0"}}
                                shape="circle"
                                onClick={e => this.setState({visible: true})}
                                icon={<PlusOutlined />}
                                ghost={true}
                                size="small"
                            />
                        }
                    >
                        <Row gutter={2}>
                            <Col span={24}>
                                {this.renderSelectCharts()}
                            </Col>
                        </Row>
                        {this.state.visible && (
                            <SelectChartModal
                                visible={this.state.visible}
                                charts={this.boardStore.board.charts}
                                selectedCharts={this.state.selectedCharts}
                                handleCancel={e => this.setState({visible: false, selectedCharts: []})}
                                handleOk={this.saveSelectedCharts}
                            />
                        )}
                        {this.deleteSelectedChartModal()}
                    </Card>
                </Col>
                <Col span={18}>
                    {this.renderSettingModal()}
                    {this.renderLayoutBoard()}
                    <div style={{textAlign: "center"}}>
                        <Button type="dashed" icon={<PlusOutlined />} onClick={this.addNewRow}>新增行</Button>
                    </div>
                </Col>
            </Row>
        );
    }
}
