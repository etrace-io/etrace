import {get, set} from "lodash";
import {UserKit} from "$utils/Util";
import {EditOutlined} from "@ant-design/icons";
import React, {useEffect, useState} from "react";
import {FrownOutlined} from "@ant-design/icons/lib";
import ChartCard from "$containers/Trace/ChartCard";
import * as ChartService from "$services/ChartService";
import ChartEditConfig from "$containers/Board/Explorer/ChartEditConfig";
import {Button, Card, Col, Descriptions, Result, Space, Spin} from "antd";

import "./EditableChart.less";
import useUser from "$hooks/useUser";

interface EditableChartProps {
    span?: number;
    globalId: string;
    metricType?: string;
    prefixKey?: string;
    awaitLoad?: boolean;
}

const EditableChart: React.FC<EditableChartProps> = props => {
    const {globalId, metricType, prefixKey, span = 8, awaitLoad} = props;

    const [result, setResult] = useState<any>();
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [showEdit, setShowEdit] = useState<boolean>(false);

    // const {chart, isLoading} = useChart({globalId});
    const user = useUser();

    useEffect(() => {
        setIsLoading(true);
        ChartService
            .searchByGroup({
                status: "Active",
                globalId,
            })
            .then((charts: any) => {
                setResult(handleResult(get(charts, "results", []).length > 0
                    ? charts.results.filter(chart => chart.globalId === globalId)[0]
                    : null));
                setIsLoading(false);
            });

    }, [globalId]);

    const handleResult = (tempResult: any) => {
        if (tempResult && Array.isArray(tempResult.targets)) {
            tempResult.targets.forEach(target => {
                target.metricType = metricType;
                target.prefixVariate = prefixKey;
            });
        }
        // 针对 Analyze Target 设置
        if (tempResult && get(tempResult.config, ChartEditConfig.analyze.path)) {
            set(tempResult.config, `${ChartEditConfig.analyze.target.path}.metricType`, this.props.metricType);
            set(tempResult.config, `${ChartEditConfig.analyze.target.path}.prefixVariate`, this.props.prefixKey);
        }
        return tempResult;
    };

    const showEditButton: boolean = UserKit.isAdmin(user);

    if (!result) {
        return (
            <Col span={span || 8}>
                <Card className="editable-chart__not-found">
                    {isLoading ? <Spin tip="配置加载中"/> : (
                        <Result
                            icon={<FrownOutlined />}
                            title="未找到"
                            subTitle={<>名为「<code>{globalId}</code>」的指标。</>}
                            extra={<Button type="primary" href={`/board/explorer?uniqueId=${globalId}`} target="_blank">新增该指标</Button>}
                        />
                    )}
                </Card>
            </Col>
        );
    }

    const overlay = showEdit && (
        <div className="editable-chart__admin-info">
            {result.targets.map((target, index) => (
                <Descriptions key={index} bordered={true} size="small" column={1}>
                    <Descriptions.Item label="ChartName">{globalId}</Descriptions.Item>
                    <Descriptions.Item label="Prefix">{target.prefix}</Descriptions.Item>
                    <Descriptions.Item label="Measurement">{target.measurement}</Descriptions.Item>
                    <Descriptions.Item label="MetricType">{metricType}</Descriptions.Item>
                </Descriptions>
            ))}

            <Space>
                <Button onClick={() => setShowEdit(false)}>返回</Button>
                <Button
                    href={`/board/explorer/edit/${result.id}`}
                    target="_blank"
                    type="primary"
                    icon={<EditOutlined />}
                >
                    编辑指标
                </Button>
            </Space>
        </div>
    );

    // const overlay = (
    //     <EditableChartOverlay
    //         chart={chart}
    //         metricType={metricType}
    //         // visible={overlayVisible}
    //         // onClose={() => setOverlayVisible(false)}
    //     />
    // );

    // return (
    //     <EMonitorChart
    //         span={span}
    //         type="card"
    //         key={globalId}
    //         chart={chart}
    //         // overlay={overlay}
    //         metricType={metricType}
    //         prefixKey={prefixKey}
    //         // globalId={globalId}
    //         // globalId="application_overview_soa_provider_latency"
    //     />
    // );

    return (
        <ChartCard
            span={span || 8}
            chart={result}
            key={globalId}
            uniqueId={globalId}
            awaitLoad={awaitLoad}
            editFunction={showEditButton ? () => setShowEdit(!showEdit) : null}
            overlay={overlay}
        />
    );
};

export default EditableChart;
