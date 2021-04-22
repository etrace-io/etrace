import React from "react";
import {useHistory} from "react-router-dom";
import {APP_ROUTER, GLOBAL_SEARCH_PAGE} from "$constants/Route";
import EMonitorLogo from "$components/Base/EMonitorLogo";
import {EMonitorPage} from "$components/EMonitorLayout";
import GlobalSearchBox from "$components/GlobalSearchBox/GlobalSearchBox";
import FunctionItemCard from "$components/FunctionItemCard/FunctionItemCard";

import "./LandingPage.less";
import {getPrefixCls} from "$utils/Theme";
import {EMONITOR_LOGO_DARK, EMONITOR_LOGO_LIGHT, SEARCH_KEY} from "$constants/index";

const LandingPage: React.FC = props => {
    const history = useHistory();

    const handlerSearch = (value: string) => {
        if (!value) {
            return;
        }
        history.push(`${GLOBAL_SEARCH_PAGE}?${SEARCH_KEY}=${value}`);
    };

    const prefixCls = getPrefixCls("landing-page");

    return (
        <EMonitorPage className={prefixCls}>
            <div className="logo-container">
                <EMonitorLogo
                    link="/"
                    dark={EMONITOR_LOGO_DARK}
                    light={EMONITOR_LOGO_LIGHT}
                />
            </div>

            <GlobalSearchBox
                size="large"
                style={{margin: "0 auto"}}
                maxWidth={700}
                needSpacing={true}
                needLink={true}
                onSelect={handlerSearch}
            />

            <FunctionItemCard title="常用功能" dataSource={APP_ROUTER} column={5}/>
        </EMonitorPage>
    );
};

export default LandingPage;
