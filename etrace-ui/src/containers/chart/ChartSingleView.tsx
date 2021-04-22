import * as qs from "query-string";
import {useFullscreen} from "ahooks";
import {Chart} from "../../models/ChartModel";
import Metric from "../../components/Metric/Metric";
import React, {useEffect, useState} from "react";
import * as notification from "../../utils/notification";
import * as ChartService from "../../services/ChartService";
import Exception from "../../components/Exception/Exception";
import {Button, Row, Space, Spin, Tooltip} from "antd";
import TimePicker from "../../components/TimePicker/TimePicker";
import {Link, useHistory, useLocation, useParams} from "react-router-dom";
import ChartMetricView from "../../components/Chart/ChartMetricView/ChartMetricView";
import {EditOutlined, FullscreenExitOutlined, FullscreenOutlined} from "@ant-design/icons/lib";
import {EMonitorPage, EMonitorSection} from "$components/EMonitorLayout";

const CHART_SINGLE_VIEW_ID = "emonitor-single-view-fullscreen";

const ChartSingleView: React.FC = props => {
    const [chart, setChart] = useState<Chart>();

    const history = useHistory();
    const location = useLocation();
    const params = useParams<{ chartId: string }>();
    const [loading, setLoading] = useState(false);
    const [isFullscreen, { toggleFull }] = useFullscreen(() => document.getElementById(CHART_SINGLE_VIEW_ID));

    const {chartId} = params;
    const {globalId} = qs.parse(location.search);

    // const {chart, isLoading} = useChart({
    //     id: chartId,
    //     globalId,
    // });

    const loadChart = (id) => {
        setLoading(true);
        ChartService
            .get(id)
            .then(c => {
                setChart(c);
                setLoading(false);
            });
    };

    useEffect(() => {
        if (globalId) {
            ChartService
                .searchByGroup({ globalId, status: "Active" })
                .then((results: any) => {
                    if (results.results.length > 0) {
                        const newId = results.results[ 0 ].id;
                        history.replace({ pathname: `/chart/${newId}` });
                        loadChart(newId);
                    } else {
                        notification.warningHandler({
                            message: "未在该环境找到 GlobalId 对应的配置",
                            description: "建议使用「同步」功能，从其他环境同步配置到当当前环境", duration: 6,
                        });
                    }
                });
        } else {
            loadChart(chartId);
        }
    }, [chartId, globalId]);

    const content = chart
        ? (
            <>
                <EMonitorSection.Item type="card">
                    <Row justify="end">
                        <Space>
                            <TimePicker hasTimeShift={true} hasTimeZoom={true}/>

                            <Tooltip title="编辑" color="blue">
                                <Link to={`/board/explorer/edit/${chart.id}`} target="_blank">
                                    <Button type="primary" icon={<EditOutlined/>}/>
                                </Link>
                            </Tooltip>

                            <Tooltip title="编辑" color="blue">
                                <Button
                                    icon={isFullscreen ? <FullscreenExitOutlined/> : <FullscreenOutlined/>}
                                    onClick={toggleFull}
                                />
                            </Tooltip>
                        </Space>
                    </Row>
                </EMonitorSection.Item>

                <EMonitorSection scroll={true}>
                    <EMonitorSection.Item>
                        <Metric
                            chart={chart}
                            height={500}
                            title={<span style={{marginBottom: "0"}} key={chart.globalId}>{chart.title}</span>}
                        />
                    </EMonitorSection.Item>

                    {chart && chart.targets && (
                        <EMonitorSection.Item type="card">
                            <ChartMetricView targets={chart.targets}/>
                        </EMonitorSection.Item>
                    )}
                </EMonitorSection>
            </>
        )
        : loading ? <Spin/> : <Exception type="202" desc="未找到对应 Chart"/>;

    return (
        <EMonitorPage>
            <EMonitorSection fullscreen={true}>
                {content}
            </EMonitorSection>
        </EMonitorPage>
    );
};

export default ChartSingleView;
