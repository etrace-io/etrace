import useBoard from "$hooks/useBoard";
import StoreManager from "$store/StoreManager";
import React, {useEffect, useState} from "react";
import {Button, Dropdown, Form, Menu, Popconfirm, Space} from "antd";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {HttpVariate, MetricVariate, Variate, VariateType} from "$models/BoardModel";

import {
    DeleteOutlined,
    DoubleLeftOutlined,
    DoubleRightOutlined,
    EditOutlined,
    PlusOutlined
} from "@ant-design/icons/lib";

interface ShowVariateProps {
    variates: Variate[];
    onEdit?: (va: Variate) => any;
    onCreate?: (variate: Variate) => void;
}

const ShowVariate: React.FC<ShowVariateProps> = props => {
    const {variates, onEdit, onCreate} = props;
    const {boardStore} = StoreManager;

    const moveLeft = (index: number) => {
        [variates[index - 1], variates[index]] = [variates[index], variates[index - 1]];
        boardStore.setVariates(variates);
    };

    const moveRight = (index: number) => {
        [variates[index], variates[index + 1]] = [variates[index + 1], variates[index]];
        boardStore.setVariates(variates);
    };

    const handleRemoveVariate = (name: string) => {
        boardStore.removeVariate(name);
    };

    const handleAddNewVariate = (variate: Variate) => {
        // 由于事先把 label 设置成了「指标名」，会导致之后展示时用户理解有误。
        // 故此处把 label 重新设置成变量的 tag name。
        if (variate) {
            variate.label = variate.name;
        }
        onCreate && onCreate(variate);
    };

    return (
        <Form><Space style={{flexWrap: "wrap"}} size={20}>
            {variates.map((variate, index) => (
                <Form.Item key={index}><Space>
                    <MultiVariateSelect variates={[variate]} key={index}/>
                    <Button.Group>
                        <Button
                            icon={<EditOutlined />}
                            onClick={() => onEdit(variate)}
                        />

                        {/* 前移 */}
                        {index !== 0 && (
                            <Button icon={<DoubleLeftOutlined />} onClick={() => moveLeft(index)}/>
                        )}

                        {/* 后移 */}
                        {index !== variates.length - 1 && (
                            <Button icon={<DoubleRightOutlined />} onClick={() => moveRight(index)}/>
                        )}
                    </Button.Group>

                    <Popconfirm
                        title="确认删除？"
                        okText="Yes"
                        cancelText="No"
                        onConfirm={() => handleRemoveVariate(variate.name)}
                    >
                        <Button type="primary" danger={true} icon={<DeleteOutlined />}/>
                    </Popconfirm>
                </Space></Form.Item>
            ))}

            <Form.Item>
                <VariateAddBtn onCreate={handleAddNewVariate}/>
            </Form.Item>
        </Space></Form>
    );
};

const VariateAddBtn: React.FC<{
    onCreate?: (option: Variate) => void;
}> = props => {
    const {onCreate} = props;
    const {boardStore} = StoreManager;

    const board = useBoard();
    const [variatesMap, setVariatesMap] = useState<any>({});

    useEffect(() => {
        if (!board) { return; }

        const content = {};

        board.layout?.forEach(layout =>
            layout.panels?.forEach(panel => {
                // 获取每个 Chart
                const chart = boardStore.getChart(panel.chartId, panel.globalId);

                chart?.targets?.forEach(target => {
                    target.variate?.forEach(name => {
                        // 遍历变量
                        const models: Variate[] = content[name] || [];
                        if (models.length === 0) {
                            models.push(new HttpVariate(name, name, ""));
                            content[name] = models;
                        }

                        const isExist = models.some((model: MetricVariate) => {
                            return model.type === VariateType.METRIC &&
                                model.prefix === target.prefix &&
                                model.measurement === target.measurement &&
                                model.label === chart.title;
                        });

                        if (!isExist) {
                            models.push(new MetricVariate(
                                chart.title,
                                name,
                                target.entity,
                                target.measurement,
                                [],
                                target.prefix
                            ));
                        }
                    });
                });
            })
        );

        setVariatesMap(content);
    }, [board]);

    const variateSelectMenu = (
        <Menu mode="vertical">
            <Menu.Item onClick={() => onCreate && onCreate(null)}>添加外部变量</Menu.Item>

            {Object.keys(variatesMap).map(name => (
                <Menu.SubMenu key={name} title={name}>
                    {variatesMap[name].map((option: Variate, index) => {
                        if (!option.type || option.type === VariateType.METRIC) {
                            const metricOp: MetricVariate = option as MetricVariate;

                            const measurement = metricOp.prefix
                                ? `${metricOp.prefix}.${metricOp.measurement}`
                                : metricOp.measurement;

                            return (
                                <Menu.Item key={index} onClick={() => onCreate && onCreate(option)}>
                                    {`${metricOp.label}(${measurement})`}
                                </Menu.Item>
                            );
                        }
                        return null;
                    })}
                </Menu.SubMenu>
            ))}
        </Menu>
    );

    return (
        <Dropdown overlay={variateSelectMenu} placement="bottomCenter">
            <Button icon={<PlusOutlined />} type="primary"/>
        </Dropdown>
    );
};

export default ShowVariate;
