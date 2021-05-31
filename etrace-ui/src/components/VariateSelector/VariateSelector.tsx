import {useDebounceFn} from "ahooks";
import {Select, Spin, Tooltip} from "antd";
import StoreManager from "$store/StoreManager";
import React, {useEffect, useState} from "react";
import * as VariateService from "$services/VariateService";
import {MetricVariate, Variate, VariateType} from "$models/BoardModel";

import "./VariateSelector.less";

const VariateSelector: React.FC<{
    variate: Variate;
    valueFormatter?: (input: string) => string;
    onLoadValues?: (variate: Variate, searchValue?: string) => string[] | Promise<string[]>; // 额外数据源
}> = props => {
    const {variate, onLoadValues, valueFormatter} = props;
    const {urlParamStore} = StoreManager;

    const [loading, setLoading] = useState<boolean>(false);
    const [options, setOptions] = useState<string[]>([]);
    const [selectOpen, setSelectOpen] = useState<boolean>(false);
    const [selectedOptions, setSelectedOptions] = useState<string[]>(() => urlParamStore.getValues(variate.name));

    const {run: debounceSearch} = useDebounceFn(input => {
        if (variate.type === VariateType.METRIC) {
            queryOptions(input).then();
        }
    }, {wait: 500});

    /**
     * 这里注释是因为和下一个 Effect 会因为 options 变化不断循环调用
     * 若其他地方不会变更 url 则不需要下方 effect 监听 URL
     */
    /*useEffect(() => {
        const disposer = reaction(
            () => urlParamStore.getValues(variate.name),
            result => {
                if (!isEqual(result, selectedOptions)) {
                    setSelectedOptions(result);
                }
            });

        return () => disposer();
    }, [variate.name]);*/

    // useEffect(() => {
    //     urlParamStore.changeURLParams({
    //         [variate.name]: selectedOptions
    //     });
    // }, [selectedOptions, variate.name]);

    useEffect(() => {
        !selectOpen && dispatchChange(selectedOptions);
    }, [selectedOptions]);

    const queryOptions = async (value?: string) => {
        setLoading(true);
        const result = onLoadValues
            ? await onLoadValues(variate, value)
            : await VariateService.loadVariateValues(variate, value);

        setOptions(result);
        setLoading(false);
    };

    const handleSelectFocus = () => {
        queryOptions().then();
    };

    const handleSelectBlur = () => {
        dispatchChange(selectedOptions);
    };

    const handleVariateChange = value => {
        const result = valueFormatter
            ? Array.isArray(value)
                ? value.map(valueFormatter)
                : valueFormatter(value)
            : value;

        setSelectedOptions(result);
    };

    const dispatchChange = value => {
        urlParamStore.changeURLParams({
            [variate.name]: value
        });
    };

    const mode = variate.onlySingleSelect ? "multiple" : "tags"; // multiple 不可自定义输入，tag 可以

    return (
        <span className="variate-selector-container">
            <VariateTooltip variate={variate}>
                <span>{variate.label}：</span>
            </VariateTooltip>
            <Select
                loading={loading}
                allowClear={true}
                showSearch={true}
                maxTagCount={4}
                dropdownMatchSelectWidth={false}
                mode={mode}
                placeholder={variate.name}
                onFocus={handleSelectFocus}
                onBlur={handleSelectBlur}
                onChange={handleVariateChange}
                onSearch={debounceSearch}
                onDropdownVisibleChange={setSelectOpen}
                style={{minWidth: "100px", width: "auto"}}
                notFoundContent={loading ? <Spin size="small"/> : "Not Found"}
                value={variate.onlySingleSelect ? selectedOptions : (selectedOptions || []).filter(Boolean)}
            >
                {options && options.map((option: string, index: number) => (
                    <Select.Option key={index} value={option}>{option}</Select.Option>
                ))}
            </Select>
        </span>
    );
};

const VariateTooltip: React.FC<{
    variate: Variate;
}> = props => {
    const {variate, children} = props;

    const tooltip = (
        <>
            <label>变量类型：</label>{variate.type}<br/>
            <label>影响的 Tag：</label>{variate.name}<br/>
            {variate.type == VariateType.METRIC && (variate as MetricVariate).relatedTagKeys &&
            (variate as MetricVariate).relatedTagKeys.length > 0 &&
            (<div><label>联动于: </label>{(variate as MetricVariate).relatedTagKeys.join(", ")}<br/></div>)}
            {variate.onlySingleSelect && (<label>仅可单选 </label>)}
        </>
    );

    return (
        <Tooltip placement="topLeft" title={tooltip} mouseEnterDelay={0.5} color="blue">
            {children}
        </Tooltip>
    );
};

export default VariateSelector;
