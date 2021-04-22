import React from "react";
import {observer} from "mobx-react";
import {Chart} from "$models/ChartModel";
import {ChartStore} from "$store/ChartStore";
import StoreManager from "$store/StoreManager";
import {UnitModelEnum} from "$models/UnitModel";
import {showField} from "$services/LinDBService";
import {Badge, Button, Dropdown, Menu} from "antd";
import {LineChartOutlined} from "@ant-design/icons/lib";

const R = require("ramda");

const MenuItem = Menu.Item;
const SubMenu = Menu.SubMenu;
const MenuItemGroup = Menu.ItemGroup;

interface ChartFieldsProps {
    uniqueId: string;
    style?: any;
}

interface ChartFieldsState {
    loaded: boolean;
    chartIdFieldMap: Map<number, MetricNameAndFields>;
    originFieldMap: Map<number, Array<string>>;
    fieldChanged: boolean;
}

class MetricNameAndFields {
    metricName: string;
    targetIndex: number;
    fields: Array<string>;

    constructor(metricName: string, targetIndex: number) {
        this.metricName = metricName;
        this.targetIndex = targetIndex;
    }
}

@observer
export default class ChartFields extends React.Component<ChartFieldsProps, ChartFieldsState> {
    chartStore: ChartStore;
    private loadedMetricNames: Set<string> = new Set();

    constructor(props: ChartFieldsProps) {
        super(props);
        this.chartStore = StoreManager.chartStore;

        this.state = {
            loaded: false,
            chartIdFieldMap: new Map(),
            originFieldMap: new Map(),
            // showFields: false,
            fieldChanged: false,
        };
    }

    render() {
        const {uniqueId} = this.props;
        const chart: Chart = this.chartStore.getChart(uniqueId);
        return (
            <Dropdown overlay={this.buildFieldsMenus()} key={3} trigger={["hover"]}>
                <Button
                    // style={{color: "#1890ff", borderColor: "#1890ff"}}
                    size="small"
                    ghost={!this.state.fieldChanged}
                    shape="circle"
                    key={3}
                    icon={<LineChartOutlined />}
                    type="primary"
                    // type={this.state.fieldChanged ? "primary" : "default"}
                    onMouseOver={() => this.loadAndShowMenu(chart)}
                    onClick={() => this.loadAndShowMenu(chart)}
                />
            </Dropdown>
        );
    }

    private buildFieldsMenus() {
        const displayFields = [];
        this.state.chartIdFieldMap.forEach((v: MetricNameAndFields, k) => {
            const originFields = this.state.originFieldMap.get(k);
            let chart: Chart = this.chartStore.getChart(this.props.uniqueId);
            if (chart && chart.targets && chart.targets[v.targetIndex]) {
                const currentFields = chart.targets[v.targetIndex].fields;
                const otherFields = v.fields.filter((oneField) => !R.equals(originFields, [oneField]));
                const subMenu = [];
                // 原始字段
                subMenu.push(
                    <MenuItemGroup title="原始配置的字段" key="origin">
                        <MenuItem
                            key={k}
                            onClick={() => this.updateField(v.targetIndex, originFields)}
                        >
                            {R.equals(currentFields, originFields)
                                ? <b><Badge status="processing" text={originFields}/></b>
                                : <i>{originFields}</i>
                            }
                        </MenuItem>
                    </MenuItemGroup>
                );
                // 其他可用的字段
                if (otherFields.length > 0) {
                    subMenu.push(<Menu.Divider key="divider"/>);
                    subMenu.push(
                        // @ts-ignore
                        <MenuItemGroup title="其他可用的字段" key="others" style={{maxHeight: 300, overflowY: "auto"}}>
                            {otherFields.map((oneField, index) => (
                                <MenuItem
                                    key={index + k + 1}
                                    onClick={() => this.updateField(v.targetIndex, [oneField])}
                                >
                                    {R.equals(currentFields, [oneField])
                                        ? <b><Badge status="processing" text={oneField}/></b>
                                        : <i>{oneField}</i>
                                    }
                                </MenuItem>
                            ))}
                        </MenuItemGroup>
                    );
                }

                // 去重
                let repeatFlag = false;
                displayFields.forEach(({fields}) => {
                    if (fields.metricName === v.metricName) {
                        repeatFlag = true;
                    }
                });
                if (!repeatFlag) {
                    displayFields.push({
                        fields: v,
                        menu: subMenu,
                    });
                }
            }
        });

        const menus = [];
        displayFields.forEach(({fields, menu}, index) => {
            // 处理只有一条的 case
            menus.push(
                displayFields.length > 1
                    ? (<SubMenu title={<span>{fields.metricName}</span>} key={index}>{menu}</SubMenu>)
                    : menu
            );
        });

        return (<Menu mode="vertical" key={123}>{menus}</Menu>);
    }

