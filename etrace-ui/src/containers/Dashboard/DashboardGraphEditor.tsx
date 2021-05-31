import React from "react";
import {User} from "$models/User";
import {GraphStore} from "$store/GraphStore";
import {difference, get, merge, uniq} from "lodash";
import StoreManager from "../../store/StoreManager";
import {ToolKit, uniqueId} from "../../utils/Util";
import * as DepartmentService from "../../services/DepartmentService";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {MetricVariate, Variate, VariateType} from "$models/BoardModel";
import DashboardGraphView from "../../components/Dashboard/DashboardGraphView";
import DashboardTargetSelector from "../../components/Dashboard/DashboardTargetSelector";
import {
    createOrUpdateGraph,
    getGraphList,
    getGraphWithId,
    getNodeConfigWithId,
    getNodeList,
    syncGraph
} from "$services/DashboardService";
import {DashboardCreateOrUpdateGraphForm, DashboardGraph, DashboardNode} from "$models/DashboardModel";
import {
    Button,
    Card,
    Cascader,
    Col,
    Divider,
    Form,
    Input,
    Layout,
    List,
    notification,
    Pagination,
    Popconfirm,
    Row,
    Select,
    Tag,
    Tooltip
} from "antd";
import {
    AlignCenterOutlined,
    CopyOutlined,
    DeleteOutlined,
    DownOutlined,
    ExportOutlined,
    FullscreenExitOutlined,
    FullscreenOutlined,
    LayoutOutlined,
    PlusOutlined,
    RedoOutlined,
    RightOutlined,
    SaveOutlined,
    UndoOutlined,
    VerticalAlignBottomOutlined,
    VerticalAlignTopOutlined,
    ZoomInOutlined,
    ZoomOutOutlined
} from "@ant-design/icons/lib";
import {ENV, SUPPORT_ENV} from "$constants/Env";
import {CURR_API, getApiByEnv} from "$constants/API";

const Content = Layout.Content;

interface DashboardGraphEditorProps {
    match?: any;
    history?: any;
}

declare type GraphForm = { id: number, title: string, globalId: string, description: string, departmentId: number, productLineId: number };

interface DashboardGraphEditorStatus {
    departmentInfo: any[];
    currSelectedItem: any; // 当前 G6 实例对象中已经选中的 Item
    currAddedNodes: number[]; // 当前已添加的 Node ID，包括 Graph 已经配置好的
    defaultGraphConfig: any; // 当前大盘配置
    baseConfig: GraphForm; // 修改后的配置（基础，需要搭配 NodeIds 和 Relations）
    targets: any; // 所有 chart 的 target 用于渲染变量
}

export default class DashboardGraphEditor extends React.Component<DashboardGraphEditorProps, DashboardGraphEditorStatus> {
    static LIST_PAGE_SIZE = 10;

    toolbarContainer = React.createRef<HTMLDivElement>();
    miniMapContainer = React.createRef<HTMLDivElement>();
    currUser: User = StoreManager.userStore.user;
    graphStore: GraphStore = StoreManager.graphStore;

    g6InstanceGraph: any;

    state = {
        departmentInfo: [],
        currAddedNodes: [],
        currSelectedItem: null,
        defaultGraphConfig: null,
        baseConfig: null,
        targets: null,
    };

    componentDidMount(): void {
        this.getDepartmentInfo();
        const defaultGraphConfig = {
            departmentId: get(this.currUser, "userConfig.config.departmentId"),
            productLineId: get(this.currUser, "userConfig.config.productLineId"),
        };
        this.setState({defaultGraphConfig});
    }

    getUrlSearchGlobalId() {
        const {location: {search}} = window;

        const searchParams = new URLSearchParams(search);
        const globalId = searchParams.get("globalId");

        return globalId || undefined;
    }

    /**
     * 获取部门信息
     */
    getDepartmentInfo = () => {
        DepartmentService.getDefaultTreeData().then(res => {
            this.setState({
                departmentInfo: res
            });
        });
    };

