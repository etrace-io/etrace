import ChartJS from "chart.js";
import {isEmpty} from "$utils/Util";
import {cloneDeep, get} from "lodash";
import StoreManager from "./StoreManager";
import {URLParamStore} from "./URLParamStore";
import {UnitModelEnum} from "$models/UnitModel";
import * as LinDBService from "../services/LinDBService";
import {LegendValue} from "$components/Chart/ChartLegend";
import {action, computed, extendObservable, observable, reaction} from "mobx";
import {getNodeConfigWithId, getNodeInfoWithConfig} from "$services/DashboardService";
import {default as ChartEditConfig, getConfigValue} from "../containers/Board/Explorer/ChartEditConfig";
import {getCountPS, getMetricDisplay} from "$utils/ChartDataConvert";
import {
    DashboardNode,
    DashboardNodeQueryResult,
    DashboardNodeQueryResultOfMetric,
    NodeStatus
} from "$models/DashboardModel";
import {DataFormatter} from "$utils/DataFormatter";

export class NodeStore {
    urlParamStore: URLParamStore; // 用于获取参数、设置参数以及监听响应

    mode: string;
    disposer: any[] = [];
    nodeConfigCache: { [k in string]: DashboardNode } = {};
    @observable nodesId: number[] = [];
    @observable nodes: { [k in string]: DashboardNodeQueryResult[] | DashboardNode } = {};
    // key 为真实 id，不是 globalId，value 为经过查询的 Node 结果：
    // 如果需要 query，则 value 为数组，代表当前节点的查询结果（如 GroupNode 查询出来有多个节点）；
    // 如果不需要 query，则 value 就是对象，就只是一个展示节点（即编辑模式下）
    @observable nodesStatus: { [k in string]: NodeStatus } = {}; // key 为真实 id

    constructor(urlParamStore: URLParamStore) {
        this.urlParamStore = urlParamStore;
    }

    @computed get allNodeLoaded() {
        return this.nodesId
            .map(id => this.nodesStatus[id])
            .every(status => status === NodeStatus.Loaded);
    }

    /* 用于 G6 渲染的 Node 结构 */
    @computed get g6Nodes()  {
        const nodesId = this.nodesId;
        if (!nodesId || nodesId.length === 0) {
            return [];
        }

        if (!this.allNodeLoaded) {
            return [];
        }
        const nodes = [];
        if (this.isView) {
            nodesId.forEach((id, index) => {
                const nodeChildren = this.nodes[id];
                if (Array.isArray(nodeChildren)) {
                    // eslint-disable-next-line no-cond-assign
                    for (let item, j = 0; item = nodeChildren[j]; j++) {
                        const contents = this.getMetricsInfo(
                            item.results,
                            this.nodeConfigCache[id],
                            Object.keys(item.group || {})
                        );
                        nodes.push({
                            id: `${item.id}`,
                            title: item.title,
                            contents,
                            class: `${index}`,
                            nodeInfo: item,
                            metrics: item.results,
                            status: {
                                publish: item.publish,
                                alert: item.alert,
                                change: item.change,
                            },
                        });
                    }
                } else {
                    // 空节点处理
                    nodes.push({
                        id: `${nodeChildren.globalId}`,
                        originId: nodeChildren.id,
                        title: nodeChildren.title,
                        contents: [],
                        nodeInfo: nodeChildren,
                        class: `${index}`,
                    });
                }
            });
        } else {
            nodesId.forEach((id, index) => {
                const node = this.nodes[id] as DashboardNode;
                nodes.push({
                    id: `${node.globalId}`,
                    originId: node.id,
                    title: node.title,
                    contents: [],
                    nodeInfo: node,
                    class: `${node.globalId}`,
                });
            });
        }
        return nodes;
    }

    @computed get isView(): boolean {
        return this.mode === "view";
    }

    public setMode(mode: string) {
        this.mode = mode;
    }

