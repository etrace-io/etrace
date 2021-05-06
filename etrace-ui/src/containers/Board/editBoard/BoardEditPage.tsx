import {get} from "lodash";
import Moment from "react-moment";
import useUser from "$hooks/useUser";
import {DndProvider} from "react-dnd";
import useBoard from "$hooks/useBoard";
import ShowVariate from "./ShowVariate";
import StoreManager from "$store/StoreManager";
import {ENV, SUPPORT_ENV} from "$constants/Env";
import * as notification from "$utils/notification";
import {errorHandler} from "$utils/notification";
import HTML5Backend from "react-dnd-html5-backend";
import {CURR_API, getApiByEnv} from "$constants/API";
import Links from "$containers/Board/editBoard/Links";
import {useHistory, useParams} from "react-router-dom";
import * as BoardService from "$services/BoardService";
import * as ChartService from "$services/ChartService";
import {isEmpty, SystemKit, UserKit} from "$utils/Util";
import Exception from "$components/Exception/Exception";
import {BOARD_EDIT, BOARD_VIEW} from "$constants/Route";
import React, {useEffect, useRef, useState} from "react";
import {TIME_FROM, TIME_TO} from "$models/TimePickerModel";
import {EMonitorSection} from "$components/EMonitorLayout";
import TimePicker from "$components/TimePicker/TimePicker";
import {HistoryApiService} from "$services/HistoryApiService";
import HistoryPopover, {HistoryType} from "../HistoryPopover";
import BoardPanel from "$components/Board/BoardPanel/BoardPanel";
import EditVariate from "$containers/Board/editBoard/EditVariate";
import EditBoardLayout from "$containers/Board/editBoard/EditBoardLayout";
import {ExportOutlined, EyeOutlined, SaveOutlined} from "@ant-design/icons/lib";
import {Board, EnumVariate, HttpVariate, Variate, VariateType} from "$models/BoardModel";
import {Button, Card, Cascader, Checkbox, Col, Descriptions, Form, Input, Row, Space, Tabs, Tooltip} from "antd";

import "./EditBoard.css";

const queryString = require("query-string");

const BoardEditPage: React.FC = props => {
    const {boardConfigStore, boardStore, productLineStore} = StoreManager;

    const [variateModalVisible, setVariateModalVisible] = useState(false);
    // const [entities, setEntities] = useState([]);
    const [variate, setVariate] = useState({});

    const board = useBoard();
    const {boardId} = useParams();
    const history = useHistory();

    useEffect(() => {
        const {globalId} = queryString.parse(window.location.search);

        if (globalId) {
            BoardService.search({globalId, status: "Active"}).then(results => {
                if (results.results.length > 0) {
                    const newId = results.results[0].id;
                    history.push({pathname: `${BOARD_EDIT}/${newId}`});
                    boardStore.setBoardId(newId);
                } else {
                    notification.warningHandler({
                        message: "未在该环境找到 GlobalId 对应的配置",
                        description: "建议使用「同步」功能，从其他环境同步配置到当前环境", duration: 10
                    });
                    boardId && boardStore.setBoardId(boardId);
                }
            });
        } else {
            boardId && boardStore.setBoardId(boardId);
        }

        // MonitorEntityService.queryEntityByType("Entity").then(result => {
        //     result && setEntities(result);
        // });

        productLineStore.loadDepartmentTree().then();

        return () => {
            boardConfigStore.reload();
            boardStore.reset();
        };
    }, []);

    const error = boardStore.error;

    if (error) {
        return (<Exception title={error.status} desc={error.description}/>);
    }

    const handleEditVariate = (value: any) => {
        setVariate(value);
        setVariateModalVisible(true);
    };

    const handleAddNewVariate = (value: Variate) => {
        if (value && (!value.type || value.type === VariateType.METRIC)) {
            boardStore.setVariate(value);
        } else {
            setVariateModalVisible(true);
            setVariate({});
        }
    };

    const handleVariateSubmit = v => {
        boardStore.setVariate(v as Variate);
        setVariateModalVisible(false);
    };

    return (
        <EMonitorSection>
            <VariatesModal
                defaultVariate={variate as Variate}
                visible={variateModalVisible}
                allVariate={get(board, "config.variates")}
                onSubmit={handleVariateSubmit}
                onCancel={() => setVariateModalVisible(false)}
            />

            {/* 看板信息 */}
            {board && <EMonitorSection.Item>
                <BoardInfoPanel board={board}/>
            </EMonitorSection.Item>}

            {/* 基础配置 */}
            <EMonitorSection.Item type="tabs">
                <BoardBasicConfig
                    board={board}
                    onAddNewVariate={handleAddNewVariate}
                    onEditVariate={handleEditVariate}
                />
            </EMonitorSection.Item>

            {/* 布局配置 */}
            <EMonitorSection.Item type="tabs">
                <Tabs defaultActiveKey="layout" >
                    <Tabs.TabPane tab="布局" key="layout">
                        <DndProvider backend={HTML5Backend}>
                            <EditBoardLayout/>
                        </DndProvider>
                    </Tabs.TabPane>
                    <Tabs.TabPane tab="预览" key="preview">
                        <PreviewBoard board={board}/>
                    </Tabs.TabPane>
                </Tabs>
            </EMonitorSection.Item>
        </EMonitorSection>
    );
};

