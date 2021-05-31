import React from "react";
import {ChartInfo} from "$models/ChartModel";
import {Button, Descriptions, Space} from "antd";
import {EditOutlined} from "@ant-design/icons";

const EditableChartOverlay: React.FC<{
    chart: ChartInfo;
    visible?: boolean;
    metricType?: string;
    onClose?(): void;
}> = props => {
    const {chart, metricType, onClose} = props;
    if (!chart) { return null; }

    const {id, globalId} = chart;

    return (
        <div className="editable-chart__admin-info">
            {chart?.targets?.map((target, index) => (
                <Descriptions key={index} bordered={true} size="small" column={1}>
                    <Descriptions.Item label="ChartName">{globalId}</Descriptions.Item>
                    <Descriptions.Item label="Prefix">{target.prefix}</Descriptions.Item>
                    <Descriptions.Item label="Measurement">{target.measurement}</Descriptions.Item>
                    <Descriptions.Item label="MetricType">{metricType}</Descriptions.Item>
                </Descriptions>
            ))}

            <Space>
                <Button onClick={() => onClose && onClose()}>返回</Button>
                <Button
                    href={`/board/explorer/edit/${id}`}
                    target="_blank"
                    type="primary"
                    icon={<EditOutlined />}
                >
                    编辑指标
                </Button>
            </Space>
        </div>
    );
};

export default EditableChartOverlay;
