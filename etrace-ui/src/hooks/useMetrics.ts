import {reaction} from "mobx";
import {useQuery} from "react-query";
import cloneDeep from "lodash/cloneDeep";
import StoreManager from "$store/StoreManager";
import useSearchParams from "$hooks/useSearchParams";
import {ChartKit, QueryKit, TargetKit} from "$utils/Util";
import {useEffect, useMemo, useRef, useState} from "react";
import {MAX_SERIES_COUNT, QUERY_KEY_METRIC} from "$constants/index";

import {ChartInfo, EMonitorMetricDataSet, EMonitorMetricTargetType, MetricStatus} from "$models/ChartModel";

/**
 * 根据 Targets 数组，获取对应的指标信息
 */
export default function useMetrics(chart: ChartInfo) {
    const uniqueId = chart?.globalId;

    const initStatus = useRef(false); // 用于避免监听
    const [status, setStatus] = useState<MetricStatus>();

    // 监听所需字段变更（用于更新 Metric）
    const dependencyKeys = useMemo(() => chart ? Array.from(new Set([
        // Tag 变量
        ...(chart.targets?.map(target => target.variate).flat() || []),
        // 「指标名」中的变量
        ...(chart.targets?.map(target => target.measurementVars).flat() || []),
        // 监控项
        ...(chart.targets?.map(target => target.prefixVariate) || []),
        // 时间范围
        "from", "to", "timeshift",
    ])).filter(Boolean) : [], [chart]);

    const dependencyValues = useSearchParams(dependencyKeys);

    const {isFetching, data: metrics, refetch, remove} = useQuery(
        [QUERY_KEY_METRIC, chart?.globalId, chart],
        (context) => {
            // 拿到更新后的 chart（如编辑中）
            // const _chart = context.queryKey[2];
            const _chart = QueryKit.getChartData(chart.globalId);
            // 提取并解析 targets（已替换好变量内容）
            const targets = ChartKit.extractAndResolveTargetsFromChart(_chart);
            const queries = TargetKit.createQueriesByTargets(targets);

            return Promise
                .allSettled(queries)
                .then(res => res
                    .map<EMonitorMetricDataSet[]>(item => item.status === "fulfilled" ? item.value : [])
                    .flat()
                );
        },
        {
            // 设置请求 key，避免 chart 中因为 config 变更而重新发送请求
            enabled: !!chart?.globalId,
            queryKeyHashFn: queryKey => Array.isArray(queryKey)
                // 如果是数组，表示用该 useQuery 传递
                // 字符串则为搜索匹配（如 queryCache.find(queryKey, { exact: true })）
                ? QueryKit.getMetricQueryKey((queryKey[2] as ChartInfo)?.globalId)
                : queryKey,
            cacheTime: uniqueId ? Infinity : 0,
        }
    );

    useEffect(() => {
        if (!chart?.globalId) {
            remove();
        }
    }, [chart, remove]);

    // 状态 Metrics 变更
    useEffect(() => {
        const datasets = metrics?.map(metric =>
            metric.results?.groups?.map(group => Object.keys(group?.fields))
        ).flat(3).filter(Boolean) ?? [];

        if (!metrics) {
            setStatus(MetricStatus.INIT);
        } else if (datasets.length === 0) {
            setStatus(MetricStatus.NO_DATA);
        } else if (datasets.length > (StoreManager.orderByStore.limit || MAX_SERIES_COUNT)) {
            setStatus(MetricStatus.EXCEEDS_LIMIT);
        } else {
            setStatus(MetricStatus.DONE);
        }
    }, [metrics]);

    // 监听字段变更，重新构建 Target 发起请求
    useEffect(() => {
        // const _chart = QueryKit.getChartData(uniqueId);
        if (!chart) { return; }

        if (!initStatus.current) {
            initStatus.current = true;
            return;
        }

        const cloneChart: ChartInfo = cloneDeep(chart);
        for (let i = 0; i < cloneChart.targets.length; i++) {
            const target = cloneChart.targets[i];
            if (target._type !== EMonitorMetricTargetType.LINDB) { continue; }
            // todo: prometheus
            cloneChart.targets[i] = TargetKit.resolveLinDBTarget(target, chart);
        }
        QueryKit.reloadChartData(chart.globalId, cloneChart);
    }, [chart, dependencyValues]);

    // 添加刷新监听
    useEffect(() => {
        if (!uniqueId) { return; }

        const disposer = reaction(
            () =>  StoreManager.urlParamStore.forceChanged,
            () => { refetch().then(); }
        );

        return () => disposer();
    }, [refetch, uniqueId]);

    return {
        isLoading: isFetching,
        metrics,
        status,
    };
}
