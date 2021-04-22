// constants about alert
export const ALERT_METHODS = [
    {label: "邮件", value: "EMAIL"},
    {label: "短信", value: "SMS"},
    {label: "钉钉", value: "DINGDING"},
    {label: "WEBHOOK", value: "WEBHOOK"},
];

export const CONVERT_FIELD_FORM_ITEM = {
    labelCol: {span: 4},
    wrapperCol: {span: 18}
};

export const ALERT_ALL_METHODS = [
    {label: "邮件", value: "EMAIL"},
    {label: "短信", value: "SMS"},
    {label: "钉钉", value: "DINGDING"},
];

export const CONVERTPLUGINLIST = [
    {label: "阈值", value: "ThresholdPlugin"},
    {label: "趋势", value: "TrendConvertPlugin"},
    {label: "Banshee", value: "BansheeConvertPlugin"},
    {label: "指标缺失", value: "AbsentConvertPlugin"},
    {label: "同环比", value: "ComparisonConvertPlugin"},
    {label: "离群", value: "OutlierConvertPlugin"}
];

export function getConvertPluginName(fieldName: string) {
    let pluginName = "";
    CONVERTPLUGINLIST.forEach(e => {
        if (e.value == fieldName) {
            pluginName = e.label;
        }
    });
    return pluginName;
}

// 时间转换
export function dateToString(date: Date) {
    let res = date.getFullYear() + "-";
    if (date.getMonth() + 1 < 10) {
        res = res + "0";
    }
    res = res + (date.getMonth() + 1) + "-";

    if (date.getDate() < 10) {
        res = res + "0";
    }
    res = res + date.getDate() + " ";

    if (date.getHours() < 10) {
        res = res + "0";
    }
    res = res + date.getHours() + ":";

    if (date.getMinutes() < 10) {
        res = res + "0";
    }
    res = res + date.getMinutes() + ":";

    if (date.getSeconds() < 10) {
        res = res + "0";
    }
    res = res + date.getSeconds() + "";
    return res;
}
