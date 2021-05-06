import React from "react";
import {observer} from "mobx-react";
import {Badge, Table, Tooltip} from "antd";
import {ColumnProps} from "antd/lib/table";
import {action, observable, runInAction} from "mobx";
import {SearchApiService} from "$services/SearchApiService";
import {OrderOrShipInfo} from "$models/SearchModel";
import {SIMPLE_TIME_FORMAT_WITH_MILLSECOND} from "$models/TimePickerModel";
import {CallstackStore} from "$store/CallstackStore";
import {isEmpty} from "$utils/Util";
import {DataFormatter} from "$utils/DataFormatter";
import StoreManager from "../../store/StoreManager";
import moment from "moment";
import {QuestionCircleOutlined} from "@ant-design/icons/lib";

interface Props {
    id: string;
    // type 1 for Order; type 2 = Ship
    type: string;
}

interface State {
}

@observer
export class OrderOrShipSearchList extends React.Component<Props, State> {
    callstackStore: CallstackStore;
    @observable
    private infos: Array<OrderOrShipInfo> = null;
    private previous: string = null;
    @observable
    private loading: boolean = false;

    @action
    public loadRequestId(id: string): void {
        if (id != this.previous) {
            this.previous = id;
            this.loading = true;

            if (this.props.type == "1") {
                SearchApiService.queryOrderId(id).then(data => {
                    if (data) {
                        runInAction(() => {
                            this.infos = data;
                            this.loading = false;
                        });
                    } else {
                        runInAction(() => {
                            this.infos = [];
                            this.loading = false;
                        });
                    }
                });
            } else if (this.props.type == "2") {
                SearchApiService.queryShipId(id).then(data => {
                    if (data) {
                        runInAction(() => {
                            this.infos = data;
                            this.loading = false;
                        });
                    } else {
                        runInAction(() => {
                            this.infos = [];
                            this.loading = false;
                        });
                    }
                });
            }
        }
    }

    constructor(props: Props) {
        super(props);
        this.callstackStore = StoreManager.callstackStore;
        this.loadRequestId(props.id);
    }

    componentWillReceiveProps(nextProps: Readonly<Props>, nextContext: any): void {
        this.loadRequestId(nextProps.id);
    }

    handleModalShow(url: string) {
        this.callstackStore.startToQuerySampling();
        this.callstackStore.callstackShowHead = true;
        this.callstackStore.loadCallstack(url, null);
    }

    render() {
        const hasData = !isEmpty(this.infos);
        let appids = new Set();
        let ezones = new Set();
        if (hasData) {
            for (let i = 0; i < this.infos.length; i++) {
                appids.add(this.infos[i].appId);
                ezones.add(this.infos[i].ezone);
            }
        }
        let appidFilters = [];
        let ezoneFilters = [];
        appids.forEach((item) => {
            appidFilters.push({"text": item, "value": item});
        });
        ezones.forEach((item) => {
            ezoneFilters.push({"text": item, "value": item});
        });
        let columns: ColumnProps<OrderOrShipInfo>[] = [
            {
                title: <Tooltip title="当前服务被调用起始时间"><span>时间<QuestionCircleOutlined/></span></Tooltip>,
                key: "timestamp",
                width: 155,
                render: (text: any, item: OrderOrShipInfo, index: number) => {
                    return (moment(item.timestamp).format(SIMPLE_TIME_FORMAT_WITH_MILLSECOND));
                },
                sorter: (a, b) => b.timestamp - a.timestamp
            },
            {
                title: <Tooltip title="当前服务调用耗时"><span>响应时间<QuestionCircleOutlined/></span></Tooltip>,
                width: 100,
                key: "duration",
                render: (text: any, item: OrderOrShipInfo) => {
                    return DataFormatter.transformMilliseconds(item.duration);
                }
            },
            {
                title: "EZone",
                width: 80,
                dataIndex: "ezone",
                align: "left",
                filters: ezoneFilters,
                onFilter: (value, item: OrderOrShipInfo) => item.ezone.indexOf(value.toString()) === 0,
            },
            {
                title: "AppID",
                dataIndex: "appId",
                onFilter: (value, item: OrderOrShipInfo) => item.appId.indexOf(value.toString()) === 0,
                filters: appidFilters,
            },
            {
                title: "流程",
                key: "process",
                render: (text: any, item: OrderOrShipInfo, index: number) => {
                    return (
                        <a onClick={() => this.handleModalShow(item.sampling)}>{item.process}</a>
                    );
                }
            },
            {
                title: "状态",
                key: "status",
                align: "center",
                width: 50,
                render: (text: any, item: OrderOrShipInfo, index: number) => {
                    if (item.status == "0") {
                        return (<Badge status="success"/>);
                    } else {
                        return (<Badge status="error"/>);
                    }
                }
            }
        ];

        return (
            <Table
                loading={this.loading}
                columns={columns}
                dataSource={this.infos}
                size="small"
                bordered={true}
                pagination={false}
                className="order-ship-table"
                rowKey={(item: OrderOrShipInfo, index: number) => "" + index}
            />
        );
    }
}