    getChartsTargets = () => {
        const targets = this.graphStore.getAllMetricTargets();
        this.setState({
            targets,
        });
    };

    /**
     * Graph 中选中 Node 或 Edge 句柄
     * 用于控制配置面板 GraphEditorDetailFormPanel 当前项目的变更
     * @param item
     */
    handleItemSelected = (item: any) => {
        this.setState({
            currSelectedItem: item
        });
    };

    /**
     * GraphEditorNodeSelector 「节点选择」列表选中 Node 句柄
     * 用于添加当前已选 Node ID
     * @param {number | string} itemId
     */
    handleNodeSelected = (itemId: number) => {
        const {currAddedNodes} = this.state;
        const index = currAddedNodes.indexOf(itemId);
        if (index === -1) {
            this.setState({currAddedNodes: [...currAddedNodes, itemId]});
        }
    };

    /**
     * Graph 中 Node 被删除句柄
     * 用于删除 state 中选中的 Node
     * @param {number[]} nodes
     */
    handleNodeDeleted = (nodes: number[]) => {
        const items = difference(this.state.currAddedNodes, nodes);

        this.setState({
            currAddedNodes: items
        });
    };

    /**
     * DashboardGraphView 加载完毕时句柄
     * 用于添加当前 Graph 已选的 Node，进行提示，保存 Config 用于修改
     * @param {DashboardGraph} graph API 获取的原始 Graph 数据
     * @param g6Instance G6 生成的实例对象
     */
    handleGraphLoaded = (graph: DashboardGraph, g6Instance: any) => {
        if (!graph) {
            return;
        }
        this.g6InstanceGraph = g6Instance;
        this.setState({
            defaultGraphConfig: graph,
            currAddedNodes: uniq([...this.state.currAddedNodes, ...graph.nodeIds])
        });
        this.getChartsTargets();
    };

    handleGraphUpdated = (graph: DashboardGraph, g6Instance: any) => {
        this.getChartsTargets();
    };

    /**
     * GraphEditorDetailFormPanel 中大盘配置表单内容变化
     */
    handleBaseConfigChanged = (fields: any) => {
        const {defaultGraphConfig} = this.state;
        const {id, title, description, globalId, department, time, variates} = fields;

        const config = {
            time: {
                from: time.value.split("&")[0],
                to: time.value.split("&")[1],
            },
            variates: variates.value,
        };

        const formInfo = {
            id: get(id, "value") || get(defaultGraphConfig, "id"),
            title: get(title, "value") || get(defaultGraphConfig, "title"),
            globalId: get(globalId, "value") || get(defaultGraphConfig, "globalId"),
            description: get(description, "value") || get(defaultGraphConfig, "description"),
            departmentId: get(department, "value[0]") || get(defaultGraphConfig, "departmentId"),
            productLineId: get(department, "value[1]") || get(defaultGraphConfig, "productLineId"),
            config: merge({}, get(defaultGraphConfig, "config", {}), config),
        };

        this.setState({baseConfig: formInfo});
    };

    /**
     * 保存配置
     */
    handleGraphSave = (data: any, env?: ENV) => {
        const {defaultGraphConfig, baseConfig, currAddedNodes} = this.state;
        if (env && baseConfig && baseConfig.id) {
            // 查询 Graph 及 Node
            getGraphWithId(baseConfig.id)
                .then(graph => {
                    const query = graph.nodeIds.map((node: number) => getNodeConfigWithId(node));
                    Promise.all(query).then(nodes => {
                        const target = Object.assign({}, graph, {nodes});
                        syncGraph(target, getApiByEnv(env).monitor)
                            .then(msg => {
                                notification.info({
                                    message: "同步结果",
                                    description: msg,
                                    duration: 5
                                });
                            })
                            .catch(e => {
                                notification.info({
                                    message: "同步失败",
                                    description: JSON.stringify(e),
                                    duration: 5
                                });
                            });
                    });
                });
        } else {
            // clear up
            if (data.nodes && data.nodes.length > 0) {
                data.nodes.forEach(node => {
                    delete node.status;
                    delete node.content;
                    delete node.metrics;
                    delete node.contents;
                    delete node.nodeInfo;
                    delete node.originId;
                    delete node.nodeConfig;
                });
            }
            const form: DashboardCreateOrUpdateGraphForm = Object.assign(
                {},
                defaultGraphConfig,
                baseConfig,
                {
                    nodeIds: currAddedNodes,
                    layout: data,
                }
            );
            delete form.relations;
            createOrUpdateGraph(form).then(id => {
                if (id && baseConfig) {
                    Object.assign(baseConfig, {id});
                    this.setState({baseConfig});
                }
            });
        }
        // console.log(form);
    };

