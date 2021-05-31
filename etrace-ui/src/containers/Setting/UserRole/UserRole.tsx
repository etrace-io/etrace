import {ToolKit} from "$utils/Util";
import {useDebounceFn} from "ahooks";
import {FormInstance} from "antd/es/form";
import {ColumnProps} from "antd/lib/table";
import * as UserRoleService from "$services/UserRoleService";
import {UserRole} from "$services/UserRoleService";
import * as UserService from "$services/UserService";
import React, {useEffect, useRef, useState} from "react";
import {CheckCircleOutlined, DeleteOutlined} from "@ant-design/icons";
import {Button, Card, Form, Input, Modal, Select, Table, Tag} from "antd";

const USER_ROLE = ["ADMIN", "USER"];

const UserRoleSelector: React.FC = props => {
    return (
        <Select mode="tags" placeholder="Select User Role" style={{width: 200}} {...props}>
            {USER_ROLE.map(item => (
                <Select.Option key={item} value={item}>{item}</Select.Option>
            ))}
        </Select>
    );
};

interface UserRolePageStateProps {
    search: any;
    create: any;
}

const SearchUserForm = ((props: UserRolePageStateProps) => {
    const [form] = Form.useForm();
    const {getFieldsValue} = form;
    const {search, create} = props;

    const queryData = () => {
        search(getFieldsValue());
    };

    return (
        <Card>
            <Form layout="inline" form={form}>
                <Form.Item name="userCode" label="用户信息">
                    <Input onPressEnter={queryData}/>
                </Form.Item>

                <Form.Item name="roles" label="权限">
                    <UserRoleSelector/>
                </Form.Item>

                <Form.Item>
                    <Button type="primary" ghost={true} onClick={queryData}>搜索</Button>
                    <Button style={{marginLeft: 8}} onClick={e => create({})}>新建</Button>
                </Form.Item>
            </Form>
        </Card>
    );
});

interface UserRoleFormProps {
    loading?: boolean;
    visible?: boolean;
    handleOk?: any;
    handleCancel?: any;
    action?: string;
    form?: any;
    dataSource?: any;
}

const UserRoleForm: React.FC<UserRoleFormProps> = props => {
    const {loading, visible, handleCancel, handleOk, action, dataSource} = props;

    const [users, setUsers] = useState<any[]>([]);
    const [form] = Form.useForm();
    const {setFieldsValue} = form;

    useEffect(() => {
        setFieldsValue({
            id: dataSource.id,
            userCode: dataSource.psncode,
            roles: dataSource.roles || []
        });
    }, [dataSource, setFieldsValue]);

    const isEdit = action === "edit";

    const searchUser = (value: any) => {
        UserService.search(value).then(data => {
            data && setUsers(data);
        });
    };

    const {run: debounceSearch} = useDebounceFn(searchUser, {wait: 300});

    return (
        <Modal
            width={600}
            title="Edit User Role"
            closable={false}
            confirmLoading={loading}
            visible={visible}
            onOk={e => handleOk(form)}
            onCancel={handleCancel}
        >
            <Form layout="horizontal" form={form} {...ToolKit.getFormLayout(4)}>
                <Form.Item name="id" label="ID" style={{display: "none"}}>
                    <Input/>
                </Form.Item>

                {!isEdit && (
                    <Form.Item
                        name="userCode"
                        label="员工号"
                        rules={[{required: true, message: "Please input user code!"}]}
                    >
                        <Select
                            style={{width: "100%"}}
                            placeholder="Search by psncode/psnname/email"
                            defaultActiveFirstOption={false}
                            filterOption={false}
                            showSearch={true}
                            onSearch={debounceSearch}
                        >
                            {users.map(value => (
                                <Select.Option key={value.psncode} value={value.psncode}>{value.psncode}</Select.Option>
                            ))}
                        </Select>
                    </Form.Item>
                )}

                {isEdit && (
                    <Form.Item name="userCode" label="员工号">
                        <Input disabled={true}/>
                    </Form.Item>
                )}

                <Form.Item
                    name="roles"
                    label="权限"
                    rules={[{required: true, message: "Please select user role"}]}
                >
                    <UserRoleSelector/>
                </Form.Item>
            </Form>
        </Modal>
    );
};

interface ModalFormProps {
    title?: string;
    visible?: boolean;
    onOk?: any;
    onCancel?: any;
    form?: any;
    html?: string;
    htmlText?: string;
    description?: string;
    color?: string;
    icon?: React.ReactNode;
}

