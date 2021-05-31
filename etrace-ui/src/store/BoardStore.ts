import {action, observable, toJS} from "mobx";
import {Board, Link, MetricVariate, Variate, VariateType} from "$models/BoardModel";
import * as BoardService from "../services/BoardService";
import * as notification from "../utils/notification";
import {get} from "lodash";
import {URLParamStore} from "./URLParamStore";
import StoreManager from "./StoreManager";
import {Chart} from "$models/ChartModel";
import {PageSwitchStore} from "./PageSwitchStore";

const R = require("ramda");

export class BoardStore {
    urlParamStore: URLParamStore;
    pageSwitchStore: PageSwitchStore;
    @observable boards: Array<Board> = [];
    @observable public board: Board;
    @observable public chart: Chart;
    @observable public discardCharts: Array<Chart> = [];
    @observable public error: any;
    @observable public initialize: boolean = false;

    constructor(urlParamStore: URLParamStore, pageSwitchStore: PageSwitchStore) {
        this.urlParamStore = urlParamStore;
        this.pageSwitchStore = pageSwitchStore;
    }

    @action
    public reset(): void {
        this.initialize = false;
        this.boards = [];
        this.board = null;
        this.chart = null;
        this.error = null;
        this.discardCharts = [];
        this.pageSwitchStore.setPromptSwitch(false);
    }

    @action
    public setChart(chart: Chart) {
        this.chart = chart;
    }

    @action
    public setBoard(board: Board) {
        this.board = Object.assign({}, board);
        this.initialize = false;
        this.setDiscardCharts();
        this.buildBoardURL(board);
    }

    @action
    public setChangeBoard(board: Board) {
        this.pageSwitchStore.setPromptSwitch(true);
        this.setBoard(board);
    }

