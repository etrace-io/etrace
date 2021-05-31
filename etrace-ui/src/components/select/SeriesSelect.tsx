import React from "react";
import {autobind} from "core-decorators";
import {Select} from "antd";
import StoreManager from "../../store/StoreManager";
import {ConvertFunctionModel} from "../../utils/ConvertFunction";
import {get} from "lodash";
import {Chart} from "../../models/ChartModel";

const Option = Select.Option;

interface SeriesSelectProps {
    chartUniqueId: string;
    value: any;
    paramIndex: number;
    fun: ConvertFunctionModel;
    onBlur: any;
}

interface SeriesSelectState {
    value: string;
    width?: number;
}

export default class SeriesSelect extends React.Component<SeriesSelectProps, SeriesSelectState> {

    constructor(props: SeriesSelectProps) {
        super(props);
        this.state = {value: props.value};
    }

    componentWillReceiveProps(nextProps: Readonly<SeriesSelectProps>, nextContext: any): void {
        this.setState({value: nextProps.value});
    }

    @autobind
    onBlur() {
        const {onBlur, paramIndex, fun} = this.props;
        const value = get(this.state, "value", "search");
        fun.params[paramIndex].display = false;
        fun.defaultParams[paramIndex] = value;
        fun.params[paramIndex].width = value.length * 8 + 25;
        if (onBlur) {
            onBlur(fun);
        }
    }

    @autobind
    onChange(value: any) {
        this.setState({value: value});
    }

    render() {
        const {chartUniqueId} = this.props;
        const value = get(this.state, "value", "search");
        let length = value.length * 8 + 10;
        const series: any = StoreManager.chartStore.seriesCache.get(chartUniqueId);
        const chart: Chart = StoreManager.chartStore.charts.get(chartUniqueId);
        const options = [];
        if (series) {
            const type = get(chart, "config.type", "line");
            if (type == "pie" || type == "radar") {
                if (series.items) {
                    series.items.forEach(item => {
                        if (options.indexOf(item.rawName) < 0) {
                            options.push(item.rawName);
                        }
                    });
                }
            } else {
                if (series.datasets) {
                    series.datasets.forEach(item => {
                        if (options.indexOf(item.rawName) < 0) {
                            options.push(item.rawName);
                        }
                    });
                }
            }
        }
        return (
            <Select
                style={{width: length, minWidth: 150}}
                value={value}
                size="small"
                mode="multiple"
                dropdownMatchSelectWidth={false}
                onBlur={this.onBlur}
                onChange={this.onChange}
            >
                {options.map((item, index) => (
                    <Option key={"" + index} value={item}>{item}</Option>
                ))}
            </Select>
        );
    }
}
