import React from "react";
import {Chart} from "$models/ChartModel";
import {Button, Modal, Space} from "antd";
import {CHART_SINGLE_VIEW, METRIC_EDIT} from "$constants/Route";
import Metric from "$components/Metric/Metric";
import {CopyToClipboard} from "react-copy-to-clipboard";
import {EditOutlined, ShareAltOutlined} from "@ant-design/icons/lib";
import ChartMetricView from "$components/Chart/ChartMetricView/ChartMetricView";
import {messageHandler} from "$utils/message";
import {APP_BASE_URL} from "$constants/index";

interface ChartPreviewProps {
    chart: Chart;
    visible: boolean;
    onOk: any;
}

/**
 * 指标模态预览框
 */
const ChartPreview: React.FC<ChartPreviewProps> = props => {
    const {chart, onOk, visible} = props;

    const chartSingleViewURL = `${APP_BASE_URL}${CHART_SINGLE_VIEW}/${chart.id}`;

    const actions = <Space>
        <Button onClick={onOk}>关闭</Button>
        <CopyToClipboard text={chartSingleViewURL} onCopy={() => messageHandler("success", "链接已复制")}>
            <Button type="primary" icon={<ShareAltOutlined />}>分享</Button>
        </CopyToClipboard>
        <Button type="primary" icon={<EditOutlined />} href={`${METRIC_EDIT}/${chart.id}`} target="_blank">编辑</Button>
    </Space>;

    return (
        <Modal
            width="80%"
            closable={false}
            onOk={onOk}
            onCancel={onOk}
            bodyStyle={{padding: 0}}
            visible={visible}
            zIndex={100}
            footer={actions}
            destroyOnClose={true}
        >
            <Metric chart={chart} title={chart.title} />
            {chart.targets && (<ChartMetricView targets={chart.targets} />)}
        </Modal>
    );
};

export default ChartPreview;
