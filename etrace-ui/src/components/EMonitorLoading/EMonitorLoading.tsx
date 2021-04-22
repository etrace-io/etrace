import React from "react";
import {Spin} from "antd";

import "./EMonitorLoading.less";

const EMonitorLoading: React.FC<{
    tip?: string;
}> = props => {
    const {tip} = props;

    return <Spin className="emonitor-loading" size="large" tip={tip || "加载中"}/>;
};

export default EMonitorLoading;
