import {get} from "lodash";
import {
    Chart,
    ComputeTarget,
    EMonitorMetricDataSet,
    EMonitorMetricTarget,
    MetricBean,
    StatItem,
    StatListItem,
    TagFilter,
    Target
} from "$models/ChartModel";
import {GroupModel} from "$models/GroupModel";
import {ResultModel} from "$models/ResultModel";
import {ResultSetModel} from "$models/ResultSetModel";
import StoreManager from "../store/StoreManager";
import {Get, Put} from "$utils/api";
import {calLineSequence} from "$utils/ChartDataConvert";
import {ConvertFunctionEnum, ConvertFunctionModel} from "$utils/ConvertFunction";
import * as notification from "../utils/notification";
import {duration, handleError} from "$utils/notification";
import {isEmpty} from "$utils/Util";
import {CURR_API} from "$constants/API";
import {MonitorHttp} from "$services/http";

const R = require("ramda");

export function buildStatItemListReport(resultSets: Array<ResultSetModel>): Array<StatItem> {
    let reportData = [];
    for (let m = 0; m < resultSets.length; m++) {
        let resultSet: ResultSetModel = resultSets[m];
        if (resultSet) {
            let result: ResultModel = resultSet.results;
            if (result) {
                const measurement = result.measurementName;
                const groupModels: Array<GroupModel> = result.groups;
                const interval = result.interval;
                for (let j = 0; j < groupModels.length; j++) {
                    const tags: Map<string, string> = groupModels[j].group;
                    let fields: Map<string, Array<number>> = groupModels[j].fields;
                    reportData.push(new StatItem(measurement, fields, tags, interval));
                }
            }
        }
    }
    return reportData;
}

function buildListReport(resultSets: Array<ResultSetModel>, showRatio: boolean): Array<StatListItem> {
    if (isEmpty(resultSets)) {
        return [];
    }
    let reportData: Map<string, StatListItem> = new Map();
    let index = 0;
    for (let m = 0; m < resultSets.length; m++) {
        let resultSet: ResultSetModel = resultSets[m];
        if (!resultSet) {
            continue;
        }
        let timeShift = getTimeshift(resultSet.functions);
        if (resultSet) {
            let result: ResultModel = resultSet.results;
            if (result) {
                let groupModels: Array<GroupModel> = result.groups;
                for (let j = 0; j < groupModels.length; j++) {
                    let groupModel = groupModels[j];
                    let groups: Map<string, string> = groupModel.group;
                    let group: string = "" + index;
                    let keys: string = "";
                    for (let key of Object.keys(groups)) {
                        keys += groups[`${key}`] + "/";
                    }
                    index++;
                    let fields: Map<string, Array<number>> = groupModel.fields;
                    if (fields) {
                        for (let key of Object.keys(fields)) {
                            let values: Array<number> = fields[`${key}`];
                            // values size should be 1
                            let value: number = values[0];
                            let statItem: StatListItem = reportData.get(keys);
                            if (statItem) {
                                if (timeShift) {
                                    statItem.historyValue = value;
                                    statItem.timeShift = timeShift;
                                } else {
                                    statItem.value = value;
                                }
                            } else {
                                reportData.set(keys, new StatListItem(group, value, groups, showRatio, timeShift));
                            }
                        }
                    }
                }
            }
        }
    }
    const resultData = [];
    reportData.forEach((v, k) => {
        resultData.push(v);
    });
    return resultData;
}

function getTimeshift(functions: Array<ConvertFunctionModel>) {
    if (functions) {
        for (let func of functions) {
            if (func.name == ConvertFunctionEnum.TIME_SHIFT) {
                let type: string = func.defaultParams[0];
                return Number.parseInt(type.slice(), 10);
            }
        }
    }
    return null;
}

function getInterval(functions: Array<ConvertFunctionModel>) {
    if (functions) {
        for (let func of functions) {
            if (func.name == ConvertFunctionEnum.INTERVAL) {
                return func.defaultParams[0];
            }
        }
    }
    return null;
}

export async function searchMetricsSimple(target: Target): Promise<Array<ResultSetModel>> {
    let url = CURR_API.monitor + "/metric";
    let resp = await Put(url, [target]);
    return resp.data;
}

