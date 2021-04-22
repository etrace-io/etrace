import {Layout} from "antd";
import classNames from "classnames";
import React, {ReactNode} from "react";
import {getPrefixCls} from "$utils/Theme";
import {EMonitorFooter} from "$components/EMonitorLayout/index";

interface ContainerProps {
    className?: string;
    header?: ReactNode;
    headerFixed?: boolean;
    style?: React.CSSProperties;
    fullscreen?: boolean; // 高为 100 view height
    footer?: ReactNode; // 若为 false 则不需要 footer，默认为 EMonitorFooter
}

/**
 * 应用级别的 Container，其下可包含多个 Page
 */
const Container: React.FC<ContainerProps> = (props) => {
    const {className, style} = props;
    const {
        header,
        footer = <EMonitorFooter/>,
        fullscreen,
        headerFixed,
        children,
    } = props;

    const layoutPrefixCls = getPrefixCls("layout");
    const containerPrefixCls = getPrefixCls("container");

    const layoutClassString = classNames(layoutPrefixCls, {
        "header-fixed": headerFixed,
        fullscreen: fullscreen,
    });

    const containerCls = classNames(
        containerPrefixCls,
        {
            "has-footer": footer,
            // "has-header": header,
        },
        className
    );

    return (
        <Layout className={layoutClassString}>
            {header && header}
            <Layout.Content className={containerCls} style={style}>
                {children}
            </Layout.Content>
            {footer && footer}
        </Layout>
    );
};

export default Container;
