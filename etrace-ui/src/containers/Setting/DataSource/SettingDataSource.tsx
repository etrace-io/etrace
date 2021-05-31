import Moment from "react-moment";
import {ToolKit} from "$utils/Util";
import {FormInstance} from "antd/lib/form";
import {ColumnProps} from "antd/lib/table";
import {useDynamicList, useUpdate} from "ahooks";
import * as DataSourceService from "$services/DataSourceService";
import {DataSource} from "$services/DataSourceService";
import React, {useEffect, useRef, useState} from "react";
import {EMonitorSection} from "$components/EMonitorLayout";
import {DeleteOutlined, PlusOutlined} from "@ant-design/icons/lib";
import {Badge, Button, Card, Form, Input, Modal, Select, Table} from "antd";
import {DATASOURCE_TYPE, LinDB, SimpleJSON} from "$constants/index";

const FormItem = Form.Item;
const formItemLayout = ToolKit.getFormLayout(4);

const DataSourceType: React.FC<any> = props => {
    return (
        <Select placeholder="Select Type" style={{width: 200}} {...props}>
            {DATASOURCE_TYPE.map(item => (
                <Select.Option key={item} value={item}>{item}</Select.Option>
            ))}
        </Select>
    );
};

const SearchForm: React.FC<{
    onSearch: any;
    onCreate: any;
}> = props => {
    const [form] = Form.useForm();
    const {getFieldsValue} = form;
    const {onSearch, onCreate} = props;

    const queryData = () => {
        onSearch(getFieldsValue());
    };

    return (
        <Form layout="inline" form={form}>
            <FormItem name="name" label="Name">
                <Input onPressEnter={queryData} placeholder="Input search name" style={{width: 260}}/>
            </FormItem>

            <FormItem name="type" label="Type">
                <DataSourceType/>
            </FormItem>

            <FormItem>
                <Button type="primary" ghost={true} onClick={queryData}>搜索</Button>
                <Button style={{marginLeft: 10}} onClick={e => onCreate({})}>新建</Button>
            </FormItem>
        </Form>
    );
};

const DataSourceForm: React.FC<{
    loading?: boolean;
    visible?: boolean;
    handleOk?: any;
    handleCancel?: any;
    dataSource?: any;
}> = props => {
    const [form] = Form.useForm();
    const {setFieldsValue, getFieldValue, resetFields} = form;
    const update = useUpdate();

    const {dataSource, loading, visible, handleOk, handleCancel} = props;

    useEffect(() => {
        if (visible) {
            setFieldsValue({
                id: dataSource.id,
                type: dataSource.type,
                status: dataSource.status,
                name: dataSource.name,
                config: dataSource.config
            });
        } else {
            resetFields();
        }
    }, [dataSource, resetFields, setFieldsValue, visible]);

    const type = getFieldValue("type") || dataSource.type;

    return (
        <Modal
            width={800}
            title="Create a datasource"
            closable={false}
            destroyOnClose={true}
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
                    name="type"
                    label="Type"
                    rules={[{required: true, message: "Please select Data Source  type!"}]}
                >
                    <DataSourceType onChange={update}/>
                </FormItem>

                <FormItem
                    name="name"
                    label="Name"
                    rules={[{required: true, message: "Please input Data Source name!"}]}
                >
                    <Input placeholder="Input Data Source name"/>
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

                {type === LinDB && <LinDBPanel config={dataSource.config}/>}
                {type === SimpleJSON && <SimpleJSONPanel/>}
            </Form>
        </Modal>
    );
};

const LinDBPanel: React.FC<{
    config: any;
}> = props => {
    const {config} = props;

    const {list, remove, getKey, push} = useDynamicList(config || []);

    const Row = (index: number, item: any) => (
        <div style={{display: "flex", alignItems: "center", margin: "15px 0"}} key={getKey(index)}>
            <FormItem
                noStyle={true}
                name={["config", getKey(index), "cluster"]}
                rules={[{required: true, message: "Please input cluster of server address."}]}
            >
                <Input placeholder="cluster name" style={{width: "20%"}} />
            </FormItem>

            <Input.Group compact={true} style={{margin: "0 8px", flex: 1, display: "flex"}}>
                <FormItem
                    noStyle={true}
                    name={["config", getKey(index), "address"]}
                    rules={[{required: true, message: "Please input cluster or delete this cluster."}]}
                >
                    <Input style={{flex: 1}} placeholder="Please input server address."/>
                </FormItem>

                <FormItem noStyle={true} name={["config", getKey(index), "status"]} initialValue="master">
                    <Select style={{width: 90}}>
                        <Select.Option key="master" value="master">master</Select.Option>
                        <Select.Option key="slave" value="slave">slave</Select.Option>
                    </Select>
                </FormItem>
            </Input.Group>

            <FormItem
                noStyle={true}
                name={["config", getKey(index), "activeStatus"]}
                initialValue="Active"
                rules={[{required: true, message: "Please input cluster of server address."}]}
            >
                <Select style={{width: 110}}>
                    <Select.Option key="Active" value="Active">Active</Select.Option>
                    <Select.Option key="Inactive" value="Inactive">Inactive</Select.Option>
                </Select>
            </FormItem>

            <Button
                style={{marginLeft: 8}}
                onClick={() => remove(index)}
                size="small"
                type="primary"
                danger={true}
                icon={<DeleteOutlined/>}
                shape="circle"
            />
        </div>
    );

    return (
        <Card title="LinDB Config" bordered={false}>
            {list.map((ele, index) => Row(index, ele))}

            <Button block={true} type="dashed" onClick={() => push({})} icon={<PlusOutlined/>}>
                Add Cluster
            </Button>
        </Card>
    );
};

