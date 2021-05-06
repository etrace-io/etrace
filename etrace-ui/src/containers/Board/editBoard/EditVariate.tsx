import React from "react";
import StoreManager from "../../../store/StoreManager";
import {QuestionCircleOutlined} from "@ant-design/icons/lib";
import {Checkbox, Form, Input, Modal, Select, Tag} from "antd";
import {MetricVariate, Variate, VariateType} from "../../../models/BoardModel";

const FormItem = Form.Item;
const TextArea = Input.TextArea;

const formItemLayout = {
    labelCol: {
        xs: {span: 4},
        sm: {span: 4},
        md: {span: 4},
    },
    wrapperCol: {
        xs: {span: 20},
        sm: {span: 20},
        md: {span: 20},
    },
};
interface EditVariateProps {
    currentVariate: Variate;
    allVariate: Array<Variate>;
    visible: boolean;
    handleVariateSubmit: () => void;
    handleVariateCancel: () => void;
    changeName: (v: any) => void;
    changeLabel: (v: any) => void;
    changeType: (v: any) => void;
    changeFiled: (v: any) => void;
    changeOnlySingleSelect: (v: any) => void;
    variateValue: string;
    variatePlaceHolder: string;
}

export const variateChecked = (that: Variate, other: Variate): boolean => {
    return (!that.type || that.type == VariateType.METRIC)
        && (that as MetricVariate).relatedTagKeys
        && (that as MetricVariate).relatedTagKeys.indexOf(other.name) >= 0;
};

const EditVariate: React.FC<EditVariateProps> = props => {
    const buildRelatedVariate = (thisVariate: Variate, variates: Array<Variate>) => {
        return variates
            .filter(v => v.name != thisVariate.name)
            .map((other, index) => (
                <Tag.CheckableTag
                    key={index}
                    checked={variateChecked(thisVariate, other)}
                    onChange={() => updateRelatedVariate(thisVariate, other)}
                >{other.name}
                </Tag.CheckableTag>
            ));
    };

    const updateRelatedVariate = (that: Variate, other: Variate) => {
        if (!that.type || that.type == VariateType.METRIC) {
            const thisVariate = that as MetricVariate;
            if (!thisVariate.relatedTagKeys) {
                thisVariate.relatedTagKeys = [];
            }
            const index = thisVariate.relatedTagKeys.indexOf(other.name);
            if (index >= 0) {
                // remove
                thisVariate.relatedTagKeys.splice(index, 1);
            } else {
                // add
                thisVariate.relatedTagKeys.push(other.name);
            }
            StoreManager.boardStore.setVariate(thisVariate);
        }
    };

    const ModalTitle = (
        <div>
            <span>编辑变量</span>
            <a
                rel="noopener noreferrer"
                style={{float: "right", fontSize: "14px"}}
                href="https://monitor-doc.faas.elenet.me/manual/e-monitor/board/list.html#看板变量"
                target="_blank"
            >
                <QuestionCircleOutlined type="question-circle" style={{paddingRight: 6}}/>
                <span>帮助</span>
            </a>
        </div>
    );

    const {
        visible,
        handleVariateCancel,
        handleVariateSubmit,
        currentVariate,
        changeName,
        changeLabel,
        changeType,
        changeFiled,
        variatePlaceHolder,
        variateValue,
        allVariate,
        changeOnlySingleSelect
    } = props;

    return (
        <Modal
            style={{top: 80}}
            closable={false}
            title={ModalTitle}
            visible={visible}
            width={600}
            onOk={handleVariateSubmit}
            onCancel={handleVariateCancel}
        >
            <Form layout="horizontal" {...formItemLayout}>
                <FormItem label="变量字段">
                    <Input
                        value={currentVariate.name}
                        placeholder="该变量控制的 tag 字段, 看板唯一"
                        disabled={currentVariate.type == VariateType.METRIC}
                        onChange={changeName}
                    />
                </FormItem>

                <FormItem label="显示名称">
                    <Input
                        value={currentVariate.label}
                        placeholder="变量显示的名称"
                        onChange={changeLabel}
                    />
                </FormItem>

                <FormItem label="变量类型">
                    <Select
                        placeholder="请选择变量类型"
                        value={currentVariate.type}
                        onChange={changeType}
                        disabled={currentVariate.type == VariateType.METRIC}
                    >
                        <Select.Option key="http" value="http">HTTP</Select.Option>
                        <Select.Option key="value" value="enum">ENUM</Select.Option>
                    </Select>
                </FormItem>

                <FormItem label="变量值">
                    <TextArea
                        onChange={changeFiled}
                        value={variateValue}
                        placeholder={variatePlaceHolder}
                        autoSize={{minRows: 2, maxRows: 6}}
                        disabled={currentVariate.type == VariateType.METRIC}
                    />
                </FormItem>

                {currentVariate.type == VariateType.METRIC && (
                    <FormItem label="级联变量">
                        {buildRelatedVariate(currentVariate, allVariate)}
                    </FormItem>
                )}

                <FormItem label="仅可单选">
                    <Checkbox
                        checked={currentVariate.onlySingleSelect}
                        onChange={changeOnlySingleSelect}
                    />
                </FormItem>
            </Form>
        </Modal>
    );
};

export default EditVariate;
