import {Card, Radio} from "antd";
import {UnitModelEnum} from "$models/UnitModel";
import {MetricVariate} from "$models/BoardModel";
import MetricStatList from "$components/StatList/MetricStatList";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {EMonitorGallery, EMonitorSection} from "$components/EMonitorLayout";
import React, {useEffect, useMemo, useState} from "react";
import IconFont from "$components/Base/IconFont";
import StoreManager from "$store/StoreManager";
import {default as EditableChart} from "$components/EMonitorChart/Chart/EditableChart/EditableChart";
import {reaction} from "mobx";
import {APP_ID} from "$constants/index";

const types = [{
    icon: "icon-historyrecord",
    text: "GC总时间",
    type: "gc_time",
    target: {
        entity: "application",
        fields: ["t_sum(count)"],
        groupBy: ["hostName"],
        functions: [{defaultParams: ["-1d"], name: "timeShift"}],
        measurement: "${appId}.jvm_gc_timer",
        measurementVars: ["appId"],
        variate: ["ezone"],
        statListUnit: UnitModelEnum.Milliseconds,
    }
}, {
    icon: "icon-fangwencishu",
    text: "GC总次数",
    type: "gc_count",
    target: {
        entity: "application",
        fields: ["t_sum(count)"],
        groupBy: ["hostName"],
        functions: [{defaultParams: ["-1d"], name: "timeShift"}],
        measurement: "${appId}.jvm_gc_count",
        measurementVars: ["appId"],
        variate: ["ezone"],
        statListUnit: UnitModelEnum.Short,
    }
}, {
    icon: "icon-memory1",
    text: "Heap使用",
    type: "heap",
    target: {
        entity: "application",
        fields: ["t_max(max(gauge))"],
        groupBy: ["hostName"],
        variate: ["ezone"],
        functions: [{defaultParams: ["-1d"], name: "timeShift"}],
        measurement: "${appId}.jvm_memory",
        measurementVars: ["appId"],
        tagFilters: [{
            key: "name",
            op: "=",
            value: ["heapused"]
        }],
        statListUnit: UnitModelEnum.Bytes,
    }
}, {
    icon: "icon-cpu",
    text: "CPU",
    type: "cpu",
    target: {
        entity: "application",
        fields: ["t_max(max(gauge))"],
        groupBy: ["hostName"],
        functions: [{defaultParams: ["-1d"], name: "timeShift"}],
        measurement: "${appId}.jvm_cpu",
        variate: ["ezone"],
        measurementVars: ["appId"],
        statListUnit: UnitModelEnum.Percent,
    }
}, {
    icon: "icon-zprofilerithread1",
    text: "线程数",
    type: "thread",
    target: {
        entity: "application",
        fields: ["t_max(max(gauge))"],
        groupBy: ["hostName"],
        functions: [{defaultParams: ["-1d"], name: "timeShift"}],
        measurement: "${appId}.jvm_thread",
        variate: ["ezone"],
        measurementVars: ["appId"],
        statListUnit: UnitModelEnum.Short,
    }
}];

const EZONE: MetricVariate = new MetricVariate(
    "EZone",
    "ezone",
    "application",
    "jvm_gc_count",
    null,
    null,
    APP_ID
);

const HOST: MetricVariate = new MetricVariate(
    "Host",
    "hostName",
    "application",
    "jvm_gc_count",
    null,
    null,
    APP_ID
);

const TraceJVMPage: React.FC = props => {
    const variates = [EZONE, HOST];

    const [currType, setCurrType] = useState<string>("gc_time");

    const options = types.map(type => ({
        label: <span><IconFont type={type.icon}/> {type.text}</span>,
        value: type.type
    }));

    const currStatListTarget = useMemo(() => types.find(i => i.type === currType).target, [currType]);

    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item>
                <Card size="small">
                    <MultiVariateSelect variates={variates}/>
                </Card>
            </EMonitorSection.Item>

            <EMonitorSection.Item>
                <Card size="small">
                    <Radio.Group
                        options={options}
                        onChange={e => setCurrType(e.target.value)}
                        value={currType}
                        optionType="button"
                        buttonStyle="solid"
                    />
                </Card>
            </EMonitorSection.Item>

            <EMonitorSection fullscreen={true} mode="horizontal">
                <EMonitorSection.Item width="30%">
                    <MetricStatList
                        target={currStatListTarget}
                        inputPlaceholder="Input Host..."
                    />
                </EMonitorSection.Item>

                <EMonitorSection.Item scroll={true}>
                    <JVMDetail/>
                </EMonitorSection.Item>
            </EMonitorSection>
        </EMonitorSection>
    );
};

const JVMDetail: React.FC = props => {
    const [selectedHost, setSelectedHost] = useState<string>();
    useEffect(() => {
        const disposer = reaction(
            () => StoreManager.urlParamStore.getValue("hostName"),
            setSelectedHost,
        );

        return () => disposer();
    }, []);

    return selectedHost
        ? <EMonitorGallery>
            <EditableChart
                globalId="application_jvm_gc_timer"
                span={12}
                prefixKey={APP_ID}
                metricType="counter"
            />
            <EditableChart
                globalId="application_jvm_gc_count"
                span={12}
                prefixKey={APP_ID}
            />
            <EditableChart
                globalId="application_jvm_cpu"
                span={12}
                prefixKey={APP_ID}
            />
            <EditableChart
                globalId="application_jvm_memory"
                span={12}
                prefixKey={APP_ID}
            />
            <EditableChart
                globalId="application_jvm_class_load"
                span={12}
                prefixKey={APP_ID}
            />
            <EditableChart
                globalId="application_jvm_thread"
                span={12}
                prefixKey={APP_ID}
                metricType="gauge"
            />
            <EditableChart
                globalId="application_jvm_memory_pool_used"
                span={8}
                prefixKey={APP_ID}
            />
            <EditableChart
                globalId="application_jvm_memory_pool_committed"
                span={8}
                prefixKey={APP_ID}
            />
            <EditableChart
                globalId="application_jvm_memory_pool_max"
                span={8}
                prefixKey={APP_ID}
            />
        </EMonitorGallery>
        : <EMonitorGallery>
            <EditableChart
                globalId="application_jvm_gc_timer_no_host"
                span={24}
                prefixKey={APP_ID}
            />
            <EditableChart
                globalId="application_jvm_gc_count_no_host"
                span={24}
                prefixKey={APP_ID}
            />
            <EditableChart
                globalId="e7bed342-4801-4b63-8ee3-088ffc06a73c"
                span={24}
                prefixKey={APP_ID}
            />
        </EMonitorGallery>;
};

export default TraceJVMPage;