const SimpleJSONPanel: React.FC = props => {
    return (
        <Card
            size="small"
            title="SimpleJSON Config"
            bordered={false}
        >
            <FormItem
                {...formItemLayout}
                name="config"
                label="DataSource"
                required={false}
                rules={[{required: true, message: "Please input the SimpleJSON HTTP endpoint"}]}
            >
                <Input placeholder="HTTP endpoint" style={{width: "20%", marginRight: 8}}/>
            </FormItem>
        </Card>
    );
};

const SettingDataSourcePage: React.FC = props => {
    const searchParams = useRef({});

    const [datasourceList, setDatasourceList] = useState<any>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [showModal, setShowModal] = useState<boolean>(false);
    const [updateLoading, setUpdateLoading] = useState<boolean>(false);
    const [dataSource, setDataSource] = useState<any>({});

    useEffect(() => {
        fetchDataSourceList();
    }, []);

    const fetchDataSourceList = () => {
        setLoading(true);
        DataSourceService.search(searchParams.current).then(list => {
            setDatasourceList(list);
            setLoading(false);
        });
    };

    const saveDataSource = (data: DataSource) => {
        setUpdateLoading(true);
        DataSourceService.save(data).then(() => {
            fetchDataSourceList();
            setShowModal(false);
            setUpdateLoading(false);
        });
    };

    const handleOk = (form: FormInstance) => {
        form.validateFields().then((values: DataSource) => {
            if (values.type === LinDB) {
                values.config = values.config.filter(c => c !== null);
            }
            saveDataSource(values);
        });
    };

    const handleCancel = () => {
        setShowModal(false);
    };

    const showDataSourceModal = (data: any) => {
        setDataSource(data);
        setShowModal(true);
    };

    const deleteDataSource = (data: any) => {
        DataSourceService.deleteSource(data.id).then(() => {
            fetchDataSourceList();
        });
    };

    const handleSearch = (params: any) => {
        searchParams.current = params;
        fetchDataSourceList();
    };

    const handleTableChange = (pagination: any, filters: any, sorter: any) => {
        const params = {
            pageNum: pagination.current,
            pageSize: pagination.pageSize,
        };

        searchParams.current = Object.assign({}, searchParams.current, params);
        fetchDataSourceList();
    };

    const columns: ColumnProps<any>[] = [
        {
            title: "ID",
            dataIndex: "id",
            align: "center",
            width: "65px"
        },
        {
            title: "DataSource",
            dataIndex: "name",
            width: "170px"
        },
        {
            title: "Type",
            dataIndex: "type",
            width: "100px"
        },
        {
            title: "Config",
            dataIndex: "config",
            render: val => JSON.stringify(val),
        },
        {
            title: "Update Time",
            dataIndex: "updatedAt",
            align: "center",
            render: val => <Moment format="YYYY-MM-DD HH:mm:ss">{val}</Moment>,
            width: "150px"
        },
        {
            title: "Status",
            dataIndex: "status",
            align: "center",
            fixed: "right",
            render: (status: any) => status === "Active" ? <Badge status="success" /> : <Badge status="error" />,
            width: "80px"
        },
        {
            title: "Operation",
            align: "center",
            fixed: "right",
            render: (text, record, index) => (
                <Button.Group>
                    <Button onClick={e => showDataSourceModal(record)}>Edit</Button>
                    <Button ghost={true} danger={true} onClick={e => deleteDataSource(record)}>Inactive</Button>
                </Button.Group>
            ),
            width: "200px"
        }
    ];

    return (
        <EMonitorSection fullscreen={true}>
            {/* Modal */}
            <DataSourceForm
                dataSource={dataSource}
                visible={showModal}
                loading={updateLoading}
                handleOk={handleOk}
                handleCancel={handleCancel}
            />

            <EMonitorSection.Item type="card">
                <SearchForm onSearch={handleSearch} onCreate={showDataSourceModal}/>
            </EMonitorSection.Item>

            <EMonitorSection.Item scroll={true}>
                <Table
                    className="e-monitor-content-section scroll"
                    bordered={true}
                    rowKey="id"
                    loading={loading}
                    columns={columns}
                    dataSource={datasourceList.results}
                    pagination={{total: datasourceList.total}}
                    onChange={handleTableChange}
                    scroll={{x: 1400}}
                />
            </EMonitorSection.Item>
        </EMonitorSection>
    );
};

export default SettingDataSourcePage;
