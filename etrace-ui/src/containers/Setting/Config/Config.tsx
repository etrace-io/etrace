import Moment from "react-moment";
import {FormInstance} from "antd/es/form";
import {ColumnProps} from "antd/lib/table";
import React, {useEffect, useRef, useState} from "react";
import * as ConfigService from "../../../services/ConfigService";
import {CheckCircleOutlined, DeleteOutlined} from "@ant-design/icons/lib";
import {Badge, Button, Card, Form, Input, Modal, Select, Table} from "antd";

const FormItem = Form.Item;
const formItemLayout = {
    labelCol: {
        xs: {span: 24},
        sm: {span: 6},
    },
    wrapperCol: {
        xs: {span: 24},
        sm: {span: 18},
    },
};

interface ConfigPageStateProps {
    search: any;
    create: any;
}

const SearchUserForm: React.FC<ConfigPageStateProps> = props => {
    const [form] = Form.useForm();
    const {getFieldsValue} = form;
    const {search, create} = props;

    const queryData = () => {
        search(getFieldsValue());
    };

    return (
        <Form layout="inline" form={form}>
            <FormItem name="key" label="Config Key">
                <Input onPressEnter={queryData}/>
            </FormItem>

            <FormItem>
                <Button type="primary" ghost={true} onClick={queryData}>搜索</Button>
                <Button style={{marginLeft: 8}} onClick={e => create({})}>新建</Button>
            </FormItem>
        </Form>
    );
};

interface ConfigFormProps {
    loading?: boolean;
    visible?: boolean;
    handleOk?: any;
    handleCancel?: any;
    action?: string;
    dataSource?: any;
}

