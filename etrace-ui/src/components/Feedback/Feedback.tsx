import React from "react";
import {Link} from "react-router-dom";
import {getPrefixCls} from "$utils/Theme";
import {Button, Dropdown, Menu} from "antd";
import {ExclamationCircleOutlined, FormOutlined, ProfileOutlined,} from "@ant-design/icons/lib";

import "./Feedback.less";

const Feedback: React.FC = props => {
    const prefixCls = getPrefixCls("feedback");

    const content = (
        <Menu className={`${prefixCls}__menu`}>
            <Menu.Item>
                <a rel="noopener noreferrer" href="https://monitor-doc.faas.elenet.me" target="_blank">
                    <ProfileOutlined/> 帮助文档
                </a>
            </Menu.Item>
            <Menu.Item>
                <Link to="/feedback" target="_blank">
                    <FormOutlined/> 建议反馈
                </Link>
            </Menu.Item>
        </Menu>
    );

    return (
        <Dropdown overlay={content} placement="topRight">
            <Button
                className={prefixCls}
                size="small"
                icon={<ExclamationCircleOutlined/>}
                type="primary"
            >文档 / 反馈
            </Button>
        </Dropdown>
    );
};

export default Feedback;
