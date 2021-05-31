import isEqual from "lodash/isEqual";
import {Badge, Dropdown, Menu} from "antd";
import StoreManager from "$store/StoreManager";
import LinDBService from "$services/LinDBService";
import React, {useEffect, useMemo, useRef, useState} from "react";
import {ChartInfo, EMonitorMetricTarget} from "$models/ChartModel";

interface SuggestFieldInfo {
    targetIndex: number; // 对应 Target 的 Field 信息
    fields: string[]; // Target 的额外字段
}

// 用于存放 Target 对应的 Field
interface TargetField {
    [measurement: string]: SuggestFieldInfo;
}

const ChartFieldSelect: React.FC<{
    chart: ChartInfo;
    onFieldChange?(targetIndex: number, fields: string[], isOriginal: boolean): void;
}> = props => {
    const {chart, onFieldChange, children} = props;
    const {targets} = chart || {};

    // 保存原始配置字段
    const targetsOriginField = useRef(null);
    const [existedTargetField, setExistedTargetField] = useState<TargetField>({});

    useEffect(() => {
        if (targets && targetsOriginField.current === null) {
            targetsOriginField.current = targets.map(target => target.fields);
        }
    }, [targets]);

    const menuGroups = useMemo(() => targets?.map((target, targetIndex) => {
        if (!targetsOriginField.current) { return null; }

        const handleMenuItemClick = (fields: string[], isOriginal: boolean = false) => {
            onFieldChange && onFieldChange(targetIndex, fields, isOriginal);
        };

        const metricName = getMetricNameBy(target);
        const originFields = targetsOriginField.current[targetIndex];
        const extraFields = existedTargetField?.[metricName]?.fields.filter(field => !originFields.includes(field));

        const currField = target.fields;

        const originFieldsMenu = (
            <Menu.ItemGroup key="originField" title="原始配置的字段">
                <Menu.Item key={originFields} onClick={() => handleMenuItemClick(originFields, true)}>
                    {isEqual(currField, originFields)
                        ? <b><Badge status="processing" text={originFields.join(", ")}/></b>
                        : originFields.join(", ")
                    }
                </Menu.Item>
            </Menu.ItemGroup>
        );

        const extraFieldsMenu = extraFields?.length ? (
            // @ts-ignore
            <Menu.ItemGroup key="extraFields" title="其他可用的字段" style={{maxHeight: 300, overflowY: "auto"}}>
                {extraFields.map(field => (
                    <Menu.Item key={field} onClick={e => handleMenuItemClick([`${e.key}`])}>
                        {currField.includes(field)
                            ? <b><Badge status="processing" text={field}/></b>
                            : field
                        }
                    </Menu.Item>
                ))}
            </Menu.ItemGroup>
        ) : null;

        const targetFieldsMenu = extraFieldsMenu
            ? [originFieldsMenu, <Menu.Divider key="divider"/>, extraFieldsMenu]
            : originFieldsMenu;

        const isMultiTarget = targets.length > 1;

        return isMultiTarget
            ? <Menu.SubMenu key={metricName} title={metricName}>{targetFieldsMenu}</Menu.SubMenu>
            : <React.Fragment key={metricName}>{targetFieldsMenu}</React.Fragment>;

    }), [existedTargetField, onFieldChange, targets]);

    if (!children || !targets || !targetsOriginField.current) { return null; }

    const handleVisibleChange = visible => {
        visible && getFieldData();
    };

    const getFieldData = () => {
        const pendingTargets = targets?.filter(target => {
            if (target.display === false) { return false; }
            return !(getMetricNameBy(target) in existedTargetField);
        });

        if (!pendingTargets?.length) { return; }

        const queries = targets.map(target => {
            const metricName = getMetricNameBy(target);
            existedTargetField[metricName] = null;
            return LinDBService.fetchField(target.entity, metricName);
        });

        // 请求并添加 Fields
        Promise.allSettled(queries).then(res => {
            res.forEach((item, targetIndex) => {
                if (item.status !== "fulfilled" || !item.value) { return; }

                const {measurement, fields} = item.value;
                existedTargetField[measurement] = {
                    targetIndex,
                    fields,
                };
            });

            setExistedTargetField({...existedTargetField});
        });
    };

    return (
        <Dropdown
            placement="bottomRight"
            overlay={<Menu>{menuGroups}</Menu>}
            onVisibleChange={handleVisibleChange}
        >
            {children}
        </Dropdown>
    );
};

// 获取当前 Target Key 值，用于标记是否已经加载过
const getMetricNameBy = (target: EMonitorMetricTarget) => {
    const {prefixVariate, measurement} = target;
    const prefix = prefixVariate ? StoreManager.urlParamStore.getValue(prefixVariate) : target.prefix;

    return prefix ? `${prefix}.${measurement}` : measurement;
};

export default ChartFieldSelect;
