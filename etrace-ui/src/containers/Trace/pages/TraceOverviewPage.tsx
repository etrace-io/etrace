import {Card} from "antd";
import React from "react";
import {APP_ID} from "$constants/index";
import {MetricVariate} from "$models/BoardModel";
import EMonitorChart from "$components/EMonitorChart/EMonitorChart";
import {EMonitorGallery, EMonitorSection} from "$components/EMonitorLayout";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";

const EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "transaction",
    null,
    null,
    APP_ID
);

const TraceOverviewPage: React.FC = props => {
    const variates = [EZONE];
    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item>
                <Card size="small">
                    <MultiVariateSelect variates={variates}/>
                </Card>
            </EMonitorSection.Item>

            <EMonitorSection.Item scroll={true}>
                <EMonitorGallery>
                    <EMonitorChart
                        globalId="application_overview_soa_provider_latency"
                        span={8}
                        prefixVariate={APP_ID}
                    />
                    <EMonitorChart
                        globalId="application_overview_soa_provider_count"
                        span={8}
                        prefixVariate={APP_ID}
                    />
                    <EMonitorChart
                        globalId="application_overview_soa_provider_rate"
                        span={8}
                        prefixVariate={APP_ID}
                    />
                    <EMonitorChart
                        globalId="application_overview_exception"
                        span={8}
                        prefixVariate={APP_ID}
                    />
                    <EMonitorChart
                        globalId="application_overview_dependency_count"
                        span={8}
                        prefixVariate={APP_ID}
                    />
                    <EMonitorChart
                        globalId="application_overview_dependency_time"
                        span={8}
                        prefixVariate={APP_ID}
                    />
                    <EMonitorChart
                        globalId="application_overview_soa_consumer_latency"
                        span={8}
                        prefixVariate={APP_ID}
                    />
                    <EMonitorChart
                        globalId="application_overview_soa_consumer_count"
                        span={8}
                        prefixVariate={APP_ID}
                    />
                    <EMonitorChart
                        globalId="application_overview_soa_consumer_rate"
                        span={8}
                        prefixVariate={APP_ID}
                    />
                </EMonitorGallery>
            </EMonitorSection.Item>

        </EMonitorSection>
    );
};

export default TraceOverviewPage;
