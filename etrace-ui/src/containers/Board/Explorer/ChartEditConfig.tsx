import {difference, get, set} from "lodash";
import {ChartTypeEnum} from "../../../models/ChartModel";
import StoreManager from "../../../store/StoreManager";
import {UnitModelEnum} from "../../../models/UnitModel";

const charConfigPrefix = "config.";
const allTypes = Object.keys(ChartTypeEnum).map(type => ChartTypeEnum[type]);

export type EditConfig<T = any> = {
    path?: string,                  // 配置路径
    defaultValue?: T,             // 默认值
    allowTypes?: ChartTypeEnum[],   // 各项设置支持的 Chart type
    relative?: { config: () => EditConfig | EditConfig[], filter: (v: any) => boolean }, // 相关联的配置项，函数返回 true 表示该项显示，返回 false 隐藏该项设置
    disable?: () => boolean,        // 是否禁用该项，函数返回 true 表示禁用，返回 false 表示不经用
};

export enum EditConfigType {
    Input = "input",                // 输入框
    Select = "select",              // 下拉框
    Number = "number",              // 数字输入框
    Switch = "switch",              // 开关
    CheckBox = "checkBox",          // 多选框
    AutoComplete = "autoComplete",  // 自动完成
    Custom = "custom",              // 自定义，则需要在 ChartEditItem 中指定 `customType`
    // 可以自行添加新类型
}

/**
 * path         string, 配置路径
 * defaultValue T，默认值
 * allowTypes   ChartTypeEnum[], 各项设置支持的 Chart type
 * relative     function, 相关联的配置项，函数返回 true 表示该项显示，返回 false 隐藏该项设置
 * disable      function, 是否禁用该项，函数返回 true 表示禁用，返回 false 表示不经用
 */
