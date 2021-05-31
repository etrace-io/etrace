import React from "react";
import {get} from "lodash";
import {checkChartType} from "$utils/chart";
import {FormItemProps} from "antd/lib/form/FormItem";
import {InputNumberProps} from "antd/lib/input-number";
import {AutoComplete, Checkbox, Form, Input, InputNumber, Select, Switch, Tooltip} from "antd";
import {
    bindConfigValue,
    EditConfig,
    EditConfigType,
    getConfigValue
} from "../../containers/Board/Explorer/ChartEditConfig";

const FormItem = Form.Item;

// todo: 这里难搞
// @ts-ignore
interface ChartEditItemProps extends FormItemProps, InputNumberProps {
    // 对应 EditConfigType，可以自行添加
    type: "select" | "checkBox" | "input" | "number" | "custom" | "autoComplete" | "switch";
    config?: EditConfig;                // 配置项
    placeholder?: string;               // 占位字符串
    forceReload?: boolean;              // 是否 onChange 后强制刷新
    notFormItem?: boolean;              // 是否需要 FormItem 包裹
    label?: React.ReactNode;            // FormItem 前的文字提示
    extraDisplayControl?: boolean;      // 显示该项额外控制条件，建议在 EditConfig.relative 中配置
    tooltipContent?: React.ReactNode;   // Tooltip 内容
    customType?: React.ComponentClass;  // 自定义 Type
    contentStyle?: React.CSSProperties; // 自定义 Style 样式
    formItemLayout?: any;               // Form Item 布局
    dataSource?: any;
    chartUniqueId?: any;
    mode?: string;

    // todo: 新增的
    // children?: any;
}

const defaultFormItemLayout = {
    labelCol: {
        xs: {span: 9},
        sm: {span: 9},
        md: {span: 9},
    },
    wrapperCol: {
        xs: {span: 15},
        sm: {span: 15},
        md: {span: 15},
    },
};

const ChartEditItem: React.FC<ChartEditItemProps> = props => {
    const {
        type,
        label,
        config,
        children,
        customType,
        forceReload,
        notFormItem,
        contentStyle,
        formItemLayout,
        tooltipContent,
        extraDisplayControl,
        ...others
    } = props;

    const {path, allowTypes, relative, disable, defaultValue} = config;

    // 检查关联配置
    let relativeShow = false;
    if (relative) {
        const {config: getConfigs, filter} = relative;
        const targetConfigs = getConfigs();
        const configs = Array.isArray(targetConfigs) ? targetConfigs : [targetConfigs];

        const relativeValue = [];
        const relativeAllowTypes = [];

        for (let _config of configs) {
            relativeValue.push(getConfigValue(_config));
            relativeAllowTypes.push(...get(_config, "allowTypes", []));
        }

        relativeShow =
            targetConfigs                                   // 判断是否存在 config
            && checkChartType(relativeAllowTypes) // 判断当前关联项是否显示
            && filter
            && filter(                                      // 判断 filter 是否存在及通过
                relativeValue.length === 1
                    ? relativeValue[0]
                    : relativeValue
            );
    }

    // 判断该项是否显示
    const show =
        // 关联配置条件是否为 true
        relativeShow
        // 检查当前 Type 是否支持该配置
        || (allowTypes && checkChartType(allowTypes))
        // 额外显示条件
        || extraDisplayControl;

    if (!show) {
        return null;
    }

    // 判断是否需要禁用
    let isDisable = false;
    if (disable && typeof disable === "function") {
        isDisable = disable();
    }

    let ItemType: any = customType || Input;
    switch (type) {
        case EditConfigType.Switch:
            ItemType = Switch;
            break;
        case EditConfigType.Select:
            ItemType = Select;
            break;
        case EditConfigType.CheckBox:
            ItemType = Checkbox;
            break;
        case EditConfigType.Input:
            ItemType = Input;
            break;
        case EditConfigType.AutoComplete:
            ItemType = AutoComplete;
            break;
        case EditConfigType.Number:
            ItemType = InputNumber;
            break;
        case EditConfigType.Custom:
        default:
            break;
    }

    const item = (
        <ItemType
            style={contentStyle}
            disabled={isDisable}
            {...bindConfigValue(
                config,
                defaultValue,
                type,
                forceReload
            )}
            {...others} // 支持覆盖之前的 Props
        >
            {children}
        </ItemType>
    );

    const wrappedItem = tooltipContent
        ? (<Tooltip title={tooltipContent} mouseEnterDelay={0.3}>{item}</Tooltip>)
        : item;

    const _formItemLayout = formItemLayout === undefined ? defaultFormItemLayout : formItemLayout;

    return !notFormItem
        ? <FormItem {..._formItemLayout} label={label} key={path}>{wrappedItem}</FormItem>
        : wrappedItem;
};

export default ChartEditItem;