export async function searchMetrics(targets: Array<Target>, chart?: Chart): Promise<Array<ResultSetModel>> {
    let url = CURR_API.monitor + "/metric";
    let tempFilters: Array<Target> = R.clone(targets);
    let pattern = /\$\{(.*?)\}/g;
    // 对于filter中的tag中非最后的"*"做转义处理
    let computeNum: Array<string> = [];
    if (chart) {
        get(chart, "config.computes", []).map((value: ComputeTarget) => {
            if (!ComputeTarget.valid(value)) {
                return;
            }
            let compute = value.compute;
            // 100*${AA}/${AB} ->  [AA, AB]
            let matched = pattern.exec(compute);
            while (matched != null) {
                computeNum.push(matched[1]);
                matched = pattern.exec(compute);
            }
        });
    }

    let filters: Array<Target> = [];
    tempFilters.forEach((filter, index) => {
        filter.tagFilters.forEach(tagFilter => {
            tagFilter.value = escapeStarInTagKeyAsArray(tagFilter.value);
        });

        let lineNum = calLineSequence(filter.lineIndex === undefined ? index : filter.lineIndex);
        if (!filter.functions) {
            filter.functions = [];
        }
        if (filter.display == undefined || filter.display == true) {
            let model = new ConvertFunctionModel(ConvertFunctionEnum.TARGET_DISPLAY, "target_display", [{
                name: "target_display",
                type: "boolean"
            }],                                  [true]);
            filter.functions.push(model);
        } else {
            let model = new ConvertFunctionModel(ConvertFunctionEnum.TARGET_DISPLAY, "target_display", [{
                name: "target_display",
                type: "boolean"
            }],                                  [false]);
            filter.functions.push(model);
        }

        let line = new ConvertFunctionModel(ConvertFunctionEnum.LINE_FLAG, "line_flag", [{
            name: "line_flag",
            type: "string"
        }],                                 [lineNum]);

        filter.functions.push(line);
        if (computeNum.length == 0 || computeNum.indexOf(lineNum) >= 0) {
            filters.push(filter);
        } else {
            if (filter.display !== false) {
                filters.push(filter);
            }
        }
    });

    if (chart) {
        const type: string = get(chart, "config.type", "line");
        if (type === "pie" || type === "radar" || type === "text") {
            filters.map((target) => {
                let fields: Array<string> = target.fields;
                fields.map((filed, indexFiled) => {
                    const regex = /^(t_max|t_min|stddev|variance|t_sum|t_gauge|t_mean)\((\S|\s)*\)$/;
                    if (!filed.match(regex)) {
                        let asIndex = filed.indexOf(" as ");
                        filed = "t_sum(" + filed;
                        if (asIndex >= 0) {
                            filed = filed.replace(" as ", ") as ");
                        } else {
                            filed = filed + ")";
                        }
                    }
                    fields[indexFiled] = filed;
                });
                return fields;
            });
        }
    }
    let resp = await Put(url, filters);
    return resp.data;
}

export async function searchMetricsList(filters: Array<Target>, showRatio: boolean): Promise<Array<StatListItem>> {
    let message = "获取LinDB数据";
    let url = CURR_API.monitor + "/metric";
    let resultSets: Array<ResultSetModel> = [];
    try {
        let resp = await Put(url, filters);
        resultSets = resp.data;
    } catch (err) {
        handleError(err, message);
    }
    return buildListReport(resultSets, showRatio);
}

export async function searchVariateList(filter: Target, variateKey: string) {
    let message = "获取LinDB数据";
    let url = CURR_API.monitor + "/metric";
    let resultSets: Array<ResultSetModel> = [];

    // todo 如果用到这个，使用LinDB t_xx函数支持
    try {
        let resp = await Put(url, [filter]);
        resultSets = resp.data;
    } catch (err) {
        let resp = err.response;
        if (resp) {
            if (resp.status !== 400) {
                notification.errorHandler({
                    message: message + "(" + resp.status + ")", description: resp.data.message, duration: duration
                });
            }
        } else {
            notification.errorHandler({message: message, description: err.message, duration: duration});
        }
    }
    return buildVariateList(resultSets, variateKey);
}

