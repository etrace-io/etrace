import React, {useEffect, useRef, useState} from "react";
import {observer} from "mobx-react";
import {Button, Radio, Space, Table, Tooltip} from "antd";
import {ColumnProps} from "antd/lib/table";
import {HolmesApiService} from "$services/HolmesApiService";
import {RequestIdInfo} from "$models/HolmesModel";

import "./TraceSampling.css";
import {DataFormatter} from "$utils/DataFormatter";
import Moment from "react-moment";
import StoreManager from "$store/StoreManager";
import RpcIdUtil from "$utils/RpcIdUtil";
import {QuestionCircleOutlined} from "@ant-design/icons/lib";
import Duration from "$components/Timeline/Duration";
import {SPACE_BETWEEN} from "$constants/index";
import {EMonitorSection} from "$components/EMonitorLayout";

enum ORDER_TYPE {
    DFS = "dfs",
    BFS = "bfs",
}

const sortCompareFn = (order: ORDER_TYPE) => (a: RequestIdInfo, b: RequestIdInfo) =>
    order === ORDER_TYPE.DFS
        ? RpcIdUtil.dfs(a.rpcId, b.rpcId)
        : RpcIdUtil.bfs(a.rpcId, b.rpcId);

const TraceSampling: React.FC<{
    requestId?: string;
    timestamp?: number;
}> = props => {
    const {requestId, timestamp} = props;
    const {callstackStore} = StoreManager;

    const parentNode = useRef<RequestIdInfo>(null);

    const [requestIdInfoList, setRequestIdInfoList] = useState<RequestIdInfo[]>(null);
    const [currOrder, setCurrOrder] = useState<ORDER_TYPE>(ORDER_TYPE.DFS);
    const [loading, setLoading] = useState<boolean>(false);
    const [hasMore, setHasMore] = useState<boolean>(false);
    const [currPage, setCurrPage] = useState<number>(1);

    const pageSize = 100;

    // 排序
    useEffect(() => {
        if (!requestIdInfoList || requestIdInfoList.length === 0) {
            return;
        }
        setRequestIdInfoList(
            requestIdInfoList.sort(sortCompareFn(currOrder)).slice()
        );
    }, [currOrder]);

    useEffect(() => {
        if (currPage === 1) {
            // 只切换日期和 RequestID
            setRequestIdInfoList(null);
            requestId && fetchRequestIdInfo(requestId, timestamp, 1);
        }
        setCurrPage(1);
    }, [requestId, timestamp]);

    // 获取 Info
    useEffect(() => {
        currPage > 1 && fetchRequestIdInfo(requestId, timestamp, currPage);
    }, [currPage]);

    // 设置 Parent Node
    useEffect(() => {
        parentNode.current = requestIdInfoList && requestIdInfoList.length > 0
            ? requestIdInfoList[0]
            : null;
    }, [requestIdInfoList]);

    const fetchRequestIdInfo = (id: string, ts: number, page: number) => {
        setLoading(true);
        HolmesApiService.getSortedRequestIdInfo(id, ts, pageSize, page).then(data => {
            if (!data) {
                return;
            }
            setRequestIdInfoList(prev => (page > 1 && prev ? [...prev, ...data] : data).sort(sortCompareFn(currOrder)));
            setLoading(false);
            setHasMore(data.length === pageSize);
        });
    };

    const handleLoadMore = () => {
        setCurrPage(page => page + 1);
    };

    const handleModalShow = (url: string) => {
        callstackStore.startToQuerySampling();
        callstackStore.callstackShowHead = true;
        callstackStore.loadCallstack(url);
    };

    const columns: ColumnProps<RequestIdInfo>[] = [
        {
            title: "RpcID",
            dataIndex: "rpcId",
        },
        {
            title: <span>时间轴 <Tooltip title="服务调用耗时"><QuestionCircleOutlined/></Tooltip></span>,
            width: 140,
            align: "center",
            render: (text: any, record: RequestIdInfo) => {
                if (!parentNode.current) {
                    return null;
                }
                const parentDuration = parentNode.current.rpcInfo ? +parentNode.current.rpcInfo.duration : 0;
                const parentTimestamp = parentNode.current.rpcInfo ? +parentNode.current.rpcInfo.timestamp : 0;

                const currDuration = record.rpcInfo ? +record.rpcInfo.duration : 0;
                const currTimestamp = record.rpcInfo ? +record.rpcInfo.timestamp : 0;

                const offset = currTimestamp > parentTimestamp
                    ? (currTimestamp - parentTimestamp) * 100 / parentDuration
                    : 0;

                const duration = parentDuration
                    ? currDuration / parentDuration * 100
                    : 100;

                const tooltip = <>
                    <div>调用时间：<Moment format="MM-DD HH:mm:ss.SSS">{currTimestamp}</Moment></div>
                    <div>服务耗时：{DataFormatter.transformMilliseconds(currDuration)}</div>
                </>;

                return <Duration tooltip={tooltip} offset={offset} duration={duration}>
                    {DataFormatter.transformMilliseconds(currDuration)}
                </Duration>;
            }
        },
        {
            title: "类型",
            width: 100,
            align: "center",
            dataIndex: "rpcType",
        },
        {
            title: "AppID / DalGroup",
            render: (text: any, item: RequestIdInfo) => (
                <Button
                    type="link"
                    style={{padding: 0}}
                    onClick={() => handleModalShow(`${item.reqId}$$${item.rpcId}`)}
                >
                    {item.appId}
                </Button>
            )
            // dataIndex: "appId"
        },
        {
            title: "调用 / 操作",
            render: (text: any, item: RequestIdInfo) => item.rpcInfo
                ? item.rpcInfo.interface ||
                item.rpcInfo.operation ||
                item.rpcInfo.url ||
                item.rpcInfo.name
                : "",
        },
        {
            title: "EZone",
            width: 75,
            align: "center",
            render: (text: any, record: RequestIdInfo) => record.rpcInfo ? record.rpcInfo.ezone : "",
        },
        {
            title: "Status",
            width: 100,
            align: "center",
            render: (text: any, record: RequestIdInfo) => record.rpcInfo
                ? (
                    <span style={{color: record.rpcInfo.status == 0 ? "#00EE00" : "#EE0000"}}>
                        ● {record.rpcInfo.status == 0 ? "成功" : "失败"}
                    </span>
                )
                : "",
        },
        {
            title: "ShardingKey",
            align: "center",
            render: (text: any, record: RequestIdInfo) => record.rpcInfo ? record.rpcInfo.shardingkey : ""
        },
        {
            title: "TestCase",
            width: 80,
            align: "center",
            render: (text: any, record: RequestIdInfo) => record.rpcInfo ? record.rpcInfo.testCase : "",
        },
        {
            title: "日志",
            width: 50,
            align: "center",
        }
    ];

    const orderRadioOptions = [
        {label: "深度优先排序", value: ORDER_TYPE.DFS},
        {label: "广度优先排序", value: ORDER_TYPE.BFS},
    ];

    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item>
                <Space size={SPACE_BETWEEN}>
                    <Radio.Group
                        optionType="button"
                        buttonStyle="solid"
                        options={orderRadioOptions}
                        onChange={e => setCurrOrder(e.target.value)}
                        value={currOrder}
                    />

                    {hasMore && (
                        <Button type="primary" loading={loading} onClick={handleLoadMore} disabled={loading}>
                            加载更多链路
                        </Button>
                    )}

                    <Tooltip title="RPC ID 的生成规则是什么？">
                        <Button
                            type="link"
                            href="https://monitor-doc.faas.elenet.me/design/data_struct/request_rpc.html#RPC_ID%E7%94%9F%E6%88%90%E8%A7%84%E5%88%99"
                            target="_blank"
                            // size="small"
                            icon={<QuestionCircleOutlined/>}
                        />
                    </Tooltip>
                </Space>
            </EMonitorSection.Item>

            <EMonitorSection.Item scroll={true}>
                {/*{!requestIdInfoList && (*/}
                {/*    loading ? <EMonitorLoading/> : <NoData/>*/}
                {/*)}*/}

                <Table
                    loading={loading}
                    columns={columns}
                    dataSource={requestIdInfoList}
                    rowKey={(item: RequestIdInfo, index: number) => "" + index}
                    size="small"
                    bordered={true}
                    pagination={false}
                    scroll={{x: 1200}}
                />
            </EMonitorSection.Item>
        </EMonitorSection>
    );
};

export default observer(TraceSampling);
