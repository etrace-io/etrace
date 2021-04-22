import {reaction} from "mobx";
import React from "react";
import {debounce} from "lodash";
import {Link} from "react-router-dom";
import {messageHandler} from "../../utils/message";
import StoreManager from "../../store/StoreManager";
import {URLParamStore} from "../../store/URLParamStore";
import {LocalStorageUtil} from "../../utils/LocalStorageUtil";
import {AutoComplete, Checkbox, Input, Menu} from "antd";
import Icon from "@ant-design/icons";

import {getSearchSuggest} from "../../services/YellowPagesService";
import YellowPages from "../../containers/YellowPages/YellowPages";
import YpSearchResultPage from "../../containers/YellowPages/YPSearchResultPage";
import {SearchCategory, SuggestItem, SuggestOptions, SuggestResult} from "../../models/YellowPagesModel";
import {SearchOutlined, SettingOutlined} from "@ant-design/icons/lib";

const classnames = require("classnames");

const searchAdvancedOptions = [
    {value: "searchList", label: "集合列表"},
    {value: "searchRecord", label: "所有 App"},
    // {value: "searchKeyWord", label: "相关关键字"},
];

interface YpSearchProps {
    className?: string;
    placeholder?: string;
    defaultValue?: string;
    size?: "default" | "large" | "small";
    // onPressEnter?: (value: string) => void;
    onSearch?: (value: string, options: SuggestOptions) => void;
}

interface YpSearchStatus {
    isLoading: boolean;
    searchValue: string;
    suggestResults: SuggestResult;
    advancedOptions: SuggestOptions;
    advancedOptionsVisible: boolean;
    currType: SearchCategory;
}

export default class YpSearch extends React.Component<YpSearchProps, YpSearchStatus> {
    static HISTORY_NUM = 10;        // 历史记录数目
    static STORAGE_KEY_SEARCH_HISTORY = "YELLOW_PAGES_SEARCH_HISTORY";
    static STORAGE_KEY_SEARCH_OPTIONS = "YELLOW_PAGES_SEARCH_OPTIONS";
    static DEFAULT_SEARCH_ADVANCED_OPTIONS: SuggestOptions = {
        // searchKeyWord: true,
        searchRecord: true,
        searchList: true,
    };

    autoCompleteRef = React.createRef();
    debounceSearch: any;                    // search 的 debounce 事件
    debounceTime: number = 400;             // search 的 debounce 时长
    lastSearchKey: string = "";             // 上一次搜索过结果的 input
    advancedOptionsChanged: boolean = false; // 是否修改过查询选项

    private readonly disposer: any[];                  // 监听 URL 中 searchKey 的变化
    private urlParamStore: URLParamStore;   // URL store

    static saveSearchValue(value: string) {
        if (!value) {
            return;
        }
        const key = YpSearch.STORAGE_KEY_SEARCH_HISTORY;
        const amount = YpSearch.HISTORY_NUM;
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
        const event = new Event(YpSearch.STORAGE_KEY_SEARCH_HISTORY);
        window.dispatchEvent(event);
    }

    static deleteSearchValue(value: string) {
        if (!value) {
            return;
        }
        const key = YpSearch.STORAGE_KEY_SEARCH_HISTORY;
        const amount = YpSearch.HISTORY_NUM;
        const historyOfKey: Array<string> = LocalStorageUtil.getStringValues(key);

        // 存储搜索记录
        const index = historyOfKey.indexOf(value);
        if (index > -1) {
            historyOfKey.splice(index, 1);
        }
        // 删除多余的记录
        if (historyOfKey.length > amount) {
            historyOfKey.splice(amount, historyOfKey.length - amount);
        }
        localStorage.setItem(key, JSON.stringify(historyOfKey));

        // 派发事件
        const event = new Event(YpSearch.STORAGE_KEY_SEARCH_HISTORY);
        window.dispatchEvent(event);
    }

    static saveSearchOptions(options: SuggestOptions) {
        localStorage.setItem(YpSearch.STORAGE_KEY_SEARCH_OPTIONS, JSON.stringify(options));
    }

