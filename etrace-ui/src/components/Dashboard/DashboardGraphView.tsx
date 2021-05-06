import {Spin} from "antd";
import React from "react";
import * as ReactDOM from "react-dom";
import {autobind} from "core-decorators";
import {GraphConfig} from "./GraphView/defaultConfig";
import {DashboardGraph, GraphStatus} from "$models/DashboardModel";
import {debounce, difference, get, uniq} from "lodash";
// G6
import Grid from "./GraphView/plugins/grid";
import Command from "./GraphView/plugins/command";
import MiniMap from "./GraphView/plugins/minimap";
import Toolbar from "./GraphView/plugins/toolbar";
import registerShape from "./GraphView/shape";
import registerLayout from "./GraphView/layout";
import registerBehavior from "./GraphView/behavior";
import {reaction} from "mobx";
import {observer} from "mobx-react";
import G6 from "@antv/g6";
import {GraphOptions} from "@antv/g6/lib/interface/graph";
import {GraphStore} from "$store/GraphStore";
import StoreManager from "$store/StoreManager";
import {URLParamStore} from "$store/URLParamStore";
import {Theme} from "$constants/Theme";
// import AddItemByDrag from "./GraphView/plugins/addItemByDrag";

// const G6 = require("@antv/g6/src");

registerShape(G6);
registerLayout(G6);
registerBehavior(G6);

interface DashboardGraphViewProps {
    id: number;
    mode?: string;
    globalId?: string;
    grid?: boolean;
    selectedNodes?: any[];
    toolbar?: HTMLDivElement;
    miniMap?: HTMLDivElement;
    onSave?: (graph: any) => void;
    onItemSelected?: (items: any) => void;
    onNodeDeleted?: (targets: number[]) => void;
    onGraphLoaded?: (graph: DashboardGraph, g6Instance: any) => void; // Graph 数据加载完毕
    onGraphUpdate?: (graph: DashboardGraph, g6Instance: any) => void; // Graph 数据更新完毕
}

interface DashboardGraphViewStatus {
    emptyTips: string;
    isLoading: boolean;
}

@observer
export default class DashboardGraphView extends React.Component<DashboardGraphViewProps, DashboardGraphViewStatus> {
    graphContainer = React.createRef<HTMLDivElement>();
    currMode: string = "default";
    urlParamStore: URLParamStore = StoreManager.urlParamStore;
    graphStore: GraphStore = StoreManager.graphStore;
    disposer: any[];

    state = {
        emptyTips: "",
        isLoading: false,
        originGraph: null, // 根据 Graph ID 请求的 Graph
        currRelations: null, // 根据当前 Node 构建出的 Relations（根据 Group Node 进行修改）
    };

    graph: any;
    cmdPlugin: any;

    componentDidMount(): void {
        this.handlePageResize = debounce(this.handlePageResize.bind(this), 200);
        this.loadGraph();
        this.addWatcher();
    }

    componentWillReceiveProps(nextProps: Readonly<DashboardGraphViewProps>, nextContext: any): void {
        const diff = difference(nextProps.selectedNodes, this.props.selectedNodes);

        // Selected Node 发生变化
        if (diff.length !== 0) {
            this.graphStore.addNodes(diff);
            if (this.graph) {
                // this.graph.setMode(this.props.mode || "default");
                this.graph.emit("canvas:click");
                // if (this.cmdPlugin) {
                //     this.cmdPlugin.initPlugin(this.graph);
                // }
            }
        }
    }

    componentWillUnmount(): void {
        window.removeEventListener("resize", this.handlePageResize);
        if (this.graph) {
            this.graph.off("beforedelete", this.handleBeforeItemDelete);
            this.graph.off("afteritemselected", this.handleItemSelected);
            this.graph.off("save", this.handleSaveGraph);
            this.graph.destroy();
        }
        if (this.disposer) {
            this.disposer.forEach(disposer => disposer());
        }
        this.graphStore.destroy();
    }

    @autobind
    loadGraph() {
        const {id, mode, globalId} = this.props;
        this.graphStore.setMode(mode);
        if (id || globalId) {
            this.setState({
                isLoading: true
            });
            this.graphStore.init(id, globalId);
        }
        if (!id) {
            this.setState({emptyTips: mode === "edit" ? "请在左侧点击添加节点" : "无数据"});
            return;
        }
    }

    @autobind
    addWatcher() {
        this.disposer = [
            reaction(
                () => this.graphStore.status,
                (status) => {
                    this.setState({
                        emptyTips: "",
                        isLoading: status === GraphStatus.Loading
                    });
                    if (status === GraphStatus.Loaded) {
                        const data = this.graphStore.renderData;
                        data && this.initGraph(data);
                    }
                }),
            reaction(
                () => get(StoreManager.userStore.user, "userConfig.config.theme", Theme.Light),
                (theme) => {
                    registerShape(G6);
                    registerLayout(G6);
                    registerBehavior(G6);

                    if (this.graph) {
                        const autoPaint = this.graph.get("autoPaint");
                        this.graph.setAutoPaint(false);
                        // Node 重渲染
                        const nodes = this.graph.getNodes();
                        nodes.forEach(function (node: any) {
                            node.draw();
                        });
                        this.graph.paint(); // 让修改生效
                        this.graph.setAutoPaint(autoPaint);
                    }
                })
        ];
    }