    render() {
        const {match: {params}} = this.props;
        const {departmentInfo, currSelectedItem, currAddedNodes, defaultGraphConfig, baseConfig, targets} = this.state;
        const {currUser} = this;

        const toolbar = (
            <div ref={this.toolbarContainer}>
                <GraphEditorToolBar
                    globalId={baseConfig ? baseConfig.globalId : ""}
                    onSave={this.handleGraphSave}
                    graph={this.g6InstanceGraph}
                />
            </div>
        );

        return (
            <Content className="e-monitor-content-sections with-footer flex">
                <Card
                    className="e-monitor-content-section e-monitor-dashboard-graph-editor take-rest-height card-body-take-rest-height"
                    title={toolbar}
                    bodyStyle={{padding: 0}}
                >
                    <Row className="graph-editor-panels-container">
                        <Col span={6}>
                            <GraphEditorNodeSelector
                                className="graph-editor__panel node-selector"
                                user={currUser}
                                departmentInfo={departmentInfo}
                                selectedNodes={currAddedNodes}
                                onItemSelected={this.handleNodeSelected}
                            />
                        </Col>
                        <Col span={12}>
                            <Card className="graph-editor__panel canvas-panel" bodyStyle={{padding: 0}}>
                                <DashboardGraphView
                                    mode="edit"
                                    grid={true}
                                    id={params.id}
                                    globalId={this.getUrlSearchGlobalId()}
                                    selectedNodes={currAddedNodes}
                                    toolbar={this.toolbarContainer.current}
                                    miniMap={this.miniMapContainer.current}
                                    onGraphLoaded={this.handleGraphLoaded}
                                    onGraphUpdate={this.handleGraphUpdated}
                                    onItemSelected={this.handleItemSelected}
                                    onNodeDeleted={this.handleNodeDeleted}
                                    onSave={this.handleGraphSave}
                                />
                            </Card>
                        </Col>
                        <Col span={6} className="editor-detail-panel-container">
                            <GraphEditorDetailFormPanel
                                user={currUser}
                                chartTargets={targets}
                                departmentInfo={departmentInfo}
                                selectedItem={currSelectedItem}
                                defaultDataSource={defaultGraphConfig}
                                onChange={this.handleBaseConfigChanged}
                                className="graph-editor__panel detail-panel"
                            />
                            {/*<GraphMiniMapPanel*/}
                            {/*className="graph-editor__panel minimap-panel"*/}
                            {/*defaultVisible={true}*/}
                            {/*minimap={<div ref={this.miniMapContainer}/>}*/}
                            {/*/>*/}
                        </Col>
                    </Row>
                </Card>
            </Content>
        );
    }
}

/* 顶部工具栏 */
interface GraphEditorToolBarProps {
    graph?: any;
    globalId?: string;
    onSave?: (graph: any, env?: ENV) => void;
}

interface GraphEditorToolBarStatus {
    isLoadingSyncStatus: boolean;
    otherEnvStates: any[];
}

