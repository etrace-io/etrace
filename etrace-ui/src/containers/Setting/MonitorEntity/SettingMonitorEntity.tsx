import React, {useEffect, useState} from "react";
import {get} from "lodash";
import {ToolKit} from "$utils/Util";
import {Link} from "react-router-dom";
import {FormInstance} from "antd/es/form";
import {EMonitorSection} from "$components/EMonitorLayout";
import * as MonitorEntityService from "$services/MonitorEntityService";
import {MonitorEntity} from "$services/MonitorEntityService";
import * as DataSourceService from "$services/DataSourceService";
import {Button, Form, Input, Select, Spin, Tree} from "antd";
import {EyeOutlined, FolderViewOutlined, PlusOutlined, SaveOutlined} from "@ant-design/icons/lib";
import {useUpdate} from "ahooks";

const formItemLayout = ToolKit.getFormLayout(6);

const renderEntities = (entityList) => {
    return entityList && entityList.map(entity => {
        const hasChildren = entity.children && entity.children.length > 0;
        return {
            title: entity.name,
            key: entity.id,
            icon: entity.type === "Category" ? <FolderViewOutlined /> : <EyeOutlined/>,
            children: hasChildren ? renderEntities(entity.children) : null,
            entity
        };
    });
};

const SettingMonitorEntityPage: React.FC = props => {
    const [dataSourceFetching, setDataSourceFetching] = useState<boolean>();
    const [datasourceList, setDatasourceList] = useState<any[]>();
    const [entityList, setEntityList] = useState<any>();
    const [selectedEntity, setSelectedEntity] = useState<Partial<MonitorEntity>>({});
    const [showEntityForm, setShowEntityForm] = useState<boolean>(false);
    const [form] = Form.useForm();

    const {getFieldValue, setFieldsValue, resetFields, validateFields} = form;

    const ROOT = {name: "ROOT", code: "ROOT", type: "Category", id: 0, parentId: 0};

    useEffect(() => {
        fetchEntity();
        loadDataSource();
    }, []);

    const selectEntity = (selectedKeys: any, info: any) => {
        const entity = info.node.entity;
        setSelectedEntity(entity);
        resetForm(entity);
    };

    const resetForm = (entity: Partial<MonitorEntity>) => {
        resetFields();
        setFieldsValue({
            id: entity.id,
            parentId: entity.parentId,
            type: entity.type,
            name: entity.name,
            code: entity.code,
            database: entity.database,
            datasourceId: entity.datasourceId,
            metaUrl: entity.metaUrl,
            config: entity.config,
            metaPlaceholder: entity.metaPlaceholder,
            aliasCode: entity.aliasCode,
        });
        setShowEntityForm(entity.type === "Entity");
    };

    const addEntity = () => {
        resetForm({parentId: get(selectedEntity, "id")});
        setSelectedEntity(null);
    };

    const handleSaveEntity = () => {
        validateFields().then(saveEntity);
    };

    const saveEntity = (entity: MonitorEntity) => {
        MonitorEntityService.save(entity).then(() => {
            fetchEntity();
        });
    };

    const fetchEntity = () => {
        MonitorEntityService.fecth().then(setEntityList);
    };

    const loadDataSource = () => {
        setDataSourceFetching(true);
        DataSourceService.fetch().then(list => {
            setDatasourceList(list);
            setDataSourceFetching(false);
        });
    };

    const handleValuesChange = (changedValues, allValues) => {
        setShowEntityForm(allValues.type === "Entity");
    };

    const treeData = [{
        title: "ROOT",
        key: "ROOT",
        icon: <FolderViewOutlined />,
        children: renderEntities(entityList),
        entity: ROOT,
    }];

    const addChildBtn = (
        <Button
            icon={<PlusOutlined/>}
            onClick={addEntity}
            disabled={getFieldValue("type") !== "Category"}
        >添加子监控项
        </Button>
    );

    return (
        <EMonitorSection fullscreen={true} mode="horizontal">
            <EMonitorSection.Item width="35%" type="card" scroll={true}>
                <Tree
                    defaultExpandedKeys={["ROOT"]}
                    onSelect={selectEntity}
                    showLine={{showLeafIcon: false}}
                    showIcon={true}
                    treeData={treeData}
                />
            </EMonitorSection.Item>

            <EMonitorSection.Item title={selectedEntity ? selectedEntity.name : ""} type="card" scroll={true} extra={addChildBtn}>
                <Form form={form} {...formItemLayout} onValuesChange={handleValuesChange}>
                    <Form.Item name="id" label="ID" style={{display: "none"}}>
                        <Input/>
                    </Form.Item>

                    <Form.Item
                        name="parentId"
                        label="Parent ID"
                        rules={[{required: true, message: "Please select parent from tree!"}]}
                    >
                        <Input/>
                    </Form.Item>

                    <Form.Item
                        name="name"
                        label="Name"
                        rules={[{required: true, message: "Please input name!"}]}
                    >
                        <Input/>
                    </Form.Item>

                    <Form.Item
                        name="code"
                        label="Code"
                        rules={[{required: true, message: "Please input code!"}]}
                    >
                        <Input disabled={getFieldValue("id") > -1}/>
                    </Form.Item>

                    <Form.Item
                        name="type"
                        label="Type"
                        rules={[{required: true, message: "Please select type!"}]}
                    >
                        <Select placeholder="Select Type" disabled={getFieldValue("id") > -1}>
                            <Select.Option value="Category">Category</Select.Option>
                            <Select.Option value="Entity">Entity</Select.Option>
                        </Select>
                    </Form.Item>

                    {/*{renderEntityForm()}*/}

                    {showEntityForm && (
                        <EntityForm
                            form={form}
                            loading={dataSourceFetching}
                            dataSource={datasourceList}
                            onFocus={loadDataSource}
                            entityList={entityList}
                        />
                    )}

                    <Form.Item wrapperCol={{offset: 6}}>
                        <Button
                            type="primary"
                            icon={<SaveOutlined/>}
                            ghost={true}
                            onClick={handleSaveEntity}
                        >保存
                        </Button>
                    </Form.Item>
                </Form>
            </EMonitorSection.Item>
        </EMonitorSection>
    );
};

