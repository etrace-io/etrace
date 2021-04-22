// NodeGroup
import {get} from "lodash";
import ChartJS from "chart.js";
import {isEmpty} from "../../utils/Util";
import {LegendValue} from "../Chart/ChartLegend";
import StoreManager from "../../store/StoreManager";
import {UnitModelEnum} from "../../models/UnitModel";
import {DataFormatter} from "../../utils/DataFormatter";
import * as LinDBService from "../../services/LinDBService";
import {getNodeConfigWithId, getNodeInfoWithConfig} from "../../services/DashboardService";
import {default as ChartEditConfig, getConfigValue} from "../../containers/Board/Explorer/ChartEditConfig";
import {getCountPS, getMetricDisplay, getTagKey, getTimeShiftInterval} from "../../utils/ChartDataConvert";
import {
    DashboardGraphRelation,
    DashboardNode,
    DashboardNodeQueryResult,
    DashboardNodeQueryResultOfMetric,
    RenderNodeData
} from "../../models/DashboardModel";

/**
 * 处理 GroupNode 查询返回的多个 Node 无 ID 的情况
 */
export function processGroupNode(nodes: DashboardNodeQueryResult[][], relations: DashboardGraphRelation[]) {
    const relationsResult: DashboardGraphRelation[] = relations ? [...relations] : [];
    const needDelete: string[] = [];

    nodes.forEach((node) => node.forEach((item, itemIdx) => {
        const groupNodeId = item.parentGlobalId;
        if (item.id === null) {
            item.id = `${groupNodeId}|${itemIdx}`;
            relationsResult.push(...processGroupNodeRelations(item, relations));
            if (needDelete.indexOf(groupNodeId) === -1) {
                needDelete.push(groupNodeId);
            }
        } else {
            item.id = groupNodeId;
        }
    }));

    return {
        nodes,
        relations: relationsResult.filter((r => {
            return r && (needDelete.indexOf(`${r.target}`) === -1 && needDelete.indexOf(`${r.source}`) === -1);
        }))
    };
}

/**
 * 处理 GroupNode Relations
 */
export function processGroupNodeRelations(item: DashboardNodeQueryResult, relations: DashboardGraphRelation[]) {
    const result = [];
    const nodeGlobalId = item.parentGlobalId;
    // 变更 Relation
    relations && relations.forEach(relation => {
        if (relation.target === nodeGlobalId) {
            result.push(Object.assign({}, relation, {target: item.id}));
        }
        if (relation.source === nodeGlobalId) {
            result.push(Object.assign({}, relation, {source: item.id}));
        }
    });
    return result;
}

/**
 * 请求 Node 配置后请求具体内容
 * @param {number} id
 * @param {boolean} needQueryResult 是否需要查询结果
 */
export async function queryNodeInfoWithId(id: number | string, needQueryResult: boolean = true) {
    const config = await getNodeConfigWithId(id);
    if (needQueryResult) {
        const builtConfig = buildTargetConfig(config);
        const queryResult = await getNodeInfoWithConfig(builtConfig);
        // 处理 Query Result 没有 Global ID 的情况
        return queryResult.map(res => {
            res.parentId = config.id + "";
            res.parentGlobalId = config.globalId;
            res.nodeConfig = config;
            return res;
        });
    } else {
        return config;
    }
}

/**
 * 处理 Node，在 View 模式下，将会对 Group Node 进行 Query
 */
export function processDataOfGroupNodes(nodes: DashboardNodeQueryResult[][]) {
    const result = [];
    for (let node, i = 0; node = nodes[i]; i++) {
        for (let item, j = 0; item = node[j]; j++) {
            const contents = getMetricsInfo(item.results, Object.keys(item.group || {}));
            result.push({
                id: `${item.id}`,
                title: item.title,
                contents,
                class: `${i}`,
                nodeInfo: item,
                metrics: item.results,
                status: {
                    publish: item.publish,
                    alert: item.alert,
                    change: item.change,
                },
            });
        }
    }
    return result;
}

