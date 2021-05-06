import React from "react";
import {PAGE_SIZE} from "./GlobalSearchPage";
import classNames from "classnames";
import {Link} from "react-router-dom";
import {DataFormatter} from "$utils/DataFormatter";
import StoreManager from "../../store/StoreManager";
import {dateToString} from "$constants/GlobalConstants";
import {Button, Divider, Dropdown, Menu, Skeleton} from "antd";
import {getApplicationMenuData} from "../Trace/application/ApplicationMenu";
import {BarsOutlined, CaretDownOutlined, FrownOutlined, LoadingOutlined} from "@ant-design/icons/lib";
import {
    AppId,
    Change,
    Chart,
    Dashboard,
    DashboardApp,
    RpcId,
    SearchResultCategory,
    SuggestResultContents,
    SuggestResultGroup,
    User
} from "$services/GlobalSearchService";

interface GlobalSearchResultsProps {
    loading?: boolean;
    className?: string;
    groups?: SuggestResultGroup[];
    currCategory?: SearchResultCategory;
    onLoadMore?: () => boolean | Promise<boolean>;
}

interface GlobalSearchResultsState {
    loadingMore: boolean;
    showMoreBtn: boolean;
}

declare type Content = { key: string; url: string; title: string; subTitle: string; options?: any[]; content?: React.ReactNode; footer: string; };

export default class GlobalSearchResults extends React.Component<GlobalSearchResultsProps, GlobalSearchResultsState> {
    private readonly resultsListRef;                                         // 存储结果集 List
    private SCROLL_TOP_CLASS = "results-list-scroll-position-top";  // CSS class: List 是否滚动到顶部

    /**
     * 需要的话，添加每个 Item 右上角快捷跳转
     * @param {any[]} options
     * @return {any}
     */
    static renderItemOptions(options: any[]) {
        if (!options) {
            return null;
        }

        const menu = (
            <Menu>
                <Menu.ItemGroup title="快捷跳转">
                    <Menu.Divider/>
                    {options.map(option => option.children
                        ? <Menu.SubMenu key={option.path} title={option.name}>
                            {option.children.map(child =>
                                <Menu.Item key={child.path}>
                                    <Link to={child.path + child.params}>{child.name}</Link>
                                </Menu.Item>
                            )}
                        </Menu.SubMenu>
                        : <Menu.Item key={option.path}>
                            <Link to={option.path + option.params}>{option.name}</Link>
                        </Menu.Item>
                    )}
                </Menu.ItemGroup>
            </Menu>
        );

        return (
            <Dropdown overlay={menu} placement="bottomRight">
                <BarsOutlined type="bars" style={{float: "right", fontSize: "16px", cursor: "pointer"}}/>
            </Dropdown>
        );
    }

    /**
     * 渲染无相关结果的内容
     */
    static renderEmptyItem(category?: string) {
        const where = category ? `「${category}」` : " E-Monitor ";
        return (
            <li className="search-page__results-item no-result">
                <FrownOutlined/> 未能在{where}中找到相关内容
            </li>
        );
    }

    constructor(props: GlobalSearchResultsProps) {
        super(props);

        this.resultsListRef = React.createRef();

        this.state = {
            loadingMore: false,
            showMoreBtn: false,
        };
    }

    componentWillReceiveProps(nextProps: Readonly<GlobalSearchResultsProps>, nextContext: any): void {
        const {groups, currCategory} = nextProps;
        if (!groups) {
            return;
        }
        const currGroup = groups.find(group => group.category === currCategory);
        if (currGroup) {
            this.setState({
                showMoreBtn: currGroup.contents.length === PAGE_SIZE
            });
        }
    }

    componentDidMount(): void {
        const {current} = this.resultsListRef;

        // 监听滚动添加 Class
        if (current) {
            current.addEventListener("scroll", this.handleResultsListScroll);
        }
    }

    componentDidUpdate(prevProps: Readonly<GlobalSearchResultsProps>, prevState: Readonly<GlobalSearchResultsState>, snapshot?: any): void {
        const {current} = this.resultsListRef;

        // 监听滚动添加 Class
        if (current) {
            current.addEventListener("scroll", this.handleResultsListScroll);
        }

        // 如果是新的请求，需要移除结果列表顶部阴影的 class
        if (prevProps.loading === true && this.props.loading === false && current) {
            const parent = current.parentElement;
            parent.classList.add(this.SCROLL_TOP_CLASS);
        }
    }

    componentWillUnmount(): void {
        const {current} = this.resultsListRef;

        // 移除监听
        if (current) {
            current.removeEventListener("scroll", this.handleResultsListScroll);
        }
    }

