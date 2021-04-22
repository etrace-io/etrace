import React from "react";
import {Event, KnownMessageType, Message, MessageClassType, Transaction} from "$models/CallstackModel";
import {Card, Col, Row, Tooltip} from "antd";
import {SIMPLE_TIME_FORMAT_WITH_MILLSECOND} from "$models/TimePickerModel";
import {CallstackService} from "$services/CallstackService";
import Moment from "react-moment";
import moment from "moment";

interface DetailProps {
    message: Transaction;
}

interface DetailState {
    redisClusters?: Map<string, string>;
    sqlIdToFullSql?: Map<string, string>;
}

export default class OneTransactionDetail extends React.Component <DetailProps, DetailState> {

    public static dataCard(data: string) {
        return data && (
            <Card title="Data" className="transaction-detail-card">
                <pre>{data}</pre>
            </Card>);
    }

    public static threadDataCard(data: string) {
        return data && (
            <Card title="Data" className="transaction-detail-thread-card">
                <pre>{data}</pre>
            </Card>);
    }

    constructor(props: DetailProps) {
        super(props);
        this.state = {redisClusters: new Map(), sqlIdToFullSql: new Map()};
    }

    eventCard(children: Array<Message>) {
        if (children) {
            return children.map((child, index) => {
                if (child._type == MessageClassType.EVENT && child.type != KnownMessageType.ETraceLink
                    && !child.type.startsWith(KnownMessageType.Statsd_Metric_Prefix)
                    // 也需忽略type=log, name=error的event
                    && !(child.type == KnownMessageType.Log && child.name == KnownMessageType.Error)) {
                    return (
                        <OneEventDetail key={index} event={child}/>
                    );
                } else {
                    return null;
                }
            });
        }
    }

    buildSoaService(message: Transaction) {
        return (
            <div>
                <TagCard tags={message.tags} message={message}/>
                {OneTransactionDetail.dataCard(message.data)}
                {this.eventCard(message.children)}
            </div>
        );
    }

    buildSoaCall(message: Transaction) {
        return (
            <div>
                <TagCard tags={message.tags} message={message}/>
                {OneTransactionDetail.dataCard(message.data)}
                {this.eventCard(message.children)}
            </div>
        );
    }

    buildDal(message: Transaction) {
        let dalGroup = message.tags.group;
        let sqlId = message.tags.sqlId;

        if (!this.state.sqlIdToFullSql.has(sqlId)) {
            this.loadFullSql(sqlId, dalGroup);
        }

        return (
            <div>
                <Card title="Dal" className="transaction-detail-card">
                    <Row>
                        <Col span={12}>
                            <b>Group</b>: <a rel="noopener noreferrer" href={`/infra/dal/sql?dalGroup=${dalGroup}`} target="_blank">{dalGroup}</a>
                        </Col>
                        <Col span={12}>
                            <b>Client App</b>: {message.tags.clientAppId}
                        </Col>
                        <Col span={12}>
                            <b>SqlId</b>: {sqlId}
                        </Col>
                        <Col span={12}>
                            <b>Client Ip</b>: {message.tags.clientIp}
                        </Col>
                    </Row>
                    <Row>
                        <Col span={24}>
                            <b>Sql Detail</b>: <code>{this.state.sqlIdToFullSql.get(sqlId)}</code>
                        </Col>
                    </Row>
                </Card>
                <TagCard tags={message.tags} message={message}/>
                {OneTransactionDetail.dataCard(message.data)}
                {this.eventCard(message.children)}
            </div>
        );
    }

    buildSql(message: Transaction) {
        return (
            <div>
                {message.tags && message.tags.dbId && message.tags.dbInfo && message.tags.affectedRows && (
                    <Card title="Sql" className="transaction-detail-card">
                        <Row>
                            <Col span={10}>
                                <b>DB Id</b>: {message.tags.dbId}
                            </Col>
                            <Col span={10}>
                                <b>DB Info</b>: {message.tags.dbInfo}
                            </Col>
                            <Col span={4}>
                                <b>Rows</b>: {message.tags.affectedRows}
                            </Col>
                        </Row>
                    </Card>
                )
                }
                <TagCard tags={message.tags} message={message}/>
                {OneTransactionDetail.dataCard(message.data)}
                {this.eventCard(message.children)}
            </div>
        );
    }

