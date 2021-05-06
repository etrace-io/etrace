import {ToolKit} from "$utils/Util";
import {observer} from "mobx-react";
import {Board} from "$models/BoardModel";
import {FormInstance} from "antd/es/form";
import {useHistory} from "react-router-dom";
import StoreManager from "$store/StoreManager";
import MetaCard from "$components/MetaCard/MetaCard";
import {DataFormatter} from "$utils/DataFormatter";
import ListView from "$components/ListView/ListView";
import * as ChartService from "$services/ChartService";
import * as BoardService from "$services/BoardService";
import {ValidateStatus} from "antd/es/form/FormItem";
import {BOARD_EDIT, BOARD_VIEW} from "$constants/Route";
import React, {useEffect, useRef, useState} from "react";
import {EMonitorSection} from "$components/EMonitorLayout";
import ProductLine from "$components/ProductLine/ProductLine";
import {Button, Cascader, Form, Input, Modal, Radio, Space} from "antd";
import {DEFAULT_PAGE_SIZE, PaginationCard} from "$components/Pagination/Pagination";
import {AppstoreOutlined, DeleteOutlined, PlusOutlined, StarOutlined, UserOutlined,} from "@ant-design/icons/lib";
import useUser from "$hooks/useUser";

const uuid = require("react-native-uuid");

const formItemLayout = ToolKit.getFormLayout(4);

enum BoardListTab {
    ALL = "all",
    FAVORITE = "favorite",
    MINE = "mine",
    INACTIVE = "inactive",
}

const BoardListPage: React.FC = props => {
    const { productLineStore, userActionStore } = StoreManager;

    const params = useRef({
        title: null,
        productLineId: null,
        departmentId: null,
        pageSize: DEFAULT_PAGE_SIZE,
        current: 1,
        user: null,
        status: null,
    });

    const user = useUser();
    const history = useHistory();

    const [currPage, setCurrPage] = useState<number>(1);
    const [formVisible, setFormVisible] = useState<boolean>(false);
    const [updateLoading, setUpdateLoading] = useState<boolean>(false);

    useEffect(() => {
        productLineStore.setParams("type", "Dashboard");
        productLineStore.setParams("status", "Active");
        productLineStore.init(BoardListTab.ALL);

        fetchBoardList();

        return () => productLineStore.reset();
    }, []);

    const fetchBoardList = (title?: string) => {
        if (typeof title === "string") {
            if (title !== params.current.title) {
                setCurrPage(1);
                params.current.current = 1;
            }

            params.current.title = title;
        }

        const path = productLineStore.currentTab;
        params.current.status = path === BoardListTab.INACTIVE ? "Inactive" : null;
        params.current.user = (path === BoardListTab.FAVORITE || path === BoardListTab.MINE)
            ? user.psncode
            : null;

        productLineStore.setParams(
            "status",
            path === BoardListTab.INACTIVE ? "Inactive" : "Active",
        );

        productLineStore.searchBoards(
            path === BoardListTab.FAVORITE ? "user-action/favorite/board" : "dashboard",
            params.current,
        ).then();
    };

    const handleFormOk = (form: FormInstance) => {
        form.validateFields()
            .then(values => {
                values.departmentId = values.category[ 0 ];
                if (values.category.length > 1) {
                    values.productLineId = values.category[ 1 ];
                }
                if (!values.description) {
                    values.description = " ";
                }
                createBoard(values);
            })
            .catch();
    };

    const createBoard = (board: object) => {
        setUpdateLoading(true);
        BoardService.create(board)
            .then(id => {
                history.push({ pathname: `${BOARD_EDIT}/${id}` });
            })
            .finally(() => {
                setUpdateLoading(false);
            });
    };

    const handleTabChange = (e: any) => {
        const tab = e.target.value;
        productLineStore.init(tab);
        params.current.current = 1;
        setCurrPage(1);
        fetchBoardList();
    };

    const handleDepartmentChange = (department: any, checked: boolean) => {
        if (!checked) {
            productLineStore.setSelectedDepartments(null);
        }
        fetchBoardList();
    };

    const handleProductLineChange = (productLine: any, checked: boolean) => {
        if (!checked) {
            productLineStore.setSelectedProductLines(null);
        }
        fetchBoardList();
    };

    const handleFavorite = (id: number, favorite: boolean) => {
        const fn = () => {
            fetchBoardList();
            userActionStore.loadUserAction().then();
        };

        favorite
            ? BoardService.deleteFavorite(id).then(fn)
            : BoardService.createFavorite(id).then(fn);
    };

    const handleBoardDelete = (id: number, isDelete: boolean) => {
        const fn = () => {
            fetchBoardList();
            userActionStore.loadUserAction().then();
        };

        isDelete
            ? BoardService.deleteBoardById(id).then(fn)
            : BoardService.rollbackBoardById(id).then(fn);
    };

    const handlePageChange = (page: number, size: number) => {
        setCurrPage(page);
        params.current.current = page;
        params.current.pageSize = size;
        fetchBoardList();
    };

    const showProductLine =
        productLineStore.currentTab === BoardListTab.ALL ||
        productLineStore.currentTab === BoardListTab.INACTIVE;

    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item type="card">
                <Space>
                    <Radio.Group defaultValue={BoardListTab.ALL} onChange={handleTabChange}>
                        <Radio.Button value={BoardListTab.ALL}><AppstoreOutlined /> 所有</Radio.Button>
                        <Radio.Button value={BoardListTab.FAVORITE}><StarOutlined /> 收藏</Radio.Button>
                        <Radio.Button value={BoardListTab.MINE}><UserOutlined /> 我的</Radio.Button>
                        <Radio.Button value={BoardListTab.INACTIVE}><DeleteOutlined /> 废弃</Radio.Button>
                    </Radio.Group>

                    <Input.Search
                        style={{width: 350}}
                        placeholder="请输入查询条件"
                        onSearch={title => fetchBoardList(title)}
                    />

                    <Button
                        type="primary"
                        icon={<PlusOutlined />}
                        onClick={() => setFormVisible(true)}
                        ghost={true}
                    >新建
                    </Button>
                </Space>
            </EMonitorSection.Item>

            {showProductLine && (
                <EMonitorSection.Item>
                    <ProductLine
                        onDepartmentChange={handleDepartmentChange}
                        onProductLineChange={handleProductLineChange}
                    />
                </EMonitorSection.Item>
            )}

            <EMonitorSection.Item scroll={true}>
                <ListView
                    loading={productLineStore.boardLoading}
                    dataSource={productLineStore.boards}
                    renderItem={(item: Board) => (
                        <MetaCard
                            id={item.id}
                            title={item.title}
                            link={id => `${BOARD_VIEW}/${id}`}
                            department={[item.departmentName, item.productLineName]}
                            isFavorite={item.star}
                            fans={item.favoriteCount}
                            onFavorite={id => handleFavorite(id, item.star)}
                            isDeleted={item.status === "Inactive"}
                            onDelete={(id, isDelete) => handleBoardDelete(id, isDelete)}
                            viewer={DataFormatter.transformShort(item.viewCount)}
                            editLink={(id) => `${BOARD_EDIT}/${id}`}
                            editInfo={[`创建者：${item.createdBy}`, `更新者：${item.updatedBy}`]}
                        />
                    )}
                />
            </EMonitorSection.Item>

            {productLineStore.boardTotal > 0 && (
                <EMonitorSection.Item>
                    <PaginationCard
                        current={currPage}
                        onChange={handlePageChange}
                        total={productLineStore.boardTotal}
                    />
                </EMonitorSection.Item>
            )}

            {formVisible && (
                <BoardForm
                    visible={formVisible}
                    loading={updateLoading}
                    handleOk={handleFormOk}
                    handleCancel={() => setFormVisible(false)}
                />
            )}
        </EMonitorSection>
    );
};

