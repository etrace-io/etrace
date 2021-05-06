import React from "react";
import {User} from "../../models/User";
import {Link} from "react-router-dom";
import {ListGridType} from "antd/es/list";
import StoreManager from "../../store/StoreManager";
import {DashboardNode} from "../../models/DashboardModel";
import ActionBtn from "../../components/MetaCardActionBtn";
import {changeNodeFavorite, deleteNode, getNodeList, rollbackNode} from "../../services/DashboardService";
import {Button, Card, Col, Input, Layout, List, Pagination, Popconfirm, Radio, Row} from "antd";
import {messageHandler} from "../../utils/message";
import {
    AppstoreOutlined,
    DeleteOutlined,
    EditOutlined,
    EyeOutlined,
    PlusOutlined,
    RollbackOutlined,
    StarFilled,
    StarOutlined,
    UserOutlined
} from "@ant-design/icons/lib";

const Content = Layout.Content;

interface DashboardNodeListProps {
}

interface DashboardNodeListStatus {
    dataSource: DashboardNode[];
    total: number;
    currPage: number;
    currTab: string;
    loading: boolean;
}

export default class DashboardNodeList extends React.Component<DashboardNodeListProps, DashboardNodeListStatus> {
    static LIST_PAGE_SIZE: number = 24;
    currUser: User = StoreManager.userStore.user;

    state = {
        dataSource: [],
        total: 0,
        currPage: 1,
        loading: false,
        currTab: "all",
    };

    componentDidMount(): void {
        const {currPage} = this.state;
        this.getNodeList(currPage);
    }

    getNodeList = (page: number, options?: any, isFavorite?: boolean) => {
        this.setState({loading: true});

        getNodeList(page, DashboardNodeList.LIST_PAGE_SIZE, options, isFavorite)
            .then(({results, total}) => {
                this.setState({
                    dataSource: results,
                    loading: false,
                    total,
                    currPage: page
                });
            });
    };

    handleFavoriteChanged = (id: number, target: boolean) => {
        changeNodeFavorite(id, target)
            .then(() => {
                const {currPage} = this.state;
                this.getNodeList(currPage);
            });
    };

    handleDeleteNode = (id: number) => {
        deleteNode(id)
            .then(() => {
                messageHandler("success", "节点已弃用，可至「弃用」面板恢复");
                const {currPage, total} = this.state;
                const page = (currPage !== 1 && ((total - 1) % DashboardNodeList.LIST_PAGE_SIZE === 0)) ? currPage - 1 : currPage;
                this.getNodeList(page);
            });
    };

    handleRollbackNode = (id: number) => {
        rollbackNode(id)
            .then(() => {
                messageHandler("success", "节点已启用");
                const {currPage, total} = this.state;
                const page = (currPage !== 1 && ((total - 1) % DashboardNodeList.LIST_PAGE_SIZE === 0)) ? currPage - 1 : currPage;
                this.getNodeList(page, {status: "Inactive"});
            });
    };

    handlePageChanged = (currPage: number) => {
        this.setState({currPage});
        this.getNodeList(currPage);
    };

    handleTabChanged = (e: any) => {
        const targetTab: any = e.target.value;
        const {currPage, currTab} = this.state;
        if (targetTab === currTab) {
            return;
        }

        this.setState({
            currTab: targetTab
        });

        const options = {
            user: targetTab === "mine" || targetTab === "favorite" ? this.currUser.psncode : null,
            status: targetTab === "delete" ? "Inactive" : "Active"
        };

        this.getNodeList(currPage, options, targetTab === "favorite");
    };

    handleSearchWithTitle = (title: string) => {
        const {currPage, currTab} = this.state;

        const options = {
            title,
            user: currTab === "mine" || currTab === "favorite" ? this.currUser.psncode : null,
        };

        this.getNodeList(currPage, options);
    };