    constructor(props: YpSearchProps) {
        super(props);

        const localStorageOptions = localStorage.getItem(YpSearch.STORAGE_KEY_SEARCH_OPTIONS);

        const advancedOptions =
            (localStorageOptions && JSON.parse(localStorageOptions) as SuggestOptions) ||
            YpSearch.DEFAULT_SEARCH_ADVANCED_OPTIONS;

        this.state = {
            suggestResults: null,
            isLoading: false,
            searchValue: props.defaultValue || "",
            currType: SearchCategory.All,
            advancedOptionsVisible: false,
            advancedOptions,
        };

        this.urlParamStore = StoreManager.urlParamStore;
        this.debounceSearch = debounce(this.handleSuggestSearch, this.debounceTime);

        this.disposer = [
            reaction(
                () => this.urlParamStore.getValue(YpSearchResultPage.YELLOW_PAGE_KEY),
                (key: string) => {
                    this.setState({searchValue: key});
                }
            )
        ];
    }

    componentWillUnmount(): void {
        if (this.disposer) {
            this.disposer.forEach(f => f());
        }
    }

    getSearchResultLink = (searchValue: string, currType: SearchCategory = SearchCategory.All) => {
        // const {searchValue} = this.state;
        const category = currType === SearchCategory.All ? "" : `&${YpSearchResultPage.YELLOW_PAGE_TYPE}=${currType}`;
        return `${YpSearchResultPage.BASE_URL}?${YpSearchResultPage.YELLOW_PAGE_KEY}=${searchValue}${category}`;
    };

    triggerSearch = (value: string) => {
        const {onSearch} = this.props;
        const {advancedOptions} = this.state;

        if (onSearch) {
            onSearch(value, advancedOptions);
        }
        if (value !== this.state.searchValue) {
            this.setState({
                searchValue: value,
            });
        }
        YpSearch.saveSearchValue(value);
    };

    handleToggleAdvancedOptions = (e: any, status: boolean) => {
        this.setState({
            advancedOptionsVisible: status
        });
        e.stopPropagation();
        // this.blur();
    };

    handleAdvancedOptionsChanged = (options: string[]) => {
        const {advancedOptions} = this.state;
        Object.keys(advancedOptions).forEach(key => {
            advancedOptions[key] = options.indexOf(key) > -1;
        });
        this.setState({
            advancedOptions
        });
        // 存储搜索选项
        this.advancedOptionsChanged = true;
        YpSearch.saveSearchOptions(advancedOptions);
        // console.log(options);
    };

    handleSuggestResultTopTabsChange = (e: any) => {
        this.setState({
            currType: e.key
        });
    };
    handleSearchValueChange = (searchValue) => {
        this.setState({searchValue});
    };

    /**
     * 搜索触发句柄
     */
    handleSuggestSearch = (value: string, options?: any) => {
        value = (options ? options.props.title : value) || value;

        if (!value) {
            this.lastSearchKey = "";
            this.setState({
                suggestResults: null,
            });
            return;
        }

        const {advancedOptions} = this.state;

        if (value && value === this.lastSearchKey && this.advancedOptionsChanged === false) {
            return;
        }

        this.advancedOptionsChanged = false;

        this.setState({
            isLoading: true,
        });

        getSearchSuggest(value, advancedOptions)
            .then(result => {
                this.lastSearchKey = value;
                this.setState({
                    suggestResults: result,
                    isLoading: false,
                });
            })
            .catch(err => {
                messageHandler("error", err.message);
                this.setState({
                    isLoading: false,
                });
            });
    };

    /**
     * 相当于 Focus 句柄，判断是否已经查询过当前 `state.searchValue` 中的 suggest，没有的话查一遍
     * 可能存在前进后退的时候，searchValue 改变，suggest 没变的情况
     */
    handleSearchResultsVisibleChange = (visible: boolean) => {
        const {searchValue} = this.state;
        this.handleSuggestSearch(searchValue);
    };

