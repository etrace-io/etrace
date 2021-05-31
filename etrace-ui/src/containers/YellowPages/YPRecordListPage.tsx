import React from "react";
import {Promise} from "es6-promise";
import {messageHandler} from "$utils/message";
import {ListItem, RecordItem, SuggestOptions} from "$models/YellowPagesModel";
import {getListById, getListRecord} from "$services/YellowPagesService";
import YellowPages from "./YellowPages";
import YpSearch from "../../components/YellowPages/YPSearch";
import YpNav from "../../components/YellowPages/YPNav";
import {Layout} from "antd";
import Footer from "$components/EMonitorLayout/Footer";
import YpSearchResultPage from "./YPSearchResultPage";
import YPRecordList from "../../components/YellowPages/YPRecordList";

interface YpRecordListPageProps {
    history: any;
    match: any;
}

interface YpRecordListPageStatus {
    isLoading: boolean;
    listInfo: ListItem;
    records: RecordItem[];
}

export default class YpRecordListPage extends React.Component<YpRecordListPageProps, YpRecordListPageStatus> {
    public static BASE_URL = "/yellow-pages/list";
    private readonly currListId;

    constructor(props: YpRecordListPageProps) {
        super(props);
        this.state = {
            isLoading: false,
            listInfo: null,
            records: [],
        };

        if (this.props.match) {
            const {match: {params}} = this.props;
            this.currListId = params.listId;
        }
    }

    componentWillMount(): void {
        const list = this.currListId;
        this.queryListInfo(list);
    }

    queryListInfo = (list: string | number) => {
        if (!list) {
            return;
        }
        this.setState({isLoading: true});

        Promise
            .all([
                getListById(+list),
                getListRecord(+list)
            ])
            .then(([listInfo, result]) => {
                this.setState({
                    listInfo,
                    records: result.results,
                    isLoading: false,
                });
            })
            .catch(err => {
                this.setState({isLoading: false});
                messageHandler("error", "获取集合内容失败 😥");
            });
    };

    handleRefresh = (list: number) => {
        this.queryListInfo(list);
    };

    handleSearch = (value: string, options: SuggestOptions) => {
        if (!value) {
            return;
        }
        const url = `${YpSearchResultPage.BASE_URL}?${YpSearchResultPage.YELLOW_PAGE_KEY}=${value}`;
        this.props.history.push(url);
    };

    render() {
        const {isLoading, listInfo, records} = this.state;
        return (
            <Layout className="e-monitor-layout e-monitor-yellow-pages-list">
                <YpNav homeLink={YellowPages.BASE_URL}/>

                <div className="list-result__body">
                    <YpSearch
                        className="e-monitor-yellow-pages__search-box"
                        onSearch={this.handleSearch}
                        // onPressEnter={this.handleSearchInputPressEnter}
                    />

                    <div className="list-records-container">
                        <YPRecordList
                            isLoading={isLoading}
                            listInfo={listInfo}
                            dataSource={records}
                            refresh={this.handleRefresh}
                        />
                    </div>
                </div>

                <Footer/>
            </Layout>
        );
    }
}
