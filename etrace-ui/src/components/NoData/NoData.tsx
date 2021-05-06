import React from "react";

import "./NoData.css";
import {ExclamationCircleOutlined} from "@ant-design/icons/lib";

interface NoDataProps {
    message?: string;
    style?: React.CSSProperties;
}

const NoData: React.FC<NoDataProps> = props => {
    return (
        <div className="m-noData" style={props.style}>
            <div style={{lineHeight: "280px"}}><ExclamationCircleOutlined /> {props.message ? props.message : "暂无数据!"}</div>
        </div>
    );
};

export default NoData;
