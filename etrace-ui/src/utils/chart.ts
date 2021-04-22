import {get} from "lodash";
import {Chart, ChartTypeEnum} from "../models/ChartModel";
import StoreManager from "../store/StoreManager";

export const clearChartTimeRange = (chart: Chart) => {
    if (chart && chart.targets) {
        chart.targets.forEach(target => {
            delete target.from;
            delete target.to;
        });
    }

    return chart; // if need
};

/**
 * 判断当前 Chart 是否在被允许的 type 中
 * @param {ChartTypeEnum[]} types 允许的 type
 * @param chart 当前 Chart
 * @return {boolean} 当前 type 是否在范围 types 内
 */
export const checkChartType = (types: ChartTypeEnum[], chart?: Chart): boolean => {
    chart = chart || StoreManager.editChartStore.getChart();
    const currType = get(chart, "config.type", ChartTypeEnum.Line);

    return !types ? false : types.indexOf(currType) > -1;
};