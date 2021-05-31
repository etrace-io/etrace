import React from "react";
import {observer} from "mobx-react";
import {checkChartType} from "$utils/chart";
import {SPACE_BETWEEN} from "$constants/index";
import ChartEditConfig from "./ChartEditConfig";
import {Card, Col, Divider, Form, Row, Select} from "antd";
import ChartEditItem from "$components/ChartEditItem/ChartEditItem";

const Legend: React.FC = props => {
    return (
        <Row gutter={SPACE_BETWEEN}>
            {checkChartType(ChartEditConfig.legend.config.allowTypes) && (
                <Col span={7}><LegendConfig/></Col>
            )}
            {checkChartType(ChartEditConfig.legend.value.allowTypes) && (
                <Col span={8}><LegendValue/></Col>
            )}
        </Row>
    );
};

export default observer(Legend);

/*
 * 配置项相关设置
 */
const LegendConfig: React.FC = props => {
    return (
        <Card size="small" title="配置项">
            <Form>
                <ChartEditItem
                    label="显示"
                    type="checkBox"
                    config={ChartEditConfig.legend.config.display}
                />

                <ChartEditItem
                    label="布局"
                    type="select"
                    config={ChartEditConfig.legend.config.layout}
                >
                    <Select.Option value="left">靠左</Select.Option>
                    <Select.Option value="center">居中</Select.Option>
                    <Select.Option value="right">靠右</Select.Option>
                </ChartEditItem>

                <ChartEditItem
                    label="表格显示"
                    type="checkBox"
                    config={ChartEditConfig.legend.config.asTable}
                />

                <ChartEditItem
                    label="右边显示"
                    type="checkBox"
                    config={ChartEditConfig.legend.config.toRight}
                />

                {/*<ChartEditItem*/}
                {/*label="保留小数位"*/}
                {/*type="number"*/}
                {/*min={0}*/}
                {/*max={100}*/}
                {/*placeholder="auto"*/}
                {/*contentStyle={{width: "100%"}}*/}
                {/*config={ChartEditConfig.legend.config.decimals}*/}
                {/*/>*/}

                <ChartEditItem
                    label="宽度"
                    type="number"
                    min={0}
                    placeholder="auto"
                    contentStyle={{width: "100%"}}
                    config={ChartEditConfig.legend.config.width}
                />

                <ChartEditItem
                    label="最大显示条数"
                    type="number"
                    min={0}
                    placeholder="超过自动隐藏图例"
                    contentStyle={{width: "100%"}}
                    tooltipContent="超过自动隐藏图例（右边显示不生效）"
                    config={ChartEditConfig.legend.config.maxItem}
                />

                <ChartEditItem
                    label="搜索框"
                    type="checkBox"
                    config={ChartEditConfig.legend.config.showSearch}
                />
            </Form>
        </Card>
    );
};

/**
 * 取值配置
 */
const LegendValue: React.FC = props => {
    return (
        <Card size="small" title="取值">
            <Row>
                <Col span={8} style={{margin: "6px 0"}}>
                    <ChartEditItem
                        type="checkBox"
                        notFormItem={true}
                        config={ChartEditConfig.legend.value.total}
                    >
                        Total
                    </ChartEditItem>
                </Col>
                <Col span={8} style={{margin: "6px 0"}}>
                    <ChartEditItem
                        type="checkBox"
                        notFormItem={true}
                        config={ChartEditConfig.legend.value.max}
                    >
                        Max
                    </ChartEditItem>
                </Col>
                <Col span={8} style={{margin: "6px 0"}}>
                    <ChartEditItem
                        type="checkBox"
                        notFormItem={true}
                        config={ChartEditConfig.legend.value.min}
                    >
                        Min
                    </ChartEditItem>
                </Col>
                <Col span={8} style={{margin: "6px 0"}}>
                    <ChartEditItem
                        type="checkBox"
                        notFormItem={true}
                        config={ChartEditConfig.legend.value.avg}
                    >
                        Avg
                    </ChartEditItem>
                </Col>
                <Col span={12} style={{margin: "6px 0"}}>
                    <ChartEditItem
                        type="checkBox"
                        notFormItem={true}
                        config={ChartEditConfig.legend.value.current}
                    >
                        Current
                    </ChartEditItem>
                </Col>
            </Row>

            <Divider dashed={true} style={{margin: "10px 0"}}/>

            <Form>
                <ChartEditItem
                    label="保留小数位"
                    type="number"
                    min={0}
                    max={100}
                    placeholder="auto"
                    contentStyle={{width: "100%"}}
                    config={ChartEditConfig.legend.value.decimals}
                />
            </Form>
        </Card>
    );
};
