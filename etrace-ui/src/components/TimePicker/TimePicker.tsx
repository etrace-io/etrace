import classNames from "classnames";
import React, {useEffect, useRef, useState} from "react";
import {
    findTimePickerModelByTime,
    findTimeShift,
    QUICK_RANGE,
    REFRESH,
    REFRESH_INTERVAL,
    TIME_FORMAT,
    TimePickerModel,
    TimePickerModelEnum,
    TIMESHIFT,
    TIMESHIFT_RANGE,
} from "../../models/TimePickerModel";
import {get} from "lodash";
import moment from "moment";
import {reaction} from "mobx";
import {observer} from "mobx-react";
import StoreManager from "../../store/StoreManager";
import {LeftOutlined, RightOutlined, SyncOutlined} from "@ant-design/icons";
import {Button, Col, DatePicker, Divider, Form, Popover, Row, Select, Space, Tooltip} from "antd";

import "./TimePicker.less";

export interface TimePickerProps {
    showCurrTimeShift?: boolean;
    hasTimeShift?: boolean;
    hasTimeZoom?: boolean;
    noTimeRefresh?: boolean; // 是否提供自动刷新功能
}

export const forbidRefresh = [
    TimePickerModelEnum.THIS_WEEK,
    TimePickerModelEnum.YESTERDAY,
    TimePickerModelEnum.TODAY,
    TimePickerModelEnum.LAST_7_DAY,
    TimePickerModelEnum.LAST_3_DAY,
    TimePickerModelEnum.LAST_1_DAY,
    TimePickerModelEnum.LAST_12_HOUR,
];

