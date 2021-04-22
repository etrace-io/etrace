import {Card, Divider, Layout, Tooltip} from "antd";
import React from "react";
import {autobind} from "core-decorators";
import DashboardGraphView from "../../components/Dashboard/DashboardGraphView";
import DashboardViewDrawer from "../../components/Dashboard/DashboardViewDrawer";
import {DashboardNodeChart, DashboardNodeQueryResult} from "../../models/DashboardModel";
import {findTimeShift, TIMESHIFT} from "../../models/TimePickerModel";
import TimePicker from "../../components/TimePicker/TimePicker";
import StoreManager from "../../store/StoreManager";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {Variate} from "../../models/BoardModel";
import {observer} from "mobx-react";
import {
    AlignCenterOutlined,
    FullscreenExitOutlined,
    FullscreenOutlined,
    ZoomInOutlined,
    ZoomOutOutlined
} from "@ant-design/icons/lib";

const Content = Layout.Content;

interface DashboardViewProps {
    match?: any;
    history?: any;
}

interface DashboardViewStatus {
    drawerVisible: boolean;
    drawerDataSource: DashboardNodeChart[];
    toolbarInstance: HTMLDivElement;
    variates: Variate[];
}

@observer
export default class DashboardView extends React.Component<DashboardViewProps, DashboardViewStatus> {
    toolbarContainer = React.createRef<HTMLDivElement>();
    urlParamStore = StoreManager.urlParamStore;
    graphStore = StoreManager.graphStore;

    state = {
        drawerVisible: false,
        drawerDataSource: null,
        toolbarInstance: null,
        variates: []
    };

    componentDidMount(): void {
        this.setState({
            toolbarInstance: this.toolbarContainer.current,
        });
    }

    getUrlSearchGlobalId() {
        const {location: {search}} = window;
        const searchParams = new URLSearchParams(search);
        const globalId = searchParams.get("globalId");
        return globalId || undefined;
    }

    @autobind
    handleCloseDrawer() {
        this.setState({drawerVisible: false});
    }

    @autobind
    handleNodeClick(item: any) {
        const model = item.get("model");
        if (model && model.nodeInfo) {
            const nodeInfo: DashboardNodeQueryResult = model.nodeInfo;
            const {nodeConfig, group} = nodeInfo;
            if (nodeConfig && nodeConfig.charts) {
                const charts = nodeConfig.charts;
                if (group) {
                    const tagFilters = Object.keys(group).map(key => ({
                        display: true,
                        key,
                        op: "=",
                        value: [group[key]],
                    }));
                    charts.forEach(chart => chart.targets.forEach(target => target.tagFilters = tagFilters));
                }

                charts.forEach(chart => {
                    chart.targets.forEach(target => {
                        // 由 ChartStore 接管时间控制
                        delete target.to;
                        delete target.from;
                    });
                });
                this.setState({drawerVisible: true, drawerDataSource: charts});
            }
            // const id = nodeInfo.parentId;
        }
    }

    @autobind
    handleGraphLoaded() {
        const variates = this.graphStore.getConfig("variates") || [];
        this.setState({variates});
    }

    render() {
        const {drawerVisible, drawerDataSource, toolbarInstance, variates} = this.state;
        const {match: {params}} = this.props;
        const globalId = this.getUrlSearchGlobalId();

        const toolbar = (
            <div className="view-page-toolbar" ref={this.toolbarContainer}>
                <GraphViewPageToolBar/>
            </div>
        );

        return (
            <Content className="e-monitor-content-sections with-footer flex e-monitor-dashboard-view-page">
                <Card className="e-monitor-content-section">
                    <span className="e-monitor-dashboard-title">{this.graphStore.graph && this.graphStore.graph.title}</span>
                    {variates.length > 0 && (<Divider style={{verticalAlign: "unset"}} type="vertical"/>)}
                    <MultiVariateSelect variates={variates}/>
                    <div className="view-page-toolbar__op-group">
                        <span>环比:<span style={{margin: "0 8px"}}>{findTimeShift(this.urlParamStore.getValue(TIMESHIFT)).valueLabel}</span></span>
                        <TimePicker hasTimeShift={true} hasTimeZoom={true}/>
                    </div>
                </Card>
                <Card
                    title={toolbar}
                    className="e-monitor-content-section view-page-graph take-rest-height card-body-take-rest-height"
                    bodyStyle={{padding: 1}}
                >
                    <DashboardGraphView
                        mode="view"
                        id={params.id}
                        globalId={globalId}
                        onItemSelected={this.handleNodeClick}
                        toolbar={toolbarInstance}
                        onGraphLoaded={this.handleGraphLoaded}
                    />
                    <DashboardViewDrawer
                        dataSource={drawerDataSource}
                        visible={drawerVisible}
                        onClose={this.handleCloseDrawer}
                    />
                </Card>
            </Content>
        );
    }
}

/* 顶部工具栏 */
interface GraphViewPageToolBarProps {
}

interface GraphViewPageToolBarStatus {
}

class GraphViewPageToolBar extends React.Component<GraphViewPageToolBarProps, GraphViewPageToolBarStatus> {
    static ToolBar = [
        [
            {icon: <ZoomInOutlined />, action: "zoomIn", tooltip: "放大"},
            {icon: <ZoomOutOutlined />, action: "zoomOut", tooltip: "缩小"},
            {icon: <AlignCenterOutlined />, action: "alignCenter", tooltip: "居中"},
            {icon: <FullscreenExitOutlined />, action: "resetZoom", tooltip: "重置缩放"},
            {icon: <FullscreenOutlined />, action: "autoFit", tooltip: "适应区域"},
        ],
        [
            {icon: "layout", action: "layout", tooltip: "整理布局"},
        ],
    ];

    render() {
        return (
            <div className="graph-editor__toolbar">
                {GraphViewPageToolBar.ToolBar.map((group: any, index: number) => (
                    <React.Fragment key={index}>
                        {group && group.map(({icon, action, tooltip}) => (
                            <Tooltip key={icon + tooltip} title={tooltip}><span data-command={action}>{icon}</span></Tooltip>
                        ))}
                        {index !== GraphViewPageToolBar.ToolBar.length - 1 && <Divider type="vertical"/>}
                    </React.Fragment>
                ))}
            </div>
        );
    }
}