class GraphEditorToolBar extends React.Component<GraphEditorToolBarProps, GraphEditorToolBarStatus> {
    static ToolBar = [
        [
            {icon: <UndoOutlined />, action: "undo", tooltip: "撤销"},
            {icon: <RedoOutlined />, action: "redo", tooltip: "重做"},
        ],
        [
            {icon: <CopyOutlined />, action: "copy", tooltip: "复制"},
            {icon: <DeleteOutlined />, action: "delete", tooltip: "删除"},
        ],
        [
            {icon: <ZoomInOutlined />, action: "zoomIn", tooltip: "放大"},
            {icon: <ZoomOutOutlined />, action: "zoomOut", tooltip: "缩小"},
            {icon: <AlignCenterOutlined />, action: "alignCenter", tooltip: "居中"},
            {icon: <FullscreenExitOutlined />, action: "resetZoom", tooltip: "重置缩放"},
            {icon: <FullscreenOutlined />, action: "autoFit", tooltip: "适应区域"},
        ],
        [
            {icon: <VerticalAlignTopOutlined />, action: "toFront", tooltip: "上移一层"},
            {icon: <VerticalAlignBottomOutlined />, action: "toBack", tooltip: "下移一层"},
        ],
        [
            {icon: <LayoutOutlined />, action: "layout", tooltip: "整理布局"},
        ],
    ];

    state = {
        isLoadingSyncStatus: false,
        otherEnvStates: null,
    };

    handleSaveToTargetEnv = (env: ENV) => {
        const {graph, onSave} = this.props;
        if (onSave) {
            onSave(graph.save(), env);
        }
    };

    handleLoadSyncStatus = async () => {
        const {globalId} = this.props;
        if (!globalId) {
            return;
        }
        this.setState({isLoadingSyncStatus: true});
        const otherEnvs = SUPPORT_ENV.filter(e => e !== CURR_API.env);

        const results = await Promise.all(otherEnvs.map(env => {
            const options = {status: "Active", globalId: globalId};
            return getGraphList(1, 24, options, false, getApiByEnv(env).monitor);
        }));

        this.setState({
            isLoadingSyncStatus: false,
            otherEnvStates: results.map((res, index) => ({
                env: otherEnvs[index],
                exist: res.total > 0
            }))
        });
    };

    render() {
        const {isLoadingSyncStatus, otherEnvStates} = this.state;

        return (
            <div className="graph-editor__toolbar">
                {GraphEditorToolBar.ToolBar.map((group: any, index: number) => (
                    <React.Fragment key={index}>
                        {group && group.map(({icon, action, tooltip}) => (
                            <Tooltip key={icon + tooltip} title={tooltip}><span data-command={action}>{icon}</span></Tooltip>
                        ))}
                        {index !== GraphEditorToolBar.ToolBar.length - 1 && <Divider type="vertical"/>}
                    </React.Fragment>
                ))}

                {/*<div className="view-page-toolbar__op-group">*/}
                {/*<span>环比:&nbsp;<span style={{marginLeft: "8px"}}>{findTimeShift(this.urlParamStore.getValue(TIMESHIFT)).valueLabel}</span></span>*/}
                {/*<TimePicker hasTimeShift={true} hasTimeZoom={true}/>*/}
                {/*</div>*/}

                <Button.Group className="graph-editor__toolbar__op-btn-group">
                    {otherEnvStates && otherEnvStates.map(item => {
                        return (
                            <Popconfirm
                                key={item.env}
                                title={item.exist
                                    ? "目标环境存在对应 Graph，确定覆盖？\n同步前请保存！"
                                    : "目标环境暂无对应 Graph，是否同步？\n同步前请保存！"
                                }
                                okText="Yes"
                                cancelText="No"
                                placement="bottomRight"
                                onConfirm={() => this.handleSaveToTargetEnv(item.env)}
                            >
                                <Button danger={true}>{item.env}</Button>
                            </Popconfirm>
                        );
                    })}
                    {(!otherEnvStates || otherEnvStates.length === 0) && (
                        <Button type="primary" onClick={this.handleLoadSyncStatus} disabled={isLoadingSyncStatus}>
                            <ExportOutlined/>{isLoadingSyncStatus ? "查询中..." : "同步"}
                        </Button>
                    )}
                    <Button type="primary" data-command="save"><SaveOutlined/>保存</Button>
                </Button.Group>
            </div>
        );
    }
}

