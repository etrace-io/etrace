import React from "react";
import {ChartInfo} from "$models/ChartModel";
import {EditOutlined} from "@ant-design/icons";
import {Button, Descriptions, Space} from "antd";

const ChartInfoPanel: React.FC<{
    chart: ChartInfo;
    metricType?: string;
    onClose?(): void;
}> = props => {
    const {chart, metricType, onClose} = props;
    if (!chart) { return null; }

    const {id, globalId} = chart;

    return (
        <div className="chart-card__admin-info">
            <Space direction="vertical">
                {chart?.targets?.map((target, index) => (
                    <Descriptions key={index} bordered={true} size="small" column={1}>
                        <Descriptions.Item label="Chart Global ID">{globalId}</Descriptions.Item>
                        <Descriptions.Item label="Prefix">{target.prefix}</Descriptions.Item>
                        <Descriptions.Item label="PrefixVariate">{target.prefixVariate}</Descriptions.Item>
                        <Descriptions.Item label="Measurement">{target.measurement}</Descriptions.Item>
                        <Descriptions.Item label="MetricType">{metricType}</Descriptions.Item>
                        <Descriptions.Item label="Variates">[ {target.variate?.join(", ")} ]</Descriptions.Item>
                    </Descriptions>
                ))}
            </Space>

            <Space>
                <Button onClick={() => onClose && onClose()}>关闭</Button>
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

export default ChartInfoPanel;
