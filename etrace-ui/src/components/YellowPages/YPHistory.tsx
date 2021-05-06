import React from "react";
import YpSearch from "./YPSearch";
import {Link} from "react-router-dom";
import {BlankBoxIcon} from "../Icon/Icon";
import {LocalStorageUtil} from "../../utils/LocalStorageUtil";
import YpSearchResultPage from "../../containers/YellowPages/YPSearchResultPage";
import {DeleteOutlined} from "@ant-design/icons/lib";

interface YpHistoryListProps {
}

interface YpHistoryListStatus {
    history: string[];
}

export class YpHistoryList extends React.Component<YpHistoryListProps, YpHistoryListStatus> {
    private localStorageKey = YpSearch.STORAGE_KEY_SEARCH_HISTORY;

    constructor(props: YpHistoryListProps) {
        super(props);
        this.state = {
            history: LocalStorageUtil.getStringValues(this.localStorageKey),
        };
        window.addEventListener(this.localStorageKey, this.watchLocalStorage);
    }

    componentWillUnmount(): void {
        window.removeEventListener(this.localStorageKey, this.watchLocalStorage);
    }

    watchLocalStorage = () => {
        this.setState({
            history: LocalStorageUtil.getStringValues(this.localStorageKey)
        });
    };

    /**
     * 清空历史记录
     */
    clearHistory() {
        localStorage.removeItem(this.localStorageKey);
        // 派发事件
        const event = new Event(this.localStorageKey);
        window.dispatchEvent(event);
    }

    getItemUrl = (item: string) => {
        return `${YpSearchResultPage.BASE_URL}?${YpSearchResultPage.YELLOW_PAGE_KEY}=${item}`;
    };

    handleDeleteHistory = (key: string) => {
        YpSearch.deleteSearchValue(key);
    };

    render() {
        const {history} = this.state;
        return (
            <div className="yellow-pages__history-list-container">
                <div className="e-monitor-yellow-pages__title">历史记录</div>
                <ol className="yellow-pages__history-list">
                    {(!history || history.length === 0) && (
                        <div className="history-list__empty-tips">
                            <BlankBoxIcon className="empty-tips__icon"/>
                            <div className="empty-tips__content">暂无搜索记录</div>
                        </div>
                    )}
                    {history && history.map(item => (
                        <li key={item}>
                            <Link to={this.getItemUrl(item)}>
                                <span className="item-name">{item}</span>
                            </Link>
                            <div className="item__operation">
                                <DeleteOutlined  onClick={() => this.handleDeleteHistory(item)}/>
                            </div>
                        </li>
                    ))}
                </ol>
            </div>
        );
    }
}
