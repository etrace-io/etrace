import React from "react";
import {ENV} from "$constants/Env";
import {SystemKit} from "$utils/Util";
import {Button, Divider, Space} from "antd";
import EMonitorLogo from "$components/Base/EMonitorLogo";
import {EMonitorContainer, EMonitorFooter} from "$components/EMonitorLayout";

import "./LoginPage.less";
import {EMONITOR_LOGO_DARK, EMONITOR_LOGO_LIGHT, EMPTY_BACKGROUND} from "$constants/index";

const ETRACE_LOGIN = (
    <Button key="etrace" className="etrace-login" size="large" onClick={() => SystemKit.redirectToSSO()}>
        Sign in
    </Button>
);

const LOGIN_BUTTON = {
    [ENV.PROD]: [ETRACE_LOGIN],
    [ENV.TEST]: [ETRACE_LOGIN],
};

const LoginPage: React.FC = props => {
    const currEnv = SystemKit.getCurrEnv();

    return (
        <EMonitorContainer
            className="emonitor-login-page"
            fullscreen={true}
            style={{background: `url(${EMPTY_BACKGROUND})`}}
            footer={<EMonitorFooter background={false}/>}
        >
            <EMonitorLogo
                height={250}
                dark={EMONITOR_LOGO_DARK}
                light={EMONITOR_LOGO_LIGHT}
            />

            <p className="e-monitor-intro">
                <span className="hl">ETrace</span> 一站式监控系统</p>

            <div className="login-btn-group">
                <Space direction="vertical" split={<Divider>或</Divider>}>
                    {LOGIN_BUTTON[currEnv]}
                </Space>
            </div>
        </EMonitorContainer>
    );
};

export default LoginPage;
