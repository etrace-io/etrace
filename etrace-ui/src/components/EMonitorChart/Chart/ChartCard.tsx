import useUser from "$hooks/useUser";
import {UserKit} from "$utils/Util";
import {EMONITOR_RED} from "$constants/index";
import React, {useCallback, useState} from "react";
import {ChartInfo, MetricStatus} from "$models/ChartModel";
import {Button, Card, Popover, Spin, Tag, Tooltip} from "antd";
import {BarChartOutlined, ProfileOutlined} from "@ant-design/icons";
import ChartCardHeader from "$components/EMonitorChart/Chart/ChartCardHeader";
import ChartInfoPanel from "$components/EMonitorChart/Chart/ChartComponent/ChartInfoPanel";
import ChartFieldSelect from "$components/EMonitorChart/Chart/ChartComponent/ChartFieldSelect";

import "./ChartCard.less";

const ChartCard: React.FC<{
    dataSource: ChartInfo;
    loading?: boolean;
    hideExtra?: boolean;
    metricStatus?: MetricStatus; // 获取 Metric 状态提示对应信息
    onFieldChange?(targetIndex: number, fields: string[]): void;
}> = props => {
    const {dataSource: chartInfo, loading, hideExtra, onFieldChange, metricStatus, children} = props;

    const user = useUser();
    const [chartInfoVisible, setChartInfoVisible] = useState(false);
    const [isOriginalFields, setIsOriginalFields] = useState(true);

    let title = chartInfo?.title;

    // if (loading) { title = "加载中..."; }

    const handleFieldChange = useCallback((targetIndex: number, fields: string[], isOriginal: boolean) => {
        onFieldChange && onFieldChange(targetIndex, fields);
        setIsOriginalFields(isOriginal);
    }, [onFieldChange]);

    const handleToggleChartInfoVisible = useCallback(() => {
        setChartInfoVisible(v => !v);
    }, []);

    // Metric 状态表示
    const statusTag = metricStatus === MetricStatus.EXCEEDS_LIMIT
        ? <Tooltip
            placement="topLeft"
            color={EMONITOR_RED}
            title="当前数据组合数超出图表限制数；可以通过右上角头像按钮，进入「设置」进行「数据条数」调整。"
        >
            <Tag color={EMONITOR_RED} style={{marginRight: 0}}>组合超限</Tag>
        </Tooltip>
        : null;

    const header = <ChartCardHeader chart={chartInfo} prefix={statusTag} title={title}/>;

    // 显示 Chart 详情
    const adminInfoBtn = UserKit.isAdmin(user)
        ? <Popover
            key="adminInfo"
            placement="topRight"
            arrowPointAtCenter={true}
            content={<><Tag color={EMONITOR_RED}>Admin</Tag> 显示该指标详情</>}
        >
            <Button
                type="text"
                size="small"
                icon={<ProfileOutlined />}
                onClick={handleToggleChartInfoVisible}
            />
        </Popover>
        : null;

    // 选择其他字段
    const fieldSelectBtn = (
        <ChartFieldSelect key="fieldSelect" chart={chartInfo} onFieldChange={handleFieldChange}>
            <Button
                key="fieldSelectBtn"
                type={isOriginalFields ? "text" : "primary"}
                size="small"
                icon={<BarChartOutlined />}
            />
        </ChartFieldSelect>
    );

    const extra = hideExtra ? null : [adminInfoBtn, fieldSelectBtn];

    return (
        <Spin spinning={loading}>
            <Card className="emonitor-chart-card" size="small" title={header} extra={extra}>
                {children}

                {/* 遮罩层 */}
                {chartInfoVisible && (
                    <ChartInfoPanel
                        chart={chartInfo}
                        metricType={chartInfo?.targets?.[0]?.metricType}
                        onClose={() => setChartInfoVisible(false)}
                    />
                )}
            </Card>
        </Spin>
    );
};

export default ChartCard;
