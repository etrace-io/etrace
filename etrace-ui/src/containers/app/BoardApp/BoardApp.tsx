import {Button} from "antd";
import {observer} from "mobx-react";
import React, {useEffect} from "react";
import BoardViewPage from "../../Board/BoardViewPage";
import StoreManager from "$store/StoreManager";
import EMonitorNav from "$components/Nav/EMonitorNav";
import EMonitorMeta from "$components/Base/EMonitorMeta";
import Exception from "$components/Exception/Exception";
import useWatchChartSeriesClick from "$hooks/useWatchChartSeriesClick";
import EMonitorLoading from "$components/EMonitorLoading/EMonitorLoading";
import {Link, Route, useHistory, useLocation, useParams} from "react-router-dom";
import {EMonitorContainer, EMonitorPage, EMonitorSection} from "$components/EMonitorLayout";

import "./BoardApp.less";

const BoardApp: React.FC = props => {
    const {dataAppStore} = StoreManager;
    const {dataAppId} = useParams();
    const location = useLocation();
    const history = useHistory();

    useWatchChartSeriesClick();

    const actions = (
        <div>
            <Button htmlType="button">
                <Link to={"/board/list"}>返回首页</Link>
            </Button>
            <Button htmlType="button" type="primary">
                <Link to={"/board/app/edit/" + dataAppId}>添加面板</Link>
            </Button>
        </div>
    );

    useEffect(() => {
        dataAppStore.current = location.pathname;
        dataAppStore.register(dataAppId, history);

        return () => {
            dataAppStore.init();
        };
    }, []);

    const {title, description} = dataAppStore.dataApp || {};

    const header = <EMonitorNav menu={[]}><span className="board-app-title">{title}</span></EMonitorNav>;

    return (
        <EMonitorContainer header={header} fullscreen={true} headerFixed={true}>
            <EMonitorMeta title={title} description={description}/>

            <EMonitorPage>
                {dataAppStore.initialize
                    ? <EMonitorLoading tip="Data App Initializing..."/>
                    : <EMonitorSection fullscreen={true}>
                        {dataAppStore.dashboards && (dataAppStore.dashboards.length > 0
                            ? <Route path="/app/:dataApp/board/:boardId" component={BoardViewPage}/>
                            : <Exception type="202" desc="没有添加任何面板" actions={actions}/>
                        )}
                    </EMonitorSection>
                }
            </EMonitorPage>
        </EMonitorContainer>
    );
};

export default observer(BoardApp);
