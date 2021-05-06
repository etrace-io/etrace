import React, {useEffect, useRef, useState} from "react";
import {reaction} from "mobx";
import {useHistory} from "react-router-dom";
import StoreManager from "$store/StoreManager";
import EMonitorNav from "$components/Nav/EMonitorNav";
import {EMonitorContainer} from "$components/EMonitorLayout";
import GlobalSearchResults from "$containers/GlobalSearchPage/Results";
import FunctionPanel from "$containers/GlobalSearchPage/FunctionPanel";
import GlobalSearchTabBar from "$containers/GlobalSearchPage/CategoryTabBar";
import {getSearchQuery, SearchResultCategory, SuggestResultGroup} from "$services/GlobalSearchService";

import "./GlobalSearchPage.less";
import {Affix, BackTop} from "antd";
import {SEARCH_KEY, SEARCH_TYPE} from "$constants/index";

export const PAGE_SIZE = 10;        // 当前结果每页显示及获取的数量

const GlobalSearchPage: React.FC = props => {
    const { urlParamStore } = StoreManager;

    const currSearchInput = useRef<string>(null);       // 存储当前用户搜索输入
    const pageInfo = useRef<Map<string, number>>(new Map());      // 存储每个 category 当前所在页数

    const [currCategory, setCurrCategory] = useState<SearchResultCategory>(null);
    const [currResults, setCurrResults] = useState<SuggestResultGroup[]>(null);
    const [loading, setLoading] = useState<boolean>(false);

    const history = useHistory();

    useEffect(() => {
        const disposer = reaction(
            () => [urlParamStore.getValue(SEARCH_TYPE), urlParamStore.getValue(SEARCH_KEY)],
            (params: [SearchResultCategory, string]) => {
                setSearchCategory(params[ 0 ]);
                getSearchQueryResults(params[ 1 ]);
            },
        );
        return () => disposer();
    }, []);

    useEffect(() => {
        const searchKey = urlParamStore.getValue(SEARCH_KEY);
        const searchCategory = urlParamStore.getValue(SEARCH_TYPE) as SearchResultCategory;

        if (!searchKey) {
            history.push({ pathname: "/" });
        }

        setSearchCategory(searchCategory);
        getSearchQueryResults(searchKey);
    }, []);

    /**
     * 获取精确搜索结果集
     */
    const getSearchQueryResults = (value: string) => {
        if (!value || currSearchInput.current === value) {
            return;
        }

        currSearchInput.current = value;

        setLoading(true);

        getSearchQuery(value).then(results => {
            // 重置
            const category = urlParamStore.getValue(SEARCH_TYPE)
                ? (urlParamStore.getValue(SEARCH_TYPE) as SearchResultCategory)
                : results.length > 0 ? results[ 0 ].category : null;

            pageInfo.current = new Map();

            setCurrCategory(category);
            setCurrResults(results);
            setLoading(false);
        });
    };

    /**
     * 设置当前显示的 category
     */
    const setSearchCategory = (category: SearchResultCategory) => {
        const nextCategory = category
            ? category
            : currResults && currResults.length > 0
                ? currResults[ 0 ].category
                : currCategory;

        setCurrCategory(nextCategory);
    };

    /**
     * 点击「加载更多」按钮逻辑
     */
    const handleResultsLoadMore = (): Promise<boolean> => {
        let page = pageInfo.current.get(currCategory) || 1;

        // 开始查询下一页
        pageInfo.current.set(currCategory, ++page);

        return getSearchQuery(currSearchInput.current, {
            page,
            size: PAGE_SIZE,
            category: currCategory,
        }).then(results => {
            const newResult = results.find(group => group.category === currCategory);

            if (newResult) {
                const oldResult = currResults.find(group => group.category === currCategory);
                oldResult.contents.push(...(newResult.contents));
                setCurrResults(currResults.slice());

                return newResult.contents.length === PAGE_SIZE;
            } else {
                // 没有下一页了
                return false;
            }
        });
    };

    /**
     * 左侧 category 切换句柄
     */
    const handlerTabChange = (group: SuggestResultGroup) => {
        const { category } = group;

        urlParamStore.changeURLParams({
            [ SEARCH_TYPE ]: category,
        });
    };

    return (
        <EMonitorContainer
            header={<EMonitorNav search={false}/>}
            // headerFixed={true}
            className="emonitor-global-search-page"
            fullscreen={true}
        >
            <Affix offsetTop={51}>
                <GlobalSearchTabBar
                    loading={loading}
                    groups={currResults}
                    currCategory={currCategory}
                    onChange={handlerTabChange}
                />
            </Affix>

            {/* 搜索结果 */}
            <GlobalSearchResults
                loading={loading}
                groups={currResults}
                currCategory={currCategory}
                onLoadMore={handleResultsLoadMore}
            />

            {/* 更多功能 */}
            <FunctionPanel />

            {/* 回到顶部 */}
            <BackTop />
        </EMonitorContainer>
    );
};

export default GlobalSearchPage;
