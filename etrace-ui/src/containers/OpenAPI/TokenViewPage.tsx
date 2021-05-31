import {ApiToken, TokenService, TokenStatus} from "./TokenService";
import React from "react";
import {SelectValue} from "antd/lib/select";
import {ColumnProps} from "antd/lib/table";
import {Button, Select, Table} from "antd";
import {autobind} from "core-decorators";
import {observer} from "mobx-react";
import moment from "moment";

const Option = Select.Option;

interface TokenViewPageProps {
}

interface TokenViewPageStates {
    allTokens: ApiToken[];
}

@observer
export default class TokenViewPage extends React.Component<TokenViewPageProps, TokenViewPageStates> {

    public columns: ColumnProps<ApiToken>[] = [
        {
            title: "ID",
            dataIndex: "id",
            width: 50,
            fixed: "left",
            sorter: (a, b) => a.id - b.id,
        },
        {
            title: "申请人（工号）",
            render: (text: any, item: ApiToken, index: number) => {
                return (
                    <span>{item.psnname}({item.userCode})</span>
                );
            },
            width: 140,
            fixed: "left"
        },
        {
            title: "Status",
            render: (text: any, item: ApiToken, index: number) => {
                return (
                    <Select defaultValue={item.status} onChange={value => this.statusOnChange(value, item)}>
                        {Object.keys(TokenStatus).map(status => (
                            <Option key={status} value={status}>
                                {status == "Active" ? (<b color="green">Active</b>) : (<i color="red">{status}</i>)}
                            </Option>
                        ))}
                    </Select>);
            },
            width: 100,
            fixed: "left"
        },
        {
            title: "二级部门",
            dataIndex: "fatdeptname",
            width: 100,
        },
        {
            title: "部门",
            dataIndex: "deptname",
            width: 100,
        },
        {
            title: "创建人",
            dataIndex: "createdBy",
            width: 100,
        },

        {
            title: "创建时间",
            render(text: any, item: ApiToken, index: number) {
                return moment(item.createdAt).format("YYYY-MM-DD HH:mm:ss");
            },
            width: 140,
        },
        {
            title: "最后更新",
            dataIndex: "updatedBy",
            width: 100,
        },
        {
            title: "更新时间",
            render(text: any, item: ApiToken, index: number) {
                return moment(item.updatedAt).format("YYYY-MM-DD HH:mm:ss");
            },
            width: 140
        },
        {
            title: "CID",
            dataIndex: "cid",
            width: 400,
        },
        {
            title: "Token",
            dataIndex: "token",
            width: 400,
        },
        {
            title: "AlwaysAccess",
            dataIndex: "alwaysAccess",
            width: 100,
        },
        {
            title: "Action ",
            render: (text: any, item: ApiToken, index: number) => {
                return (
                    <div>
                        <Button onClick={() => this.update(item)}>更新 </Button>
                    </div>
                );
            },
            width: 100,
            fixed: "right"
        },
    ];

    constructor(props: any) {
        super(props);
        this.loadAllTokenInfo();
        this.state = {
            allTokens: []
        };
    }

    render() {
        return (
            <div className="e-monitor-tab-content">
                <div className="e-monitor-content-sections flex">
                    <Table
                        className="e-monitor-content-section take-rest-height scroll"
                        columns={this.columns}
                        dataSource={this.state.allTokens}
                        rowKey={(item: ApiToken, index: number) => item.id.toString()}
                        size="small"
                        bordered={true}
                        // scroll={{x: 2000}}
                    />
                </div>
            </div>
        );
    }

    @autobind
    private loadAllTokenInfo() {
        TokenService.adminQueryAllToken().then(allTokens => {
            this.setState({allTokens: allTokens});
        });
    }

    @autobind
    private update(log: ApiToken) {
        TokenService.adminUpdateApiToken(log).then(ok => {
            this.loadAllTokenInfo();
        });
    }

    @autobind
    private statusOnChange(value: SelectValue, item: ApiToken) {
        item.status = TokenStatus[value as string];
    }
}
