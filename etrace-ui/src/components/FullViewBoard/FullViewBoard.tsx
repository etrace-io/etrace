import React from "react";
import {useParams} from "react-router-dom";
import EditableBoard from "../Board/EditableBoard";
import useWatchChartSeriesClick from "$hooks/useWatchChartSeriesClick";
import {EMonitorContainer, EMonitorPage, EMonitorSection} from "$components/EMonitorLayout";

export const FullScreenBoardView: React.FC = props => {
    useWatchChartSeriesClick();

    const {globalId} = useParams();

    return (
        <EMonitorContainer fullscreen={true}>
            <EMonitorPage>
                <EMonitorSection fullscreen={true} scroll={true}>
                    <EditableBoard globalId={globalId}/>
                </EMonitorSection>
            </EMonitorPage>
        </EMonitorContainer>
    );
};
