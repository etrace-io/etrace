import React from "react";
import {User} from "$models/User";
import {uniqueId} from "$utils/Util";
import {debounce, get, intersection} from "lodash";
import StoreManager from "../../store/StoreManager";
import {DataSourceItemType} from "antd/lib/auto-complete";
import * as DepartmentService from "../../services/DepartmentService";
import {DashboardNode, DashboardNodeTypeEnum} from "$models/DashboardModel";
import * as BasicInformationService from "../../services/BasicInformationService";
import DashboardTargetSelector from "../../components/Dashboard/DashboardTargetSelector";
import {createOrUpdateNode, getNodeConfigWithId, getNodeList, syncNode} from "$services/DashboardService";
import {
    AutoComplete,
    Button,
    Card,
    Cascader,
    Col,
    Form,
    Input,
    Layout,
    notification,
    Popconfirm,
    Popover,
    Radio,
    Row,
    Select,
    Tabs,
    Tooltip
} from "antd";
import {Chart} from "$models/ChartModel";
import {ExportOutlined, LoadingOutlined, QuestionCircleOutlined, SaveOutlined} from "@ant-design/icons/lib";
import {CURR_API, getApiByEnv} from "$constants/API";
import {ENV, SUPPORT_ENV} from "$constants/Env";

const Content = Layout.Content;
const {Option} = Select;

interface DashboardNodeEditorProps {
    match?: any;
    history?: any;
}

interface DashboardCreateOrUpdateStatus {
    baseConfig: any;
    chartConfig: any;
    chartIds: any[];
    departmentInfo: any[];
    defaultNodeConfig: DashboardNode;
    selectedCharts: any[];
}

export default class DashboardNodeEditor extends React.Component<DashboardNodeEditorProps, DashboardCreateOrUpdateStatus> {
    currUser: User = StoreManager.userStore.user;

    static getUrlSearchGlobalId() {
        const {location: {search}} = window;

        const searchParams = new URLSearchParams(search);
        const globalId = searchParams.get("globalId");

        return globalId || undefined;
    }

    constructor(props: DashboardNodeEditorProps) {
        super(props);

        const globalId = DashboardNodeEditor.getUrlSearchGlobalId();
        const {match: {params}} = props;
        const id = params.id;

        if (globalId) {
            this.getNodeInfoByGlobalId(globalId);
        } else if (id) {
            this.getNodeInfoById(id);
        }
        this.getDepartmentInfo();

        this.state = {
            baseConfig: {},
            chartIds: [],
            chartConfig: {},
            departmentInfo: [],
            selectedCharts: [],
            defaultNodeConfig: {id, globalId},
        };
    }

    /**
     * 获取部门信息
     */
    getDepartmentInfo() {
        DepartmentService.getDefaultTreeData().then(res => {
            this.setState({
                departmentInfo: res
            });
        });
    }

    getNodeInfoById = (id: number) => {
        getNodeConfigWithId(id).then((res) => {
            const {chartIds, charts, config} = res;
            this.setState({
                baseConfig: res,
                defaultNodeConfig: res,
                chartIds: chartIds,
                chartConfig: get(config, "charts", {}),
                selectedCharts: charts,
            });
        });
    };

    getNodeInfoByGlobalId = (globalId: string) => {
        const options = {status: "Active", globalId};

        getNodeList(1, 24, options).then(({results}) => {
            const res = results[0];
            if (!res) {
                notification.error({
                    message: "无法找到对应 Global ID Node",
                    description: "建议使用「同步」功能，从其他环境同步配置到当当前环境",
                    duration: 5
                });

                return;
            }

            const {chartIds, charts, config} = res;

            this.setState({
                baseConfig: res,
                defaultNodeConfig: res,
                chartIds: chartIds,
                chartConfig: get(config, "charts", {}),
                selectedCharts: charts,
            });
        });
    };