    @action
    public setAdminVisible(adminVisible: boolean) {
        this.board.adminVisible = adminVisible;
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public unRegisterBoard() {
        this.chart = null;
        this.board = null;
        this.initialize = false;
        this.error = null;
        this.discardCharts = [];
    }

    @action
    public setBoards(boards: Array<Board>) {
        this.boards = boards;
    }

    @action
    public setBoardId(boardId: number) {
        if (this.initialize) {
            return;
        }
        this.reset();
        // let board: Board = await BoardService.get(boardId);
        BoardService.get(boardId).then(board => {
            this.buildBoard(board);
        }).catch(err => {
            let resp = err.response;
            if (resp) {
                notification.errorHandler({
                    message: `获取面板（${resp.status}）`,
                    description: resp.data.message
                });
                this.error = {status: resp.status, description: resp.data.message};
            } else {
                notification.errorHandler({message: "获取面板", description: err.message});
                this.error = {status: 404, description: err.message};
            }
        });
        // if (board) {
        //     // 必须先设置URL参数，再设置store中board的值，否则board render的时候可能拿不到URL中的值
        //     this.buildBoardURL(board);
        //     this.buildBoard(board);
        // }else {
        //     console.log(234,board)
        // }
        this.initialize = false;
    }

    /**
     * 根据看板配置，初始化URL参数，如果URL参数已经包括了参数，优化以URL上取值
     * @param board
     */
    @action
    public buildBoardURL(board: Board) {
        const urlParams = {
            refresh: StoreManager.urlParamStore.getValue("refresh") || get(board, "config.refresh", null),
            from: StoreManager.urlParamStore.getValue("from") || get(board, "config.time.from", null),
            to: StoreManager.urlParamStore.getValue("to") || get(board, "config.time.to", null)
        };
        const variates = get(board, "config.variates", []);
        variates.forEach(variate => {
            const value = StoreManager.urlParamStore.getValues(variate.name);
            urlParams[`${variate.name}`] = (value && value.length > 0) ? value : variate.current;
        });
        StoreManager.urlParamStore.changeURLParams(urlParams, [], false, "replace");
    }

    @action
    public removeVariate(name: string) {
        const variates = this.board.config.variates;
        const index = R.findIndex(R.propEq("name", name))(variates);
        if (index >= 0) {
            variates.splice(index, 1);
            // also delete 'relatedTags'
            variates.forEach(v => {
                if (v.type == VariateType.METRIC && (v as MetricVariate).relatedTagKeys &&
                    (v as MetricVariate).relatedTagKeys.indexOf(name) >= 0) {
                    (v as MetricVariate).relatedTagKeys.splice((v as MetricVariate).relatedTagKeys.indexOf(name), 1);
                }
            });
            this.board.config.variates = variates;
            this.pageSwitchStore.setPromptSwitch(true);
        }
    }

    @action
    public editLink(link: Link, index: number) {
        let config = this.board.config;
        let links: Array<Link> = get(config, "links", []);
        links[index] = R.mergeDeepRight(links[index], link);
        this.board.config.links = links;
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public removeLink(index: number) {
        let config = this.board.config;
        let links: Array<Link> = get(config, "links", []);
        links.splice(index, 1);
        this.board.config.links = links;
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public addLink(link: Link) {
        console.log({link});
        let config = this.board.config;
        let links: Array<Link> = get(config, "links", []);
        links.push(link);
        console.log(links);
        this.board.config.links = links;
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public setVariate(variate: Variate) {
        let config = this.board.config;
        let variates: Array<any> = config.variates || [];
        // 以name做唯一主键
        const index = R.findIndex(R.propEq("name", variate.name))(variates);
        if (index < 0) {
            variates.push(variate);
        } else {
            variates[index] = variate;
        }
        this.board.config = config;
        this.board.config.variates = variates;
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public setVariates(variates: Array<Variate>) {
        this.board.config.variates = variates;
        this.pageSwitchStore.setPromptSwitch(true);
    }

    @action
    public buildBoard(board: Board) {
        if (board) {
            if (!board.layout) {
                board.layout = [];
                board.layout.push({title: "新的行", panels: []});
            }
            if (board.config) {
                if (!board.config.variates) {
                    board.config.variates = [];
                }
            } else {
                // init board config
                board.config = {};
                board.config.variates = [];
            }
            this.setBoard(board);
        }
    }

    // return immutable Board!
    // TODO：移除这个方法（每次返回的 board 对象不一样导致重新 render）
    public getImmutableBoard(): Board {
        if (this.board) {
            return toJS(this.board);
        }
        return null;
    }

    // 若是从其他环境同步的chartId不是本环境正确的ch
    public getRightChartId(chartId: number, globalId: string) {
        if (this.board && this.board.charts && globalId) {
            const relatedChartByGlobalId = this.board.charts.filter(chart => chart.globalId == globalId)[0];
            if (relatedChartByGlobalId) {
                return relatedChartByGlobalId.id;
            }
        }
        return chartId;
    }

    public setDiscardCharts() {
        if (this.board && this.board.charts) {
            this.board.charts.forEach(chart => {
                if (chart.status && chart.status == "Inactive") {
                    this.discardCharts.push(chart);
                }
            });
        }
    }

    public getDiscardCharts() {
        return toJS(this.discardCharts);
    }

    public removeDiscardCharts() {
        if (this.board && this.discardCharts.length > 0) {
            let chartIds = this.board.chartIds.filter((value => {
                return !this.discardCharts.find(chart => chart.id == value);
            }));
            let layouts = toJS(this.board.layout).map(value => {
                let panels = value.panels.filter(panel => {
                    return !this.discardCharts.find(chart => chart.id == panel.chartId);
                });
                value.panels = panels;
                return value;
            });
            this.board.chartIds = chartIds;
            this.board.layout = layouts;
            BoardService.save(this.board).then(value => {
                this.discardCharts = [];
            });
        }
    }

    public getChart(chartId: number, globalId: string) {
        if (!this.board || !this.board.charts) {
            return null;
        }
        let result;

        // 优先搜索`globalId`，因为他是unique，而`id`不是unique
        if (globalId) {
            result = R.find(R.propEq("globalId", globalId))(this.board.charts);
        }
        if (!result) {
            result = R.find(R.propEq("id", chartId))(this.board.charts);
        }
        return result;
    }
}
