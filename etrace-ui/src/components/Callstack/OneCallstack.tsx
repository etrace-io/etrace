import React from "react";
import {Callstack, KnownMessageType, Message, MessageClassType, Transaction} from "$models/CallstackModel";
import {Col, notification, Row, Tooltip} from "antd";
import {SIMPLE_TIME_FORMAT_WITH_MILLSECOND} from "$models/TimePickerModel";
import {CallstackService} from "$services/CallstackService";
import {autobind} from "core-decorators";
import {default as OneTransactionDetail, TagCard} from "./OneTransactionDetail";
import {observer} from "mobx-react";
import {CallstackStore} from "$store/CallstackStore";
import StoreManager from "../../store/StoreManager";
import {DataFormatter} from "$utils/DataFormatter";
import CallstackTooltipChart from "../Chart/CallstackTooltipChart";
import RpcIdUtil from "../../utils/RpcIdUtil";
import moment from "moment";
import {FileTextOutlined, LoadingOutlined, ShrinkOutlined} from "@ant-design/icons/lib";

interface Props {
}

interface State {
    callstack?: Callstack;
}

@observer
export default class OneCallstack extends React.Component<Props, State> {

    callstackStore: CallstackStore;

    private leftMarginStep: number = 10; // 5px
    private rpcId: string; // "1.2.3"
    private requestId: string;

    private parentDuration: number;
    private parentTimestamp: number;

    constructor(props: Props) {
        super(props);
        this.callstackStore = StoreManager.callstackStore;
        this.state = {callstack: this.callstackStore.callstack};
    }

    shouldShowDrillUp(isRoot: boolean) {
        return isRoot && (this.rpcId.indexOf(".") > 0 || this.rpcId.indexOf("^") > 0);
    }

    shouldShowDrillDown(children: Array<Message>) {
        if (children) {
            for (let child of children) {
                if (child.type === KnownMessageType.ETraceLink && child.name !== "AsyncCall" && child.name !== "Truncate") {
                    return true;
                }
            }
        }
        return false;
    }

    showDrillUp(message: Message) {
        let fatherRpcId = RpcIdUtil.parseFatherRpcId(this.rpcId);
        message.isLoading = true;
        this.forceUpdate();
        let fatherMsgId = this.requestId + "$$" + fatherRpcId;
        CallstackService.queryRequestId(fatherMsgId, null, this.notifyAboutFailureInDrillUpOrDrillDown)
            .then(callstack => {
                message.isLoading = false;
                let foundRemoteLink = false;
                const children = (callstack.message as Transaction).children;
                if (children) {
                    if (callstack.message._type == MessageClassType.TRANSACTION) {
                        foundRemoteLink = this.traverseFindAndSetCurrentMessage(children, this.state.callstack.message);
                    }
                    // 还会有上层callstack中没有remote link的情形
                    // 直接append到children之后
                    if (!foundRemoteLink) {
                        children.push(this.state.callstack.message);
                    }
                } else {
                    // 针对上层中无children的
                    (callstack.message as Transaction).children = [this.state.callstack.message];
                }

                this.setState({callstack});
            }).catch(error => {
            message.isError = true;
            this.setState({callstack: this.state.callstack});
        });
    }

