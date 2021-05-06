import React, {useMemo} from "react";
import {observer} from "mobx-react";
import {Cascader, Tooltip} from "antd";
import StoreManager from "$store/StoreManager";
import {MonitorEntity} from "$services/MonitorEntityService";

const buildEntities = (list: MonitorEntity[]) => {
    if (!list || list.length === 0) { return; }

    list.forEach(item => {
        item.disabled = item.status === "Inactive";
        item.value = item.name;

        if (item.config) {
            const meteConfig = JSON.parse(item.config);
            if (meteConfig.description) {
                item.description = meteConfig.description;
            }
        }

        // div 为了撑开 tooltip 响应区域
        item.label = <Tooltip placement="left" color="blue" title={item.description || item.name}>
            <div>{item.name}</div>
        </Tooltip>;

        if (item.type === "Category" && (!item.children || item.children.length <= 0)) {
            item.disabled = true;
        }

        if (item.children && item.children.length > 0) {
            buildEntities(item.children);
        }
    });

    return list;
};

interface EntitySelectorProps {
    databaseName: string;
    disabled: boolean;
    selectMonitorEntity?: (idx: number, value: string[], selectedOptions: any) => void;
    entityIndex?: number;
    style?: any;
}

const EntitySelector: React.FC<EntitySelectorProps> = props => {
    const {databaseName, disabled, selectMonitorEntity, entityIndex, style} = props;
    const { monitorEntityStore } = StoreManager;

    const entities = useMemo<MonitorEntity[]>(() => buildEntities(monitorEntityStore.entityTree), [monitorEntityStore.entityTree]);

    const handleChange = (v, selectedOptions) => selectMonitorEntity(entityIndex, v, selectedOptions);

    const searchFilter = (v: string, path: any) => (
        path.some(option => option.name.toLowerCase().indexOf(v.toLowerCase()) > -1)
    );

    const displayRender = (_, path: any) => path.map(i => i.value).join(" / ");

    return (
        <Cascader
            value={monitorEntityStore.findEntity(databaseName, entities)}
            allowClear={false}
            options={entities}
            placeholder="请选择监控类型"
            expandTrigger="hover"
            displayRender={displayRender}
            style={style}
            disabled={disabled}
            onChange={handleChange}
            showSearch={{ filter: searchFilter, sort: () => 1, render: displayRender }}
        />
    );
};

export default observer(EntitySelector);
