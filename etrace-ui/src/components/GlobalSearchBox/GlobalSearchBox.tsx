import {reaction} from "mobx";
import classNames from "classnames";
import {Link} from "react-router-dom";
import {useDebounceFn} from "ahooks";
import StoreManager from "$store/StoreManager";
import {GLOBAL_SEARCH_PAGE} from "$constants/Route";
import {AutoComplete, Card, Menu, Select} from "antd";
import {LocalStorageUtil} from "$utils/LocalStorageUtil";
import {SizeType} from "antd/lib/config-provider/SizeContext";
import React, {useEffect, useMemo, useRef, useState} from "react";
import {
    Chart,
    Dashboard,
    DashboardApp,
    getSearchSuggest,
    HostName,
    SearchResultCategory,
    SuggestResultGroup,
    User,
} from "$services/GlobalSearchService";

import "./GlobalSearchBox.less";
import {SEARCH_KEY, SEARCH_TYPE, STORAGE_KEY_GLOBAL_SEARCH} from "$constants/index";

interface GlobalSearchBoxProps {
    size?: SizeType;
    style?: React.CSSProperties;
    className?: string;
    placeholder?: string;
    maxWidth?: number;
    needLink?: boolean;
    needSpacing?: boolean;
    focusClass?: string; // focus 后的 className
    dropdownStyle?: React.CSSProperties;
    dropdownMatchSelectWidth?: boolean;
    onSelect?: (value: string) => any;
}

const ALL_CATEGORY = "all"; // Tabs「全部」key
const HISTORY_NUM = 10;     // 历史记录数目