    handleBaseConfigChanged = (fields: any) => {
        const {defaultNodeConfig} = this.state;
        const {history} = this.props;
        const {id, nodeType, title, description, groupBy, appId, singleNodeConfig, globalId, department} = fields;

        // GroupNode 子节点配置
        if (singleNodeConfig) {
            const {type, appId: groupNodeAppId, nodeName} = singleNodeConfig;
            singleNodeConfig.type = get(type, "value") || get(defaultNodeConfig, "singleNodeConfig.type");
            singleNodeConfig.nodeName = get(nodeName, "value") || get(defaultNodeConfig, "singleNodeConfig.nodeName");
            singleNodeConfig.appId = get(groupNodeAppId, "value") || get(defaultNodeConfig, "singleNodeConfig.appId");
        }

        const formInfo = {
            id: get(id, "value") || get(defaultNodeConfig, "id"),
            title: get(title, "value") || get(defaultNodeConfig, "title"),
            appId: get(appId, "value") || get(defaultNodeConfig, "appId"),
            groupBy: get(groupBy, "value") || get(defaultNodeConfig, "groupBy"),
            globalId: get(globalId, "value") || get(defaultNodeConfig, "globalId"),
            nodeType: get(nodeType, "value") || get(defaultNodeConfig, "nodeType"),
            description: get(description, "value") || get(defaultNodeConfig, "description"),
            departmentId: get(department, "value[0]") || get(defaultNodeConfig, "departmentId"),
            productLineId: get(department, "value[1]") || get(defaultNodeConfig, "productLineId"),
            singleNodeConfig,
        };

        const {location: {search}} = window;
        const searchParams = new URLSearchParams(search);

        if (formInfo.globalId && searchParams.get("globalId") !== formInfo.globalId) {
            searchParams.set("globalId", formInfo.globalId);
            history.replace({search: searchParams.toString()});
        }

        this.setState({baseConfig: formInfo});
    };

    handleChartIdsChanged = (charts: any[]) => {
        if (charts) {
            const chartIds = charts.map(item => item.id);
            this.setState({chartIds, selectedCharts: charts});
        }
    };

    handleChartConfigChanged = (charts: any) => {
        const config = {charts};
        this.setState({
            chartConfig: config
        });
    };

    handleSaveNodeConfig = (data: any, env?: ENV) => {
        const {baseConfig} = this.state;
        if (env && data.id) {
            getNodeConfigWithId(data.id)
                .then(res => {
                    syncNode(res, getApiByEnv(env).monitor).then(msg => {
                        notification.info({
                            message: "同步结果",
                            description: msg,
                            duration: 5
                        });
                    });
                });
        } else {
            createOrUpdateNode(data).then(id => {
                if (id) {
                    Object.assign(baseConfig, {id});
                    this.setState({baseConfig});
                }
            });
        }
    };

    render() {
        const {baseConfig, chartIds, selectedCharts, departmentInfo, defaultNodeConfig, chartConfig} = this.state;
        // 保存等操作
        const tabBarExtraContent = (
            <FormOperationBtnGroup
                data={Object.assign({}, baseConfig, {chartIds}, {config: chartConfig})}
                onSave={this.handleSaveNodeConfig}
            />
        );

        return (
            <Content className="e-monitor-content-sections with-footer scroll e-monitor-dashboard-edit-node">
                <Card className="e-monitor-content-section" style={{marginBottom: 4}} bodyStyle={{paddingTop: 2}}>
                    <Tabs tabBarExtraContent={tabBarExtraContent}>
                        <Tabs.TabPane tab="基础配置" key="Base">
                            <NewNodeForm
                                user={this.currUser}
                                charts={selectedCharts}
                                defaultDataSource={defaultNodeConfig}
                                departmentInfo={departmentInfo}
                                onFocus={this.handleBaseConfigChanged}
                                onChange={this.handleBaseConfigChanged}
                            />
                        </Tabs.TabPane>
                        <Tabs.TabPane tab="指标选择" key="Chart">
                            <DashboardTargetSelector
                                defaultSelected={selectedCharts}
                                departmentInfo={departmentInfo}
                                chartConfig={chartConfig}
                                onChange={this.handleChartIdsChanged}
                                onConfigChange={this.handleChartConfigChanged}
                            />
                        </Tabs.TabPane>
                    </Tabs>
                </Card>
            </Content>
        );
    }
}

