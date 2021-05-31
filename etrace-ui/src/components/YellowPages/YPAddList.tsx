import React from "react";
import {Button, Form, Input, Modal, Popconfirm, Spin} from "antd";

import {User} from "../../models/User";
import {withRouter} from "react-router-dom";
import IconSelector from "../Base/IconSelector";
import {RouteComponentProps} from "react-router";
import {messageHandler} from "../../utils/message";
import StoreManager from "../../store/StoreManager";
import {ListForm, ListItem, SearchCategory} from "../../models/YellowPagesModel";
import YpSearchResultPage from "../../containers/YellowPages/YPSearchResultPage";
import {createList, deleteList, editList, getListById} from "../../services/YellowPagesService";

interface YpAddListProps extends  RouteComponentProps<any> {
    visible?: boolean;
    id?: number; // 编辑状态
    onOk?: (visible: boolean, needRefresh?: boolean) => void;
    onCancel?: () => void;
}

interface YpAddListStatus {
    dataSource: ListForm;
    isPosting: boolean; // 创建 or 编辑请求的 Loading
    isDeleting: boolean; // 删除的 Loading
    listLoading: boolean; // 根据 ID 获取 List 详情
    shouldDestroyOnClose: boolean;
}

// todo: 未使用注解的方式
// @withRouter
class YpAddList extends React.Component<YpAddListProps, YpAddListStatus> {
    list: ListForm = null; // 用户填写的信息
    isEdit: boolean = false;

    constructor(props: YpAddListProps) {
        super(props);
        this.isEdit = !!this.props.id;
        this.state = {
            dataSource: null,
            isPosting: false,
            isDeleting: false,
            listLoading: false,
            shouldDestroyOnClose: this.isEdit,
        };
    }

    componentWillReceiveProps(nextProps: Readonly<YpAddListProps>, nextContext: any): void {
        if (nextProps.visible) {
            const {id} = nextProps;
            id && this.getListInfoById(id);
        }
    }

    getListInfoById = (id: number) => {
        this.setState({listLoading: true});

        getListById(id)
            .then(dataSource => {
                this.setState({
                    dataSource,
                    listLoading: false
                });

                this.list = Object.assign({}, dataSource);
            })
            .catch(err => {
                messageHandler("error", "获取集合配置失败");
                this.setState({
                    listLoading: false
                });
            });
    };

    validateListForm = (list: ListForm) => {
        if (!list.name) {
            messageHandler("warning", "请填写集合名称");
            return false;
        }
        if (!list.maintainerAliEmail) {
            messageHandler("warning", "请填写集合维护者邮箱");
            return false;
        }
        return true;
    };

    handleModalOk = () => {
        const {onOk} = this.props;
        const {dataSource} = this.state;

        const listNeedPost = Object.assign({}, dataSource, this.list);
        // 校验 & Post Request
        if (this.validateListForm(listNeedPost)) {
            this.setState({isPosting: true, shouldDestroyOnClose: true});
            if (this.isEdit) {
                editList(listNeedPost)
                    .then(() => {
                        this.setState({isPosting: false});
                        messageHandler("success", "编辑成功");
                        onOk && onOk(false, true);
                    })
                    .catch((err) => {
                        this.setState({isPosting: false});
                        messageHandler("error", "修改集合信息失败，请联系管理员或稍后重试~");
                        onOk && onOk(true);
                    });
            } else {
                createList(listNeedPost)
                    .then(() => {
                        this.setState({isPosting: false});
                        messageHandler("success", "创建成功");

                        const {history} = this.props;
                        const search = `?${YpSearchResultPage.YELLOW_PAGE_KEY}=${listNeedPost.name}&${YpSearchResultPage.YELLOW_PAGE_TYPE}=${SearchCategory.List}`;
                        history.push({search, pathname: YpSearchResultPage.BASE_URL});

                        onOk && onOk(false);
                    })
                    .catch((err) => {
                        this.setState({isPosting: false});
                        messageHandler("error", "创建集合失败，请联系管理员或稍后重试~");
                        onOk && onOk(true);
                    });
            }
        }
    };

