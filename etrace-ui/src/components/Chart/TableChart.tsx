import {reaction} from "mobx";
import React from "react";
import {get, isEqual} from "lodash";
import {autobind} from "core-decorators";
import {Table, Tag, Tooltip} from "antd";
import {Resizable} from "react-resizable";
import StoreManager from "../../store/StoreManager";
import {ChartStatusEnum} from "../../models/ChartModel";
import {DataFormatter} from "../../utils/DataFormatter";
import {getGroupName} from "../../utils/ChartDataConvert";
import {CaretDownOutlined, CaretUpOutlined, MinusOutlined} from "@ant-design/icons/lib";
import ChartEditConfig, {getConfigValue} from "../../containers/Board/Explorer/ChartEditConfig";

import "react-resizable/css/styles.css";
import "./TableChart.css";

const ResizeableTitle = props => {
    const {onResize, width, ...restProps} = props;

    if (!width) {
        return <th {...restProps} />;
    }

    return (
        <Resizable
            width={width}
            height={0}
            onResize={onResize}
            draggableOpts={{enableUserSelectHack: false}}
        >
            <th {...restProps} />
        </Resizable>
    );
};

interface TableChartProps {
    uniqueId: string;
    chart?: any;
    height?: number; // 图表高度
}

interface TableChartState {
    currData: any;
    loading: boolean;
    widths: Object;
}

export default class TableChart extends React.Component<TableChartProps, TableChartState> {
    components = {
        header: {
            cell: ResizeableTitle,
        },
    };

    private disposer: any;

    private static concatedTags(tags: Object): string {
        let id = "";
        Object.keys(tags).sort().forEach(key => {
            id = id + key + tags[key];
        });
        return id;
    }

    private static humanReadableTimeShift(n: number): string {
        let aboveZero: boolean = n >= 0;
        let ret = "";
        if (n < 0) {
            n = -1 * n;
        }
        while (n > 0) {
            if (n >= 86400000) {
                let quotient = Math.floor(n / 86400000);
                n = n % 86400000;
                ret = ret + quotient.toString() + "d";
            } else if (n >= 3600000) {
                let quotient = Math.floor(n / 3600000);
                n = n % 3600000;
                ret = ret + quotient.toString() + "h";
            } else if (n >= 60000) {
                let quotient = Math.floor(n / 60000);
                n = n % 60000;
                ret = ret + quotient.toString() + "m";
            } else {
                let quotient = Math.floor(n / 1000);
                ret = ret + quotient.toString() + "s";
                n = 0;
            }
        }
        if (!aboveZero) {
            ret = "-" + ret;
        }
        return ret;
    }

    toPercent(newValue: number, oldValue: any, timeShift: number, valueUnit: any): any {
        if (oldValue == undefined) {
            const unknownText = TableChart.humanReadableTimeShift(timeShift) + ":unknown";
            return <Tooltip placement="right" title={unknownText}><Tag color="gray">Null</Tag></Tooltip>;
        }
        const formattedOldValue = DataFormatter.formatterByUnit(valueUnit, oldValue, 2);
        const text: string = TableChart.humanReadableTimeShift(timeShift) + ":" + formattedOldValue.toString();
        const nanTooltip = <Tooltip placement="right" title={text}><Tag color="geekblue">NaN</Tag></Tooltip>;
        if (isNaN(newValue) || isNaN(oldValue)) {
            return nanTooltip;
        }
        let ratio = (newValue - oldValue) / oldValue;
        if (isNaN(ratio)) {
            return nanTooltip;
        }
        let tag: any;
        const p = Math.round(ratio * 10000) / 100.00;
        const strP = Math.abs(p).toFixed(2);
        const icon = (p > 0) ? <CaretUpOutlined /> : (p == 0 ? <MinusOutlined /> : <CaretDownOutlined />);
        if (p < 0) {
            tag = <Tag color="red">{icon}{strP}%</Tag>;
        } else {
            tag = <Tag color="green">{icon}{strP}%</Tag>;
        }
        return <Tooltip placement="bottom" title={text}>{tag}</Tooltip>;
    }

    buildTagColumn(header: string): any {
        let thisWidth = this.state.widths[header];
        return {
            "title": header,
            "dataIndex": header,
            "key": header,
            width: thisWidth ? thisWidth : 100,
            sorter: (a, b) => a[header].localeCompare(b[header]),
            render: (text, record) => text,
            sortDirections: ["descend", "ascend"],
        };
    }

