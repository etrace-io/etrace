import React from "react";
import {withRouter} from "react-router-dom";
import {autobind} from "core-decorators";
import {Badge, Breadcrumb, Button, Card, Cascader, Col, Divider, Form, Input, List, message, Row, Tooltip} from "antd";
import "./editBoard/EditBoard.css";
import * as DataAppService from "../../services/DataAppService";
import HistoryPopover, {HistoryType} from "./HistoryPopover";
import {observer} from "mobx-react";
import {HistoryApiService} from "../../services/HistoryApiService";
import {errorHandler} from "../../utils/notification";
import StoreManager from "../../store/StoreManager";
import {uniqueId} from "../../utils/Util";
import {RouteComponentProps} from "react-router";
import {ArrowDownOutlined, ArrowUpOutlined, CloseOutlined, HomeOutlined} from "@ant-design/icons/lib";

const BreadcrumbItem = Breadcrumb.Item;
const ButtonGroup = Button.Group;
const FormItem = Form.Item;
const TextArea = Input.TextArea;
const formItemLayout = {
    labelCol: {
        xs: {span: 24},
        sm: {span: 4},
    },
    wrapperCol: {
        xs: {span: 24},
        sm: {span: 20},
    },
};

interface EditBoardAppPageWrapperProps  extends RouteComponentProps<any> {
    form?: any;
}

interface EditBoardAppPageWrapperStatus {
    initialize: boolean;
    boardApp: any;
}

@observer
class EditBoardAppPageWrapper extends React.Component<EditBoardAppPageWrapperProps, EditBoardAppPageWrapperStatus> {
    boardAppId;
    boardConfigStore;
    productLineStore;
    pageSwitchStore;

    constructor(props: EditBoardAppPageWrapperProps) {
        super(props);
        const {match: {params}} = this.props;
        this.boardAppId = params.boardAppId;
        this.boardConfigStore = StoreManager.boardConfigStore;
        this.productLineStore = StoreManager.productLineStore;
        this.pageSwitchStore = StoreManager.pageSwitchStore;

        this.state = {
            initialize: true,
            boardApp: {}
        };
    }

    componentDidMount() {
        this.boardConfigStore.init("Dashboard");
        this.fetchBoardAppDetail();
        this.getDepartmentTreeData();
    }

    componentWillUnmount() {
        this.pageSwitchStore.setPromptSwitch(false);
    }

    async getDepartmentTreeData() {
        await this.productLineStore.loadDepartmentTree();
    }

    async fetchBoardAppDetail() {
        let boardApp: any = await DataAppService.get(this.boardAppId);
        if (!boardApp.dashboards) {
            boardApp.dashboards = [];
        }
        this.setState({
            boardApp: boardApp,
            initialize: false
        });
    }

    @autobind
    selectDashboard(selectedChart: any) {
        let dashboards = this.state.boardApp.dashboards;
        let found = dashboards.filter((item) => item.id == selectedChart.id);
        if (!found || found.length == 0) {
            dashboards.push(selectedChart);
            this.pageSwitchStore.setPromptSwitch(true);
            this.setState({boardApp: this.state.boardApp});
        } else {
            error("所选的面板已经选择过了!");
        }
    }

    @autobind
    moveDashboard(type: any, index: number) {
        let dashboards = this.state.boardApp.dashboards;
        let board = null;
        switch (type) {
            case "up":
                if (index != 0) {
                    board = dashboards.splice(index, 1)[0];
                    dashboards.splice(index - 1, 0, board);
                }
                break;
            case "down":
                if (index < dashboards.length) {
                    board = dashboards.splice(index, 1)[0];
                    dashboards.splice(index + 1, 0, board);
                }
                break;
            case "remove":
                dashboards.splice(index, 1);
                break;
            default:
                break;
        }
        this.pageSwitchStore.setPromptSwitch(true);
        this.setState({boardApp: this.state.boardApp});
    }

