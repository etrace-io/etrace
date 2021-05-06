import {observer} from "mobx-react";
import {Target} from "$models/ChartModel";
import StoreManager from "$store/StoreManager";
import {clone, debounce, find, get} from "lodash";
import * as MetaService from "$services/MetaService";
import * as LinDBService from "$services/LinDBService";
import {calLineSequence} from "$utils/ChartDataConvert";
import React, {ReactText, useEffect, useState} from "react";
import TitleWithTooltip from "$components/Base/TitleWithTooltip";
import {FunctionSelect} from "$components/select/FunctionSelect";
import EntitySelector from "$components/EntitySelector/EntitySelector";
import TagFilterSelect from "$components/Board/Explorer/TargetOption/TagFilterSelect";
import TargetOptionItem from "$components/Board/Explorer/TargetOption/TargetOptionItem";
import {Button, Cascader, Checkbox, Dropdown, Form, Input, Menu, message, Row, Tooltip} from "antd";
import {ConvertFunctionModel, findConverFunctionModelByName, FUNCTIONS} from "$utils/ConvertFunction";
import {BarsOutlined, DeleteOutlined, EyeInvisibleOutlined, EyeOutlined, PlusOutlined} from "@ant-design/icons/lib";

import "./TargetOption.less";
import TargetKit from "$utils/UtilKit/TargetKit";

const SAMPLING_CONFIG_KEY = "sampling_config";

const TOOLTIP = {
    // SimpleJSON
    SimpleJSON: <span>实现了<a href="https://github.com/grafana/simple-json-datasource" target="_blank">Grafana Simple JSON</a>的数据源</span>,
    // Prometheus PromQL
    PromQL: <span>实现了<a href="https://prometheus.io/docs/prometheus/latest/querying/basics/" target="_blank">PromQL</a>的数据源</span>,
    // 监控项
    PREFIX: "与监控类型有关，具体选择的时候会提示需要用户输入什么，如监控类型为应用层的数据，监控项为 AppID，如果没有提示需要输监控项，则直接输入指标名即可。",
    // 指标名
    MEASUREMENT: "指业务上打点数据，或者框架组件的数据等，选完监控类型或者监控项之后会带出相关的指标名，支持前缀查询过滤，提示前 100 的指标。",
    // 字段
    FIELDS: <span>当前指标下的字段，一个指标可以有多个字段，选择指标名之后会带出该指标下所有的字段，可以选择多个字段，也可以对字段进行表达式或者函数运行，<a rel="noopener noreferrer" href="https://monitor-doc.faas.elenet.me/manual/e-monitor/board/explorer.html#%E5%AD%97%E6%AE%B5" target="_blank">详细说明。</a></span>,
    // 过滤条件
    TAG_FILTER: "主要是指对 tags 数据的过滤，这个可以理解成对数据库某个表中的字段的过滤，条件目前支持全匹配和前缀匹配，也可以多选。",
    // Group By
    GROUP_BY: "类似数据库的 Group By 操作，这里主要对 Tag 的 Key 进行分组，就会按每个组合进行分组展现，如果选择的分组很多，并且组合很多的话，会显示前几的数据， 因为一个图上有很多线的时候已经很难看清了，所以这时可以结合 order by 来使用，显示 TopN 的数据。",
    // Order By
    ORDER_BY: <span>可以对 group by 出来的多根曲线进行排序，可以在 Order By 上加 time 函数，目前支持的函数：t_sum、t_min、t_max、t_gauge 等，<a rel="noopener noreferrer" href="https://monitor-doc.faas.elenet.me/manual/e-monitor/board/explorer.html#Order_By" target="_blank">详细说明。</a></span>,
    // 变量
    VARIATE: "可以对 tag key 设置成变量，主要用于把指标加到看板中，可以选择看板的变量来过滤指标中的数据，如果指标的条件是固定的，直接在过滤条件中添加即可。",
    // 函数
    FUNCTIONS: <a rel="noopener noreferrer" href="https://monitor-doc.faas.elenet.me/manual/e-monitor/board/explorer.html#%E5%B1%95%E7%A4%BA%E5%B1%82%E7%9A%84%E5%87%BD%E6%95%B0" target="_blank">函数详细说明</a>,
    // 采样
    SAMPLING: "目前只支持 Trace 和 Dal 监控",
};

