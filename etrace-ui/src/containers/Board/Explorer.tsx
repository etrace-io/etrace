import * as qs from "query-string";
import {observer} from "mobx-react";
import {UserConfig} from "$models/User";
import Metric from "$components/Metric/Metric";
import StoreManager from "$store/StoreManager";
import {SystemKit, UserKit} from "$utils/Util";
import React, {useEffect, useState} from "react";
import {clearChartTimeRange} from "$utils/chart";
import MetricOption from "./Explorer/MetricOption";
import MetaLink from "$components/Metric/MetaLink";
import Axis from "$containers/Board/Explorer/Axis";
import {cloneDeep, get, merge, set} from "lodash";
import * as notification from "$utils/notification";
import * as ChartService from "$services/ChartService";
import Legend from "$containers/Board/Explorer/Legend";
import ChartEditConfig from "./Explorer/ChartEditConfig";
import General from "$containers/Board/Explorer/General";
import Display from "$containers/Board/Explorer/Display";
import LinkConfig from "$containers/Board/Explorer/Link";
import {EMonitorSection} from "$components/EMonitorLayout";
import TimeRange from "$containers/Board/Explorer/TimeRange";
import ChartEditLog from "$components/Board/Explorer/ChartEditLog";
import {useHistory, useLocation, useParams} from "react-router-dom";
import {Button, Checkbox, Modal, Popconfirm, Tabs, Tooltip} from "antd";
import ChartTypeSelector from "$components/Board/Explorer/ChartTypeSelector";
import {ExportOutlined, PlusOutlined, SaveOutlined} from "@ant-design/icons/lib";
import {SPACE_BETWEEN} from "$constants/index";
import {ENV, SUPPORT_ENV} from "$constants/Env";
import {CURR_API, getApiByEnv} from "$constants/API";
import useUser from "$hooks/useUser";
import {PageSwitchStore} from "$store/PageSwitchStore";

const uuid = require("react-native-uuid");

