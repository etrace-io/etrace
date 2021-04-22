import React from "react";
import {Tooltip} from "antd";
import {ChartTypeEnum} from "$models/ChartModel";

import {
    AreaChartOutlined,
    BarChartOutlined,
    DotChartOutlined,
    FileTextOutlined,
    LineChartOutlined,
    PieChartOutlined,
    RadarChartOutlined,
    TableOutlined
} from "@ant-design/icons";

const chartTypeIcon = {
    [ChartTypeEnum.Area]: <AreaChartOutlined />,
    [ChartTypeEnum.Column]: <BarChartOutlined />,
    [ChartTypeEnum.Pie]: <PieChartOutlined />,
    [ChartTypeEnum.Radar]: <RadarChartOutlined />,
    [ChartTypeEnum.Scatter]: <DotChartOutlined />,
    [ChartTypeEnum.Table]: <TableOutlined />,
    [ChartTypeEnum.Text]: <FileTextOutlined />,
    [ChartTypeEnum.Line]: <LineChartOutlined />,
};

const chartTypeTooltip = {
    [ChartTypeEnum.Area]: "是面积图哦",
    [ChartTypeEnum.Column]: "是柱状图哦",
    [ChartTypeEnum.Pie]: "是饼图哦",
    [ChartTypeEnum.Radar]: "是雷达图哦",
    [ChartTypeEnum.Scatter]: "是散点图哦",
    [ChartTypeEnum.Table]: "是表图哦",
    [ChartTypeEnum.Text]: "是文本哦",
    [ChartTypeEnum.Line]: "是线图哦",
};

const ChartTypeIcon: React.FC<{
    type: ChartTypeEnum;
}> = props => {
    const {type} = props;
    const icon = chartTypeIcon[type];

    if (!icon) { return null; }

    return (
        <Tooltip
            color="blue"
            placement="topLeft"
            mouseEnterDelay={0.8}
            mouseLeaveDelay={0.2}
            arrowPointAtCenter={true}
            title={chartTypeTooltip[type]}
        >
            <span>{icon}</span>
        </Tooltip>
    );
};

export default ChartTypeIcon;