    buildRedis(message: Transaction) {
        let redisCmds = [];
        const exclude = ["Redis", "EagleEye-TraceId", "redisType"];
        for (let key in message.tags) {
            if (exclude.indexOf(key) === -1) {
                const obj = JSON.parse(message.tags[key]);
                obj.commands.forEach(cmd => {
                    cmd.cluster = obj.url;
                    redisCmds.push(cmd);
                });
            }
        }

        const eagleEyeValue = message.tags["EagleEye-TraceId"];
        const redisType = message.tags.redisType;

        // const eagleEyeUrl = GetApi.profile === Profile.PROD
        //     ? `http://eagleeye.alibaba-inc.com/trace/callChain.htm?traceId=${eagleEyeValue}`
        //     : `http://eagleeye.alibaba.net/trace/callChain.htm?traceId=${eagleEyeValue}`;

        return (
            <div>
                {(eagleEyeValue || redisType) && (
                    <Card title="Tags" className="transaction-detail-card">
                        {redisType && (
                            <div><b>RedisType</b>: {redisType}</div>
                        )}
                    </Card>
                )}
                <Card title="Redis" className="transaction-detail-card">
                    <Row>
                        <Col span={6}>Cluster</Col>
                        <Col span={2}>Command</Col>
                        <Col span={4}>Success/Failure</Col>
                        <Col span={2}>Hit Rate</Col>
                        <Col span={6}>Duration(Max/Min)</Col>
                        <Col span={4}>Response Size(Max/Min)</Col>
                    </Row>
                    {redisCmds.map((cmd, index) => {
                        return (
                            <Row key={index}>
                                <Col span={6}>{cmd.cluster}</Col>
                                <Col span={2}>{cmd.command}</Col>
                                <Col span={4}>{cmd.succeedCount}/{cmd.failCount}</Col>
                                <Col span={2}>
                                    {cmd.hitCount > 0 ? (cmd.hitCount / (cmd.succeedCount + cmd.failCount) * 100).toFixed(0) : 0}%</Col>
                                <Col span={6}>
                                    {(cmd.durationSucceedSum + cmd.durationFailSum).toFixed(2)}ms
                                    ({cmd.maxDuration.toFixed(2)}ms/{cmd.minDuration.toFixed(2)}ms)</Col>
                                <Col span={4}>
                                    {Math.max(cmd.responseSizeSum, 0)}({Math.max(0, cmd.maxResponseSize)}/{Math.max(0, cmd.minResponseSize)})
                                </Col>
                            </Row>
                        );
                    })}
                </Card>
                {OneTransactionDetail.dataCard(message.data)}
                {this.eventCard(message.children)}
            </div>
        );
    }

    buildProducer(message: Transaction) {
        return (
            <div>
                <Card title="Producer Special" className="transaction-detail-card"/>
                <TagCard tags={message.tags} message={message}/>
                {OneTransactionDetail.dataCard(message.data)}
                {this.eventCard(message.children)}
            </div>
        );
    }

    buildTransaction(message: Transaction) {
        return (
            <div>
                <TagCard tags={message.tags} message={message}/>
                {OneTransactionDetail.dataCard(message.data)}
                {this.eventCard(message.children)}
            </div>
        );
    }

    buildSystem(message: Transaction) {
        if (message.data) {
            let data = JSON.parse(message.data);
            return (
                <div>
                    {OneTransactionDetail.threadDataCard(data["jvmThread.threadInfos"])}
                </div>
            );
        }

    }

    render() {
        const message = this.props.message;
        switch (message.type) {
            case KnownMessageType.SOAService:
                return this.buildSoaService(message);
            case KnownMessageType.SOACall:
                return this.buildSoaCall(message);
            case KnownMessageType.Redis:
                return this.buildRedis(message);
            case KnownMessageType.DAL:
                return this.buildDal(message);
            case KnownMessageType.SQL:
                return this.buildSql(message);
            case KnownMessageType.threaddump:
                return this.buildSystem(message);
            default:
                return this.buildTransaction(message);
        }
    }

    async loadFullSql(sqlId: string, dalGroup: string) {
        let result = await CallstackService.loadFullSql(sqlId, dalGroup, err => {
            console.warn("Fail to load full sql for sqlId: ", sqlId, "\t err: ", err);
        });
        if (result && result.length > 0) {
            this.state.sqlIdToFullSql.set(result[0].sqlId, result[0].fullSql);
        } else {
            this.state.sqlIdToFullSql.set(sqlId, "Sql Detail For (" + sqlId + ") Not Found.");
        }
        this.setState({sqlIdToFullSql: this.state.sqlIdToFullSql});
    }

