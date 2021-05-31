import React, {useEffect, useState} from "react";
import Moment from "react-moment";
import {FormInstance} from "antd/es/form";
import {ColumnProps} from "antd/lib/table";
import {ProxyConfig, ProxyService} from "../../../services/ProxyService";
import {Badge, Button, Card, Form, Input, Modal, Select, Table} from "antd";

const Option = Select.Option;
const FormItem = Form.Item;
const ButtonGroup = Button.Group;

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

const ProxyPage: React.FC = props => {
    const [proxyConfigs, setProxyConfigs] = useState<ProxyConfig[]>([]);
    const [selectedProxyConfig, setSelectedProxyConfig] = useState<ProxyConfig>();
    const [loading, setLoading] = useState<boolean>(false);
    const [showModal, setShowModal] = useState<boolean>(false);

    useEffect(() => {
        loadAllProxyConfigs();
    }, []);

    const createProxy = () => {
        setSelectedProxyConfig(null);
        setShowModal(true);
    };

    const loadAllProxyConfigs = () => {
        setLoading(true);
        ProxyService.loadAll().then(data => {
            setProxyConfigs(data);
            setLoading(false);
        });
    };

    const saveOrUpdate = (pc: ProxyConfig) => {
        if (pc.id) {
            ProxyService.update(pc).then(ok => {
                loadAllProxyConfigs();
            });
        } else {
            ProxyService.save(pc).then(ok => {
                loadAllProxyConfigs();
            });
        }
    };

    const handleOk = (form: FormInstance) => {
        form.validateFields().then((values: ProxyConfig) => {
            saveOrUpdate(values);
            setShowModal(false);
        });
    };

    const handleCancel = () => {
        setShowModal(false);
    };

    const showDataSourceModal = (pc: ProxyConfig) => {
        setSelectedProxyConfig(pc);
        setShowModal(true);
    };

    const deleteDataSource = (pc: ProxyConfig) => {
        pc.status = "Inactive";
        saveOrUpdate(pc);
    };

    const columns: ColumnProps<ProxyConfig>[] = [{
        title: "Proxy Path",
        dataIndex: "proxyPath",
        width: "200px"
    }, {
        title: "Server Name",
        dataIndex: "serverName",
        width: "200px"
    }, {
        title: "Path",
        dataIndex: "path",
        width: "200px"
    }, {
        title: "Clusters",
        dataIndex: "clusters",
        width: "100px"
    }, {
        title: "Updated Time",
        dataIndex: "updatedAt",
        render(val: any) {
            return (<Moment format="YYYY-MM-DD HH:mm:ss">{val}</Moment>);
        },
        width: "200px"
    }, {
        title: "Status",
        dataIndex: "status",
        align: "center",
        fixed: "right",
        render(status: any) {
            return status === "Active" ? <Badge status="success" /> : <Badge status="error" />;
        },
        width: "70px"
    }, {
        title: "Operation",
        align: "center",
        fixed: "right",
        render: (text, record, index) => (
            <div>
                <ButtonGroup>
                    <Button onClick={e => showDataSourceModal(record)}>Edit</Button>
                    <Button
                        ghost={true}
                        danger={true}
                        onClick={e => deleteDataSource(record)}
                    >Inactive
                    </Button>
                </ButtonGroup>
            </div>
        ),
        width: "160px"
    }];

    return (
        <div className="e-monitor-content-sections take-rest-height flex">
            <Card className="e-monitor-content-section">
                <Button style={{float: "right"}} type="primary" onClick={createProxy}>新建</Button>
            </Card>

            <Table
                className="e-monitor-content-section scroll"
                bordered={true}
                rowKey="id"
                loading={loading}
                columns={columns}
                dataSource={proxyConfigs}
                scroll={{x: 1400}}
            />

            <DataSourceForm
                dataSource={selectedProxyConfig}
                visible={showModal}
                loading={loading}
                handleOk={handleOk}
                handleCancel={handleCancel}
            />
        </div>
    );
};

export default ProxyPage;

interface DataSourceFormProps {
    loading?: boolean;
    visible?: boolean;
    handleOk: any;
    handleCancel: any;
    dataSource: ProxyConfig;
}

const DataSourceForm: React.FC<DataSourceFormProps> = props => {
    const [form] = Form.useForm();
    const {setFieldsValue} = form;

    const {dataSource, loading, visible, handleOk, handleCancel} = props;

    useEffect(() => {
        setFieldsValue(dataSource);
    }, [dataSource, setFieldsValue]);

    return (
        <Modal
            width={800}
            title="Create or Update a Proxy Config"
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
                    name="proxyPath"
                    label="Proxy Path"
                    rules={[{required: true, message: "Please input proxy path"}]}
                >
                    <Input id="proxy path"/>
                </FormItem>

                <FormItem
                    name="serverName"
                    label="Server Name"
                    rules={[{required: true, message: "Please input server name"}]}
                >
                    <Input id="server name"/>
                </FormItem>

                <FormItem
                    name="path"
                    label="Path"
                    rules={[{required: true, message: "Please input path"}]}
                >
                    <Input id="path"/>
                </FormItem>

                <FormItem
                    name="clusters"
                    label="clusters"
                    rules={[{required: true, message: "Please input clusters（用'逗号'分割)"}]}
                >
                    <Input id="clusters"/>
                </FormItem>

                <FormItem
                    name="status"
                    label="Status"
                    initialValue="Active"
                    rules={[{required: true, message: "Please select Data Source status!"}]}
                >
                    <Select placeholder="Select Type" style={{width: 200}}>
                        <Option value="Active">Active</Option>
                        <Option value="Inactive">Inactive</Option>
                    </Select>
                </FormItem>
            </Form>
        </Modal>
    );
};
