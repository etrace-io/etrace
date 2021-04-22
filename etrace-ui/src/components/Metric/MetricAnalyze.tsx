import {MonitorOutlined, ReloadOutlined} from "@ant-design/icons/lib";
import {get, set} from "lodash";
import React from "react";
import {autobind} from "core-decorators";
import {Button, Popover, Table} from "antd";
import StoreManager from "../../store/StoreManager";
import {URLParamStore} from "../../store/URLParamStore";
import {EditChartStore} from "../../store/EditChartStore";
import * as ChartService from "../../services/ChartService";
import {default as ChartEditConfig, getConfigValue} from "../../containers/Board/Explorer/ChartEditConfig";
import {TimePickerModel, TimePickerModelEnum} from "../../models/TimePickerModel";

interface MetricAnalyzeProps {
    config: any;
}

interface MetricAnalyzeStatus {
    analyzeResult: any;
    isLoading: boolean;
    tagKeys: string[];
    fieldKeys: string[];
}

export default class MetricAnalyze extends React.Component<MetricAnalyzeProps, MetricAnalyzeStatus> {
    editChartStore: EditChartStore;
    urlParamStore: URLParamStore;
    analyzeConfig: any;

    state = {
        analyzeResult: null,
        isLoading: false,
        tagKeys: null,
        fieldKeys: null,
    };

    constructor(props: MetricAnalyzeProps) {
        super(props);
        this.editChartStore = StoreManager.editChartStore;
        this.urlParamStore = StoreManager.urlParamStore;
    }

    @autobind
    async startAnalyze() {
        this.setState({isLoading: true});
        const {config} = this.props;
        const chart = this.editChartStore.getChart();
        const analyzeConfig = chart.id !== -1
            ? getConfigValue(ChartEditConfig.analyze, get(chart, "config"))
            : config;

        this.analyzeConfig = analyzeConfig;
        if (!get(analyzeConfig, "first.target")) {
            set(analyzeConfig, "first.target", get(chart, "targets", []).filter(target => target.isAnalyze)[0]);
        }
        // 获取时间
        const selectedTime = this.urlParamStore.getSelectedTime(false) || new TimePickerModel(TimePickerModelEnum.LAST_1_HOUR, "now()-1h", "now()-30s", "");
        set(analyzeConfig, "first.target.from", selectedTime.fromTime);
        set(analyzeConfig, "first.target.to", selectedTime.toTime);
        if (analyzeConfig.others) {
            analyzeConfig.others.forEach(item => {
                item.target.from = selectedTime.fromTime;
                item.target.to = selectedTime.toTime;
            });
        }
        // 调整 Prefix
        const prefixVariate = get(analyzeConfig, `first.target.prefixVariate`);
        if (prefixVariate) {
            const value = StoreManager.urlParamStore.getValue(prefixVariate);
            value && set(analyzeConfig, "first.target.prefix", value);
        }

        ChartService.analyze(analyzeConfig)
            .then(analyzeResult => {
                this.setState({
                    analyzeResult,
                    isLoading: false,
                });
            })
            .catch(err => {
                this.setState({
                    isLoading: false,
                });
            });
    }

    renderAnalyzePopoverTitle() {
        const {analyzeResult, isLoading} = this.state;
        const groups = get(analyzeResult, "groups");

        if (!groups) {
            return null;
        }

        return (
            <div style={{padding: "5px 0"}}>
                <span>指标分析</span>
                <Button
                    style={{float: "right", transform: "translate(8px, -2px)"}}
                    size="small"
                    type="primary"
                    loading={isLoading}
                    icon={<ReloadOutlined />}
                    shape="circle"
                    onClick={this.startAnalyze}
                />
            </div>
        );
    }

    renderAnalyzePanel() {
        const {analyzeResult, isLoading} = this.state;

        if (!analyzeResult) {
            return <Button type="primary" loading={isLoading} onClick={this.startAnalyze}>点击进行指标分析</Button>;
        }

        const groups = get(analyzeResult, "groups");
        const resultKeys = Object.keys(groups[0]);
        const groupBy = get(this.analyzeConfig, "first.target.groupBy", []);
        const extraGroupBy = get(this.analyzeConfig, "first.analyzeConfig.extraGroupBy", []);
        const fields = [...groupBy, extraGroupBy];

        // init
        groups.forEach(group => {
            group.rowSpan = {};
            fields.forEach(field => group.rowSpan[group[field]] = 0);
        });

        fields.forEach(field => {
            let currLineFlag = 0;
            const fieldValues = groups.map(group => group[field]); // 当前列的所有值

            while (currLineFlag !== -1 && currLineFlag < fieldValues.length) {
                const value = fieldValues[currLineFlag];
                // eslint-disable-next-line no-loop-func
                const lastIndex = groups.findIndex((item, index) => item[field] !== value && index > currLineFlag); // 当前值在连续区间上的最后一个位置
                const length = lastIndex === -1 ? fieldValues.length - currLineFlag : lastIndex - currLineFlag; // currLineFlag !== -1 表示最后一个与 value 相同
                if (length > -1) {
                    groups[currLineFlag].rowSpan[value] = length;
                }
                currLineFlag = lastIndex;
            }
        });

        const columns = fields.filter(field => resultKeys.indexOf(field) > -1).map(key => {
            const fieldValues = groups.map(group => group[key]); // 当前列的所有值
            const maxLength = Math.max.apply(null, fieldValues.map(value => value.length));

            return {
                title: key,
                dataIndex: key,
                // width: "max-content",
                width: maxLength > 20 ? 200 : 100,
                render: (value, row, index) => {
                    return {
                        children: value,
                        props: {rowSpan: row.rowSpan[value]},
                    };
                }
            };
        });

        return (
            <div className="e-monitor-analyze-popover__table" style={{width: 600}}>
                <Table columns={columns} dataSource={analyzeResult.groups} scroll={{y: 800}} pagination={false} size="small"/>
            </div>
        );
    }

    render() {
        const analyzePanel = this.renderAnalyzePanel();
        const popoverTitle = this.renderAnalyzePopoverTitle();

        return (
            <Popover arrowPointAtCenter={true} overlayClassName="dark-mode" placement="bottomRight" content={analyzePanel} title={popoverTitle} trigger="click">
                <Button
                    type="primary"
                    shape="circle"
                    size="small"
                    icon={<MonitorOutlined />}
                />
            </Popover>
        );
    }
}
