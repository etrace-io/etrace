import React from "react";
import {autobind} from "core-decorators";
import {Mentions} from "antd";
import StoreManager from "../../store/StoreManager";
import {get} from "lodash";
import {reaction} from "mobx";
import {isEmpty} from "../../utils/Util";
import {ChartStatusEnum} from "../../models/ChartModel";
import {ConvertFunctionModel} from "../../utils/ConvertFunction";

const Option = Mentions.Option;

interface SeriesNameMentionProps {
    chartUniqueId: string;
    value?: any;
    paramIndex: number;
    onBlur?: any;
    fun: ConvertFunctionModel;
    prefix?: string | string[];
    onChange?: any;
    disabled?: boolean;
}

interface SeriesNameMentionState {
    value: string;
    options: Array<any>;
}

export default class SeriesNameMention extends React.Component<SeriesNameMentionProps, SeriesNameMentionState> {
    private readonly disposer;
    private mention;
    private isSelect: boolean = false;

    constructor(props: SeriesNameMentionProps) {
        super(props);
        this.state = {value: props.value, options: this.buildSuggestions()};
        this.disposer = reaction(
            () => StoreManager.chartStore.chartStatusMap.get(this.props.chartUniqueId),
            chartStatus => {
                if (isEmpty(chartStatus) || chartStatus.status == ChartStatusEnum.Loading) {
                    return;
                }

                this.setState({options: this.buildSuggestions()});
            });
    }

    componentWillUnmount(): void {
        if (this.disposer) {
            this.disposer();
        }
    }

    componentWillReceiveProps(nextProps: Readonly<SeriesNameMentionProps>, nextContext: any): void {
        this.setState({value: nextProps.value});
    }

    onBlur = () => {
        if (!this.isSelect) {
            const {onBlur, paramIndex, fun} = this.props;
            const value = this.state.value;
            fun.params[paramIndex].display = false;
            fun.defaultParams[paramIndex] = value;
            fun.params[paramIndex].width = value.length * 8 + 10;
            if (onBlur) {
                onBlur(fun);
            }
        }
        this.isSelect = false;
    }

    onChange = (contentState: any) => {
        // todo: https://2x.ant.design/components/mention/
        // let mention: string = Mention.toString(contentState);
        let mention: string = "todo:";
        this.setState({value: mention});
        const {onChange} = this.props;
        if (onChange) {
            onChange(mention);
        }
    };

    onSelect = () => {
        this.isSelect = true;
    };

    @autobind
    buildSuggestions(): Array<any> {
        const series = get(StoreManager.chartStore.initializeSeriesCache.get(this.props.chartUniqueId), "datasets", []);
        const options = [];
        if (series) {
            series.forEach(item => {
                if (options.indexOf(item.label) < 0) {
                    let label = item.label;
                    options.push(
                        <Option
                            value={"{" + label + "}"}
                            // data={label}
                            key={label}
                        >
                            <span>{label}</span>
                        </Option>
                    );
                }
            });
        }
        return options;

    }

    render() {
        const value = get(this.state, "value", null);
        let length = value.length * 8 + 10;
        const {disabled} = this.props;
        this.mention = (
            <Mentions
                ref={ele => this.mention = ele}
                style={{width: length, minWidth: 150}}
                defaultValue={value}
                onSelect={this.onSelect}
                onChange={this.onChange}
                onBlur={this.onBlur}
                placeholder="input $ to mention"
                prefix={this.props.prefix ? this.props.prefix : "$"}
                disabled={disabled}
                // todo: 这里变化很大 https://ant.design/components/mentions/#components-mentions-demo-async
                onSearch={this.buildSuggestions}
            />
        );
        return (
            this.mention
        );
    }
}
