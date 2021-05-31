import moment from "moment";
import {ColumnProps} from "antd/lib/table";
import {Button, Popover, Table} from "antd";
import React, {useEffect, useState} from "react";
import {TIME_FORMAT} from "$models/TimePickerModel";
import {FieldTimeOutlined} from "@ant-design/icons/lib";
import {HistoryApiService} from "$services/HistoryApiService";

export class HistoryType {
    public static CHART = new HistoryType("chart", "指标");
    public static DASHBOARD = new HistoryType("dashboard", "看板");
    public static DASHBOARDAPP = new HistoryType("dashboardApp", "App看板");

    public type: string;
    public cname: string;

    private constructor(type: string, cname: string) {
        this.type = type;
        this.cname = cname;
    }
}

interface HistoryResult {
    results: any[];
    total: number;
}

interface HistoryCardProps {
    type: HistoryType;
    id: number;
    applyFunction: (config: any, historyId: number) => void;
}

const HistoryPopover: React.FC<HistoryCardProps> = props => {
    const {type, id, applyFunction} = props;

    const [visible, setVisible] = useState<boolean>(false);
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [histories, setHistories] = useState<HistoryResult>();

    useEffect(() => {
        id >= 0 && queryData();
    }, [id]);

    const queryData = (page: number = 1) => {
        setIsLoading(true);
        HistoryApiService.queryHistory(type.type, id, page)
            .then(data => {
                setHistories(data.data);
            })
            .catch(err => {
                alert(type.type + ", id: " + id + err);
            })
            .finally(() => {
                setIsLoading(false);
            });
    };

    const historyTable = (
        <HistoryContent
            configs={histories ? histories.results : []}
            total={histories ? histories.total : 0}
            onChange={queryData}
            applyFunction={applyFunction}
            loading={isLoading}
        />
    );

    return (
        <Popover
            trigger="click"
            content={historyTable}
            placement="bottomRight"
            visible={visible}
            onVisibleChange={v => setVisible(v && histories && histories.total > 0)}
        >
            <Button disabled={!histories || (histories && histories.total === 0)} icon={<FieldTimeOutlined />}>
                {type.cname}历史 [{histories ? histories.total : 0} 条]
            </Button>
        </Popover>
    );
};

export default HistoryPopover;

interface HistoryContentProps {
    total: number;
    loading: boolean;
    configs: Array<any>;
    onChange: (page: number) => void;
    applyFunction: (config: any, historyId: number) => void;
}

const HistoryContent: React.FC<HistoryContentProps> = props => {
    const {loading, total, configs, onChange, applyFunction} = props;

    const columns: ColumnProps<any>[] = [{
        title: "更新时间",
        render: (text: any, item: any, index: number) => moment(item.history.updatedAt).format(TIME_FORMAT),
        dataIndex: "id"
    }, {
        title: "更新人",
        align: "center",
        render: (text: any, item: any, index: number) => item.history.updatedBy
    }, {
        title: "操作",
        align: "center",
        render: (text: any, item: any, index: number) => (
            <Button size="small" onClick={() => applyFunction(item.history, item.id)}>预览</Button>
        )
    }];

    return (
        <Table
            loading={loading}
            rowKey={record => record.id}
            columns={columns}
            dataSource={configs}
            size="small"
            bordered={true}
            pagination={{
                hideOnSinglePage: true,
                size: "small",
                total: total,
                showTotal: () => `共 ${total} 条历史记录`,
                pageSize: 10,
                onChange: (page) => onChange(page),
            }}
        />
    );
};
