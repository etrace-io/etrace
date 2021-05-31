import {Layout} from "antd";
import React from "react";
import {getDashboardMenuData} from "./DashboardMenu";
import NavMenu from "../../components/NavMenu/NavMenu";
import {Redirect, Route, Switch} from "react-router-dom";

import DashboardView from "./DashboardView";
import DashboardNodeEditor from "./DashboardNodeEditor";
import DashboardNodeList from "./DashboardNodeList";
import DashboardGraphList from "./DashboardGraphList";
import DashboardGraphEditor from "./DashboardGraphEditor";

const Sider = Layout.Sider;

interface DashboardProps {
}

interface DashboardStatus {
}

export default class Dashboard extends React.Component<DashboardProps, DashboardStatus> {

    render() {
        const menu = getDashboardMenuData({});

        return (
            <Layout className="e-monitor-content">
                <Sider className="e-monitor-side-menu" breakpoint="xl" collapsible={true} collapsedWidth={60}>
                    <NavMenu mode="inline" menuData={menu}/>
                </Sider>
                <Switch>
                    <Route path="/dashboard/graph" exact={true} component={DashboardGraphList}/>
                    <Route path="/dashboard/graph/view/:id" exact={true} component={DashboardView}/>
                    <Route path="/dashboard/graph/new" exact={true} component={DashboardGraphEditor}/>
                    <Route path="/dashboard/graph/edit/:id" exact={true} component={DashboardGraphEditor}/>
                    <Route path="/dashboard/node" exact={true} component={DashboardNodeList}/>
                    <Route path="/dashboard/node/new" exact={true} component={DashboardNodeEditor}/>
                    <Route path="/dashboard/node/edit/:id" exact={true} component={DashboardNodeEditor}/>
                    <Redirect to="/dashboard/graph"/>
                </Switch>
            </Layout>
        );
    }
}
