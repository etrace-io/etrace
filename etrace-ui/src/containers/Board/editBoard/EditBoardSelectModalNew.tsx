import React from "react";
import * as ChartService from "../../../services/ChartService";
import {AutoComplete, Badge, Breadcrumb, Card, Col, Form, List, message, Modal, Row, Select, Tooltip} from "antd";
import {autobind} from "core-decorators";
import {debounce} from "lodash";
import {observer} from "mobx-react";
import StoreManager from "../../../store/StoreManager";
import {HomeOutlined} from "@ant-design/icons/lib";

const {Option} = Select;
const BreadcrumbItem = Breadcrumb.Item;

interface SelectChartFormProps {
    loading?: boolean;
    visible?: boolean;
    handleOk?: any;
    form?: any;
    handleCancel?: any;
    selectedCharts?: Array<any>;
    charts?: any;
}

@observer
class SelectChartModalWrapper extends React.Component<any, any> {
    boardConfigStore;

    constructor(props: SelectChartFormProps) {
        super(props);
        this.boardConfigStore = StoreManager.boardConfigStore;
        this.state = {
            productLineNameTree: new Map(),
            searchTreeData: [],
            listItems: [],
            charts: props.charts,
            selectedCharts: props.selectedCharts,
            chartMap: new Map(),
        };
    }

    componentDidMount(): void {
        this.boardConfigStore.init("Chart");
    }

    @autobind
    async fetchChartByTitle(title: string) {
        let param: any = {};
        param.status = "Active";
        param.title = title;
        let charts: any = await ChartService.searchByGroup(param);
        if (charts && charts.results && charts.results.length > 0) {
            let searchTreeData = [];
            let chartMap: Map<number, any> = new Map();
            charts.results.forEach(item => {
                searchTreeData.push(
                    <Option key={item.id} value={item.id}>
                        {item.departmentName + "/" + item.productLineName + "/" + item.title}
                    </Option>);
                chartMap.set(item.id, item);
            });
            this.setState({
                chartMap: chartMap,
                searchTreeData: searchTreeData
            });
        }
    }

    @autobind
    chooseItem(value: any) {
        if (value.departmentName && value.productLineName) {
            this.selectChart(value);
        } else if (value.departmentName) {
            this.boardConfigStore.setDepartment(value);
        } else if (value.productLineName) {
            this.boardConfigStore.setProductLine(value);
        }
    }

    @autobind
    selectChartByTitle(value: any) {
        const chartMap: Map<number, any> = this.state.chartMap;
        let selectedChart = chartMap.get(Number.parseInt(value, 10));
        let charts = this.state.charts ? this.state.charts : [];
        let selectedCharts = this.state.selectedCharts ? this.state.selectedCharts : [];
        const found1 = charts.filter(item => item.id == selectedChart.id);
        const found2 = selectedCharts.filter(item => item.id == selectedChart.id);
        let flag = false;
        if (found1 && found1.length > 0) {
            flag = true;
        }
        if (found2 && found2.length > 0) {
            flag = true;
        }
        if (flag) {
            error("所选的指标已经选择过了!");
        } else {
            selectedCharts.push(selectedChart);
            this.setState({selectedCharts: selectedCharts});
        }
    }

    @autobind
    selectChart(selectedChart: any) {
        let charts = this.state.charts ? this.state.charts : [];
        let selectedCharts = this.state.selectedCharts ? this.state.selectedCharts : [];
        const found1 = charts.filter(item => item.id == selectedChart.id);
        const found2 = selectedCharts.filter(item => item.id == selectedChart.id);
        let flag = false;
        if (found1 && found1.length > 0) {
            flag = true;
        }
        if (found2 && found2.length > 0) {
            flag = true;
        }
        if (flag) {
            error("所选的指标已经选择过了!");
        } else {
            selectedCharts.push(selectedChart);
            this.setState({selectedCharts: selectedCharts});
        }
    }