const GlobalSearchBox: React.FC<GlobalSearchBoxProps> = props => {
    const {className, needLink, dropdownMatchSelectWidth, dropdownStyle, size, placeholder, maxWidth, needSpacing, style, focusClass} = props;
    const {onSelect} = props;
    const {urlParamStore} = StoreManager;

    const autoComplete = useRef<any>();
    const lastSearchKey = useRef("");
    const [suggestResults, setSuggestResults] = useState<SuggestResultGroup[]>(null);
    const [inputValue, setInputValue] = useState<string>(() => urlParamStore.getValue(SEARCH_KEY) || "");
    const [loading, setLoading] = useState<boolean>(false);
    const [category, setCategory] = useState<string>(ALL_CATEGORY);
    const [focus, setFocus] = useState<boolean>(false);

    useEffect(() => {
        const disposer = reaction(
            () => urlParamStore.getValue(SEARCH_KEY),
            v => setInputValue(v)
        );
        return () => disposer();
    }, []);

    /**
     * 搜索框 change 句柄
     * @param params
     */
    const handleInputChange = (...params: any[]) => {
        setInputValue(params[0]);
        debounceSearch.apply(this, params);
    };

    /**
     * 搜索结果选择触发句柄
     */
    const handleSuggestResultSelect = (value: string, opts: any) => {
        const v = opts.props.title;
        onSelect && onSelect.call(null, v);
        saveGlobalSearchHistory(v);
        setInputValue(v);
    };

    /**
     * 相当于 Focus 句柄，判断是否已经查询过当前 `state.inputValue` 中的 suggest，没有的话查一遍
     * 可能存在前进后退的时候，inputValue 改变，suggest 没变的情况
     */
    const handleSearchResultsVisibleChange = (visible: boolean) => {
        if (visible && inputValue !== lastSearchKey.current) {
            handleSuggestSearch(inputValue);
        }
        setFocus(visible);
    };

    /**
     * 搜索触发句柄
     */
    const handleSuggestSearch = (value: string, opts?: any) => {
        value = (opts ? opts.props.title : value) || value;
        // 上面的逻辑：
        // 正常情况下输入的时候不存在 `options.props.title`，所以使用第一个 `value` 也就是 input 输入的内容
        // 还有一种 case 就是：select 结果后，callback 到 input 中，也会触发 onChange
        // 但是这时候第一个参数 `value` 是 `Option` 组件的 `value` 值，也就是唯一值，如：HOSTNAME8039
        // 但是这时候由于是 select 的 callback，所以存在 `options.props.title` 也就是我们需要的内容
        if (!value) {
            setSuggestResults(null);
            setInputValue("");
            return;
        }

        setLoading(true);

        lastSearchKey.current = value; // 存储上一次查询过的内容，配合 `handleSearchResultsVisibleChange`

        getSearchSuggest(value)
            .then(results => {
                setSuggestResults(results);
                // setInputValue(value);

                if (results.map(group => (group.category as string)).indexOf(category) === -1) {
                    setCategory(ALL_CATEGORY);
                }
            })
            .finally(() => {
                setLoading(false);
            });
    };

    /**
     * 渲染搜索结果每个 Groups
     */
    const renderSuggestResultGroups = () => {
        if (loading) {
            return <AutoComplete.Option disabled={true} value="loading">查询中...</AutoComplete.Option>;
        }

        if (!suggestResults) {
            return null;
        }

        if (suggestResults.length === 0) {
            return <AutoComplete.Option disabled={true} value="no-result">啥也没找到鸭</AutoComplete.Option>;
        }

        /**
         * 渲染搜索结果单个 Group 的 title
         * @param {string} title 标题文字
         * @param c Group 类别
         */
        const renderSuggestResultGroupTitle = (title: string, c: SearchResultCategory) => {
            const url = `${GLOBAL_SEARCH_PAGE}?${SEARCH_KEY}=${inputValue}&${SEARCH_TYPE}=${c}`;
            return (
                <div className="result-group-title">
                    <span className="result-group-title__text">{title}</span>
                    <span className="result-group-title__summary">{c}</span>
                    <Link to={url} onClick={() => autoComplete.current.blur()}>更多</Link>
                </div>
            );
        };

        /**
         * 渲染搜索结果中 Group 中的 options
         * @param {SuggestResultGroup} group
         * @return {any[]}
         */
        const renderSuggestResultItemOption = (group: SuggestResultGroup) => group.contents.map(content => {
            const {category: c} = group;
            let key;
            let title;
            let summary;
            switch (c) {
                case SearchResultCategory.HOSTNAME:
                    key = c + (content as HostName).id;
                    title = (content as HostName).hostname;
                    summary = (content as HostName).nic0Ip;
                    break;
                case SearchResultCategory.USER:
                    key = c + (content as User).id;
                    title = (content as User).psnname;
                    summary = `${(content as User).onedeptname} / ${(content as User).fatdeptname} / ${(content as User).deptname}`;
                    break;
                case SearchResultCategory.CHART:
                    key = c + (content as Chart).id;
                    title = (content as Chart).title;
                    summary = `${(content as Chart).departmentName} / ${(content as Chart).productLineName}`;
                    break;
                case SearchResultCategory.DASHBOARD:
                    key = c + (content as Dashboard).id;
                    title = (content as Dashboard).title;
                    summary = `${(content as Dashboard).departmentName} / ${(content as Dashboard).productLineName}`;
                    break;
                case SearchResultCategory.DASHBOARDAPP:
                    key = c + (content as DashboardApp).id;
                    title = (content as DashboardApp).title;
                    summary = `${(content as DashboardApp).departmentName} / ${(content as DashboardApp).productLineName}`;
                    break;
                case SearchResultCategory.APPID:
                default:
                    key = title = typeof content === "string" ? content : JSON.stringify(content);
            }

            const Wrapper = needLink
                ? Link
                : React.Fragment;
            const params = {
                to: `${GLOBAL_SEARCH_PAGE}?${SEARCH_KEY}=${title}`
            };

            // https://github.com/ant-design/ant-design/issues/12334
            // 目前 Antd 中的赋值 React Component 的 key 逻辑
            // 只有 key，就用 key。
            // 只有 value，就用 value。
            // 两者都有，用 value。
            return (
                <AutoComplete.Option key={key} value={key} title={title}>
                    <Wrapper {...params}>
                        <span className="result-group-item__title">{title}</span>
                        <span className="result-group-item__summary">{summary}</span>
                    </Wrapper>
                </AutoComplete.Option>
            );
        });

        return suggestResults.map(group => {
            return (category === ALL_CATEGORY || category === group.category) && <Select.OptGroup
                key={group.category}
                label={renderSuggestResultGroupTitle(group.label, group.category)}
            >
                {renderSuggestResultItemOption(group)}
            </Select.OptGroup>;
        }).filter(i => i);
    };

    const {run: debounceSearch} = useDebounceFn(handleSuggestSearch, { wait: 400 });

    const searchBoxStyle = {};
    maxWidth && Object.assign(searchBoxStyle, {maxWidth: maxWidth + "px"});
    needSpacing && Object.assign(searchBoxStyle, {width: "90%"});
    Object.assign(searchBoxStyle, style);

    const options = useMemo(() => renderSuggestResultGroups(), [loading, category, suggestResults]);

    const dropdownRender = menu => {
        const tabs = <Menu
            className="result-tabs"
            mode="horizontal"
            onClick={e => setCategory(e.key as string)}
            selectedKeys={[category]}
        >
            <Menu.Item key={ALL_CATEGORY}>全部</Menu.Item>
            {suggestResults && suggestResults.map(group => (
                <Menu.Item key={group.category}>{group.label}</Menu.Item>
            ))}
        </Menu>;

        const url = `${GLOBAL_SEARCH_PAGE}?${SEARCH_KEY}=${inputValue}${category === ALL_CATEGORY ? "" : `&${SEARCH_TYPE}=${category}`}`;
        const showAll = <Card className="result-show-all" bodyStyle={{padding: "10px 0 5px"}}>
            <Link to={url} onClick={() => autoComplete.current.blur()}>查看所有结果</Link>
        </Card>;

        return (loading || suggestResults === null || suggestResults.length === 0)
            ? menu
            : <>{tabs}{menu}{showAll}</>;
    };

    const searchBoxClassString = classNames("emonitor-global-search-box", className, {
        [focusClass]: focus
    });

    const searchResultClassString = classNames("emonitor-global-search-box__result", {
        "link-items": needLink,
    });

    return (
        <div className={searchBoxClassString} style={searchBoxStyle}>
            <AutoComplete
                style={{width: "100%"}}
                size={size}
                ref={autoComplete}
                allowClear={true}
                value={inputValue}
                defaultValue={inputValue}
                dropdownStyle={dropdownStyle}
                placeholder={placeholder || "全局搜索"}
                dropdownRender={dropdownRender}
                onChange={handleInputChange}
                onSelect={handleSuggestResultSelect}
                onDropdownVisibleChange={handleSearchResultsVisibleChange}
                defaultActiveFirstOption={false}
                dropdownClassName={searchResultClassString}
                dropdownMatchSelectWidth={dropdownMatchSelectWidth !== undefined ? dropdownMatchSelectWidth : true}
            >
                {options}
            </AutoComplete>
        </div>
    );
};

export function saveGlobalSearchHistory(value: string) {
    if (!value) {
        return;
    }
    const key = STORAGE_KEY_GLOBAL_SEARCH;
    const amount = HISTORY_NUM;
    const historyOfKey: Array<string> = LocalStorageUtil.getStringValues(key);

    // 存储搜索记录
    const index = historyOfKey.indexOf(value);
    if (index > -1) {
        historyOfKey.splice(index, 1);
    }
    historyOfKey.unshift(value);
    // 删除多余的记录
    if (historyOfKey.length > amount) {
        historyOfKey.splice(amount, historyOfKey.length - amount);
    }
    localStorage.setItem(key, JSON.stringify(historyOfKey));

    // 派发事件
    const event = new Event(STORAGE_KEY_GLOBAL_SEARCH);
    window.dispatchEvent(event);
}

export default GlobalSearchBox;