    /**
     * 搜索结果选择触发句柄
     */
    handleSuggestResultSelect = (value: string, options: any) => {
        const v = options.props.title;
        this.triggerSearch(v);
    };

    handleSearchInputPressEnter = (e: any) => {
        this.blur();
        const value = e.target.value;
        this.triggerSearch(value);
    };

    renderSuggestResultGroups = (suggestResults: SuggestResult, currCategory: SearchCategory) => {
        const {isLoading} = this.state;
        // const {isLoading, searchValue} = this.state;

        if (isLoading) {
            return [(
                <AutoComplete.Option disabled={true} key="loading" value="loading">
                    <div className="search-box__loading">
                        查询中...
                    </div>
                </AutoComplete.Option>
            )];
        }

        if (!suggestResults) {
            return null;
        }
        const options = [];
        // todo: 变化较大 https://ant.design/components/auto-complete/#components-auto-complete-demo-certain-category
        // const options = Object.keys(suggestResults)
        //     .filter(category =>
        //         suggestResults[category].length > 0 &&
        //         (currCategory === SearchCategory.All || currCategory === category)
        //     )
        //     .map((category: SearchCategory) => (
        //         <AutoComplete.OptGroup
        //             key={category}
        //             label={this.renderSuggestResultGroupTitle(category)}
        //         >
        //             {this.renderSuggestResultItemOption(suggestResults[category], category)}
        //             <AutoComplete.Option key={`${category}more`} value={`${category}more`}  >
        //                 <Link
        //                     to={this.getSearchResultLink(searchValue, category)}
        //                     className="search-box__show-more"
        //                 >
        //                     <Icon type="ellipsis" />
        //                     <span>查看全部</span>
        //                 </Link>
        //                 {/*<span className="search-box__result-group-item__summary">{summary}</span>*/}
        //             </AutoComplete.Option>
        //         </AutoComplete.OptGroup>
        //     ));

        const hasResult = Object.keys(suggestResults).map(key => suggestResults[key].length).reduce((a, b) => a + b, 0) > 0;

        if (hasResult) {
            // 添加顶部 Tab
            options.push(this.renderSuggestResultTopTabs(suggestResults, currCategory));

            // 添加查看所有结果
            options.push(this.renderSuggestResultShowAllItem());
        } else {
            // 无建议结果
            options.push((
                <AutoComplete.Option disabled={true} key="no-result" value="no-result">
                    <div className="search-box__no-result">
                        啥也没找到鸭
                    </div>
                </AutoComplete.Option>
            ));
        }

        return options;
    };

    /**
     * 渲染搜索结果单个 Group 的 title
     * @param {string} category 类别
     */
    renderSuggestResultGroupTitle = (category: SearchCategory) => {
        let title = YellowPages.getCategoryName(category);

        return (
            <div className="search-box__result-group-title">
                <span className="search-box__result-group-title__text">{title}</span>
                {/*<span className="search-box__result-group-title__summary">{category}</span>*/}
                {/*<Link to={url} onClick={() => this.blur()}>更多</Link>*/}
            </div>
        );
    };

    /**
     * 渲染搜索结果中 Group 中的 options
     * @param {SuggestResultGroup} group
     * @return {any[]}
     */
    renderSuggestResultItemOption = (group: SuggestItem[], category: SearchCategory) => {
        return group.map(item => {
            const {
                id: key,
                name: title,
                icon,
            } = item;
            // https://github.com/ant-design/ant-design/issues/12334
            // 目前 Antd 中的赋值 React Component 的 key 逻辑
            // 只有 key，就用 key。
            // 只有 value，就用 value。
            // 两者都有，用 value。
            return (
                // @ts-ignore
                <AutoComplete.Option key={title} value={`${key}-${category}`} title={title}>
                    <div className="search-box__result-group-item">
                        {category === SearchCategory.Record && (
                            <img
                                alt="LOGO"
                                className="search-box__result-group-item__icon record"
                                src={icon || YellowPages.DEFAULT_RECORD_LOGO}
                            />
                        )}
                        {category === SearchCategory.List && (
                            <Icon
                                className="search-box__result-group-item__icon list"
                                type={icon || YellowPages.DEFAULT_LIST_LOGO}
                            />
                        )}
                        <span className="search-box__result-group-item__title">{title}</span>
                    </div>
                    {/*<span className="search-box__result-group-item__summary">{summary}</span>*/}
                </AutoComplete.Option>
            );
        });
    };