// export default observer(BoardEditPage);
export default BoardEditPage;

const VariatesModal: React.FC<{
    defaultVariate?: Variate;
    visible?: boolean;
    onSubmit?: (variate: Variate) => void;
    onCancel?: () => void;
    allVariate?: Variate[];
}> = props => {
    const {defaultVariate, visible, onSubmit, onCancel, allVariate} = props;

    const [variate, setVariate] = useState<Variate>(defaultVariate);

    useEffect(() => {
        if (visible) {
            setVariate(defaultVariate);
        }
    }, [visible, defaultVariate]);

    const handleVariateSubmit = () => {
        onSubmit(variate);
        setVariate({} as Variate);
    };

    const changeVariateConfigValue = (type: string, value: any) => {
        // variate[type] = value;
        setVariate(prevVar => (
            {...prevVar, [type]: value}
        ));
    };

    const getVariateValueField = (v?: Variate) => {
        if (!v) { return "query"; }

        if (v.type === "http") {
            return "query";
        } else if (v.type === "enum") {
            return "lists";
        }
    };

    const getVariateValue = (v?: Variate) => {
        if (!v) { return null; }

        if (v.type === "http") {
            return (v as HttpVariate).query;
        } else if (v.type === "enum") {
            return (v as EnumVariate).lists;
        }
    };

    const getVariatePlaceHolder = (v?: Variate): string => {
        if (!v) { return "请输入变量值"; }

        if (v.type === "http") {
            return "请输入变量值，对应的 URL 如 https://xxx.com 或 https://xxx.com?prefix=";
        } else if (v.type === "enum") {
            return "请输入变量值，以英文 `,` 分隔如 `a,b,c`";
        }
        return "请输入变量值";
    };

    return (
        <EditVariate
            currentVariate={variate}
            allVariate={allVariate}
            visible={visible}
            handleVariateSubmit={handleVariateSubmit}
            handleVariateCancel={onCancel}
            changeName={(v) => changeVariateConfigValue("name", v.target.value)}
            changeLabel={(v) => changeVariateConfigValue("label", v.target.value)}
            changeType={(v) => changeVariateConfigValue("type", v)}
            changeFiled={(v) => changeVariateConfigValue(getVariateValueField(variate), v.target.value)}
            changeOnlySingleSelect={(v) => changeVariateConfigValue("onlySingleSelect", v.target.checked)}
            variateValue={getVariateValue(variate)}
            variatePlaceHolder={getVariatePlaceHolder(variate)}
        />
    );
};

