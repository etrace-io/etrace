import {get} from "lodash";
import React from "react";
import YpRecord from "./YPRecord";
import {Tabs} from "antd";
import {messageHandler} from "$utils/message";
import {getHomeData, getListRecord} from "$services/YellowPagesService";
import {BlankBoxIcon, HottestIcon, NewestIcon, PinIcon} from "../Icon/Icon";
import {HomeData, ListItem, RecordItem} from "$models/YellowPagesModel";
import RecordList from "./YPRecordList";
import {LoadingOutlined, StarOutlined} from "@ant-design/icons/lib";


interface YpRecordListProps {
    className?: string;
    starList?: ListItem[];
    onLike?: (item: RecordItem) => void;
}

interface YpRecordListStatus {
    isLoading: boolean;
    homeDataSource: HomeData;
    currListId: string;
    listData: RecordItem[];
    currTab: string;
}

export default class YPHomeRecords extends React.Component<YpRecordListProps, YpRecordListStatus> {
    state = {
        currTab: "home",
        isLoading: false,
        homeDataSource: null,
        currListId: null,
        listData: [],
    };

    componentWillMount(): void {
        this.queryHomeData();
    }

    queryHomeData = () => {
        this.setState({isLoading: true});
        getHomeData()
            .then(homeDataSource => {
                this.setState({homeDataSource, isLoading: false});
            })
            .catch(err => {
                this.setState({isLoading: false});
                messageHandler("error", "获取首页数据失败，请刷新或稍后重试");
            });
    };

    handleTabsChange = (key: string) => {
        this.setState({
            currTab: key,
        });

        if (key === "home") {
            this.queryHomeData();
        } else {
            this.setState({isLoading: true});
            getListRecord(+key)
                .then(result => {
                    this.setState({
                        listData: result.results,
                        isLoading: false,
                    });
                })
                .catch(err => {
                    // console.log(err);
                    this.setState({isLoading: false});
                    messageHandler("error", "获取集合内容失败 😥");
                });
        }
    };

    renderHomePage = () => {
        const {homeDataSource, isLoading} = this.state;
        const pinList = get(homeDataSource, "RECOMMEND", []);
        const hottestList = get(homeDataSource, "HOTEST", []);
        const newestList = get(homeDataSource, "NEWEST", []);

        if (isLoading) {
            return (
                <div className="record-list__loading-tips">
                    <LoadingOutlined/>
                    <div className="loading-tips__content">加载中...</div>
                </div>
            );
        }

        return (
            <div className="record-list__collection">
                {pinList && pinList.length > 0 && (
                    <div className="record-list__collection-group">
                        <div className="record-list__collection-title">置顶 <PinIcon/></div>

                        <div className="record-list__collection-content">
                            {pinList.map(this.renderRecordItem)}
                        </div>
                    </div>
                )}

                {hottestList && hottestList.length > 0 && (

                    <div className="record-list__collection-group">
                        <div className="record-list__collection-title">最热 <HottestIcon/></div>

                        <div className="record-list__collection-content">
                            {hottestList.map(this.renderRecordItem)}
                        </div>
                    </div>
                )}

                {newestList && newestList.length > 0 && (
                    <div className="record-list__collection-group">
                        <div className="record-list__collection-title">最新 <NewestIcon/></div>

                        <div className="record-list__collection-content">
                            {newestList.map(this.renderRecordItem)}
                        </div>
                    </div>
                )}

                {newestList.length === 0 && hottestList.length === 0 && pinList.length === 0 && (
                    <div className="record-list__empty-tips">
                        <BlankBoxIcon className="empty-tips__icon"/>
                        <div className="empty-tips__content">暂无内容</div>
                    </div>
                )}
            </div>
        );
    };

    renderListData = () => {
        const {listData, isLoading, homeDataSource, currTab} = this.state;
        const currList = get(homeDataSource, "LIST", []).filter(list => list.id === +currTab)[0];

        return (
            <RecordList
                hideTitle={true}
                listInfo={currList}
                isLoading={isLoading}
                dataSource={listData}
            />
        );

        // return (
        //     <div className="record-list__list-container">
        //         {listData.map(this.renderRecordItem)}
        //         {(!listData || listData.length === 0) && (
        //             <div className="record-list__empty-tips">
        //                 <BlankBoxIcon className="empty-tips__icon"/>
        //                 <div className="empty-tips__content">暂无内容</div>
        //             </div>
        //         )}
        //     </div>
        // );
    };

    renderRecordItem = (record: RecordItem) => {
        const {onLike} = this.props;
        return <YpRecord dataSource={record} key={record.id} onLike={onLike}/>;
    };

    render() {
        const {className, starList} = this.props;
        const {homeDataSource, currTab} = this.state;

        const recommendList = get(homeDataSource, "LIST", []);

        return (
            <div className={className}>
                <Tabs className="record-list__tabs" onChange={this.handleTabsChange} type="card">
                    <Tabs.TabPane tab="推荐" key="home"/>

                    {/* 收藏的列表 */}
                    {starList && starList.map(list => (
                        <Tabs.TabPane
                            tab={<span><StarOutlined style={{color: "#ffdd00"}}/>{list.name}</span>}
                            key={list.id + ""}
                        />
                    ))}

                    {/* 推荐的列表 */}
                    {recommendList.map(list => (
                        <Tabs.TabPane tab={list.name} key={list.id + ""}/>
                    ))}

                    {/*<Tabs.TabPane tab={<span><Icon style={{color: "#ffdd00"}} type="star" theme="filled"/>开发必备</span>} key="2"/>*/}
                    {/*<Tabs.TabPane tab="融合必备" key="3"/>*/}
                    {/*<Tabs.TabPane tab="日常开发" key="4"/>*/}
                    {/*<Tabs.TabPane tab="发布系统" key="5"/>*/}
                    {/*<Tabs.TabPane tab="绩效相关" key="6"/>*/}
                    {/*<Tabs.TabPane tab="人力资源" key="7"/>*/}
                    {/*<Tabs.TabPane tab="薪酬福利" key="8"/>*/}
                </Tabs>

                {currTab === "home"
                    ? this.renderHomePage()
                    : this.renderListData()
                }

                {/*<Spin spinning={isLoading} delay={500}>*/}
                {/**/}
                {/*</Spin>*/}
            </div>
        );
    }
}