const EntityForm: React.FC<{
    form: FormInstance;
    loading?: boolean;
    dataSource?: any[];
    onFocus?: () => void;
    entityList: any[];
}> = props => {

    const updater = useUpdate();

    const {form, loading, dataSource, onFocus, entityList} = props;

    const {getFieldValue} = form;

    const selectedAliasCode = getFieldValue("aliasCode");

    const metaTypeFormat = (rule, value, callback) => {
        if (value) {
            const metaType = getFieldValue("config");
            try {
                JSON.parse(metaType);
            } catch (err) {
                callback("数据格式不正确");
            }
        }
        callback();
    };

    return (
        <>
            <Form.Item
                name="config"
                label="Meta Type"
                rules={[{validator: metaTypeFormat}]}
            >
                <Input.TextArea autoSize={{minRows: 2, maxRows: 6}}/>
            </Form.Item>

            <Form.Item name="metaUrl" label="Meta Url">
                <Input/>
            </Form.Item>

            <Form.Item name="metaPlaceholder" label="Meta Placeholder">
                <Input/>
            </Form.Item>

            <Form.Item label="DataSource">
                <Input.Group compact={true}>
                    <Form.Item
                        noStyle={true}
                        name="datasourceId"
                        rules={[{required: true, message: "Please select data source!"}]}
                    >
                        <Select
                            placeholder="Select data source"
                            showSearch={true}
                            style={{width: "calc(100% - 100px)"}}
                            onFocus={onFocus}
                            notFoundContent={loading && <Spin size="small"/>}
                        >
                            {dataSource.map(ds =>
                                <Select.Option key={ds.id} value={ds.id}>{ds.name}</Select.Option>
                            )}
                        </Select>
                    </Form.Item>
                    <Link to="/setting/datasource" target="_blank">
                        <Button style={{width: 100}}>新建数据源</Button>
                    </Link>
                </Input.Group>
            </Form.Item>

            <Form.Item
                name="database"
                label="Database"
                rules={[{required: true, message: "Please input database!"}]}
            >
                <Input/>
            </Form.Item>
            <Form.Item label="AliasCode">
                <Form.Item name="aliasCode" noStyle={true}>
                <Select
                    placeholder="Select Existed Code"
                    showSearch={false}
                    style={{width: "50%"}}
                    onFocus={onFocus}
                    notFoundContent={loading && <Spin size="small"/>}
                    onChange={updater}
                    allowClear={true}
                >
                    {entityList.map((entity, index) =>
                        <Select.Option key={index} value={entity.code}>{entity.name}-{entity.code}</Select.Option>
                    )}
                </Select>
                </Form.Item>
                {selectedAliasCode &&
                <span style={{width: "50%", margin: "5px"}}>即查询<b>{selectedAliasCode}</b>时也会查询该数据源。</span>
                }
            </Form.Item>
        </>
    );
};

export default SettingMonitorEntityPage;
