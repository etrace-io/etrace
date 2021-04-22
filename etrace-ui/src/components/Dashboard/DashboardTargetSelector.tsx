import React, {ReactNode} from "react";
import {merge} from "lodash";
import {autobind} from "core-decorators";
import * as ChartService from "../../services/ChartService";
import {DragDropContext, Draggable, Droppable} from "react-beautiful-dnd"; // EditBoardLayout L269
import {Button, Card, Cascader, Col, Form, Input, List, Modal, Pagination, Row, Select, Tag, Tooltip} from "antd";

import StoreManager from "../../store/StoreManager";
import {User} from "../../models/User";
import {DeleteOutlined, QuestionCircleOutlined, SettingOutlined} from "@ant-design/icons/lib";

interface DashboardFormTargetSelectorProps {
    defaultSelected?: any[]; // 默认选中
    className?: string;
    departmentInfo?: any[]; // 部门信息
    chartConfig?: any; // 各 chart 配置
    onChange?: (selected: any[]) => void;
    onConfigChange?: (config: any) => void;
}

interface DashboardFormTargetSelectorStatus {
    sourceDataSource: any[]; // 待选指标
    sourceTotal: number;
    sourceCurrPage: number;
    targetCurrPage: number;
    selectedDataSource: any[]; // 新添加的指标
    metricSettingModalVisible: boolean; // 设置模态框的显示
    currSettingMetric: any; // 当前进行设置的 Metric
    metricConfig: any;
}

/**
 * Dashboard 表单选择穿梭框
 */
export default class DashboardTargetSelector extends React.Component<DashboardFormTargetSelectorProps, DashboardFormTargetSelectorStatus> {
    static LIST_PAGE_SIZE: number = 10;

    currUser: User = StoreManager.userStore.user;

    searchTitle: string; // 搜索内容
    isMine: boolean; // 只查找我的指标
    departmentId: string | number; // 一级部门 ID
    productLineId: string | number; // 产品线 ID

    constructor(props: DashboardFormTargetSelectorProps) {
        super(props);

        this.state = {
            sourceDataSource: [],
            sourceTotal: 0,
            sourceCurrPage: 1,
            targetCurrPage: 1,
            selectedDataSource: props.defaultSelected || [],
            metricSettingModalVisible: false,
            currSettingMetric: null,
            metricConfig: props.chartConfig || {},
        };
    }

    componentDidMount(): void {
        this.getChartsList(1);
    }

    /**
     * 获取指标列表
     */
    getChartsList(page: number) {
        const params = {
            pageNum: page,
            title: this.searchTitle,
            departmentId: this.departmentId,
            productLineId: this.productLineId,
            pageSize: DashboardTargetSelector.LIST_PAGE_SIZE,
            user: this.isMine ? this.currUser.psncode : null,
        };

        if (this.state.sourceCurrPage !== page) {
            this.setState({sourceCurrPage: page});
        }

        ChartService.search(params).then(({results, total}) => {
            this.setState({
                sourceDataSource: results,
                sourceTotal: total,
            });
        });
    }

    /**
     * 指标列表翻页句柄
     */
    handleSourcePageChanged = (page: number) => {
        this.setState({
            sourceCurrPage: page,
        });
        this.getChartsList(page);
    };

    /**
     * 已选指标翻页句柄
     */
    handleTargetPageChanged = (page: number) => {
        this.setState({
            targetCurrPage: page,
        });
    };

    /**
     * 根据 title 或 description 搜索指标句柄
     */
    handleSearchWithTitle = (title: string) => {
        this.searchTitle = title;
        this.getChartsList(1);
    };

    /**
     * 根据部门过滤句柄
     */
    handleDepartmentChanged = (info: string[]) => {
        const [departmentId, productLineId] = info;

        this.departmentId = departmentId;
        this.productLineId = productLineId;

        this.getChartsList(1);
    };

    /**
     * 分类变化句柄
     */
    handleCategoryChanged = (value: string) => {
        this.isMine = value === "mine";
        this.getChartsList(1);
    };