    @action
    public register(ids: number[], replace: boolean = false) {
        if (replace) {
            this.nodesId = ids;
        } else {
            this.nodesId.push(...ids);
        }
        const needQueryResult = this.isView;

        if (!ids || ids.length === 0) {
            return;
        }

        if (this.disposer.length === 0) {
            this.addWatcher();
        }

        ids.forEach(async id => {
            // 设置 status
            if (!this.nodesStatus[id]) {
                extendObservable(this.nodesStatus, {[id]: NodeStatus.Loading});
            } else {
                this.nodesStatus[id] = NodeStatus.Loading;
            }

            // 获取并缓存节点 config
            if (!this.nodeConfigCache[id]) {
                this.nodeConfigCache[id] = await getNodeConfigWithId(id);
            }

            const config = this.nodeConfigCache[id];
            let result;

            // 根据是否查询结果判断获取的 config（空指标节点也不查）
            if (needQueryResult && config.charts) {
                const queryResult = await getNodeInfoWithConfig(this.buildTargetConfig(config));
                queryResult.map((res, index) => {
                    res.id = res.id
                        ? config.globalId
                        : `${config.globalId}|${index}`;
                    // res.parentId = `${config.id}`;
                    // res.parentGlobalId = config.globalId;
                    res.nodeConfig = config; // 用于点击时候获取对应的 Node Config
                    return res;
                });
                result = queryResult;
            } else {
                result = config;
            }

            if (!this.nodes[id]) {
                extendObservable(this.nodes, {[id]: result});
            } else {
                this.nodes[id] = result;
            }
            this.nodesStatus[id] = NodeStatus.Loaded;
        });
    }

    public unregister(ids: number[]) {
        ids.forEach(id => {
            const index = this.nodesId.findIndex(item => item === id);
            this.nodesId.splice(index, 1);

            delete this.nodes[id];
            delete this.nodesStatus[id];
            delete this.nodeConfigCache[id];
        });
    }

    public destroy() {
        this.nodesId = [];
        this.nodes = {};
        this.nodesStatus = {};
        this.nodeConfigCache = {};
        this.disposer.forEach(disposer => disposer());
        this.disposer = [];
    }

    public reloadNodes() {
        if (this.nodesId && this.nodesId.length > 0) {
            // 判断是否需要刷新
            setTimeout(() => this.register(this.nodesId, true), 0);
        }
    }

    /**
     * 获取所有 Node 下的所有 chart
     */
    public getAllCharts() {
        const charts = [];
        this.nodesId.forEach(id => {
            const node = this.nodeConfigCache[id];
            charts.push(...node.charts);
        });
        return charts;
    }

    private addWatcher() {
        this.disposer = [
            reaction(
                () => [this.urlParamStore.changed, this.urlParamStore.forceChanged],
                () => {
                    this.reloadNodes();
                }
            )
        ];
    }

    private buildTargetConfig(config: DashboardNode) {
        config = cloneDeep(config);
        const {charts} = config;
        charts.forEach(chart => {
            const {targets} = chart;
            const newTargets = [];

            targets.forEach(target => {
                // 添加变量、时间等
                const newTarget = LinDBService.buildTargetWithoutOrderBy(target, chart, true);

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
    private getMetricsInfo(dataSource: DashboardNodeQueryResultOfMetric[], nodeConfig: any, groupBy?: string[]) {
        const groupData = []; // 存放各 Group 下的数据

        dataSource.forEach(({metricShowName, chart, result}) => {
            // Map 指标
            const alias = get(nodeConfig, `config.charts[${chart.globalId}].alias`); // 别名
            const name = alias || metricShowName;
            const minInterval = get(result, "results.interval", 0);
            const chartConfig = chart.config;

            if (!result || minInterval <= 0) {
                return [{name}];
            }

            const {results: {interval, groups}} = result;
            // const {metricType, tagFilters, results: {startTime, interval, measurementName, groups}} = result;
            const functions = get(chart, "targets[0].functions");
            // const tagKey = getTagKey(functions);
            const countPs = getCountPS(functions);
            const display = getMetricDisplay(functions);
            // const timeShift = getTimeShiftInterval(functions);

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
                valuesKeys = [LegendValue.MAX, LegendValue.CURRENT];
            }

            const nullAsZero = nullValue ? nullValue === "null_as_zone" : true;

            if (!groups[0] || !display) {
                return [{name}];
            }

            // const {fields} = groups[0];
            groups.forEach(({group, fields}) => {
                // 遍历一个指标下 Group 出来的不同数据
                const groupTitle = Object.keys(group).filter(k => groupBy.indexOf(k) === -1).map(k => `${k}: ${group[k]}`).join(" | ");
                const itemIndex = groupData.findIndex(item => item.group === groupTitle);
                const index = itemIndex === -1 ? groupData.length : itemIndex;

                !groupData[index] && (groupData[index] = {
                    group: groupTitle,
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
                            chart: this.generateTrendChartImageView(values, chartConfig),
                        }
                    );
                }
                groupData[index].metric.push(metricData);
            });
        });

        return groupData;
    }

    /**
     * 根据查询结果构趋势图表
     */
    private generateTrendChartImageView(data: number[], chartConfig: any) {
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
