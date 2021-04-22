import {reaction} from "mobx";
import {useEffect} from "react";
import {QueryKit} from "$utils/Util";
import {useHistory} from "react-router-dom";
import StoreManager from "$store/StoreManager";
import {TIME_FROM, TIME_TO} from "$models/TimePickerModel";
import ChartEditConfig, {getConfigValue} from "$containers/Board/Explorer/ChartEditConfig";

const useWatchChartSeriesClick = () => {
    const history = useHistory();

    useEffect(() => {
        const disposer = reaction(
            () => StoreManager.chartEventStore.seriesClickEvent,
            event => {
                // const metric = get(event, "series.metric", null);
                const {metric, uniqueId} = event;
                if (!metric || !uniqueId) { return; }

                const chart = QueryKit.getChartData(uniqueId);
                const seriesLink = getConfigValue(ChartEditConfig.link.series, chart?.config);
                if (!seriesLink) { return; }

                const linkTarget = getConfigValue(ChartEditConfig.link.linkTarget, chart?.config);
                const timeRange = getConfigValue(ChartEditConfig.link.timeRange, chart?.config);

                /**
                 * field: 对应的字段
                 * metric: 对应的指标名
                 * monitorItem: 对应的监控项
                 * tag_tagKey: tagKey 对应的值，如 tag_ezone，指具体 ezone 的值
                 */

                const params = {
                    field: metric.field,
                    metric: metric.results.measurementName.replace(metric.name + ".", ""),
                    monitorItem: metric.name,
                };

                const tags = metric.tags;
                Object.keys(tags).forEach(key => params[`tag_${key}`] = tags[key]);

                let finalURL = Object.keys(params).reduce((link, key) => {
                    return link.replace(`$\{${key}}`, params[key]);
                }, seriesLink);

                if (timeRange) {
                    const [originURL, originSearch] = finalURL.split("?");

                    const searchParams = new URLSearchParams(originSearch);
                    const time = StoreManager.urlParamStore.getSelectedTime();
                    searchParams.set(TIME_FROM, time.fromString);
                    searchParams.set(TIME_TO, time.toString);

                    finalURL = [originURL, searchParams.toString()].join("?");
                }

                if (linkTarget === "_blank" || finalURL.startsWith("http")) {
                    window.open(finalURL, linkTarget);
                } else {
                    history.push(finalURL);
                }
            }
        );

        return () => disposer();

    }, []);
};

export default useWatchChartSeriesClick;
