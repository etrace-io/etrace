import React from "react";
import classNames from "classnames";
import {GalleryClass} from "$components/EMonitorLayout/Gallery/Gallery";
import {Col} from "antd";

export const GalleryItemClass = `${GalleryClass}-item`;

interface EMonitorGalleryItemProps {
    className?: string;
    span?: string | number;
}

const GalleryItem: React.FC<EMonitorGalleryItemProps> = props => {
    const {
        className,
        children,
        span,
    } = props;

    const classString = classNames(GalleryItemClass, className, {
    });

    if (span) {
        return <Col span={span} className={classString}>{children}</Col>;
    }

    return (
        <div className={classString}>
            {children}
        </div>
    );
};

export default GalleryItem;
