import {Chart, EMonitorMetricTarget, SunfireMetricDataSet, Target} from "$models/ChartModel";
import {ResultSetModel} from "$models/ResultSetModel";
import {Post} from "$utils/api";
import StoreManager from "../store/StoreManager";
import {GroupModel} from "$models/GroupModel";
import {MapToObject} from "$utils/Util";
import {ConvertFunctionEnum, ConvertFunctionModel} from "$utils/ConvertFunction";
import {calLineSequence} from "$utils/ChartDataConvert";
import {Http} from "$services/http";

export class SimpleJsonService {

    /**
     * 参考自 src/service/LinDBService.tsx:462
     * @param target
     * @param chart
     * @param forceChangeTime
     */
    public static buildTargetWithoutOrderBy(target: Target, chart?: Chart, forceChangeTime: Boolean = false): Target {
        // set time range
        const selectedTime = StoreManager.urlParamStore.getSelectedTime();

        // to standard ISO string format: YYYY-MM-DDTHH:mm:ss.sssZ
        target.from = new Date(selectedTime.from).toISOString();
        target.to = new Date(selectedTime.to).toISOString();
        return target;
    }

    /**
     * 以grafana的格式请求数据：
     *
     * https://grafana.com/grafana/plugins/grafana-simple-json-datasource
     *
     * @param targets
     */
    public static async searchMetrics(targets: Array<Target>): Promise<Array<ResultSetModel>> {
        let url = "https://etrace-gw.ele.me/sunfire";

        let simpleJsonTarget = targets.map((target, index) => {
            return {
                "target": target.measurement,
                "refId": index,
                "type": "timeserie"
            };
        });

        let requestBody = {
            "panelId": 1,
            "range": {
                "from": targets[0].from,
                "to": targets[0].to,
            },
            "rangeRaw": {
                "from": "now-6h",
                "to": "now"
            },
            "interval": "30s",
            "intervalMs": 30000,
            "targets": simpleJsonTarget,
            "adhocFilters": [],
            "format": "json",
            "maxDataPoints": 550
        };

        let resp = await Post(url, requestBody);
        if (!resp.data) {
            return [];
        }
        return this.transferData(resp.data, targets);
    }

    private static transferData(data: SunfireMetricDataSet[], targets: Array<Target>): Array<ResultSetModel> {
        // data 示例
        //
        // 0: {datapoints: Array(180), target: "(0)unsh 磁盘使用率"}
        // 1: {datapoints: Array(180), target: "(0)center 磁盘使用率"}
        // 2: {datapoints: Array(180), target: "(0)une1 磁盘使用率"}
        // 3: {datapoints: Array(180), target: "(1)center 磁盘使用率"}
        // 4: {datapoints: Array(180), target: "(1)une1 磁盘使用率"}
        // 5: {datapoints: Array(180), target: "(1)unsh 磁盘使用率"}
        //
        // 需要根据data内的target编号获取到target的索引位置

        let results: Array<ResultSetModel> = [];
        for (let i = 0; i < data.length; i++) {
            const rawTargetString = data[i].target;
            const targetIndex: number = parseInt(rawTargetString.substring(rawTargetString.indexOf("(") + 1, rawTargetString.indexOf(")")));
            const target = targets[targetIndex];

            const one = data[i];
            let group = new Map();
            group.set("target", one.target);

            // datapoints其中是[[1.2, 1580689800000]] 这样的结果，取第一个点的值
            let values = one.datapoints.map(point => point[0]);

            let fields = new Map();
            fields.set(one.target, values);

            let groups: Array<GroupModel> = [];
            groups.push({
                group,
                // fields这里只能接受{}，不能接受Map，否则后面画图时处理不了
                fields: MapToObject(fields),
            });

            let startTime = one.datapoints[0][1];
            let endTime = one.datapoints[one.datapoints.length - 1][1];
            let interval = one.datapoints.length > 1 ? (one.datapoints[1][1] - one.datapoints[0][1]) : 10000;

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
            }],                                 [lineNum]);
            functions.push(line);

            results.push( {
                "results": {
                    groups,
                    measurementName: "sunfire_has_no_measurement_name",
                    startTime: startTime,
                    endTime: endTime,
                    interval: interval,
                    pointCount: values.length,
                },
                functions: functions,
                "name": "someName",
            });
        }

        return results;
    }
}

function fetchMetricByTargets(targets: EMonitorMetricTarget[]) {
    // 构建请求体
    const simpleJsonTargets = targets.map((target, index) => ({
        target: target.measurement,
        refId: index,
        type: "timeserie"
    }));

    const params = {
        panelId: 1,
        range: {
            from: targets[0].from,
            to: targets[0].to,
        },
        rangeRaw: {
            from: "now-6h",
            to: "now"
        },
        interval: "30s",
        intervalMs: 30000,
        targets: simpleJsonTargets,
        adhocFilters: [],
        format: "json",
        maxDataPoints: 550
    };

    return Http.post<SunfireMetricDataSet[]>("https://etrace-gw.ele.me/sunfire", params);
}

export default {
    fetchMetricByTargets,
};
