import {debounce, get} from "lodash";
import StoreManager from "$store/StoreManager";
import {ComputeTarget} from "$models/ChartModel";
import React, {useState} from "react";
import {Button, Form, Input, Row, Tooltip} from "antd";
import {DeleteOutlined, EyeInvisibleOutlined, EyeOutlined} from "@ant-design/icons/lib";

const ComputeTargetOption: React.FC<{
    index: number;
    target: ComputeTarget;
}> = props => {
    const {index, target} = props;
    const display = get(target, "display", true);

    const [express, setExpress] = useState<string>(get(target, "compute", ""));
    const [alias, setAlias] = useState<string>(get(target, "alias", ""));

    // const targetCompute = ;
    // const targetAlias = ;
    //
    // useEffect(() => {
    //     setExpress(targetCompute);
    // }, [targetCompute]);
    //
    // useEffect(() => {
    //     setAlias(targetAlias);
    // }, [targetAlias]);

    const displayComputeTarget = (idx: number, computeTarget: ComputeTarget) => {
        if (computeTarget.display === undefined) {
            computeTarget.display = false;
        } else {
            computeTarget.display = !computeTarget.display;
        }
        StoreManager.editChartStore.setComputeTarget(idx, computeTarget);
    };

    const changeCompute = debounce((idx: number, value: string) => {
        const chart = StoreManager.editChartStore.getChart();
        const computeTarget = chart.config.computes[idx];
        computeTarget.compute = value;

        StoreManager.editChartStore.setComputeTarget(idx, computeTarget);
    }, 500);

    const changeAlias = debounce((idx: number, value: string) => {
        const chart = StoreManager.editChartStore.getChart();
        const computeTarget = chart.config.computes[idx];
        computeTarget.alias = value;

        StoreManager.editChartStore.setComputeTarget(idx, computeTarget);
    },  500);

    const handleExpressChange = (idx: number, value: string) => {
        setExpress(value);
        changeCompute(idx, value);
    };

    const handleAliasChange = (idx: number, value: string) => {
        setAlias(value);
        changeAlias(idx, value);
    };

    return (
        <Row className="each-metrics-config">
            <Form layout="inline" style={{opacity: !display ? 0.35 : 1}}>
                <Form.Item label="表达式">
                    <Input
                        placeholder="请输入指标编号的四则运算，如：${A}/(${A}+${B})"
                        onChange={(e) => handleExpressChange(index, e.target.value)}
                        style={{width: "550px"}}
                        value={express}
                    />
                </Form.Item>
                <Form.Item label="别名">
                    <Input
                        placeholder="请输入"
                        onChange={(e) => handleAliasChange(index, e.target.value)}
                        value={alias}
                    />
                </Form.Item>
            </Form>
            <Button.Group style={{flexShrink: 0, alignSelf: "center", width: "92px"}}>
                <Tooltip title={!display ? "显示指标" : "隐藏指标"}>
                    <Button
                        icon={!display ? <EyeOutlined /> : <EyeInvisibleOutlined />}
                        onClick={() => displayComputeTarget(index, target)}
                    />
                </Tooltip>
                <Tooltip title="删除当前指标" placement="topRight">
                    <Button
                        icon={<DeleteOutlined />}
                        danger={true}
                        onClick={() => StoreManager.editChartStore.deleteComputeTarget(index)}
                    />
                </Tooltip>
            </Button.Group>
        </Row>
    );
};

export default ComputeTargetOption;
