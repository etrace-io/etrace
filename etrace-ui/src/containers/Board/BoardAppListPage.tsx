import {observer} from "mobx-react";
import {ToolKit} from "$utils/Util";
import {FormInstance} from "antd/es/form";
import {useHistory} from "react-router-dom";
import StoreManager from "$store/StoreManager";
import ListView from "$components/ListView/ListView";
import MetaCard from "$components/MetaCard/MetaCard";
import * as BoardService from "../../services/BoardService";
import {EMonitorSection} from "$components/EMonitorLayout";
import ProductLine from "$components/ProductLine/ProductLine";
import {BOARD_APP_EDIT, BOARD_APP_VIEW} from "$constants/Route";
import * as DataAppService from "../../services/DataAppService";
import React, {useEffect, useRef, useState} from "react";
import {Button, Cascader, Form, Input, Modal, Radio, Space} from "antd";
import {DEFAULT_PAGE_SIZE, PaginationCard} from "$components/Pagination/Pagination";
import {AppstoreOutlined, PlusOutlined, StarOutlined, UserOutlined,} from "@ant-design/icons/lib";
import useUser from "$hooks/useUser";

enum BoardAppListTab {
    ALL = "all",
    FAVORITE = "favorite",
    MINE = "mine",
}

const BoardAppListPage: React.FC = props => {
    const { productLineStore, userActionStore } = StoreManager;

    const params = useRef({
        title: null,
        productLineId: null,
        departmentId: null,
        current: null,
        pageSize: DEFAULT_PAGE_SIZE,
        user: null,
    });

    const [currPage, setCurrPage] = useState<number>(1);
    const [formVisible, setFormVisible] = useState<boolean>(false);
    const [updateLoading, setUpdateLoading] = useState<boolean>(false);

    const user = useUser();
    const history = useHistory();

    useEffect(() => {
        productLineStore.setParams("type", "App");
        productLineStore.setParams("status", "Active");
        productLineStore.setCurrentTab(BoardAppListTab.ALL);

        fetchBoardAppList();

        return () => productLineStore.reset();
    }, []);

    const fetchBoardAppList = (title?: string) => {
        if (typeof title === "string") {
            if (title !== params.current.title) {
                setCurrPage(1);
                params.current.current = 1;
            }

            params.current.title = title;
        }

        const tab = productLineStore.currentTab;
        params.current.user = (tab === BoardAppListTab.FAVORITE || tab === BoardAppListTab.MINE)
            ? user.psncode
            : null;

        productLineStore.searchApps(
            tab === BoardAppListTab.FAVORITE ? "user-action/favorite/app" : "dashboard/app",
            params.current,
        ).then();
    };

    const createBoardApp = app => {
        setUpdateLoading(true);
        DataAppService.create(app).then(result => {
            history.push({ pathname: `${BOARD_APP_EDIT}/${result}` });
        });
    };

    const handleDepartmentChange = (department: any, checked: boolean) => {
        if (!checked) {
            productLineStore.selectedDepartments = null;
        }
        fetchBoardAppList();
    };

    const handleProductLineChange = (productLine: any, checked: boolean) => {
        if (!checked) {
            productLineStore.selectedProductLines = null;
        }
        fetchBoardAppList();
    };

    const handleTabChange = (e: any) => {
        const tab = e.target.value;
        productLineStore.init(tab);
        params.current.current = 1;
        setCurrPage(1);
        fetchBoardAppList();
    };

    const handleFavorite = (id: number, favorite: boolean) => {
        const fn = () => {
            fetchBoardAppList();
            userActionStore.loadUserAction().then();
        };

        favorite
            ? BoardService.deleteFavoriteApp(id).then(fn)
            : BoardService.createFavoriteApp(id).then(fn);
    };

    const handlePageChange = (page: number, size: number) => {
        setCurrPage(page);
        params.current.current = page;
        params.current.pageSize = size;
        fetchBoardAppList();
    };

    const handleFormOk = (form: FormInstance) => {
        form.validateFields()
            .then(values => {
                values.departmentId = values.category[ 0 ];
                if (values.category.length > 1) {
                    values.productLineId = values.category[ 1 ];
                }
                createBoardApp(values);
            });
    };

    const showProductLine = productLineStore.currentTab === BoardAppListTab.ALL;

    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item type="card">
                <Space>
                    <Radio.Group defaultValue={BoardAppListTab.ALL} onChange={handleTabChange}>
                        <Radio.Button value={BoardAppListTab.ALL}><AppstoreOutlined /> 所有</Radio.Button>
                        <Radio.Button value={BoardAppListTab.FAVORITE}><StarOutlined />收藏</Radio.Button>
                        <Radio.Button value={BoardAppListTab.MINE}><UserOutlined /> 我的</Radio.Button>
                    </Radio.Group>

                    <Input.Search
                        style={{ width: 350 }}
                        placeholder="请输入查询条件"
                        onSearch={title => fetchBoardAppList(title)}
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
                    dataSource={productLineStore.dataApps}
                    renderItem={(item: any) => (
                        <MetaCard
                            id={item.id}
                            title={item.title}
                            link={id => `${BOARD_APP_VIEW}/${id}`}
                            department={[item.departmentName, item.productLineName]}
                            isFavorite={item.star}
                            fans={item.favoriteCount}
                            onFavorite={id => handleFavorite(id, item.star)}
                            editLink={(id) => `${BOARD_APP_EDIT}/${id}`}
                            editInfo={[`创建者：${item.createdBy}`, `更新者：${item.updatedBy}`]}
                        />
                    )}
                />
            </EMonitorSection.Item>

            {productLineStore.appTotal > 0 && (
                <EMonitorSection.Item>
                    <PaginationCard
                        current={currPage}
                        onChange={handlePageChange}
                        total={productLineStore.appTotal}
                    />
                </EMonitorSection.Item>
            )}

            {formVisible && (
                <BoardAppForm
                    visible={formVisible}
                    loading={updateLoading}
                    onOk={handleFormOk}
                    onCancel={() => setFormVisible(false)}
                />
            )}
        </EMonitorSection>
    );
};