/**
 * Edit 模式下，不会对 Group Node 进行请求，以及数据的处理
 */
export function processDataOfNodes(nodes: DashboardNode[]) {
    const result: RenderNodeData[] = [];
    nodes.forEach((node, index) => {
        result.push({
            id: `${node.globalId}`,
            originId: node.id,
            title: node.title,
            contents: [],
            nodeInfo: node,
            class: `${node.globalId}`,
        });
    });
    return result;
}

/**
 * 处理 Edge
 */
export function processDataOfEdges(relations: DashboardGraphRelation[]) {
    return relations && relations.map(relation => ({
        source: `${relation.from || relation.source}`,
        target: `${relation.to || relation.target}`,
    }));
}

export function buildTargetConfig(config: DashboardNode) {
    const {charts} = config;
    charts.forEach(chart => {
        const {targets} = chart;
        const newTargets = [];

        targets.forEach(target => {
            const newTarget = LinDBService.buildTargetWithoutOrderBy(target, chart);

            const {groupBy, fields, orderBy} = newTarget;

            newTarget.orderBy = (!orderBy && groupBy && groupBy.length > 0 && fields && fields.length > 0)
                ? orderKey(fields[0])
                : orderBy;

            newTargets.push(newTarget);
        });
        chart.targets = newTargets;
    });

    return config;
}

/**
 * 获取需要在节点上需要展示的信息
 * 如：Name、Total、Current 等
 */
export function getMetricsInfo(dataSource: DashboardNodeQueryResultOfMetric[], groupBy?: string[]) {
    const groupData = []; // 存放各 Group 下的数据

    dataSource.forEach(({metricShowName, chart: chart, result}) => {
        // Map 指标
        const name = metricShowName;
        const minInterval = get(result, "results.interval", 0);
        const chartConfig = chart.config;

        if (!result || minInterval <= 0) {
            return [{name}];
        }

        const {metricType, tagFilters, results: {startTime, interval, measurementName, groups}} = result;
        const functions = get(chart, "targets[0].functions");
        const tagKey = getTagKey(functions);
        const countPs = getCountPS(functions);
        const display = getMetricDisplay(functions);
        const timeShift = getTimeShiftInterval(functions);

        const nullValue = getConfigValue<string>(ChartEditConfig.display.series.nullAsZero, chartConfig);
        const decimals = getConfigValue<number>(ChartEditConfig.legend.value.decimals, chartConfig);
        const unit = getConfigValue(ChartEditConfig.axis.leftYAxis.unit, chartConfig) as UnitModelEnum;

        let valuesKeys = [LegendValue.MIN, LegendValue.MAX, LegendValue.AVG, LegendValue.CURRENT, LegendValue.TOTAL]
            .map(key => {
                const shouldDisplay = getConfigValue<boolean>(ChartEditConfig.legend.value[key], chartConfig, false);
                return shouldDisplay ? key : "";
            })
            .filter(Boolean);

        // 默认显示
        if (valuesKeys.length === 0) {
            // valuesKeys = [LegendValue.MAX, LegendValue.CURRENT];
            valuesKeys = [LegendValue.AVG, LegendValue.TOTAL];
        }

        const nullAsZero = nullValue ? nullValue === "null_as_zone" : true;

        if (!groups[0] || !display) {
            return [{name}];
        }

        // const {fields} = groups[0];
        groups.forEach( ({group, fields}, index) => {
            // 遍历一个指标下 Group 出来的不同数据
            !groupData[index] && (groupData[index] = {
                group: Object.keys(group).filter(k => groupBy.indexOf(k) === -1).map(k => `${k}: ${group[k]}`).join(" | "),
                valuesKeys,
                metric: []
            });

            const keys = Object.keys(fields);
            const key = keys[0];
            const metricData = {name};

            if (key) {
                const {values, ...chartLegendValue} = calcMetricData(fields[key], countPs, interval, nullAsZero);
                const legendValueWithUnit = valueWithUnit(chartLegendValue, unit, decimals, interval);
                Object.assign(
                    metricData,
                    legendValueWithUnit,
                    {
                        chart: generateTrendChartImageView(values, chartConfig),
                    }
                );
            }
            groupData[index].metric.push(metricData);
        });
    });

    return groupData;
}

