import React from "react";
import {Layout} from "antd";
import classNames from "classnames";
import {MenuItem} from "$models/Menu";
import {getPrefixCls} from "$utils/Theme";
import SiderBar, {MenuParams, MenuParamsFn} from "$components/Base/SiderBar";

interface PageOptions {
    scroll?: boolean;
    footer?: boolean;
    flex?: boolean;
}

interface PageProps {
    className?: string;
    siderClassName?: string;
    contentClassName?: string;
    sider?: MenuItem[];
    options?: PageOptions;
    menuParams?: MenuParams | MenuParamsFn;
}

const Page: React.FC<PageProps> = props => {
    const { className, siderClassName, contentClassName } = props;
    const { sider, children, menuParams } = props;

    const pagePrefixCls = getPrefixCls("page");
    const siderPrefixCls = getPrefixCls("sider");
    const contentPrefixCls = getPrefixCls("content");

    const pageCls = classNames(pagePrefixCls, className);
    const siderCls = classNames(siderPrefixCls, siderClassName);
    const contentCls = classNames(contentPrefixCls, contentClassName);

    return (
        <Layout className={pageCls}>
            {sider && <SiderBar menu={sider} menuParams={menuParams} className={siderCls}/>}

            <Layout.Content className={contentCls}>
                {children}
            </Layout.Content>
        </Layout>
    );
};

export default Page;
