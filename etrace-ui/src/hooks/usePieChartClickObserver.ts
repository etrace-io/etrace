import {get} from "lodash";
import {reaction} from "mobx";
import {useEffect} from "react";
import StoreManager from "$store/StoreManager";

/**
 * 监听 Pie 图点击事件
 */
const usePieChartClickObserver = () => {
    useEffect(() => {
        const {chartEventStore, urlParamStore} = StoreManager;

        const disposer = reaction(
            () => chartEventStore.pieClickEvent,
            event => {
                const groups = get(event, "item.groups", null);
                if (groups) {
                    urlParamStore.changeURLParams(groups);
                }
            }
        );

        return () => disposer();
    }, []);
};

export default usePieChartClickObserver;