    @autobind
    async saveBoardApp() {
        const boardApp = this.state.boardApp;
        if (!boardApp.departmentId) {
            message.warning("组织结构不能为空！");
            return;
        }
        if (boardApp.title == null || boardApp.title == "") {
            message.warning("标题不能为空！");
            return;
        } else {
            this.pageSwitchStore.setPromptSwitch(false);
            boardApp.dashboardIds = boardApp.dashboards.map(item => {
                return item.id;
            });
            await DataAppService.update(boardApp);
        }
    }

    @autobind
    previewHistoryConfig(historyId: number, boardConfig: any) {
        HistoryApiService.queryHistoryDetail(HistoryType.DASHBOARDAPP.type, historyId).then(data => {
            const newConfig = data.data;
            this.pageSwitchStore.setPromptSwitch(true);
            this.setState({boardApp: newConfig});
        }).catch(err => {
            errorHandler({message: "查询[" + historyId + "]的历史失败", description: "不会更新当前页面的配置"});
        });
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
    chooseItem(value: any) {
        if (value.departmentName && value.productLineName) {
            this.selectDashboard(value);
        } else if (value.departmentName) {
            this.boardConfigStore.setDepartment(value);
        } else if (value.productLineName) {
            this.boardConfigStore.setProductLine(value);
        }
    }

    @autobind
    changeBoardCategory(value: Array<string>) {
        let boardApp = this.state.boardApp;
        boardApp.departmentId = Number.parseInt(value[0], 10);
        boardApp.productLineId = Number.parseInt(value[1], 10);
        this.pageSwitchStore.setPromptSwitch(true);
        this.setState({
            boardApp: boardApp
        });
    }

    @autobind
    changeBoardTitle(e: any) {
        let boardApp = this.state.boardApp;
        boardApp.title = e.target.value;
        this.pageSwitchStore.setPromptSwitch(true);
        this.setState({
            boardApp: boardApp
        });
    }

    @autobind
    changeBoardDescription(e: any) {
        let boardApp = this.state.boardApp;
        boardApp.description = e.target.value;
        this.pageSwitchStore.setPromptSwitch(true);
        this.setState({
            boardApp: boardApp
        });
    }

    render() {
        this.boardConfigStore.getDataTree("dashboard");
        const dataSource = this.boardConfigStore.getSelectedBoardConfig();
        const department = this.boardConfigStore.department;
        const productLine = this.boardConfigStore.productLine;
        return (
            <div className="e-monitor-content-sections with-footer scroll">
                <Card className="e-monitor-content-section">
                    <Row gutter={16}>
                        <Col span={20}>
                            <Form layout="horizontal">
                                <FormItem {...formItemLayout} label="子部门" required={true}>
                                    <Cascader
                                        value={this.state.boardApp && this.state.boardApp.departmentId ?
                                            [this.state.boardApp.departmentId, this.state.boardApp.productLineId] : []}
                                        onChange={this.changeBoardCategory}
                                        options={this.productLineStore.departmentTree}
                                        placeholder="请选择子部门"
                                        changeOnSelect={true}
                                        showSearch={true}
                                    />
                                </FormItem>
                                <FormItem {...formItemLayout} label="标题" required={true}>
                                    <Input
                                        value={this.state.boardApp && this.state.boardApp.title ? this.state.boardApp.title : null}
                                        onChange={this.changeBoardTitle}
                                        placeholder="请输入标题"
                                    />
                                </FormItem>
                                <FormItem {...formItemLayout} label="描述">
                                        <TextArea
                                            onChange={this.changeBoardDescription}
                                            value={this.state.boardApp && this.state.boardApp.description ? this.state.boardApp.description : null}
                                            placeholder="请输入描述"
                                            autoSize={{minRows: 3, maxRows: 6}}
                                        />
                                </FormItem>
                            </Form>
                        </Col>
                        <Col span={4}>
                            <ButtonGroup>
                                <Button type="primary" ghost={true} onClick={this.saveBoardApp}>保存</Button>
                                <div style={{marginTop: "10px"}}>
                                    <HistoryPopover
                                        type={HistoryType.DASHBOARDAPP}
                                        id={this.boardAppId}
                                        applyFunction={(boardConfig, historyId) => this.previewHistoryConfig(historyId, boardConfig)}
                                    />
                                </div>
                            </ButtonGroup>
                        </Col>
                    </Row>
                </Card>
                <Card className="e-monitor-content-section">
                    <Row gutter={6}>
                        <Col span={8}>
                            <Breadcrumb style={{marginTop: "4px"}}>
                                <BreadcrumbItem><a onClick={() => this.chooseBreadcrumbItem("home")}><HomeOutlined /></a></BreadcrumbItem>
                                {department && (<BreadcrumbItem>
                                    <a onClick={() => this.chooseBreadcrumbItem("department")}>{department.departmentName}</a>
                                </BreadcrumbItem>)}
                                {productLine && (<BreadcrumbItem>{productLine.productLineName}</BreadcrumbItem>)}
                            </Breadcrumb>
                            <Divider style={{margin: "8px 0 0 0"}}/>
                            <div
                                style={{
                                    minHeight: 450,
                                    height: 500,
                                    overflow: "auto"
                                }}
                            >
                                {dataSource && (
                                    <List
                                        bordered={false}
                                        size="small"
                                        loading={productLine ? this.boardConfigStore.loadingBoardConfig : this.boardConfigStore.loading}
                                        dataSource={dataSource}
                                        renderItem={item => (<List.Item>
                                            <div onClick={() => this.chooseItem(item)}>{this.showItemTitle(item)}</div>
                                        </List.Item>)}
                                    />
                                )}
                            </div>

                        </Col>
                        <Col span={16}>
                            <Breadcrumb style={{marginTop: "4px"}}>
                                <BreadcrumbItem>已选看板</BreadcrumbItem>
                            </Breadcrumb>
                            <Divider style={{margin: "8px 0 0 0"}}/>
                            <div
                                style={{
                                    minHeight: 450,
                                    height: 500,
                                    overflow: "auto"
                                }}
                            >
                                {this.state.boardApp.dashboards && (
                                    <List
                                        bordered={false}
                                        size="small"
                                        rowKey={() => {
                                            return uniqueId();
                                        }}
                                        dataSource={this.state.boardApp.dashboards}
                                        renderItem={(item: any, index) => (
                                            <List.Item>
                                                <Row style={{width: "100%"}}>
                                                    <Col span={18}>
                                                        {
                                                            [index == 0 && (
                                                                <div key={item.title + item.id}>{item.title}<span style={{color: "#1890ff"}}>(首页)</span>
                                                                </div>), index != 0 && (
                                                                <div key={item.title + item.id}>{item.title}</div>)]
                                                        }
                                                    </Col>
                                                    <Col span={6}>
                                                        <Button
                                                            shape="circle"
                                                            icon={<ArrowUpOutlined />}
                                                            size="small"
                                                            onClick={e => this.moveDashboard("up", index)}
                                                        />
                                                        <Divider type="vertical"/>
                                                        <Button
                                                            shape="circle"
                                                            icon={<ArrowDownOutlined />}
                                                            size="small"
                                                            onClick={e => this.moveDashboard("down", index)}
                                                        />
                                                        <Divider type="vertical"/>
                                                        <Button
                                                            shape="circle"
                                                            danger={true}
                                                            icon={<CloseOutlined />}
                                                            size="small"
                                                            onClick={e => this.moveDashboard("remove", index)}
                                                        />
                                                    </Col>
                                                </Row>
                                            </List.Item>)}
                                    />
                                )}
                            </div>
                        </Col>
                    </Row>
                </Card>
            </div>
        );
    }
}

const error = (msg) => {
    message.error(msg);
};

// const EditBoardAppPage = Form.create()(EditBoardAppPageWrapper);
const EditBoardAppPage = (v: EditBoardAppPageWrapperProps) => (<Form><EditBoardAppPageWrapper {...v}/></Form>);
export default withRouter(EditBoardAppPage);
