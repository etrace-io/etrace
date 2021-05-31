import {NO_CHART_DATA} from "$constants/index";
import {Empty} from "antd";
import React from "react";

import "./EmptyTips.less";

const EmptyTips: React.FC<{
    tips?: React.ReactNode;
    image?: React.ReactNode;
}> = props => {
    const {image, tips} = props;
    return (
        <Empty
            className="emonitor-empty-tips"
            image={image || NO_CHART_DATA}
            description={tips || "暂无图表数据"}
        />
    );
};

export default EmptyTips;
