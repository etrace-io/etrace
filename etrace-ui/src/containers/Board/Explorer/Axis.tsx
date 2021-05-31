import {TableOutlined} from "@ant-design/icons/lib";
import React from "react";
import {Button, Card, Col, Form, InputNumber, Popover, Row, Select} from "antd";
import {UnitModel, UNITS} from "../../../models/UnitModel";
import StoreManager from "../../../store/StoreManager";
import {observer} from "mobx-react";
import {SketchPicker} from "react-color";
import {get} from "lodash";
import {ScaleModel, SCALES} from "../../../models/ScaleModel";
import SeriesNameSelect from "../../../components/select/SeriesNameSelect";
import {checkChartType} from "../../../utils/chart";
import ChartEditConfig from "./ChartEditConfig";
import ChartEditItem from "../../../components/ChartEditItem/ChartEditItem";
import {SPACE_BETWEEN} from "$constants/index";

const FormItem = Form.Item;
const Option = Select.Option;

const formItemLayout = {
    labelCol: {
        xs: {span: 8},
        sm: {span: 8},
        md: {span: 8},
    },
    wrapperCol: {
        xs: {span: 16},
        sm: {span: 16},
        md: {span: 16},
    },
};

interface AxisProps {
}

interface AxisState {
}

@observer
export default class Axis extends React.Component<AxisProps, AxisState> {
    editChartStore;

    constructor(props: AxisProps) {
        super(props);
        this.editChartStore = StoreManager.editChartStore;
    }

    buildUnits(ranges: any) {
        return (ranges.map((col: UnitModel) => {
                return (<Option key={col.text} value={col.text}>{col.text}</Option>);
            }
        ));
    }

    buildScales(ranges: any) {
        return (ranges.map((col: ScaleModel) => {
                return (<Option key={col.text} value={col.text}>{col.text}</Option>);
            }
        ));
    }

    render() {
        return (
            <Row gutter={SPACE_BETWEEN}>
                {checkChartType(ChartEditConfig.axis.leftYAxis.allowTypes) && (
                    <Col span={8}>{this.renderLeftYAxis()}</Col>
                )}

                {checkChartType(ChartEditConfig.axis.rightYAxis.allowTypes) && (
                    <Col span={8}>{this.renderRightAxis()}</Col>
                )}

                {checkChartType(ChartEditConfig.axis.threshold.allowTypes) && (
                    <Col span={8}>{this.renderThreshold()}</Col>
                )}
            </Row>
        );
    }

    /**
     * 左 Y 轴相关配置
     */
    private renderLeftYAxis() {
        return (
            <Card size="small" title="左Y轴">
                <Form>
                    <ChartEditItem
                        label="显示 Y 轴"
                        type="checkBox"
                        config={ChartEditConfig.axis.leftYAxis.visible}
                    />

                    <ChartEditItem
                        label="Y 轴单位"
                        type="select"
                        config={ChartEditConfig.axis.leftYAxis.unit}
                    >
                        {this.buildUnits(UNITS.colOne)}
                    </ChartEditItem>

                    <ChartEditItem
                        label="Y 轴刻度函数"
                        type="select"
                        disabled={true}
                        config={ChartEditConfig.axis.leftYAxis.scale}
                    >
                        {this.buildScales(SCALES.scales)}
                    </ChartEditItem>

                    <ChartEditItem
                        label="Y 轴标题"
                        type="input"
                        config={ChartEditConfig.axis.leftYAxis.title}
                        placeholder="请输入 Y 轴标题"
                    />

                    <ChartEditItem
                        label="Y 轴最小值"
                        type="number"
                        contentStyle={{width: "100%"}}
                        config={ChartEditConfig.axis.leftYAxis.min}
                        placeholder="请输入 Y 轴最小值"
                    />

                    <ChartEditItem
                        label="Y 轴最大值"
                        type="number"
                        contentStyle={{width: "100%"}}
                        config={ChartEditConfig.axis.leftYAxis.max}
                        placeholder="请输入 Y 轴最大值"
                    />

                    <ChartEditItem
                        label="保留小数位"
                        type="number"
                        min={0}
                        max={5}
                        contentStyle={{width: "100%"}}
                        config={ChartEditConfig.axis.leftYAxis.decimals}
                        placeholder="请输入 Y 轴保留小数位"
                    />
                </Form>
            </Card>
        );
    }