/* 基础配置表单 */
interface DashboardNodeEditorFormProps {
    form?: any;
    charts?: Chart[];
    user?: User;
    className?: string;
    departmentInfo?: any[];
    onFocus?: any;
    onChange?: (data: any) => void;
    defaultDataSource?: DashboardNode;
}

interface DashboardNodeEditorFormStatus {
    appIdSearchSourceData: DataSourceItemType[];
    appNodeAppIdLoading: boolean;
}

class DashboardNodeEditorForm extends React.Component<DashboardNodeEditorFormProps, DashboardNodeEditorFormStatus> {
    static NODE_TYPE_INTRO = "节点类型介绍";
    static GROUP_NODE_NAME_INTRO = "使用 `{}` 作为占位符；如 `{clientApp}调用方` 会根据结果将 `{clientApp}` 渲染为对应的 App ID";

    static CONTAINER_LAYOUT = {
        xl: {span: 14, offset: 5},
        lg: {span: 16, offset: 4},
        md: {span: 24, offset: 0},
        xs: {span: 24, offset: 0},
    };

    static FORM_LAYOUT = {
        labelCol: {
            xs: {span: 6},
            md: {span: 6},
            lg: {span: 6},
            xl: {span: 6},
            xxl: {span: 4},
        },
        wrapperCol: {
            xs: {span: 16},
            md: {span: 16},
            lg: {span: 16},
            xl: {span: 16},
            xxl: {span: 16},
        },
    };

    globalId: string = uniqueId();

    state = {
        appIdSearchSourceData: null,
        appNodeAppIdLoading: false,
    };

    componentWillReceiveProps(nextProps: Readonly<any>, nextContext: any): void {
        const {form: {setFieldsValue}} = this.props;
        const prevNodeType = get(this.props.defaultDataSource, "nodeType");
        const nextNodeType = get(nextProps.defaultDataSource, "nodeType");
        const prevGlobalId = get(this.props.defaultDataSource, "globalId");
        const nextGlobalId = get(nextProps.defaultDataSource, "globalId");
        if (prevNodeType !== nextNodeType) {
            setFieldsValue({"nodeType": nextNodeType});
        }
        if (prevGlobalId !== nextGlobalId) {
            setFieldsValue({globalId: nextGlobalId});
        }
    }

    handleAppIdSearchChanged = (value: string) => {
        this.setState({appNodeAppIdLoading: true});
        BasicInformationService.getConsoleAppIds(value).then(res => {
            this.setState({
                appNodeAppIdLoading: false,
                appIdSearchSourceData: res || [],
            });
            this.props.form.setFieldsValue({appId: value});
        });
    };

    handleNodeGroupAppIdSearchChanged = (value: string) => {
        this.setState({appNodeAppIdLoading: true});
        BasicInformationService.getConsoleAppIds(value).then(res => {
            this.setState({
                appNodeAppIdLoading: false,
                appIdSearchSourceData: res || [],
            });
            this.props.form.setFieldsValue({"singleNodeConfig.appId": value});
        });
    };

    handleAppIdSearchResultSelected = (value: string) => {
        this.props.form.setFieldsValue({appId: value});
    };

    handleGroupNodeAppIdSearchResultSelected = (value: string) => {
        this.props.form.setFieldsValue({"singleNodeConfig.appId": value});
    };

