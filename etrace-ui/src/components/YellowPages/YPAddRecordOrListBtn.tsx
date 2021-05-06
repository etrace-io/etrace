import {PlusOutlined} from "@ant-design/icons/lib";
import React from "react";
import YPAddRecord from "./YPAddRecord";
import YpAddList from "./YPAddList";
import {Button, Popover} from "antd";
import IconFont from "$components/Base/IconFont";

const classnames = require("classnames");

interface YpAddRecordOrListBtnProps {
}

interface YpAddRecordOrListBtnStatus {
    addRecordModalVisible: boolean;
    addListModalVisible: boolean;
    popoverVisible: boolean;
}

export default class YpAddRecordOrListBtn extends React.Component<YpAddRecordOrListBtnProps, YpAddRecordOrListBtnStatus> {
    state = {
        addRecordModalVisible: false,
        addListModalVisible: false,
        favorite: null,
        popoverVisible: false,
    };

    handleToggleAddRecordModal = (status: boolean) => {
        this.setState({addRecordModalVisible: status, popoverVisible: false});
    };

    handleToggleAddListModal = (status: boolean) => {
        this.setState({addListModalVisible: status, popoverVisible: false});
    };

    handleVisibleChange = visible => {
        this.setState({
            popoverVisible: visible
        });
    };

    render() {
        const {addRecordModalVisible, addListModalVisible, popoverVisible} = this.state;

        const content = (
            <div className="global-add-btn__btn-group">
                <button key="record" onClick={() => this.handleToggleAddRecordModal(true)}>
                    <IconFont type="icon-yingjian"/> 收录
                </button>
                <button key="list" onClick={() => this.handleToggleAddListModal(true)}>
                    <IconFont type="icon-huowudui"/> 集合
                </button>
            </div>
        );

        const btnCls = classnames("global-add-btn__button", {
            active: popoverVisible,
        });

        return (
            <div className="e-monitor-yellow-pages__global-add-btn">
                <Popover
                    content={content}
                    overlayClassName="global-add-btn__overlay"
                    trigger="click"
                    visible={popoverVisible}
                    onVisibleChange={this.handleVisibleChange}
                >
                    <Button className={btnCls} icon={<PlusOutlined />}/>
                </Popover>

                <YPAddRecord
                    visible={addRecordModalVisible}
                    onOk={visible => this.handleToggleAddRecordModal(visible)}
                    onCancel={() => this.handleToggleAddRecordModal(false)}
                />

                <YpAddList
                    visible={addListModalVisible}
                    onOk={visible => this.handleToggleAddListModal(visible)}
                    onCancel={() => this.handleToggleAddListModal(false)}
                />
            </div>
        );
    }
}
