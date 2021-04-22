import * as API from "$utils/api";
import * as notification from "$utils/notification";
import {handleError} from "$utils/notification";
import {Board} from "$models/BoardModel";
import * as messageUtil from "$utils/message";
import {CURR_API} from "$constants/API";

export async function search(board: object) {
    let url = CURR_API.monitor + "/dashboard";
    let message = "获取面板";
    try {
        let resp = await API.Get(url, board);
        let boardList = resp.data;
        return boardList;
    } catch (err) {
        handleError(err, message);
    }
}

export async function searchBoards(path: string, board: object) {
    const url = `${CURR_API.monitor}/${path}?${Object.keys(board).map(key => board[key] ? `${key}=${board[key]}` : null).filter(Boolean).join("&")}`;
    let message = "获取面板";
    try {
        let resp = await API.Get(encodeURI(url));
        let boardList: Array<any> = resp.data;
        if (!boardList) {
            return [];
        }
        return boardList;
    } catch (err) {
        handleError(err, message);
    }
}

export async function get(boardId: number) {
    let url = CURR_API.monitor + "/dashboard/" + boardId;
    // let message = "获取面板";
    // try {
    let resp = await API.Get(url);
    let board: Board = resp.data;
    return board;
    // } catch (err) {
    //     let resp = err.response;
    //     if (resp) {
    //         notification.errorHandler({
    //             message: message + "(" + resp.status + ")",
    //             description: resp.data.message,
    //             duration: duration
    //         });
    //     } else {
    //         notification.errorHandler({message: message, description: err.message, duration: duration});
    //     }
    // }
}

export async function getByGroup(globalId: number) {
    let url = CURR_API.monitor + "/dashboard";
    let message = "获取面板";
    try {
        let resp = await API.Get(url, {globalId: globalId});
        let dashboard: any = resp.data;
        if (dashboard && dashboard.results && dashboard.results.length > 0) {
            return dashboard.results[0];
        }
    } catch (err) {
        handleError(err, message);
    }
}

export async function save(board: any) {
    let url = CURR_API.monitor + "/dashboard";
    let message = "更新面板信息";
    try {
        if (board.layout) {
            let layouts: Array<any> = [];
            for (let layout of board.layout) {
                let panels: Array<any> = [];
                for (let panel of layout.panels) {
                    panels.push({chartId: panel.chartId, span: panel.span, globalId: panel.globalId});
                }
                layouts.push({
                    title: layout.title,
                    panels,
                    chartHeight: layout.chartHeight,
                    titleShow: layout.titleShow,
                    defaultFold: layout.defaultFold,
                });
            }
            board.layout = layouts;
        }
        let resp = await API.Put(url, board);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function syncBoard(board: any, monitorUrl: string) {
    let url = monitorUrl + "/dashboard/sync";
    let message = "同步面板信息";
    try {
        if (board.layout) {
            let layouts: Array<any> = [];
            for (let layout of board.layout) {
                let panels: Array<any> = [];
                for (let panel of layout.panels) {
                    panels.push({chartId: panel.chartId, span: panel.span, globalId: panel.globalId});
                }
                layouts.push({
                    title: layout.title,
                    panels,
                    chartHeight: layout.chartHeight,
                    titleShow: layout.titleShow,
                    defaultFold: layout.defaultFold,
                });
            }
            board.layout = layouts;
        }
        let resp = await API.Put(url, board);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
        throw  err;
    }
}

export async function saveChartIds(board: any) {
    let url = CURR_API.monitor + "/dashboard/" + board.id + "/charts";
    let message = "更新面板信息";
    try {
        let resp = await API.Put(url, board);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function create(board: object) {
    let url = CURR_API.monitor + "/dashboard";
    let message = "新建面板信息";
    try {
        let resp = await API.Post(url, board);
        notification.httpCodeHandler(resp, url, message);
        return resp.data;
    } catch (err) {
        handleError(err, message);
    }
}

export async function createFavorite(boardId: number) {
    return changeFavorite("create", boardId);
}

export async function deleteFavorite(boardId: number) {
    return changeFavorite("delete", boardId);
}

async function changeFavorite(action: "create" | "delete", boardId: number) {
    let url = CURR_API.monitor + "/user-action/favorite/board/" + boardId;
    let message;
    if (action === "create") {
        message = "添加面板收藏";
    } else if (action === "delete") {
        message = "删除面板收藏";
    }
    try {
        let resp;
        if (action === "create") {
            resp = await API.Put(url);
        } else if (action === "delete") {
            resp = await API.Delete(url);
        }
        messageUtil.httpCodeHandler(resp, url, message);
        // notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function deleteBoardById(boardId: number) {
    let url = CURR_API.monitor + "/dashboard/" + boardId;
    let message = "废弃面板";
    try {
        let resp = await API.Delete(url);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function rollbackBoardById(boardId: number) {
    let url = CURR_API.monitor + "/dashboard/" + boardId + "?status=Active";
    let message = "启用面板";
    try {
        let resp = await API.Delete(url);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function deleteFavoriteApp(boardId: number) {
    let url = CURR_API.monitor + "/user-action/favorite/app/" + boardId;
    let message = "删除App收藏";
    try {
        let resp = await API.Delete(url);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function createFavoriteApp(boardId: number) {
    let url = CURR_API.monitor + "/user-action/favorite/app/" + boardId;
    let message = "添加App收藏";
    try {
        let resp = await API.Put(url);
        notification.httpCodeHandler(resp, url, message);
    } catch (err) {
        handleError(err, message);
    }
}

export async function getFavoriteBoards(num?: number) {

    let url = CURR_API.monitor + "/user-action/top";
    if (num) {
        url = url + "/" + num;
    }
    let message = "获取top20";
    try {
        let resp = await API.Get(url);
        let favorite: any = resp.data;
        if (!favorite) {
            return {};
        }
        return favorite;
    } catch (err) {
        handleError(err, message);
    }
}

export function findChartFromBoard(board: any, chartId: number, globalId: string) {
    if (globalId) {
        return board?.charts?.find(chart => chart.globalId === globalId);
    }
    if (chartId) {
        return board?.charts?.find(chart => chart.id === chartId);
    }
    return;
}
