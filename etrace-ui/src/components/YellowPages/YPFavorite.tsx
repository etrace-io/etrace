import React from "react";
import {BlankBoxIcon} from "../Icon/Icon";
import {RecordItem} from "../../models/YellowPagesModel";
import {likeRecord} from "../../services/YellowPagesService";
import {messageHandler} from "../../utils/message";
import {YpHistoryList} from "./YPHistory";
import {DeleteOutlined, HeartFilled, HeartOutlined} from "@ant-design/icons/lib";
import {Spin} from "antd";

interface YpFavoriteProps {
    loading?: boolean;
    className?: string;
    records?: RecordItem[];
    onLike?: (item: RecordItem) => void;
}

interface YpFavoriteStatus {
}

export default class YpFavorite extends React.Component<YpFavoriteProps, YpFavoriteStatus> {
    handleRecordLike = (item: RecordItem) => {
        const {onLike} = this.props;
        const target = !item.star;

        likeRecord(item.id, target)
            .then(reuslt => {
                messageHandler("success", target ? "收藏成功" : "取消成功");
                if (onLike) {
                    onLike(item);
                }
            })
            .catch(err => {
                messageHandler("error", "收藏失败，请联系管理员或稍后再试");
            });
    };

    render() {
        const {className, records, loading} = this.props;
        return (
            <div className={className}>
                <div className="e-monitor-yellow-pages__title">我的收藏</div>
                <YpFavoriteList dataSource={records} onLike={this.handleRecordLike}  isLoading={loading}/>
                {/*<div className="e-monitor-yellow-pages__title">历史记录</div>*/}
                {/*<YpFavoriteList dataSource={list}/>*/}
                <YpHistoryList/>
            </div>
        );
    }
}

interface YpFavoriteListProps {
    dataSource: RecordItem[];
    onLike?: (item: RecordItem) => void;
    onDelete?: (item: RecordItem) => void;
    emptyTips?: string;
    isLoading?: boolean;
}

interface YpFavoriteListStatus {
}

class YpFavoriteList extends React.Component<YpFavoriteListProps, YpFavoriteListStatus> {
    render() {
        const {dataSource, onLike, onDelete, isLoading} = this.props;

        if (!dataSource || dataSource.length === 0) {
            return (
                <div className="favorite-list__empty-tips">
                    <BlankBoxIcon className="empty-tips__icon"/>
                    <div className="empty-tips__content">暂无收藏</div>
                </div>
            );
        }

        return (
            <Spin spinning={isLoading}>
            <ol className="favorite-list">
                {dataSource && dataSource.map(item => (
                    <li key={item.id}>
                        <a href={item.url}>
                            {item.icon && <img className="app-logo" src={item.icon} alt="Logo"/>}
                            {/*{item.icon && <Icon className="list-icon" type={item.icon} />}*/}
                            <span className="item-name">{item.name}</span>
                        </a>
                        <div className="item__operation">
                            {onLike && (item.star ? <HeartFilled  onClick={() => onLike(item)}/> : <HeartOutlined  onClick={() => onLike(item)}/>)}
                            {onDelete && <DeleteOutlined  onClick={() => onDelete(item)}/>}
                        </div>
                    </li>
                ))}
            </ol>
            </Spin>
        );
    }
}
