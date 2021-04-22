import React from "react";
import {Drawer} from "antd";
import {uniqueId} from "../../utils/Util";
import {DashboardNodeChart} from "../../models/DashboardModel";
import ChartCard from "../../containers/Trace/ChartCard";

interface DashboardDrawerProps {
    dataSource: DashboardNodeChart[];
    visible: boolean;
    width?: number | string;
    onClose?: (e: React.MouseEvent<HTMLDivElement> | React.MouseEvent<HTMLButtonElement>) => void;
}

interface DashboardDrawerStatus {
    isLoading: boolean;
}

export default class DashboardViewDrawer extends React.Component<DashboardDrawerProps, DashboardDrawerStatus> {
    uniqueId: string = uniqueId();

    state = {
        isLoading: false,
    };

    render() {
        const {dataSource, width, visible, onClose} = this.props;
        return (
            <Drawer
                width={width || 560}
                onClose={onClose}
                visible={visible}
                closable={false}
            >
                {visible && dataSource && dataSource.map(chart => {
                    const uid = uniqueId();
                    // const canvasChart = <CanvasChart uniqueId={uid} chart={chart} height={280}/>;
                    const canvasChart = <ChartCard span={24} chart={chart} key={uid} uniqueId={uid}/>;
                    return (
                        <div key={chart.id}>{canvasChart}</div>
                    );
                })}
            </Drawer>
        );
    }
}
