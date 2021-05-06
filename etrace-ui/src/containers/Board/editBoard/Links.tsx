import React from "react";
import {get} from "lodash";
import {observer} from "mobx-react";
import StoreManager from "$store/StoreManager";
import {Button, Checkbox, Form, Input, Select} from "antd";
import {DeleteOutlined, PlusOutlined} from "@ant-design/icons/lib";

const Links: React.FC = props => {
    const {boardStore} = StoreManager;

    const board = boardStore.getImmutableBoard();
    const links = get(board, "config.links", []);

    return (
        <>
            {links.map((item, index) => (
                <Form layout="inline" key={index} style={{marginBottom: 10}}>
                    <Form.Item label="标题">
                        <Input
                            value={get(item, "title", "")}
                            onChange={(value) => boardStore.editLink({"title": value.target.value}, index)}
                            placeholder="请输入标题"
                        />
                    </Form.Item>

                    <Form.Item label="地址">
                        <Input
                            value={get(item, "url", "")}
                            onChange={(value) => boardStore.editLink({"url": value.target.value}, index)}
                            style={{width: 380}}
                            placeholder="请输入需要跳转的 URL"
                            addonAfter={
                                <Select
                                    style={{width: 100}}
                                    value={get(item, "target", "_blank")}
                                    onChange={(value) => boardStore.editLink({"target": value}, index)}
                                >
                                    <Select.Option value="_blank">新窗口</Select.Option>
                                    <Select.Option value="_self">当前窗口</Select.Option>
                                </Select>
                            }
                        />
                    </Form.Item>
                    <Form.Item>
                        <Checkbox
                            checked={get(item, "time_range", false)}
                            onChange={(value) => boardStore.editLink({time_range: value.target.checked}, index)}
                        >
                            附带时间范围
                        </Checkbox>
                    </Form.Item>

                    <Form.Item>
                        <Checkbox
                            checked={get(item, "variable_value", false)}
                            onChange={(value) => boardStore.editLink({variable_value: value.target.checked}, index)}
                        >
                            附带看板变量
                        </Checkbox>
                    </Form.Item>

                    <Form.Item>
                        <Button
                            type="primary"
                            danger={true}
                            icon={<DeleteOutlined />}
                            onClick={() => boardStore.removeLink(index)}
                        />
                    </Form.Item>
                </Form>
            ))}

            <Button
                htmlType="button"
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => boardStore.addLink({})}
            >
                添加
            </Button>
        </>
    );
};

export default observer(Links);