    /**
     * 渲染搜索结果中的顶部 Tab
     */
    renderSuggestResultTopTabs = (suggestResults: SuggestResult, currCategory: string) => {
        return (
            <AutoComplete.Option disabled={true} key="tabs" value="tabs">
                <Menu
                    className="search-box__tabs"
                    mode="horizontal"
                    onClick={this.handleSuggestResultTopTabsChange}
                    selectedKeys={[currCategory]}
                >
                    <Menu.Item key={SearchCategory.All}>全部</Menu.Item>
                    {Object.keys(suggestResults)
                        .filter(category => suggestResults[category].length > 0)
                        .map((category: SearchCategory) => (
                            <Menu.Item key={category}>{YellowPages.getCategoryName(category)}</Menu.Item>
                        ))
                    }
                </Menu>
            </AutoComplete.Option>
        );
    };

    /**
     * 渲染搜索结果中的「查看所有结果」按钮
     */
    renderSuggestResultShowAllItem = () => {
        const url = this.getSearchResultLink(this.state.searchValue);

        return (
            <AutoComplete.Option disabled={true} key="all" value="all">
                <div className="search-box__show-all">
                    <Link to={url} onClick={this.blur}>查看所有结果</Link>
                </div>
            </AutoComplete.Option>
        );
    };

    render() {
        const {className, placeholder} = this.props;
        const {advancedOptionsVisible, suggestResults, currType, searchValue, advancedOptions} = this.state;

        const optionsCls = classnames("search-box__advanced-options", {
            visible: advancedOptionsVisible
        });

        const settingCls = classnames("search-box__setting", {
            visible: advancedOptionsVisible
        });

        const searchResultCls = classnames("e-monitor-yellow-pages-search-box__result", {
            "hide-group-title": currType !== SearchCategory.All,
        });

        const setting = <SettingOutlined  className={settingCls} onClick={(e) => this.handleToggleAdvancedOptions(e, !advancedOptionsVisible)}/>;

        return (
            <div className={className}>
                <AutoComplete
                    // ref={this.autoCompleteRef}
                    dropdownClassName={searchResultCls}
                    style={{width: "100%"}}
                    defaultActiveFirstOption={false}
                    dropdownMatchSelectWidth={true}
                    // size={size || "large"}
                    // optionLabelProp="title"
                    backfill={true}
                    value={searchValue}
                    defaultValue={searchValue}
                    dataSource={this.renderSuggestResultGroups(suggestResults, currType)}
                    onChange={this.handleSearchValueChange}
                    onSearch={this.debounceSearch}
                    onSelect={this.handleSuggestResultSelect}
                    onDropdownVisibleChange={this.handleSearchResultsVisibleChange}
                >
                    <Input
                        allowClear={true}
                        prefix={<SearchOutlined  className="search-box__icon"/>}
                        suffix={setting}
                        placeholder={placeholder || "搜索更多应用、记录、集合等"}
                        onPressEnter={this.handleSearchInputPressEnter}
                    />
                </AutoComplete>

                <div className={optionsCls}>
                    搜索结果包含：
                    <Checkbox.Group
                        options={searchAdvancedOptions}
                        defaultValue={Object.keys(advancedOptions).filter(key => advancedOptions[key])}
                        onChange={this.handleAdvancedOptionsChanged}
                    />
                </div>
            </div>
        );
    }

    /**
     * 让 AutoComplete 失焦
     */
    blur() {
        // @ts-ignore
        this.autoCompleteRef.current && this.autoCompleteRef.current.blur();
    }
}
