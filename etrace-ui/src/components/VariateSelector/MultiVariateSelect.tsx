import React from "react";
import {get} from "lodash";
import {Space} from "antd";
import {Variate} from "$models/BoardModel";
import VariateSelector from "$components/VariateSelector/VariateSelector";

const MultiVariateSelect: React.FC<{
    variates: Variate[];
    onLoadValues?: (variate: Variate, searchValue?: string) => string[] | Promise<string[]>; // 额外数据源
}> = props => {
    const {variates, onLoadValues} = props;

    if (!variates || variates.length === 0) {
        return null;
    }

    return (
        <Space size={16}>
            {variates.map((variate, index) => {
                return (
                    <VariateSelector
                        key={index}
                        variate={variate}
                        onLoadValues={onLoadValues}
                    />
                );
            })}
        </Space>
    );
};

export default MultiVariateSelect;