    /**
     * 部门级联选择框输入搜索过滤
     */
    departmentSearchFilter(inputValue: string, path: any[]) {
        return path.some(option => option.label.toLowerCase().indexOf(inputValue.toLowerCase()) > -1);
    }

    /**
     * 获取各指标之间交叉的 Group keys
     */
    getChartsGroupKeys(charts: any) {
        if (!charts) {
            return;
        }
        const result = [];

        charts.forEach(chart => {
            const targets = get(chart, "targets", []);
            targets.map(target => target.groupBy).filter(Boolean).forEach(i => result.push(i));
        });

        return intersection(...result);
    }

    render() {
        const {className, departmentInfo, defaultDataSource, charts, user} = this.props;
        const [form] = Form.useForm();
        const {getFieldValue} = form;
        const {appIdSearchSourceData} = this.state;
        const {appNodeAppIdLoading} = this.state;

        const currNodeType = getFieldValue("nodeType");
        const groupPerNodeType = getFieldValue("singleNodeConfig.type");
        const isAppNode = currNodeType === DashboardNodeTypeEnum.AppNode;
        const isGroupNode = currNodeType === DashboardNodeTypeEnum.GroupNode;

        const isGroupAppNode = isGroupNode && groupPerNodeType === DashboardNodeTypeEnum.AppNode;

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

        const groupByKeys = this.getChartsGroupKeys(charts);

        const departmentValue = defaultDataSource
        && defaultDataSource.departmentId
        && defaultDataSource.productLineId
            ? [defaultDataSource.departmentId, defaultDataSource.productLineId]
            : [get(user, "userConfig.config.departmentId", undefined), get(user, "userConfig.config.productLineId", undefined)];

        // getFieldDecorator("id", {initialValue: get(defaultDataSource, "id")}); // 设置 id

        return (
            <Row><Col {...DashboardNodeEditorForm.CONTAINER_LAYOUT}>
                <Form layout="horizontal" className={className} onFieldsChange={this.props.onChange}>
                    {/* 节点类型配置 */}
                    <Row>
                        <Form.Item
                            label={<Tooltip title={DashboardNodeEditorForm.NODE_TYPE_INTRO}><span>节点类型&nbsp;
                                <QuestionCircleOutlined/></span></Tooltip>}
                            {...DashboardNodeEditorForm.FORM_LAYOUT}
                            initialValue={get(defaultDataSource, "nodeType", DashboardNodeTypeEnum.SimpleNode)}
                        >
                            <Radio.Group>
                                <Radio.Button value={DashboardNodeTypeEnum.SimpleNode}>
                                    {DashboardNodeTypeEnum.SimpleNode}
                                </Radio.Button>
                                <Radio.Button value={DashboardNodeTypeEnum.AppNode}>
                                    {DashboardNodeTypeEnum.AppNode}
                                </Radio.Button>
                                <Radio.Button value={DashboardNodeTypeEnum.GroupNode}>
                                    {DashboardNodeTypeEnum.GroupNode}
                                </Radio.Button>
                            </Radio.Group>
                        </Form.Item>
                    </Row>

                    {/* 全局 ID */}
                    <Form.Item label="全局 ID" {...DashboardNodeEditorForm.FORM_LAYOUT} initialValue={get(defaultDataSource, "globalId", this.globalId)}>
                        <Input disabled={true} placeholder="全局 ID"/>
                    </Form.Item>

                    {/* 选择部门 */}
                    <Form.Item label="分类" {...DashboardNodeEditorForm.FORM_LAYOUT} initialValue={departmentValue}>
                        <Cascader
                            placeholder="选择部门"
                            expandTrigger="hover"
                            options={options}
                            allowClear={true}
                            showSearch={{filter: this.departmentSearchFilter, matchInputWidth: false}}
                        />
                    </Form.Item>

                    {/* 节点名称 */}
                    <Form.Item
                        label="节点名称"
                        {...DashboardNodeEditorForm.FORM_LAYOUT}
                        initialValue={defaultDataSource && defaultDataSource.title}
                        rules={[{required: true}]}
                    >
                        <Input placeholder="请输入节点名称"/>
                    </Form.Item>

                    {/* 节点描述 */}
                    <Form.Item label="节点描述" {...DashboardNodeEditorForm.FORM_LAYOUT} initialValue={defaultDataSource && defaultDataSource.description}>
                        <Input placeholder="请输入节点描述"/>
                    </Form.Item>

                    {/* App Node - AppID */}
                    {isAppNode && (
                        <Form.Item label="App ID" {...DashboardNodeEditorForm.FORM_LAYOUT} initialValue={get(defaultDataSource, "appId")}>
                            <AppIdSearchInput
                                placeholder="请输入 App ID"
                                loading={appNodeAppIdLoading}
                                dataSource={appIdSearchSourceData}
                                onSearchChange={debounce(this.handleAppIdSearchChanged, 500)}
                                onSearchResultSelect={this.handleAppIdSearchResultSelected}
                            />
                        </Form.Item>
                    )}

                    {/* Group Node - GroupBy */}
                    {isGroupNode && (
                        <Form.Item label="Group By" {...DashboardNodeEditorForm.FORM_LAYOUT} initialValue={get(defaultDataSource, "groupBy")}>
                            <Select
                                mode="multiple"
                                placeholder="请选择 Group By 的 Tag Key（需先选择指标）"
                            >
                                {groupByKeys && groupByKeys.map((k: any) => (
                                    <Option key={k} value={k}>{k}</Option>
                                ))}
                            </Select>
                        </Form.Item>
                    )}

                    {/* Group 下各节点配置 */}
                    {isGroupNode && (
                        <Row>
                            <Form.Item label="各节点类型" {...DashboardNodeEditorForm.FORM_LAYOUT} initialValue={get(defaultDataSource, "singleNodeConfig.type", DashboardNodeTypeEnum.SimpleNode)}>
                                <Radio.Group>
                                    <Radio.Button value={DashboardNodeTypeEnum.SimpleNode}>
                                        {DashboardNodeTypeEnum.SimpleNode}
                                    </Radio.Button>
                                    <Radio.Button value={DashboardNodeTypeEnum.AppNode}>
                                        {DashboardNodeTypeEnum.AppNode}
                                    </Radio.Button>
                                </Radio.Group>
                            </Form.Item>

                            {isGroupAppNode && (
                                <Form.Item label="各节点 App ID" {...DashboardNodeEditorForm.FORM_LAYOUT} initialValue={get(defaultDataSource, "singleNodeConfig.appId")}>
                                    <AppIdSearchInput
                                        placeholder="请输入 App ID"
                                        loading={appNodeAppIdLoading}
                                        dataSource={appIdSearchSourceData}
                                        onSearchChange={debounce(this.handleNodeGroupAppIdSearchChanged, 500)}
                                        onSearchResultSelect={this.handleGroupNodeAppIdSearchResultSelected}
                                    />
                                </Form.Item>
                            )}

                            <Form.Item
                                label={<Tooltip title={DashboardNodeEditorForm.GROUP_NODE_NAME_INTRO}>
                                    <span>各节点名称&nbsp;<QuestionCircleOutlined/></span>
                                </Tooltip>}
                                {...DashboardNodeEditorForm.FORM_LAYOUT}
                                initialValue={get(defaultDataSource, "singleNodeConfig.nodeName")}
                            >
                                <Input placeholder="请输入节点名称"/>
                            </Form.Item>
                        </Row>
                    )}
                </Form>
            </Col></Row>
        );
    }
}