const TimePicker: React.FC<TimePickerProps> = props => {
    const { urlParamStore } = StoreManager;
    const { noTimeRefresh, hasTimeZoom, showCurrTimeShift } = props;

    const countDown = useRef<number>();
    const isRangeOpened = useRef<boolean>(false); // 时间范围选择框是否打开
    const isSelectingRefresh = useRef<boolean>(false); // 刷新时间下拉框是否打开

    const [popoverVisible, setPopoverVisible] = useState(false);
    const [seconds, setSeconds] = useState(urlParamStore.getSeconds());
    const [selectedRangeDates, setSelectedRangeDates] = useState(urlParamStore.getSelectedTime());
    const [refreshState, setRefreshState] = useState(urlParamStore.getValue(REFRESH));

    const currRefresh = urlParamStore.getTimeAutoReflesh() || "off";
    const selectedTime = urlParamStore.getSelectedTime();

    useEffect(() => {
        const disposer = reaction(() => urlParamStore.changed, () => {
            if (selectedRangeDates.text !== urlParamStore.getSelectedTime().text) {
                setSelectedRangeDates(urlParamStore.getSelectedTime());
            }
            buildCountDown();
        });
        return () => {
            disposer();
        };
    }, []);

    useEffect(() => {
        const disposer = reaction(() => urlParamStore.forceChanged, () => {
            buildCountDown();
        });
        return () => {
            disposer();
        };
    }, []);

    useEffect(() => {
        buildCountDown();

        return () => {
            clearInterval(countDown.current);
        };
    }, []);

    const buildCountDown = () => {
        if (countDown.current) {
            clearInterval(countDown.current);
        }

        const refresh = urlParamStore.getTimeAutoReflesh() || "off";
        const interval = refresh && refresh !== "off" ? getRefreshTime(refresh) / 1000 : null;

        if (refresh && refresh === "off") {
            setRefreshState("off");
            setSeconds(null);
        }

        if (interval) {
            if (!noTimeRefresh) {
                setSeconds(interval);
                countDown.current = +setInterval(
                    () => {
                        setSeconds(s => {
                            s = s || interval;
                            return s - 1 === 0 ? urlParamStore.getSeconds() : s - 1;
                        });
                    },
                    1000
                );
            } else {
                urlParamStore.changeURLParams({ refresh: "off" }, [], false);
            }
        }
    };

    const getRefreshTime = (refresh: string): number => {
        const interval: number = parseInt(refresh, 10);
        const type = refresh[ refresh.length - 1 ].toLowerCase();
        if (type === "m") {
            return interval * 60 * 1000;
        }
        if (type === "s") {
            return interval * 1000;
        }
    };

    const handlePopVisibleChange = (status: boolean) => {
        if (!isRangeOpened.current && !isSelectingRefresh.current) {
            setPopoverVisible(status);
        }
    };

    const handleZoomOutChart = (type: string) => {
        const currSelectedTime: TimePickerModel = urlParamStore.getSelectedTime();
        if (currSelectedTime) {
            const interval = 600000;
            const { from, to } = currSelectedTime;
            let fromTime, toTime;

            switch (type) {
                case "left":
                    fromTime = moment(from - interval).format(TIME_FORMAT);
                    toTime = moment(to - interval).format(TIME_FORMAT);
                    break;
                case "right":
                    fromTime = moment(from + interval).format(TIME_FORMAT);
                    toTime = moment(to + interval).format(TIME_FORMAT);
                    break;
                case "all":
                default:
                    fromTime = moment(from - interval).format(TIME_FORMAT);
                    toTime = moment(to + interval).format(TIME_FORMAT);
                    break;
            }
            urlParamStore.changeURLParams({ from: fromTime, to: toTime });
        }
    };

    const handleQuickRangeClick = (col: TimePickerModel | { value: string }) => {
        if (col instanceof TimePickerModel) {
            const model: TimePickerModel = col as TimePickerModel;
            if (forbidRefresh.indexOf(model.modelEnum) > 0) {
                urlParamStore.changeURLParams({
                    "from": get(model, "fromString", null),
                    "to": get(model, "toString", null),
                    "refresh": "off",
                });
                setSelectedRangeDates(model);
                setRefreshState("off");
            } else {
                urlParamStore.changeURLParams({
                    "from": get(model, "fromString", null),
                    "to": get(model, "toString", null),
                });
                setSelectedRangeDates(model);
            }
        } else {
            urlParamStore.changeURLParams({ "timeshift": col.value });
        }

        setPopoverVisible(false);
    };

    const handleRefreshSelect = (e: any) => {
        isSelectingRefresh.current = false;
        setRefreshState(e);
    };

    const handleApply = () => {
        const refresh = !get(selectedRangeDates, "modelEnum", null) ? "off" : refreshState;

        urlParamStore.changeURLParams({
            "from": get(selectedRangeDates, "fromString", null),
            "to": get(selectedRangeDates, "toString", null),
            "refresh": refresh,
        });

        if (refresh && refresh !== "off") {
            buildCountDown();
        } else {
            clearInterval(countDown.current);
        }

        setPopoverVisible(false);
    };

    const RangePickerChange = (dates: any[], dateString: string[]) => {
        setSelectedRangeDates(findTimePickerModelByTime(dateString[ 0 ], dateString[ 1 ]));
    };

    const timePanel = (
        <TimePickerPanel
            selectedTime={selectedRangeDates}
            refreshState={refreshState}
            onPickerOpen={status => isRangeOpened.current = status}
            onQuickRangeClick={handleQuickRangeClick}
            onRefreshSelect={handleRefreshSelect}
            onApply={handleApply}
            onToggleRefreshDropdown={(status => isSelectingRefresh.current = status)}
            onRangeChange={RangePickerChange}
            {...props}
        />
    );

    return (
        <Space align="center">
            {showCurrTimeShift && (
                <span>环比：{findTimeShift(urlParamStore.getValue(TIMESHIFT)).valueLabel}</span>
            )}
            {hasTimeZoom && (
                <Button.Group>
                    <Tooltip title="时间左移 10m" placement="topLeft">
                        <Button
                            htmlType="button"
                            icon={<LeftOutlined/>}
                            style={{ paddingLeft: "2px", paddingRight: "2px" }}
                            onClick={() => handleZoomOutChart("left")}
                        />
                    </Tooltip>
                    <Tooltip title="时间放大 20m" placement="topRight">
                        <Button
                            htmlType="button"
                            style={{ paddingLeft: "10px", paddingRight: "10px" }}
                            onClick={() => handleZoomOutChart("all")}
                        >
                            Zoom Out
                        </Button>
                    </Tooltip>
                    <Tooltip title="时间右移 10m" placement="topRight">
                        <Button
                            htmlType="button"
                            style={{ paddingLeft: "2px", paddingRight: "2px" }}
                            icon={<RightOutlined/>}
                            onClick={() => handleZoomOutChart("right")}
                        />
                    </Tooltip>
                </Button.Group>
            )}

            <Button.Group>
                <Popover
                    content={timePanel}
                    trigger="click"
                    visible={popoverVisible}
                    onVisibleChange={handlePopVisibleChange}
                    placement="bottomRight"
                >
                    <Button>
                        {get(selectedTime, "text", "Last 1 h")}
                        {currRefresh !== "off" ? `(${currRefresh}) ` : ""}
                        {currRefresh !== "off" && seconds ? `${seconds}s` : ""}
                    </Button>
                </Popover>

                <Tooltip title="强制刷新" placement="topRight">
                    <Button
                        htmlType="button"
                        icon={<SyncOutlined/>}
                        onClick={() => urlParamStore.forceChange()}
                    />
                </Tooltip>
            </Button.Group>
        </Space>
    );
};

