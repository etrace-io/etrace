import React from "react";
import {Panel} from "$models/BoardModel";
import StoreManager from "$store/StoreManager";
import {EMonitorGallery} from "$components/EMonitorLayout";
import {Empty} from "antd";
import BoardChart from "$components/Board/BoardPanel/BoardChart";

/**
 * 看板版面所有行
 */
const BoardRow: React.FC<{
    panels: Panel[];
    height?: number;
}> = props => {
    const {panels, height} = props;
    const {boardStore} = StoreManager;

    const charts = panels.map((chartInfo, colNum) => {
        const chart = boardStore.getChart(chartInfo.chartId, chartInfo.globalId);

        if (
            !chart ||
            (chart.status && chart.status === "Inactive")
        ) {
            return null;
        }

        return (
            <EMonitorGallery.Item key={colNum} span={chartInfo.span}>
                <BoardChart chart={chart} globalId={chartInfo.globalId} height={height}/>
            </EMonitorGallery.Item>
        );
    }).filter(Boolean);

    return charts.length !== 0
        ? <EMonitorGallery>{charts}</EMonitorGallery>
        : <Empty description="暂无指标"/>;
};

export default BoardRow;