const NewNodeForm = (v: DashboardNodeEditorFormProps) => (<DashboardNodeEditorForm {...v}/>);
//     onFieldsChange(props: DashboardNodeEditorFormProps, changedFields: any, allFields: any) {
//         const {onChange} = props;
//         if (onChange) {
//             onChange(allFields);
//         }
//     }
// })(DashboardNodeEditorForm);

/* APP ID 搜索框 */
interface AppIdSearchInputProps {
    value?: string;
    loading?: boolean;
    dataSource?: DataSourceItemType[];
    placeholder?: string;
    charts?: any[];
    onFocus?: (value: string) => void;
    onSearchChange?: (value: string) => void;
    onSearchResultSelect?: (value: string) => void;
}

interface AppIdSearchInputStatus {
    searchValue: string;
    isFocus: boolean;
}

class AppIdSearchInput extends React.Component<AppIdSearchInputProps, AppIdSearchInputStatus> {
    constructor(props: AppIdSearchInputProps) {
        super(props);
        this.state = {
            searchValue: props.value || "",
            isFocus: false,
        };
    }

    handleSearchChanged = (value: string) => {
        const {onSearchChange} = this.props;
        if (onSearchChange) {
            onSearchChange(value);
        }
        this.setState({searchValue: value});
    };

    handleSearchFocus = (e: any) => {
        const {onFocus} = this.props;
        if (onFocus) {
            onFocus(e);
        }
        this.setState({
            isFocus: true,
        });
    };

