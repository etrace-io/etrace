import React from "react";
import {Tooltip} from "antd";
import classNames from "classnames";
import {getPrefixCls} from "$utils/Theme";

import "./Duration.less";

export const DurationClass = getPrefixCls("duration");

const Duration: React.FC<{
    offset?: number;    // 0 ~ 100，偏移量
    duration?: number;  // 0 ~ 100，跨度
    tooltip?: React.ReactNode;
    width?: number; // 时间轴的宽度，默认 120
    color?: string; // 时间轴颜色
    contrast?: boolean;     // 是否使用反差色（在 duration 上的颜色进行反差）
    contrastColor?: string; // TODO
}> = props => {
    const {offset, duration, tooltip, width, color, contrast, children} = props;

    const timelineStyle: React.CSSProperties = {
        left: `${offset}%`,
        width: `${duration}%`,
        background: color,
    };

    const contentStyle: React.CSSProperties = {
        backgroundPositionX: `${(offset / 100 * (width || 120)).toFixed(2)}px`,
        backgroundSize: `${duration}% 100%`,
    };

    const contentClassString = classNames(`${DurationClass}-content`, {
        contrast
    });

    const durationLine = (
        <div className={DurationClass} style={{width}}>
            <div className={`${DurationClass}-timeline`} style={timelineStyle} />
            <div className={contentClassString} style={contentStyle}>{children}</div>
        </div>
    );

    if (tooltip) {
        return <Tooltip overlay={tooltip}>{durationLine}</Tooltip>;
    }

    return durationLine;
};

export default Duration;
