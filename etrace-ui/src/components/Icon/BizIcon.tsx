import React from "react";

interface BizIconProps {
    // code from http://iconfont.cn/, like: "icon-org"
    type: string;
    style?: any;
    className?: string;
}

export const BizIcon: React.FC<BizIconProps> = props => {
    const cls = props.className || "";
    return (<i className={`iconfont ${props.type} ${cls}`} style={props.style}/>);
};

export const BizMenuIcon: React.FC<BizIconProps> = props => {
    return (<i className={`iconfont prefer-iconfont ${props.type}`} style={{marginRight: "10px"}}/>);
};