    // async loadRedisCluster(clusters: Array<string>) {
    //     if (clusters && clusters.length > 0) {
    //         let result = await CallstackService.transferRedis(clusters);
    //         if (result && result.data) {
    //             let data = result.data;
    //             Object.keys(data).forEach((key) => this.state.redisClusters.set(key, data[key]));
    //             this.setState({redisClusters: this.state.redisClusters});
    //         }
    //     }
    // }
}

interface OneEventProps {
    event: Event;
}

interface OneEventState {
}

export class OneEventDetail extends React.Component <OneEventProps, OneEventState> {
    render() {
        const child = this.props.event;
        return (
            <div
                className="transaction-detail-card"
                // style={{padding: 8}}
            >
                <Row style={{background: Message.isStatusSuccess(child) ? "" : "#fa887fa6"}}>
                    <Col span={4} style={{fontSize: "12px"}}>
                        {moment(child.timestamp).format(SIMPLE_TIME_FORMAT_WITH_MILLSECOND)}
                    </Col>
                    <Col span={16}>
                        <Tooltip placement="top" title="Type" mouseEnterDelay={1}><b>{child.type}</b></Tooltip> :
                        <Tooltip placement="top" title="Name" mouseEnterDelay={1}><b>{child.name}</b></Tooltip>
                    </Col>
                    <Col span={4}><b>{child.status}</b></Col>
                </Row>
                {child.tags && <TagCard tags={child.tags} message={child}/>}
                {child.data &&
                <Row gutter={8}>
                    <Col span={24}>
                        <Tooltip placement="top" title="Data(过长可横向滚动)" mouseEnterDelay={1}>
                            <pre>{child.data}</pre>
                        </Tooltip>
                    </Col>
                </Row>
                }
            </div>
        );
    }
}

interface TagCardProps {
    tags: object;
    message: Message;
}

interface TagCardState {
}

export class TagCard extends React.Component <TagCardProps, TagCardState> {
    addLinkIfNeeded(tagKey: string, value: string, message: Message) {
        switch (tagKey.toLocaleLowerCase()) {
            case "SOAService.clientApp".toLocaleLowerCase():
                return (
                    <span><b>调用方 App ID</b>: <a rel="noopener noreferrer" href={`/trace/soa/consumer?appId=${value}&method=${message.name}`} target="_blank">{value}</a></span>);
            case "SOACall.serviceApp".toLocaleLowerCase():
                return (
                    <span><b>服务方 App ID</b>: <a rel="noopener noreferrer" href={`/trace/soa/provider?appId=${value}&method=${message.name}`} target="_blank">{value}</a></span>);
            case "group".toLocaleLowerCase():
                return (
                    <span><b>{tagKey}</b>: <a rel="noopener noreferrer" href={`/infra/dal/sql?dalGroup=${value}`} target="_blank">{value}</a></span>);
            case "SOAService.resultCode".toLocaleLowerCase():
            case "SOACall.resultCode".toLocaleLowerCase():
                return (<span><b>请求结果</b>: <span style={{color: value == "success" ? "" : "red"}}>{value}</span></span>);
            case "sendtime".toLocaleLowerCase():
                return (<span><b>生产者发送时间</b>: <Moment format="MM-DD HH:mm:ss.SSS">{parseInt(value)}</Moment></span>);
            default:
                let title = this.translateInfoCN(tagKey);
                return (<span><b>{title}</b>: {value}</span>);
        }
    }

    translateInfoCN(tagKey: string): string {
        const server = "SOAService.";
        const client = "SOACall.";

        switch (tagKey) {
            case server + "clientIP":
                return "调用方 IP";

            case server + "total":
                return "服务端总耗时";

            case server + "deserialize":
                return "反序列化耗时";

            case server + "serialize":
                return "序列化耗时";

            case server + "waiting":
                return "请求排队等待耗时";

            case server + "biz":
                return "业务代码执行耗时";

            case client + "respLen":
                return "返回结果长度";

            case client + "total":
                return "SOA 调用总耗时";

            case client + "start":
                return "RPC 请求发出时间";

            case client + "reqLen":
                return "请求参数字节数";

            case client + "serialize":
                return "序列化耗时";

            case client + "serviceIP":
                return "调用服务方 IP";

            case client + "deserialize":
                return "反序列化耗时";
            default:
                return tagKey;
        }
    }

    render() {
        const tags = this.props.tags;
        if (tags) {
            return (
                <Card title="Tags" className="transaction-detail-card">
                    {
                        Object.keys(tags).map((key: string, index: number) => (
                            <div key={index}>{this.addLinkIfNeeded(key, tags[key], this.props.message)}<br/></div>
                        ))
                    }
                </Card>);
        } else {
            return <div/>;
        }
    }
}