/* 节点选择器 */
interface GraphEditorNodeSelectorProps {
    className?: string;
    user?: any; // 用户信息
    departmentInfo?: any[]; // 部门信息
    selectedNodes?: number[];
    onItemSelected?: (items: number) => void;
}

interface GraphEditorNodeSelectorStatus {
    listDataSource: DashboardNode[];
    total: number;
    currPage: number;
}

class GraphEditorNodeSelector extends React.Component<GraphEditorNodeSelectorProps, GraphEditorNodeSelectorStatus> {
    type: string = "all";
    isMine: boolean;
    searchTitle: string;
    departmentInfo: string[] = [];

    state = {
        listDataSource: [],
        total: 0,
        currPage: 1
    };

    componentDidMount(): void {
        this.getNodeList(1);
    }

    /**
     * 获取节点列表
     */
    getNodeList = (page: number, isFavorite?: boolean) => {
        const {user} = this.props;

        const params = {
            title: this.searchTitle,
            departmentId: this.departmentInfo[0],
            productLineId: this.departmentInfo[1],
            user: this.isMine ? user.psncode : null,
        };

        if (this.state.currPage !== page) {
            this.setState({currPage: page});
        }

        getNodeList(page, DashboardGraphEditor.LIST_PAGE_SIZE, params, isFavorite)
            .then(({results, total}) => {
                this.setState({
                    listDataSource: results,
                    total,
                });
            });
    };

    /**
     * 节点列表搜索功能
     */
    handleNodeSelectorSearch = (type: string, title: string) => {
        this.isMine = type === "mine";
        this.searchTitle = title;
        this.getNodeList(1, type === "favorite");
    };

    /**
     * 部门级联选择框输入搜索过滤
     */
    departmentSearchFilter = (inputValue: string, path: any[]) => {
        return path.some(option => option.label.toLowerCase().indexOf(inputValue.toLowerCase()) > -1);
    };

    handleSearch = () => {
        const {type, searchTitle} = this;
        this.handleNodeSelectorSearch(type, searchTitle);
    };

    handleTypeChanged = (type: string) => {
        this.type = type;
        this.handleSearch();
    };

    handleTitleChanged = (e: any) => {
        this.searchTitle = e.target.value;
    };

    handleDepartmentChanged = (departmentInfo: string[]) => {
        this.departmentInfo = departmentInfo;
        this.handleSearch();
    };

    handleItemToggle = (itemId: number) => {
        const {onItemSelected} = this.props;

        if (onItemSelected) {
            onItemSelected(itemId);
        }
    };

    /**
     * 节点列表翻页句柄
     */
    handleNodeListPageChanged = (page: number) => {
        this.setState({
            currPage: page,
        });
        this.getNodeList(page);
    };

    renderNodeListItem = (item: DashboardNode) => {
        const {departmentInfo, selectedNodes} = this.props;

        const department = departmentInfo.find(i => i.id === item.departmentId);
        const productLint = department && department.children.find(i => i.id === item.productLineId);
        const departmentName = department && department.departmentName;
        const productLineName = productLint && productLint.departmentName;

        const isSelected = selectedNodes.indexOf(item.id) > -1;
        const selectedTag =
            <Tag.CheckableTag className="node-select-list__item_selected" checked={true}>已选</Tag.CheckableTag>;

        const title = <div>{item.title}{isSelected && selectedTag}</div>;
        const desc = (
            <div style={{fontSize: 12}}>
                {item.description && <div>{item.description}</div>}
                <div>
                    {[departmentName, productLineName, item.createdBy].filter(Boolean).join(" / ")}
                </div>
            </div>
        );

        return (
            <List.Item data-item={item.id}>
                <div className="node-select-list__item" onClick={() => this.handleItemToggle(item.id)}>
                    <List.Item.Meta title={title} description={desc}/>
                </div>
            </List.Item>
        );
    };

