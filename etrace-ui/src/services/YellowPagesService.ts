import {AxiosResponse} from "axios";
import {Delete, Get, Post, Put} from "$utils/api";
import {
    FavoriteData,
    HomeData,
    KeywordItem,
    ListForm,
    ListItem,
    RecordForm,
    RecordItem,
    RecordList,
    SearchRecordOptions,
    SearchResultData,
    SuggestOptions,
    SuggestResult
} from "$models/YellowPagesModel";
import {SystemKit} from "$utils/Util";
import {CURR_API} from "$constants/API";

const baseUrl = CURR_API.monitor + "/yellowpage";

export const PIC_UPLOAD_URL = `${CURR_API.monitor}/file/upload`;

/**
 * 获取首页数据
 */
export function getHomeData() {
    const url = `${baseUrl}/search/home`;
    const params = {};
    return responseProxy<HomeData>(Post(url, params));
}

/**
 * Record 相关
 */
// 创建 Record
export function createRecord(record: RecordForm) {
    const url = `${baseUrl}/search/record`;
    Object.assign(record, {status: "Active"});
    return responseProxy(Post(url, record));
}

// 编辑 Record
export function editRecord(record: RecordForm) {
    const url = `${baseUrl}/search/record`;
    return responseProxy(Put(url, record));
}

// 根据 ID 获取 Record
export function getRecordById(id: number) {
    const url = `${baseUrl}/search/record/${id}`;
    return responseProxy<RecordItem>(Get(url));
}

// 根据名称获取 Record（搜索添加 App）
export function getRecordByName(name: string) {
    return getRecordByParams({name});
}

// 根据选项获取 Record
export function getRecordByParams(options: SearchRecordOptions) {
    const url = `${baseUrl}/search/record/findByParams`;
    return responseProxy<RecordList>(Get(url, options));
}

// 收藏 Record
export function likeRecord(id: number, like: boolean) {
    const url = `${CURR_API.monitor}/user-action/favorite/record/${id}`;
    return responseProxy(like ? Put(url) : Delete(url));
}

/**
 * List 相关
 */
// 创建 List
export function createList(list: ListForm) {
    const url = `${baseUrl}/search/list`;
    Object.assign(list, {status: "Active"});
    return responseProxy(Post(url, list));
}

// 编辑 List
export function editList(list: ListForm) {
    const url = `${baseUrl}/search/list`;
    return responseProxy(Put(url, list));
}

// 删除 List
export function deleteList(id: number) {
    const url = `${baseUrl}/search/list?id=${id}`;
    return responseProxy(Delete(url));
}

// 根据 ID 获取 List
export function getListById(id: number) {
    const url = `${baseUrl}/search/list/${id}`;
    return responseProxy<ListItem>(Get(url));
}

// 往 List 里添加 Record
export function addRecordsToList(listId: number, recordIdList: number[]) {
    const recordIdListParams = recordIdList.map(id => `recordIdList=${id}`).join("&");
    const url = `${baseUrl}/search/list/editListRecord?listId=${listId}${recordIdListParams && `&${recordIdListParams}`}`;
    return responseProxy(Post(url, {listId, recordIdList}));
}

// 获取 List 下所有 Record
export function getListRecord(listId: number) {
    return getRecordByParams({listId});
}

export function getFavorite(current: number = 1, pageSize: number = 10) {
    const url = `${CURR_API.monitor}/user-action/favorite/recordAndList?current=${current}&pageSize=${pageSize}`;
    return responseProxy<FavoriteData>(Get(url));
}

/**
 * 关键字相关
 */
// 获取搜索关键字建议
export function getKeywordSuggest(key: string) {
    const url = `${baseUrl}/search/keyword/findByKeyword?keyword=${key}`;
    return responseProxy<KeywordItem[]>(Get(encodeURI(url)));
}

// 获取可能关联的关键字
export function getAssociateKeywords(ids: number[]) {
    const params = ids.map(id => `keywordList=${id}`).join("&");
    const url = `${baseUrl}/search/keyword/findSuggestKeyword?${params}`;
    return responseProxy<KeywordItem[]>(Get(url));
}

/**
 * 获取搜索框搜索建议
 */
export function getSearchSuggest(search: string, options: SuggestOptions) {
    const url = `${baseUrl}/search/suggest`;
    return responseProxy<SuggestResult>(Post(url, Object.assign({
        keyword: search
    }, options)));
}

/**
 * 获取搜索结果
 */
export function getSearchResult(search: string, options: SuggestOptions) {
    const url = `${baseUrl}/search/data`;
    return responseProxy<SearchResultData>(Post(url, Object.assign({
        keyword: search
    }, options)));
}

async function responseProxy<T>(request: Promise<AxiosResponse>): Promise<T> {
    return request
        .then(result => {
            return result.data;
        })
        .catch(err => {
            const resp = err.response;
            if (resp && resp.status === 401) {
                SystemKit.redirectToLogin(window.location.pathname + window.location.search);
            } else {
                throw err;
            }
        });
}