function buildVariateList(resultSets: Array<ResultSetModel>, variateKey: string): Array<string> {
    let reportData = [];
    if (resultSets) {
        let result: ResultModel = resultSets[0].results;
        if (result) {
            let groupModels: Array<GroupModel> = result.groups;
            for (let j = 0; j < groupModels.length; j++) {
                let groupModel = groupModels[j];
                let groups: Map<string, string> = groupModel.group;
                let variateValue = groups[`${variateKey}`];
                if (variateValue && reportData.indexOf(variateValue) < 0) {
                    reportData.push(variateValue);
                }
            }
        }
    }
    return reportData;
}

export async function searchMeasurement(target: Target) {
    let entity = target.entity;
    let prefix = target.prefix;
    let searchPrefix = isEmpty(prefix) ? "" : prefix;

    if (!isEmpty(target.measurement)) {
        if (!isEmpty(searchPrefix)) {
            searchPrefix += ".";
        }
        searchPrefix += target.measurement;
    } else if (target.prefixRequired) {
        // if prefixRequired, add `.` to query the metric name suggest.
        searchPrefix += ".";
    }

    let measurement: Array<string> = await showMeasurement(entity, searchPrefix);
    let result: Array<string> = [];
    if (measurement && measurement.length > 0) {
        if (target.prefixRequired != false) {
            result = measurement.filter(item => {
                return prefix + "." != item;
            }).map(item => {
                if (item) {
                    let a = item.replace(prefix + ".", "");
                    return a;
                }
            });
        } else {
            result = measurement;
        }

    }
    // only return 50 metric names at most.
    return result.slice(0, 50);
}

export async function showMeasurement(code: string, measurement: string): Promise<Array<string>> {
    let message = "获取Measurement";
    let url = CURR_API.monitor + "/metric/suggest?code=" +
        code + "&ql=show measurements with measurement =~'" + measurement + "' limit 200";
    let measurements = [];
    try {
        let resp = await Get(url);
        let resultSet: ResultSetModel = resp.data;
        if (!resultSet) {
            return measurements;
        }
        let result: ResultModel = resultSet.results;
        if (result) {
            let series = result.data;
            if (series) {
                return series;
            }
        }
    } catch (err) {
        let resp = err.response;
        if (!resp) {
            notification.errorHandler({message: message, description: err.message, duration: duration});
        }
    }
    return measurements;
}

export async function showTagKey(code: string, measurement: string): Promise<Array<string>> {
    let message = "获取Tag Key";
    let url = CURR_API.monitor + "/metric/suggest?code=" + code +
        "&ql=show tag keys from '" + measurement + "' limit 100";
    let tagKeys = [];
    try {
        let resp = await Get(url);
        let resultSet: ResultSetModel = resp.data;
        if (!resultSet) {
            return tagKeys;
        }
        let result = resultSet.results;
        if (result) {
            let series = result.data;
            if (series) {
                return series;
            }
        }
    } catch (err) {
        handleError(err, message);
    }
    return tagKeys;
}

export async function showTagValueByMetricBean(metricBean: MetricBean): Promise<Array<any>> {
    let url = CURR_API.monitor + "/metric/tagSuggest";
    let resultSets: Array<ResultSetModel> = [];
    try {
        let resp = await Put(url, metricBean);
        resultSets = resp.data.results.data;
    } catch (err) {
        console.warn("get tag value err:", err);
    }
    return resultSets;
}

export async function showTagValue(code: string, measurement: string, tagKey: string, prefix?: string): Promise<Array<string>> {
    let url = CURR_API.monitor + "/metric/suggest?code=" + code
        + "&ql=show tag values from '" + measurement + "' with key='" + tagKey + "'";
    if (!isEmpty(prefix)) {
        url += " where value = '" + prefix + "*'";
    }
    url += " limit 100";
    let tagValues = [];
    try {
        let resp = await Get(url);
        let resultSet: ResultSetModel = resp.data;
        if (!resultSet) {
            return [];
        }
        let result: ResultModel = resultSet.results;
        if (result) {
            let series = result.data;
            if (series) {
                return series;
            }
        }
    } catch (err) {
        console.warn("get tag value err:", err);
    }
    return tagValues;
}

export async function showField(code: string, measurement: string): Promise<Array<string>> {
    let message = "获取Field";
    let url = CURR_API.monitor + "/metric/suggest?code=" +
        code + "&ql=show field keys from '" + measurement + "' limit 100";
    let fieldKeys = [];
    try {
        let resp = await Get(url);
        let resultSet: ResultSetModel = resp.data;
        if (!resultSet) {
            return fieldKeys;
        }
        let result: ResultModel = resultSet.results;
        if (result) {
            let series = result.data;
            if (series) {
                for (let key of Object.keys(series)) {
                    fieldKeys.push(key);
                }
            }
        }
    } catch (err) {
        handleError(err, message);
    }
    return fieldKeys;
}