    /**
     * 待选指标点击句柄
     */
    handleSelectorItemClick = (item: any, e: any) => {
        const {selectedDataSource} = this.state;

        const index = selectedDataSource.map(i => i.id).indexOf(item.id);
        if (index === -1) {
            selectedDataSource.push(item);
        }
        this.handleTargetListChanged(selectedDataSource);
    };

    handleRemoveSelectorItem = (item: any, e: any) => {
        const {onConfigChange} = this.props;
        let {selectedDataSource, targetCurrPage, metricConfig} = this.state;
        const index = selectedDataSource.map(i => i.id).indexOf(item.id);
        selectedDataSource.splice(index, 1);
        // 删除后注意「已选指标」页码问题
        if (targetCurrPage !== 1 && selectedDataSource.length <= (targetCurrPage - 1) * DashboardTargetSelector.LIST_PAGE_SIZE) {
            targetCurrPage--;
        }
        this.handleTargetListChanged(selectedDataSource);
        delete metricConfig[item.globalId]; // 删除对应配置

        this.setState({targetCurrPage, metricConfig}, () => {
            if (onConfigChange) {
                onConfigChange(this.state.metricConfig);
            }
        });
    };

    /**
     * 已选指标变化句柄
     */
    handleTargetListChanged = (selectedDataSource: any[]) => {
        const {onChange} = this.props;
        if (onChange) {
            onChange(selectedDataSource);
        }
        this.setState({selectedDataSource});
    };

    handleTargetItemDragEnd = (result: any) => {
        const {source, destination} = result;
        if (!destination) {
            return;
        }

        const {selectedDataSource} = this.state;
        if (source.index !== destination.index) {
            const sourceIndex = source.index;
            const destinationIndex = destination.index;

            const sourceItem = selectedDataSource.splice(sourceIndex, 1);
            selectedDataSource.splice(destinationIndex, 0, sourceItem[0]);

            this.handleTargetListChanged(selectedDataSource);
        }
    };

    handleToggleMetricSetting = (show: boolean, item?: any) => {
        this.setState({
            metricSettingModalVisible: show,
            currSettingMetric: item,
        });
    };

    handleSaveMetricSetting = (values: any) => {
        const {onConfigChange} = this.props;
        const {currSettingMetric, metricConfig} = this.state;
        this.handleToggleMetricSetting(false);

        this.setState({
            metricConfig: merge(metricConfig, {
                [currSettingMetric.globalId]: values
            })
        }, () => {
            if (onConfigChange) {
                onConfigChange(this.state.metricConfig);
            }
        });
    };

    /**
     * 部门级联选择框输入搜索过滤
     */
    departmentSearchFilter = (inputValue: string, path: any[]) => {
        return path.some(option => option.label.toLowerCase().indexOf(inputValue.toLowerCase()) > -1);
    };

    renderSourceSelectorItem = (item: any) => {
        return this.renderSelectorItem(item);
    };

    renderTargetSelectorItem = (item: any) => {
        return this.renderSelectorItem(item, true);
    };

