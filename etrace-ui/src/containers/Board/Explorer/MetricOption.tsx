import React from "react";
import {get} from "lodash";
import {observer} from "mobx-react";
import {Button, Divider, Row} from "antd";
import TargetOption from "../../../components/Board/Explorer/TargetOption/TargetOption";
import {PlusOutlined} from "@ant-design/icons";
import ComputeTargetOption from "./ComputeTargetOption";
import StoreManager from "../../../store/StoreManager";
import {Chart, ChartTypeEnum, ComputeTarget, Target} from "../../../models/ChartModel";

const ButtonGroup = Button.Group;

function initTarget() {
    return {
        entity: null,
        tagFilters: [],
        fields: [],
        groupBy: [],
        orderBy: "",
        prefixRequired: false,
        prefixDisplay: false,
        measurementDisplay: false,
        fieldsDisplay: false,
        groupByDisplay: false,
        tags: new Map(),
        tagKeys: [],
        variate: [],
        functions: []
    };
}

function initComputeTarget() {
    return {
        compute: null,
        functions: [],
        display: true,
    };
}

const MetricOption: React.FC = props => {
    const {editChartStore} = StoreManager;

    const newTarget = () => {
        const target: Target = initTarget();
        editChartStore.newTarget(target);
    };

    const newComputeTarget = () => {
        const target: ComputeTarget = initComputeTarget();
        editChartStore.newComputeTarget(target);
    };

    const chart = editChartStore.getChart();
    const targets = get(chart, "targets", []);
    const chartType = get(chart, "config.type", "");
    const showNewTargetBtn = chartType === ChartTypeEnum.Text ? targets.length < 1 : true;

    return (
        <>
            {get<Chart, "targets", Target[]>(chart, "targets", []).map((value: Target, index) => value.isAnalyze ? null : (
                <div key={index} style={{marginTop: -3}}>
                    <TargetOption target={value} index={index}/>
                    <Divider style={{margin: "8px 0"}} dashed={true}/>
                </div>
            ))}

            {get(chart, "config.computes", []).map((value, index) => (
                <div key={index} style={{marginTop: -3}}>
                    <ComputeTargetOption target={value} index={index}/>
                    <Divider style={{margin: "8px 0"}} dashed={true}/>
                </div>
            ))}

            {showNewTargetBtn && (
                <Row>
                    <ButtonGroup>
                        <Button type="primary" icon={<PlusOutlined />} onClick={newTarget}>添加指标</Button>
                        <Button type="dashed" icon={<PlusOutlined />} onClick={newComputeTarget}>添加计算指标</Button>
                    </ButtonGroup>
                </Row>
            )}
        </>
    );
};

export default observer(MetricOption);