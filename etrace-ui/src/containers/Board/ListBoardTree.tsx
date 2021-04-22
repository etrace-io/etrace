import {observer} from "mobx-react";
import React from "react";
import StoreManager from "../../store/StoreManager";
import {isEmpty, uniqueId} from "$utils/Util";
import {Button, Dropdown, Input, Menu, Popover} from "antd";

import {Link} from "react-router-dom";
import {autobind} from "core-decorators";
import {Board} from "$models/BoardModel";
import {toJS} from "mobx";
import {AppstoreOutlined, CaretDownOutlined, QuestionOutlined} from "@ant-design/icons/lib";
import ChartPreview from "$components/Chart/ChartPreview";

const MenuItem = Menu.Item;
const SubMenu = Menu.SubMenu;
const MenuDivider = Menu.Divider;
const ButtonGroup = Button.Group;

interface ListBoardTreeProps {
    board: Board;
}

interface ListBoardTreeState {
    fieldKeysSearch: Array<any>;
    viewChart: boolean;
    chart: any;
}

@observer
export class ListBoardTree extends React.Component<ListBoardTreeProps, ListBoardTreeState> {
    productLineStore;
    userActionStore;
    boardStore;
    department: Map<string, string> = new Map<string, string>();
    productLine: Map<string, string> = new Map<string, string>();

    constructor(props: ListBoardTreeProps) {
        super(props);
        this.productLineStore = StoreManager.productLineStore;
        this.userActionStore = StoreManager.userActionStore;
        this.boardStore = StoreManager.boardStore;
        this.userActionStore.loadAllUserAction();
        this.state = {
            fieldKeysSearch: [],
            viewChart: false,
            chart: null
        };
    }

    componentDidMount() {
        this.loadValues();
    }

    @autobind
    async loadValues() {
        let boardLists = this.productLineStore.boardLists;
        if (isEmpty(boardLists)) {
            await this.productLineStore.searchBoardList({pageSize: 9999});
        }
    }

    @autobind
    filterBoard(e: any) {
        let search = e.target.value;
        const boardLists: Array<any> = toJS(this.productLineStore.boardLists);
        let boards = [];
        if (search != "") {
            if (!isEmpty(boardLists)) {
                boards = boardLists.filter(value => {
                    let lower = value.title.toLocaleLowerCase();
                    let lowerSearch = search.toLocaleLowerCase();
                    return lower.indexOf(lowerSearch) >= 0;
                });
            }
        }
        this.setState({
            fieldKeysSearch: boards
        });
    }

    @autobind
    searchFilterBoard() {
        const {fieldKeysSearch} = this.state;
        let favorite = [];
        if (fieldKeysSearch) {
            fieldKeysSearch.forEach(board => {
                favorite.push(
                    <MenuItem key={board.id + "搜索面板"}>
                        <Popover
                            placement="right"
                            key={board.id + "popover_user"}
                            content={
                                <div style={{fontSize: 12}}>
                                    <div><b>{"创建者：" + board.createdBy}</b></div>
                                    <div><b>{"更新者：" + board.updatedBy}</b></div>
                                </div>}
                        >
                            <Link to={"/board/view/" + board.id} key={board.globalId}>
                                {board.title}
                            </Link>
                        </Popover>
                    </MenuItem>);
            });
        } else {
            favorite.push(<MenuItem key={"没有搜索面板"}>没有相应面板</MenuItem>);
        }
        return favorite;
    }

