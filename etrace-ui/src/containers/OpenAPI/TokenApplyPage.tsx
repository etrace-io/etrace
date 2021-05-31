import React, {useEffect, useState} from "react";
import {ApiToken, TokenService} from "./TokenService";
import {EMonitorSection} from "$components/EMonitorLayout";
import {Button, notification, Table} from "antd";

const columns = [
    {dataIndex: "title", key: "title"},
    {dataIndex: "value", key: "value"}
];

const TokenApplyPage: React.FC = props => {
    const [tokenInfo, setTokenInfo] = useState<ApiToken>();

    useEffect(() => {
        loadTokenInfo();
    }, []);

    const handleApply = () => {
        TokenService.apply().then(ok => {
            loadTokenInfo();
        }).catch(err => {
            notification.error({message: "申请失败", description: err.message, duration: 10});
        });
    };

    const loadTokenInfo = () => {
        TokenService.queryUserToken(err => {
            console.warn("未找到有效 Token。可能未申请过或被拒绝了，可重新申请");
        }).then(result => {
            setTokenInfo(result || null);
        }).catch(err => {
            setTokenInfo(null);
            console.warn("未找到有效 Token。可能未申请过或被拒绝了，可重新申请");
        });
    };

    return (
        <EMonitorSection fullscreen={true} scroll={true}>
            <EMonitorSection.Item type="card" title="Open API 使用说明">
                {tokenInfo === null && (
                    <h2 style={{textAlign: "center", margin: "20px 0"}}>
                        如需使用 Monitor 的数据查询服务，
                        <Button onClick={handleApply} type="primary" style={{verticalAlign: "top"}}>点我申请 Open
                            API</Button>
                    </h2>
                )}
                <ol style={{margin: "1em 0"}}>
                    <li>各环境分配的 <i>CID</i> 与 <i>Token</i> 不一致，即需要各环境都申请一遍！</li>
                    <li>请合理使用（如限制 QPS 在 10 以内），为保证可用性，可能会降级查询服务。</li>
                </ol>
            </EMonitorSection.Item>

            <TokenInfoView info={tokenInfo}/>
        </EMonitorSection>
    );
};

const TokenInfoView: React.FC<{
    info?: ApiToken;
}> = props => {
    const {info} = props;

    if (!info) {
        return null;
    }

    const {status, userCode, cid, token, alwaysAccess} = info;

    const dataSource = [
        {title: "员工号", value: userCode},
        {title: "状态", value: status === "WAIT_AUDIT" ? "审核中..." : status},
        {title: "CID", value: cid},
        {title: "Token", value: token},
        {title: "是否可降级", value: (alwaysAccess || !alwaysAccess) ? "可降级" : "不可降级"},
    ];

    return (
        <EMonitorSection.Item>
            <Table
                dataSource={dataSource}
                columns={columns}
                pagination={false}
                size="middle"
                showHeader={false}
                bordered={true}
            />
        </EMonitorSection.Item>
    );
};

export default TokenApplyPage;
