import React from "react";
import {get} from "lodash";
import StoreManager from "$store/StoreManager";
import {Card, Checkbox, Form, Input, Select} from "antd";
import TitleWithTooltip from "$components/Base/TitleWithTooltip";

const LinkConfig: React.FC = props => {
    const {editChartStore} = StoreManager;

    const chart = editChartStore.getChart();

    const content = (
        <div>
            在 Series 的点击时跳到某个地址，可以把 Series 对应的值放在 URL 上
            <ul>
                <li><code>field</code>：对应的字段</li>
                <li><code>monitorItem</code>：对应的监控项</li>
                <li><code>metric</code>：对应的指标名</li>
                <li><code>tag_[tagKey]</code>：tagKey 对应的值，如 tag_ezone，指具体 ezone 的值</li>
            </ul>
            <div>例如: <code>/trace/soa/provider?appId=$&#123;monitorItem&#125;&ezone=$&#123;tag_ezone&#125;</code></div>
        </div>
    );

    return (
        <Card size="small" title={<TitleWithTooltip popover={true} title="Series 级别" tooltip={content}/>}>
            <Form layout="inline">
                <Form.Item>
                    <Select
                        onChange={(value) => editChartStore.mergeChartConfig({"seriesLink": {"target": value}})}
                        value={get(chart, "config.seriesLink.target", "_blank")}
                    >
                        <Select.Option value="_blank">新窗口打开</Select.Option>
                        <Select.Option value="_self">当前窗口打开</Select.Option>
                    </Select>
                </Form.Item>

                <Form.Item label="地址">
                    <Input
                        style={{width: "500px"}}
                        value={get(chart, "config.seriesLink.url", null)}
                        onChange={(value) => editChartStore.mergeChartConfig({"seriesLink": {"url": value.target.value}})}
                        placeholder="请输入需要跳转的URL"
                    />
                </Form.Item>

                <Form.Item>
                    <Checkbox
                        checked={get(chart, "config.seriesLink.time_range", false)}
                        onChange={(value) => editChartStore.mergeChartConfig({"seriesLink": {"time_range": value.target.checked}})}
                    >
                        附带看板时间范围
                    </Checkbox>
                </Form.Item>
            </Form>
        </Card>
    );
}

export default LinkConfig;
