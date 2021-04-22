import React from "react";
import {observer} from "mobx-react";
import {Spin, Tag, Tooltip} from "antd";
import StoreManager from "../../store/StoreManager";
import {LoadingOutlined} from "@ant-design/icons/lib";
import {ChartStatusEnum} from "../../models/ChartModel";

interface MetricChartStatusProps {
    uniqueId: string;
    focus?: ChartStatusEnum;
    style?: React.CSSProperties;
}

const MetricChartStatus: React.FC<MetricChartStatusProps> = props => {
    const {chartStore} = StoreManager;

    const {uniqueId, focus, style} = props;
    const chartStatus = chartStore.chartStatusMap.get(uniqueId);

    if (!chartStatus) { return null; }

    if ((!focus || (focus && focus === ChartStatusEnum.Loading)) && chartStatus.status === ChartStatusEnum.Loading) {
        return (
            <Spin
                style={style}
                className="metric-chart-status__loading"
                spinning={true}
                indicator={<LoadingOutlined style={{fontSize: 20}}/>}
            />
        );
    }

    if ((!focus || (focus && focus === ChartStatusEnum.UnLimit)) && chartStatus.status === ChartStatusEnum.UnLimit) {
        return (
            <Tooltip
                placement="topLeft"
                color="blue"
                title="当前数据组合数超出图表限制数；可以通过右上角头像按钮，进入「设置」进行「数据条数」调整。"
            >
                <Tag color="#ff4d4f">组合超限</Tag>
            </Tooltip>
        );
    }
};

export default observer(MetricChartStatus);
