import {get} from "lodash";
import {reaction} from "mobx";
import React from "react";
import {Button, Layout, Tabs} from "antd";
import YellowPages from "./YellowPages";
import {messageHandler} from "../../utils/message";
import StoreManager from "../../store/StoreManager";
import YpNav from "../../components/YellowPages/YPNav";
import {URLParamStore} from "../../store/URLParamStore";
import YpSearch from "../../components/YellowPages/YPSearch";
import YpRecord from "../../components/YellowPages/YPRecord";
import Footer from "$components/EMonitorLayout/Footer";
import {getSearchResult} from "../../services/YellowPagesService";
import {YpHistoryList} from "../../components/YellowPages/YPHistory";
import {SearchResultData, SuggestOptions} from "../../models/YellowPagesModel";
import YpSearchResultList from "../../components/YellowPages/YPSearchResultList";
import classNames from "classnames";
import {Link} from "react-router-dom";
import {BlankBoxIcon} from "../../components/Icon/Icon";
import {LoadingOutlined} from "@ant-design/icons/lib";

enum ResultTabs {
    Record = "RECORD",
    List = "LIST",
}

interface YpSearchResultPageProps {
    history: any;
}

interface YpSearchResultPageStatus {
    isLoading: boolean;
    searchResult: SearchResultData;
    currTab: ResultTabs;
}

export default class YpSearchResultPage extends React.Component<YpSearchResultPageProps, YpSearchResultPageStatus> {
    public static BASE_URL = "/yellow-pages/search";
    public static YELLOW_PAGE_TYPE = "type";
    public static YELLOW_PAGE_KEY = "q";

    static TABS_INDEX = [ResultTabs.Record, ResultTabs.List];

    private urlParamStore: URLParamStore;               // URL store 用于更改 key 和 category
    private searchAdvancedOptions: SuggestOptions;
    private lastSearchInput: string;                    // 存储当前用户搜索输入
    private defaultSearchValue: string;                    // 存储当前用户搜索输入
    private readonly disposer: any; // 监听 URL 变化触发查询和 type 切换

    constructor(props: YpSearchResultPageProps) {
        super(props);
        this.urlParamStore = StoreManager.urlParamStore;

        this.state = {
            isLoading: false,
            searchResult: null,
            currTab: ResultTabs.Record,
        };

        const localStorageOptions = localStorage.getItem(YpSearch.STORAGE_KEY_SEARCH_OPTIONS);

        this.searchAdvancedOptions =
            (localStorageOptions && JSON.parse(localStorageOptions) as SuggestOptions) ||
            YpSearch.DEFAULT_SEARCH_ADVANCED_OPTIONS;

        // 主要用于浏览器前进后退触发
        this.disposer = reaction(
            () => [
                this.urlParamStore.getValue(YpSearchResultPage.YELLOW_PAGE_KEY),
                this.urlParamStore.getValue(YpSearchResultPage.YELLOW_PAGE_TYPE),
            ],
            (params: [string, ResultTabs]) => {
                this.querySearchResult(params[0]);
                this.setCurrTab(params[1]);
            }
        );
    }

    componentWillMount(): void {
        // 无搜索内容返回首页
        const searchKey = this.urlParamStore.getValue(YpSearchResultPage.YELLOW_PAGE_KEY);
        const currTab = this.urlParamStore.getValue(YpSearchResultPage.YELLOW_PAGE_TYPE) as ResultTabs;
        if (!searchKey) {
            this.props.history.push({pathname: YellowPages.BASE_URL});
        } else {
            this.defaultSearchValue = searchKey;
            this.querySearchResult(searchKey);
            this.setCurrTab(currTab);
        }
    }

    componentWillUnmount(): void {
        if (this.disposer) {
            this.disposer();
        }
    }

