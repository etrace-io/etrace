import React from "react";
import {Button, Popover, Space, Tooltip} from "antd";
import {QuestionOutlined} from "@ant-design/icons";
import ReactMarkdown from "react-markdown/with-html";
import {ChartInfo, ChartTypeEnum} from "$models/ChartModel";
import ChartTypeIcon from "$components/EMonitorChart/Chart/ChartComponent/ChartTypeIcon";

const ChartCardHeader: React.FC<{
    chart: ChartInfo;
    title: React.ReactNode;
    prefix?: React.ReactNode;
}> = props => {
    const {chart, prefix, title} = props;

    if (!chart) { return null; }

    const type = chart.config?.type ?? ChartTypeEnum.Line;
    const desc = chart.description;

    const suffix = desc ? (
        <Popover
            key="desc"
            placement="bottom"
            arrowPointAtCenter={true}
            overlayStyle={{maxWidth: 500}}
            content={<ReactMarkdown className="e-monitor-markdown" source={desc} escapeHtml={false}/>}
        >
            <Button className="chart-desc-btn" shape="circle" size="small" icon={<QuestionOutlined />}/>
        </Popover>
    ) : null;

    return (
        <Space className="chart-card-header" align="center">
            {prefix}
            <ChartTypeIcon type={type} />
            <span>
                {typeof title === "string" && title.length > 5
                    ? <Tooltip title={title} mouseEnterDelay={0.6} placement="topLeft" color="blue">
                        <span>{title}</span>
                    </Tooltip>
                    : title
                }
            </span>
            {suffix}
        </Space>
    );
};

export default React.memo(ChartCardHeader);
