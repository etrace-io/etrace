import {get} from "lodash";
import {notification} from "antd";
import {NodeStore} from "./NodeStore";
import {URLParamStore} from "./URLParamStore";
import {action, computed, observable, reaction, toJS} from "mobx";
import {getGraphList, getGraphWithId} from "$services/DashboardService";
import {HttpVariate, MetricVariate, Variate, VariateType} from "$models/BoardModel";
import {
    DashboardGraph,
    DashboardGraphRelation,
    DashboardNodeChart,
    DashboardNodeQueryResult,
    GraphStatus
} from "$models/DashboardModel";

/**
 * Graph 只做为一个 Node 的容器
 * 获取的只是 Layout / Config
 */
export class GraphStore {
    urlParamStore: URLParamStore; // 用于获取参数以及设置参数
    nodeStore: NodeStore; // 用于获取参数以及设置参数

    graphId: number;
    nodesId: number[];
    @observable nodes: any[];
    @observable mode: string;
    @observable graph: DashboardGraph; // Graph 有任何变化表示重新获取了 Graph
    @observable status: GraphStatus = GraphStatus.Init;
    @observable originRelations: DashboardGraphRelation[] = [];

    // @observable nodesId: number[];

    @computed get relations(): DashboardGraphRelation[] {
        const isView = this.mode === "view";
        return isView ? this.replaceGroupNodeRelation(this.originRelations) : this.originRelations;
    }

    // 用于 G6 渲染的数据源
    @computed get renderData() {
        const nodes = toJS(this.nodes);
        const edges = this.relations;
        if (!nodes || !edges || this.status === GraphStatus.Loading) {
            return null;
        }

        return {
            nodes,
            edges
        };
    }

    constructor(urlParamStore: URLParamStore, nodeStore: NodeStore) {
        this.urlParamStore = urlParamStore;
        this.nodeStore = nodeStore;

        reaction(() => this.nodeStore.g6Nodes, (nodes) => {
            this.status = this.nodeStore.allNodeLoaded
                ? GraphStatus.Loaded
                : GraphStatus.Loading;
            this.nodes = toJS(nodes);
        });
    }

    @action
    public init(id: number, globalId?: string) {
        this.status = GraphStatus.Loading;
        if (globalId) {
            this.getGraphInfoByGlobalId(globalId);
        } else {
            this.getGraphInfoById(id);
        }
    }

    @action
    public reload() {
        this.graphId && this.init(this.graphId);
    }

    @action
    public destroy() {
        this.graph = null;
        this.graphId = null;
        this.mode = null;
        this.nodeStore.destroy();
        this.status = GraphStatus.Init;
    }

    public setMode(mode: string) {
        this.mode = mode;
    }

    @action
    public addNodes(nodes: number[]) {
        const targetNodes = [];
        nodes.forEach(id => {
            if (!this.nodesId) {
                this.nodesId = [];
            }
            if (this.nodesId.indexOf(id) === -1) {
                targetNodes.push(id);
            }
        });
        if (targetNodes.length > 0) {
            this.nodesId.push(...targetNodes);
            this.nodeStore.register(targetNodes);
        }
    }

    @action
    public removeNodes(nodes: number[]) {
        nodes.forEach(id => {
            const index = this.nodesId.findIndex(item => item === id);
            this.nodesId.splice(index, 1);
        });
        this.nodeStore.unregister(nodes);
    }

    @action
    public removeEdges(edges: any[]) {
        edges.forEach(edge => {
            const index = this.originRelations.findIndex(item => {
                return item.target === edge.target && item.source === edge.source;
            });

            if (index > -1) {
                this.originRelations.splice(index, 1);
            }
        });
    }

    public getConfig(key: string) {
        if (!this.graph) {
            return;
        }
        const config = this.graph.config || {};

        return config[key];
    }