    handleModalCancel = () => {
        const {onCancel} = this.props;
        onCancel && onCancel();
    };
    handleDeleteList = () => {
        const {id, onOk} = this.props;
        if (!id) {
            return;
        }
        this.setState({isDeleting: true});
        deleteList(id)
            .then(() => {
                this.setState({isDeleting: false});
                messageHandler("success", "删除成功");
                onOk && onOk(false, true);
            })
            .catch(() => {
                this.setState({isDeleting: false});
                messageHandler("error", "删除当前集合失败，请联系管理员或稍后重试~");
            });
    };

    handleAddListFormChange = (allField: any) => {
        // const needFields = ["name", "description", "dingtalkNumber", "fileListId", "ownerDingtalkNumber"];
        const form: any = {};
        Object.keys(allField).forEach(key => {
            const {value} = allField[key];
            form[key] = value;
            // return {name, value};
        });

        // console.log(form);

        this.list = {
            icon: form.icon,
            name: form.name,
            description: form.description,
            maintainerAliEmail: form.maintainerAliEmail,
            status: "Active",
        };
    };

    render() {
        const {visible} = this.props;
        const {dataSource, isDeleting, isPosting, shouldDestroyOnClose, listLoading} = this.state;

        const deleteBtn = this.isEdit && (
            <Popconfirm
                key="delete"
                title="确定删除当前收录吗？"
                placement="topRight"
                okText="确定！"
                cancelText="我再想想"
                onConfirm={this.handleDeleteList}
            >
                <Button className="add-list-modal__delete-btn" loading={isDeleting}>删除集合</Button>
            </Popconfirm>
        );

        const modalFooter = [
            <Button key="back" className="add-list-modal__cancel-btn" onClick={this.handleModalCancel}>取消</Button>,
            deleteBtn,
            <Button key="submit" className="add-list-modal__ok-btn" loading={isPosting} onClick={this.handleModalOk}>{this.isEdit ? "提交修改" : "提交创建"}</Button>,
        ];

        return (
            <Modal
                destroyOnClose={shouldDestroyOnClose}
                className="e-monitor-yellow-pages__add-list-modal"
                title={this.isEdit ? "编辑集合" : "添加集合"}
                visible={visible}
                onOk={this.handleModalOk}
                onCancel={this.handleModalCancel}
                footer={modalFooter}
            >
                <Spin tip="配置加载中..." delay={0} spinning={listLoading}>
                    <WrappedAddListForm dataSource={dataSource || {}} onChange={this.handleAddListFormChange}/>
                </Spin>
            </Modal>
        );
    }
}

interface AddListFormProps {
    form?: any;
    onChange?: (allFields: any) => void;
    dataSource?: ListItem;
}

interface AddListFormStatus {
}

class AddListForm extends React.Component<AddListFormProps, AddListFormStatus> {
    private static FORM_LAYOUT = {
        labelCol: {span: 4},
        wrapperCol: {span: 18},
    };

    handleSetMaintainerMyself = () => {
        const {setFieldsValue} = this.props.form;
        const user: User = StoreManager.userStore.user;
        setFieldsValue({maintainerAliEmail: user.aliEmail});
    };

    render() {
        const {dataSource} = this.props;

        return (
            <Form onFieldsChange={this.props.onChange} {...AddListForm.FORM_LAYOUT}>
                {/* 名称 */}
                <Form.Item label="名称" className="list-icon-name" required={true} initialValue={dataSource.icon}>
                        <IconSelector/>
                    {/*{getFieldDecorator("name", {*/}
                    {/*    initialValue: dataSource.name,*/}
                    {/*    rules: [{required: true, message: "请填写集合名称"}],*/}
                    {/*})(*/}
                    <Input placeholder="👈🏻 选择图标，填写集合名称"/>
                </Form.Item>

                {/* 简介 */}
                <Form.Item label="简介" initialValue={dataSource.description}>
                        initialValue: dataSource.description,
                        <Input placeholder="这个集合干啥用的呢？"/>
                </Form.Item>

                {/* 维护者 */}
                <Form.Item label="维护者" className="list-maintainer-email" initialValue={dataSource.maintainerAliEmail} rules={[{required: true, message: "请输入维护者的阿里邮箱"}]}>
                        <Input placeholder="请输入阿里邮箱"/>
                    {<Button type="primary" onClick={this.handleSetMaintainerMyself}>添加我</Button>}
                </Form.Item>
            </Form>
        );
    }
}

const WrappedAddListForm = (v: AddListFormProps) => (<AddListForm {...v}/>);


export default withRouter(YpAddList);
