/**
 * Metric 相关处理
 * - 根据输出对应 Series
 * - 查询得到的数据集处理
 */
import {TargetKit} from "$utils/Util";
import {ConvertFunctionEnum, ConvertFunctionModel} from "$utils/ConvertFunction";
import {
    EMonitorMetricDataSet,
    EMonitorMetricTarget,
    EMonitorMetricTargetType,
    SunfireMetricDataSet
} from "$models/ChartModel";

export default {
    resolveUniversalDataSet,
    mapTimeScale,
};

/**
 * fetch 到对应的结果集后，
 * 将额外数据源的结果集，转为通用结果集
 * @param type 数据源类型
 * @param targets 为了兼容不返回查询前数据信息的数据源，传入 targets 作为指标配置参考
 * @param dataSet 返回的数据集
 */
function resolveUniversalDataSet(type: EMonitorMetricTargetType, targets: EMonitorMetricTarget[], dataSet: any): EMonitorMetricDataSet[] {
    switch (type) {
        case EMonitorMetricTargetType.SUNFIRE:
            return resolveSunfireDateSet(dataSet, targets);
        case EMonitorMetricTargetType.LINDB:
        default:
            return dataSet;
    }
}

/**
 * Sunfire 指标数据转通用格式
 * @param result Sunfire 数据集
 * @param targets 原 targets 集合，用于获取对应 target 信息
 */
function resolveSunfireDateSet(result: SunfireMetricDataSet[], targets: EMonitorMetricTarget[]): EMonitorMetricDataSet[] {
    // 需要根据 data 内的 target 编号获取到 target 的索引位置

    return result?.map((data, index) => {
        // datapoints 其中是 [[1.2, 1580689800000]] 这样的结果，取第一个点的值
        const values = data.datapoints.map(point => point[0]);

        const groups = [{
            group: { target: data.target },
            fields: { [data.target]: values },
        }];

        const startTime = data.datapoints[0][1];
        const endTime = data.datapoints[data.datapoints.length - 1][1];
        const interval = data.datapoints.length > 1 ? (data.datapoints[1][1] - data.datapoints[0][1]) : 10000;

        const rawTargetString = data.target;
        const targetIndex = parseInt(rawTargetString.substring(rawTargetString.indexOf("(") + 1, rawTargetString.indexOf(")")));
        const target = targets[targetIndex];
        const functions = [
            new ConvertFunctionModel(
                ConvertFunctionEnum.TARGET_DISPLAY,
                "target_display",
                [{name: "target_display", type: "boolean"}],
                [target.display !== false]
            ),
            new ConvertFunctionModel(
                ConvertFunctionEnum.LINE_FLAG,
                "line_flag",
                [{name: "line_flag", type: "string"}],
                [TargetKit.targetIndexToCode(target.lineIndex ?? index)]
            ),
        ];

        const results = {
            groups,
            startTime: startTime,
            endTime: endTime,
            interval: interval,
            pointCount: values.length,
            measurementName: "__sunfire_has_no_measurement__",
        };

        return {
            results,
            functions,
            name: "__sunfire_data_set__",
        };
    }) || [];
}

function mapTimeScale(timeScale: number[]) {
    const timeScaleMap = {};
    timeScale.forEach((timestamp, idx) => {
        timeScaleMap[timestamp] = idx;
    });

    return timeScaleMap;
}
