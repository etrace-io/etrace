import React from "react";
import {observer} from "mobx-react";
import {checkChartType} from "$utils/chart";
import {Card, Col, Form, Row, Select} from "antd";
import {UnitModel, UNITS} from "$models/UnitModel";
import ChartEditItem from "$components/ChartEditItem/ChartEditItem";
import ChartEditConfig from "$containers/Board/Explorer/ChartEditConfig";

const Display: React.FC = props => {
    return (
        <Row gutter={8} style={{marginBottom: 6}}>
            {checkChartType(ChartEditConfig.display.series.allowTypes) && (
                <Col span={6}><DisplaySeries/></Col>
            )}

            {checkChartType(ChartEditConfig.display.tooltip.allowTypes) && (
                <Col span={6}><DisplayTooltip/></Col>
            )}

            {checkChartType(ChartEditConfig.display.title.allowTypes) && (
                <Col span={6}><DisplayTitle/></Col>
            )}

            {checkChartType(ChartEditConfig.display.text.allowTypes) && (
                <Col span={6}><DisplayText/></Col>
            )}
            {checkChartType(ChartEditConfig.display.table.allowTypes) && (
                <Col span={6}><DisplayTable/></Col>
            )}
        </Row>
    );
};

export default observer(Display);

/**
 * Series 相关设置
 */
const DisplaySeries: React.FC = props => {
    return (
        <Card size="small" title="Series">
            <Form>
                <ChartEditItem
                    label="透明度"
                    type="number"
                    placeholder="Select Fill Opacity"
                    min={0}
                    max={10}
                    contentStyle={{width: "100%"}}
                    tooltipContent="0 为全透明，10 为不透明"
                    config={ChartEditConfig.display.series.fillOpacity}
                    forceReload={true}
                />

                <ChartEditItem
                    label="线宽度"
                    type="number"
                    placeholder="Select Line Width"
                    min={1}
                    max={10}
                    contentStyle={{width: "100%"}}
                    config={ChartEditConfig.display.series.lineWidth}
                />

                <ChartEditItem
                    label="显示点"
                    type="checkBox"
                    config={ChartEditConfig.display.series.showPoint}
                />

                <ChartEditItem
                    label="点大小"
                    type="number"
                    placeholder="Select Marker Radius"
                    min={1}
                    max={10}
                    contentStyle={{width: "100%"}}
                    config={ChartEditConfig.display.series.markerRadius}
                />

                <ChartEditItem
                    label="点样式"
                    type="select"
                    placeholder="Select Marker Style"
                    config={ChartEditConfig.display.series.pointStyle}
                >
                    <Select.Option value="circle">Circle</Select.Option>
                    <Select.Option value="cross">Cross</Select.Option>
                    <Select.Option value="crossRot">CrossRot</Select.Option>
                    <Select.Option value="dash">Dash</Select.Option>
                    <Select.Option value="line">Line</Select.Option>
                    <Select.Option value="rect">Rect</Select.Option>
                    <Select.Option value="rectRounded">RectRounded</Select.Option>
                    <Select.Option value="rectRot">RectRot</Select.Option>
                    <Select.Option value="star">Star</Select.Option>
                    <Select.Option value="triangle">Triangle</Select.Option>
                </ChartEditItem>

                <ChartEditItem
                    label="空值处理"
                    type="select"
                    placeholder="Select null value process"
                    forceReload={true}
                    config={ChartEditConfig.display.series.nullAsZero}
                >
                    <Select.Option value="null">null</Select.Option>
                    <Select.Option value="null_as_zone">null as zero</Select.Option>
                    <Select.Option value="connectNulls">connected</Select.Option>
                </ChartEditItem>

                <ChartEditItem
                    label="堆叠"
                    type="select"
                    placeholder="Select Series Stacking"
                    config={ChartEditConfig.display.series.seriesStacking}
                >
                    <Select.Option value="">不堆叠</Select.Option>
                    <Select.Option value="normal">堆叠</Select.Option>
                </ChartEditItem>

                <ChartEditItem
                    label="环比线样式"
                    type="select"
                    placeholder="Select Dash Style"
                    forceReload={true}
                    config={ChartEditConfig.display.series.dashStyle}
                >
                    <Select.Option value="2,2">Dot</Select.Option>
                    <Select.Option value="1,1">ShortDot</Select.Option>
                    <Select.Option value="10,10">Dash</Select.Option>
                    <Select.Option value="5,5">ShortDash</Select.Option>
                    <Select.Option value="10,2,2,2">DashDot</Select.Option>
                    <Select.Option value="5,1,1,1">ShortDashDot</Select.Option>
                    <Select.Option value="">Solid</Select.Option>
                </ChartEditItem>
            </Form>
        </Card>
    );
};

