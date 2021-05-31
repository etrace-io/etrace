import React from "react";
import {OPENAPI_SIDER} from "$constants/Route";
import {EMonitorPage} from "$components/EMonitorLayout";
import {Redirect, Route, Switch} from "react-router-dom";
import TokenApplyPage from "$containers/OpenAPI/TokenApplyPage";
import TokenManagePage from "$containers/OpenAPI/TokenManagePage";

const OpenAPIEntrance: React.FC = props => {
    return (
        <EMonitorPage sider={OPENAPI_SIDER}>
            <Switch>
                <Route path="/token/apply" exact={true} component={TokenApplyPage}/>
                <Route path="/token/manage" exact={true} component={TokenManagePage}/>
                <Redirect to="/token/apply"/>
            </Switch>
        </EMonitorPage>
    );
};

export default OpenAPIEntrance;
