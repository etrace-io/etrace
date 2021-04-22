import React, {useState} from "react";
import StoreManager from "$store/StoreManager";
import {get} from "lodash";
import moment from "moment";
import {SIMPLE_TIME_FORMAT, TIME_FORMAT} from "$models/TimePickerModel";
import OneCallstack from "$components/Callstack/OneCallstack";
import {Button, Row, Space, Spin} from "antd";
import {SamplingCantFindPage} from "$components/Callstack/CallstackPage";
import {observer} from "mobx-react";
import {Link} from "react-router-dom";
import {SPACE_BETWEEN} from "$constants/index";

const SamplingCallStacks: React.FC<{
    requestId: string;
}> = props => {
    const {requestId} = props;
    const {callstackStore} = StoreManager;

    const [loading, setLoading] = useState<boolean>(true);

    if (callstackStore.getCantFindSampling()) {
        return <SamplingCantFindPage/>;
    }

    // if (callstackStore.getCantFindCallstack()) {
    //
    // }

    if (requestId && get(callstackStore.callstack, "message.timestamp")) {
        const [id, timestamp] = requestId.split("|");

    }

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
            {/*{this.callstackStore.callstackShowHead && (*/}
            {/*    <CallstackHead*/}
            {/*        expandFn={this.expand}*/}
            {/*        collapseFn={this.collapse}*/}
            {/*    />*/}
            {/*)}*/}
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
};

const CallStackHeader: React.FC = observer(props => {
    const {callstackStore} = StoreManager;
    const requestId = decodeURIComponent(callstackStore.selectedRequestId || "").split("$$")[0];

    return (
        <Row justify="space-between">
            <Space size={SPACE_BETWEEN}>
                <span>
                    <b>Request ID: </b>
                    {requestId && (
                        <Link to={`/search/request?requestId=${requestId}`} target="_blank">{requestId}</Link>
                    )}
                </span>
            </Space>

            <Space size={SPACE_BETWEEN}>
                {/*<CopyToClipboard*/}
                {/*    text={window.location.origin + "/requestId/" + requestId}*/}
                {/*    onCopy={() => this.setState({copyText: "已复制到剪贴板", threadText: "复制Thread"})}*/}
                {/*>*/}
                {/*    <Button*/}
                {/*        size="small"*/}
                {/*        type="primary"*/}
                {/*    >{this.state.copyText}*/}
                {/*    </Button>*/}
                {/*</CopyToClipboard>*/}

                <Button size="small" onClick={this.props.expandFn}>展开全部</Button>
                <Button size="small" onClick={this.props.collapseFn}>收起全部</Button>
            </Space>
        </Row>
    );
});
