/**
 * Target 相关处理，目标输出对应的 Metric
 */
import get from "lodash/get";
import groupBy from "lodash/groupBy";
import cloneDeep from "lodash/cloneDeep";
import StoreManager from "$store/StoreManager";
import SunfireService from "$services/SimpleJsonService";
import {ConvertFunctionEnum, ConvertFunctionModel} from "$utils/ConvertFunction";
import {
    ChartInfo,
    ChartTypeEnum,
    EMonitorMetricComputeTarget,
    EMonitorMetricTagFilter,
    EMonitorMetricTarget,
    EMonitorMetricTargetType
} from "$models/ChartModel";
import {ChartKit, MetricKit} from "$utils/Util";
import LinDBService from "$services/LinDBService";
import uniq from "lodash/uniq";
import moment, {DurationInputArg2} from "moment";
import PrometheusService from "$services/PrometheusService";

type Target = EMonitorMetricTarget | null;
type ComputeTarget = EMonitorMetricComputeTarget | null;

export default {
    checkTargetValid,
    checkComputeTargetValid,
    isSimpleJsonTarget,
    isPrometheusTarget,
    resolveLinDBTarget,
    resolveSunfireTarget,
    resolvePrometheusTarget,
    getValueFromFunctions,
    getCountPS,
    getAliasNameByFunctions,
    targetIndexToCode,
    createQueriesByTargets,
    groupTargetsByType,
    extractVariateFromComputedTargetExpression,
    resolveTimeShit,
    resolveTagFiltersFromURL,
};

/**
 * 判断是否为 SimpleJson 类型
 * @param target 待检验 Target
 */
function isSimpleJsonTarget(target: Target): boolean {
    return target?.entity === "sunfire";
    // return !!(target && target.entity && target.entity === "sunfire");
}

/**
 * 判断是否为 prometheus 类型
 * @param target 待检验 Target
 */
function isPrometheusTarget(target: Target): boolean {
    return target?.entity?.toLocaleLowerCase().includes("prometheus") ?? false;
}

/**
 * 校验 Target 是否合法
 * @param target 待检验 Target
 */
function checkTargetValid(target?: Target): boolean {
    if (!target) { return false; }
    if (target.isAnalyze) { return true; }
    return !!(target.measurement && target.fields);
}

/**
 * 检验计算指标的合法性
 * @param target 计算指标
 */