export function generateTrendChartImageView(data: number[], chartConfig: any) {
    return new Promise((resolve, reject) => {
        const canvas = document.createElement("canvas");
        canvas.width = 100;
        canvas.height = 30;

        const nullValue = getConfigValue<string>(ChartEditConfig.display.series.nullAsZero, chartConfig);
        const stacked = getConfigValue<string>(ChartEditConfig.display.series.seriesStacking, chartConfig);
        const connected = nullValue === "connectNulls";

        const options = {
            responsive: false,
            animation: false,
            stacked,
            layout: {
                padding: {
                    left: 0,
                    right: 0,
                    top: 0,
                    bottom: 0
                }
            },
            legend: {
                display: false
            },
            scales: {
                xAxes: [{
                    display: false,
                }],
                yAxes: [{
                    display: false,
                }]
            },
            elements: {
                line: {
                    tension: 0, // disables bezier curves
                    borderWidth: 1
                },
                point: {
                    radius: 0
                },
                arc: {
                    borderWidth: 0
                }
            },
            hover: {
                animationDuration: 0 // duration of animations when hovering an item
            },
            responsiveAnimationDuration: 0, // animation duration after a resize
        };

        const config = Object.assign({}, chartConfig, {
            type: "line",
            options,
            data: {
                labels: data.map(_ => ""),
                datasets: [{
                    label: "",
                    data,
                    fill: false,
                    borderColor: "#1890ff",
                    spanGaps: connected,
                }]
            },
            plugins: [{
                afterRender: function (c: any) {
                    // Do anything you want
                    const base64 = c.toBase64Image();
                    resolve(base64);
                },
            }],
        });

        new ChartJS(canvas, config);
    });
}

function calcMetricData(data: number[], countPs: boolean, interval: number, nullAsZero: boolean) {
    let total = 0;
    let count = 0;
    let max = null;
    let min = null;
    let current = null;
    const values = [];

    data.forEach(value => {
        // 计算（不参与 null as zero 的计算）
        if (value !== null) {
            count++;
        }
        if (nullAsZero && value === null) {
            value = 0;
        }
        if (value && countPs) {
            value = value * 1000 / interval;
        }
        if (value) {
            value = Math.floor(value * 1000) / 1000;
        }
        // 计算（不参与 null as zero 的计算）
        if (value !== null) {
            total += value;
            current = value;
        }
        if (max === null || value > max) {
            max = value;
        }
        if (min === null || value < min) {
            min = value;
        }
        values.push(value);
    });

    return {
        values,
        total,
        max: max === null ? 0 : max,
        min: min === null ? 0 : min,
        avg: count > 0 ? total / count : 0,
        current: current === null ? 0 : current,
    };
}

function valueWithUnit(values: any, unit: UnitModelEnum, decimals: number, interval: number) {
    const result = {};

    Object.keys(values).forEach(key => {
        let value = values[key];
        value = +value.toFixed(decimals);
        if (interval && key === LegendValue.TOTAL) {
            value = value * interval / 1000;
        }
        result[key] = DataFormatter.formatterByUnit(unit, value, decimals);
    });

    return result;
}

function orderKey(field: string): string {
    if (isEmpty(field)) {
        return field;
    }
    if (field.indexOf(" as ") >= 0) {
        field = field.split("as")[0].trimRight();
    }
    const regex = /^(t_max|t_min|stddev|variance|t_sum|t_gauge|t_mean)\((\S|\s)*\)$/;
    if (field.match(regex)) {
        return StoreManager.orderByStore.getOrderBy(field);
    }
    const type = StoreManager.orderByStore.type;
    field = type + "(" + field + ")";
    return StoreManager.orderByStore.getOrderBy(field);
}