    /**
     * 渲染每个 Item
     */
    renderSelectorItem = (item: any, isSelectedList: boolean = false) => {
        const {departmentInfo} = this.props;
        let title;

        if (!isSelectedList) {
            const {selectedDataSource} = this.state;
            const isSelected = selectedDataSource.map(i => i.id).indexOf(item.id) > -1;
            const selectedTag = <Tag.CheckableTag className="dashboard-selector__list-item_selected" checked={true}>已选</Tag.CheckableTag>;
            title = <div>{item.title}{isSelected && <>&nbsp;{selectedTag}</>}</div>;
        } else {
            title = <div>{item.title}</div>;
        }

        const department = departmentInfo.find(i => i.id === item.departmentId);
        const productLint = department && department.children.find(i => i.id === item.productLineId);
        const departmentName = item.departmentName || (department && department.departmentName);
        const productLineName = item.productLineName || (productLint && productLint.departmentName);

        item.departmentStr = [departmentName, productLineName, item.createdBy].filter(Boolean).join(" / ");

        const desc = (
            <div style={{fontSize: 12}}>
                {item.description && <div>{item.description}</div>}
                <div>{item.departmentStr}</div>
            </div>
        );

        const ListItem = isSelectedList
            ? (
                <List.Item>
                    <div className="dashboard-selector__list-item">
                        <List.Item.Meta title={title} description={desc}/>
                        <div className="dashboard-selector__list-item__btn-group">
                            <Button
                                icon={<SettingOutlined />}
                                onClick={(e) => this.handleToggleMetricSetting(true, item)}
                            />
                            <Button
                                type="default"
                                icon={<DeleteOutlined />}
                                onClick={(e) => this.handleRemoveSelectorItem(item, e)}
                            />
                        </div>
                    </div>
                </List.Item>
            )
            : (
                <List.Item>
                    <div
                        className="dashboard-selector__list-item"
                        onClick={(e) => this.handleSelectorItemClick(item, e)}
                    >
                        <List.Item.Meta title={title} description={desc}/>
                    </div>
                </List.Item>
            );

        return !isSelectedList
            ? ListItem
            // 包装可拖拽
            : (
                <Draggable key={item.id} draggableId={"CardsContainer-" + item.id} index={item.index}>
                    {(innerProvided, innerSnapshot) => (
                        <div
                            ref={innerProvided.innerRef}
                            {...innerProvided.dragHandleProps}
                            {...innerProvided.draggableProps}
                            className="dashboard-selector__list-item__drag-wrapper"
                        >
                            {ListItem}
                        </div>
                    )}
                </Draggable>
            );
    };

    render() {
        const {className, departmentInfo} = this.props;
        const {sourceDataSource, sourceTotal, sourceCurrPage} = this.state;
        const {targetCurrPage, selectedDataSource} = this.state;
        const {metricSettingModalVisible, currSettingMetric, metricConfig} = this.state;

        // 已选指标相关数据
        const selectedStart = (targetCurrPage - 1) * DashboardTargetSelector.LIST_PAGE_SIZE; // 当前页起始位置
        const selectedEnd = selectedStart + DashboardTargetSelector.LIST_PAGE_SIZE; // 当前页结束位置

        // 部门信息级联选择框数据源
        const options = departmentInfo ? departmentInfo.map(department => {
            const children = department.children ? department.children.map(child => ({
                value: child.id,
                label: child.departmentName
            })) : null;

            return {
                value: department.id,
                label: department.departmentName,
                children
            };
        }) : [];

        sourceDataSource.forEach((item: any, index: number) => item.index = index);
        selectedDataSource.forEach((item: any, index: number) => item.index = index);

        // 根据已选择里的 ID，给予待定指标以「已选」标识
        return (
            <div className={`dashboard-selector ${className}`}>
                <Input.Group compact={true} className="dashboard-selector__search">
                    <Select style={{width: "10%"}} defaultValue="all" onChange={this.handleCategoryChanged}>
                        <Select.Option value="all">全部</Select.Option>
                        <Select.Option value="mine">我的</Select.Option>
                    </Select>
                    <Cascader
                        style={{width: "25%"}}
                        // size="small"
                        placeholder="根据部门过滤"
                        expandTrigger="hover"
                        options={options}
                        onChange={this.handleDepartmentChanged}
                        changeOnSelect={true}
                        allowClear={true}
                        showSearch={{filter: this.departmentSearchFilter, matchInputWidth: false}}
                    />
                    <Input.Search
                        style={{width: "65%"}}
                        placeholder="搜索 title 或 description"
                        onSearch={this.handleSearchWithTitle}
                    />
                </Input.Group>

                <Row gutter={8}>
                    {/* 待选指标 */}
                    <Col span={12}>
                        <TargetSelector
                            title="指标列表"
                            dataSource={sourceDataSource}
                            renderItem={this.renderSourceSelectorItem}
                            total={sourceTotal}
                            currPage={sourceCurrPage}
                            onPageChanged={this.handleSourcePageChanged}
                        />
                    </Col>

                    {/* 已选指标 */}
                    <Col span={12}>
                        <TargetSelector
                            title="已选指标（拖拽调整位置）"
                            canDrag={true}
                            onDragEnd={this.handleTargetItemDragEnd}
                            dataSource={selectedDataSource.slice(selectedStart, selectedEnd)}
                            renderItem={this.renderTargetSelectorItem}
                            total={selectedDataSource.length}
                            currPage={targetCurrPage}
                            onPageChanged={this.handleTargetPageChanged}
                        />
                    </Col>
                </Row>

                <MetricSettingModal
                    visible={metricSettingModalVisible}
                    metric={currSettingMetric}
                    dataSource={currSettingMetric && metricConfig[currSettingMetric.globalId]}
                    onCancel={(e) => this.handleToggleMetricSetting(false)}
                    onOk={this.handleSaveMetricSetting}
                />
            </div>
        );
    }
}

