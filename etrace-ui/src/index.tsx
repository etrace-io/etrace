import "./wdyr";

import React from "react";
import * as ReactDOM from "react-dom";
import MonitorApp from "./App";
import "antd/dist/antd.less";
// import "antd/dist/antd.dark.less";
import "./styles/theme.less";
import "./components/EMonitorLayout/style/index.less";
// import "./index.css";
import * as serviceWorker from "./serviceWorker";
// 本地化
import {ConfigProvider} from "antd";
import zh_CN from "antd/es/locale/zh_CN";
import {Router} from "react-router-dom";
import {browserHistory} from "$utils/UtilKit/SystemKit";
import {QueryClientProvider} from "react-query";
import {queryClient} from "$services/http";
// import "moment/locale/zh-cn";

ReactDOM.render(
    <ConfigProvider locale={zh_CN}>
        <QueryClientProvider client={queryClient}>
            <Router history={browserHistory}>
                <MonitorApp/>
            </Router>
        </QueryClientProvider>
    </ConfigProvider>,
    document.getElementById("main") as HTMLElement
);
// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
