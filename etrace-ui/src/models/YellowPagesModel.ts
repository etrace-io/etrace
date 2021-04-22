export enum SearchCategory {
    All = "ALL",
    List = "LIST",
    Record = "RECORD",
    Keyword = "KEYWORD",
}

/* 首页 */
// 我的最爱 / 历史记录
export interface FavoriteItem {
    logo?: string;
    icon?: string;
    title: string;
    url: string;
    like: boolean;
}

export interface HomeData {
    LIST: ListItem[];
    HOTEST: RecordItem[];
    RECOMMEND: RecordItem[];
    NEWEST: RecordItem[];
}

export interface FavoriteData {
    LIST: ListList;
    RECORD: RecordList;
}

// Record
export interface RecordItem {
    id?: number;
    listId?: number;
    // 点击数
    clickIndex?: number;
    // 收藏数
    favoriteIndex?: number;
    // Record 指向
    url?: string;
    // Record 名称
    name?: string;
    // 简介
    description?: string;
    // 类型
    type?: string;
    // 状态
    status?: string;
    // owner 的阿里邮箱
    ownerAliEmail?: string;
    // 钉钉群号
    dingtalkNumber?: string;
    // 负责人钉钉号
    ownerDingtalkNumber?: string;
    // 文件记录 id
    fileRecordId?: number;
    // icon 地址
    icon?: string;
    iconKey?: string;
    // 关键词 ID 列表
    keywordIdList?: number[];
    // 关键词列表
    keywordList?: KeywordItem[];
    star?: boolean;
}
export interface RecordList {
    results: RecordItem[];
    total: number;
}

// List
export interface ListItem {
    id?: number;
    icon?: string;
    // List 名称
    name?: string;
    // 简介
    description?: string;
    // 维护者阿里邮箱
    maintainerAliEmail?: string;
}

// List 的集合
export interface ListList {
    results: ListItem[];
    total: number;
}

// Keyword
export interface KeywordItem {
    id?: number;
    name?: string;
    status?: string;
}

/* 搜索建议相关 */
export interface SuggestResult {
    LIST: SuggestListItem[];
    RECORD: SuggestRecordItem[];
    KEYWORD: SuggestKeywordItem[];
}
export type SuggestItem = SuggestListItem | SuggestRecordItem | SuggestKeywordItem;
export interface SuggestBaseItem {
    id: number;
    name: string;
    type: SearchCategory;
    icon?: string;
}
export interface SuggestListItem extends SuggestBaseItem {}
export interface SuggestRecordItem extends SuggestBaseItem {}
export interface SuggestKeywordItem extends SuggestBaseItem {}

/* 搜索结果相关 */
export interface SearchResultData {
    LIST: any[];
    RECORD: RecordItem[];
    KEYWORD: KeywordItem[];
}

/* 搜索参数相关 */
// 搜索下拉框建议请求选项
export interface SuggestOptions {
    // 搜索集合
    searchList: boolean;
    // 搜索 Record
    searchRecord: boolean;
    // 搜索关键字
    // searchKeyWord: boolean;
}
// 搜索 Record 选项
export interface SearchRecordOptions {
    listId?: number;
    pageNum?: number;
    pageSize?: number;
    name?: string;
    status?: string;
}

// Form
export interface RecordForm {
    id?: number;
    listId?: number;
    // Record 指向
    url?: string;
    // Record 名称
    name?: string;
    // 简介
    description?: string;
    // 状态
    status?: string;
    // owner 的阿里邮箱
    ownerAliEmail?: string;
    // 钉钉群号
    dingtalkNumber?: string;
    // 负责人钉钉号
    ownerDingtalkNumber?: string;
    // 文件记录 id（icon 上传后拿到的 id）
    fileRecordId?: number;
    // 关键词列表
    keywordList?: KeywordItem[];
}
export interface ListForm {
    id?: number;
    // 图标
    icon?: string;
    // 状态
    status?: string;
    // Record 名称
    name?: string;
    // 简介
    description?: string;
    // 维护者阿里邮箱
    maintainerAliEmail?: string;
}