    render() {
        const {departmentInfo, user, className} = this.props;

        const {listDataSource, total, currPage} = this.state;

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

        const departmentId = get(user, "userConfig.config.departmentId");
        const productLineId = get(user, "userConfig.config.productLineId");

        const footer = total > 0 ? (
            <Pagination
                size="small"
                style={{textAlign: "right"}}
                total={total}
                current={currPage}
                pageSize={DashboardTargetSelector.LIST_PAGE_SIZE}
                showTotal={t => `总共 ${t} 条`}
                onChange={this.handleNodeListPageChanged}
            />
        ) : null;

        return (
            <Card title="节点选择" className={className} bordered={false}>
                <Form layout="vertical">
                    <Form.Item>
                        <Cascader
                            defaultValue={(departmentId || productLineId) ? [departmentId, productLineId] : null}
                            placeholder="选择部门"
                            expandTrigger="hover"
                            options={options}
                            allowClear={true}
                            showSearch={{filter: this.departmentSearchFilter, matchInputWidth: false}}
                            onChange={this.handleDepartmentChanged}
                        />
                    </Form.Item>
                    <Form.Item>
                        <Input.Group compact={true} className="dashboard-selector__search">
                            <Select style={{width: "25%"}} defaultValue="all" onChange={this.handleTypeChanged}>
                                <Select.Option value="all">全部</Select.Option>
                                <Select.Option value="mine">我的</Select.Option>
                                <Select.Option value="favorite">收藏</Select.Option>
                            </Select>
                            <Input.Search
                                style={{width: "75%"}}
                                placeholder="搜索 title 或 description"
                                onChange={this.handleTitleChanged}
                                onSearch={this.handleSearch}
                            />
                        </Input.Group>
                    </Form.Item>
                </Form>

                <Divider style={{margin: 0}}/>

                {/* 待选 Node */}
                <List
                    dataSource={listDataSource}
                    renderItem={this.renderNodeListItem}
                    footer={footer}
                />
            </Card>
        );
    }
}

/* 详细配置面板 */
interface GraphEditorDetailPanelProps {
    form?: any;
    user?: User;
    className?: string;
    selectedItem?: any;
    defaultDataSource?: any;
    departmentInfo?: any[];
    onChange?: (data: any) => void;
    chartTargets?: any;
}

interface GraphEditorDetailPanelStatus {
}

class GraphEditorDetailPanel extends React.Component<GraphEditorDetailPanelProps, GraphEditorDetailPanelStatus> {
    globalId: string = uniqueId();

    static renderSelectedItemConfigPanel(selectedItem: any) {
        const model = selectedItem.get("model");

        if (model && model.nodeInfo) {
            const label = model.title;
            const nodeInfo = model.nodeInfo;
            const nodeType = `${nodeInfo.isGroupNode ? "GroupNode - " : ""}${nodeInfo.nodeType}`;

            return (
                <Form layout="vertical">
                    <Form.Item label="节点名称"><Input disabled={true} value={label}/></Form.Item>
                    <Form.Item label="节点类型"><Input disabled={true} value={nodeType}/></Form.Item>
                </Form>
            );
        }
    }

    componentWillReceiveProps(nextProps: Readonly<any>, nextContext: any): void {
        const {form: {setFieldsValue}} = this.props;
        const prevGlobalId = get(this.props.defaultDataSource, "globalId");
        const nextGlobalId = get(nextProps.defaultDataSource, "globalId");
        if (prevGlobalId !== nextGlobalId) {
            setFieldsValue({globalId: nextGlobalId});
        }
    }

