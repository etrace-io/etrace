import moment, {DurationInputArg2, unitOfTime} from "moment";

const R = require("ramda");

export const DATE_FORMAT = "MM-DD";
export const TIME_FORMAT: string = "YYYY-MM-DD HH:mm:ss";
export const SIMPLE_TIME_FORMAT: string = "MM-DD HH:mm:ss";
export const SIMPLE_TIME_FORMAT_WITH_MILLSECOND: string = "MM-DD HH:mm:ss.SSS";
export const TIME_FROM: string = "from";
export const TIME_TO: string = "to";
export const REFRESH: string = "refresh";
export const TIMESHIFT: string = "timeshift";

export enum TimePickerModelEnum {
    LAST_1_DAY = "Last 1 day",
    LAST_3_DAY = "Last 3 days",
    LAST_7_DAY = "Last 7 days",
    TODAY = "Today",
    YESTERDAY = "Yesterday",
    THIS_WEEK = "This week",
    LAST_5_MIN = "Last 5 m",
    LAST_15_MIN = "Last 15 m",
    LAST_30_MIN = "Last 30 m",
    LAST_1_HOUR = "Last 1 h",
    LAST_3_HOUR = "Last 3 h",
    LAST_6_HOUR = "Last 6 h",
    LAST_12_HOUR = "Last 12 h",
}

export class TimePickerModel {

    public static models: Array<TimePickerModel> = [
        new TimePickerModel(TimePickerModelEnum.LAST_1_DAY, "now()-1d", "now()-30s"),
        new TimePickerModel(TimePickerModelEnum.LAST_3_DAY, "now()-3d", "now()-30s"),
        new TimePickerModel(TimePickerModelEnum.LAST_7_DAY, "now()-7d", "now()-30s"),
        new TimePickerModel(
            TimePickerModelEnum.TODAY,
            moment().startOf("day").format(TIME_FORMAT),
            moment().endOf("day").format(TIME_FORMAT)
        ),
        new TimePickerModel(
            TimePickerModelEnum.YESTERDAY,
            moment().subtract(1, "day").startOf("day").format(TIME_FORMAT),
            moment().subtract(1, "day").endOf("day").format(TIME_FORMAT)
        ),
        new TimePickerModel(
            TimePickerModelEnum.THIS_WEEK,
            moment().startOf("week").format(TIME_FORMAT),
            moment().endOf("week").format(TIME_FORMAT)
        ),
        new TimePickerModel(TimePickerModelEnum.LAST_5_MIN, "now()-5m", "now()-30s"),
        new TimePickerModel(TimePickerModelEnum.LAST_15_MIN, "now()-15m", "now()-30s"),
        new TimePickerModel(TimePickerModelEnum.LAST_30_MIN, "now()-30m", "now()-30s"),
        new TimePickerModel(TimePickerModelEnum.LAST_1_HOUR, "now()-1h", "now()-30s"),
        new TimePickerModel(TimePickerModelEnum.LAST_3_HOUR, "now()-3h", "now()-30s"),
        new TimePickerModel(TimePickerModelEnum.LAST_6_HOUR, "now()-6h", "now()-30s"),
        new TimePickerModel(TimePickerModelEnum.LAST_12_HOUR, "now()-12h", "now()-30s"),
    ];

    public static default: TimePickerModel = new TimePickerModel(TimePickerModelEnum.LAST_1_HOUR, "now()-1h", "now()-30s", "");

    text: string;
    modelEnum: TimePickerModelEnum;
    fromString: string;
    toString: string;
    from?: number;
    to?: number;
    fromTime?: string;
    toTime?: string;

    constructor(modelEnum: TimePickerModelEnum, fromString: string, toString: string, text?: string) {
        this.modelEnum = modelEnum;
        this.text = text ? simplifyTimeText(text) : modelEnum.toString();
        this.fromString = fromString;
        this.toString = toString;
        this.fromTime = findTime(fromString, TIME_FROM);
        this.toTime = findTime(toString, TIME_TO);
        this.from = moment(this.fromTime).valueOf();
        this.to = moment(this.toTime).valueOf();
    }
}

export function findTimePickerModel(tp: TimePickerModelEnum): TimePickerModel {
    for (let model of TimePickerModel.models) {
        if (model.modelEnum == tp) {
            return model;
        }
    }
    return null;
}

