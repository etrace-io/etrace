import {get} from "lodash";
import React from "react";
import YPAddRecord from "./YPAddRecord";
import YpRecordInfo from "./YPRecordInfo";
import {messageHandler} from "../../utils/message";
import {RecordItem} from "../../models/YellowPagesModel";
import {likeRecord} from "../../services/YellowPagesService";
import {
    BarsOutlined,
    EditOutlined,
    FireOutlined,
    MoreOutlined,
    ShareAltOutlined,
    StarFilled,
    StarOutlined
} from "@ant-design/icons/lib";
import {Dropdown, Menu, Modal} from "antd";
import YpRecordInfoPage from "../../containers/YellowPages/YPRecordInfoPage";
import {APP_BASE_URL} from "$constants/index";

const copy = require("copy-to-clipboard");
const pinIcon = (
    <svg className="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="32" height="32">
        <path d="M547.1 889.4L134.3 476.6c-15.8-15.8-3.9-42.9 18.5-41.8l118.6 5.6 311.9-207.9 5.5-92.8c1.2-21.1 26.8-30.8 41.8-15.9l269.3 269.3c14.9 14.9 5.2 40.5-15.9 41.8l-92.8 5.5-207.9 311.9 5.6 118.6c1.1 22.4-25.9 34.3-41.8 18.5z" fill="#EF5343"/>
        <path d="M354.6 470.9c-4 0-7.8-1.9-10.2-5.5-3.8-5.6-2.2-13.2 3.4-17l190-126.7c5.6-3.8 13.2-2.2 17 3.4 3.8 5.6 2.2 13.2-3.4 17l-190 126.7c-2.1 1.4-4.5 2.1-6.8 2.1z" fill="#FFFFFF"/>
        <path d="M899.9 393.2l-61.3-61.3c14.9 14.9 5.2 40.5-15.9 41.8l-92.8 5.5L522 691l5.6 118.6c1.1 22.4-26 34.3-41.8 18.5l61.3 61.3c15.8 15.8 42.9 3.9 41.8-18.5l-5.6-118.6 207.9-311.9L884 435c21.1-1.3 30.8-26.9 15.9-41.8z" fill="#D84141"/>
        <path d="M587.3 307.7m-12.3 0a12.3 12.3 0 1 0 24.6 0 12.3 12.3 0 1 0-24.6 0Z" fill="#FFFFFF"/>
        <path d="M912.9 380.2L643.5 110.9c-12.1-12.1-29.6-15.8-45.6-9.8s-26.6 20.5-27.6 37.6l-4.9 83.7-299.1 199.4-112.6-5.4c-17.8-0.7-34 9.2-41.3 25.5s-3.7 35 8.9 47.7L314.7 683 102.5 895.2c-7.2 7.2-7.2 18.8 0 26 3.6 3.6 8.3 5.4 13 5.4s9.4-1.8 13-5.4L340.7 709l193.4 193.4c8.3 8.3 19.1 12.6 30.2 12.6 5.9 0 11.8-1.2 17.4-3.7 16.3-7.2 26.3-23.4 25.5-41.3l-5.4-112.6 199.5-299.2 83.7-4.9c17.1-1 31.5-11.6 37.6-27.6s2.4-33.4-9.7-45.5z m-24.6 32.5c-0.5 1.4-1.9 3.7-5.4 3.9l-85.2 5-135.3-135.3c-7.2-7.2-18.8-7.2-26 0-7.2 7.2-7.2 18.8 0 26l130.9 130.9-187.5 281.2-223.7-223.7c-7.2-7.2-18.8-7.2-26 0-7.2 7.2-7.2 18.8 0 26l225.1 225.1c2.8 2.8 6.3 4.5 9.9 5.1l5.5 114.9c0.2 3.5-1.9 5.1-3.6 5.9-1.7 0.8-4.4 1.2-6.8-1.3L147.3 463.6c-2.5-2.5-2-5.1-1.3-6.8 0.8-1.7 2.5-4 5.9-3.6l118.6 5.6c3.9 0.2 7.8-0.9 11.1-3.1l311.9-207.9c4.8-3.2 7.8-8.5 8.2-14.2l5.5-92.8c0.2-3.5 2.6-4.8 3.9-5.4 1.4-0.5 4.1-1 6.5 1.4l269.3 269.3c2.4 2.5 1.9 5.2 1.4 6.6z" fill="#2D3742"/>
    </svg>
);

interface YpRecordProps {
    dataSource: RecordItem;
    pin?: boolean; // 是否显示置顶图标
    newest?: boolean; // 是否显示最新图标
    onLike?: (item: RecordItem) => void; // 收藏
}

interface YpRecordStatus {
    infoModalVisible: boolean;
    editRecordModalVisible: boolean;
    isLike: boolean;
}

