import {CopyOutlined, ShareAltOutlined} from "@ant-design/icons/lib";
import React from "react";
import {autobind} from "core-decorators";
import {CopyToClipboard} from "react-copy-to-clipboard";
import {Button, Checkbox, DatePicker, Form, Input, Modal, Radio, Row, Select, Tooltip} from "antd";
import {APP_BASE_URL} from "$constants/index";

interface ShareBoardBtnProps {
    boardId: number;
}

interface ShareBoardBtnStatus {
    modalVisible: boolean;
    theme: string;
    refresh: string;
    timeRange: string;
    copied: boolean;
    showName: boolean;
    hideToolsBar: boolean;
    needFullScreen: boolean;
    customTimeRange: boolean;
}

export default class ShareBoardBtn extends React.Component<ShareBoardBtnProps, ShareBoardBtnStatus> {
    state = {
        modalVisible: false,
        theme: "Light",
        copied: false,
        refresh: "off",
        timeRange: "from=now()-1h&to=now()-30s",
        showName: true,
        hideToolsBar: false,
        needFullScreen: false,
        customTimeRange: false,
    };

    handleModalVisible(visible: boolean) {
        this.setState({
            modalVisible: visible
        });
    }

    @autobind
    handleThemeChange(e: any) {
        this.setState({
            theme: e.target.value,
        });
    }

    @autobind
    handleCustomTimeRangeChange(date: any, dateString: string[]) {
        this.setState({
            timeRange: `from=${dateString[0]}&to=${dateString[1]}`,
        });
    }

    @autobind
    handleShowName(e: any) {
        this.setState({
            showName: e.target.checked
        });
    }

    @autobind
    handleFullScreenButton(e: any) {
        this.setState({
            needFullScreen: e.target.checked
        });
    }

    @autobind
    handleCustomTimeRange(e: any) {
        this.setState({
            customTimeRange: e.target.checked
        });
    }

    @autobind
    handleHideToolsBar(e: any) {
        this.setState({
            hideToolsBar: e.target.checked
        });
    }

    @autobind
    handleRefreshChange(value: string) {
        this.setState({
            refresh: value
        });
    }

    @autobind
    handleTimeRangeChange(value: string) {
        this.setState({
            timeRange: value
        });
    }

    @autobind
    handleTextCopied() {
        this.setState({copied: true});
        setTimeout(() => this.setState({copied: false}), 2000);
    }

    @autobind
    getCode() {
        const {theme, hideToolsBar, needFullScreen, refresh, timeRange, showName} = this.state;
        const {boardId} = this.props;

        const params = {
            theme,
            refresh,
            showName,
            hideToolsBar,
            needFullScreen,
        };

        const paramsStr = Object
            .keys(params)
            .filter(key => params[key])
            .map(key => `${key}=${params[key]}`)
            .concat(timeRange)
            .join("&");

        const src = `${APP_BASE_URL}/board/share/${boardId}?${paramsStr}`;

        const attrs = {
            src,
            width: "100%",
            height: "700px",
            scrolling: "no",
            frameborder: "no",
            title: "E-Monitor",
            allowfullscreen: needFullScreen ? "true" : null,
        };

        const attrStr = Object
            .keys(attrs)
            .filter(key => attrs[key])
            .map(key => `${key}="${attrs[key]}"`)
            .join(" ");

        return `<iframe ${attrStr}></iframe>`;
    }

    render() {
        const { modalVisible, theme, timeRange, refresh, customTimeRange, showName, copied } = this.state;
        const formLayout = customTimeRange
            ? {labelCol: {span: 7}, wrapperCol: {span: 15, offset: 1}, style: {width: "100%"}}
            : {labelCol: {span: 10}, wrapperCol: {span: 12, offset: 1}, style: {width: "100%"}};

        const clipBtn = (
            <CopyToClipboard text={this.getCode()} onCopy={this.handleTextCopied}>
                <Button icon={<CopyOutlined />}>{copied ? "Copied!" : "Copy"}</Button>
            </CopyToClipboard>
        );

        return (
            <>
                <Tooltip title="外嵌看板">
                    <Button
                        icon={<ShareAltOutlined />}
                        style={{borderTopLeftRadius: 0, borderBottomLeftRadius: 0}}
                        onClick={() => this.handleModalVisible(true)}
                    />
                </Tooltip>

                <Modal
                    title="Embed This Board"
                    visible={modalVisible}
                    width={800}
                    // centered={true}
                    footer={null}
                    onCancel={() => this.handleModalVisible(false)}
                >
                    <Form layout="inline">
                        <Form.Item label="Theme" {...formLayout}>
                            <Radio.Group value={theme} onChange={this.handleThemeChange}>
                                <Radio value="Light">Light</Radio>
                                <Radio value="Dark">Dark</Radio>
                            </Radio.Group>
                        </Form.Item>

                        <Form.Item label="Show Board Name" {...formLayout}>
                            <Checkbox defaultChecked={showName} onChange={this.handleShowName}/>
                        </Form.Item>

                        <Form.Item label="FullScreen Button" {...formLayout}>
                            <Checkbox onChange={this.handleFullScreenButton}/>
                        </Form.Item>

                        <Form.Item label="Hide Tools Bar" {...formLayout}>
                            <Checkbox onChange={this.handleHideToolsBar}/>
                        </Form.Item>

                        <Form.Item label="Time Range" {...formLayout}>
                            {!customTimeRange && (
                                <Select
                                    defaultValue={timeRange}
                                    style={{ width: 120 }}
                                    onChange={this.handleTimeRangeChange}
                                >
                                    <Select.Option value="from=now()-5m&to=now()-30s">Last 5 m</Select.Option>
                                    <Select.Option value="from=now()-15m&to=now()-30s">Last 15 m</Select.Option>
                                    <Select.Option value="from=now()-30m&to=now()-30s">Last 30 m</Select.Option>
                                    <Select.Option value="from=now()-1h&to=now()-30s">Last 1 h</Select.Option>
                                    <Select.Option value="from=now()-3h&to=now()-30s">Last 3 h</Select.Option>
                                    <Select.Option value="from=now()-6h&to=now()-30s">Last 6 h</Select.Option>
                                    <Select.Option value="from=now()-12h&to=now()-30s">Last 12 h</Select.Option>
                                </Select>
                            )}

                            {customTimeRange && (
                                <DatePicker.RangePicker
                                    showTime={{ format: "HH:mm:ss" }}
                                    format="YYYY-MM-DD+HH:mm:ss"
                                    placeholder={["Start Time", "End Time"]}
                                    onChange={this.handleCustomTimeRangeChange}
                                />
                            )}

                            <Checkbox style={{marginLeft: 15}} onChange={this.handleCustomTimeRange}>Custom</Checkbox>
                        </Form.Item>

                        <Form.Item label="Refresh" {...formLayout}>
                            <Select
                                defaultValue={refresh}
                                style={{ width: 120 }}
                                onChange={this.handleRefreshChange}
                            >
                                <Select.Option value="off">off</Select.Option>
                                <Select.Option value="10s">10s</Select.Option>
                                <Select.Option value="30s">30s</Select.Option>
                                <Select.Option value="1m">1m</Select.Option>
                            </Select>
                        </Form.Item>

                        <Input.TextArea
                            style={{marginTop: 20}}
                            readOnly={true}
                            autoSize={true}
                            value={this.getCode()}
                        />
                    </Form>

                    <Row  justify="center" style={{marginTop: 15}}>
                        {clipBtn}
                    </Row>
                </Modal>
            </>
        );
    }
}