function checkComputeTargetValid(target?: ComputeTarget): boolean {
    let compute = target?.compute;

    if (!compute) { return false; }

    // 括号是否成对
    if (compute.match(/\(/g)?.length !== compute.match(/\)/g)?.length) {
        return false;
    }

    // const keys: number[] = [];
    const j = compute.split(/\([+\-*/]/).length - 1;
    const n = compute.split(/\(/).length - 1;
    const m = compute.split(/\)/).length - 1;
    // keys.push( j, j > 0 ? n - j : n, m );

    for (let i = 0; i < j; i++) {
        compute = compute.replace(/\([+\-*/]/, "");
    }
    for (let i = 0; i < (j > 0 ? n - j : n); i++) {
        compute = compute.replace("(", "");
    }
    for (let i = 0; i < m; i++) {
        compute = compute.replace(")", "");
    }

    return compute.search(/^((\$\{[A-Z]+\})|[0-9]*)+([+\-*/]{1}((\$\{[A-Z]+\}){1}|[0-9]+){1})*$/) >= 0;
}

/**
 * 同 resolveLinDBTarget
 * 动态替换 target 内容
 * @param target
 */
function resolveSunfireTarget(target: Target): Target {
    if (!target) { return null; }

    const newTarget = cloneDeep(target);
    newTarget._type = EMonitorMetricTargetType.SUNFIRE;

    const selectedTime = getSelectedTimeRange();
    newTarget.from = new Date(selectedTime.from).toISOString(); // 其实会根据 MQ 去获取数据，这里意义不大
    newTarget.to = new Date(selectedTime.to).toISOString();

    return newTarget;
}

/**
 * 同 resolveLinDBTarget
 * 动态替换 target 内容
 * @param target
 */
function resolvePrometheusTarget(target: Target): Target {
    if (!target) { return null; }

    const newTarget = cloneDeep(target);
    newTarget._type = EMonitorMetricTargetType.PROMETHEUS;
    // const selectedTime = getSelectedTimeRange();
    // newTarget.from = new Date(selectedTime.from).toISOString(); // 其实会根据 MQ 去获取数据，这里意义不大
    // newTarget.to = new Date(selectedTime.to).toISOString();

    return newTarget;
}

/**
 * 主要处理 Target 中的变量部分
 * 从 URL 中获取对应变量值，将真实值赋值到对应字段中
 * @param target 待处理 target
 * @param chart
 * @param overrideTimeRange 是否覆盖原有时间范围
 */
function resolveLinDBTarget(target: Target, chart: ChartInfo, overrideTimeRange?: boolean): Target {
    if (!target) { return null; }

    const newTarget = cloneDeep(target);
    newTarget._type = EMonitorMetricTargetType.LINDB;

    // 处理「监控项」中的变量
    if (newTarget.prefixVariate) {
        const urlValue = getValueFromURL(newTarget.prefixVariate);
        if (urlValue) {
            newTarget.prefix = urlValue;
        }
    }

    // 处理「指标名」中的变量
    newTarget.measurementVars?.forEach(variate => {
        const urlValue = getValueFromURL(variate);
        if (urlValue) {
            newTarget.measurement = newTarget.measurement?.replace("${" + variate + "}", urlValue);
        }
    });

    // 处理「变量」，添加对应的变量值到 TagFilters
    newTarget.tagFilters = resolveTagFiltersFromURL(newTarget);

    // 处理「时间范围」
    if (overrideTimeRange || !(newTarget.from && newTarget.to)) {
        const selectedTime = getSelectedTimeRange();
        const relativeTime = getRelativeTimeFromChart(chart);

        if (relativeTime && relativeTime.length > 0) {
            newTarget.from = "now()-" + relativeTime;
            newTarget.to = "now()-30s";
        } else if (selectedTime) {
            newTarget.from = selectedTime.fromString;
            newTarget.to = selectedTime.toString;
        }

        const interval = getValueFromFunctions(newTarget.functions, ConvertFunctionEnum.INTERVAL);
        if (newTarget.to === "now()-30s" && /([1-5])m/.test(interval)) {
            newTarget.to = `now()-${interval}`;
        }
    }

    // 处理「环比」
    setTimeShiftToFunc(getTimeShift(), newTarget.functions);

    // 处理「Order By」
    setOrderByToTarget(newTarget);

    // 处理 fields
    const chartType = ChartKit.getChartType(chart);
    if (
        chartType === ChartTypeEnum.Pie ||
        chartType === ChartTypeEnum.Radar ||
        chartType === ChartTypeEnum.Text
    ) {
        newTarget.fields = newTarget.fields?.map(field => {
            if (!field.match(/^(t_max|t_min|stddev|variance|t_sum|t_gauge|t_mean)\((\S|\s)*\)$/)) {
                field = "t_sum(" + field;

                field = field.includes(" as ")
                    ? field.replace(" as ", ") as ")
                    : field + ")";
            }
            return field;
        });
    }

    // 添加 functions
    const displayFunc = new ConvertFunctionModel(
        ConvertFunctionEnum.TARGET_DISPLAY,
        "target_display",
        [{name: "target_display", type: "boolean"}],
        [!(newTarget.display === false)]
    );
    const lineFlagFunc = new ConvertFunctionModel(
        ConvertFunctionEnum.LINE_FLAG,
        "line_flag",
        [{name: "line_flag", type: "string"}],
        [targetIndexToCode(target.lineIndex ?? chart.targets.indexOf(target))]
    );

    if (!newTarget.functions) {
        newTarget.functions = [
            displayFunc,
            lineFlagFunc
        ];
    } else {
        const _displayIndex = newTarget.functions?.findIndex(func => func.name === "target_display");
        const _lineFlagIndex = newTarget.functions?.findIndex(func => func.name === "line_flag");

        if (_displayIndex > -1) {
            newTarget.functions[_displayIndex] = displayFunc;
        } else {
            newTarget.functions.push(displayFunc);
        }

        if (_lineFlagIndex > -1) {
            newTarget.functions[_lineFlagIndex] = lineFlagFunc;
        } else {
            newTarget.functions.push(lineFlagFunc);
        }
    }

    return newTarget;
}

/**
 * 为每个 target 创建请求函数
 */
function createQueriesByTargets(targets: Target[]) {
    // 数据源分类
    const groupedTargets = groupTargetsByType(targets);

    return Object
        .keys(groupedTargets)
        .map((type: EMonitorMetricTargetType) => {
            const currTargets = groupedTargets[type];
            let query;

            switch (type) {
                case EMonitorMetricTargetType.SUNFIRE:
                    // query = SunfireService.fetchMetricByTargets(currTargets);
                    query = SunfireService.fetchMetricByTargets.bind(null, currTargets);
                    break;
                case EMonitorMetricTargetType.PROMETHEUS:
                    query = PrometheusService.fetchMetricByTargets.bind(null, currTargets);
                    break;
                case EMonitorMetricTargetType.LINDB:
                default:
                    // query = LinDBService.fetchMetricByTargets(currTargets);
                    query = LinDBService.fetchMetricByTargets.bind(null, currTargets);
                    break;
            }

            // 将不同类型的数据源转为同一数据集格式
            return query().then(MetricKit.resolveUniversalDataSet.bind(null, type, currTargets));
        });
}

/**
 * 根据 URL 中监听的变量，修改 Target 中的 tagFilters 「不存在副作用」
 */
function resolveTagFiltersFromURL(target: Target) {
    // 处理「变量」，添加对应的变量值到 TagFilters
    const tagFilters = cloneDeep(target.tagFilters) || [];
    target.variate?.forEach(variate => {
        const urlValues = getValuesFromURL(variate);

        if (urlValues) {
            const tagFilter = tagFilters?.find(filter => filter.key === variate && filter.op == "=") ;
            if (tagFilter) {
                tagFilter.value = Array.from(new Set(urlValues));
            } else if (urlValues.length > 0) {
                tagFilters.push(createTagFilter(variate, urlValues));
            }
        }
    });

    // 处理 tagFilters 的 value
    tagFilters.forEach(filter => {
        filter.value = filter.value?.map(escapeTarKeyWildcard);
    });

    return tagFilters;
}

/* 工具函数 */

// 创建一个新的 tagFilter
function createTagFilter(key: string, value?: string[]): EMonitorMetricTagFilter {
    return {
        display: true,
        key,
        op: "=",
        value: value || [],
    };
}

// 获取 URL 中的 value（直接量）
function getValueFromURL(key: string): string | null {
    return StoreManager.urlParamStore.getValue(key);
}

// 获取 URL 中的 values（数组）
function getValuesFromURL(key: string): string[] {
    return StoreManager.urlParamStore.getValues(key);
}

// 获取 URL 中设置的时间
function getSelectedTimeRange() {
    return StoreManager.urlParamStore.getSelectedTime();
}

// 获取 URL 中的环比
function getTimeShift() {
    return StoreManager.urlParamStore.getTimeShift();
}

// 获取环比对应的时间戳超度
function resolveTimeShit(timeShift: string) {
    if (!timeShift) { return null; }

    const type = timeShift.slice(-1);
    const duration = timeShift.slice(0, -1);

    const start: number = moment().unix();
    const end: number = moment().subtract(duration, type as DurationInputArg2).unix();
    return (start - end) * 1000;
}

// 获取配置中的「相对时间」
function getRelativeTimeFromChart(chart: ChartInfo) {
    return get(chart, "config.config.timeRange.relative", null);
}

// 根据 field 获取 orderBy
function getOrderByWithField(field: string) {
    const {orderByStore} = StoreManager;

    if (field.includes(" as ")) {
        field = field.split("as")[0].trimRight();
    }

    if (field.match(/^(t_max|t_min|stddev|variance|t_sum|t_gauge|t_mean)\((\S|\s)*\)$/)) {
        return orderByStore.getOrderBy(field);
    }

    return orderByStore.getOrderBy(`${orderByStore.type}(${field})`);
}

// 获取指定 Function 中的 value
function getCountPS(dataSource: ConvertFunctionModel[]) {
    if (!dataSource) { return; }
    return dataSource.find(func => func.name === ConvertFunctionEnum.COUNT_PS)?.defaultParams;
}

// 获取指定 Function 中的 value
function getValueFromFunctions(dataSource: ConvertFunctionModel[], target: ConvertFunctionEnum) {
    if (!dataSource) { return; }
    return dataSource.find(func => func.name === target)?.defaultParams?.[0];
}

// 获取指标 alias 后的「显示名称」
function getAliasNameByFunctions(name: string, functions: ConvertFunctionModel[]) {
    return functions?.reduce(function (prevName: string, func: ConvertFunctionModel) {
        switch (func.modelEnum) {
            case ConvertFunctionEnum.ALIAS_REPLACE:
                try {
                    const [search, replace] = func.defaultParams;
                    if (!search) { return prevName; }
                    return (new RegExp(search)).test(prevName) ? replace : prevName;
                } catch {
                    return prevName;
                }
            case ConvertFunctionEnum.ALIAS:
                return func.defaultParams[0];
            case ConvertFunctionEnum.ALIAS_PREFIX:
                return func.defaultParams[0] + prevName;
            case ConvertFunctionEnum.ALIAS_POSTFIX:
                return prevName + func.defaultParams[0];
            case ConvertFunctionEnum.ALIAS_PATTERN: {
                // {{textA}}-{{textB}}-{{textC}}
                let pattern = func.defaultParams[0];
                let patternKeys = pattern.match(/{{\w+}}/g);
                if (patternKeys && patternKeys.length) {
                    patternKeys.forEach(onePatternKey => {
                        if (onePatternKey.length > 4) {
                            let key = onePatternKey.substring(2, onePatternKey.length - 2);
                            let indexInGroupName = name.indexOf(key + "=");
                            if (indexInGroupName >= 0) {
                                let value = name.substring(indexInGroupName + key.length + 2,
                                    name.indexOf("\"", indexInGroupName + key.length + 2));
                                pattern = pattern.replace(onePatternKey, value);
                            }
                        }
                    });
                }
                return pattern;
            }
            default:
                return prevName;
        }
    }, name) || name;
}

// 为不同数据源的 target 分类
function groupTargetsByType(targets: Target[]): { [type: string]: Target[] } {
    return groupBy(targets, "_type");
}

// 转义 TagFilter 中的 value 中的通配符
function escapeTarKeyWildcard(tagKey: string) {
    const newTagKey = tagKey?.replace(/\*(?!$)/g, "\\*");
    if (tagKey?.length !== newTagKey?.length) {
        console.warn("为解决 Tag 中带 '*' 的指标查询问题，将 Tag 中的 [%s] 转换为 [%s] ", tagKey, newTagKey);
    }
    return newTagKey;
}

function targetIndexToCode(lineNum: number) {
    // 65:A 90:Z
    if (lineNum < 26) {
        return String.fromCharCode(lineNum + 65);
    }
    // from AA to ZZ
    const highRank = lineNum / 26 - 1;
    const lowRank = lineNum % 26;
    return String.fromCharCode(highRank + 65) + String.fromCharCode(lowRank + 65);
}

/**
 * 从计算指标表达式中提取变量名
 * @param expression 表达式
 */
function extractVariateFromComputedTargetExpression(expression: string) {
    // `100 * ${AA} / ${AB}` -> ["AA", "AB"]
    const reg = /\${(.*?)}/g;
    let computeNum: string[] = [];
    let matched;
    while ((matched = reg.exec(expression)) !== null) {
        computeNum.push(matched[1]);
    }

    return uniq(computeNum);
}

// 以下 set 方法均产生副作用
// 所以在传入之前衡量是否需要 cloneDeep

function setTimeShiftToFunc(timeShift: string, functions?: ConvertFunctionModel[]) {
    if (!timeShift || !functions) { return; }
    const timeShiftFunc = functions.find(func => func.name === ConvertFunctionEnum.TIME_SHIFT);
    if (timeShiftFunc) {
        timeShiftFunc.defaultParams = [timeShift];
    }
}

function setOrderByToTarget(target: Target) {
    if (!target) { return; }
    const {groupBy: targetGroupBy, fields, orderBy} = target;
    if (
        orderBy &&
        targetGroupBy?.length > 0 &&
        fields?.length > 0
    ) {
        target.orderBy = getOrderByWithField(fields[0]);
    }
}
