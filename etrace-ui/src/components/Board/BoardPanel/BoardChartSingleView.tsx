import React from "react";
import {Chart} from "$models/ChartModel";
import {EMonitorSection} from "$components/EMonitorLayout";
import BoardChart from "$components/Board/BoardPanel/BoardChart";

/**
 * 单一图表视图
 */
const BoardChartSingleView: React.FC<{
    chart: Chart;
}> = props => {
    const {chart} = props;

    return (
        <EMonitorSection>
            <BoardChart chart={chart}/>
        </EMonitorSection>
    );
};

export default BoardChartSingleView;