export default class YpRecord extends React.Component<YpRecordProps, YpRecordStatus> {
    constructor(props: YpRecordProps) {
        super(props);
        this.state = {
            infoModalVisible: false,
            editRecordModalVisible: false,
            isLike: get(props.dataSource, "star", false),
        };
    }

    getActions = () => {
        const {dataSource} = this.props;
        const {isLike} = this.state;
        // const canEdit = true; // 暂时默认均可编辑
        // const isAdmin = User.isAdmin(StoreManager.userStore.user);

        const favorite = (
            <li key="favorite" onClick={() => this.handleToggleFavoriteRecord(dataSource)}>
                {isLike ? <StarFilled style={{color: "#ffdd00"}}/> : <StarOutlined style={{color: "#ffdd00"}} />}收藏
            </li>
        );
        const detail = <li key="detail" onClick={() => this.handleToggleInfoModal(true)}><BarsOutlined /> 详情</li>;
        // const edit = <li key="edit" onClick={() => this.handleToggleEditModal(true)}><EditOutlined /> 编辑</li>;

        // const actions = [favorite, detail, edit];
        // if (canEdit) {
        //     actions.concat(edit);
        // }
        return [favorite, detail];
    };

    handleToggleInfoModal = (status: boolean) => {
        this.setState({infoModalVisible: status});
    };

    handleToggleEditModal = (status: boolean) => {
        this.setState({editRecordModalVisible: status});
    };

    handleToggleFavoriteRecord = (item: RecordItem) => {
        const {isLike} = this.state;
        const {onLike} = this.props;
        const target = !isLike;

        likeRecord(item.id, target)
            .then(reuslt => {
                messageHandler("success", target ? "收藏成功" : "取消成功");
                this.setState({
                    isLike: target
                });
                onLike(Object.assign({}, item, {star: target}));
            })
            .catch(err => {
                messageHandler("error", "收藏失败，请联系管理员或稍后再试");
            });
    };
    handleMoreOperationMenuClick = ({key}) => {
        const {dataSource} = this.props;
        switch (key) {
            case "share":
                const url = `${APP_BASE_URL}${YpRecordInfoPage.BASE_URL}/${dataSource.id}`;
                copy(url);
                messageHandler("success", "复制链接成功~ 🔗");
                break;
            case "edit":
                this.handleToggleEditModal(true);
                break;
            default:
                break;
        }
    };
    render() {
        const {dataSource, pin} = this.props;
        const {icon, url, name, description, favoriteIndex} = dataSource;

        const {infoModalVisible, editRecordModalVisible} = this.state;

        const actions = this.getActions();
        const moreOperationMenu = (
            <Menu onClick={this.handleMoreOperationMenuClick}>
                <Menu.Item key="share"><ShareAltOutlined /> 分享</Menu.Item>
                <Menu.Item key="edit"><EditOutlined /> 编辑</Menu.Item>
            </Menu>
        );
        return (
            <div className="e-monitor-yellow-pages__record-container">
                {pin && <div className="record-pined">{pinIcon}</div>}
                {/*{newest && <div className="record-newest">{pinIcon}</div>}*/}

                <div className="e-monitor-yellow-pages__record">
                    <div className="record-bg" style={{backgroundImage: `url(${icon})`}}/>
                    <div className="operation-btn">
                        <div className="more-op-btn" title="更多操作">
                            <Dropdown overlay={moreOperationMenu} placement="bottomRight">
                                <MoreOutlined />
                            </Dropdown>
                        </div>
                    </div>

                    <a target="_blank" href={url} className="record-link"  rel="noopener noreferrer">

                        <div className="record-header">
                            <div className="record-info">
                                {icon && <img className="record__logo" src={icon}/>}
                                <span className="record__name">{name}</span>
                            </div>

                            {favoriteIndex > 0 && (
                                <div className="record-badges">
                                    <div className="record-hot" title="热度">
                                        <FireOutlined />
                                        <span>{favoriteIndex}</span>
                                    </div>
                                </div>
                            )}
                        </div>

                        <div className="record-body">
                            <span className="record-description">{description || "暂无介绍"}</span>
                        </div>
                    </a>

                    <div className="record-footer">
                        <ul className="record-actions">{actions}</ul>
                    </div>
                </div>

                <Modal
                    destroyOnClose={true}
                    visible={infoModalVisible}
                    onCancel={() => this.handleToggleInfoModal(false)}
                    footer={null}
                    className="e-monitor-yellow-pages__record-info-modal"
                    width={480}
                >
                    <div className="record-info-modal__bg"><img alt="icon" src={dataSource.icon}/></div>
                    <YpRecordInfo dataSource={dataSource}/>
                </Modal>

                <YPAddRecord
                    id={dataSource.id}
                    visible={editRecordModalVisible}
                    onOk={visible => this.handleToggleEditModal(visible)}
                    onCancel={() => this.handleToggleEditModal(false)}
                />
            </div>
        );
    }
}