    @autobind
    initGraph(data: any) {
        const { mode } = this.props;
        const graphContainer: HTMLElement = ReactDOM.findDOMNode(this.graphContainer.current) as HTMLElement;
        if (!graphContainer) {
            return;
        }

        const needInit = !this.graph;

        if (needInit) {
            const {toolbar, miniMap, grid} = this.props;
            const {width, height} = graphContainer.getBoundingClientRect();

            // 插件
            const plugins = [];

            // 命令插件
            this.cmdPlugin = new Command();
            plugins.push(this.cmdPlugin);

            // 工具栏
            if (toolbar) {
                plugins.push(new Toolbar({
                    container: ReactDOM.findDOMNode(toolbar) as HTMLElement
                }));
            }

            // 缩率图
            if (miniMap) {
                const mapDom = ReactDOM.findDOMNode(miniMap) as HTMLElement;
                const {width: mapWidth} = mapDom.getBoundingClientRect();
                plugins.push(new MiniMap({
                    container: mapDom,
                    size: [mapWidth, mapWidth / width * height]
                }));
            }

            // 网格
            if (grid) {
                plugins.push(new Grid({}));
            }

            const preset = {
                container: graphContainer,
                width,
                height,
                plugins,
            };
            const config: GraphOptions = Object.assign(preset, GraphConfig);

            this.graph = new G6.Graph(config);
            const {onGraphLoaded} = this.props;
            if (onGraphLoaded) {
                onGraphLoaded(this.graphStore.graph, this.graph);
            }

            this.graph.read(data);
            // this.graph.changeData(data);
            this.graph.updateLayout({needLayout: true});
            this.graph.fitView(5);
            this.graph.setMode(this.props.mode || "default");
            this.initEvent();
        } else {
            // Graph 更新
            const allNodesIns = this.graph.get("nodes"); // 所有节点实例
            const needLayout = allNodesIns.length !== data.nodes.length && mode === "view";

            this.graph.changeData(data);
            // autoPaint 指当图中元素更新，或视口变换时，是否自动重绘
            // 在批量操作节点时关闭，以提高性能，完成批量操作后再打开
            const autoPaint = this.graph.get("autoPaint");
            this.graph.setAutoPaint(false);
            // Node 重渲染
            const nodes = this.graph.getNodes();
            nodes.forEach(function (node: any) {
                node.draw();
            });
            this.graph.paint(); // 让修改生效
            this.graph.setAutoPaint(autoPaint);

            // 节点数量不一致的时候重新布局
            if (needLayout) {
                this.graph.updateLayout({needLayout: true});
            }
            if (mode === "edit") {
                this.graph.fitView(5);
            }

            // 通知更新
            const {onGraphUpdate} = this.props;
            if (onGraphUpdate) {
                onGraphUpdate(this.graphStore.graph, this.graph);
            }
        }
    }

    @autobind
    initEvent() {
        // Resize
        window.addEventListener("resize", this.handlePageResize);

        // delete item
        this.graph.on("beforedelete", this.handleBeforeItemDelete);
        this.graph.on("afterdelete", this.handleAfterItemDelete);
        // Select Items
        this.graph.on("afteritemselected", this.handleItemSelected);
        // save graph
        this.graph.on("save", this.handleSaveGraph);
    }

    @autobind
    handleBeforeItemDelete({items: targets}: { items: number[] }) {
        const {onNodeDeleted} = this.props;

        const result = []; // 需要删除的 Node ID

        targets.forEach(item => {
            if (this.graph) {
                const targetItem = this.graph.findById(item);
                if (targetItem && targetItem.get("type") === "node") {
                    const targetClass = targetItem.get("model").class;
                    const groupNode = this.graph.findAll("node", node => {
                        return node.get("model").class === targetClass;
                    });

                    result.push(...groupNode);
                }
            }
        });

        // List 中目标移除 Node
        const deletedNode = uniq(result.map(node => {
            return get(node.get("model"), "originId") || get(node.get("model"), "id");
        }));

        this.graphStore.removeNodes(deletedNode);

        if (onNodeDeleted) {
            onNodeDeleted(deletedNode);
        }
    }

    @autobind
    handleAfterItemDelete({edges}: {edges: any[]}) {
        this.graphStore.removeEdges(edges);
    }

    /**
     * Node 或 Edge 被选中
     */
    @autobind
    handleItemSelected(items: any[]) {
        const {onItemSelected} = this.props;
        if (onItemSelected) {
            if (items && items.length > 0) {
                const item = this.graph.findById(items[0]);
                onItemSelected(item);
            } else {
                onItemSelected(this.graph);
            }
        }
    }

    @autobind
    handlePageResize() {
        if (this.graph) {
            setTimeout(() => {
                const graphContainer: Element = ReactDOM.findDOMNode(this.graphContainer.current) as Element;
                const {width, height} = graphContainer.getBoundingClientRect();
                this.graph.changeSize(width, height);
            }, 0);
        }
    }

    @autobind
    handleSaveGraph({graph}: { graph: any }) {
        const {onSave} = this.props;
        if (onSave) {
            onSave(graph);
        }
    }

    render() {
        const {isLoading, emptyTips} = this.state;

        return (
            <div className="e-monitor-dashboard-graph-view__container">
                <div className={"graph-canvas" + (isLoading ? " loading" : "")} ref={this.graphContainer}/>
                <Spin className="graph-view__loading" spinning={isLoading} tip="Loading Graph..."/>
                {emptyTips && (<div className="graph-view__empty-tips">{emptyTips}</div>)}
            </div>
        );
    }
}
