import {Row} from "antd";
import React from "react";
import classNames from "classnames";
import {getPrefixCls} from "$utils/Theme";
import {SPACE_BETWEEN} from "$constants/index";

export const GalleryClass = getPrefixCls("gallery");

interface EMonitorGalleryProps {
    className?: string;
    style?: React.CSSProperties;
}

/**
 * 主要用于放置不规则布局
 * 如：Chart 列表，由于其根据 Col 进行折行放置，水平间隔距离无法控制
 */
const Gallery: React.FC<EMonitorGalleryProps> = props => {
    const {
        className,
        style,
        children,
    } = props;

    const classString = classNames(GalleryClass, className, {
    });

    return (
        <div className={`${GalleryClass}-container`} style={style}>
            <Row className={classString} gutter={SPACE_BETWEEN}>
                {children}
            </Row>
        </div>
    );
};

export default Gallery;
