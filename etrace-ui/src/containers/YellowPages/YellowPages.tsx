import {get} from "lodash";
import React from "react";
import {messageHandler} from "$utils/message";
import YpSearchResultPage from "./YPSearchResultPage";
import YpNav from "../../components/YellowPages/YPNav";
import YpSearch from "../../components/YellowPages/YPSearch";
import {getFavorite} from "$services/YellowPagesService";
import YpFavorite from "../../components/YellowPages/YPFavorite";
import {FavoriteData, RecordItem, SearchCategory} from "$models/YellowPagesModel";
import YpAddRecordOrListBtn from "../../components/YellowPages/YPAddRecordOrListBtn";
import YpNotificationCenter from "../../components/YellowPages/YpNotificationCenter";
import YPHomeRecords from "../../components/YellowPages/YPHomeRecords";
import {EMonitorContainer} from "$components/EMonitorLayout";

// import YpNotificationCenter from "../../component/YellowPages/YpNotificationCenter";

interface YellowPagesProps {
    history: any;
}

interface YellowPagesStatus {
    favorite: FavoriteData;
    favoriteLoading: boolean;
}

export default class YellowPages extends React.Component<YellowPagesProps, YellowPagesStatus> {
    public static BASE_URL = "/yellow-pages";
    public static DEFAULT_RECORD_LOGO = "https://arch-etracealertconsole-altest-alpha-oss-1.oss-cn-shanghai.aliyuncs.com/2020/3/16/11/28/23/IMG_2109.JPG?Expires=1584606144&OSSAccessKeyId=LTAIZYrPlrcDRL7z&Signature=kJQd%2BY5TwK5ghyASIe3M%2ByCR0eM%3D";
    public static DEFAULT_LIST_LOGO = "appstore";

    state = {
        favorite: null,
        favoriteLoading: false,
    };

    public static getCategoryName(category: SearchCategory) {
        switch (category) {
            case SearchCategory.List:
                return "集合";
            case SearchCategory.Record:
                return "App";
            case SearchCategory.Keyword:
                return "关键字";
            default:
                return "";
        }
    }

    componentWillMount(): void {
        this.queryFavorite();
    }

    queryFavorite = () => {
        this.setState({favoriteLoading: true});
        // 获取收藏内容
        getFavorite()
            .then(favorite => {
                this.setState({
                    favorite,
                    favoriteLoading: false
                });
            })
            .catch((err) => {
                this.setState({favoriteLoading: false});
                messageHandler("error", "获取收藏内容失败 😥");
            });
    };

    handleSearch = (value: string) => {
        if (!value) {
            return;
        }
        const url = `${YpSearchResultPage.BASE_URL}?${YpSearchResultPage.YELLOW_PAGE_KEY}=${value}`;
        this.props.history.push(url);
    };

    handleLikeChange = (item: RecordItem) => {
        this.queryFavorite();
    };

    render() {
        const {favorite, favoriteLoading} = this.state;

        const starRecords = get(favorite, "RECORD.results");
        const starList = get(favorite, "LIST.results");

        return (
            <EMonitorContainer header={<YpNav/>} className="e-monitor-yellow-pages" fullscreen={true} headerFixed={true}>
                <div className="e-monitor-yellow-pages__body">
                    <YpSearch className="e-monitor-yellow-pages__search-box" onSearch={this.handleSearch}/>

                    <div className="e-monitor-yellow-pages__content">
                        <YpFavorite
                            loading={favoriteLoading}
                            className="e-monitor-yellow-pages__column favorite"
                            records={starRecords}
                            onLike={this.handleLikeChange}
                        />
                        <YPHomeRecords
                            className="e-monitor-yellow-pages__column record-list"
                            onLike={this.handleLikeChange}
                            starList={starList}
                        />
                        <YpAddRecordOrListBtn/>
                        <YpNotificationCenter className="e-monitor-yellow-pages__column notification-center"/>
                    </div>
                </div>
            </EMonitorContainer>
        );
    }
}