const BoardAppForm: React.FC<{
    loading?: boolean;
    visible?: boolean;
    onOk?: (form: FormInstance) => void;
    onCancel?: any;
}> = observer(props => {
    const { loading, visible, onOk, onCancel } = props;

    const { productLineStore } = StoreManager;
    const [form] = Form.useForm();

    useEffect(() => {
        productLineStore.loadDepartmentTree().then(() => {
            const defaultCategory: number[] = productLineStore.getDefaultCategory();
            if (defaultCategory.length === 2) {
                form.setFieldsValue({
                    category: [defaultCategory[ 0 ], defaultCategory[ 1 ]],
                });
            }
        });
    }, []);

    return (
        <Modal
            title="新建看板应用"
            width="50%"
            closable={false}
            confirmLoading={loading}
            visible={visible}
            onOk={e => onOk && onOk(form)}
            onCancel={onCancel}
        >
            <Form layout="horizontal" form={form} {...ToolKit.getFormLayout(4)}>
                <Form.Item name="category" label="分类" rules={[{ required: true, message: "请选择应用分类!" }]}>
                    <Cascader
                        options={productLineStore.departmentTree}
                        placeholder="请选择应用分类"
                        changeOnSelect={true}
                        showSearch={true}
                    />
                </Form.Item>
                <Form.Item name="title" label="标题" rules={[{ required: true, message: "请输入应用标题!" }]}>
                    <Input placeholder="请输入应用标题" />
                </Form.Item>
                <Form.Item name="description" label="描述">
                    <Input.TextArea placeholder="请输入应用描述" autoSize={{ minRows: 3, maxRows: 6 }} />
                </Form.Item>
            </Form>
        </Modal>
    );
});

export default observer(BoardAppListPage);
