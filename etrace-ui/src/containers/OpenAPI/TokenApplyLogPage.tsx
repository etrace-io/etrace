import {
    ApplyTokenAuditStatus,
    ApplyTokenLog,
    ApplyTokenLogSearchResult,
    TokenService,
    TokenStatus
} from "./TokenService";
import React from "react";
import {SelectValue} from "antd/lib/select";
import {ColumnProps} from "antd/lib/table";
import {Button, Radio, Select, Table} from "antd";
import {autobind} from "core-decorators";
import {observer} from "mobx-react";
import {observable} from "mobx";
import {ButtonType} from "antd/lib/button";
import moment from "moment";

const Option = Select.Option;

interface TokenApplyPageProps {
}

interface TokenApplyPageStates {
    isLoading: boolean;
    searchResult: ApplyTokenLogSearchResult;
}

@observer
export default class TokenApplyLogPage extends React.Component<TokenApplyPageProps, TokenApplyPageStates> {
    @observable selectedAuditStatus: ApplyTokenAuditStatus;

    public columns: ColumnProps<ApplyTokenLog>[] = [
        {
            title: "ID",
            dataIndex: "id",
            defaultSortOrder: "descend",
            sorter: (a, b) => a.id - b.id,
            width: 65
        },
        {
            title: "二级部门",
            dataIndex: "fatdeptname",
        },
        {
            title: "部门",
            dataIndex: "deptname",
        },
        {
            title: "申请人（工号）",
            render: (text: any, item: ApplyTokenLog, index: number) => {
                return (
                    <span>{item.createdBy}({item.userCode})</span>
                );
            }
        },
        {
            title: "Apply Reason",
            dataIndex: "applyReason",
        },
        {
            title: "Audit Opinion",
            dataIndex: "auditOpinion",
        },
        {
            title: "Audit Status",
            render: (text: any, item: ApplyTokenLog, index: number) => {
                return (
                    <Select value={item.auditStatus} onSelect={value => this.auditStatusOnChange(value, item)}>
                        {Object.keys(ApplyTokenAuditStatus).map(status => (
                            <Option key={status} value={status}>{status}</Option>
                        ))}
                    </Select>
                );
            }
        },

        {
            title: "创建时间",
            render(text: any, item: ApplyTokenLog, index: number) {
                return moment(item.updatedAt).format("YYYY-MM-DD HH:mm:ss");
            },
            width: 140
        },
        {
            title: "最后更新",
            dataIndex: "updatedBy",
        },
        {
            title: "更新时间",
            render(text: any, item: ApplyTokenLog, index: number) {
                return moment(item.updatedAt).format("YYYY-MM-DD HH:mm:ss");
            },
            width: 140
        },
        {
            title: "Action ",
            render: (text: any, item: ApplyTokenLog, index: number) => {
                return (
                    <div>
                        <Button onClick={() => this.update(item)}>更新 </Button>
                        <Button disabled={true} onClick={() => this.delete(item)}>删除 </Button>
                    </div>
                );
            }
        },
    ];

    constructor(props: any) {
        super(props);
        this.selectedAuditStatus = ApplyTokenAuditStatus.NOT_AUDIT;
        this.loadAllTokenInfo();
        this.state = {
            searchResult: null,
            isLoading: false,
        };
    }

    render() {
        const {searchResult, isLoading} = this.state;

        const {total, results} = searchResult || {total: 0, results: []};

        const radio = (
            <Radio.Group className="e-monitor-content-section" defaultValue={ApplyTokenAuditStatus.NOT_AUDIT} onChange={this.selectAndReload} buttonStyle="solid">
                <Radio.Button value={ApplyTokenAuditStatus.NOT_AUDIT}>未处理</Radio.Button>
                <Radio.Button value={ApplyTokenAuditStatus.AGREE}>已通过</Radio.Button>
                <Radio.Button value={ApplyTokenAuditStatus.REFUSED}>已拒绝</Radio.Button>
            </Radio.Group>
        );

        return (
            <div className="e-monitor-tab-content">
                <div className="e-monitor-content-sections flex">
                    <span>{radio} <b>Total: {total}</b></span>
                    <Table
                        className="e-monitor-content-section take-rest-height scroll"
                        loading={isLoading}
                        columns={this.columns}
                        dataSource={results}
                        rowKey={(item: ApplyTokenLog, index: number) => "" + index}
                        size="small"
                        bordered={true}
                    />
                </div>
            </div>
        );
    }

    @autobind
    private loadAllTokenInfo() {
        this.setState({isLoading: true});
        TokenService.adminQueryAllTokenApplyLog(this.selectedAuditStatus).then(tokenInfo => {
            this.setState({searchResult: tokenInfo, isLoading: false});
        });
    }

    @autobind
    private selectAndReload(e: any) {
        const auditStatus: ApplyTokenAuditStatus = e.target.value;
        if (this.selectedAuditStatus == auditStatus) {
            this.selectedAuditStatus = null;
        } else {
            this.selectedAuditStatus = auditStatus;
        }
        this.loadAllTokenInfo();
    }

    @autobind
    private update(log: ApplyTokenLog) {
        TokenService.adminUpdateTokenLog(log).then(ok => {
            this.loadAllTokenInfo();
        });
    }

    @autobind
    private delete(log: ApplyTokenLog) {
        TokenService.adminDeleteTokenLog(log.id).then(ok => {
            this.loadAllTokenInfo();
        });
    }

    @autobind
    private auditStatusOnChange(value: SelectValue, item: ApplyTokenLog) {
        item.auditStatus = ApplyTokenAuditStatus[value as string];
        item.shouldUpdate = true;
        this.setState({searchResult: this.state.searchResult});
    }

    @autobind
    private statusOnChange(value: SelectValue, item: ApplyTokenLog) {
        item.status = TokenStatus[value as string];
        item.shouldUpdate = true;
    }

    @autobind
    private buttonSelected(auditStatus: ApplyTokenAuditStatus): ButtonType {
        if (this.selectedAuditStatus != null && this.selectedAuditStatus == auditStatus) {
            return "primary";
        } else {
            return "default";
        }
    }
}
