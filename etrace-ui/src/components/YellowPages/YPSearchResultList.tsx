import {get} from "lodash";
import React from "react";
import {messageHandler} from "../../utils/message";
import {getListRecord} from "../../services/YellowPagesService";
import {ListItem, RecordItem} from "../../models/YellowPagesModel";
import YellowPages from "../../containers/YellowPages/YellowPages";
import * as AllIcon from "@ant-design/icons/lib";
import YPRecordList from "./YPRecordList";

const classnames = require("classnames");

interface YpSearchResultListProps {
    className?: string;
    defaultSelect?: number; // 默认选中 id
    dataSource: ListItem[];
}

interface YpSearchResultListStatus {
    currSelected: number; // 当前 List ID
    currList: ListItem; // 用于给 List 展示 List 信息
    records: RecordItem[];
    totalRecords: number; // 当前 List 下 Record 总数
    isLoading: boolean; // 加载 List 下 Record Loading
    addRecordToListModalVisible: boolean;
    editListModalVisible: boolean;
}

export default class YpSearchResultList extends React.Component<YpSearchResultListProps, YpSearchResultListStatus> {
    constructor(props: YpSearchResultListProps) {
        super(props);
        const {dataSource, defaultSelect} = props;
        const currSelected = defaultSelect || get(dataSource, "[0].id");
        this.state = {
            currSelected,
            records: [],
            isLoading: false,
            totalRecords: 0,
            addRecordToListModalVisible: false,
            editListModalVisible: false,
            currList: dataSource.find(i => i.id === currSelected),
        };
    }

    componentWillMount(): void {
        const {currSelected} = this.state;

        if (currSelected) {
            this.queryListRecords(currSelected);
        }
    }

    componentWillReceiveProps(nextProps: Readonly<YpSearchResultListProps>, nextContext: any): void {
        if (nextProps.dataSource !== this.props.dataSource) {
            const {defaultSelect, dataSource} = nextProps;
            const currSelected = defaultSelect || get(dataSource, "[0].id");
            this.setState({currSelected});
            this.queryListRecords(currSelected);
        }
    }

    queryListRecords = (listId: number) => {
        this.setState({isLoading: true});

        getListRecord(listId)
            .then(res => {
                this.setState({
                    records: res.results,
                    totalRecords: res.total,
                    isLoading: false,
                });
            })
            .catch(err => {
                this.setState({isLoading: false});
                messageHandler("error", "获取当前集合内容失败");
            });
    };
    handleRefresh = (list: number) => {
        this.queryListRecords(list);
    };

    handleListTabsChange = (list: ListItem) => {
        this.setState({
            currSelected: list.id,
            currList: list
        });
        this.queryListRecords(list.id);
    };

    renderListTabs = (dataSource: ListItem[], selectedItem?: number) => {
        return (
            <ul className="lists-tabs">
                {dataSource.map((list, index) => {
                    const selected = selectedItem ? list.id === selectedItem : index === 0;
                    const itemCls = classnames("list-tab__item", {selected});
                    const ListIcon = AllIcon[list.icon || YellowPages.DEFAULT_LIST_LOGO];

                    return (
                        <li className={itemCls} key={list.id} onClick={() => this.handleListTabsChange(list)}>
                            <ListIcon className="list-tab__item-icon"/>
                            <div className="list-tab__item-name">{list.name}</div>
                        </li>
                    );
                })}
            </ul>
        );
    };

    render() {
        const {className, dataSource} = this.props;
        const {currSelected, currList, isLoading, records} = this.state;

        const tabs = this.renderListTabs(dataSource, currSelected);

        return (
            <div className={className}>
                {tabs}

                <div className="list-records-container">
                    <YPRecordList
                        isLoading={isLoading}
                        listInfo={currList}
                        dataSource={records}
                        refresh={this.handleRefresh}
                    />
                </div>
            </div>
        );
    }
}
