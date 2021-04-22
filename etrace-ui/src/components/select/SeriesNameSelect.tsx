import React from "react";
import {autobind} from "core-decorators";
import {Select} from "antd";
import StoreManager from "../../store/StoreManager";
import {get} from "lodash";
import {reaction} from "mobx";
import {isEmpty} from "../../utils/Util";
import {ChartStatusEnum} from "../../models/ChartModel";

const Option = Select.Option;

interface SeriesNameSelectProps {
    chartUniqueId: string;
    value?: any;
    onBlur?: any;
    onChange?: any;
    disabled?: boolean;
}

interface SeriesNameSelectState {
    value: string;
    options: Array<string>;
}

export default class SeriesNameSelect extends React.Component<SeriesNameSelectProps, SeriesNameSelectState> {
    private readonly disposer;

    constructor(props: SeriesNameSelectProps) {
        super(props);
        this.state = {value: props.value, options: this.getOptions()};
        this.disposer = reaction(
            () => StoreManager.chartStore.chartStatusMap.get(this.props.chartUniqueId),
            chartStatus => {
                if (isEmpty(chartStatus) || chartStatus.status == ChartStatusEnum.Loading) {
                    return;
                }

                this.setState({options: this.getOptions()});
            });
    }

    componentWillUnmount(): void {
        if (this.disposer) {
            this.disposer();
        }
    }

    componentWillReceiveProps(nextProps: Readonly<SeriesNameSelectProps>, nextContext: any): void {
        this.setState({value: nextProps.value});
    }

    @autobind
    getOptions() {
        const series = get(StoreManager.chartStore.seriesCache.get(this.props.chartUniqueId), "datasets", []);
        const options = [];
        if (series) {
            series.forEach(item => {
                if (options.indexOf(item.label) < 0) {
                    options.push(item.label);
                }
            });
        }
        return options;
    }

    @autobind
    onBlur() {
        const {onBlur} = this.props;
        if (onBlur) {
            onBlur(get(this.state, "value", null));
        }
    }

    @autobind
    onChange(value: any) {
        this.setState({value: value});
        const {onChange} = this.props;
        if (onChange) {
            onChange(value);
        }
    }

    render() {
        const value = get(this.state, "value", []);
        const options = this.state.options || [];
        const {disabled} = this.props;
        return (
            <Select
                value={value}
                mode="tags"
                dropdownMatchSelectWidth={false}
                onBlur={this.onBlur}
                onChange={this.onChange}
                disabled={disabled}
            >
                {options.map((item, index) => (
                    <Option key={"" + index} value={item}>{item}</Option>
                ))}
            </Select>
        );
    }
}
