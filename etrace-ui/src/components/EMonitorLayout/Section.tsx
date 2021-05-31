import React from "react";
import classNames from "classnames";
import {getPrefixCls} from "$utils/Theme";
import {Card} from "antd";

export const SectionClass = getPrefixCls("section");
const SectionCardClass = `${SectionClass}-card`;

type ModeType = "vertical" | "horizontal";
type SectionType = "card" | "default";

interface SectionProps {
    fullscreen?: boolean;
    mode?: ModeType;
    scroll?: boolean;
    className?: string;
    children?: React.ReactNode;
    type?: SectionType;
    width?: React.ReactText;
    style?: React.CSSProperties;
}

/**
 * 布局块
 *
 * # 常用操作
 *
 * 顶部工具栏固定：
 *   在工具栏的父级 Section 添加 `fullscreen={true}`
 *   告诉 EMonitor Section 该部分为全屏状态，需注意该状态为 `height: 100%`
 *   然后再需要滚动的 EMonitor Section Item 上添加 `scroll={true}` 令其可滚动。
 */
const Section: React.ForwardRefRenderFunction<HTMLDivElement, SectionProps> = (props, ref) => {
    const {
        fullscreen,
        scroll,
        children,
        mode = "vertical",
        className,
        type,
        style,
        width
    } = props;

    const classString = classNames(SectionClass, {
        [`${SectionClass}-fullscreen`]: fullscreen,
        [`${SectionClass}-scroll`]: scroll,
    }, `${SectionClass}-${mode}`, className);

    const cardClassString = classNames(classString, SectionCardClass);

    const finalStyle = style || {};

    width !== undefined && Object.assign(finalStyle,
        scroll ? {flex: `0 0 ${width}`} : {width}
    );

    switch (type) {
        case "card":
            return <Card className={cardClassString} style={finalStyle}>{children}</Card>;
        case "default":
        default:
            return <div ref={ref} className={classString} style={finalStyle}>{children}</div>;
    }
};

export default React.forwardRef(Section);
