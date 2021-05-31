import {get} from "lodash";
import moment from "moment";
import StoreManager from "$store/StoreManager";

const uuid = require("react-native-uuid");

export default {
    getFormLayout,
    paramsToURLSearch,
    firstUpperCase,
    createUuid,
    getTimeScaleByRange,
    formatChartTimeLabel,
    changeURLTime,
};

function getFormLayout(labelSpan: number) {
    return {
        labelCol: { xs: {span: 24}, sm: {span: labelSpan} },
        wrapperCol: { xs: {span: 24}, sm: {span: 24 - labelSpan} },
    };
}

interface ParamsToURLSearchOptions {
    // 默认值：false；传递 value 为空的字段（default：false）
    passEmpty?: boolean;
    // 默认值：true；search 是否前置 `?` 连接符
    questionMark?: boolean;
}

function paramsToURLSearch(
    params: Object,
    defaultURL?: string,
    options?: ParamsToURLSearchOptions
) {
    const passEmpty = get(options, "passEmpty", false);
    const questionMark = get(options, "questionMark", true);

    return params ? `${defaultURL || ""}${questionMark ? "?" : ""}` + Object
        .keys(params)
        .map(param => {
            const values = params[param];
            if (!values && !passEmpty) { return null; }
            return Array.isArray(values)
                ? values.map(v => `${param}=${v}`).join("&")
                : `${param}=${values}`;
        })
        .filter(i => i)
        .join("&") : defaultURL || "";
}

/**
 * 首字母大写
 */
function firstUpperCase(str: string) {
    if (str === null || str === undefined || str.length === 0) {
        return;
    }
    return str[0].toUpperCase() + str.substr(1).toLowerCase();
}

function createUuid() {
    return uuid.v4();
}

/**
 * 格式化时间戳对应的 Label，用于 Chart 坐标轴显示
 * @param timeScale 所有时间点
 */
function formatChartTimeLabel(timeScale: number[]) {
    const start = moment(timeScale[0]);
    const end = moment(timeScale[timeScale.length - 1]);
    const range = timeScale[timeScale.length - 1] - timeScale[0];

    // 是否为跨天（跨天则显示日期）
    const showDate = !start.isSame(end, "day");

    return timeScale.map(timestamp => {
        const dateTime = moment(timestamp);

        if (showDate) {
            return dateTime.format("MM/DD HH:mm");
        } else if (range > 5 * 60 * 1000) {
            return dateTime.format("HH:mm");
        } else {
            return dateTime.format("HH:mm:ss");
        }
    });
}

/**
 * 获取一段时间范围内所有将为 interval 的时间点
 * @param start 起始时间
 * @param end 终止时间
 * @param interval 时间间隔
 */
function getTimeScaleByRange(start: number, end: number, interval: number) {
    if (interval === undefined || interval < 0) {
        return [];
    }

    let current = start;
    const timeScale = [];

    while (current < end) {
        timeScale.push(current);
        current += interval;
    }

    return timeScale;
}

function changeURLTime(start: number, end: number) {
    const from = moment(start).format("YYYY-MM-DD HH:mm:ss");
    const to = moment(end).format("YYYY-MM-DD HH:mm:ss");
    StoreManager.urlParamStore.changeSelectedTime(from, to);
}