const ChartEditConfig = {
    // 坐标轴
    axis: {
        // 左 Y 轴
        leftYAxis: {
            allowTypes: except(ChartTypeEnum.Text),
            // 显示 Y 轴
            visible: {
                path: "config.yAxis.visible",
                defaultValue: true,
                allowTypes: allTypes,
            },
            // Y 轴单位
            unit: {
                path: "config.unit",
                defaultValue: "none",
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.leftYAxis.visible);
                },
            },
            // Y 轴刻度函数
            scale: {
                path: "config.scale",
                defaultValue: "linear",
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.leftYAxis.visible);
                },
            },
            // Y 轴标题
            title: {
                path: "config.yAxis.title.text",
                defaultValue: null,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.leftYAxis.visible);
                },
            },
            // Y 轴最小值
            min: {
                path: "config.yAxis.min",
                defaultValue: null,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.leftYAxis.visible);
                },
            },
            // Y 轴最大值
            max: {
                path: "config.yAxis.max",
                defaultValue: null,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.leftYAxis.visible);
                },
            },
            // 保留小数位
            decimals: {
                path: "config.yAxis.decimals",
                defaultValue: 2,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.leftYAxis.visible);
                },
            },
        },
        // 右 Y 轴
        rightYAxis: {
            allowTypes: except([ChartTypeEnum.Text, ChartTypeEnum.Pie, ChartTypeEnum.Radar, ChartTypeEnum.Table]),
            // 显示 Y 轴
            visible: {
                path: "config.rightyAxis.visible",
                defaultValue: false,
                allowTypes: allTypes,
            },
            // Y 轴单位
            unit: {
                path: "config.rightunit",
                defaultValue: "none",
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.rightYAxis.visible);
                },
            },
            // Y 轴刻度函数
            scale: {
                path: "config.rightscale",
                defaultValue: "linear",
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.rightYAxis.visible);
                },
            },
            // Y 轴标题
            title: {
                path: "config.rightyAxis.title.text",
                defaultValue: null,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.rightYAxis.visible);
                },
            },
            // Y 轴最小值
            min: {
                path: "config.rightyAxis.min",
                defaultValue: null,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.rightYAxis.visible);
                },
            },
            // Y 轴最大值
            max: {
                path: "config.rightyAxis.max",
                defaultValue: null,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.rightYAxis.visible);
                },
            },
            // 保留小数位
            decimals: {
                path: "config.rightyAxis.decimals",
                defaultValue: 2,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.rightYAxis.visible);
                },
            },
            series: {
                path: "config.rightyAxis.series",
                defaultValue: [],
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.axis.rightYAxis.visible);
                },
            }
        },
        // 阈值
        threshold: {
            allowTypes: allTypes,
        }
    },
    // 图例
    legend: {
        // 配置项
        config: {
            allowTypes: except(ChartTypeEnum.Text),
            // 显示
            display: {
                path: "config.legend.show",
                defaultValue: true,
                allowTypes: allTypes,
            },
            // 布局
            layout: {
                path: "config.legend.layout",
                defaultValue: "center",
                allowTypes: [],
                relative: {
                    config: () => [ChartEditConfig.legend.config.asTable, ChartEditConfig.legend.config.toRight],
                    filter: ([asTable, toRight]) => asTable !== true && toRight !== true
                },
                disable() {
                    return !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
            // 表格显示
            asTable: {
                path: "config.legend.asTable",
                defaultValue: false,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
            // 右边显示
            toRight: {
                path: "config.legend.toRight",
                defaultValue: false,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
            // 宽度
            width: {
                path: "config.legend.info.width",
                defaultValue: null,
                allowTypes: [],
                relative: {
                    config: () => ChartEditConfig.legend.config.toRight,
                    filter: toRight => toRight === true
                },
                disable() {
                    return !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
            // 最大显示条数
            maxItem: {
                path: "config.legend.maxItems",
                defaultValue: 50,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
            // 搜索框
            showSearch: {
                path: "config.legend.search",
                defaultValue: false,
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
        },
        value: {
            allowTypes: allTypes,
            path: "config.legend.info",
            // Total
            total: {
                path: "config.legend.info.total",
                allowTypes: allTypes,
                disable() {
                    return !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
            // Max
            max: {
                path: "config.legend.info.max",
                allowTypes: except(ChartTypeEnum.Pie),
                disable() {
                    return !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
            // Nin
            min: {
                path: "config.legend.info.min",
                allowTypes: except(ChartTypeEnum.Pie),
                disable() {
                    return !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
            // Avg
            avg: {
                path: "config.legend.info.avg",
                allowTypes: except(ChartTypeEnum.Pie),
                disable() {
                    return !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
            // Current
            current: {
                path: "config.legend.info.current",
                allowTypes: except(ChartTypeEnum.Pie),
                disable() {
                    return !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
            // 保留小数位
            decimals: {
                path: "config.legend.info.decimals",
                defaultValue: 2,
                allowTypes: allTypes,
                disable() {
                    const relative = [
                        ChartEditConfig.legend.value.total,
                        ChartEditConfig.legend.value.max,
                        ChartEditConfig.legend.value.min,
                        ChartEditConfig.legend.value.avg,
                        ChartEditConfig.legend.value.current,
                    ];

                    return !relative
                            .map(target => getConfigValue(target))
                            .some(item => item === true)
                        || !getConfigValue(ChartEditConfig.legend.config.display);
                },
            },
        }
    },
    // 显示
    display: {
        series: {
            allowTypes: except(ChartTypeEnum.Text),
            // 透明度
            fillOpacity: {
                path: "config.fillOpacity",
                allowTypes: [ChartTypeEnum.Area],
                defaultValue: 2,
            },
            // 线宽度
            lineWidth: {
                path: "config.plotOptions.series.lineWidth",
                allowTypes: [ChartTypeEnum.Line, ChartTypeEnum.Area, ChartTypeEnum.Radar],
                defaultValue: 1,
            },
            // 显示点
            showPoint: {
                path: "config.plotOptions.series.marker.enabled",
                allowTypes: [ChartTypeEnum.Line, ChartTypeEnum.Area],
                defaultValue: false,
            },
            // 点大小
            markerRadius: {
                path: "config.plotOptions.series.marker.radius",
                allowTypes: [ChartTypeEnum.Scatter, ChartTypeEnum.Radar],
                defaultValue: 2,
                relative: {
                    config: () => ChartEditConfig.display.series.showPoint,
                    filter: value => value === true
                },
            },
            // 点样式
            pointStyle: {
                path: "config.plotOptions.series.marker.pointStyle",
                allowTypes: [ChartTypeEnum.Scatter, ChartTypeEnum.Radar],
                defaultValue: "circle",
                relative: {
                    config: () => ChartEditConfig.display.series.showPoint,
                    filter: value => value === true
                }
            },
            // 空值处理
            nullAsZero: {
                path: "config.nullValue",
                defaultValue: "null_as_zone",
                allowTypes: allTypes,
            },
            // 堆叠
            seriesStacking: {
                path: "config.plotOptions.series.stacking",
                defaultValue: "",
                allowTypes: [ChartTypeEnum.Line, ChartTypeEnum.Area, ChartTypeEnum.Column, ChartTypeEnum.Scatter],
            },
            // 环比线样式
            dashStyle: {
                path: "config.dashStyle",
                defaultValue: "2,2",
                allowTypes: [ChartTypeEnum.Line, ChartTypeEnum.Area],
            },
        },
        tooltip: {
            allowTypes: except(ChartTypeEnum.Text),
            // 0 值显示
            showZero: {
                path: "config.tooltip.show.zero",
                defaultValue: false,
                allowTypes: allTypes,
            },
            // 排序
            sort: {
                path: "config.plotOptions.series.sort",
                defaultValue: "Decreasing",
                allowTypes: allTypes,
            },
        },
        title: {
            allowTypes: allTypes,
            // 显示聚合时间
            showInterval: {
                path: "config.showInterval",
                defaultValue: false,
                allowTypes: allTypes,
            },
        },
        text: {
            allowTypes: [ChartTypeEnum.Text],
            // 显示环比
            showTimeshift: {
                path: "config.textType.showTimeshift",
                defaultValue: false,
                allowTypes: allTypes,
            },
            // 环比精度
            timeshiftPrecision: {
                path: "config.textType.timeshiftPrecision",
                defaultValue: 2,
                allowTypes: [],
                relative: {
                    config: () => ChartEditConfig.display.text.showTimeshift,
                    filter: value => value === true
                }
            },
            // 单位
            valueUnit: {
                path: "config.textType.valueUnit",
                defaultValue: "none",
                allowTypes: allTypes,
            },
            // 数值精度
            valuePrecision: {
                path: "config.textType.valuePrecision",
                defaultValue: 2,
                allowTypes: [],
                relative: {
                    config: () => ChartEditConfig.display.text.valueUnit,
                    filter: value => value !== UnitModelEnum.None
                }
            },
            // 数值标题
            valueTitle: {
                path: "config.textType.valueTitle",
                defaultValue: "",
                allowTypes: allTypes,
            },
            // 前缀
            customPrefix: {
                path: "config.textType.customPrefix",
                defaultValue: "",
                allowTypes: allTypes,
            },
            // 尾缀
            customSuffix: {
                path: "config.textType.customSuffix",
                defaultValue: "",
                allowTypes: allTypes,
            },
        },
        table: {
            allowTypes: [ChartTypeEnum.Table],
            // 显示环比
            showTimeshift: {
                path: "config.tableType.showTimeshift",
                defaultValue: true,
                allowTypes: allTypes,
            },
        },
    },
    // 链接
    link: {
        series: {
            allowTypes: allTypes,
            path: "seriesLink.url",
            defaultValue: "",
        },
        linkTarget: {
            allowTypes: allTypes,
            path: "seriesLink.target",
            defaultValue: "_blank",
        },
        timeRange: {
            allowTypes: allTypes,
            path: "seriesLink.time_range",
            defaultValue: false,
        }
    },
    // 分析
    analyze: {
        path: "config.analyze",
        defaultValue: null,
        allowTypes: allTypes,
        first: {
            path: "config.analyze.first",
            defaultValue: null,
            allowTypes: allTypes,
        },
        target: {
            path: "config.analyze.first.target",
            defaultValue: null,
            allowTypes: allTypes,
        },
        advancedConfig: {
            excludeTags: {
                path: "config.analyze.first.analyzeConfig.excludeTags",
                defaultValue: [],
                allowTypes: allTypes,
            },
            includeTags: {
                path: "config.analyze.first.analyzeConfig.includeTags",
                defaultValue: [],
                allowTypes: allTypes,
            },
            /* @deprecated */
            extraGroupBy: {
                path: "config.analyze.first.analyzeConfig.extraGroupBy",
                defaultValue: "",
                allowTypes: allTypes,
            },
            /* @deprecated */
            filterField: {
                path: "config.analyze.first.analyzeConfig.filterField",
                defaultValue: "",
                allowTypes: allTypes,
            },
            /* @deprecated */
            filterTimeCount: {
                path: "config.analyze.first.analyzeConfig.filterTimeCount",
                defaultValue: false,
                allowTypes: allTypes,
            },
            /* @deprecated */
            filterFieldCount: {
                path: "config.analyze.first.analyzeConfig.filterFieldCount",
                defaultValue: false,
                allowTypes: allTypes,
            },
            /* @deprecated */
            distributionRatio: {
                path: "config.analyze.first.analyzeConfig.distributionRatio",
                defaultValue: 0.9,
                allowTypes: allTypes,
            },
            /* @deprecated */
            classifyTopRatio: {
                path: "config.analyze.first.analyzeConfig.classifyTopRatio",
                defaultValue: 0.7,
                allowTypes: allTypes,
            },
            /* @deprecated */
            classifyCountRatio: {
                path: "config.analyze.first.analyzeConfig.classifyCountRatio",
                defaultValue: 0.7,
                allowTypes: allTypes,
            },
            /* @deprecated */
            rootCaseGroups: {
                path: "config.analyze.first.analyzeConfig.rootCaseGroups",
                defaultValue: 5,
                allowTypes: allTypes,
            },
        }
    }
};

function except(types: ChartTypeEnum | ChartTypeEnum[]): ChartTypeEnum[] {
    const target = Array.isArray(types) ? types : [types];
    return difference(allTypes, target);
}

/**
 * 针对对应 Config，绑定 value 和 onChange
 * @param {EditConfig} config 配置项
 * @param {T} defaultValue 默认值，为空则设置为配置项的 defaultValue
 * @param {string} type 当前配置项的表单元素类型
 * @param {boolean} forceLoadData 是否需要强制更新 Chart
 */
export function bindConfigValue<T>(config: EditConfig, defaultValue?: T, type?: string, forceLoadData?: boolean) {
    if (!config) {
        return defaultValue;
    }

    const {path, defaultValue: configDefaultValue} = config;
    if (!defaultValue) {
        defaultValue = configDefaultValue;
    }
    const value = get(StoreManager.editChartStore.getChart(), charConfigPrefix + path, defaultValue);

    const onChange = (v) => {
        let targetValue;

        switch (type) {
            case EditConfigType.CheckBox:
                targetValue = v.target.checked;
                break;
            case EditConfigType.Input:
                targetValue = v.target.value;
                break;
            case EditConfigType.Select:
            case EditConfigType.Number:
            case EditConfigType.AutoComplete:
            case EditConfigType.Switch:
            default:
                targetValue = v;
                break;
        }

        // 合并到 config store
        StoreManager.editChartStore.mergeChartConfig(set({}, path, targetValue), forceLoadData);
    };

    let valuePropName;

    switch (type) {
        case EditConfigType.CheckBox:
        case EditConfigType.Switch:
            valuePropName = "checked";
            break;
        case EditConfigType.Input:
        case EditConfigType.Select:
        case EditConfigType.Number:
        case EditConfigType.AutoComplete:
        default:
            valuePropName = "value";
            break;
    }

    return {
        [valuePropName]: value,
        onChange,
    };
}

/**
 * 获取当前 Config 对应的值（如果 ChartObj 上没有，则自动获取配置中的 defaultValue）
 * @param {EditConfig | any} config 相关 Config 配置
 * @param chartConfig 额外获取来源，一般为 `chartObj.config`
 * @param {T} defaultValue 默认值，为空则获取配置中的 defaultValue
 * @return {T} 返回对应配置的值
 */
export function getConfigValue<T>(config: EditConfig<T>, chartConfig?: any, defaultValue?: T): T {
    if (!config) {
        return defaultValue;
    }

    const {path, defaultValue: configDefaultValue} = config;

    if (defaultValue === undefined) {
        defaultValue = configDefaultValue;
    }

    const currChart = chartConfig || StoreManager.editChartStore.getChart();
    const targetPath = chartConfig ? path : charConfigPrefix + path;

    return get(currChart, targetPath, defaultValue) as T;
}

export type ChartConfigType = typeof ChartEditConfig;

export default ChartEditConfig;
