import React from "react";
import {Button, Tooltip} from "antd";
import {QuestionOutlined} from "@ant-design/icons/lib";

const HelpLink: React.FC = props => {
    const {location: {pathname}} = window;
    const path =
        (pathname.indexOf("/board/explorer/edit") > -1 && "/board/explorer") ||
        (pathname.indexOf("/board/view/") > -1 && "/board/view") ||
        pathname;

    const link = `https://monitor-doc.faas.elenet.me/manual/e-monitor${path}.html`;

    return (
        <Tooltip title="帮助文档">
            <Button href={link} target="_blank" shape="circle" icon={<QuestionOutlined />}/>
        </Tooltip>
    );
};

export default HelpLink;
