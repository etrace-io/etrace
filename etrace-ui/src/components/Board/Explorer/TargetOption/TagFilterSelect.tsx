import {useDebounceFn} from "ahooks";
import React, {useEffect, useState} from "react";
import {DeleteOutlined} from "@ant-design/icons/lib";
import {TagFilter, Target} from "../../../../models/ChartModel";
import {Button, Dropdown, Input, Menu, Select} from "antd";

interface TagFilterSelectProps {
    index: number;
    filterIndex: number;
    target: Target;
    tagFilter: TagFilter;
    value: Array<string>;
    showTagValues?: any;
    deleteTagFilter: any;
    onBlur?: any;
    disabled?: boolean;
    onChange?: any;  // 以触发onBlur无法触发的操作，如：(不blur)直接删除操作
    onOptionChange?: any;
}

const TagFilterSelect: React.FC<TagFilterSelectProps> = props => {
    const {tagFilter, deleteTagFilter, target, index, filterIndex, disabled, showTagValues} = props;
    const {onOptionChange, onChange, onBlur} = props;

    const [value, setValue] = useState<string[]>(props.value || []);
    const [options, setOptions] = useState<string[]>([]);

    useEffect(() => {
        setValue(props.value);
    }, [props.value]);

    const handleBlur = () => {
        onBlur && onBlur(index, filterIndex, value);
    };

    const handleChange = (v: any) => {
        setValue(v);
        onChange && onChange(index, filterIndex, v);
    };

    const {run: handleSearch} = useDebounceFn(async (v: string) => {
        if (!showTagValues) {
            return;
        }
        const opts = await showTagValues(target, tagFilter.key, v);
        setOptions(opts);
    }, {wait: 500});

    const handleOptionChange = (e: any) => {
        onOptionChange && onOptionChange(index, filterIndex, e.key);
    };

    const menu = (
        <Menu onClick={handleOptionChange}>
            <Menu.Item key="=">=</Menu.Item>
            <Menu.Item key="!=">!=</Menu.Item>
        </Menu>
    );

    return (
        <Input.Group compact={true} style={{margin: "5px 0"}}>
            <Button style={{minWidth: 100}} disabled={target.display == false}>{tagFilter.key}</Button>

            <Dropdown overlay={menu}>
                <Button disabled={target.display == false}>{tagFilter.op ? tagFilter.op : "="}</Button>
            </Dropdown>

            <Select
                style={{minWidth: 150, maxWidth: 300}}
                disabled={target.display === false}
                value={value}
                mode="tags"
                placeholder="请输入 Tag Value"
                dropdownMatchSelectWidth={false}
                onFocus={() => handleSearch("")}
                onChange={handleChange}
                onSearch={handleSearch}
                onBlur={handleBlur}
            >
                {options && options.map((item, i) => (
                    <Select.Option key={"" + i} value={item}>{item}</Select.Option>
                ))}
            </Select>

            <Button
                type="primary"
                danger={true}
                icon={<DeleteOutlined />}
                disabled={disabled}
                onClick={() => deleteTagFilter(index, filterIndex)}
            />
        </Input.Group>
    );
};

export default TagFilterSelect;