const Explorer: React.FC = props => {
    const [showSyncResult, setShowSyncResult] = useState<boolean>(false);
    const [showModal, setShowModal] = useState<boolean>(false);
    const [otherEnvStates, setOtherEnvStates] = useState<{ env: ENV, exist: boolean }[]>([]);

    const history = useHistory();
    const location = useLocation();
    const params = useParams<{ chartId: string }>();
    const user = useUser();

    const { editChartStore, userStore, productLineStore, pageSwitchStore } = StoreManager;

    useEffect(() => {
        editChartStore.isEditing = true;
        const { globalId } = qs.parse(location.search);
        const { chartId } = params;
        // 用于复制指标
        const targetCopyChartID = get(history.location.state, "chartId"); // 见 ChartList「复制指标」功能

        if (globalId) {
            ChartService
                .searchByGroup({ globalId: globalId, status: "Active" })
                .then((results: any) => {
                    if (results.results.length > 0) {
                        const newId = results.results[ 0 ].id;
                        history.push(`/board/explorer/edit/${newId}`);
                        // currChartID.current = newId;
                        editChartStore.loadChart(newId);
                    } else {
                        notification.warningHandler({
                            message: "未在当前环境找到 GlobalId 对应的配置",
                            description: "建议使用「同步」功能，从其他环境同步配置到当当前环境", duration: 10,
                        });
                    }
                });
        } else if (chartId) {
            editChartStore.loadChart(+chartId);
        } else if (!chartId && targetCopyChartID) {
            editChartStore.loadCopyChart(+targetCopyChartID, productLineStore.getDefaultCategory());
        }

        return () => {
            editChartStore.reset();
            editChartStore.isEditing = false;
            pageSwitchStore.setPromptSwitch(false);
        };
    }, []);

    const saveChartToEnv = (env: ENV) => {
        saveChart(getApiByEnv(env).monitor, true, env);
    };

    const getSyncResult = () => {
        const otherEnvs = SUPPORT_ENV.filter(i => i !== CURR_API.env);

        Promise.all(otherEnvs.map(env => {
            const { globalId } = editChartStore.getChart();
            const url = getApiByEnv(env).monitor;

            return ChartService.searchByGroup({ globalId, status: "Active" }, url);
        })).then(list => {
            setOtherEnvStates(list.map((data: any, index) => ({
                env: otherEnvs[ index ],
                exist: data.total > 0,
            })));
            setShowSyncResult(true);
        });
    };

    const createNewChart = () => {
        const { promptSwitch } = pageSwitchStore;
        if (promptSwitch) {
            setShowModal(true);
        } else {
            // reset
            editChartStore.reset();
            const category = productLineStore.getDefaultCategory();
            editChartStore.mergeChartGeneral({ "departmentId": category[ 0 ] });
            editChartStore.mergeChartGeneral({ "productLineId": category[ 1 ] });
            setShowSyncResult(false);

            // redirect
            history.push("/board/explorer");
        }
    };

    const saveChart = (monitorUrl ?: string, isSyncToOtherEnvAction?: boolean, otherEnvProfile?: ENV) => {
        // clone one chart from store
        const chart = cloneDeep(editChartStore.getChart());

        if (!chart) {
            notification.warningHandler({ message: "指标 ID 无效", description: "请重新新建指标", duration: 5 });
            return;
        }

        // clear time info
        clearChartTimeRange(chart);

        if (chart.id === -1) {
            chart.id = null;
        }

        if (!chart.globalId) {
            chart.globalId = uuid.v4();
            console.warn("自动生成全局 ID: ", chart.globalId);
        }

        if (!chart.departmentId || !chart.productLineId) {
            notification.warningHandler({ message: "指标信息不全", description: "必须选择「基本信息」👉🏻「分类」", duration: 5 });
            return;
        }

        if (!chart.title) {
            notification.warningHandler({ message: "指标信息不全", description: "必须输入「基本信息」👉🏻「标题」", duration: 5 });
            return;
        }

        // 处理 Analyze Target
        const analyzeTargetIndex = chart.targets ? chart.targets.findIndex(t => t.isAnalyze) : -1;
        if (analyzeTargetIndex > -1) {
            const analyzeTarget = chart.targets.splice(analyzeTargetIndex, 1)[ 0 ];
            const { path } = ChartEditConfig.analyze.target;
            chart.config = merge(chart.config, set({}, path, analyzeTarget));
        }

        if (isSyncToOtherEnvAction) {
            ChartService.sync(chart, monitorUrl)
                .then(() => {
                    otherEnvStates.forEach(status => {
                        if (status.env === otherEnvProfile) {
                            status.exist = true;
                        }
                    });

                    setShowSyncResult(true);
                    setOtherEnvStates(otherEnvStates);
                })
                .catch(err => {
                    if (get(err, "response.status") === 403) {
                        // 其他环境未登录，提示用户登录
                    }
                    SystemKit.redirectToLogin(window.location.pathname + window.location.search, otherEnvProfile);
                });
        } else {
            const userConfig: UserConfig = get(userStore, "userConfig.config", {});

            // 新建时保存用户部门信息
            if (!chart.id && (
                userConfig.departmentId !== chart.departmentId
                || userConfig.productLineId !== chart.productLineId
            )) {
                userStore.saveUserConfig({
                    departmentId: chart.departmentId,
                    productLineId: chart.productLineId,
                });
            }

            editChartStore.saveChart(chart, monitorUrl).then(id => {
                if (!chart.id) {
                    // create new chart link to edit
                    // currChartID.current = id;
                    chart.id = id;

                    editChartStore.setChart(chart);
                    history.push(`/board/explorer/edit/${id}`);
                }
            });

            pageSwitchStore.setPromptSwitch(false);
        }
    };

    const currChart = editChartStore.getChart();
    const chartUniqueId = editChartStore.chartUniqueId;
    const showLockButton = UserKit.isAdmin(user);

    const operations = (
        <>
            {currChart && showLockButton && (
                <Checkbox
                    checked={editChartStore.getChart().adminVisible}
                    onChange={e => editChartStore.setAdminVisible(e.target.checked)}
                >
                    <Tooltip title="仅 Admin 可更改、查看"><span>Lock</span></Tooltip>
                </Checkbox>
            )}
            <Button.Group>
                {showSyncResult && otherEnvStates.map((otherEnv, index) => (
                    otherEnv.exist ? (
                        <Popconfirm
                            key={index}
                            title="确定覆盖？"
                            okText="Yes"
                            cancelText="No"
                            onConfirm={e => saveChartToEnv(otherEnv.env)}
                        >
                            <Button danger={true}>{otherEnv.env}</Button>
                        </Popconfirm>
                    ) : (
                        <Button
                            key={index}
                            type="dashed"
                            onClick={e => saveChartToEnv(otherEnv.env)}
                        >
                            {otherEnv.env}
                        </Button>
                    )
                ))}

                {!showSyncResult && <Button onClick={getSyncResult} icon={<ExportOutlined />}>同步</Button>}
                <Button onClick={createNewChart} icon={<PlusOutlined />}>新建</Button>
                <Button type="primary" onClick={e => saveChart()} icon={<SaveOutlined />}>保存</Button>
            </Button.Group>
        </>
    );

    const ChartNotSaveModal = () => {
        const handleOk = () => {
            pageSwitchStore.setPromptSwitch(false);
            editChartStore.reset();
            setShowSyncResult(false);
            setShowModal(false);
            history.push("/board/explorer");
        };

        return (
            <Modal
                title="保存提示"
                visible={showModal}
                onOk={handleOk}
                onCancel={() => setShowModal(false)}
                okText="重新加载"
                cancelText="取消"
            >
                <p>{PageSwitchStore.MSG_LEAVE_WITHOUT_SAVE}</p>
            </Modal>
        );
    };

    const contentStyle = {padding: SPACE_BETWEEN};

    return (
        <EMonitorSection fullscreen={true}>
            {/* 编辑记录 */}
            {currChart && currChart.createdBy && <EMonitorSection.Item>
              <ChartEditLog chart={currChart} />
            </EMonitorSection.Item>}

            {/* 类型选择 */}
            <EMonitorSection.Item>
                <ChartTypeSelector chart={currChart} />
                {/*<ChartTypeSelector chartId={currChart.globalId} />*/}
            </EMonitorSection.Item>

            <EMonitorSection scroll={true}>
                <EMonitorSection.Item>
                    <Metric
                        title={get(currChart, "title", "未命名")}
                        uniqueId={chartUniqueId}
                        chart={currChart}
                        extraLinks={<MetaLink targets={currChart ? currChart.targets : []} key={chartUniqueId} />}
                        hideFields={true}
                    />
                </EMonitorSection.Item>

                <EMonitorSection.Item type="tabs">
                    <Tabs defaultActiveKey="2" tabBarExtraContent={operations}>
                        <Tabs.TabPane tab="基本信息" key="1" forceRender={true} style={contentStyle}><General /></Tabs.TabPane>
                        <Tabs.TabPane tab="指标" key="2" forceRender={true} style={contentStyle}><MetricOption /></Tabs.TabPane>
                        <Tabs.TabPane tab="坐标轴" key="3" forceRender={true} style={contentStyle}><Axis /></Tabs.TabPane>
                        <Tabs.TabPane tab="图例" key="4" forceRender={true} style={contentStyle}><Legend /></Tabs.TabPane>
                        <Tabs.TabPane tab="显示" key="5" forceRender={true} style={contentStyle}><Display /></Tabs.TabPane>
                        <Tabs.TabPane tab="链接" key="6" forceRender={true} style={contentStyle}><LinkConfig /></Tabs.TabPane>
                        <Tabs.TabPane tab="时间" key="7" forceRender={true} style={contentStyle}><TimeRange /></Tabs.TabPane>
                    </Tabs>
                </EMonitorSection.Item>

                <ChartNotSaveModal />
            </EMonitorSection>
        </EMonitorSection>
    );
};

export default observer(Explorer);
