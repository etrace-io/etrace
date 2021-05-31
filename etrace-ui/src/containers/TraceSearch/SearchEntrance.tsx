import React from "react";
import {SEARCH_SIDER} from "$constants/Route";
import {EMonitorPage} from "$components/EMonitorLayout";
import {Redirect, Route, Switch} from "react-router-dom";
import EMonitorMeta from "$components/Base/EMonitorMeta";

// Page
import RequestIdSearchPage from "$containers/TraceSearch/RequestIdSearchPage";
import OrderSearchPage from "$containers/TraceSearch/OrderSearchPage";

const SearchEntrance: React.FC = props => {
    return (
        <EMonitorPage sider={SEARCH_SIDER}>
            <EMonitorMeta title="搜索"/>

            <Switch>
                <Route path="/search/request" exact={true} component={RequestIdSearchPage}/>
                <Route path="/search/order" exact={true} component={OrderSearchPage}/>
                <Redirect to="/search/request"/>
            </Switch>
        </EMonitorPage>
    );
};

export default SearchEntrance;
