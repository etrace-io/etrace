import React from "react";
import {Chart} from "$models/ChartModel";
import StoreManager from "$store/StoreManager";
import {FULL_SCREEN_CHART} from "$constants/Route";
import {Button, Popover, Space} from "antd";
import IconFont from "$components/Base/IconFont";
import {Link} from "react-router-dom";
import Metric, {MetricShortcut} from "$components/Metric/Metric";
import MetaLink from "$components/Metric/MetaLink";

/**
 * 看板页面所有图表
 */
const BoardChart: React.FC<{
    chart: Chart;
    globalId?: string;
    height?: number;
}> = props => {
    const {chart, height} = props;
    const {boardStore} = StoreManager;

    const fullViewChart = () => {
        boardStore.setChart(chart);
        StoreManager.urlParamStore.changeURLParams({[FULL_SCREEN_CHART]: chart.id});
    };

    const exitFullViewChart = () => {
        boardStore.setChart(null);
        StoreManager.urlParamStore.changeURLParams({}, [FULL_SCREEN_CHART]);
    };

    const popoverContent = (
        <Space>
            <Button
                type="dashed"
                size="small"
                onClick={fullViewChart}
            >
                <span>
                    查看 <span><IconFont type="icon-jianpan"/> <kbd>v</kbd></span>
                </span>
            </Button>

            <Button type="dashed" size="small" style={{lineHeight: "24px"}}>
                <Link
                    type="primary"
                    to={`/board/explorer/edit/${boardStore.getRightChartId(chart.id, chart.globalId)}`}
                    target="_blank"
                >
                    <span>编辑</span>
                </Link>
            </Button>
        </Space>
    );

    const chartShortcuts: MetricShortcut[] = [
        {
            keys: ["v"],
            onMatch: exitFullViewChart
        },
    ];

    // return (
    //     <EMonitorChart
    //         chart={chart}
    //     />
    // );

    return (
        <Metric
            title={(
                <Popover content={popoverContent} trigger="hover">
                    <span>{chart.title}</span>
                </Popover>
            )}
            chart={chart}
            height={height}
            extraLinks={<MetaLink targets={chart.targets}/>}
            shortcuts={chartShortcuts}
        />
    );
};

export default BoardChart;
