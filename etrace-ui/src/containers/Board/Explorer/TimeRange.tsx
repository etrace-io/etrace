import {get} from "lodash";
import React from "react";
import StoreManager from "$store/StoreManager";
import {Card, Form, Input, Tooltip} from "antd";


const TimeRange: React.FC = props => {
    const {editChartStore} = StoreManager;

    const chart = editChartStore.getChart();

    return (
        <Card size="small">
            <Form layout="inline">
                <Form.Item label="时间范围">
                    <Tooltip title="m：分钟，h：小时，d：天" placement="topRight">
                        <Input
                            defaultValue={get(chart, "config.config.timeRange.relative", null)}
                            addonBefore="最近"
                            placeholder=""
                            onChange={e => editChartStore.mergeChartConfig({"config": {"timeRange": {"relative": e.target.value}}})}
                        />
                    </Tooltip>
                </Form.Item>
            </Form>
        </Card>
    );
};

export default TimeRange;
