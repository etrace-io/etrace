import {useDebounceFn} from "ahooks";
import React, {ReactNode, useEffect, useState} from "react";
import {Target} from "$models/ChartModel";
import {buildAppIdNotFoundTooltipTitle} from "$containers/app/EMonitorApp";
import {AutoComplete, Form, Input, Popover, Select} from "antd";

interface InputSelectProps {
    visible?: boolean; // 用于控制是否渲染
    required?: boolean;
    label: string| ReactNode;
    value: any;
    index: number;
    tagMode?: boolean;
    target: Target;
    placeholder?: string;
    onFocus?: any;
    onBlur?: any;
    onSelect?: any;
    onSearch?: any;
    onChange?: any;
    textarea?: boolean;
    filterOption?: boolean;
    showNotFoundToolTip?: boolean;
}

const TargetOptionItem: React.FC<InputSelectProps> = props => {
    const {visible, index, label, tagMode, required, target, placeholder, filterOption, textarea, showNotFoundToolTip} = props;

    const {onFocus, onBlur, onChange, onSearch, onSelect} = props;

    const [options, setOptions] = useState<string[]>();
    const [value, setValue] = useState<string>();
    // const [width, setWidth] = useState<number>();
    // const [prevInput, setPrevInput] = useState<string>();
    const [notFoundTooltip, setNotFoundTooltip] = useState<ReactNode>();

    useEffect(() => {
        setValue(props.value);
    }, [props.value]);

    const {run: doSearch} = useDebounceFn(() => {
        if (!onSearch) {
            return;
        }

        onSearch(target, value).then(ops => {
            const tooltip = ops && ops.length > 0 ? null : buildAppIdNotFoundTooltipTitle(value);
            setOptions(ops);
            setNotFoundTooltip(tooltip);
        });
    }, {wait: 300});

    if (visible === false) {
        return null;
    }

    const handleFocus = async () => {
        console.log("onFocus");
        if (!onFocus) {
            return;
        }
        const result = await onFocus(target);
        setOptions(result);
    };

    const handleBlur = () => {
        console.log("onBlur");
        onBlur && onBlur(index, value);
    };

    const handleChange = (v: any) => {
        console.log("onChange", v);
        onChange && onChange(index, v);
        setValue(v);
    };

    const handleSelect = (v: any) => {
        console.log("select", v);
        if (onSelect) {
            setValue(v);
            onSelect(index, v);
        }
    };

    const finalValue = value || (tagMode ? [] : "");

    const style: React.CSSProperties = {
        width: textarea ? "auto" : 160,
        minWidth: textarea ? 350 : undefined,
        maxWidth: "unset",
        // maxWidth: textarea ? 350 : "unset"
    };

    const item = tagMode ? (
        <Select
            allowClear={true}
            style={{minWidth: 160, maxWidth: 300}}
            value={finalValue}
            disabled={target.display === false}
            placeholder={placeholder}
            filterOption={filterOption}
            dropdownMatchSelectWidth={false}
            mode="tags"
            onFocus={handleFocus}
            onBlur={handleBlur}
            onSelect={handleSelect}
            onChange={handleChange}
            showSearch={true}
            onSearch={doSearch}
        >
            {options && options.map(opt => (
                <Select.Option key={opt} value={opt}>{opt}</Select.Option>
            ))}
        </Select>
    ) : (
        <AutoComplete
            allowClear={true}
            style={style}
            value={(finalValue as string)}
            disabled={target.display === false}
            placeholder={placeholder}
            filterOption={filterOption}
            dropdownMatchSelectWidth={false}
            options={options && options.map(v => ({value: v}))}
            onFocus={handleFocus}
            onBlur={handleBlur}
            onSelect={handleSelect}
            onChange={handleChange}
            onSearch={doSearch}
        >
            {textarea && <Input.TextArea  autoSize={{maxRows: 6}}/>}
        </AutoComplete>
    );

    const option = showNotFoundToolTip
        ? <Popover visible={!!notFoundTooltip} content={notFoundTooltip}>{item}</Popover>
        : item;

    return <Form.Item label={label} required={required}>{option}</Form.Item>;
};

export default TargetOptionItem;
