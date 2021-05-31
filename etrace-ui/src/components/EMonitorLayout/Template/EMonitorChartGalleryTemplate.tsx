import React from "react";
import {EMonitorGallery} from "$components/EMonitorLayout";
import EditableChart from "$components/EMonitorChart/Chart/EditableChart/EditableChart";

interface GalleryChart {
    globalId: string;
    span?: number;
}

const EMonitorChartGalleryTemplate: React.FC<{
    datSource: GalleryChart[]
    style?: React.CSSProperties;
}> = props => {
    const {datSource = [], style} = props;
    return (
        <EMonitorGallery style={style}>
            {datSource.map(chart => <EditableChart key={chart.globalId} globalId={chart.globalId} span={chart.span} />)}
        </EMonitorGallery>
    );
};

export default EMonitorChartGalleryTemplate;
