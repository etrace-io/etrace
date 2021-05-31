import React from "react";
import {get} from "lodash";
import {observer} from "mobx-react";
import {Descriptions, Tag} from "antd";
import StoreManager from "../../store/StoreManager";

interface ProductLineProps {
    onDepartmentChange?: (tag: any, checked: boolean) => void;
    onProductLineChange?: (tag: any, checked: boolean) => void;
}

const ProductLine: React.FC<ProductLineProps> = observer(props => {
    const {productLineStore} = StoreManager;
    const {onDepartmentChange, onProductLineChange} = props;

    const departments = productLineStore.getDepartments();
    const productLines = productLineStore.getProductLines();

    const selectedDepartments = get(productLineStore.selectedDepartments, "departmentId");
    const selectedProductLines = get(productLineStore.selectedProductLines, "productLineId");

    const handleDepartmentChange = (tag: any, checked: boolean) => {
        const nextSelectedTags = checked ? tag : null;
        productLineStore.setDepartmentId(tag.id);
        productLineStore.setSelectedDepartments(nextSelectedTags);
        productLineStore.setSelectedProductLines(null);
        onDepartmentChange && onDepartmentChange(tag, checked);
    };

    const handleProductLineChange = (tag: any, checked: boolean) => {
        const nextSelectedTags = checked ? tag : null;
        productLineStore.setSelectedProductLines(nextSelectedTags);
        onProductLineChange && onProductLineChange(tag, checked);
    };

    if (!departments || departments.length === 0) {
        return null;
    }

    return (
        <Descriptions bordered={true} column={1} size="small">
            <Descriptions.Item label="部门">
                {departments.map(tag => (
                    <Tag.CheckableTag
                        key={tag.id}
                        checked={selectedDepartments === tag.departmentId}
                        onChange={checked => handleDepartmentChange(tag, checked)}
                    >
                        <span style={{fontSize: 14}}>
                            {tag.departmentName} <span className="departments-count">[{tag.count}]</span>
                        </span>
                    </Tag.CheckableTag>
                ))}
            </Descriptions.Item>

            {productLines.length > 0 && (
                <Descriptions.Item label="子部门">
                    {productLines.map(tag => (
                        <Tag.CheckableTag
                            key={tag.id}
                            checked={selectedProductLines === tag.productLineId}
                            onChange={checked => handleProductLineChange(tag, checked)}
                        >
                            <span style={{fontSize: 14}}>
                                {tag.productLineName} <span className="departments-count">[{tag.count}]</span>
                            </span>
                        </Tag.CheckableTag>
                    ))}
                </Descriptions.Item>
            )}
        </Descriptions>
    );
});

export default ProductLine;