/**
 * ToolTip 相关设置
 */
const DisplayTooltip: React.FC = props => {
    return (
        <Card size="small" title="Tooltip">
            <Form>
                <ChartEditItem
                    type="checkBox"
                    label="0 值显示"
                    config={ChartEditConfig.display.tooltip.showZero}
                />

                <ChartEditItem
                    type="select"
                    label="排序"
                    placeholder="Select Sort order"
                    config={ChartEditConfig.display.tooltip.sort}
                >
                    <Select.Option value="Increasing">升序</Select.Option>
                    <Select.Option value="Decreasing">降序</Select.Option>
                </ChartEditItem>
            </Form>
        </Card>
    );
};

/**
 * Title 相关设置
 */
const DisplayTitle: React.FC = props => {
    return (
        <Card size="small" title="Title">
            <Form>
                <ChartEditItem
                    type="checkBox"
                    label="显示聚合时间"
                    config={ChartEditConfig.display.title.showInterval}
                />
            </Form>
        </Card>
    );
};

/**
 * Text Type 相关设置
 */
const DisplayText: React.FC = props => {
    const buildUnits = (ranges: any) => (
        ranges.map((col: UnitModel) => <Select.Option key={col.text} value={col.text}>{col.text}</Select.Option>
    ));

    return (
        <Card size="small" title="Text">
            <Form>
                <ChartEditItem
                    type="checkBox"
                    label="显示环比"
                    config={ChartEditConfig.display.text.showTimeshift}
                />

                <ChartEditItem
                    type="number"
                    label="环比精度"
                    min={0}
                    max={10}
                    placeholder="环比保留小数位数"
                    config={ChartEditConfig.display.text.timeshiftPrecision}
                />

                <ChartEditItem
                    label="单位"
                    type="select"
                    config={ChartEditConfig.display.text.valueUnit}
                >
                    {buildUnits(UNITS.colOne)}
                </ChartEditItem>

                <ChartEditItem
                    type="number"
                    label="数值精度"
                    min={0}
                    max={10}
                    placeholder="数值保留小数位数"
                    config={ChartEditConfig.display.text.valuePrecision}
                />

                <ChartEditItem
                    type="input"
                    label="数值标题"
                    placeholder="请输入前缀"
                    config={ChartEditConfig.display.text.valueTitle}
                />

                <ChartEditItem
                    type="input"
                    label="前缀"
                    placeholder="请输入前缀"
                    config={ChartEditConfig.display.text.customPrefix}
                />

                <ChartEditItem
                    type="input"
                    label="尾缀"
                    placeholder="请输入尾缀"
                    config={ChartEditConfig.display.text.customSuffix}
                />
            </Form>
        </Card>
    );
};

/**
 * Table Type 相关设置
 */
const DisplayTable: React.FC = props => {
    return (
        <Card size="small" title="Table">
            <Form>
                <ChartEditItem
                    type="checkBox"
                    label="显示环比"
                    config={ChartEditConfig.display.table.showTimeshift}
                />
            </Form>
        </Card>
    );
};