    /**
     * 部门级联选择框输入搜索过滤
     */
    departmentSearchFilter(inputValue: string, path: any[]) {
        return path.some(option => option.label.toLowerCase().indexOf(inputValue.toLowerCase()) > -1);
    }

    handleAddVariates = (value: string[]) => {
        const {form: {getFieldValue, setFieldsValue}} = this.props;

        if (value.length > 1) {
            const variates = getFieldValue("variates") || [];
            const option = JSON.parse(value[1]);
            option.label = option.name;

            const index = variates.findIndex(item => item.name === option.name);

            if (index > -1) {
                variates[index] = option;
            } else {
                variates.push(option);
            }
            setFieldsValue({variates});
        }
    };

    handleRemoveVariates = (name: string) => {
        const {form: {getFieldValue, setFieldsValue}} = this.props;
        const variates = getFieldValue("variates") || [];
        const index = variates.findIndex(item => item.name === name);
        if (index > -1) {
            variates.splice(index, 1);
            setFieldsValue({variates});
        }
    };

    renderVariatesItem(variates: Variate[]) {
        if (!variates) {
            return null;
        }
        return variates.map((variate, index) => {
            return (
                <div key={index} style={{marginBottom: 12}}>
                    <MultiVariateSelect variates={[variate]}/>
                    <Popconfirm
                        placement="topRight"
                        title="确认删除？"
                        okText="Yes"
                        cancelText="No"
                        onConfirm={() => this.handleRemoveVariates(variate.name)}
                    >
                        <Button style={{marginLeft: 5, float: "right"}} danger={true} shape="circle" icon={<DeleteOutlined />}/>
                    </Popconfirm>
                </div>
            );
        });
    }

    renderVariatesEditor(targets: any) {
        const options = [];

        if (!targets) {
            return null;
        }

        Object.keys(targets).forEach(key => {
            const value = targets[key];
            const _op = {value: key, label: key, children: []};
            value.forEach((option: Variate) => {
                if (!option.type || option.type == VariateType.METRIC) {
                    const metricOp: MetricVariate = option as MetricVariate;
                    const name = metricOp.prefix ? metricOp.prefix + "." + metricOp.measurement : metricOp.measurement;
                    _op.children.push({
                        value: JSON.stringify(option),
                        label: `${metricOp.label}(${name})`
                    });
                }
            });

            options.push(_op);
        });

        return (
            <Cascader options={options} onChange={this.handleAddVariates}>
                <Button type="dashed" icon={<PlusOutlined />} block={true}>Add Variates</Button>
            </Cascader>
        );
    }

