import React from "react";
import {EMonitorGallery, EMonitorSection} from "$components/EMonitorLayout";
import MetricStatList from "$components/StatList/MetricStatList";
import {Target, Targets} from "$models/ChartModel";
import {Card, Space} from "antd";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {Variate} from "$models/BoardModel";

const EMonitorPageTemplate: React.FC<{
    variates?: Variate[];
    target?: Target; // target 和 targets 必须其中配置一个
    targets?: Targets; // 设置多个 target 需要切换显示；可以和 target 并存，但是该字段会被忽略
    statListPlaceholder?: string;
}> = props => {
    const {variates} = props;
    const {target, targets, statListPlaceholder} = props;
    const {children} = props;

    return (
        <EMonitorSection fullscreen={true}>
            {variates && (
                <EMonitorSection.Item>
                    <Card size="small">
                        <Space size={16}>
                            <MultiVariateSelect variates={variates}/>
                        </Space>
                    </Card>
                </EMonitorSection.Item>
            )}

            <EMonitorSection fullscreen={true} mode="horizontal">
                {(target || targets) && (
                    <EMonitorSection.Item width="30%">
                        <MetricStatList
                            target={target}
                            targets={targets}
                            inputPlaceholder={statListPlaceholder}
                        />
                    </EMonitorSection.Item>
                )}

                <EMonitorSection.Item scroll={true}>
                    <EMonitorGallery>
                        {children}
                    </EMonitorGallery>
                </EMonitorSection.Item>
            </EMonitorSection>
        </EMonitorSection>
    );
};

export default EMonitorPageTemplate;
