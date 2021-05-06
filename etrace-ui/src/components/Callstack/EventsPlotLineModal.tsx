import React from "react";
import {Modal, Table} from "antd";
import {autobind} from "core-decorators";
import StoreManager from "../../store/StoreManager";
import {reaction} from "mobx";
import {isEmpty} from "$utils/Util";
import {get} from "lodash";
import {columnsAlert, columnsEvent} from "$constants/EventConstants";
import {AlertEventVO, ChangeEventVO} from "$models/HolmesModel";

interface Props {
}

interface Status {
    visible?: boolean;
    info?: AlertEventVO[] | ChangeEventVO[];
}

export default class EventsPlotLineModal extends React.Component<Props, Status> {
    disposer;

    constructor(props: Props) {
        super(props);
        this.state = {};
        this.disposer = reaction(
            () => StoreManager.chartEventStore.plotLineClickEvent,
            event => {
                if (isEmpty(event)) {
                    return;
                }
                this.setState({info: event, visible: true});
            });
    }

    componentWillUnmount(): void {
        if (this.disposer) {
            this.disposer();
        }
    }

    @autobind
    getContent() {
        if (this.state.info) {
            return (
                <Table
                    dataSource={this.state.info}
                    columns={get(this.state.info[0], "_type", "") == "change" ? columnsEvent : columnsAlert}
                    pagination={false}
                    rowKey={(record, index) => index.toString()}
                    size="small"
                />
            );
        }
    }

    @autobind
    handleClose() {
        this.setState({visible: false});
    }

    render() {
        return (
            <Modal
                footer={null}
                style={{top: 50}}
                width="90%"
                title={get(this.state.info, "[0]", []) instanceof ChangeEventVO ? "变更事件详情" : "报警事件详情"}
                visible={this.state.visible}
                onCancel={this.handleClose}
            >
                {this.getContent()}
            </Modal>
        );
    }
}