    querySearchResult = (key: string) => {
        if (!key || key === this.lastSearchInput) {
            return;
        }
        this.setState({isLoading: true});
        getSearchResult(key, this.searchAdvancedOptions)
            .then(searchResult => {
                this.lastSearchInput = key;

                const {currTab} = this.state;
                const currTabHasResult = get(searchResult, currTab, []).length > 0;
                let targetTab = undefined;

                !currTabHasResult && YpSearchResultPage.TABS_INDEX.forEach(tab => {
                    if (!targetTab && get(searchResult, tab, []).length > 0) {
                        targetTab = tab;
                    }
                });

                this.setState({
                    currTab: targetTab || currTab || ResultTabs.Record,
                    searchResult,
                    isLoading: false
                });
            })
            .catch(err => {
                this.setState({isLoading: false});
                messageHandler("error", "获取搜索结果出错");
            });
    };

    setCurrTab = (currTab: ResultTabs) => {
        if (!currTab || currTab === this.state.currTab) {
            return;
        }

        const defaultTab = ResultTabs.Record;

        this.setState({currTab: currTab || defaultTab}, () => {
            this.urlParamStore.changeURLParams({
                [YpSearchResultPage.YELLOW_PAGE_TYPE]: currTab
            });
        });
    };

    handleSearch = (value: string, options: SuggestOptions) => {
        if (!value) {
            return;
        }
        this.searchAdvancedOptions = options;
        const url = `${YpSearchResultPage.BASE_URL}?${YpSearchResultPage.YELLOW_PAGE_KEY}=${value}`;
        this.props.history.push(url);
    };

    handleTabsChanged = (currTab: ResultTabs) => {
        this.setCurrTab(currTab);
    };

    render() {
        const {searchResult, currTab, isLoading} = this.state;

        const records: Array<any> = get(searchResult, "RECORD", []);
        const lists = get(searchResult, "LIST", []);

        const isEmpty = searchResult && records.length === 0 && lists.length === 0;

        // console.log(records);
        const containerCls = classNames("search-result__container", {
            loading: isLoading
        });

        return (
            <Layout className="e-monitor-layout e-monitor-yellow-pages-search-result">
                <YpNav homeLink={YellowPages.BASE_URL}/>

                <div className="search-result__body">
                    <div className={containerCls}>
                        <YpSearch
                            className="e-monitor-yellow-pages__search-box"
                            onSearch={this.handleSearch}
                            defaultValue={this.defaultSearchValue}
                            // onPressEnter={this.handleSearchInputPressEnter}
                        />

                        <Tabs
                            className="search-result__tabs"
                            onChange={this.handleTabsChanged}
                            type="card"
                            defaultActiveKey={currTab}
                            tabBarExtraContent={(
                                <Link to={YellowPages.BASE_URL}>
                                    <Button className="search-result__home-btn" icon="home">返回首页</Button>
                                </Link>
                            )}
                        >
                            {records.length > 0 && <Tabs.TabPane tab="App" key={ResultTabs.Record}/>}
                            {lists.length > 0 && <Tabs.TabPane tab="相关集合" key={ResultTabs.List}/>}
                        </Tabs>

                        {currTab === ResultTabs.Record && records.length > 0 && (
                            <div className="search-result__records">
                                {records && records.map(data => (
                                    <YpRecord dataSource={data} key={data.id}/>
                                ))}
                            </div>
                        )}

                        {currTab === ResultTabs.List && lists.length > 0 && (
                            <YpSearchResultList
                                className="search-result__lists"
                                dataSource={lists}
                            />
                        )}

                        {isEmpty && (
                            <div className="search-result__empty-tips">
                                <BlankBoxIcon className="empty-tips__icon"/>
                                <div className="empty-tips__content">暂无搜索结果 😥</div>
                            </div>
                        )}

                        {isLoading && (
                            <div className="search-result__loading">
                                <LoadingOutlined className="search-result__loading-logo"/>
                                <div className="search-result__loading-text">搜索中...</div>
                            </div>
                        )}
                    </div>

                    <YpHistoryList/>
                </div>

                <Footer/>
            </Layout>
        );
    }
}