    @autobind
    showItemTitle(value: any) {
        if (value.departmentName && value.productLineName) {
            return (
                <Tooltip
                    trigger="hover"
                    title={
                        <div>
                            <div>创建者：{value.createdBy}</div>
                            <div>更新者：{value.updatedBy}</div>
                            {
                                value.description && (
                                    <div>描述：{value.description}</div>
                                )
                            }
                        </div>
                    }
                ><a>{value.title}</a>
                </Tooltip>
            );
        } else if (value.productLineName) {
            return (
                <Badge
                    count={value.count}
                    style={{backgroundColor: "#52c41a"}}
                    offset={[8, 0]}
                ><a>{value.productLineName}</a>
                </Badge>
            );
        } else {
            return (
                <Badge
                    count={value.count}
                    style={{backgroundColor: "#52c41a"}}
                    offset={[8, 0]}
                ><a>{value.departmentName}</a>
                </Badge>
            );
        }
    }

    @autobind
    chooseBreadcrumbItem(type: string) {
        if (type == "home") {
            this.boardConfigStore.setDepartment(null);
            this.boardConfigStore.setProductLine(null);
        } else {
            this.boardConfigStore.setProductLine(null);
        }
    }

    @autobind
    getAllSelectedCharts(): Array<any> {
        let charts = this.state.charts ? this.state.charts : [];
        let selectedCharts = this.state.selectedCharts ? this.state.selectedCharts : [];
        let tempCharts = [];
        charts.forEach(chart => {
            tempCharts.push(chart);
        });
        selectedCharts.forEach(chart => {
            tempCharts.push(chart);
        });
        return tempCharts;
    }

    render() {
        this.boardConfigStore.getDataTree("chart");
        const dataSource = this.boardConfigStore.getSelectedBoardConfig();
        const department = this.boardConfigStore.department;
        const productLine = this.boardConfigStore.productLine;
        return (
            <Modal
                title="指标选择"
                visible={this.props.visible}
                onCancel={this.props.handleCancel}
                onOk={e => this.props.handleOk(this.state.selectedCharts)}
                width="65%"
                closable={false}
                style={{top: 20}}
            >
                <Row gutter={6}>
                    <Col span={12}>
                        <AutoComplete
                            style={{width: "100%"}}
                            dataSource={this.state.searchTreeData}
                            placeholder="请输入 title / description"
                            onSearch={debounce(this.fetchChartByTitle, 300)}
                            onSelect={this.selectChartByTitle}
                        />
                        <Card
                            style={{marginTop: 6}}
                            title={<Breadcrumb>
                                <BreadcrumbItem><a onClick={() => this.chooseBreadcrumbItem("home")}><HomeOutlined /></a></BreadcrumbItem>
                                {department && (<BreadcrumbItem><a onClick={() => this.chooseBreadcrumbItem("department")}>{department.departmentName}</a></BreadcrumbItem>)}
                                {productLine && (<BreadcrumbItem>{productLine.productLineName}</BreadcrumbItem>)}
                            </Breadcrumb>}
                        >
                            <div
                                style={{
                                    minHeight: 450,
                                    height: 500,
                                    overflow: "auto"
                                }}
                            >
                                {dataSource && (
                                    <List
                                        size="small"
                                        bordered={false}
                                        loading={productLine ? this.boardConfigStore.loadingBoardConfig : this.boardConfigStore.loading}
                                        dataSource={dataSource}
                                        renderItem={item => (<List.Item>
                                            <div onClick={() => this.chooseItem(item)}>{this.showItemTitle(item)}</div>
                                        </List.Item>)}
                                    />
                                )}
                            </div>
                        </Card>

                    </Col>
                    <Col
                        span={12}
                        style={{
                            minHeight: 450,
                            height: 550,
                            overflowY: "auto"
                        }}
                    >
                        <List
                            size="small"
                            header={<b>已选指标</b>}
                            bordered={true}
                            dataSource={this.getAllSelectedCharts()}
                            renderItem={item => (<List.Item>
                                <Tooltip
                                    trigger="hover"
                                    title={
                                        <div>
                                            <div>创建者：{item.createdBy}</div>
                                            <div>更新者：{item.updatedBy}</div>
                                            {
                                                item.description && (
                                                    <div>描述：{item.description}</div>
                                                )
                                            }
                                        </div>
                                    }
                                >{item.title}
                                </Tooltip>
                            </List.Item>)}
                        />
                    </Col>

                </Row>
            </Modal>
        );
    }

}

const error = (msg) => {
    message.error(msg);
};

// export const SelectChartModal = Form.create<SelectChartFormProps>({})(SelectChartModalWrapper);
export const SelectChartModal = (v: SelectChartFormProps) => (<Form><SelectChartModalWrapper {...v}/></Form>);
