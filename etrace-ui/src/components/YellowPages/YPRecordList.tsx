import React from "react";
import {Button} from "antd";
import YpRecord from "./YPRecord";
import {BlankIcon} from "../Icon/Icon";
import StoreManager from "$store/StoreManager";
import {YpAddRecordToListModal} from "./YPAddRecordToList";
import YpAddList from "./YPAddList";
import YpRecordListPage from "$containers/YellowPages/YPRecordListPage";
import {messageHandler} from "$utils/message";
import {AppstoreAddOutlined, LoadingOutlined, SettingOutlined, ShareAltOutlined} from "@ant-design/icons/lib";
import {ListItem, RecordItem} from "$models/YellowPagesModel";
import {UserKit} from "$utils/Util";
import {APP_BASE_URL} from "$constants/index";

const copy = require("copy-to-clipboard");

interface YPRecordListProps {
    listInfo: ListItem; // 用于给 List 展示 List 信息
    hideTitle?: boolean;
    isLoading?: boolean;
    dataSource?: RecordItem[];
    refresh?: (list: number) => void;
}

interface YPRecordListStatus {
    addRecordToListModalVisible: boolean;
    editListModalVisible: boolean;
}

export default class YPRecordList extends React.Component<YPRecordListProps, YPRecordListStatus> {
    state = {
        addRecordToListModalVisible: false,
        editListModalVisible: false,
    };

    handleToggleAddRecordToListModal = (status: boolean, needRefresh?: boolean) => {
        this.setState({addRecordToListModalVisible: status});
        if (needRefresh) {
            const {refresh, listInfo} = this.props;
            refresh && refresh(listInfo.id);
        }
    };

    handleToggleEditListModal = (status: boolean, needRefresh?: boolean) => {
        this.setState({editListModalVisible: status});
        if (needRefresh) {
            const {refresh, listInfo} = this.props;
            refresh && refresh(listInfo.id);
        }
    };

    render() {
        const {isLoading, listInfo, dataSource, hideTitle} = this.props;
        const {addRecordToListModalVisible, editListModalVisible} = this.state;

        return (
            <div className="list-records-container">
                <RecordList
                    list={listInfo}
                    loading={isLoading}
                    hideTitle={hideTitle}
                    dataSource={dataSource}
                    onAddRecord={() => this.handleToggleAddRecordToListModal(true)}
                    onEditList={() => this.handleToggleEditListModal(true)}
                />

                {listInfo && (
                    <YpAddRecordToListModal
                        list={listInfo.id}
                        visible={addRecordToListModalVisible}
                        onOk={(visible, needRefresh) => this.handleToggleAddRecordToListModal(visible, needRefresh)}
                        onCancel={() => this.handleToggleAddRecordToListModal(false)}
                    />
                )}

                {listInfo && (
                    <YpAddList
                        id={listInfo.id}
                        visible={editListModalVisible}
                        onOk={(visible, needRefresh) => this.handleToggleEditListModal(visible, needRefresh)}
                        onCancel={() => this.handleToggleEditListModal(false)}
                    />
                )}
            </div>
        );
    }
}

interface RecordListProps {
    list: ListItem;
    loading?: boolean;
    hideTitle?: boolean;
    dataSource?: RecordItem[];
    onAddRecord?: () => void;
    onEditList?: () => void;
}

const RecordList: React.SFC<RecordListProps> = props => {
    const {list, loading, dataSource, onAddRecord, onEditList, hideTitle} = props;

    if (loading) {
        return (
            <div className="list-records__loading">
                <LoadingOutlined className="list-records__loading-logo"/>
                <div className="list-records__loading-text">加载中...</div>
            </div>
        );
    }

    if (!list) {
        return;
    }

    const user = StoreManager.userStore.user;
    const isAdmin = UserKit.isAdmin(user);
    const iAmMaintainer = list.maintainerAliEmail === user.aliEmail;
    const canEdit = isAdmin || iAmMaintainer;
    const handleCopyShareLink = () => {
        const url = `${APP_BASE_URL}${YpRecordListPage.BASE_URL}/${list.id}`;
        copy(url);
        messageHandler("success", "复制链接成功~ 🔗");
    };

    return (
        <div className="list-records__list">
            <div className="list-records__list-info">
                {!hideTitle && (
                    <div className="list-info__header">
                        <AppstoreAddOutlined />
                        {/*<Icon className="list-info__logo" type={list.icon || YellowPages.DEFAULT_LIST_LOGO}/>*/}
                        <div className="list-info__name">{list.name}</div>
                        <div className="list-info__op-btn-group">
                            {canEdit &&
                            <SettingOutlined className="list-info__op-btn" type="setting" onClick={onEditList}/>}
                            <ShareAltOutlined className="list-info__op-btn" type="share-alt" onClick={handleCopyShareLink}/>
                        </div>
                    </div>
                )}
                {list.description && <div className="list-info__desc">{list.description}</div>}
            </div>

            {(!dataSource || dataSource.length === 0) && (
                <div className="list-records__empty">
                    <div className="list-records__empty-tips">
                        <div className="empty-tips__img"><BlankIcon/></div>
                        <div className="empty-tips__text">
                            暂无内容
                            {canEdit && (
                                <>，<Button className="list-records__op-btn" icon="setting" onClick={onEditList}>编辑集合</Button> 或 <Button className="list-records__op-btn" icon="plus" onClick={onAddRecord}>添加收录</Button></>
                            )}
                        </div>
                    </div>
                </div>
            )}

            <div className="list-records__record-list">
                {dataSource && dataSource.map(record => (
                    <YpRecord dataSource={record} key={record.id}/>
                ))}
            </div>

            {canEdit && dataSource && dataSource.length > 0 && (
                <div className="list-records__list-op-btn">
                    {hideTitle && <Button className="list-records__op-btn" icon="setting" onClick={onEditList}>编辑集合</Button>}
                    <Button className="list-records__op-btn" icon="plus" onClick={onAddRecord}>添加收录</Button>
                    {hideTitle && <Button className="list-records__op-btn" icon="share-alt" onClick={handleCopyShareLink}>分享集合</Button>}
                </div>
            )}
        </div>
    );
};
