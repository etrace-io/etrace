import React from "react";
import {Route, useLocation} from "react-router-dom";
import BoardViewPage from "$containers/Board/BoardViewPage";
import {EMonitorContainer, EMonitorPage} from "$components/EMonitorLayout";

const ShareBoard: React.FC = props => {
    const location = useLocation();
    const params = new URLSearchParams(location.search);

    const showName = params.get("showName") === "true";
    const hideToolsBar = params.get("hideToolsBar") === "true";
    const needFullScreen = params.get("needFullScreen") === "true";

    const Board = (
        <BoardViewPage
            hideStarButton={true}
            hideEditButton={true}
            hideBoardChoose={true}
            hideShareButton={true}
            showBoardName={showName}
            hideToolsBar={hideToolsBar}
            hideFullScreenButton={!needFullScreen}
        />
    );

    return (
        <EMonitorContainer fullscreen={true}>
            <EMonitorPage>
                <Route
                    path="/board/share/:boardId"
                    exact={false}
                    component={(_) => Board}
                />
            </EMonitorPage>
        </EMonitorContainer>
    );
};

export default ShareBoard;