interface TimePickerPanelProps extends TimePickerProps {
    onPickerOpen?: any;
    refreshState?: string;
    selectedTime?: any;
    onQuickRangeClick?: any;
    onRangeChange?: any;
    onRefreshSelect?: any;
    onToggleRefreshDropdown?: any;
    onApply?: any;
}

const TimePickerPanel: React.FC<TimePickerPanelProps> = props => {
    const { urlParamStore } = StoreManager;
    const { onPickerOpen, onRefreshSelect, onApply, onQuickRangeClick, onRangeChange, onToggleRefreshDropdown } = props;
    const { hasTimeShift, noTimeRefresh, selectedTime, refreshState } = props;

    const quickRangeSpan = hasTimeShift ? 6 : 8;
    const defaultSelectedTime = urlParamStore.getSelectedTime();
    const currSelectedTime = selectedTime || defaultSelectedTime;

    // 刷新频率选项
    const disableRefresh = (
        !get(selectedTime, "modelEnum", null) ||
        forbidRefresh.indexOf(get(selectedTime, "modelEnum", null)) > -1 ||
        noTimeRefresh
    );

    const options = disableRefresh
        ? [<Select.Option value="off" key="off">off</Select.Option>]
        : REFRESH_INTERVAL.map(e => <Select.Option value={e.value} key={e.value}>{e.label}</Select.Option>);

    const cls = classNames("emonitor-timepick", {
        timeshift: hasTimeShift,
    });

    return (
        <div className={cls}>
            <Divider style={{marginTop: 5}}>常用快捷选择</Divider>

            <Row>
                {hasTimeShift && (
                    <TimeQuickSelector span={quickRangeSpan} range={TIMESHIFT_RANGE} onClick={onQuickRangeClick}/>
                )}
                <TimeQuickSelector span={quickRangeSpan} range={QUICK_RANGE.DAYS} onClick={onQuickRangeClick}/>
                <TimeQuickSelector span={quickRangeSpan} range={QUICK_RANGE.PERIODS} onClick={onQuickRangeClick}/>
                <TimeQuickSelector span={quickRangeSpan} range={QUICK_RANGE.TIMES} onClick={onQuickRangeClick}/>
            </Row>

            <Divider>自定义选择</Divider>

            <Form>
                <Form.Item label="指定时间">
                    <DatePicker.RangePicker
                        format={TIME_FORMAT}
                        showTime={true}
                        onOpenChange={onPickerOpen}
                        defaultValue={[moment(defaultSelectedTime.fromTime), moment(defaultSelectedTime.toTime)]}
                        onChange={onRangeChange}
                        value={[moment(currSelectedTime.fromTime), moment(currSelectedTime.toTime)]}
                    />
                </Form.Item>

                <Form.Item label="刷新频率">
                    <Select
                        disabled={disableRefresh}
                        placeholder="Select refreshing time"
                        optionFilterProp="children"
                        value={refreshState || urlParamStore.getValue(REFRESH) || "off"}
                        onFocus={() => onToggleRefreshDropdown(true)}
                        onBlur={() => onToggleRefreshDropdown(false)}
                        onSelect={onRefreshSelect}
                    >
                        {options}
                    </Select>
                </Form.Item>
            </Form>

            <Row justify="center" style={{margin: "20px 0 10px"}}>
                <Button type="primary" onClick={onApply}>Apply</Button>
            </Row>
        </div>
    );
};

interface TimeQuickSelectorProps {
    span?: number | string;
    range?: any;
    onClick?: (col: any) => void;
}

const TimeQuickSelector: React.FC<TimeQuickSelectorProps> = props => {
    const {span, range, onClick} = props;

    return (
        <Col span={span}>
            {range.map((col: TimePickerModel | { value: string, title: string }) => (
                <a
                    key={col instanceof TimePickerModel ? col.text : col.value}
                    className="quick-select-btn"
                    onClick={() => onClick(col)}
                >
                    {col instanceof TimePickerModel ? col.text : col.title}
                </a>
            ))}
        </Col>
    );
};

export default observer(TimePicker);
