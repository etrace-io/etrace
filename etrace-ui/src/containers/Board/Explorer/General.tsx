import {get} from "lodash";
import {observer} from "mobx-react";
import StoreManager from "$store/StoreManager";
import React, {useEffect, useState} from "react";
import {Cascader, Col, Form, Input, Row} from "antd";
import * as ChartService from "$services/ChartService";

const General: React.FC = props => {
    const {editChartStore, productLineStore, urlParamStore} = StoreManager;

    const [globalIdValidated, setGlobalIdValidated] = useState<boolean>(false);

    useEffect(() => {
        loadData();
        const globalId = urlParamStore.getValue("uniqueId");
        changeGlobalId(globalId);
    }, []);

    const loadData = () => {
        productLineStore.loadDepartmentTree().then(() => {
            const defaultCategory: number[] = productLineStore.getDefaultCategory();
            const chart = editChartStore.getChart();
            if (defaultCategory.length == 2 && chart && chart.id < 0) {
                chart.departmentId = defaultCategory[0];
                chart.productLineId = defaultCategory[1];
                editChartStore.setChart(chart);
            }
            setGlobalIdValidated(true);
        });
    };

    const changeGlobalId = (inputGlobalId: string) => {
        const chart = editChartStore.getChart();
        const globalId = chart.globalId;

        if (inputGlobalId && inputGlobalId != globalId) {
            ChartService.validateChartGlobalId(inputGlobalId).then((isOk: boolean) => {
                editChartStore.mergeChartGeneral({"globalId": inputGlobalId});
                setGlobalIdValidated(isOk);
            }).catch(err => {
                setGlobalIdValidated(false);
            });
        } else if (inputGlobalId) {
            editChartStore.mergeChartGeneral({"globalId": inputGlobalId});
            setGlobalIdValidated(false);
        }
    };

    const changeChartCategory = (value: Array<string>) => {
        const chart = editChartStore.getChart();
        chart.departmentId = Number(value[0]);
        chart.productLineId = Number(value[1]);
        editChartStore.setChartChange(chart);
    };

    const currChart = editChartStore.getChart();
    const departmentTree = productLineStore.getDepartmentTree();
    const category = currChart && currChart.departmentId > 0 && currChart.productLineId > 0
        ? [currChart.departmentId, currChart.productLineId]
        : productLineStore.getDefaultCategory();

    return (
        <Form>
            <Row gutter={24}>
                <Col span={8}>
                    <Form.Item label="分类" required={true}>
                        <Cascader
                            value={category}
                            options={departmentTree}
                            placeholder="请选择部门分类"
                            showSearch={true}
                            onChange={changeChartCategory}
                        />
                    </Form.Item>
                </Col>

                <Col span={8}>
                    <Form.Item label="标题" required={true}>
                        <Input
                            value={get(currChart, "title", "")}
                            placeholder="请输入指标标题"
                            onChange={(e) => editChartStore.mergeChartGeneral({"title": e.target.value})}
                        />
                    </Form.Item>
                </Col>

                <Col span={8}>
                    <Form.Item label="全局 ID">
                        <Input
                            value={get(currChart, "globalId", "")}
                            disabled={true}
                            placeholder="将自动生成随机全局 ID"
                            onChange={(e) => changeGlobalId(e.target.value)}
                        />
                        {!globalIdValidated &&
                        <span style={{color: "red"}}>ID 无效或与其他 ID 重复，请更换</span>}
                    </Form.Item>
                </Col>

                <Col span={24}>
                    <Form.Item label="描述">
                        <Input.TextArea
                            placeholder="请输入指标描述"
                            value={get(currChart, "description", "")}
                            autoSize={{minRows: 3, maxRows: 6}}
                            onChange={(e) => editChartStore.mergeChartGeneral({"description": e.target.value})}
                        />
                    </Form.Item>
                </Col>
            </Row>
        </Form>
    );
};

export default observer(General);