    public getAllMetricTargets() {
        const charts: DashboardNodeChart[] = this.nodeStore.getAllCharts();
        const targets = {}; // name => model

        charts.forEach(chart => {
            chart.targets && chart.targets.forEach(target => {
                const variateNames: Array<string> = target.variate;
                if (variateNames) {
                    for (const name of variateNames) {
                        let models: Array<Variate> = targets[name];
                        if (!models) {
                            models = [];
                            models.push(new HttpVariate(name, name, ""));
                        }
                        let isExist = false;

                        for (let model of models) {
                            if (model.type === VariateType.METRIC) {
                                const temp: MetricVariate = model as MetricVariate;
                                if (temp.prefix === target.prefix &&
                                    temp.measurement === target.measurement &&
                                    temp.label === chart.title
                                ) {
                                    isExist = true;
                                    break;
                                }
                            }
                        }

                        if (!isExist) {
                            models.push(new MetricVariate(
                                chart.title,
                                name,
                                target.entity,
                                target.measurement,
                                [],
                                target.prefix)
                            );
                        }
                        targets[name] = models;
                    }
                }
            });
        });

        return targets;
    }

    @action
    private register(graph: DashboardGraph) {
        if (graph) {
            this.graph = graph;
            this.graphId = graph.id;
            this.nodesId = graph.nodeIds;
            this.originRelations = get(graph, "layout.edges", []);
            this.commitConfig(graph.config);
            this.registerNodes(graph.nodeIds);
        }
    }

    private commitConfig(config: any) {
        const urlParams = {
            refresh: this.urlParamStore.getValue("refresh") || get(config, "time.refresh", null),
            from: this.urlParamStore.getValue("from") || get(config, "time.from", null),
            to: this.urlParamStore.getValue("to") || get(config, "time.to", null)
        };
        // const variates = get(board, "config.variates", []);
        // variates.forEach(variate => {
        //     const value = this.urlParamStore.getValues(variate.name);
        //     urlParams[`${variate.name}`] = (value && value.length > 0) ? value : variate.current;
        // });
        this.urlParamStore.changeURLParams(urlParams, [], false, "replace");
    }

    @action
    private registerNodes(nodesId: number[]) {
        this.nodeStore.setMode(this.mode);
        this.nodeStore.register(nodesId);
    }

    /**
     * 根据 ID 获取 Graph
     * @param {number} id
     */
    private getGraphInfoById(id: number) {
        return getGraphWithId(id)
            .then(async (graph) => {
                this.register(graph);
            });
    }

    /**
     * 根据 GlobalID 获取 Graph
     * @param {number} globalId
     */
    private getGraphInfoByGlobalId(globalId: string) {
        const options = {status: "Active", globalId};

        return getGraphList(1, 24, options)
            .then(({results}) => {
                const graph = results[0];
                if (graph) {
                    this.register(graph);
                } else {
                    notification.error({
                        message: "无法找到对应 Global ID Node",
                        description: "建议使用「同步」功能，从其他环境同步配置到当当前环境",
                        duration: 5
                    });
                }
            });
    }

    private replaceGroupNodeRelation(originRelations: DashboardGraphRelation[]) {
        const nodes = this.nodeStore.nodes;
        const nodesId = Object.keys(nodes);

        const groupNodesInfo = {}; // key: Global ID, value: node is list array

        nodesId
            .map(id => nodes[id])
            .filter(n => {
                return Array.isArray(n) && n.length > 0 && `${n[0].id}`.indexOf("|") > -1;
            })
            .forEach((groupNodes: DashboardNodeQueryResult[]) => {
                const globalId = groupNodes[0].nodeConfig.globalId;
                groupNodesInfo[globalId] = groupNodes.map(groupNode => groupNode.id); // 需要替换的 id 列表
            });

        const relations = [];

        const groupNodesGlobalId = Object.keys(groupNodesInfo); // 所有需要替换的 Global ID

        // replace 那些
        originRelations.forEach(relation => {
            if (groupNodesGlobalId.indexOf(`${relation.target}`) > -1) {
                groupNodesInfo[relation.target].forEach(id => {
                    relations.push(Object.assign({}, relation, {target: id}));
                });
            } else if (groupNodesGlobalId.indexOf(`${relation.source}`) > -1) {
                groupNodesInfo[relation.source].forEach(id => {
                    relations.push(Object.assign({}, relation, {source: id}));
                });
            } else {
                relations.push(relation);
            }
        });

        return relations;
    }
}