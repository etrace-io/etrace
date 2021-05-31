import React from "react";
import {Card, Col} from "antd";

import "./InfoPart.css";

interface InfoPartProps {
    title: string | React.ReactNode;
    span?: number;
    info?: Array<string>;
}

export const InfoPart: React.FC<InfoPartProps> = props => {
    return (
        <Col span={props.span || 8}>
            <Card className="meta-info-part-card" title={props.title} bodyStyle={{padding: "0 15px"}}>
                {props.info && props.info.map((info, index) => {
                    return (<div key={index} className="meta-info-part-item">{info}</div>);
                })}
                {(!props.info || !props.info.length) && (
                    <div className="meta-info-part-no-data">暂无数据</div>
                )}
            </Card>
        </Col>
    );
};