const FUNCTIONS_OPTIONS = [
    {value: "alias", label: "Alias", children: FUNCTIONS.ALIAS.map(item => ({value: item.name, label: item.name}))},
    {value: "special", label: "Special", children: FUNCTIONS.SPECIAL.map(item => ({value: item.name, label: item.name}))},
    {value: "Transform", label: "Transform", children: FUNCTIONS.TRANSFORM.map(item => ({value: item.name, label: item.name}))},
];

interface TargetOptionProps {
    index: number;
    target: Target;
    singleMode?: boolean; // 用于只配置一个 Target 的 Case（隐藏序号、新增 Target 等）
}

const TargetOption: React.FC<TargetOptionProps> = props => {
    const {target, index, singleMode} = props;

    const {editChartStore, monitorEntityStore, configStore} = StoreManager;

    const [tagKeys, setTagKeys] = useState<string[]>([]);
    const [fieldKeys, setFieldKeys] = useState<string[]>([]);
    const [isSunfire, setIsSunfire] = useState<boolean>(() => TargetKit.isSimpleJsonTarget(target));
    const [isPrometheus, setIsPrometheus] = useState<boolean>(TargetKit.isPrometheusTarget(target));

    useEffect(() => {
        if (target && target.entity && target.measurement) {
            loadMeta(target);
        }
        loadSamplingConfig();
    }, []);

    const fetchMeta = async (value: string, metaUrl: string) => {
        return await MetaService.getMeta(metaUrl + value) || [];
    };

    const loadMeta = (t: Target) => {
        const newTarget = clone(t);
        editChartStore.showTagKeys(newTarget).then(keys => {
            setTagKeys(keys);
        });
        editChartStore.showFields(newTarget).then(keys => {
            if (keys.indexOf("timerSum") > -1 && keys.indexOf("timerCount") > -1) {
                keys.push("timerSum/timerCount");
            }
            setFieldKeys(keys);
            // setFieldKeysSearch(keys);
        });
    };

    const loadSamplingConfig = () => {
        configStore.loadConfig(SAMPLING_CONFIG_KEY);
    };

    /**
     * 选择监控项
     * @param idx
     * @param value
     * @param selectedOptions
     */
    const handleSelectMonitorEntity = (idx: number, value: Array<string>, selectedOptions: any) => {
        const entity = selectedOptions[selectedOptions.length - 1];

        // Reset
        target.fields = [];
        target.tagFilters = [];
        target.measurement = null;
        target.groupBy = [];
        target.orderBy = "";
        target.prefix = null;
        target.variate = [];
        target.functions = [];
        target.entity = entity.code;
        target.prefixRequired = !!entity.metaUrl;
        target.metricType = null;

        editChartStore.setTarget(idx, target);
        setIsSunfire(TargetKit.isSimpleJsonTarget(target));
        setIsPrometheus(TargetKit.isPrometheusTarget(target));
    };

    const handleMQLChange = debounce((idx: number, value: any) => {
        target.measurement = value;

        target.fields = ["mock_value"]; // 不然后面的 Target.valid(target) 过不了
        editChartStore.setTarget(idx, target);
    }, 500);

    const handlePrefixSearch = (t: Target, value: string) => {
        const entity = monitorEntityStore.findEntityByCode(t.entity);
        return fetchMeta(value, entity.metaUrl);
    };

    const handleSelectMetaEntity = (idx: number, value: any) => {
        const chart = editChartStore.getChart();
        const t = chart.targets[idx];
        if (t.prefix === value) {
            return;
        }
        t.prefix = value;
        chart.targets[idx] = t;
        editChartStore.setChartChange(chart);
    };

    const handleMetricNameSearch = async (t: Target, value: string) => {
        let newTarget = clone(t);
        newTarget.prefix = t.prefix;
        newTarget.measurement = value;
        if (newTarget.prefix || !newTarget.prefixRequired) {
            return LinDBService.searchMeasurement(newTarget);
        }
    };

    const handleSelectMeasurement = (idx: number, value: string) => {
        const chart = editChartStore.getChart();
        const t = chart.targets[idx];
        if (t.measurement === value) {
            return;
        }
        t.measurement = value;
        if (t.measurement) {
            loadMeta(t);
        }
        editChartStore.setTarget(idx, t);
    };

    const handleFieldChange = (idx: number, value: any) => {
        target.fields = value ? value : [];
        target.metricType = null;
        editChartStore.setTarget(idx, target);
    };

    const handleTagFilterValueChange = (idx: number, filterIndex: number, value: any) => {
        const tagFilter = target.tagFilters[filterIndex];
        tagFilter.value = value;
        editChartStore.setTarget(idx, target);
    };

    const handleDeleteTagFilter = (idx: number, filterIndex: number) => {
        target.tagFilters.splice(filterIndex, 1);
        editChartStore.setTarget(idx, target);
    };

    const handleTagFilterOptionChange = (idx: number, filterIndex: number, value: any) => {
        const tagFilter = target.tagFilters[filterIndex];
        tagFilter.op = value;
        editChartStore.setTarget(idx, target);
    };

    const handleAddTagFilter = (key: ReactText) => {
        const tagFilters = target.tagFilters ? target.tagFilters : [];
        const variate = find(tagFilters, {key});
        if (!variate) {
            tagFilters.push({key: key + "", op: "=", value: [], display: true});
            target.tagFilters = tagFilters;
            editChartStore.setTarget(index, target);
        } else {
            message.info(key + " 已经添加！");
        }
    };

    const handleGroupByChange = (idx: number, value: any) => {
        target.groupBy = value ? value : [];
        editChartStore.setTarget(idx, target);
    };

    const handleOrderByChange = (idx: number, value: any) => {
        target.orderBy = value;
        editChartStore.setTarget(idx, target);
    };

    const handleVariateChange = (idx: number, variate: any) => {
        target.variate = variate ? variate : [];
        editChartStore.setTarget(idx, target);
    };

    const handleFunctionChange = (targetIndex: number, functionIndex: number, fun: ConvertFunctionModel) => {
        target.functions[functionIndex] = fun;
        editChartStore.setTarget(targetIndex, target);
    };

    const handleFunctionRemove = (targetIndex: number, functionIndex: number) => {
        target.functions.splice(functionIndex, 1);
        editChartStore.setTarget(targetIndex, target);
    };

    const handleFunctionSelect = (idx: number) => {
        return (key: string[]) => {
            const functionModel: ConvertFunctionModel = findConverFunctionModelByName(key[key.length - 1]);
            const model = clone(functionModel);
            const functions = target.functions || [];

            functions.push(model);
            target.functions = functions;
            editChartStore.setTarget(idx, target);
        };
    };

    const isSampling = (entity: any): boolean => {
        const config = JSON.parse(get(entity, "config", "{}"));
        return !!(config.sampling);
    };

    const metricTypeChange = (idx: number, value: any) => {
        const configs: Array<any> = configStore.getConfig(SAMPLING_CONFIG_KEY);
        let config;
        if (configs && configs.length > 0) {
            const configStr = configs[0];
            config = JSON.parse(configStr.value);
        }

        if (target.fields && target.fields.length > 0) {
            target.metricType = value.target.checked && config
                ? getMetricType(config, target.fields[0])
                : null;
            editChartStore.setTarget(idx, target);
        }
    };

    const getMetricType = (config: any, field: string): string => {
        let metricType = null;
        Object.keys(config).forEach(key => {
            if (field.search(config[`${key}`]) >= 0) {
                metricType = key;
            }
        });

        if (!metricType) {
            if (field.indexOf("histogram") >= 0 || field.indexOf("upper") >= 0) {
                return "histogram";
            } else if (field.indexOf("timer") >= 0) {
                return "timer";
            } else if (field.indexOf("payload") >= 0) {
                return "payload";
            } else if (field.indexOf("count") >= 0) {
                return "counter";
            } else if (field.indexOf("gauge") >= 0) {
                return "gauge";
            } else {
                return "counter";
            }
        }

        return metricType;
    };

    const toggleDisplayTarget = () => {
        target.display = target.display === undefined ? false : !target.display;
        editChartStore.setTarget(index, target);
    };

    const display = get(target, "display", true);
    // let selectedEntity = {metaPlaceholder: ""};

    const selectedEntity = target.entity ? monitorEntityStore.findEntityByCode(target.entity) : null;
    if (selectedEntity && selectedEntity.config) {
        const config = JSON.parse(selectedEntity.config);
        target.prefixRequired = !!(config && config.url);
    }

    const menu = (
        <Menu>
            <Menu.Item>
                <Tooltip title="复制当前指标" placement="left">
                    <a onClick={() => editChartStore.duplicateTarget(target)}>复制</a>
                </Tooltip>
            </Menu.Item>

            <Menu.Item>
                <Tooltip title="上移当前指标" placement="left">
                    <a onClick={() => editChartStore.moveTarget("up", index)}>上移</a>
                </Tooltip>
            </Menu.Item>

            <Menu.Item>
                <Tooltip title="下移当前指标" placement="left">
                    <a onClick={() => editChartStore.moveTarget("down", index)}>下移</a>
                </Tooltip>
            </Menu.Item>
        </Menu>
    );

    const tagFilterMenu = (
        <Menu onClick={({key}) => handleAddTagFilter(key)}>
            {tagKeys && tagKeys.map((col: any) =>
                find(get(target, "tagFilters", []), {key: col})
                    ? null
                    : <Menu.Item key={col}>{col}</Menu.Item>
            )}
            {(!tagKeys || tagKeys.length === 0) && (
                <Menu.Item disabled={true}>暂无可选项目</Menu.Item>
            )}
        </Menu>
    );

    return (
        <Row className="explorer__metrics-config">
            <Form layout="inline" style={{opacity: !display ? 0.35 : 1}}>
                {!singleMode && (
                    <Form.Item>
                        <Button type="dashed" shape="circle" disabled={true}>{calLineSequence(index)}</Button>
                    </Form.Item>
                )}

                <Form.Item label="监控类型" required={true}>
                    <EntitySelector
                        databaseName={target.entity}
                        disabled={!display}
                        selectMonitorEntity={handleSelectMonitorEntity}
                        entityIndex={index}
                    />
                </Form.Item>

                <TargetOptionItem
                    visible={isSunfire}
                    required={true}
                    label={<TitleWithTooltip title="SimpleJSON" tooltip={TOOLTIP.SimpleJSON}/>}
                    value={target.measurement}
                    target={target}
                    index={index}
                    textarea={true}
                    onChange={handleMQLChange} // mql is stored in the prefix field
                />

                <TargetOptionItem
                    visible={isPrometheus}
                    required={true}
                    label={<TitleWithTooltip title="PromQL" tooltip={TOOLTIP.PromQL}/>}
                    value={target.measurement}
                    target={target}
                    index={index}
                    textarea={true}
                    onChange={handleMQLChange} // mql is stored in the prefix field
                />

                <TargetOptionItem
                    visible={Boolean(!isSunfire && !isPrometheus && target.prefixRequired)}
                    required={true}
                    label={<TitleWithTooltip title="监控项" tooltip={TOOLTIP.PREFIX}/>}
                    value={target.prefix}
                    target={target}
                    index={index}
                    placeholder={selectedEntity ? selectedEntity.metaPlaceholder : null}
                    onSearch={handlePrefixSearch}
                    onBlur={handleSelectMetaEntity}
                />

                <TargetOptionItem
                    visible={!isSunfire && !isPrometheus}
                    required={true}
                    label={<TitleWithTooltip title="指标名" tooltip={TOOLTIP.MEASUREMENT}/>}
                    value={target.measurement}
                    placeholder="请输入指标名"
                    target={target}
                    index={index}
                    onFocus={handleMetricNameSearch}
                    onSearch={handleMetricNameSearch}
                    onSelect={handleSelectMeasurement}
                    onBlur={handleSelectMeasurement}
                />

                <TargetOptionItem
                    visible={!isSunfire && !isPrometheus}
                    required={true}
                    label={<TitleWithTooltip title="字段" tooltip={TOOLTIP.FIELDS}/>}
                    value={target.fields}
                    target={target}
                    filterOption={true}
                    index={index}
                    tagMode={true}
                    placeholder="请添加检索字段"
                    onFocus={() => fieldKeys}
                    onChange={handleFieldChange}
                />

                {!isSunfire && !isPrometheus && (
                    <Form.Item label={<TitleWithTooltip title="过滤条件" tooltip={TOOLTIP.TAG_FILTER}/>}>
                        {target.tagFilters && target.tagFilters.map((keyValue, filterIndex) => (
                            <TagFilterSelect
                                key={index * 10 + filterIndex}
                                value={keyValue.value}
                                target={target}
                                index={index}
                                filterIndex={filterIndex}
                                tagFilter={keyValue}
                                showTagValues={editChartStore.showTagValues}
                                onBlur={handleTagFilterValueChange}
                                onChange={handleTagFilterValueChange}
                                deleteTagFilter={handleDeleteTagFilter}
                                onOptionChange={handleTagFilterOptionChange}
                                disabled={!display}
                            />
                        ))}

                        <Dropdown
                            overlay={tagFilterMenu}
                            disabled={!display}
                        >
                            <Button style={{minWidth: 32}} block={true} icon={<PlusOutlined />} type="dashed"/>
                        </Dropdown>
                    </Form.Item>
                )}

                <TargetOptionItem
                    visible={!isSunfire && !isPrometheus}
                    label={<TitleWithTooltip title="Group By" tooltip={TOOLTIP.GROUP_BY}/>}
                    value={target.groupBy}
                    target={target}
                    index={index}
                    filterOption={true}
                    tagMode={true}
                    placeholder="请选择 Group By 的 Tag Key"
                    onFocus={() => tagKeys}
                    onChange={handleGroupByChange}
                />

                <TargetOptionItem
                    visible={!isSunfire && !isPrometheus}
                    label={<TitleWithTooltip title="Order By" tooltip={TOOLTIP.ORDER_BY}/>}
                    value={target.orderBy}
                    target={target}
                    index={index}
                    filterOption={true}
                    placeholder="请选择曲线排序方式"
                    onFocus={() => fieldKeys.map(key => `${key} DESC limit 10`)}
                    onBlur={handleOrderByChange}
                    onSelect={handleOrderByChange}
                />

                <TargetOptionItem
                    visible={!isSunfire}
                    tagMode={true}
                    label={<TitleWithTooltip title="变量" tooltip={TOOLTIP.VARIATE}/>}
                    value={target.variate}
                    target={target}
                    placeholder="请选择需要变量"
                    index={index}
                    showNotFoundToolTip={true}
                    onFocus={() => tagKeys}
                    onChange={handleVariateChange}
                />

                {!isSunfire && (
                    <Form.Item label={<TitleWithTooltip title="函数" tooltip={TOOLTIP.FUNCTIONS}/>}>
                        {target.functions && target.functions.map((modal, indexF) => {
                            const func = (<FunctionSelect
                                key={indexF + "=" + index * 10}
                                target={target}
                                fun={modal}
                                index={index}
                                chartUniqueId={editChartStore.chartUniqueId}
                                funIndex={indexF}
                                changeFunction={handleFunctionChange}
                                removeFunction={handleFunctionRemove}
                            />);

                            return modal.name === "compute"
                                ? <Tooltip title="不支持饼图、雷达图、文本计算" key={indexF + "=" + index * 10}>{func}</Tooltip>
                                : func;
                        })}

                        <Cascader options={FUNCTIONS_OPTIONS} onChange={handleFunctionSelect(index)}>
                            <Button icon={<PlusOutlined />} type="dashed"/>
                        </Cascader>
                    </Form.Item>
                )}

                {isSampling(selectedEntity) && (target.fields && target.fields.length > 0) && (
                    <Form.Item label={<TitleWithTooltip title="采样" tooltip={TOOLTIP.SAMPLING}/>}>
                        <Checkbox
                            checked={!!target.metricType}
                            onChange={(value) => metricTypeChange(index, value)}
                        />
                    </Form.Item>
                )}
            </Form>

            {!singleMode && (
                <Button.Group style={{marginLeft: "auto", flexShrink: 0, alignSelf: "center"}}>
                    <Dropdown overlay={menu}>
                        <Button icon={<BarsOutlined />}/>
                    </Dropdown>
                    <Tooltip title={!display ? "显示指标" : "隐藏指标"}>
                        <Button
                            icon={!display ? <EyeOutlined /> : <EyeInvisibleOutlined />}
                            onClick={toggleDisplayTarget}
                        />
                    </Tooltip>
                    <Tooltip title="删除当前指标" placement="topRight">
                        <Button
                            icon={<DeleteOutlined />}
                            danger={true}
                            onClick={() => editChartStore.deleteTarget(index)}
                        />
                    </Tooltip>
                </Button.Group>
            )}
        </Row>
    );
};

export default observer(TargetOption);
