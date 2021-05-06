import React from "react";
import classNames from "classnames";
import {SectionClass} from "$components/EMonitorLayout/Section";
import {Card} from "antd";

export const SectionItemClass = `${SectionClass}-item`;
const SectionTabsItemClass = `${SectionClass}-item-tabs`;

type SectionItemType = "card" | "tabs" | "default";

interface SectionItemProps {
    width?: React.ReactText;
    scroll?: boolean;
    fullscreen?: boolean;
    children?: React.ReactNode;
    type?: SectionItemType;
    title?: React.ReactNode;
    className?: string;
    style?: React.CSSProperties;
    // 用于 Card 的 extra
    extra?: React.ReactNode;
}

const SectionItem: React.ForwardRefRenderFunction<HTMLDivElement, SectionItemProps> = (props, ref) => {
    const {children, width, type, title, className, extra} = props;
    const {scroll, fullscreen, style} = props; // item 样式相关

    const classString = classNames(SectionItemClass, {
        [`${SectionItemClass}-scroll`]: scroll,
        [`${SectionItemClass}-fullscreen`]: fullscreen,
    }, className);

    const tabsCardClassString = classNames(classString, SectionTabsItemClass, "tab-card");

    const finalStyle = style || {};

    width !== undefined && Object.assign(finalStyle,
        scroll ? {flex: `0 0 ${width}`} : {width}
    );

    switch (type) {
        case "card":
            return <Card size="small" title={title} className={classString} style={finalStyle} extra={extra}>{children}</Card>;
        case "tabs":
            return <Card size="small" title={title} className={tabsCardClassString} style={finalStyle} extra={extra}>{children}</Card>;
        case "default":
        default:
            return <div ref={ref} className={classString} style={finalStyle}>{children}</div>;
    }
};

export default React.forwardRef(SectionItem);
