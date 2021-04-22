import {useRef} from "react";
import set from "lodash/set";
import {QueryKit} from "$utils/Util";
import {useQuery} from "react-query";
import {queryClient} from "$services/http";
import {ChartInfo} from "$models/ChartModel";
import ChartService from "$services/ChartService";
import ChartEditConfig, {getConfigValue} from "$containers/Board/Explorer/ChartEditConfig";

interface ChartSearchOptions {
    id?: string;
    globalId?: string;
    chart?: ChartInfo; // 外部数据源
    // 用于采样
    metricType?: string;
    // 用于设置监控项
    prefixVariate?: string;
    // 手动控制是否获取
    enabled?: boolean;
}

/**
 * 通过 ID 或 GlobalID 获取对应的 Chart 信息
 * GlobalID 优先
 */
export default function useChart(options: ChartSearchOptions) {
    const {id, globalId, chart, metricType, prefixVariate, enabled} = options;
    const uniqueId = globalId || id || chart?.globalId;
    const queryKey = QueryKit.getChartQueryKey(uniqueId);

    const memoizedChart = useRef<ChartInfo>();

    if (chart !== memoizedChart.current) {
        queryClient.setQueryData(queryKey, chart);
        memoizedChart.current = chart;
    }

    const fetchEnable = enabled ?? (!chart && !!uniqueId);

    const {data: chartInfo, isFetching} = useQuery(
        queryKey,
        () => globalId
            ? ChartService.fetchChartByGlobalId(globalId)
            : ChartService.fetchChartById(id),
        {
            enabled: fetchEnable,
            cacheTime: uniqueId ? Infinity : 0,
        },
    );

    chartInfo?.targets?.forEach(target => {
        target.metricType = metricType; // 用于采样
        target.prefixVariate = prefixVariate; // 用于 Editable 替换监控项变量
    });

    const analyzeConfig = getConfigValue(ChartEditConfig.analyze, chartInfo?.config);
    if (analyzeConfig) {
        set(chartInfo.config, `${ChartEditConfig.analyze.target.path}.metricType`, metricType);
        set(chartInfo.config, `${ChartEditConfig.analyze.target.path}.prefixVariate`, prefixVariate);
    }

    return {
        chart: enabled ? null : chartInfo,
        isLoading: isFetching,
    };
}