// 看板信息
const BoardInfoPanel: React.FC<{
    board: Board;
}> = props => {
    const {board} = props;
    if (!board) { return null; }
    const {boardStore} = StoreManager;

    const previewHistoryConfig = (historyId: number) => {
        HistoryApiService.queryHistoryDetail(HistoryType.DASHBOARD.type, historyId).then(data => {
            const newBoardConfig = data.data;
            boardStore.setChangeBoard(newBoardConfig);
        }).catch(err => {
            errorHandler({message: `查询 ["${historyId}"] 的历史失败`, description: "不会更新当前页面的配置"});
        });
    };

    const historyOp = (
        <HistoryPopover
            type={HistoryType.DASHBOARD}
            id={board.id}
            applyFunction={(boardConfig, historyId) => previewHistoryConfig(historyId)}
        />
    );

    return (
        <Card title="看板信息" extra={historyOp} size="small" className="body-no-padding">
            <Descriptions bordered={true} size="small">
                <Descriptions.Item label="创建者">{board.createdBy}</Descriptions.Item>
                <Descriptions.Item label="最后更新者">{board.updatedBy}</Descriptions.Item>
                <Descriptions.Item label="更新时间">
                    <Moment format="YYYY-MM-DD HH:mm:ss">
                        {board.updatedAt}
                    </Moment>
                </Descriptions.Item>
            </Descriptions>
        </Card>
    );
};