    renderDashboardNodeListItem = (item: DashboardNode) => {
        const title = (
            <a className="e-monitor-content-list-item-title">{item.title}</a>
        );
        const desc = <p className="e-monitor-content-list-item-desc">{item.description}</p>;
        const editInfo = (
            <div style={{fontSize: 12}}>
                <div><b>创建人：{item.createdBy}</b></div>
                <div><b>更新者：{item.updatedBy}</b></div>
            </div>
        );

        const viewCount = <ActionBtn icon={<EyeOutlined/>} text={item.viewCount}/>;
        const favorite = (
            <ActionBtn
                onClick={() => this.handleFavoriteChanged(item.id, !item.star)}
                icon={item.star ? <StarFilled/> : <StarOutlined/>}
                text={item.favoriteCount}
            />
        );
        const edit = (
            <Link to={`/dashboard/node/edit/${item.id}`}>
                <ActionBtn icon={<EditOutlined/>} popoverContent={editInfo} popoverPlacement="topRight"/>
            </Link>
        );
        const deleteBtn = (
            <Popconfirm title={`确定弃用「${item.title}」吗？`} onConfirm={() => this.handleDeleteNode(item.id)}>
                <ActionBtn icon={<DeleteOutlined/>}/>
            </Popconfirm>
        );
        const rollbackBtn = (
            <Popconfirm title={`确定启用「${item.title}」吗？`} onConfirm={() => this.handleRollbackNode(item.id)}>
                <ActionBtn icon={<RollbackOutlined/>}/>
            </Popconfirm>
        );

        const opBtn = item.status === "Active" ? deleteBtn : rollbackBtn;

        const actions = [opBtn, favorite, viewCount, edit];
        return (
            <List.Item className="e-monitor-content-list-item">
                <Card actions={actions}>
                    <Card.Meta className="e-monitor-content-list-item-meta" title={title} description={desc}/>
                </Card>
            </List.Item>
        );
    };

    render() {
        const {dataSource, total, currPage, loading} = this.state;
        const listGrid: ListGridType = {xxl: 6, xl: 6, lg: 4, md: 4, sm: 3, xs: 2};
        return (
            <Content className="e-monitor-content-sections with-footer flex">

                <Card className="e-monitor-content-section">
                    <Row gutter={8}>
                        <Col span={8}>
                            <Radio.Group defaultValue="all" onChange={this.handleTabChanged}>
                                <Radio.Button value="all"><AppstoreOutlined/> 所有</Radio.Button>
                                <Radio.Button value="favorite"><StarOutlined/> 收藏</Radio.Button>
                                <Radio.Button value="mine"><UserOutlined/> 我的</Radio.Button>
                                <Radio.Button value="delete"><DeleteOutlined/> 弃用</Radio.Button>
                            </Radio.Group>
                        </Col>
                        <Col span={8}>
                            <Input.Search
                                placeholder="请输入查询条件"
                                onSearch={this.handleSearchWithTitle}
                            />
                        </Col>

                        <Col span={8} style={{textAlign: "right"}}>
                            <Link to="/dashboard/node/new">
                                <Button type="primary" icon={<PlusOutlined/>}>新建</Button>
                            </Link>
                        </Col>
                    </Row>
                </Card>

                <List
                    loading={loading}
                    className="e-monitor-content-section e-monitor-content-list"
                    grid={listGrid}
                    dataSource={dataSource}
                    renderItem={this.renderDashboardNodeListItem}
                />

                {total > 0 && (
                    <Card className="e-monitor-content-section" style={{marginTop: 2, marginBottom: 4}}>
                        <Pagination
                            pageSize={DashboardNodeList.LIST_PAGE_SIZE}
                            style={{textAlign: "right"}}
                            current={currPage}
                            total={total}
                            showTotal={t => `总共 ${t} 条`}
                            onChange={this.handlePageChanged}
                        />
                    </Card>
                )}
            </Content>
        );
    }
}
