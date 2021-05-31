import React from "react";
import {observer} from "mobx-react";
import {autobind} from "core-decorators";
import {CallstackStore} from "../../store/CallstackStore";
import StoreManager from "../../store/StoreManager";
import {Modal, Table} from "antd";
import {uniqueId} from "../../utils/Util";
import {ColumnProps} from "antd/lib/table";

@observer
export class ConsumerModal extends React.Component<any, any> {

    callstackStore: CallstackStore;

    constructor(props: any) {
        super(props);
        this.callstackStore = StoreManager.callstackStore;
    }

    @autobind
    handleCancel() {
        this.callstackStore.clearConsumers();
    }

    render() {
        const columns: ColumnProps<any>[] = [{
            title: "AppId",
            dataIndex: "appId",
            key: "appId"
        }, {
            title: "MessageId",
            dataIndex: "msgId",
            key: "msgId",
            width: "160px",
            render: (text) => {
                return <a rel="noopener noreferrer" target="_blank" href={"/requestId/" + text}>Consume Transaction</a>;
            },
        }, {
            title: "Queue",
            dataIndex: "queue",
            key: "queue"
        }, {
            title: "Exchange",
            dataIndex: "exchange",
            key: "exchange",
        }, {
            title: "Routing",
            dataIndex: "routing",
            key: "routing",
        }, {
            title: "Server",
            dataIndex: "server",
            key: "server"
        }];
        const consumers = this.callstackStore.getConsumers();
        return (
            <Modal
                destroyOnClose={true}
                footer={null}
                width={"90%"}
                bodyStyle={{padding: "0px"}}
                visible={this.callstackStore.showConsumers}
                onCancel={this.handleCancel}
                title="Consumers"
                style={{top: 50}}
            >
                <Table
                    className="table-background"
                    dataSource={consumers}
                    size="small"
                    rowKey={() => {
                        return uniqueId().toString();
                    }}
                    locale={{emptyText: this.callstackStore.consumersEmptyText}}
                    loading={this.callstackStore.consumersLoading}
                    columns={columns}
                    pagination={false}
                />
            </Modal>
        );
    }
}