// 看板基础信息配置
const BoardBasicConfig: React.FC<{
    board: Board;
    onAddNewVariate: (value: Variate) => void;
    onEditVariate: (value: Variate) => void;
}> = props => {
    const {board, onAddNewVariate, onEditVariate} = props;
    const {boardStore, productLineStore, urlParamStore, pageSwitchStore} = StoreManager;

    const globalIdValidated = useRef<boolean>(true);
    const originGlobalId = useRef<string>(null);
    const [showSyncResult, setShowSyncResult] = useState<boolean>(false);
    const [otherEnvs, setOtherEnvs] = useState<{ env: ENV, syncDone: boolean }[]>([]);

    const user = useUser();
    const isAdmin = UserKit.isAdmin(user);
    const disableSaveAction = board && board.adminVisible && !isAdmin;

    if (!board) {
        return null;
    }

    const handleSaveDashboardToEnv = (target: ENV) => {
        BoardService.syncBoard(board, getApiByEnv(target).monitor).then(res => {
            otherEnvs.forEach(env => {
                if (env.env === target) {
                    env.syncDone = true;
                }
            });
            setOtherEnvs(otherEnvs.slice());
        }).catch(err => {
            SystemKit.redirectToLogin(window.location.pathname + window.location.search, target);
        });
    };

    const handleShowSyncResult = () => {
        setShowSyncResult(true);
        setOtherEnvs(SUPPORT_ENV.filter(e => e !== CURR_API.env).map(env => {
            return {env: env, syncDone: false};
        }));
    };

    const handleSaveBoard = () => {
        const latestBoard: Board = boardStore.board;
        const boardConfig: any = get(latestBoard, "config", {});
        // save variates select for default values;
        const variates = get(boardConfig, "variates", []);
        variates.forEach(variate => {
            variate.current = urlParamStore.getValues(variate.name);
        });
        boardConfig.variates = variates;

        // auto refresh
        const refresh = urlParamStore.getTimeAutoReflesh();
        if (!isEmpty(refresh)) {
            boardConfig.refresh = refresh;
        }
        // time range selected
        const from = urlParamStore.getValue(TIME_FROM);
        if (!isEmpty(from)) {
            boardConfig.time = {from: from, to: urlParamStore.getValue(TIME_TO)};
        }

        latestBoard.config = boardConfig;
        BoardService.save(latestBoard).then(() => {
            pageSwitchStore.setPromptSwitch(false);
        });
    };

    const handleChangeBoardDescription = (e: any) => {
        boardStore.board.description = e.target.value;
        pageSwitchStore.setPromptSwitch(true);
    };

    const handleChangeBoardGlobalId = (globalId: string) => {
        // init originGlobalId
        if (!originGlobalId.current) {
            if (boardStore.board.globalId) {
                originGlobalId.current = boardStore.board.globalId;
            } else {
                // if no boardStore.board.globalId, set originGlobalId to mock id: -999
                originGlobalId.current = "-999";
            }
        }
        if (globalId) {
            if (globalId !== originGlobalId.current) {
                ChartService.validateBoardGlobalId(globalId).then((isOk: boolean) => {
                    globalIdValidated.current = isOk;
                }).catch(err => {
                    globalIdValidated.current = false;
                });
            }
        }
        boardStore.board.globalId = globalId;
        pageSwitchStore.setPromptSwitch(true);
    };

    const handleChangeBoardTitle = (e: any) => {
        boardStore.board.title = e.target.value;
        pageSwitchStore.setPromptSwitch(true);
    };

    const handleChangeBoardCategory = (value: Array<string>) => {
        boardStore.board.departmentId = Number.parseInt(value[0], 10);
        boardStore.board.productLineId = Number.parseInt(value[1], 10);
        pageSwitchStore.setPromptSwitch(true);
    };

    const operations = <Space>
        <Checkbox
            checked={board.adminVisible}
            onChange={e => boardStore.setAdminVisible(e.target.checked)}
        >
            <Tooltip title="选中后仅 Admin 可保存与同步"><span>Lock</span></Tooltip>
        </Checkbox>

        <Button.Group>
            {showSyncResult && otherEnvs.map((env, index) => (
                <Button
                    key={index}
                    type={env.syncDone ? "primary" : "dashed"}
                    onClick={e => handleSaveDashboardToEnv(env.env)}
                >{env.env}
                </Button>
            ))}

            {!showSyncResult && (
                <Button
                    disabled={disableSaveAction}
                    onClick={handleShowSyncResult}
                    icon={<ExportOutlined/>}
                >同步
                </Button>
            )}

            <Tooltip title="查看面板">
                <Button icon={<EyeOutlined/>} href={`${BOARD_VIEW}/${board.id}`} target="_blank">查看</Button>
            </Tooltip>

            <Tooltip title="保存修改">
                <Button
                    disabled={disableSaveAction}
                    onClick={handleSaveBoard}
                    icon={<SaveOutlined/>}
                    type="primary"
                    ghost={true}
                >保存
                </Button>
            </Tooltip>
        </Button.Group>

        <TimePicker/>
    </Space>;

    return <Tabs tabBarExtraContent={operations}>
        <Tabs.TabPane tab="基础信息" forceRender={true} key="basic">
            <Row gutter={24}>
                <Col span={8}>
                    <Form.Item label="标题">
                        <Input
                            value={board.title}
                            onChange={handleChangeBoardTitle}
                            placeholder="请输入面板标题"
                            onBlur={() => !board.globalId && handleChangeBoardGlobalId(board.title)}
                        />
                    </Form.Item>
                </Col>
                <Col span={8}>
                    <Form.Item label="分类">
                        <Cascader
                            value={board.departmentId ? [board.departmentId, board.productLineId] : []}
                            onChange={handleChangeBoardCategory}
                            options={productLineStore.departmentTree}
                            placeholder="请选择面板分类"
                            changeOnSelect={true}
                            showSearch={true}
                        />
                    </Form.Item>
                </Col>
                <Col span={8}>
                    <Form.Item label="全局 ID">
                        <Input
                            disabled={!isAdmin}
                            value={board.globalId}
                            onChange={e => handleChangeBoardGlobalId(e.target.value)}
                            placeholder="请确认全局 ID"
                        />
                        {(!globalIdValidated.current || !board.globalId) &&
                        <span style={{color: "red"}}>ID 无效或与其他 ID 重复，请更换</span>}
                    </Form.Item>
                </Col>
                <Col span={24}>
                    <Form.Item label="描述">
                        <Input.TextArea
                            onChange={handleChangeBoardDescription}
                            value={board.description}
                            placeholder="请输入面板描述"
                            autoSize={{minRows: 2, maxRows: 6}}
                        />
                    </Form.Item>
                </Col>
            </Row>
        </Tabs.TabPane>

        <Tabs.TabPane tab="变量配置" forceRender={true} key="variate">
            {board.config && (
                <ShowVariate
                    variates={board.config.variates}
                    onCreate={onAddNewVariate}
                    onEdit={onEditVariate}
                />
            )}
        </Tabs.TabPane>

        <Tabs.TabPane tab="链接" forceRender={true} key="link">
            <Links/>
        </Tabs.TabPane>
    </Tabs>;
};

const PreviewBoard: React.FC<{
    board: Board;
}> = props => {
    const {board} = props;

    if (!board || !board.layout) {
        return null;
    }

    return <BoardPanel board={board}/>;
};