    handleSearchBlur = () => {
        this.setState({
            isFocus: false,
        });
    };

    render() {
        const {onSearchResultSelect, dataSource, placeholder, loading} = this.props;
        const {searchValue, isFocus} = this.state;

        const notFoundTooltip = (<div>AppId Not Found</div>);

        return (
            <Popover
                visible={isFocus && searchValue !== "" && dataSource && dataSource.length === 0}
                content={notFoundTooltip}
            >
                <AutoComplete
                    dataSource={dataSource}
                    value={searchValue}
                    onChange={this.handleSearchChanged}
                    onSelect={onSearchResultSelect}
                >
                    <Input
                        onFocus={this.handleSearchFocus}
                        onBlur={this.handleSearchBlur}
                        placeholder={placeholder}
                        suffix={loading ? <LoadingOutlined/> : null}
                    />
                </AutoComplete>
            </Popover>
        );
    }
}

/* 扩展区域保存按钮等操作 */
interface FormOperationBtnGroupProps {
    data?: any;
    onSync?: any;
    onSave?: (data: any, url?: ENV) => void;
}

interface FormOperationBtnGroupStatus {
    isLoadingSyncStatus: boolean;
    otherEnvStates: any[];
}

class FormOperationBtnGroup extends React.Component<FormOperationBtnGroupProps, FormOperationBtnGroupStatus> {
    state = {
        isLoadingSyncStatus: false,
        otherEnvStates: null,
    };

    handleSave = () => {
        const {data, onSave} = this.props;
        if (onSave) {
            onSave(data);
        }
    };

    handleSaveToTargetEnv = (env: ENV) => {
        const {data, onSave} = this.props;
        if (onSave) {
            onSave(data, env);
        }
    };

    handleLoadSyncStatus = async () => {
        this.setState({isLoadingSyncStatus: true});
        const {data} = this.props;
        const otherEnvs = SUPPORT_ENV.filter(e => e !== CURR_API.env);

        const results = await Promise.all(otherEnvs.map(env => {
            const options = {status: "Active", globalId: data.globalId};
            return getNodeList(1, 24, options, false, getApiByEnv(env).monitor);
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
            <Button.Group>
                {otherEnvStates && otherEnvStates.map(item => {
                    return (
                        <Popconfirm
                            key={item.env}
                            title={item.exist
                                ? "目标环境存在对应 Node，确定覆盖？\n同步前请保存！"
                                : "目标环境暂无对应 Node，是否同步？\n同步前请保存！"
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
                <Button type="primary" onClick={this.handleSave}>
                    <SaveOutlined/>保存
                </Button>
            </Button.Group>
        );
    }
}
