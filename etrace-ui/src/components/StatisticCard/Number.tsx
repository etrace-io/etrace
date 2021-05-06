import React from "react";
import {FormatConfig, valueType} from "./Utils";

interface StatisticProps extends FormatConfig {
    value?: valueType;
}

const StatisticNumber: React.SFC<StatisticProps> = props => {
    const {
        value,
        formatter,
        precision = 0,
        groupSeparator = ",",
        decimalSeparator = "."
    } = props;

    let valueNode: React.ReactNode;

    if (typeof formatter === "function") {
        // Customize formatter
        valueNode = formatter(value);
    } else {
        // Internal formatter
        const val: string = String(value);

        // 匹配 value
        // 匹配 123.456 | 123 | -123
        // 不匹配 +123 | 123.
        const cells = val.match(/^(-?)(\d*)(\.(\d+))?$/);

        // 未匹配到结果，处理非法数字
        if (!cells) {
            valueNode = val;
        } else {
            const negative = cells[1];      // 得到第一组内容，也就是 (-?)，判断是否负数，如果是负数则得到 `-`
            let int = cells[2] || "0";      // 得到第二组内容，也就是 (\d*)，得到绝对值的小数点前的部分
            let decimal = cells[4] || "";   // 得到第四组内容，也就是 (\d+)，得到小数点后的部分

            // 匹配
            // \B 非字符开始或结束的位置
            // ?=(\d{3})+ 至少一组 3 位数在后头
            // (?!\d) 后面不是数字
            int = int.replace(/\B(?=(\d{3})+(?!\d))/g, groupSeparator);

            // 给小数位填充对应的 `0` 到指定位数 `precision`，并截取 `precision` 位数字。
            const decimalWithZero = Number("0." + parseInt(decimal));
            decimal = Number.isNaN(decimalWithZero) ? "" : decimalWithZero.toFixed(precision).slice(2);

            if (decimal) {
                decimal = `${decimalSeparator}${decimal}`;
            }

            valueNode = [
                <span key="int" className="e-monitor-statistic-content-value-int">{negative}{int}</span>,
                decimal && (<span key="decimal" className="e-monitor-statistic-content-value-decimal">{decimal}</span>),
            ];
        }
    }

    return <span className="e-monitor-statistic-content-value">{valueNode}</span>;
};

export default StatisticNumber;
