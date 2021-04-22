import React from "react";
import YpRecordInfo from "../../components/YellowPages/YPRecordInfo";
import {RecordItem, SuggestOptions} from "$models/YellowPagesModel";
import {getRecordById} from "$services/YellowPagesService";
import {messageHandler} from "$utils/message";
import {Layout} from "antd";
import YpSearch from "../../components/YellowPages/YPSearch";
import YpSearchResultPage from "./YPSearchResultPage";
import YellowPages from "./YellowPages";
import YpNav from "../../components/YellowPages/YPNav";
import Footer from "$components/EMonitorLayout/Footer";

interface YpRecordInfoPageProps {
    match: any;
    history: any;
}

interface YpRecordInfoPageStatus {
    isLoading: boolean;
    recordInfo: RecordItem;
}

export default class YpRecordInfoPage extends React.Component<YpRecordInfoPageProps, YpRecordInfoPageStatus> {
    public static BASE_URL = "/yellow-pages/record";
    private readonly currRecordId;

    constructor(props: YpRecordInfoPageProps) {
        super(props);
        this.state = {
            isLoading: false,
            recordInfo: null,
        };

        if (this.props.match) {
            const {match: {params}} = this.props;
            this.currRecordId = params.recordId;
        }
    }

    componentWillMount(): void {
        const record = this.currRecordId;
        this.queryRecordInfo(record);
    }

    queryRecordInfo = (recordId) => {
        if (!recordId) {
            return;
        }

        this.setState({isLoading: true});
        getRecordById(+recordId)
            .then(recordInfo => {
                this.setState({
                    recordInfo,
                    isLoading: false
                });
            })
            .catch(err => {
                messageHandler("error", "获取收录信息失败 😥");
                this.setState({
                    isLoading: false
                });
            });
    };

    handleSearch = (value: string, options: SuggestOptions) => {
        if (!value) {
            return;
        }
        const url = `${YpSearchResultPage.BASE_URL}?${YpSearchResultPage.YELLOW_PAGE_KEY}=${value}`;
        this.props.history.push(url);
    };

    render() {
        const {recordInfo} = this.state;

        return (
            <Layout className="e-monitor-layout e-monitor-yellow-pages-record">
                <YpNav homeLink={YellowPages.BASE_URL}/>

                <div className="record-result__body">
                    <YpSearch
                        className="e-monitor-yellow-pages__search-box"
                        onSearch={this.handleSearch}
                        // onPressEnter={this.handleSearchInputPressEnter}
                    />

                    {recordInfo && (
                        <div className="record-result__record-info">
                            <YpRecordInfo dataSource={recordInfo}/>
                        </div>
                    )}
                </div>

                <Footer/>
            </Layout>
        );
    }
}
