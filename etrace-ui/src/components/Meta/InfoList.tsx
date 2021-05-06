import React from "react";
import {autobind} from "core-decorators";
import {Card, Input, Table} from "antd";

import "./InfoList.css";
import {observable} from "mobx";
import {observer} from "mobx-react";

interface InfoListProps {
    title?: string;
    placeholder?: string;
    columns?: Array<any>;
    dataSource?: Array<any>;
    searchField?: Array<string>;
}

interface InfoListState {
}

@observer
export class InfoList extends React.Component<InfoListProps, InfoListState> {
    @observable private dataSource: Array<any>;

    constructor(props: InfoListProps) {
        super(props);
        this.dataSource = this.props.dataSource;
    }

    componentWillReceiveProps(nextProps: Readonly<InfoListProps>): void {
        this.dataSource = nextProps.dataSource;
    }

    @autobind
    handleSearch(e: any) {
        const searchValue = e.target.value;
        if (searchValue) {
            if (!this.props.searchField || this.props.searchField.length === 0) {
                console.warn("please set `searchField` on InfoList component");
                return;
            }
            if (!this.props.dataSource) {
                return;
            }
            this.dataSource = this.props.dataSource.slice().map(record => {
                for (let field of this.props.searchField) {
                    if (record[field] && record[field].indexOf(searchValue) > -1) {
                        return record;
                    }
                }
                return null;
            }).filter(i => i);
        } else {
            this.dataSource = this.props.dataSource;
        }
    }

    render() {
        return (
            <Card className="meta-info-list-card" title={this.props.title}>
                <Input placeholder={this.props.placeholder} onChange={this.handleSearch}/>
                <Table
                    className="meta-info-list-table"
                    columns={this.props.columns}
                    dataSource={this.dataSource}
                    bordered={true}
                    size="middle"
                    rowKey={(record, index) => {
                        if (record.key) {
                            return record.key;
                        } else {
                            return (record ? JSON.stringify(record) : "") + index;
                        }
                    }}
                />
            </Card>
        );
    }
}