    traverseFindAndSetCurrentMessage(children: Array<Message>, currentMessage: Message): boolean {
        if (children) {
            for (let index = 0; index < children.length; index++) {
                let child = children[index];
                if (child.type == KnownMessageType.ETraceLink && child.data == this.rpcId) {
                    children[index] = currentMessage;
                    return true;
                } else if (child._type == MessageClassType.TRANSACTION) {
                    let childResult: boolean = this.traverseFindAndSetCurrentMessage((child as Transaction).children, currentMessage);
                    if (childResult) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    showDrillDown(message: Message, children: Array<Message>, isAsyncCall: Boolean) {
        let sonRpcId: string = null;
        let linkIndex;

        if (isAsyncCall) {
            const data = message.data.split("|");
            sonRpcId = message.name === "Truncate"
                ? (data.length > 1 ? data[1] : data[0])
                : message.data + "";
        } else {
            for (let index = 0; index < children.length; index++) {
                let child = children[index];
                if (child.type == KnownMessageType.ETraceLink) {
                    sonRpcId = (child as Transaction).data;
                    linkIndex = index;
                }
            }
        }

        if (sonRpcId) {
            message.isLoading = true;
            this.forceUpdate();
            let sonMsgId = this.requestId + "$$" + sonRpcId;
            CallstackService.queryRequestId(sonMsgId, null, this.notifyAboutFailureInDrillUpOrDrillDown)
                .then(callstack => {
                    message.isLoading = false;
                    if (isAsyncCall) {
                        Object.assign(message, callstack.message);
                        message.callstackRef = callstack;
                        message.shouldShowDetailInfo = true;
                    } else {
                        children[linkIndex] = callstack.message;
                        children[linkIndex].callstackRef = callstack;
                        children[linkIndex].shouldShowDetailInfo = true;
                    }
                    this.setState({callstack: this.state.callstack});
                }).catch(error => {
                message.isError = true;
                this.setState({callstack: this.state.callstack});
            });
        }
    }

    expand() {
        this.expandOne(this.state.callstack.message);
        this.setState({callstack: this.state.callstack});
    }

    collapse() {
        this.collapseOne(this.state.callstack.message);
        this.setState({callstack: this.state.callstack});
    }

    async loadConsumers(message: Message) {
        let producerRpcId = message.tags.rpcid;
        // let producerAppId = this.rawCallStack.appId;
        let msg = this.requestId + "$$" + producerRpcId;
        await this.callstackStore.loadConsumers(msg);
        this.callstackStore.showConsumers = true;
    }

    render() {
        const callstack = this.state.callstack;
        if (callstack) {
            this.rpcId = callstack.id.substr(callstack.id.indexOf("|") + 1);
            this.requestId = this.getRequestId(callstack.requestId);
            callstack.message.callstackRef = callstack;
            callstack.message.shouldShowDetailInfo = true;
            return (
                <Row>
                    <Col span={24}>
                        <Row className="callstack-table-header">
                            <Col span={3}>AppID</Col>
                            <Col span={1}>EZone</Col>
                            <Col span={2}>Cluster</Col>
                            <Col span={2}><Tooltip title="服务耗时"><span>Duration</span></Tooltip></Col>
                            <Col span={2}><Tooltip title="当前服务耗时/服务总耗时"><span>Duration(%)</span></Tooltip></Col>
                            <Col span={10}>Description</Col>
                            <Col span={2}>Action</Col>
                            <Col span={2}><Tooltip title="服务调用起始时间"><span>Timestamp</span></Tooltip></Col>
                        </Row>
                        {this.oneLine(callstack, callstack.message, 0, true)}
                    </Col>
                </Row>
            );
        } else {
            return (
                <div>
                    No Callstack Found.
                </div>
            );
        }
    }

    private expandOne(message: Message) {
        message.isOpen = true;

        if (message._type == MessageClassType.TRANSACTION) {
            if ((message as Transaction).children) {
                (message as Transaction).children.forEach(child => this.expandOne(child));
            }
        }
    }

    private collapseOne(message: Message) {
        message.isOpen = false;
        if (message._type == MessageClassType.TRANSACTION) {
            if ((message as Transaction).children) {
                (message as Transaction).children.forEach(child => this.collapseOne(child));
            }
        }
    }

    @autobind
    private expandOrCollapse(message: Message) {
        message.isOpen = !message.isOpen;
        this.setState({callstack: this.state.callstack});
    }

    private buildCallstackSimpleInfo(callstack: Callstack) {
        let shortRpcId;
        if (callstack) {
            shortRpcId = callstack.id.indexOf("|") >= 0 ? callstack.id.substr(callstack.id.indexOf("|") + 1) : callstack.id;
        }
        let hostLink = (
            <span><b>HostName: </b><a rel="noopener noreferrer"
                                      href={"/system/host/overview?host=" + callstack.hostName} target="_blank">
                    {callstack.hostName}</a></span>);
        const title = (
            <div>
                <span>AppId：<b>{callstack.appId}</b></span><br/>
                <span>RpcId：<b>{callstack.id}</b></span>
                {hostLink && (<br/>)}
                {hostLink && (hostLink)}
            </div>
        );

        return callstack && (
            <div style={{overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap"}}>
                <Tooltip title={title} overlayStyle={{maxWidth: "none"}}>
                    <div>{callstack.appId}
                        <mark style={{marginLeft: "3px"}}>{shortRpcId}</mark>
                    </div>
                </Tooltip>
            </div>
        );
    }

    private buildTypeAndNameWithLink(callstack: Callstack, transaction: Transaction, type: string, name: string, leftMargin: number) {
        const expandButton = transaction.isOpen ?
            <ShrinkOutlined style={{cursor: "pointer", marginLeft: "5px", color: "black"}}
                            onClick={() => this.expandOrCollapse(transaction)}/>
            : <FileTextOutlined style={{cursor: "pointer", marginLeft: "5px", color: "black"}}
                                onClick={() => this.expandOrCollapse(transaction)}/>;
        const faiStatus = (!Message.isStatusSuccess(transaction) && (
            <Tooltip title="Status"><span style={{color: "red", float: "right"}}>{transaction.status}
                </span></Tooltip>));
        switch (type) {
            case KnownMessageType.SOAService:
                return (
                    <div className="callstack-table-col-description-content" style={{paddingLeft: leftMargin}}>
                        <b>{type}: </b>
                        <a
                            rel="noopener noreferrer"
                            href={`/trace/soa/provider?appId=${callstack.appId}&method=${name}`}
                            target="_blank"
                        >{name}
                        </a>
                        {faiStatus}
                        {expandButton}
                    </div>);
            case KnownMessageType.SOACall:
                return (
                    <div className="callstack-table-col-description-content" style={{paddingLeft: leftMargin}}>
                        <b>{type}: </b>
                        <a
                            rel="noopener noreferrer"
                            href={`/trace/soa/consumer?appId=${callstack.appId}&method=${name}`}
                            target="_blank"
                        >{name}
                        </a>
                        {faiStatus}
                        {expandButton}
                    </div>);
            case KnownMessageType.DAL:
                let dalGroup: string = transaction.tags.group;
                let sqlId: string = transaction.tags.sqlId;
                return (
                    <div className="callstack-table-col-description-content" style={{paddingLeft: leftMargin}}>
                        <b>{type}: </b>
                        <a
                            rel="noopener noreferrer"
                            href={`/infra/dal/sql?dalGroup=${dalGroup}&sqlId=${sqlId}`}
                            target="_blank"
                        >{name}
                        </a>
                        {faiStatus}
                        {expandButton}
                    </div>);
            case KnownMessageType.URL:
                return (
                    <div className="callstack-table-col-description-content" style={{paddingLeft: leftMargin}}>
                        <b>{type}: </b>
                        <a
                            rel="noopener noreferrer"
                            href={`/trace/url?appId=${callstack.appId}&url=${name}`}
                            target="_blank"
                        >{name}
                        </a>
                        {faiStatus}
                        {expandButton}
                    </div>);
            case KnownMessageType.RMQ_CONSUME:
                let sendTime: number;
                if (transaction.tags && transaction.tags.sendtime) {
                    sendTime = transaction.tags.sendtime;
                }
                let consumeLatencyString: string, consumeLatencyComponent;
                if (sendTime) {
                    consumeLatencyString = DataFormatter.transformMilliseconds(transaction.timestamp - sendTime);
                    consumeLatencyComponent = (
                        <div style={{color: "rgb(128, 128, 255)", float: "right"}}>
                            <Tooltip title="消息从生产者到消费者的时间">
                                <span>消费延迟:{consumeLatencyString}</span>
                            </Tooltip>
                        </div>
                    );
                }
                return (
                    <div className="callstack-table-col-description-content" style={{paddingLeft: leftMargin}}>
                        <b>{type}: </b>{name}
                        {consumeLatencyString && (consumeLatencyComponent)}

                        {faiStatus}{expandButton}
                    </div>
                );
            default:
                return (
                    <div className="callstack-table-col-description-content" style={{paddingLeft: leftMargin}}>
                        <b>{type}: </b>{name}{faiStatus}{expandButton}
                    </div>
                );
        }
    }

    private queryRpcId(tags: any): string {
        if (tags.rpc_id) {
            let index = tags.rpc_id.lastIndexOf("^^");
            if (index > 0) {
                return tags.rpc_id.substring(0, index);
            } else {
                let index2 = tags.rpc_id.lastIndexOf("^");
                if (index2 > 0) {
                    if (tags.rpc_id.substring(index2).length > 10) {
                        return tags.rpc_id.substring(0, index2);
                    }
                }
            }
            return tags.rpc_id;
        } else if (tags.rpcid) {
            let index = tags.rpcid.lastIndexOf("^^");
            if (index > 0) {
                return tags.rpcid.substring(0, index);
            } else {
                let index2 = tags.rpcid.lastIndexOf("^");
                if (index2 > 0) {
                    if (tags.rpcid.substring(index2).length > 10) {
                        return tags.rpcid.substring(0, index2);
                    }
                }
            }
            return tags.rpcid;
        }
        return null;
    }

    private getRequestId(requestId: string): string {
        if (requestId == null) {
            return "";
        }
        let index = requestId.indexOf("^^");
        if (index > 0 && index + 2 <= requestId.length) {
            return requestId.substring(index + 2);
        }
        return requestId;
    }

    private buildCallstackDetailInfo(callstack: Callstack) {
        let hostLink = (
            <a
                style={{color: "#f77e1c", textDecoration: "underline"}}
                rel="noopener noreferrer"
                target="_blank"
            >
                {callstack.hostName}
                {/*<CallstackTooltipChart callstack={callstack} showType={CallstackTooltipChart.SHOW_MACHINE_INFO}/>*/}
            </a>
        );

        return callstack && (
            <Row>
                <Col span={12} style={{overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap"}}>
                    <Tooltip title={callstack.hostName} overlayStyle={{maxWidth: "none"}}>
                        <div><b>HostName: </b>{hostLink}<img height={18} src="" alt="HostLink"/></div>
                    </Tooltip>
                </Col>
                <Col span={8}><b>IP: </b>
                    <a rel="noopener noreferrer" target="_blank">{callstack.hostIp}</a>
                </Col>
                <Col span={4}>
                    {/*log link*/}
                </Col>
            </Row>
        );
    }

    private oneLine(callstack: Callstack, message: Message, leftMargin: number, isRoot: boolean, parentDuration?: number) {
        let lines = [];
        if (message._type == MessageClassType.TRANSACTION ||
            (message._type === MessageClassType.EVENT && message.type == KnownMessageType.ETraceLink && (message.name === "AsyncCall" || message.name === "Truncate"))) {

            const isAsyncCall =
                message._type === MessageClassType.EVENT &&
                message.type == KnownMessageType.ETraceLink &&
                (message.name === "AsyncCall" || message.name === "Truncate");

            const transaction = message as Transaction;
            const currDuration = isAsyncCall ? 0 : transaction.duration;
            if (isRoot) {
                this.parentDuration = currDuration;
                this.parentTimestamp = transaction.timestamp;
            }

            // set default open when its or its child status is not success.
            // only init defaultOpen when it's null
            if (transaction.isOpen == null) {
                let defaultOpen = false;
                if (transaction.children) {
                    transaction.children.forEach(child => {
                        if (!Message.isStatusSuccess(child)) {
                            defaultOpen = true;
                        }
                    });
                }
                transaction.isOpen = !Message.isStatusSuccess(transaction) || defaultOpen;
            }

            const timeRatio = !parentDuration ? 100 : ((currDuration / parentDuration) * 100).toFixed(2);
            const leftOffsetRatio = (message.timestamp - this.parentTimestamp) / parentDuration * 100;
            const isLoading = message.isLoading;

            const SOAService = {
                total: message.tags && message.tags["SOAService.total"],
                deserialize: message.tags && message.tags["SOAService.deserialize"],
                waiting: message.tags && message.tags["SOAService.waiting"],
                biz: message.tags && message.tags["SOAService.biz"]
            };
            const durationToolTip = (
                <div>
                    <div>{`当前耗时占比: ${timeRatio}%, 当前耗时: ${currDuration}ms${parentDuration ? ", 服务总耗时: " + parentDuration + "ms" : ""}`}</div>
                    {message.type === KnownMessageType.SOAService && message.tags && (
                        <React.Fragment>
                            {SOAService.total &&
                            <div>{`服务端总耗时: ${SOAService.total}ms,`}</div>
                            }
                            {SOAService.deserialize &&
                            <div>{`序列化耗时: ${SOAService.deserialize}ms,`}</div>
                            }
                            {SOAService.waiting &&
                            <div>{`请求排队等待耗时: ${SOAService.waiting}ms,`}</div>
                            }
                            {SOAService.biz &&
                            <div>{`业务代码执行耗时: ${SOAService.biz}ms`}</div>
                            }
                        </React.Fragment>
                    )}
                </div>
            );

            lines.push(
                <Row key={currDuration + 1} className="callstack-table-one-line" gutter={8}>
                    <Col span={3}>{this.buildCallstackSimpleInfo(message.callstackRef)}</Col>
                    <Col span={1}>{message.callstackRef ? message.callstackRef.ezone : ""}</Col>
                    <Col span={2}>{message.callstackRef ? message.callstackRef.cluster : ""}</Col>
                    <Col span={2}>
                        <Tooltip title={"服务耗时:" + currDuration + "ms"}><span>{currDuration}ms</span></Tooltip></Col>
                    <Col span={2}>
                        <div className="callstack-table-col-duration-container">
                            <Tooltip
                                title={<span>{durationToolTip}</span>}
                            >
                                <div
                                    className={`callstack-table-col-duration ${(!isRoot && timeRatio >= 50) ? "warning" : ""}`}
                                    style={{
                                        marginLeft: leftOffsetRatio + "%",
                                        width: timeRatio + "%",
                                    }}
                                >
                                    <div className="callstack-table-col-duration-content">
                                        {timeRatio}(%)
                                    </div>
                                </div>
                            </Tooltip>
                        </div>
                    </Col>
                    <Col span={10} className="callstack-table-col-description">
                        {this.buildTypeAndNameWithLink(callstack, transaction, message.type, message.name, leftMargin)}
                    </Col>
                    <Col span={2}>
                        {this.shouldShowDrillUp(isRoot) &&
                        <a onClick={() => !isLoading && this.showDrillUp(transaction)}>
                            {isLoading
                                ? <span><LoadingOutlined/>Loading...</span>
                                : <span>Drill Up</span>
                            }
                        </a>
                        }
                        {(isAsyncCall || this.shouldShowDrillDown(transaction.children)) &&
                        <a onClick={() => !isLoading && this.showDrillDown(transaction, transaction.children, isAsyncCall)}>
                            {isLoading
                                ? <span><LoadingOutlined/> Loading...</span>
                                : <span>Drill Down</span>
                            }
                        </a>
                        }
                        {message.isError ? <span style={{color: "red"}}>Error</span> : ""}
                        {message.type == KnownMessageType.RMQ_PRODUCE &&
                        <a style={{color: "#8080ff"}} onClick={() => this.loadConsumers(message)}>Consume</a>}
                        {message.type == "RMQ_CONSUME" && message.tags && this.getRequestId(message.tags.rid) != this.requestId && this.getRequestId(message.tags.rid)
                        && this.queryRpcId(message.tags)
                        && <a
                            rel="noopener noreferrer"
                            style={{color: "#8080ff"}}
                            target="_blank"
                            href={"/requestId/" + this.getRequestId(message.tags.rid) + "$$" + this.queryRpcId(message.tags)}
                        >
                            Produce
                        </a>
                        }
                        {this.shouldShowChart(transaction.type) &&
                        <CallstackTooltipChart message={message}
                                               showType={CallstackTooltipChart.SHOW_TRANSACTION_INFO}/>}
                    </Col>
                    <Col span={2} style={{fontSize: "14px"}}>
                        <Tooltip
                            title={"服务调用时间:" + moment(message.timestamp).format(SIMPLE_TIME_FORMAT_WITH_MILLSECOND)}
                        >
                            <div>{moment(message.timestamp).format(SIMPLE_TIME_FORMAT_WITH_MILLSECOND)}</div>
                        </Tooltip>
                    </Col>
                </Row>
            );

            if (message.isOpen) {
                lines.push(
                    <Row key={currDuration + 2}>
                        <Col span={24}>
                            {message.shouldShowDetailInfo && this.buildCallstackDetailInfo(message.callstackRef)}
                        </Col>
                        <Col span={24} className="transaction-detail-wrapper">
                            <OneTransactionDetail message={transaction}/>
                        </Col>
                    </Row>);
            }

            if (transaction.children) {
                transaction.children.forEach(child => {
                    if (!child.callstackRef) {
                        child.callstackRef = transaction.callstackRef;
                    }
                    lines.push(this.oneLine(child.callstackRef, child, this.leftMarginStep + leftMargin, false, this.parentDuration));
                });
            }
        } else if (isRoot && message._type == MessageClassType.EVENT && message.type != KnownMessageType.ETraceLink
            && !message.type.startsWith(KnownMessageType.Statsd_Metric_Prefix)) {
            // 若没有这个分支，单独的EVENT如 exception PAGE 出不来。
            lines.push(
                <Row key={message.id}>
                    <Col span={3}>
                        {this.buildCallstackSimpleInfo(message.callstackRef)}
                        {(message.isOpen) &&
                        this.buildCallstackDetailInfo(message.callstackRef)
                        }
                    </Col>
                    <Col span={1}>{message.callstackRef ? message.callstackRef.ezone : ""}</Col>
                    <Col span={2}>--</Col>
                    <Col span={2}>--</Col>
                    <Col span={11}><b>{message.type}</b>: {message.name}</Col>
                    <Col span={2}>--</Col>
                    <Col span={3} style={{fontSize: "12px"}}>
                        {moment(message.timestamp).format(SIMPLE_TIME_FORMAT_WITH_MILLSECOND)}</Col>
                </Row>
            );

            lines.push(
                <Row key={message.id + 1}>
                    <Col span={24}>
                        {isRoot && this.buildCallstackDetailInfo(message.callstackRef)}
                    </Col>
                    <Col span={24} className="transaction-detail-wrapper">
                        <TagCard tags={message.tags} message={message}/>
                    </Col>
                </Row>);
            lines.push(
                <Row key={message.id + 2}>
                    {OneTransactionDetail.dataCard(message.data)}
                </Row>);
        } else if (message._type == MessageClassType.HEARTBEAT && message.type == KnownMessageType.threaddump) {
            const transaction = message as Transaction;
            lines.push(
                <Row key={transaction.id + 2}>
                    <Col span={24} className="transaction-detail-wrapper">
                        <OneTransactionDetail message={transaction}/>
                    </Col>
                </Row>);
        }
        return lines;
    }

    private notifyAboutFailureInDrillUpOrDrillDown() {
        notification.error({
            message: "查看链路失败",
            description: (
                <div>可能是链路不完整，原因参见
                    <a rel="noopener noreferrer"
                       href="https://monitor-doc.faas.elenet.me/faq/trace/cant_drill_up_down.html" target="_blank">
                        帮助文档
                    </a>
                </div>),
            duration: 5
        });
    }

    private shouldShowChart(transactionType: string): boolean {
        return transactionType === KnownMessageType.SOAService ||
            transactionType === KnownMessageType.SOACall ||
            transactionType === KnownMessageType.DAL ||
            transactionType === KnownMessageType.RMQ_CONSUME ||
            transactionType === KnownMessageType.RMQ_PRODUCE;
    }

}