    /**
     * 右 Y 轴相关配置
     */
    private renderRightAxis() {
        return (
            <Card size="small" title="右Y轴">
                <Form>
                    <ChartEditItem
                        label="显示 Y 轴"
                        type="checkBox"
                        config={ChartEditConfig.axis.rightYAxis.visible}
                    />

                    <ChartEditItem
                        label="Y 轴单位"
                        type="select"
                        config={ChartEditConfig.axis.rightYAxis.unit}
                    >
                        {this.buildUnits(UNITS.colOne)}
                    </ChartEditItem>

                    <ChartEditItem
                        label="Y 轴刻度函数"
                        type="select"
                        disabled={true}
                        config={ChartEditConfig.axis.rightYAxis.scale}
                    >
                        {this.buildScales(SCALES.scales)}
                    </ChartEditItem>

                    <ChartEditItem
                        label="Y 轴标题"
                        type="input"
                        config={ChartEditConfig.axis.rightYAxis.title}
                        placeholder="请输入 Y 轴标题"
                    />

                    <ChartEditItem
                        label="Y 轴最小值"
                        type="number"
                        contentStyle={{width: "100%"}}
                        config={ChartEditConfig.axis.rightYAxis.min}
                        placeholder="请输入 Y 轴最小值"
                    />

                    <ChartEditItem
                        label="Y 轴最大值"
                        type="number"
                        contentStyle={{width: "100%"}}
                        config={ChartEditConfig.axis.rightYAxis.max}
                        placeholder="请输入 Y 轴最大值"
                    />

                    <ChartEditItem
                        label="保留小数位"
                        type="number"
                        min={0}
                        max={5}
                        contentStyle={{width: "100%"}}
                        config={ChartEditConfig.axis.rightYAxis.decimals}
                        placeholder="请输入 Y 轴保留小数位"
                    />

                    <ChartEditItem
                        label="Series"
                        type="custom"
                        customType={SeriesNameSelect}
                        config={ChartEditConfig.axis.rightYAxis.series}
                        chartUniqueId={this.editChartStore.chartUniqueId}
                        forceReload={true}
                    />
                </Form>
            </Card>
        );
    }

    /**
     * Threshold 相关配置
     */
    private renderThreshold() {
        const chart = this.editChartStore.getChart();

        return (
            <Card size="small" title="Threshold">
                <Form>
                    <FormItem {...formItemLayout} label="Threshold Min:">
                        <InputNumber
                            value={get(chart, "config.config.yAxis.plotBands[0].from", 0)}
                            style={{width: "calc(100% - 146px)"}}
                            onChange={(value) => this.editChartStore.mergeChartConfig({
                                "config": {
                                    "yAxis": {
                                        "plotBands": [{
                                            "from": value,
                                            "fromColor": get(chart, "config.config.yAxis.plotBands[0].fromColor", "rgba(252, 255, 197, 0.5)"),
                                            "to": get(chart, "config.config.yAxis.plotBands[0].to", 0),
                                            "toColor": get(chart, "config.config.yAxis.plotBands[0].toColor", "rgba(249, 233, 233, 0.5)")
                                        }]
                                    }
                                }
                            })}
                        />
                        <Popover
                            trigger="click"
                            content={
                                <SketchPicker
                                    color={get(chart, "config.config.yAxis.plotBands[0].fromColor", "rgba(252, 255, 197, 0.5)")}
                                    onChange={(value) => this.editChartStore.mergeChartConfig({
                                        "config": {
                                            "yAxis": {
                                                "plotBands": [{
                                                    "from": get(chart, "config.config.yAxis.plotBands[0].from", 0),
                                                    "fromColor": value.rgb,
                                                    "to": get(chart, "config.config.yAxis.plotBands[0].to", 0),
                                                    "toColor": get(chart, "config.config.yAxis.plotBands[0].toColor", "rgba(249, 233, 233, 0.5)")
                                                }]
                                            }
                                        }
                                    })}
                                />
                            }
                            title={"颜色选择器"}
                            placement={"right"}
                        >
                            <Button
                                htmlType="button"
                                icon={<TableOutlined />}
                                style={{backgroundColor: get(chart, "config.config.yAxis.plotBands[0].fromColor", "rgba(252, 255, 197, 0.5)")}}
                            />
                        </Popover>
                    </FormItem>
                    <FormItem {...formItemLayout} label="Threshold Max:">
                        <InputNumber
                            value={get(chart, "config.config.yAxis.plotBands[0].to", 0)}
                            style={{width: "calc(100% - 146px)"}}
                            onChange={(value) => this.editChartStore.mergeChartConfig({
                                "config": {
                                    "yAxis": {
                                        "plotBands": [{
                                            "from": get(chart, "config.config.yAxis.plotBands[0].from", 0),
                                            "fromColor": get(chart, "config.config.yAxis.plotBands[0].fromColor", "rgba(252, 255, 197, 0.5)"),
                                            "to": value,
                                            "toColor": get(chart, "config.config.yAxis.plotBands[0].toColor", "rgba(249, 233, 233, 0.5)")
                                        }]
                                    }
                                }
                            })}
                        />
                        <Popover
                            trigger="click"
                            content={
                                <SketchPicker
                                    color={get(chart, "config.config.yAxis.plotBands[0].toColor", "rgba(249, 233, 233, 0.5)")}
                                    onChange={(value) => this.editChartStore.mergeChartConfig({
                                        "config": {
                                            "yAxis": {
                                                "plotBands": [{
                                                    "from": get(chart, "config.config.yAxis.plotBands[0].from", 0),
                                                    "fromColor": get(chart, "config.config.yAxis.plotBands[0].fromColor", "rgba(252, 255, 197, 0.5)"),
                                                    "to": get(chart, "config.config.yAxis.plotBands[0].to", 0),
                                                    "toColor": value.rgb
                                                }]
                                            }
                                        }
                                    })}
                                />
                            }
                            title={"颜色选择器"}
                            placement={"right"}
                        >
                            <Button
                                htmlType="button"
                                icon={<TableOutlined />}
                                style={{backgroundColor: get(chart, "config.config.yAxis.plotBands[0].toColor", "rgba(249, 233, 233, 0.5)")}}
                            />
                        </Popover>
                    </FormItem>
                </Form>
            </Card>
        );
    }
}