export function findTimePickerModelByTime(from: string, to: string): TimePickerModel {
    let found: TimePickerModel = null;
    for (let model of TimePickerModel.models) {
        if (model.fromString.indexOf(from) >= 0 && model.toString.indexOf(to) >= 0) {
            found = model;
            break;
        }
    }
    // time-range selected
    if (found == null) {
        return new TimePickerModel(null, from, to, from + " ~ " + to);
    }
    // yesterday selected
    if (found.modelEnum == TimePickerModelEnum.YESTERDAY) {
        let startDate = moment(getTime(from)).subtract(1, "day").format(DATE_FORMAT);
        return new TimePickerModel(found.modelEnum, from, to, found.modelEnum.toString() + "(" + startDate + ")");
    }
    // thisWeek selected
    if (found.modelEnum == TimePickerModelEnum.THIS_WEEK) {
        let startDate =  moment(getTime(from)).startOf("week").subtract(1, "day").format(DATE_FORMAT);
        let endDate =  moment(getTime(to)).endOf("week").subtract(1, "day").format(DATE_FORMAT);
        return new TimePickerModel(found.modelEnum, from, to, found.modelEnum.toString() + "(" + startDate + " ~ " + endDate + ")");
    }
    return found;
}

export function findTime(time: string, kind: string): string {
    let times = time.split("/");
    if (times.length == 2) {
        let type: string = times[1];
        let date = getTime(times[0]);
        switch (kind) {
            case TIME_FROM:
                return moment(date).startOf(type as unitOfTime.StartOf).format(TIME_FORMAT);
            case TIME_TO:
                return moment(date).endOf(type as unitOfTime.StartOf).format(TIME_FORMAT);
            default:
                return moment(date).format(TIME_FORMAT);
        }
    } else {
        if (time.indexOf("now()") >= 0) {
            return getTime(times[0]);
        } else {
            return time;
        }
    }
}

export function getTime(time: string): string {
    let times = time.split("-");
    if (times.length == 2) {
        let timePost = times[1];
        let length = timePost.length;
        let type: string = timePost.slice(length - 1);
        let interval: number = Number.parseInt(timePost.slice(0, length - 1));
        return moment().subtract(interval, type as DurationInputArg2).format(TIME_FORMAT);
    } else {
        return moment().format(TIME_FORMAT);
    }
}

export interface RefreshIntervalModel {
    value: string;
    label: string;
}

export const REFRESH_INTERVAL: Array<RefreshIntervalModel> = [
    {value: "off", label: "off"},
    {value: "10s", label: "10s"},
    {value: "30s", label: "30s"},
    {value: "1m", label: "1m"}
];

export const QUICK_RANGE: any = {
    DAYS: [
        findTimePickerModel(TimePickerModelEnum.LAST_1_DAY),
        findTimePickerModel(TimePickerModelEnum.LAST_3_DAY),
        findTimePickerModel(TimePickerModelEnum.LAST_7_DAY)
    ],
    PERIODS: [
        findTimePickerModel(TimePickerModelEnum.TODAY),
        findTimePickerModel(TimePickerModelEnum.YESTERDAY),
        findTimePickerModel(TimePickerModelEnum.THIS_WEEK)
    ],
    TIMES: [
        findTimePickerModel(TimePickerModelEnum.LAST_5_MIN),
        findTimePickerModel(TimePickerModelEnum.LAST_15_MIN),
        findTimePickerModel(TimePickerModelEnum.LAST_30_MIN),
        findTimePickerModel(TimePickerModelEnum.LAST_1_HOUR),
        findTimePickerModel(TimePickerModelEnum.LAST_3_HOUR),
        findTimePickerModel(TimePickerModelEnum.LAST_6_HOUR),
        findTimePickerModel(TimePickerModelEnum.LAST_12_HOUR)
    ]
};

export function findTimeShift(value: string) {
    const result = R.filter(R.propEq("value", value), TIMESHIFT_RANGE);
    if (result.length) {
        return result[0];
    } else {
        return TIMESHIFT_RANGE[0];
    }
}

export function simplifyTimeText(text: string): string {
    const fromDate = new Date(text.split(" ~ ")[0]);
    const toDate = new Date(text.split(" ~ ")[1]);

    // 合法性校验
    if (Number.isNaN(fromDate.getTime()) || Number.isNaN(toDate.getTime())) {
        return text;
    }

    try {
        if (fromDate.toLocaleDateString() === toDate.toLocaleDateString()) {
            return `${text.split(" ~ ")[0]} ~ ${text.split(" ~ ")[1].split(" ")[1]}`;
        } else {
            return text;
        }
    } catch (e) {
        return text;
    }
}

export const TIMESHIFT_RANGE = [
    {
        title: "环比一天",
        valueLabel: "一天",
        key: "timeshift",
        value: "-1d"
    },
    {
        title: "环比三天",
        valueLabel: "三天",
        key: "timeshift",
        value: "-3d"
    },
    {
        title: "环比七天",
        valueLabel: "七天",
        key: "timeshift",
        value: "-7d"
    }
];
