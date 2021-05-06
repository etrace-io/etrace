import React from "react";
import Moment from "react-moment";
import {observer} from "mobx-react";
import {Descriptions} from "antd";
import {Chart} from "$models/ChartModel";

const ChartEditLog: React.FC<{
    chart: Chart;
}> = props => {
    const {chart} = props;

    return chart && (
        <Descriptions bordered={true} column={3} size="small">
            <Descriptions.Item label="创建者">{chart.createdBy}</Descriptions.Item>

            <Descriptions.Item label="最后更新者">{chart.updatedBy}</Descriptions.Item>

            <Descriptions.Item label="更新时间">
                <Moment format="YYYY-MM-DD HH:mm:ss">{chart.updatedAt}</Moment>
            </Descriptions.Item>
        </Descriptions>
    );
};

export default observer(ChartEditLog);