    /**
     * 监听滚动时是否需要添加类名（用于滚动部分顶部的阴影效果）
     * @param e
     */
    handleResultsListScroll = (e: any) => {
        const {target} = e;
        const parent = target.parentElement;
        if (parent && target && target.scrollTop > 0 && parent.classList.contains(this.SCROLL_TOP_CLASS)) {
            parent.classList.remove(this.SCROLL_TOP_CLASS);
        } else if (parent && target && target.scrollTop === 0 && !parent.classList.contains(this.SCROLL_TOP_CLASS)) {
            parent.classList.add(this.SCROLL_TOP_CLASS);
        }
    };

    /**
     * 渲染搜索结果集
     */
    renderSearchResultItems = (groups?: SuggestResultGroup[]) => {
        if (groups && groups.length > 0) {
            const currGroup = groups.find(group => group.category === this.props.currCategory);
            return currGroup ? this.renderItem(currGroup) : GlobalSearchResults.renderEmptyItem(this.props.currCategory);
        } else if (groups && groups.length === 0) {
            return GlobalSearchResults.renderEmptyItem();
        } else {
            return null;
        }
    };

    /**
     * 渲染结果条目
     */
    renderItem(group: SuggestResultGroup) {
        const {category} = group;
        let contents = group.contents;

        // 针对 RpcID，只取第一条
        // if (SearchResultCategory.RPCID === category) {
        //     contents = group.contents[0] ? [group.contents[0]] : [];
        // }

        return contents.map(item => {
            const {key, title, subTitle, options, footer, url, content} = this.getItemContent(category, item);

            const titleNode = url ? (
                <Link to={url} className="search-page__results-item__title">{title}</Link>
            ) : (
                <span className="search-page__results-item__title">{title}</span>
            );

            return (
                <li key={key} className="search-page__results-item">
                    <p className="search-page__results-item__title-wrapper">
                        {titleNode}
                        {subTitle && (
                            <small className="search-page__results-item__sub-title">
                                {subTitle}
                            </small>
                        )}
                        {options && GlobalSearchResults.renderItemOptions(options)}
                    </p>
                    {content && (<div className="search-page__results-item__content">{content}</div>)}
                    {footer && (<p className="search-page__results-item__footer">{footer}</p>)}
                </li>
            );
        });
    }