/**
 * 根据URL上面的参数，构建新的Target对象，但不是处理Target的order by
 * @param target 老的Target对象
 * @param chart 用于判断 Chart Config 中的配置
 * @param forceChangeTime 是否强制修改时间
 * @return 新的对象
 */
export function buildTargetWithoutOrderBy(target: Target, chart?: Chart, forceChangeTime: Boolean = false): Target {
    const result = R.clone(target);
    // setup measurement
    if (!isEmpty(result.measurementVars)) {
        result.measurementVars.map(measurementVar => {
            const val = StoreManager.urlParamStore.getValue(measurementVar);
            if (!isEmpty(val)) {
                result.measurement = result.measurement.replace("${" + measurementVar + "}", val);
            }
        });
    }
    if (!isEmpty(result.prefixVariate)) {
        const val = StoreManager.urlParamStore.getValue(result.prefixVariate);
        if (!isEmpty(val)) {
            result.prefix = val;
        }
    }
    // setup variate for tag filters
    const tagFilters = result.tagFilters || [];
    if (result.variate) {
        result.variate.map(variate => {
            const values = StoreManager.urlParamStore.getValues(variate);
            if (!isEmpty(values)) {
                const index = R.findIndex(R.propEq("key", variate))(tagFilters);
                if (index > -1 && tagFilters[index].op === "=") {
                    const tagFilter = tagFilters[index];
                    if (!tagFilter.value) {
                        tagFilter.value = values;
                    } else {
                        tagFilter.value.push(...values);
                    }
                } else {
                    const tag: TagFilter = new TagFilter();
                    tag.key = variate;
                    tag.op = "=";
                    tag.value = values;
                    tagFilters.push(tag);
                }
            }
        });
    }
    result.tagFilters = tagFilters;

    // set time range
    const selectedTime = StoreManager.urlParamStore.getSelectedTime();
    const relativeTime = get(chart, "config.config.timeRange.relative", null);
    if (!chart || (chart && !chart.staticChart)) {
        if (!forceChangeTime && result.from && result.to) {
            // nothing change
        } else if (relativeTime && relativeTime.length > 0) {
            result.from = "now()-" + relativeTime;
            result.to = "now()-30s";
        } else if (selectedTime) {
            result.from = selectedTime.fromString;
            result.to = selectedTime.toString;
        }
        const interval = getInterval(result.functions);
        if (result.to === "now()-30s" && /([1-5])m/.test(interval)) {
            result.to = `now()-${interval}`;
        }
    }
    // set time shift
    const timeShift = StoreManager.urlParamStore.getTimeShift();
    if (!isEmpty(timeShift)) {
        get(result, "functions", []).forEach(fun => {
            if (fun.name === "timeShift") {
                fun.defaultParams = [timeShift];
            }
        });
    }

    return result;
}

function escapeStarInTagKey(tagKey: string): string {
    if (!tagKey) {
        return tagKey;
    } else {
        // 仅针对: 非最后的"*"
        let newTagKey = tagKey.replace(/\*(?!$)/g, "\\*");
        if (tagKey.length != newTagKey.length) {
            console.warn("为解决Tag中带'*'的指标查询问题，将Tag中的[%s]转换为[%s]", tagKey, newTagKey);
        }
        return newTagKey;
    }
}

function escapeStarInTagKeyAsArray(tagKeys: string[]): string[] {
    return tagKeys.map(tk => escapeStarInTagKey(tk));
}

function fetchMetricByTargets(targets: EMonitorMetricTarget[]) {
    return MonitorHttp.put<EMonitorMetricDataSet[]>("/metric", targets);
}

function fetchField(entity: string, measurement: string) {
    return MonitorHttp
        .get(`/metric/suggest?code=${entity}&ql=show field keys from '${measurement}' limit 100`)
        .catch(err => {
            handleError(err, "获取 Field");
        })
        .then(res => {
            const series = res.results?.data;
            return series ? {
                fields: Object.keys(series),
                measurement,
            } : null;
        });
}

export default {
    fetchMetricByTargets,
    fetchField,
};
