import React from "react";
import {observer} from "mobx-react";
import StoreManager from "$store/StoreManager";
import {Form, InputNumber, Select, Tooltip} from "antd";
import TitleWithTooltip from "$components/Base/TitleWithTooltip";
import {ToolKit} from "$utils/Util";

const sortType = ["DESC", "ASC"];
const functionType = [
    {label: "和值", value: "t_sum", description: "将数据线所有时间点上的数据相加得到一个总和"},
    {label: "最大值", value: "t_max", description: "获取数据线上的最大值"},
    {label: "瞬时值", value: "t_gauge", description: "获取数据线上的瞬时值"},
    {label: "平均值", value: "t_mean", description: "将数据线所有时间点上的数据的平均值"},
    {label: "标准差", value: "t_stddev", description: "将数据线所有时间点上的数据求标准差"},
    {label: "波动值", value: "variance", description: "计算数据线的波动值"},
];

const OrderBySetting: React.FC = props => {
    const {orderByStore} = StoreManager;

    const handleSortChange = (value: string) => {
        if (orderByStore.sort !== value) {
            orderByStore.sort = value;
            dispatchChange();
        }
    };

    const handleFunctionChange = (value: string) => {
        if (orderByStore.type !== value) {
            orderByStore.type = value;
            dispatchChange();
        }
    };

    const handleDataLimitChange = (value: number | string) => {
        orderByStore.limit = Math.min(Math.max(1, +value), 100);
    };

    const dispatchChange = () => {
        StoreManager.urlParamStore.forceChange();
    };

    return (
        <Form {...ToolKit.getFormLayout(10)}>
            {/* 排序方式 */}
            <Form.Item label={<TitleWithTooltip title="排序方式" tooltip="指标查询聚合函数"/>}>
                <Select value={orderByStore.type} onChange={handleFunctionChange}>
                    {functionType.map(type => (
                        <Select.Option key={type.label} value={type.value}>
                            <Tooltip title={type.description}>
                                <span style={{width: "100%"}}>{type.label}</span>
                            </Tooltip>
                        </Select.Option>
                    ))}
                </Select>
            </Form.Item>

            {/* 排序类型 */}
            <Form.Item label={<TitleWithTooltip title="排序类型" tooltip="指标查询数据按照聚合函数排序"/>}>
                <Select value={orderByStore.sort} onChange={handleSortChange}>
                    {sortType.map(value => (
                        <Select.Option key={value} value={value}>{value}</Select.Option>
                    ))}
                </Select>
            </Form.Item>

            {/* 数据条数 */}
            <Form.Item label={<TitleWithTooltip title="数据条数" tooltip="查询指标返回的最多数据线条数（1 ~ 100）"/>}>
                <InputNumber
                    type="number"
                    defaultValue={orderByStore.limit}
                    onChange={v => handleDataLimitChange(v)}
                    // 失焦点执行强制触发
                    onBlur={dispatchChange}
                    min={1}
                    max={100}
                />
            </Form.Item>
        </Form>
    );
};

export default observer(OrderBySetting);
