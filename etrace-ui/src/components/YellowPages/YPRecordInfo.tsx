import React from "react";
import {Tag, Tooltip} from "antd";
import {RecordItem} from "../../models/YellowPagesModel";
import {CopyToClipboard} from "react-copy-to-clipboard";
import {messageHandler} from "../../utils/message";
import {CopyOutlined, DingdingOutlined} from "@ant-design/icons/lib";

interface YpRecordInfoProps {
    dataSource: RecordItem;
}

interface YpRecordInfoStatus {
}

export default class YpRecordInfo extends React.Component<YpRecordInfoProps, YpRecordInfoStatus> {
    handleCopyText = () => {
        messageHandler("success", "复制成功~");
    };

    render() {
        const {dataSource} = this.props;

        const ownerDingTalkNumber = dataSource.ownerDingtalkNumber && dataSource.ownerDingtalkNumber.split("|")[0];
        const ownerNickname = dataSource.ownerDingtalkNumber && dataSource.ownerDingtalkNumber.split("|")[1];

        const dingTalkLink = `dingtalk://dingtalkclient/action/sendmsg?dingtalk_id=${ownerDingTalkNumber}`;

        return (
            <div className="e-monitor-yellow-pages__record-info">
                <div className="record-info__header">
                    <img className="record-icon" src={dataSource.icon} alt="icon"/>
                    <div className="record-info__header-parts">
                        <div className="record-keywords">
                            {dataSource.keywordList && dataSource.keywordList.map(keyword => (
                                <Tag className="record-keyword" key={keyword.id}>{keyword.name}</Tag>
                            ))}
                        </div>
                        <div className="record-name">{dataSource.name}</div>
                    </div>
                </div>

                <div className="record-info__item">
                    <div className="record-info__item-title">简介</div>
                    <div className="record-info__item-content">{dataSource.description}</div>
                </div>

                <div className="record-info__item">
                    <div className="record-info__item-title">App 网址</div>
                    <div className="record-info__item-content">
                        <a rel="noopener noreferrer" target="_blank" href={dataSource.url}>{dataSource.url}</a>
                        <CopyToClipboard text={dataSource.url} onCopy={this.handleCopyText}>
                            <Tooltip title="点击复制 App 网址"><CopyOutlined /></Tooltip>
                        </CopyToClipboard>
                    </div>
                </div>

                {ownerDingTalkNumber && (
                    <div className="record-info__item">
                        <div className="record-info__item-title">负责人</div>
                        <div className="record-info__item-content">
                            <div><a href={dingTalkLink}><DingdingOutlined />{ownerNickname || ownerDingTalkNumber}</a></div>
                            <CopyToClipboard text={ownerNickname || ownerDingTalkNumber} onCopy={this.handleCopyText}>
                                <Tooltip title={ownerNickname ? "点击复制花名" : "点击复制钉钉号"}><CopyOutlined /></Tooltip>
                            </CopyToClipboard>
                        </div>
                    </div>
                )}

                {dataSource.dingtalkNumber && (
                    <div className="record-info__item">
                        <div className="record-info__item-title">钉钉群</div>
                        <div className="record-info__item-content">
                            <span>{dataSource.dingtalkNumber}</span>
                            <CopyToClipboard text={dataSource.dingtalkNumber} onCopy={this.handleCopyText}>
                                <Tooltip title="点击复制钉钉群号"><CopyOutlined /></Tooltip>
                            </CopyToClipboard>
                        </div>
                    </div>
                )}
            </div>
        );
    }
}