const BoardForm: React.FC<{
    loading?: boolean;
    visible?: boolean;
    handleOk?: any;
    handleCancel?: any;
}> = observer(props => {
    const { productLineStore } = StoreManager;
    const [form] = Form.useForm();

    const [globalIdStatus, setGlobalIdStatus] = useState<ValidateStatus>("");

    useEffect(() => {
        form.setFieldsValue({ globalId: uuid.v4() });
        productLineStore.loadDepartmentTree()
            .then(() => {
                const defaultCategory: number[] = productLineStore.getDefaultCategory();
                form.setFieldsValue({
                    category: [defaultCategory[ 0 ], defaultCategory[ 1 ]],
                });
            });
    }, []);

    const validateGlobalId = (rule: any, globalId: string, callback: any) => {
        if (globalId) {
            setGlobalIdStatus("validating");
            ChartService.validateBoardGlobalId(globalId).then((isOk: boolean) => {
                if (isOk) {
                    setGlobalIdStatus("success");
                    callback();
                } else {
                    setGlobalIdStatus("error");
                    callback("此 ID 已存在，请重新输入");
                }
            }).catch(err => {
                setGlobalIdStatus("error");
                callback("校验全局 ID 异常！");
            });
        } else {
            setGlobalIdStatus("error");
            callback();
        }
    };

    const { loading, visible, handleCancel, handleOk } = props;

    return (
        <Modal
            title="新建看板"
            width="50%"
            closable={false}
            confirmLoading={loading}
            visible={visible}
            onOk={e => handleOk(form)}
            onCancel={handleCancel}
        >
            <Form form={form} layout="horizontal" {...formItemLayout}>
                <Form.Item name="category" label="分类" rules={[{ required: true, message: "请选择面板分类!" }]}>
                    <Cascader
                        options={productLineStore.departmentTree}
                        placeholder="请选择面板分类"
                        changeOnSelect={true}
                        showSearch={true}
                    />
                </Form.Item>
                <Form.Item name="title" label="标题" rules={[{ required: true, message: "请输入面板标题!" }]}>
                    <Input
                        placeholder="请输入面板标题"
                        type="text"
                        name="emonitor-board-title"
                        autoComplete="emonitor-board-title"
                    />
                </Form.Item>
                <Form.Item
                    name="globalId"
                    label="全局 ID"
                    hasFeedback={true}
                    validateStatus={globalIdStatus}
                    rules={[{ required: true }, { validator: validateGlobalId }]}
                >
                    <Input
                        placeholder="请确认全局 ID"
                        type="text"
                        name="emonitor-global-Id"
                        autoComplete="emonitor-global-Id"
                    />
                </Form.Item>
                <Form.Item name="description" label="看板描述">
                    <Input.TextArea placeholder="请输入看板描述" autoSize={{ minRows: 3, maxRows: 6 }} />
                </Form.Item>
            </Form>
        </Modal>
    );
});

export default observer(BoardListPage);
