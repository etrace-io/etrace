import React from "react";
import {SETTING_SIDER} from "$constants/Route";
import {EMonitorPage} from "$components/EMonitorLayout";
import {Redirect, Route, Switch} from "react-router-dom";
import EMonitorMeta from "$components/Base/EMonitorMeta";
import SettingDataSourcePage from "./DataSource/SettingDataSource";
import SettingMonitorEntityPage from "$containers/Setting/MonitorEntity/SettingMonitorEntity";

const SettingEntrance: React.FC = props => {
    return (
        <EMonitorPage sider={SETTING_SIDER}>
            <EMonitorMeta title="设置" />

            <Switch>
                <Route path="/setting/datasource" exact={true} component={SettingDataSourcePage}/>
                <Route path="/setting/entity" exact={true} component={SettingMonitorEntityPage}/>
                <Redirect to="/setting/datasource"/>
            </Switch>
        </EMonitorPage>
    );
};

export default SettingEntrance;
