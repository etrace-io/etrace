import React from "react";
import {get, set} from "lodash";
import StoreManager from "$store/StoreManager";
import {Radio, Row, Space, Tooltip} from "antd";
import {Chart, ChartTypeEnum} from "$models/ChartModel";
import TimePicker from "$components/TimePicker/TimePicker";
import HistoryPopover, {HistoryType} from "$containers/Board/HistoryPopover";
import {
    AreaChartOutlined,
    BarChartOutlined,
    DotChartOutlined,
    FileTextOutlined,
    LineChartOutlined,
    PieChartOutlined,
    RadarChartOutlined,
    TableOutlined
} from "@ant-design/icons/lib";

const ChartTypeSelector: React.FC<{
    chart: Chart;
    // chartId: string;
}> = props => {
    const {chart} = props;
    const {editChartStore} = StoreManager;
    const chartType = get(chart, "config.type", ChartTypeEnum.Line);

    const handleTypeChange = (e: any) => {
        set(chart, "config.type", e.target.value);
        editChartStore.setChartChange(chart, true);
    };

    const applyHistory = (chartConfig) => {
        editChartStore.setChartChange(chartConfig, true);
    };

    return (
        <Row justify="space-between">
            <Radio.Group
                value={chartType}
                onChange={handleTypeChange}
                buttonStyle="solid"
            >
                <Tooltip placement="topLeft" title="线图">
                    <Radio.Button value={ChartTypeEnum.Line}><LineChartOutlined /></Radio.Button>
                </Tooltip>
                <Tooltip placement="top" title="面积图">
                    <Radio.Button value={ChartTypeEnum.Area}><AreaChartOutlined /></Radio.Button>
                </Tooltip>
                <Tooltip placement="top" title="饼图">
                    <Radio.Button value={ChartTypeEnum.Pie}><PieChartOutlined /></Radio.Button>
                </Tooltip>
                <Tooltip placement="top" title="柱状图">
                    <Radio.Button value={ChartTypeEnum.Column}><BarChartOutlined /></Radio.Button>
                </Tooltip>
                <Tooltip placement="top" title="散点图">
                    <Radio.Button value={ChartTypeEnum.Scatter}><DotChartOutlined /></Radio.Button>
                </Tooltip>
                <Tooltip placement="top" title="雷达图">
                    <Radio.Button value={ChartTypeEnum.Radar}><RadarChartOutlined /></Radio.Button>
                </Tooltip>
                <Tooltip placement="top" title="文本">
                    <Radio.Button value={ChartTypeEnum.Text}><FileTextOutlined /></Radio.Button>
                </Tooltip>
                <Tooltip placement="topRight" title="表图">
                    <Radio.Button value={ChartTypeEnum.Table}><TableOutlined /></Radio.Button>
                </Tooltip>
            </Radio.Group>

            <Space>
                <HistoryPopover type={HistoryType.CHART} id={chart?.id} applyFunction={applyHistory} />
                <TimePicker/>
            </Space>
        </Row>
    );
};

export default ChartTypeSelector;
