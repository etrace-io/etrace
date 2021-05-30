import {Chart, EMonitorMetricTarget, PrometheusData, PrometheusDataSet, Target} from "$models/ChartModel";
import {Http} from "$services/http";
import {CURR_API} from "$constants/API";
import {ENV} from "$constants/Env";
import StoreManager from "$store/StoreManager";
import {ResultSetModel} from "$models/ResultSetModel";
import {GroupModel} from "$models/GroupModel";
import {isEmpty, MapToObject} from "$utils/Util";
import moment from "moment";
import {ConvertFunctionEnum, ConvertFunctionModel} from "$utils/ConvertFunction";
import {calLineSequence} from "$utils/ChartDataConvert";
import {PROMETHEUS_URL_PROD, PROMETHEUS_URL_TEST} from "$constants/index";

const uuid = require("react-native-uuid");

/**
 * https://prometheus.io/docs/prometheus/latest/querying/api/
 * $ curl 'http://localhost:9090/api/v1/query_range?query=up&start=2015-07-01T20:10:30.781Z&end=2015-07-01T20:11:00.781Z&step=15s'
 */
function fetchMetricByOneTarget(target: EMonitorMetricTarget, indexStart?: number) {
    const url = CURR_API.env == ENV.PROD ? PROMETHEUS_URL_PROD : PROMETHEUS_URL_TEST;
    // const url = CURR_API.monitor + "/prometheus-daily";

    const start = target.from;
    const end = target.to;
    const step = "10s";

    let startSecond = moment(start).unix();
    startSecond = startSecond - startSecond % 10;
    let endSecond = moment(end).unix();
    endSecond = endSecond - endSecond % 10;

    // return Http.get<PrometheusDataSet[]>(url + `/api/v1/query_range?query=${ql}&start=${start}&end=${end}&step=${step}`);
    // &timeout=10s 好像无效
    return Http.get<PrometheusDataSet>(url + `/api/v1/query_range?query=${target.measurement}&start=${startSecond}&end=${endSecond}&step=${step}`)
        .then(data => transferData(data.data?.result, target, indexStart));
}

function transferData(data: PrometheusData[], target: EMonitorMetricTarget, indexStart?: number): Array<ResultSetModel> {
    let results: Array<ResultSetModel> = [];
    for (let i = 0; i < data.length; i++) {
        const one = data[i];

        let startTime = one.values[0][0];
        let endTime = one.values[one.values.length - 1][0];
        let interval = one.values.length > 1 ? (one.values[1][0] - one.values[0][0]) : 10000;
        startTime = startTime * 1000;
        endTime = endTime * 1000;
        interval = interval * 1000;
        //
        let groups: Array<GroupModel> = [];

        let group = new Map();

        // 组装 曲线名字，像grafana一样
        let seriesName = "" + indexStart;
        seriesName += one.metric.__name__ ? one.metric.__name__ + "{" : "{";
        for (let metricKey in one.metric) {
            if (metricKey != "__name__") {
                seriesName += `${metricKey}="${one.metric[metricKey]}",`;
            }
        }
        seriesName = seriesName.replace(/.$/, "}");

        group.set("target", seriesName + uuid.v4());

        let fields = new Map();
        let values = one.values.map(point => point[1]);
        fields.set(seriesName, values);

        groups.push({
            group,
            // fields这里只能接受{}，不能接受Map，否则后面画图时处理不了
            fields: MapToObject(fields),
        });

        // 处理 functions
        let functions: Array<ConvertFunctionModel> = [];

        let model = new ConvertFunctionModel(ConvertFunctionEnum.TARGET_DISPLAY, "target_display", [{
            name: "target_display",
            type: "boolean"
        }], [target.display !== false]);
        functions.push(model);

        let lineNum = calLineSequence(target.lineIndex === undefined ? i : target.lineIndex);
        let line = new ConvertFunctionModel(ConvertFunctionEnum.LINE_FLAG, "line_flag", [{
            name: "line_flag",
            type: "string"
        }], [lineNum]);
        functions.push(line);
        if (target.functions ) {
            functions.push(...target.functions);
        }

        results.push({
            "results": {
                groups,
                measurementName: "prometheus_has_no_measurement_name",
                startTime: startTime,
                endTime: endTime,
                interval: interval,
                pointCount: one.values.length
            },
            functions: functions,
            "name": "someName",
        });
    }

    return results;
}

function buildTargetWithoutOrderBy(target: Target, chart?: Chart, forceChangeTime: Boolean = false): Target {
    // set time range
    const selectedTime = StoreManager.urlParamStore.getSelectedTime();

    // to standard ISO string format: YYYY-MM-DDTHH:mm:ss.sssZ
    target.from = new Date(selectedTime.from).toISOString();
    target.to = new Date(selectedTime.to).toISOString();

    // setup variate for tag filters
    if (target.variate) {
        target.variate.map(variate => {
            const values = StoreManager.urlParamStore.getValues(variate);
            if (!isEmpty(values)) {
                // 只取第一个来做  $variate 替换，不支持更多的
                target.measurement = target.measurement.replace(new RegExp("\\$" + variate, "gm"), values[0]);
            }
        });
    }
    // if (target.variate) {
    //     target.variate.forEach(oneVariate => {
    //         target.measurement = target.measurement.replace(new RegExp("\\$" + oneVariate, "gm"), "33.20.6.41");
    //     });
    // }
    return target;
}

//
// export async function showTagKey(code: string, measurement: string): Promise<Array<string>> {
//     const url = CURR_API.env == ENV.PROD ? PROMETHEUS_URL_PROD : PROMETHEUS_URL_TEST;
//
//     let message = "获取Tag Key";
//     let requestUrl = url + `/api/v1/labels?match[]=${measurement}`;
//
//     console.log("showTagKey: ", requestUrl);
//
//     try {
//         let resp = await Get(requestUrl);
//         return resp?.data?.data;
//     } catch (err) {
//         handleError(err, message);
//     }
//     return [];
// }
//
// export async function showTagValue(code: string, measurement: string, tagKey: string, prefix?: string): Promise<Array<string>> {
//     const url = CURR_API.env == ENV.PROD ? PROMETHEUS_URL_PROD : PROMETHEUS_URL_TEST;
//     let requestUrl = url + `/api/v1/label/${tagKey}/values?match[]=${prefix}`;
//
//     console.log("showTagValue: ", requestUrl);
//
//     try {
//         let resp = await Get(requestUrl);
//         return resp?.data?.data;
//     } catch (err) {
//         console.warn("get tag value err:", err);
//     }
//     return [];
// }

export default {
    fetchMetricByTargets: fetchMetricByOneTarget,
    buildTargetWithoutOrderBy,
};