interface TargetSelectorProps {
    dataSource: any[];
    renderItem: (item: any) => ReactNode;
    title?: React.ReactNode;
    total?: number;
    currPage?: number;
    onPageChanged?: (page: number, pageSize: number) => void;
    onDragEnd?: (e: any) => void;
    canDrag?: boolean;
}

const TargetSelector: React.SFC<TargetSelectorProps> = props => {
    const {dataSource, renderItem, onPageChanged, currPage, total, title, onDragEnd, canDrag} = props;

    const footer = total > 0 ? (
        <Pagination
            size="small"
            style={{textAlign: "right"}}
            total={total}
            current={currPage}
            pageSize={DashboardTargetSelector.LIST_PAGE_SIZE}
            showTotal={t => `总共 ${t} 条`}
            onChange={onPageChanged}
        />
    ) : null;

    const list = (
        <Card title={title} bodyStyle={{paddingTop: 0, paddingBottom: 0}}>
            <List
                className="dashboard-selector__list"
                size="small"
                dataSource={dataSource}
                renderItem={renderItem}
                footer={footer}
            />
        </Card>
    );

    return canDrag ? (
        <DragDropContext onDragEnd={onDragEnd}>
            {/* 提供拖拽上下文环境 */}
            <Droppable droppableId="list">
                {/* 提供可放置区域 */}
                {(provided, snapshot) => (
                    <div ref={provided.innerRef}>
                        {list}
                    </div>
                )}
            </Droppable>
        </DragDropContext>
    ) : list;
};

interface MetricSettingModalProps {
    visible: boolean;
    metric: any;
    onOk?: (values: any, e?: any) => void;
    onCancel?: (e: any) => void;
    dataSource?: any;
}

const MetricSettingModal: React.SFC<MetricSettingModalProps> = props => {
    const {visible, onCancel, onOk, dataSource, metric} = props;

    let form;

    const title = metric && (
        <>
            <p style={{fontSize: 16, fontWeight: "bold", margin: 0}}>{metric.title}</p>
            <p style={{fontSize: 12, color: "rgba(0, 0, 0, 0.45)", margin: 0}}>{metric.departmentStr}</p>
        </>
    );

    const handleSaveSetting = (e: any) => {
        const values = form && form.getFormValue && form.getFormValue();
        if (onOk) {
            onOk(values, e);
        }
    };

    return (
        <Modal visible={visible} onCancel={onCancel} onOk={handleSaveSetting} title={title} destroyOnClose={true}>
            <MetricSettingForm wrappedComponentRef={(f: any) => form = f} dataSource={dataSource}/>
        </Modal>
    );
};

class MetricSetting extends React.Component<any, any> {
    @autobind
    getFormValue() {
        const {form: {getFieldsValue}} = this.props;
        return getFieldsValue();
    }

    render() {
        const {dataSource} = this.props;
        const formItemLayout = {
            labelCol: {
                xs: {span: 6},
                sm: {span: 6},
            },
            wrapperCol: {
                xs: {span: 18},
                sm: {span: 18},
            }
        };

        return (
            <Form layout="horizontal">
                <Form.Item
                    label={<span>指标别名&nbsp;<Tooltip title="用于修改在大盘中展示的指标名称"><QuestionCircleOutlined/></Tooltip></span>}
                    initialValue={dataSource && dataSource.alias}
                    {...formItemLayout}
                >
                    <Input placeholder="请输入指标别名"/>
                </Form.Item>
            </Form>
        );
    }
}

// const MetricSettingForm = Form.create({})(MetricSetting);
const MetricSettingForm = (v: any) => (<Form><MetricSetting {...v}/></Form>);
