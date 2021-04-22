import React from "react";
import {Card, Layout} from "antd";
import classNames from "classnames";
import Feedback from "../Feedback/Feedback";
import {getPrefixCls} from "$utils/Theme";

interface EMonitorFooterProps {
    className?: string;
    title?: string;
    background?: boolean; // 背景是否透明，指定 false 为透明，默认纯色背景
}

const Footer: React.FC<EMonitorFooterProps> = props => {
    const currYear = new Date();

    const {
        className,
        title = `Copyright © 2021~${currYear.getFullYear()} ETrace Team`,
        background,
    } = props;

    const prefixCls = getPrefixCls("footer");
    const classString = classNames(prefixCls, className, {
        "no-bg": background === false,
    });

    return (
        <Layout.Footer className={classString}>
            <Card>
                <span className={`${prefixCls}-content`}>{title}</span>
                <Feedback />
            </Card>
        </Layout.Footer>
    );
};

export default Footer;