    /**
     * 根据不同的 category 输出相应内容
     * @param {string} category 当前分类
     * @param {SuggestResultContents} item
     * @return {Content}
     */
    getItemContent(category: string, item: SuggestResultContents): Content {
        let key = category;
        let title;
        let subTitle;
        let footer;
        let url;
        let content;
        let options;
        switch (category) {
            case SearchResultCategory.USER:
                key += (item as User).id;
                title = (item as User).psnname;
                footer = `${(item as User).onedeptname} / ${(item as User).fatdeptname} / ${(item as User).deptname}`;
                break;

            case SearchResultCategory.CHART:
                const chartContent = (item as Chart);
                const chartGlobalId = chartContent.globalId ? `?globalId=${chartContent.globalId}` : "";

                key += chartContent.id;
                title = chartContent.title;
                footer = `${chartContent.departmentName} / ${chartContent.productLineName}`;
                url = `/chart/${chartContent.id}${chartGlobalId}`;
                break;

            case SearchResultCategory.DASHBOARD:
                const dashBoardContent = (item as Dashboard);
                const dashBoardGlobalId = dashBoardContent.globalId ? `?globalId=${dashBoardContent.globalId}` : "";

                key += dashBoardContent.id;
                title = dashBoardContent.title;
                footer = `${dashBoardContent.departmentName} / ${dashBoardContent.productLineName}`;
                url = `/board/view/${dashBoardContent.id}` + dashBoardGlobalId;
                break;

            case SearchResultCategory.DASHBOARDAPP:
                const dashBoardAppContent = (item as DashboardApp);

                key += dashBoardAppContent.id;
                title = dashBoardAppContent.title;
                footer = `${dashBoardAppContent.departmentName} / ${dashBoardAppContent.productLineName}`;
                url = `/app/${dashBoardAppContent.id}`;
                break;

            case SearchResultCategory.APPID:
                if (!(typeof item === "string")) {
                    const appidContent = (item as AppId);
                    let appIdDepartment = "";
                    if (appidContent.departmentName && appidContent.productLineName) {
                        appIdDepartment = `${appidContent.departmentName} / ${appidContent.productLineName}：`;
                    }

                    key += appidContent.id;
                    title = appidContent.appId;
                    url = `/trace/overview?appId=${appidContent.appId}`;
                    footer = `${appIdDepartment}${appidContent.moduleOwner}`;
                    options = getApplicationMenuData({
                        appId: appidContent.appId,
                    });

                } else {
                    key = title = item;
                }
                break;

            case SearchResultCategory.CHANGE:
                const changeContent = (item as Change);
                let changeDepartment = "";
                if (changeContent.parentDepartment && changeContent.department) {
                    changeDepartment = `${changeContent.parentDepartment} / ${changeContent.department}：`;
                }

                key += JSON.stringify(changeContent);
                title = `${changeContent.source} / ${changeContent.severity ? "高危" : "普通"} / ${changeContent.isKeyPath ? "非" : ""}关键路径`;
                footer = `${changeDepartment}${changeContent.operator}`;
                content = (
                    <React.Fragment>
                        <p>变更时间：{dateToString(new Date(changeContent.timestamp))}</p>
                        <p>变更操作：<code>{changeContent.eventAction}</code></p>
                        {changeContent.description && <p>变更描述：{changeContent.description}</p>}
                        <p>变更内容：{changeContent.content}</p>
                    </React.Fragment>
                );
                break;

            case SearchResultCategory.RPCID:
                const rpcIdContent = (item as RpcId);
                const rpcInfo = rpcIdContent.rpcInfo;

                const operation = rpcInfo
                    ? rpcInfo.interface || rpcInfo.operation || rpcInfo.url || rpcInfo.name || ""
                    : "";

                key += rpcIdContent.blockId;
                title = rpcIdContent.reqId;
                url = `/search/request?requestId=${rpcIdContent.reqId}`;
                content = (
                    <React.Fragment>
                        {/*Rpc ID*/}
                        {rpcInfo && (
                            <p>
                                Rpc ID：
                                <a onClick={() => this.hanldleSamplingModalShow(rpcIdContent.reqId + "$$" + rpcIdContent.rpcId)}>{rpcIdContent.rpcId}</a>
                            </p>
                        )}

                        {/*调用时间*/}
                        {rpcInfo && <p>调用时间：{dateToString(new Date(rpcInfo.timestamp || 0))}</p>}

                        {/*服务耗时*/}
                        {rpcInfo && <p>服务耗时：{DataFormatter.transformMilliseconds(rpcInfo.duration || 0)}</p>}

                        {/*类型*/}
                        <p>类型：{rpcIdContent.rpcType}</p>

                        {/*App ID*/}
                        <p>App ID：
                            <Link to={`/trace/overview?appId=${rpcIdContent.appId}`}>
                                {rpcIdContent.appId}
                            </Link>
                        </p>

                        {/*调用 / 操作*/}
                        {rpcInfo && <p>调用 / 操作：{operation}</p>}

                        {/*EZone*/}
                        {rpcInfo && <p>EZone：{rpcInfo.ezone}</p>}

                        {/*Status*/}
                        {rpcInfo && (
                            <p>Status：
                                <span
                                    style={{
                                        textAlign: "center", color: rpcInfo.status === 0 ? "#67C23A" : "#F56C6C"
                                    }}
                                >
                                ●
                                </span>
                            </p>
                        )}

                        {/*ShardingKey*/}
                        {rpcInfo && <p>ShardingKey：{rpcInfo.shardingkey}</p>}

                        {/*日志*/}
                        <p>日志</p>
                    </React.Fragment>
                );
                break;

            default:
                key = title = JSON.stringify(item);
        }

        return {
            key,
            title,
            subTitle,
            options,
            content,
            footer,
            url,
        };
    }

    /**
     * 渲染「加载更多」按钮
     */
    renderMoreBtn = () => {
        const {loadingMore, showMoreBtn} = this.state;
        const {groups} = this.props;

        if (showMoreBtn) {
            if (groups.length === 0) {
                return null;
            }
            return (
                <Button
                    className="search-page__results-more-btn"
                    size="large"
                    onClick={this.handleLoadingMore}
                >
                    {loadingMore ? <LoadingOutlined/> : <CaretDownOutlined/>}
                    {loadingMore ? "加载中..." : "加载更多"}
                </Button>
            );
        } else if (!showMoreBtn && groups && groups.length > 0) {
            return (
                <Divider className="search-page__results-no-more">没啦没啦没啦</Divider>
            );
        } else {
            return null;
        }
    };

    /**
     * 「加载更多」按钮点击句柄
     */
    handleLoadingMore = async () => {
        this.setState({
            loadingMore: true
        });

        const {onLoadMore} = this.props;

        if (onLoadMore) {
            const hasResult = await onLoadMore.call(this);

            this.setState({
                loadingMore: false,
                showMoreBtn: hasResult,
            });
        }
    };

    /**
     * 采样模态框
     * @param {string} url
     */
    hanldleSamplingModalShow(url: string) {
        StoreManager.callstackStore.startToQuerySampling();
        StoreManager.callstackStore.callstackShowHead = true;
        StoreManager.callstackStore.loadCallstack(url);
    }

    render() {
        const {className, groups, loading} = this.props;
        const classString = classNames("search-page__results", {
            [this.SCROLL_TOP_CLASS]: true,
        }, className);

        return (
            <div className={classString}>
                <Skeleton active={true} loading={loading}>
                    <ul className="search-page__results-list" ref={this.resultsListRef}>
                        {this.renderSearchResultItems(groups)}
                        {this.renderMoreBtn()}
                    </ul>
                </Skeleton>
            </div>
        );
    }
}
