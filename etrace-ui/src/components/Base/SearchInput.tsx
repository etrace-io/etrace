import React, {ReactNode, useEffect, useState} from "react";
import {DownOutlined} from "@ant-design/icons/lib";
import {AutoComplete, Dropdown, Input, Menu, Popover, Space} from "antd";

const SearchInput: React.FC<{
    width?: number | string;
    defaultValue?: string;
    storageKey?: string;
    historyAmount?: number;
    dataSource?: string[];
    title?: string;
    placeholder?: string;
    notFoundTooltip?: ReactNode;
    onChange?: (value: string) => void;
    onSelect?: (value: string) => void;
    onSearch?: (value: string) => void;
    onBlur?: (value: string | string[]) => void;
}> = props => {
    const {title, dataSource, width, storageKey, historyAmount, defaultValue, notFoundTooltip, placeholder} = props;
    const {onChange, onSelect, onBlur, onSearch} = props;

    const [inputValue, setInputValue] = useState<string>(defaultValue);
    const [historyList, setHistoryList] = useState<string[]>(() => JSON.parse(localStorage.getItem(storageKey) || "[]"));

    useEffect(() => {
        if (historyList.length === 0 && defaultValue) {
            saveHistory(defaultValue);
        }
    }, []);

    const handleInputChange = value => {
        onChange && onChange(value);
        setInputValue(value);
    };

    const handleAutoCompleteSelect = value => {
        onSelect && onSelect(value);
        setInputValue(value);

        saveHistory(value);
    };

    const handleHistoryItemSelect = value => {
        onSelect && onSelect(value);
        setInputValue(value);

        saveHistory(value);
    };

    /**
     * 存储搜索记录
     */
    const saveHistory = value => {
        if (!storageKey) {
            return;
        }
        const amount = historyAmount || 10; // 历史记录条数最大值, 默认 10
        const index = historyList.indexOf(value);
        if (index > -1) {
            historyList.splice(index, 1);
        }
        historyList.unshift(value);
        // 删除多余的记录
        if (historyList.length > amount) {
            historyList.splice(amount, historyList.length - amount);
        }
        localStorage.setItem(storageKey, JSON.stringify(historyList));
        setHistoryList(historyList.slice());
    };

    return (
        <Space style={{width: width || "auto"}} size={16}>
            <Popover visible={notFoundTooltip != null} content={notFoundTooltip}>
                <AutoComplete
                    options={dataSource.map(value => ({value}))}
                    value={inputValue}
                    onChange={handleInputChange}
                    onSelect={handleAutoCompleteSelect}
                    onBlur={() => onBlur && onBlur(inputValue)}
                >
                    <Input.Search
                        addonBefore={title || null}
                        style={{minWidth: "100px"}}
                        placeholder={placeholder}
                        onSearch={onSearch}
                    />
                </AutoComplete>
            </Popover>

            {storageKey && (
                <SearchHistory dataSource={historyList} onSelect={handleHistoryItemSelect}/>
            )}
        </Space>
    );
};

const SearchHistory: React.FC<{
    dataSource: string[];
    onSelect?: (key: string | number) => void;
}> = props => {
    const {dataSource, onSelect} = props;

    const historyMenu = (
        <Menu onClick={(e) => onSelect && onSelect(e.key)}>
            {(dataSource || []).map(e => <Menu.Item key={e}>{e}</Menu.Item>)}
            {(!dataSource || dataSource.length === 0) && (
                <Menu.Item disabled={true}>暂无数据</Menu.Item>
            )}
        </Menu>
    );

    return (
        <Dropdown overlay={historyMenu}>
            <a onClick={e => e.preventDefault()}>
                历史记录 <DownOutlined />
            </a>
        </Dropdown>
    );
};

export default SearchInput;
