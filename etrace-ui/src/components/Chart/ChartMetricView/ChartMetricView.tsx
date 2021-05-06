import React from "react";
import {Divider, Form, Tag} from "antd";
import {Target} from "$models/ChartModel";
import {getPrefixCls} from "$utils/Theme";

import "./ChartMetricView.less";

const MetricViewClass = getPrefixCls("metric-view");

interface ChartMetricViewProps {
    targets: Target[];
}

const ChartMetricView: React.FC<ChartMetricViewProps> = props => {
    const {targets} = props;

    return (
        <div className={MetricViewClass}>
            {targets.map((target, index) => (
                <React.Fragment key={index}>
                    <TargetInfo target={target}/>
                    {index !== targets.length - 1 && <Divider dashed={true} style={{margin: "10px 0"}}/>}
                </React.Fragment>
            ))}
        </div>
    );
};

interface TargetInfoProps {
    target: Target;
}

const TargetInfo: React.FC<TargetInfoProps> = props => {
    const {target} = props;

    return (
        <Form layout="inline" className="metric-view__item">
            {target.prefix && (
                <Form.Item label="监控项">
                    <Tag color="blue">{target.prefix}</Tag>
                </Form.Item>
            )}

            <Form.Item label="指标名">
                <Tag color="blue">{target.measurement}</Tag>
            </Form.Item>

            <Form.Item label="字段">
                <Tag color="blue">{target.fields}</Tag>
            </Form.Item>

            {target.groupBy && target.groupBy.length > 0 && (
                <Form.Item label="Group By">
                    <Tag color="blue">{target.groupBy}</Tag>
                </Form.Item>
            )}

            {target.tagFilters && target.tagFilters.length > 0 && (
                <Form.Item label="过滤条件">
                    {target.tagFilters.map((v, i) => (
                        <Tag key={i} color="blue">{`${v.key} ${v.op} ${v.value}`}</Tag>
                    ))}
                </Form.Item>
            )}

            {target.variate && target.variate.length > 0 && (
                <Form.Item label="变量">
                    {target.variate.map(item => (
                        <Tag key={item} color="blue">{item}</Tag>
                    ))}
                </Form.Item>
            )}

            {target.functions && target.functions.length > 0 && (
                <Form.Item label="函数">
                    {target.functions.map((modal, idx) => modal.params && (
                        <Tag key={idx} color="blue">
                            {`${modal.name}(${modal.params.map((p, i) => !p.display && modal.defaultParams[i])})`}
                        </Tag>
                    ))}
                </Form.Item>
            )}
        </Form>
    );
};

export default ChartMetricView;
