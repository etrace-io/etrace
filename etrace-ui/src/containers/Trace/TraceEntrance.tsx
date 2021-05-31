import {reaction} from "mobx";
import {observer} from "mobx-react";
import {Button, Tooltip} from "antd";
import {TRACE_SIDER} from "$constants/Route";
import StoreManager from "$store/StoreManager";
import React, {useEffect, useState} from "react";
import IconFont from "$components/Base/IconFont";
import EMonitorMeta from "$components/Base/EMonitorMeta";
import {Link, Redirect, Route, Switch} from "react-router-dom";
import {TIME_FROM, TIME_TO, TIMESHIFT} from "$models/TimePickerModel";
import {EMonitorPage, EMonitorSection} from "$components/EMonitorLayout";
import EntranceToolBar from "$components/EntranceToolBar/EntranceToolBar";
import * as BasicInformationService from "$services/BasicInformationService";

/* Page */
import TraceOverviewPage from "$containers/Trace/pages/TraceOverviewPage";
import EventPage from "$containers/Trace/pages/EventPage";
import TransactionPage from "$containers/Trace/pages/TransactionPage";
import ExceptionPage from "$containers/Trace/pages/ExceptionPage";
import SOAProviderPage from "$containers/Trace/pages/SOAProviderPage";
import SOAConsumerPage from "$containers/Trace/pages/SOAConsumerPage";
import SOADependencyPage from "$containers/Trace/pages/SOADependencyPage";
import SOAPizzaPage from "$containers/Trace/pages/SOAPizzaPage";
import TraceURLPage from "$containers/Trace/pages/TraceURLPage";
import TraceJVMPage from "$containers/Trace/pages/TraceJVMPage";
import TraceRedisPage from "$containers/Trace/pages/TraceRedisPage";
import RMQPublisherPage from "$containers/Trace/pages/RMQPublisherPage";
import RMQConsumerPage from "$containers/Trace/pages/RMQConsumerPage";
import {APP_ID, EZONE, STORAGE_KEY_APP_ID} from "$constants/index";

const TraceEntrance: React.FC = props => {
    const {urlParamStore, eventStore} = StoreManager;

    const [appId, setAppId] = useState<string>(() => urlParamStore.getValue(APP_ID));

    useEffect(() => {
        const disposer = reaction(
            () => urlParamStore.getValue(APP_ID),
            value => setAppId(value),
            {fireImmediately: true}
        );

        return () => disposer();
    }, []);

    useEffect(() => {
        return () => eventStore.clearAll();
    }, []);

    const params = {
        [APP_ID]: appId,
        ezone: urlParamStore.getValues(EZONE),
        from: urlParamStore.getValue(TIME_FROM),
        to: urlParamStore.getValue(TIME_TO),
        timeshift: urlParamStore.getValue(TIMESHIFT)
    };

    return (
        <EMonitorPage sider={TRACE_SIDER} menuParams={params}>
            <EMonitorMeta title="应用"/>

            <EMonitorSection fullscreen={true}>
                <EMonitorSection.Item>
                    <TraceToolbar appId={appId}/>
                </EMonitorSection.Item>

                <Switch>
                    <Route path="/trace/overview" exact={true} component={TraceOverviewPage}/>
                    <Route path="/trace/transaction" exact={true} component={TransactionPage}/>
                    <Route path="/trace/event" exact={true} component={EventPage}/>
                    <Route path="/trace/exception" exact={true} component={ExceptionPage}/>

                    <Route path="/trace/soa/provider" exact={true} component={SOAProviderPage}/>
                    <Route path="/trace/soa/consumer" exact={true} component={SOAConsumerPage}/>
                    <Route path="/trace/soa/dependency" exact={true} component={SOADependencyPage}/>
                    <Route path="/trace/soa/pizza" exact={true} component={SOAPizzaPage}/>

                    <Route path="/trace/url" exact={true} component={TraceURLPage}/>
                    <Route path="/trace/jvm" exact={true} component={TraceJVMPage}/>
                    <Route path="/trace/redis" exact={true} component={TraceRedisPage}/>
                    <Route path="/trace/rmq_publish" exact={true} component={RMQPublisherPage}/>
                    <Route path="/trace/rmq_consumer" exact={true} component={RMQConsumerPage}/>
                    <Redirect to="/trace/overview"/>
                </Switch>
            </EMonitorSection>
        </EMonitorPage>
    );
};

const TraceToolbar: React.FC<{
    appId?: string;
}> = props => {
    const {appId} = props;

    return (
        <EntranceToolBar
            title="App ID"
            dataSource={BasicInformationService.getConsoleAppIds}
            urlKey={APP_ID}
            placeholder="请输入 App ID"
            storageKey={STORAGE_KEY_APP_ID}
        >
            {appId && (
                <>
                    <Tooltip title="该应用服务器相关监控" color="blue">
                        <Link to={`/system/dashboard?hostgroup=${appId}`} target="_blank">
                            <Button icon={<IconFont type="icon-server"/>}/>
                        </Link>
                    </Tooltip>
                </>
            )}
        </EntranceToolBar>
    );
};

export default observer(TraceEntrance);
