import React from "react";
import {Popover, Tooltip} from "antd";
import {QuestionCircleOutlined} from "@ant-design/icons/lib";

interface TitleWithTooltipProps {
    title: string;
    tooltip?: React.ReactNode;
    popover?: boolean; // 默认 Tooltip，可选 popover
}

const TitleWithTooltip: React.FC<TitleWithTooltipProps> = props => {
    const {title, tooltip, popover} = props;
    const content = <span>{title} <QuestionCircleOutlined /></span>;

    if (popover) { return <Popover content={tooltip}>{content}</Popover>; }

    return <Tooltip title={tooltip}>{content}</Tooltip>;
};

export default TitleWithTooltip;
