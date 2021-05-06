// 重新请求对应 chart
import {queryClient} from "$services/http";
import {QUERY_KEY_CHART, QUERY_KEY_METRIC} from "$constants/index";
import {QueryFilters} from "react-query/types/core/utils";
import {ChartInfo} from "$models/ChartModel";

export default {
    getChartData,
    refetchMetrics,
    getChartQueryKey,
    getMetricQueryKey,
    reloadChartData,
};

function getChartData(id: string): ChartInfo {
    // console.log(
    //     "get",
    //     id,
    //     queryClient.getQueryData(getChartQueryKey(id), { exact: true })
    // );
    // console.trace()
    return queryClient.getQueryData(getChartQueryKey(id));
}

// 用于重新获取 Chart 并调度 Metric 的获取
// 支持传入 chart 用于（不请求）直接设置 chart 信息，方便全局获取
function reloadChartData(id: string, chart?: ChartInfo) {
    const chartKey = getChartQueryKey(id);
    if (chart) {
        queryClient.setQueryData(chartKey, chart);
        refetchMetrics(id).then();
    } else {
        refetchChart(id).then(() => {
            refetchMetrics(id).then();
        });
    }
}

// 仅重新获取 Metrics
function refetchMetrics(globalId: string) {
    // 在 refetch 之前，需要更新对应的 chart
    return refetchQuery(getMetricQueryKey(globalId));
}

// 仅重新获取 Chart 信息
function refetchChart(globalId: string) {
    return refetchQuery(getChartQueryKey(globalId));
}

function refetchQuery(key: string) {
    const query = findQuery(key);
    return query?.fetch();
}

function findQuery(key: string | unknown[], options: QueryFilters = {exact: true}) {
    const queryCache = queryClient.getQueryCache();
    return queryCache.find(key, options);
}

// 获取对应 Chart 的 Metric 请求 key
function getMetricQueryKey(globalId: string) {
    return globalId ? QUERY_KEY_METRIC + globalId : "";
}

// 获取 Chart 的请求 key
function getChartQueryKey(id: string) {
    return QUERY_KEY_CHART + id;
}
