import {Tooltip} from "antd";
import React, {useCallback, useEffect, useState} from "react";
import classNames from "classnames";
import {MenuItem} from "$models/Menu";
import {APP_ROUTER} from "$constants/Route";
import {LocalStorageKit} from "$utils/Util";
import {DeleteOutlined} from "@ant-design/icons/lib";
import FunctionItemCard from "$components/FunctionItemCard/FunctionItemCard";
import {saveGlobalSearchHistory} from "$components/GlobalSearchBox/GlobalSearchBox";
import {STORAGE_KEY_GLOBAL_SEARCH} from "$constants/index";

interface FunctionPanelProps {
    className?: string;
}

const FunctionPanel: React.FC<FunctionPanelProps> = props => {
    const { className } = props;

    const [history, setHistory] = useState<string[]>(
        () => LocalStorageKit.getArray(STORAGE_KEY_GLOBAL_SEARCH)
    );

    const watchLocalStorage = useCallback((e: any) => {
        setHistory(LocalStorageKit.getArray(STORAGE_KEY_GLOBAL_SEARCH));
    }, []);

    /**
     * 监听到 LocalStorage 变化的时候更新 List
     */
    useEffect(() => {
        window.addEventListener(STORAGE_KEY_GLOBAL_SEARCH, watchLocalStorage);
        return () => window.removeEventListener(STORAGE_KEY_GLOBAL_SEARCH, watchLocalStorage);
    }, [watchLocalStorage]);

    /**
     * 清空历史记录
     */
    const clearHistory = () => {
        localStorage.removeItem(STORAGE_KEY_GLOBAL_SEARCH);
        // 派发事件
        const event = new Event(STORAGE_KEY_GLOBAL_SEARCH);
        window.dispatchEvent(event);
    };

    const classString = classNames("search-page__function-panel", className);

    const clearHistoryBtn = (
        <Tooltip placement="left" title="清空搜索历史" color="blue">
            <DeleteOutlined type="delete" style={{ cursor: "pointer", padding: "9px 0" }} onClick={clearHistory} />
        </Tooltip>
    );

    const searchHistory = history.map(item => ({
        url: `/query?s=${item}`,
        label: item,
        onClick: (i: MenuItem) => saveGlobalSearchHistory(i.label), // 当搜索历史 item 点击后，更新搜索历史
    }));

    return (
        <div className={classString}>
            <FunctionItemCard
                title="功能入口"
                dataSource={APP_ROUTER}
                column={4}
            />

            <FunctionItemCard
                title="搜索历史"
                dataSource={searchHistory}
                notFoundContent="暂无搜索历史"
                extra={clearHistoryBtn}
            />
        </div>
    );
};

export default FunctionPanel;