    private updateField(targetIndex: number, field: Array<string>) {
        let chart: Chart = this.chartStore.getChart(this.props.uniqueId);
        if (chart) {
            // update with new fields
            chart.targets[targetIndex].fields = field;
            // update unit
            const newUnit = this.findUnitByField(field);
            if (newUnit && chart.config && chart.config && chart.config.config) {
                chart.config.config.unit = newUnit;
            }

            this.chartStore.reRegister(this.props.uniqueId, chart);

            this.setState({fieldChanged: !R.equals(this.state.originFieldMap.get(targetIndex), field)});
        }
    }

    private findUnitByField(field: Array<string>): UnitModelEnum {
        let newUnit: UnitModelEnum = null;
        if (field && field[0]) {
            switch (field[0]) {
                case "count":
                case "timerCount":
                case "histogramCount":
                case "payloadCount":
                    newUnit = UnitModelEnum.Short;
                    break;

                case "timerSum":
                case "timerMax":
                case "timerMin":
                case "timerSum/timerCount":
                case "histogramSum":
                case "histogramMax":
                case "histogramMin":
                case "upper(80)":
                case "upper(90)":
                case "upper(95)":
                case "upper(99)":
                    newUnit = UnitModelEnum.Milliseconds;
                    break;

                case "payloadMax":
                case "payloadMin":
                case "payloadSum":
                case "payloadSum/payloadCount":
                    newUnit = UnitModelEnum.Bytes;
                    break;
                default:
            }

        }
        return newUnit;
    }

    private loadAndShowMenu(chart: Chart) {
        const unloadedTargets = chart.targets ? chart.targets.filter(target => {
            if (target.display == false) {
                return false;
            }
            let prefix = target.prefix;
            if (target.prefixVariate) {
                prefix = StoreManager.urlParamStore.getValue(target.prefixVariate);
            }
            let metricName = prefix ? prefix + "." + target.measurement : target.measurement;
            return !this.loadedMetricNames.has(metricName);
        }) : [];

        if (unloadedTargets.length > 0) {
            const tempMetricNameMap: Map<number, MetricNameAndFields> = new Map();
            Promise.all(
                unloadedTargets.map((target, index) => {
                        let prefix = target.prefix;
                        if (target.prefixVariate) {
                            prefix = StoreManager.urlParamStore.getValue(target.prefixVariate);
                        }
                        let metricName = prefix ? prefix + "." + target.measurement : target.measurement;
                        tempMetricNameMap.set(index, new MetricNameAndFields(metricName, index));
                        this.state.originFieldMap.set(index, target.fields);
                        // add to loadedMetricNames  避免重复查询
                        this.loadedMetricNames.add(metricName);
                        return showField(target.entity, metricName);
                    }
                )).then(res => {
                if (res) {
                    res.forEach((r, index) => {
                        const metricNameAndFields: MetricNameAndFields = tempMetricNameMap.get(index);
                        if (!metricNameAndFields) {
                            console.warn("this should not happen: can't find id " + index + " from tempMetricNameMap");
                        } else {
                            metricNameAndFields.fields = r;
                            this.state.chartIdFieldMap.set(index, metricNameAndFields);
                        }
                    });
                }
                this.setState({
                    chartIdFieldMap: this.state.chartIdFieldMap,
                    originFieldMap: this.state.originFieldMap,
                    loaded: true,
                });
            }).catch(err => {
                console.warn("err", err);
            });
        }
    }
}