    renderGraphConfigPanel() {
        const {defaultDataSource, departmentInfo, user, chartTargets} = this.props;
        const [form] = Form.useForm();
        const {getFieldValue} = form;

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

        const departmentValue = defaultDataSource
        && defaultDataSource.departmentId
        && defaultDataSource.productLineId
            ? [defaultDataSource.departmentId, defaultDataSource.productLineId]
            : [get(user, "userConfig.config.departmentId", undefined), get(user, "userConfig.config.productLineId", undefined)];

        const config = defaultDataSource && defaultDataSource.config;

        // getFieldDecorator("variates", {initialValue: get(config, "variates", [])}); // 设置 id
        // getFieldDecorator("id", {initialValue: get(defaultDataSource, "id")}); // 设置 id

        return (
            <Form layout="vertical" onFieldsChange={this.props.onChange}>
                {/* 全局 ID */}
                <Form.Item label="全局 ID" initialValue={get(defaultDataSource, "globalId", this.globalId)}>
                    <Input disabled={true} placeholder="全局 ID"/>
                </Form.Item>

                {/* 选择部门 */}
                <Form.Item label="大盘分类" initialValue={departmentValue}>
                    <Cascader
                        placeholder="选择部门"
                        expandTrigger="hover"
                        options={options}
                        allowClear={true}
                        showSearch={{filter: this.departmentSearchFilter, matchInputWidth: false}}
                    />
                </Form.Item>

                {/* 大盘名称 */}
                <Form.Item label="大盘名称" initialValue={defaultDataSource && defaultDataSource.title} rules={[{required: true}]}>
                    <Input placeholder="请输入大盘名称"/>
                </Form.Item>

                {/* 大盘描述 */}
                <Form.Item label="大盘描述" initialValue={defaultDataSource && defaultDataSource.description}>
                    <Input placeholder="请输入大盘描述"/>
                </Form.Item>

                {/* 默认查询时间 */}
                <Form.Item label="默认查询时间" initialValue={`${get(config, "time.from", "now()-30m")}&${get(config, "time.to", "now()-30s")}`}>
                    <Select>
                        <Select.Option value="now()-5m&now()-30s">Last 5 m</Select.Option>
                        <Select.Option value="now()-15m&now()-30s">Last 15 m</Select.Option>
                        <Select.Option value="now()-30m&now()-30s">Last 30 m</Select.Option>
                        <Select.Option value="now()-1h&now()-30s">Last 1 h</Select.Option>
                        <Select.Option value="now()-3h&now()-30s">Last 3 h</Select.Option>
                        <Select.Option value="now()-6&now()-30s">Last 6 h</Select.Option>
                        <Select.Option value="now()-12h&now()-30s">Last 12 h</Select.Option>
                    </Select>
                </Form.Item>

                {/* 变量配置 */}
                <Form.Item label="变量配置">
                    {this.renderVariatesItem(getFieldValue("variates"))}
                    {this.renderVariatesEditor(chartTargets)}
                </Form.Item>
            </Form>
        );
    }

    render() {
        const {className, selectedItem} = this.props;

        const type = ToolKit.firstUpperCase((selectedItem && selectedItem._cfg && selectedItem.get("type")) || "Graph");

        // 判断需要显示的配置面板
        const content = selectedItem && selectedItem._cfg && selectedItem.get && selectedItem.get("type")
            ? GraphEditorDetailPanel.renderSelectedItemConfigPanel(selectedItem)
            : this.renderGraphConfigPanel();

        return (
            <Card title={`${type} 详情`} className={className} bordered={false}>
                {content}
            </Card>
        );
    }
}

const GraphEditorDetailFormPanel = (v: GraphEditorDetailPanelProps) => (<GraphEditorDetailPanel {...v}/>);

//     Form.create<GraphEditorDetailPanelProps>({
//     onFieldsChange(props: GraphEditorDetailPanelProps, changedFields: any, allFields: any) {
//         const {onChange} = props;
//         if (onChange) {
//             onChange(allFields);
//         }
//     }
// })(GraphEditorDetailPanel);

/* 缩略图面板 */
interface GraphMiniMapPanelProps {
    className?: string;
    minimap?: any;
    defaultVisible?: boolean;
}

interface GraphMiniMapPanelStatus {
    visible: boolean;
}

// eslint-disable-next-line @typescript-eslint/no-unused-vars
class GraphMiniMapPanel extends React.Component<GraphMiniMapPanelProps, GraphMiniMapPanelStatus> {
    constructor(props: GraphMiniMapPanelProps) {
        super(props);

        this.state = {
            visible: props.defaultVisible || false
        };
    }

    handleVisibleChanged = () => {
        this.setState({
            visible: !this.state.visible
        });
    };

    render() {
        const {className, minimap} = this.props;
        const {visible} = this.state;

        const title = (<div className="minimap-title-trigger" onClick={this.handleVisibleChanged}>缩略图</div>);

        return (
            <>
                <Divider style={{margin: 0}}/>
                <Card
                    title={title}
                    className={className}
                    bordered={false}
                    bodyStyle={{padding: 0}}
                    extra={visible ? <DownOutlined/> : <RightOutlined/>}
                >
                    <div style={{display: visible ? "block" : "none"}}>
                        {minimap}
                    </div>
                </Card>
            </>
        );
    }
}
