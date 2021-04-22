import {observer} from "mobx-react";
import useUser from "$hooks/useUser";
import {Link, useHistory} from "react-router-dom";
import {Chart} from "$models/ChartModel";
import StoreManager from "$store/StoreManager";
import {METRIC_EDIT} from "$constants/Route";
import ListView from "$components/ListView/ListView";
import MetaCard from "$components/MetaCard/MetaCard";
import * as ChartService from "$services/ChartService";
import ChartPreview from "$components/Chart/ChartPreview";
import React, {useEffect, useRef, useState} from "react";
import {EMonitorSection} from "$components/EMonitorLayout";
import {Button, Input, Radio, Space, Tooltip} from "antd";
import {PaginationCard} from "$components/Pagination/Pagination";
import {
    AppstoreOutlined,
    CopyOutlined,
    DeleteOutlined,
    LockOutlined,
    PlusOutlined,
    UserOutlined
} from "@ant-design/icons/lib";

enum ChartListTab {
    ALL = "all",
    MINE = "mine",
    INACTIVE = "inactive",
}

const ChartListPage: React.FC = props => {
    const {productLineStore} = StoreManager;

    const params = useRef({
        title: null,
        productLineId: null,
        departmentId: null,
        pageNum: null,
        pageSize: 30,
        user: null,
        status: null
    });

    const user = useUser();
    const history = useHistory();

    const [chart, setChart] = useState<any>({});
    const [currPage, setCurrPage] = useState<number>(1);
    const [showChart, setShowChart] = useState<boolean>(false);

    useEffect(() => {
        productLineStore.setParams("type", "Chart");
        productLineStore.setParams("status", "Active");
        productLineStore.init(ChartListTab.ALL);

        fetchChartList();

        return () => productLineStore.reset();
    }, []);

    const fetchChartList = (title?: string) => {
        if (typeof title === "string") {
            if (title !== params.current.title) {
                setCurrPage(1);
                params.current.pageNum = 1;
            }

            params.current.title = title;
        }

        const tab = productLineStore.currentTab;
        params.current.status = tab === ChartListTab.INACTIVE ? "Inactive" : null;
        params.current.user = tab === ChartListTab.MINE ? user.psncode : null;

        productLineStore.setParams(
            "status",
            tab === ChartListTab.INACTIVE ? "Inactive" : "Active",
        );

        productLineStore.searchCharts(params.current).then();
    };

    const queryChart = (chartId: number) => {
        ChartService.get(chartId).then((c: Chart) => {
            setChart(c);
            setShowChart(true);
        });
    };

    const handleTabChange = (e: any) => {
        const tab = e.target.value;
        params.current.pageNum = 1;
        productLineStore.init(tab);
        setCurrPage(1);
        fetchChartList();
    };

    const handleChartDelete = (id: number, isDelete: boolean) => {
        const fn = () => {
            fetchChartList();
        };

        isDelete
            ? ChartService.deleteChartById(id).then(fn)
            : ChartService.rollbackChartById(id).then(fn);
    };

    const handlePageChange = (page: number, size: number) => {
        setCurrPage(page);
        params.current.pageNum = page;
        params.current.pageSize = size;
        fetchChartList();
    };

    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item type="card">
                <Space>
                    <Radio.Group defaultValue={ChartListTab.ALL} onChange={handleTabChange}>
                        <Radio.Button value={ChartListTab.ALL}><AppstoreOutlined/> 所有</Radio.Button>
                        <Radio.Button value={ChartListTab.MINE}><UserOutlined/> 我的</Radio.Button>
                        <Radio.Button value={ChartListTab.INACTIVE}><DeleteOutlined/> 废弃</Radio.Button>
                    </Radio.Group>

                    <Input.Search
                        style={{width: 350}}
                        placeholder="请输入查询条件"
                        onSearch={title => fetchChartList(title)}
                    />

                    <Link to="/board/explorer">
                        <Button type="primary" icon={<PlusOutlined/>} ghost={true}>新建</Button>
                    </Link>
                </Space>
            </EMonitorSection.Item>

            <EMonitorSection.Item scroll={true}>
                <ListView
                    loading={productLineStore.boardLoading}
                    dataSource={productLineStore.charts}
                    renderItem={(item: any) => {
                        const title = <>
                            {item.adminVisible && (
                                <Tooltip
                                    title="仅 Admin 可更改、查看"
                                    placement="top"
                                    color="volcano"
                                ><LockOutlined/>&nbsp;
                                </Tooltip>
                            )}
                            <Tooltip
                                placement="topLeft"
                                color="blue"
                                title={(item.description || "").trim() ? item.description.trim() : item.title}
                            >
                                {item.title}
                            </Tooltip>
                        </>;

                        return <MetaCard
                            id={item.id}
                            title={title}
                            onClick={() => queryChart(item.id)}
                            department={[item.departmentName, item.productLineName]}
                            isDeleted={item.status === "Inactive"}
                            onDelete={(id, isDelete) => handleChartDelete(id, isDelete)}
                            editLink={(id) => `${METRIC_EDIT}/${id}`}
                            editInfo={[`创建者：${item.createdBy}`, `更新者：${item.updatedBy}`]}
                            extraActions={[{
                                icon: <CopyOutlined />,
                                popover: "复制当前指标",
                                onClick: () => history.push({
                                    pathname: "/board/explorer",
                                    state: { chartId: item.id }
                                })
                            }]}
                        />;
                    }}
                />
            </EMonitorSection.Item>

            {productLineStore.chartTotal > 0 && (
                <EMonitorSection.Item>
                    <PaginationCard
                        defaultPageSize={30}
                        current={currPage}
                        onChange={handlePageChange}
                        total={productLineStore.chartTotal}
                    />
                </EMonitorSection.Item>
            )}

            <ChartPreview
                chart={chart}
                visible={showChart}
                onOk={() => setShowChart(false)}
            />
        </EMonitorSection>
    );
};

export default observer(ChartListPage);