    buildFieldColumn(header: string, valueUnit: any, timeShifts: Array<number>, showTimeShift: boolean): any {
        let item = this.buildTagColumn(header);
        item.sorter = (a, b) => a[header][0] - b[header][0];
        item.render = (text, record) => {
            const data = record[header];
            if (data == undefined) {
                return null;
            }
            const newValue = data["0"];
            let ratioTooltips = [];
            if (showTimeShift) {
                timeShifts.forEach(timeShift => {
                    const oldValue = data[timeShift.toString()];
                    if (timeShift != 0) {
                        ratioTooltips.push(this.toPercent(newValue, oldValue, timeShift, valueUnit));
                    }
                });
            }
            const newFormattedValue = DataFormatter.formatterByUnit(valueUnit, newValue, 2);
            return <div>{newFormattedValue.toString()} <span className={"tooltips"}> {ratioTooltips}</span></div>;
        };
        return item;
    }

    constructor(props: TableChartProps) {
        super(props);
        const {uniqueId, chart} = props;
        // 监听 Chart 加载状态
        this.addListenerOfChartStatus(uniqueId);
        this.state = {
            currData: null,
            loading: false,
            widths: {},
        };
        StoreManager.chartStore.register(uniqueId, chart);
    }

    shouldComponentUpdate(nextProps: Readonly<TableChartProps>, nextState: Readonly<TableChartState>, nextContext: any): boolean {
        return !(isEqual(this.props.chart, nextProps.chart)
            && isEqual(this.state.currData, nextState.currData)
            && isEqual(this.state.widths, nextState.widths));
    }

    componentWillUnmount(): void {
        if (this.disposer) {
            this.disposer();
        }
    }

    handleResize(column: any) {
        return (e, {size}) => {
            let nextWidths = Object.assign({}, this.state.widths);
            nextWidths[column.key] = size.width;
            this.setState({widths: nextWidths});
        };
    }

    render() {
        const chartConfig = get(this.props.chart, "config", {});
        const valueUnit = getConfigValue<string>(ChartEditConfig.axis.leftYAxis.unit, chartConfig);
        const showTimeShift = getConfigValue<boolean>(ChartEditConfig.display.table.showTimeshift, chartConfig);
        const {height} = this.props;

        const currData = this.state.currData;
        if (!currData || Object.keys(currData).length == 0) {
            return null;
        }
        // key: concated-tags-id, value: {fieldname, tags}
        let data: Object = {};
        let columns = [];
        let headers = new Set();
        let fields = new Set();
        let allTimeShifts = new Set(); // int number
        const datasets: any[] = currData.datasets;
        datasets.forEach(dataset => {
            if (!dataset.seriesDisplay) {
                return;
            }
            let thisTags = dataset.metric.tags;
            // collect tag
            Object.keys(thisTags).forEach(key => {
                headers.add(key);
            });
            // collect fieldName
            const fieldName = getGroupName(dataset.metric.field, dataset.metric.functions);
            fields.add(fieldName);
            const concatedTagsID = TableChart.concatedTags(thisTags);
            let item = data[concatedTagsID];
            if (!item) {
                item = {...thisTags}; // copy it
            }
            item.key = concatedTagsID;
            if (!item[fieldName]) {
                item[fieldName] = {};
            }
            // ignore timeShift data
            const timeShift = dataset.metric.timeShift;
            allTimeShifts.add(timeShift);
            if (dataset.data.length == 0) {
                return;
            }
            item[fieldName][timeShift.toString()] = dataset.data[dataset.data.length - 1];
            data[concatedTagsID] = item;
        });

        // sorted timeshifts
        const timeShiftList = Array.from(allTimeShifts).sort((n1: any, n2: any) => parseInt(n1) - parseInt(n2)) as number[];
        // build columns with tags
        headers.forEach((tagName: string) => {
            columns.push(this.buildTagColumn(tagName));
        });
        // build columns with fields
        Array.from(fields).sort().forEach((fieldName: string) => {
            columns.push(this.buildFieldColumn(fieldName, valueUnit, timeShiftList, showTimeShift));
        });

        const finalColumns = columns.map((col, index) => ({
            ...col,
            onHeaderCell: column => {
                return {
                    width: column.width,
                    onResize: this.handleResize(column),
                };
            },
        }));
        let finalData = [];
        Object.keys(data).forEach(d => {
            finalData.push(data[d]);
        });
        return (
            <Table style={{height, overflowY: "auto"}} bordered={true} size={"small"} components={this.components} columns={finalColumns} dataSource={finalData}/>
        );
    }

    /**
     * 添加对 Chart Loading 的监听
     */
    @autobind
    private addListenerOfChartStatus(uniqueId: string) {
        this.disposer = reaction(
            () => StoreManager.chartStore.chartStatusMap.get(uniqueId),
            chartStatus => {
                if (!chartStatus || chartStatus.status === ChartStatusEnum.Loading) {
                    this.setState({
                                      loading: true
                                  });
                    return;
                }

                this.setState({
                                  currData: StoreManager.chartStore.seriesCache.get(uniqueId),
                                  loading: false,
                              });
                this.render();
            }
        );
    }
}