const ConfigForm: React.FC<ConfigFormProps> = props => {
    const [form] = Form.useForm();

    const {dataSource, loading, visible, handleOk, handleCancel, action} = props;
    const {setFieldsValue} = form;

    useEffect(() => {
        const ds = Object.assign({
            id: null,
            idc: null,
            appName: null,
            key: null,
            value: null,
            json: null,
            status: "Active",
        }, dataSource);
        setFieldsValue(ds);
    }, [dataSource, setFieldsValue]);

    return (
        <Modal
            width={600}
            title="Edit Config"
            closable={false}
            confirmLoading={loading}
            visible={visible}
            onOk={e => handleOk(form)}
            onCancel={handleCancel}
        >
            <Form layout="horizontal" {...formItemLayout} form={form}>
                <FormItem name="id" label="ID" style={{display: "none"}}>
                    <Input/>
                </FormItem>

                <FormItem
                    name="idc"
                    label="IDC"
                    rules={[{required: false, message: "Please input config value"}]}
                >
                    <Input id="idc" placeholder="idc"/>
                </FormItem>

                <FormItem
                    name="appName"
                    label="App Name"
                    rules={[{required: false, message: "Please input config value"}]}
                >
                    <Input id="appName" placeholder="App Name(appId)"/>
                </FormItem>

                {action == "create" &&
                    <FormItem
                        name="key"
                        label="Config Key"
                        rules={[{required: true, message: "Please input config key"}]}
                    >
                        <Input placeholder="config key"/>
                    </FormItem>
                }

                {action == "edit" &&
                    <FormItem
                        name="key"
                        label="Config Key"
                        rules={[{required: true, message: "Please input config key"}]}
                    >
                        <Input disabled={true}/>
                    </FormItem>
                }

                <FormItem
                    name="value"
                    label="Config Value"
                    rules={[{required: true, message: "Please input config value"}]}
                >
                    <Input.TextArea placeholder="config value" autoSize={{minRows: 3, maxRows: 6}}/>
                </FormItem>

                <FormItem
                    name="json"
                    label="Extend Config"
                    rules={[{required: false, message: "Please input extend config"}]}
                >
                    <Input.TextArea placeholder="Extend Config" autoSize={{minRows: 3, maxRows: 6}}/>
                </FormItem>

                <FormItem
                    name="status"
                    label="Status"
                    initialValue="Active"
                    rules={[{required: true, message: "Please select Data Source status!"}]}
                >
                    <Select placeholder="Select Type" style={{width: 200}}>
                        <Select.Option key="Active" value="Active">Active</Select.Option>
                        <Select.Option key="Inactive" value="Inactive">Inactive</Select.Option>
                    </Select>
                </FormItem>
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

const ConfigPage: React.FC = props => {
    const searchParams = useRef<any>({});

    const [configList, setConfigList] = useState<any[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [showModal, setShowModal] = useState<boolean>(false);
    const [methodModal, setMethodModal] = useState<boolean>(false);
    const [updateLoading, setUpdateLoading] = useState<boolean>(false);
    const [action, setAction] = useState<string>("create");
    const [config, setConfig] = useState<any>({});
    const [dataSource, setDataSource] = useState<any>({});

    useEffect(() => {
        fetchConfigList();
    }, []);

    const fetchConfigList = () => {
        setLoading(true);
        ConfigService.search(searchParams.current).then((list) => {
            setConfigList(list);
            setLoading(false);
        });
    };

    const handleOk = (form: FormInstance) => {
        form.validateFields().then(values => {
            setUpdateLoading(true);
            saveConfig(values);
        });
    };

    const saveConfig = (target: any) => {
        setUpdateLoading(true);

        ConfigService.create(target).then(() => {
            fetchConfigList();
            setShowModal(false);
            setUpdateLoading(false);
        });
    };

    const handleCancel = () => {
        setShowModal(false);
    };

    const handleTableChange = (pagination: any, filters: any, sorter: any) => {
        searchParams.current = Object.assign({}, searchParams.current, pagination);
        fetchConfigList();
    };

    const handleSearch = (params: any) => {
        if (params) {
            searchParams.current.key = params.key;
        }
        fetchConfigList();
    };

    const createConfig = (data: any) => {
        setDataSource(data);
        setShowModal(true);
        setAction("create");
    };

    const editConfig = (data: any) => {
        setDataSource(data);
        setShowModal(true);
        setAction("edit");
    };

    const deleteConfig = (target: any) => {
        setConfig(target);
        setShowModal(true);
    };

    const deleteModalOk = () => {
        setMethodModal(false);
        if (config) {
            ConfigService.deleteConfig(config.id).then(() => {
                fetchConfigList();
            });
        }
    };

    const deleteModalCancel = () => {
        setMethodModal(false);
    };

    const columns: ColumnProps<any>[] = [
        {
            title: "IDC",
            dataIndex: "idc",
            width: "100px"
        },
        {
            title: "App Name",
            dataIndex: "appName",
            width: "150px"
        },
        {
            title: "Config Key",
            dataIndex: "key",
            width: "150px"
        },
        {
            title: "Config Value",
            dataIndex: "value",
            width: 500
        },
        {
            title: "Extend Config",
            dataIndex: "json",
            width: "200px"
        },
        {
            title: "Updated Time",
            dataIndex: "updatedAt",
            render(val: any) {
                return (<Moment format="YYYY-MM-DD HH:mm:ss">{val}</Moment>);
            },
            width: "170px"
        },
        {
            title: "Status",
            dataIndex: "status",
            fixed: "right",
            align: "center",
            render(val: any) {
                if (val == "Active") {
                    return (<Badge status="success"/>);
                } else {
                    return (<Badge status="error"/>);
                }
            },
            width: "80px"
        },
        {
            title: "Operation",
            fixed: "right",
            render: (text, record, index) => (
                <Button.Group>
                    <Button onClick={e => editConfig(record)}>Edit</Button>
                    <Button danger={true} ghost={true} onClick={e => deleteConfig(record)}>Inactive</Button>
                    <ModalForm
                        title="删除配置"
                        visible={methodModal}
                        icon={<DeleteOutlined />}
                        onOk={deleteModalOk}
                        description={"删除配置为：" + config.key}
                        onCancel={deleteModalCancel}
                    />
                </Button.Group>
            ),
            width: "160px"
        }
    ];

    return (
        <div className="e-monitor-content-sections flex">
            <Card className="e-monitor-content-section">
                <SearchUserForm search={handleSearch} create={createConfig}/>
            </Card>

            <Table
                className="e-monitor-content-section take-rest-height scroll"
                size="small"
                rowKey="id"
                bordered={true}
                loading={loading}
                columns={columns}
                dataSource={configList}
                onChange={handleTableChange}
                scroll={{x: 1400}}
            />

            <ConfigForm
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

export default ConfigPage;