const ModalForm: React.FC<ModalFormProps> = props => {
    const {title, visible, onOk, onCancel, description, color = "#b7eb8f", icon = <CheckCircleOutlined />} = props;

    const titleNode = <span style={{fontSize: 16, color}}>{icon} <span>{title}</span></span>;

    return (
        <Modal title={titleNode} visible={visible} onOk={onOk} onCancel={onCancel}>
            <p>{description}</p>
        </Modal>
    );
};

const UserRolePage: React.FC = props => {
    const searchParams = useRef<any>({});

    const [userRoleList, setUserRoleList] = useState<any[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [dataSource, setDataSource] = useState<any>({});
    const [userRole, setUserRole] = useState<any>({});
    const [action, setAction] = useState<string>("create");
    const [updateLoading, setUpdateLoading] = useState<boolean>(false);
    const [methodModal, setMethodModal] = useState<boolean>(false);
    const [showModal, setShowModal] = useState<boolean>(false);

    useEffect(() => {
        fetchUserRoleList();
    }, []);

    const handleSearch = (params: any) => {
        if (params) {
            if (params.roles) {
                searchParams.current.userRole = params.roles[0];
            }
            if (params.userCode) {
                searchParams.current.keyword = params.userCode;
            }
        }
        fetchUserRoleList();
    };

    const fetchUserRoleList = () => {
        setLoading(true);
        UserRoleService.search(searchParams.current).then(list => {
            setUserRoleList(list);
            setLoading(false);
        });
    };

    const createUserRole = (data: any) => {
        setDataSource(data);
        setShowModal(true);
        setAction("create");
    };

    const handleTableChange = (pagination: any, filters: any, sorter: any) => {
        searchParams.current = Object.assign({}, searchParams.current, pagination);
        fetchUserRoleList();
    };

    const handleOk = (form: FormInstance) => {
        form.validateFields().then((values: UserRole) => {
            saveUserRole(values);
        });
    };

    const saveUserRole = (user: UserRole) => {
        setUpdateLoading(true);

        UserRoleService.save(user).then(() => {
            fetchUserRoleList();
            setShowModal(false);
            setUpdateLoading(false);
        });
    };

    const handleCancel = () => {
        setShowModal(false);
    };

    const editUserRole = (data: any) => {
        setDataSource(data);
        setShowModal(true);
        setAction("edit");
    };

    const deleteUserRole = (user: UserRole) => {
        setUserRole(user);
        setMethodModal(true);
    };

    const deleteModalOk = () => {
        setMethodModal(false);

        userRole && UserRoleService.deleteUser(userRole.id).then(() => {
            fetchUserRoleList();
        });
    };

    const deleteModalCancel = () => {
        setMethodModal(false);
    };

    const UserRolePageColumn: ColumnProps<any>[] = [
        {
            title: "姓名",
            dataIndex: "psnname"
        },
        {
            title: "员工号",
            dataIndex: "psncode"
        },
        {
            title: "一级部门",
            dataIndex: "onedeptname"
        },
        {
            title: "部门",
            dataIndex: "fatdeptname"
        },
        {
            title: "子部门",
            dataIndex: "deptname"
        },

        {
            title: "权限",
            dataIndex: "roles",
            render(val: Array<string>) {
                if (val) {
                    return val.map((value, index) => {
                        return (<Tag key={index}>{value}</Tag>);
                    });
                }
            }
        },
        {
            title: "操作",
            render: (text, record, index) => (
                <div>
                    <Button.Group>
                        <Button onClick={e => editUserRole(record)}>编辑</Button>
                        <Button danger={true} ghost={true} onClick={e => deleteUserRole(record)}>删除</Button>
                        <ModalForm
                            title="删除用户"
                            visible={methodModal}
                            icon={<DeleteOutlined />}
                            color="red"
                            onOk={deleteModalOk}
                            description={"删除员工号为：" + userRole && userRole.userCode + "用户！"}
                            onCancel={deleteModalCancel}
                        />
                    </Button.Group>
                </div>
            )
        }
    ];

    return (
        <div className="e-monitor-content-sections flex">
            <div className="e-monitor-content-section">
                <SearchUserForm search={handleSearch} create={createUserRole}/>
            </div>

            <Card className="e-monitor-content-section take-rest-height scroll">
                <Table
                    size="small"
                    rowKey="id"
                    loading={loading}
                    columns={UserRolePageColumn}
                    dataSource={userRoleList}
                    onChange={handleTableChange}
                />
            </Card>

            <UserRoleForm
                dataSource={dataSource}
                action={action}
                visible={showModal}
                loading={updateLoading}
                handleOk={handleOk}
                handleCancel={handleCancel}
            />
        </div>
    );
};

export default UserRolePage;
