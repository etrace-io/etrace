import React from "react";
import {LoginError} from "$models/User";
import {Button, Divider, Typography} from "antd";
import {EMonitorContainer, EMonitorFooter} from "$components/EMonitorLayout";
import {DingdingOutlined, ExceptionOutlined, EyeOutlined, LinkOutlined,} from "@ant-design/icons/lib";

import "./LoginErrorPage.less";
import {ERROR_IMAGE} from "$constants/index";

const LoginErrorPage: React.FC<{
    error: LoginError;
}> = props => {
    const { error } = props;

    const { message, url, status } = error;

    return (
        <EMonitorContainer
            className="emonitor-login-error-page"
            fullscreen={true}
            footer={<EMonitorFooter background={false}/>}
        >
            <div className="error-page-container">
                <img alt="Login Error" className="login-error__image" src={ERROR_IMAGE} />

                {status === 403 && (
                    <div className="error-tips-container">
                        <div className="error-tips-content">❗️ 当前暂无权限访问，请点击下方按钮申请访问权限。</div>
                        <Button
                            type="primary"
                            icon={<EyeOutlined />}
                            href=""
                            target="_blank"
                        >权限申请
                        </Button>
                    </div>
                )}

                <Divider/>

                {url && (
                    <div className="login-error__content-item">
                        <p className="content-item__title">报错链接 <LinkOutlined /></p>
                        <p className="content-item__body">
                            <Typography.Paragraph copyable={true}>{url}</Typography.Paragraph>
                        </p>
                    </div>
                )}

                <div className="login-error__content-item">
                    <p className="content-item__title">报错内容 <ExceptionOutlined /></p>
                    <p className="content-item__body code">{message}</p>
                </div>

                <div className="login-error__content-item">
                    <p className="content-item__title">钉钉支持群 <DingdingOutlined style={{ color: "#008cee" }} /></p>
                    <div className="content-item__body">
                        <Typography.Paragraph copyable={true}>123456</Typography.Paragraph>
                    </div>
                </div>
            </div>
        </EMonitorContainer>
    );
};

export default LoginErrorPage;
