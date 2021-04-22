import React from "react";
import {autobind} from "core-decorators";
import {Button, Card, Col, Layout, Row, Spin} from "antd";
import OneCallstack from "./OneCallstack";
import {CopyToClipboard} from "react-copy-to-clipboard";
import {CallstackStore} from "../../store/CallstackStore";
import {observer} from "mobx-react";
import {SIMPLE_TIME_FORMAT, TIME_FORMAT} from "../../models/TimePickerModel";
import StoreManager from "../../store/StoreManager";
import moment from "moment";
import {APP_BASE_URL} from "$constants/index";

const Content = Layout.Content;

interface Props {
    match: any;
    location: any;
    history: any;
    router: any;
}

interface State {
}

@observer
export default class CallstackPage extends React.Component<Props, State> {
    requestId: string;
    callstackStore: CallstackStore;

    constructor(props: Props) {
        super(props);
        const {match: {params}} = this.props;
        this.callstackStore = StoreManager.callstackStore;
        this.requestId = params.requestId;
        this.callstackStore.loadCallstack(this.requestId);
    }

    render() {
        return (
            <Layout className="e-monitor-content">
                <Content className="e-monitor-content-sections with-footer flex scroll">
                    <Card className="e-monitor-content-section">
                        <Callstacks requestId={this.requestId}/>
                    </Card>
                </Content>
            </Layout>
        );
    }
}

interface CallstacksProps {
    requestId: string;
}

interface CallstacksState {
}

@observer
export class Callstacks extends React.Component<CallstacksProps, CallstacksState> {

    oneCallstack: any;
    callstackStore: CallstackStore;

    constructor(props: CallstacksProps) {
        super(props);
        this.callstackStore = StoreManager.callstackStore;
        this.state = {};
    }

    render() {
        if (this.callstackStore.getCantFindSampling()) {
            return (
                <div>
                    <SamplingCantFindPage/>
                </div>);
        } else {
            let error;
            if (this.props.requestId && this.callstackStore.callstack && this.callstackStore.callstack.message.timestamp) {
                let temps = this.props.requestId.split("|");
                if (temps.length == 2) {
                    let time = Number.parseInt(temps[1]);
                    let from = this.callstackStore.callstack.message.timestamp - 120000;
                    let to = this.callstackStore.callstack.message.timestamp + 120000;
                    if ((time < from || time > to) && this.callstackStore.samplingData) {
                        error = (
                            <b>
                                {"你的埋点有问题，请自行检查，你的Request ID时间为："
                                + moment(this.callstackStore.samplingData.timestamp).format(SIMPLE_TIME_FORMAT)
                                + "当前采样时间：" + moment(this.callstackStore.callstack.message.timestamp).format(SIMPLE_TIME_FORMAT) + ","}
                            </b>);
                    }
                }
            }
            const requestIdTime = this.props.requestId ? parseInt(this.props.requestId.split("|")[1]) : "";
            const tips = `${error || ""}无法找到该埋点数据${requestIdTime ? `，该链路时间为：${moment(requestIdTime).format(TIME_FORMAT)}，时间过老，有可能已经被删除了。` : "。"}`;
            return (
                <div>
                    {this.callstackStore.callstackShowHead && (
                        <CallstackHead
                            expandFn={this.expand}
                            collapseFn={this.collapse}
                        />
                    )}
                    {
                        this.callstackStore.getCallstack() ?
                            <OneCallstack ref={ref => (this.oneCallstack = ref)}/>
                            :
                            this.callstackStore.getCantFindCallstack() ?
                                <div>{tips}</div>
                                :
                                <Spin
                                    tip="Loading Callstacks..."
                                    size="large"
                                    style={{margin: "50px auto 40px", display: "block"}}
                                />
                    }

                </div>
            );
        }
    }

    @autobind
    private expand() {
        this.oneCallstack.expand();
    }

    @autobind
    private collapse() {
        this.oneCallstack.collapse();
    }
}

interface HeadProps {
    expandFn: () => void;
    collapseFn: () => void;
}

interface HeadState {
    copyText: string;
    threadText: string;
}

@observer
export class CallstackHead extends React.Component<HeadProps, HeadState> {

    callstackStore: CallstackStore;

    constructor(props: HeadProps) {
        super(props);
        this.callstackStore = StoreManager.callstackStore;
        this.state = {
            copyText: "分享链接",
            threadText: "复制Thread"
        };
    }

    render() {
        const requestId = this.callstackStore.selectedRequestId;
        let simplifiedRequestId = requestId ? requestId : "";
        if (simplifiedRequestId.indexOf("$$") >= 0) {
            simplifiedRequestId = simplifiedRequestId.split("$$")[0];
        }
        if (simplifiedRequestId.indexOf("%24%24") >= 0) {
            simplifiedRequestId = simplifiedRequestId.split("%24%24")[0];
        }

        return (
            <Row className="callstack-header">
                <Col span={16}>
                    <b>Request ID: </b>
                    {requestId && (
                        <a
                            rel="noopener noreferrer"
                            target="_blank"
                            href={`${APP_BASE_URL}/search/request?requestId=${simplifiedRequestId}`}
                        >{simplifiedRequestId}
                        </a>
                    )}
                </Col>
                <Col span={8} className="callstack-header-func">
                    <CopyToClipboard
                        text={APP_BASE_URL + "/requestId/" + requestId}
                        onCopy={() => this.setState({copyText: "已复制到剪贴板", threadText: "复制Thread"})}
                    >
                        <Button
                            size="small"
                            type="primary"
                        >{this.state.copyText}
                        </Button>
                    </CopyToClipboard>
                    {/*{*/}
                    {/*CallstackStore.isThreadDump && (*/}
                    {/*<CopyToClipboard*/}
                    {/*text={CallstackStore.threadDumpData}*/}
                    {/*onCopy={() => this.setState({threadText: "Thread已复制到剪贴板", copyText: "复制Thread"})}*/}
                    {/*>*/}
                    {/*<Button*/}
                    {/*size="small"*/}
                    {/*type="primary"*/}
                    {/*>{this.state.threadText}*/}
                    {/*</Button>*/}
                    {/*</CopyToClipboard>*/}
                    {/*)*/}
                    {/*}*/}
                    <Button size="small" onClick={this.props.expandFn}>展开全部</Button>
                    <Button size="small" onClick={this.props.collapseFn}>收起全部</Button>
                </Col>
            </Row>
        );
    }
}

interface SamplingCantFindPageProps {
}

interface SamplingCantFindPageState {
}

@observer
export class SamplingCantFindPage extends React.Component<SamplingCantFindPageProps, SamplingCantFindPageState> {
    callstackStore: CallstackStore;

    constructor(props: SamplingCantFindPageProps) {
        super(props);
        this.callstackStore = StoreManager.callstackStore;
    }

    render() {
        return (
            <div>
                未能找到<b>{this.callstackStore.samplingData.metricName}</b>，时间为{moment(this.callstackStore.samplingData.timestamp).format(SIMPLE_TIME_FORMAT)}
                ~ {moment(this.callstackStore.samplingData.timestamp + this.callstackStore.samplingData.interval).format(SIMPLE_TIME_FORMAT)}间的采样数据
            </div>);
    }
}