    buildDepartmentMenu() {
        const menu = [];
        const boardLists = toJS(this.productLineStore.boardLists);
        const allFavoriteBoards = this.userActionStore.getAllFavoriteBoards();
        const allViewBoards = this.userActionStore.getAllViewBoards();
        this.department = new Map<string, string>();
        this.productLine = new Map<string, string>();
        menu.push(
            <SubMenu
                title={<Input size="small" placeholder="搜索面板" onChange={this.filterBoard}/>}
                key={"搜索面板"}
            >
                {this.searchFilterBoard()}
            </SubMenu>
        );
        if (!isEmpty(allFavoriteBoards)) {
            let favorite = [];
            allFavoriteBoards.forEach(board => {
                favorite.push(
                    <MenuItem key={board.id + "收藏面板"}>
                        <Popover
                            placement="right"
                            key={board.id + "popover_user"}
                            content={
                                <div style={{fontSize: 12}}>
                                    <div><b>{"创建者：" + board.createdBy}</b></div>
                                    <div><b>{"更新者：" + board.updatedBy}</b></div>
                                </div>}
                        >
                            <Link to={"/board/view/" + board.id} key={board.globalId}>
                                {board.title}
                            </Link>
                        </Popover>
                    </MenuItem>);
            });
            menu.push(
                <SubMenu title={"收藏面板"} key={"收藏面板"}>{favorite}</SubMenu>
            );
        }
        if (!isEmpty(allViewBoards)) {
            let view = [];
            allViewBoards.forEach(board => {
                view.push(
                    <MenuItem key={board.id + "浏览历史记录"}>
                        <Popover
                            placement="right"
                            key={board.id + "popover_user"}
                            content={
                                <div style={{fontSize: 12}}>
                                    <div><b>{"创建者：" + board.createdBy}</b></div>
                                    <div><b>{"更新者：" + board.updatedBy}</b></div>
                                </div>}
                        >
                            <Link to={"/board/view/" + board.id} key={board.globalId}>
                                {board.title}
                            </Link>
                        </Popover>
                    </MenuItem>);
            });
            menu.push(
                <SubMenu title={"浏览历史记录"} key={"浏览历史记录"}>{view}</SubMenu>
            );
            menu.push(
                <MenuDivider key={"divider"}/>
            );
        }
        boardLists.forEach(board1 => {
            if (board1.departmentName) {
                if (!this.department.get(board1.departmentName)) {
                    this.department.set(board1.departmentName, board1.departmentName);
                    const menuLine = [];
                    let departmentTotal = 0;
                    boardLists.forEach(board2 => {
                        if (board2.productLineName) {
                            if (board2.departmentName == board1.departmentName &&
                                !this.productLine.get(board2.productLineName)) {
                                this.productLine.set(board2.productLineName, board2.productLineName);
                                const menuTitle = [];
                                boardLists.forEach(board3 => {
                                    if (board3.departmentName == board2.departmentName && board3.productLineName == board2.productLineName) {
                                        const board3Id = uniqueId();
                                        menuTitle.push(
                                            <MenuItem key={board3.departmentId + board3.productLineId + board3Id}>
                                                <Popover
                                                    placement="right"
                                                    key={board3Id + "popover_user"}
                                                    content={
                                                        <div style={{fontSize: 12}}>
                                                            <div><b>{"创建者：" + board3.createdBy}</b></div>
                                                            <div><b>{"更新者：" + board3.updatedBy}</b></div>
                                                        </div>}
                                                >
                                                    <Link to={"/board/view/" + board3.id} key={board3.globalId}>
                                                        {board3.title}
                                                    </Link>
                                                </Popover>
                                            </MenuItem>);
                                    }
                                });
                                const menuLineTitle = (
                                    <span>
                                {board2.productLineName}
                                        <span className="e-monitor-menu-count">{menuTitle.length}</span>
                            </span>
                                );
                                menuLine.push(
                                    <SubMenu
                                        title={menuLineTitle}
                                        key={uniqueId() + board2.departmentId + "_" + board2.productLineId}
                                    >{menuTitle}
                                    </SubMenu>
                                );
                                departmentTotal += menuTitle.length;
                            }
                        }
                    });
                    const title = (
                        <span>
                        {board1.departmentName}
                            <span className="e-monitor-menu-count">{departmentTotal}</span>
                    </span>
                    );
                    menu.push(
                        <SubMenu
                            title={title}
                            key={board1.departmentId + uniqueId()}
                        >{menuLine}
                        </SubMenu>
                    );
                }
            }
        });
        return (<Menu style={{zIndex: 10}}>{menu}</Menu>);
    }

    async queryChart(chart: any) {
        this.setState({
            viewChart: true,
            chart: chart
        });
    }

    @autobind
    removeAllDiscardCharts() {
        this.boardStore.removeDiscardCharts();
    }

    @autobind
    chartModalOk() {
        this.setState({
            viewChart: false
        });
    }

    render() {
        const board = this.boardStore.getImmutableBoard();
        const discardCharts = this.boardStore.getDiscardCharts();
        const content = (
            <div>
                {discardCharts.map(chart => {
                    return (<div key={chart.globalId}><a onClick={() => this.queryChart(chart)}>{chart.title}</a> 由 <b>{chart.createdBy}</b> 创建 </div>);
                })}
            </div>
        );
        return (
            <div>
                <ButtonGroup>
                    <Dropdown trigger={["click"]} overlay={this.buildDepartmentMenu()}>
                        <Button
                            htmlType="button"
                            type="dashed"
                            icon={<AppstoreOutlined />}
                            style={{backgroundColor: "transparent"}}
                        >{board ? board.title : ""}<CaretDownOutlined />
                        </Button>
                    </Dropdown>
                    {discardCharts.length > 0 && (
                        <Popover
                            trigger="hover"
                            title={
                                <div>{"配置中废弃指标"}
                                    <a style={{float: "right"}} onClick={this.removeAllDiscardCharts}>移除</a>
                                </div>}
                            content={content}
                        >
                            <Button icon={<QuestionOutlined />} type="primary"/>
                        </Popover>
                    )}
                </ButtonGroup>
                {
                    this.state.viewChart && (
                        <ChartPreview chart={this.state.chart} visible={this.state.viewChart} onOk={this.chartModalOk}/>
                    )
                }
            </div>
        );
    }
}
