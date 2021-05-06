import {reaction} from "mobx";
import {observer} from "mobx-react";
import React, {useEffect, useState} from "react";
import StoreManager from "$store/StoreManager";
import {TimePickerModel} from "$models/TimePickerModel";
import {Button, Modal, Space, Table, Tooltip} from "antd";
import {HolmesApiService} from "$services/HolmesApiService";
import {AlertOutlined, BulbOutlined} from "@ant-design/icons/lib";
import {columnsAlert, columnsEvent} from "$constants/EventConstants";

const EVENT_PAGE_SIZE = 10;
enum EVENT_TYPE {
    Alert = "alert",
    Change = "change",
}

const EventTimeLine: React.FC = props => {
    const {eventStore, urlParamStore} = StoreManager;

    const [modalVisible, setModalVisible] = useState<boolean>(false);
    const [dataSource, setDataSource] = useState<any[]>([]);
    const [needLoad, setNeedLoad] = useState<boolean>(true);
    const [currEventType, setEventType] = useState<EVENT_TYPE>();
    const [currPage, setCurrPage] = useState<number>(1);

    useEffect(() => {
        const disposer = reaction(
            () => StoreManager.eventStore.changed,
            changed => {
                eventStore.getAllChangeEvents();
                eventStore.getAllAlertEvents();
            });

        return () => {
            disposer();
            eventStore.clearAll();
        };
    }, []);

    const handleChangeEvent = () => {
        eventStore.changeEventDisplay = !eventStore.changeEventDisplay;
    };

    const handleAlertEvent = () => {
        eventStore.alertEventDisplay = !eventStore.alertEventDisplay;
    };

    const handleLoadMore = () => {
        const selectedTime: TimePickerModel = StoreManager.urlParamStore.getSelectedTime();
        this.page += 1;
        let queries;
        switch (currEventType) {
            case EVENT_TYPE.Alert:
                queries = Array
                    .from(StoreManager.eventStore.alertEvents.keys())
                    .map(appId =>
                        HolmesApiService.getAlertEventsByAppId(
                            appId,
                            selectedTime.from,
                            selectedTime.to,
                            currPage,
                            EVENT_PAGE_SIZE,
                        )
                    );
                break;
            case EVENT_TYPE.Change:
                queries = Array
                    .from(StoreManager.eventStore.changeEvents.keys())
                    .map(appId =>
                        HolmesApiService.getChangeEventsByAppId(
                            appId,
                            selectedTime.from,
                            selectedTime.to,
                            currPage,
                            EVENT_PAGE_SIZE,
                        )
                    );
                break;
            default:
                break;
        }

        if (queries) {
            Promise.all(queries).then(data => {
                const originDataSource = dataSource.slice();
                data.forEach(d => {
                    // @ts-ignore
                    originDataSource.push(...d);
                });

                setNeedLoad(originDataSource.length === currPage * EVENT_PAGE_SIZE);
                setDataSource(originDataSource);
            });
        }
    };

    const handleCloseModal = () => {
        setCurrPage(1);
        setModalVisible(false);
    };

    return (
        <>
            <Space>
                {eventStore.totalChangeCount > 0 && (
                    <Tooltip title={`点击${eventStore.changeEventDisplay ? "隐藏" : "显示"}变更事件`}>
                        <Button
                            style={{border: "unset"}}
                            onClick={handleChangeEvent}
                            className={(eventStore.changeEventDisplay ? "change-event" : "change-event-uncheck") + " ant-btn-icon-only"}
                            icon={<BulbOutlined/>}
                            type={eventStore.changeEventDisplay ? "primary" : "default"}
                        />
                    </Tooltip>
                )}

                {eventStore.totalAlertCount > 0 && (
                    <Tooltip title={`点击${eventStore.alertEventDisplay ? "隐藏" : "显示"}报警事件`}>
                        <Button
                            style={{border: "unset"}}
                            danger={true}
                            onClick={handleAlertEvent}
                            className={(eventStore.alertEventDisplay ? "alert-event" : "alert-event-uncheck") + " ant-btn-icon-only"}
                            icon={<AlertOutlined />}
                            type={eventStore.alertEventDisplay ? "primary" : "default"}
                        />
                    </Tooltip>
                )}
            </Space>

            <Modal
                footer={null}
                style={{top: 50}}
                width="90%"
                title={currEventType === EVENT_TYPE.Alert ? "报警事件详情" : "变更事件详情"}
                visible={modalVisible}
                onCancel={handleCloseModal}
            >
                <Table
                    dataSource={dataSource}
                    columns={currEventType === EVENT_TYPE.Alert ? columnsAlert : columnsEvent}
                    rowKey={(record, index) => index.toString()}
                    size="small"
                    pagination={false}
                />

                {needLoad && (
                    <div style={{textAlign: "center", marginTop: 12, height: 32, lineHeight: "32px"}}>
                        <Button onClick={handleLoadMore}>加载更多</Button>
                    </div>
                )}
            </Modal>
        </>
    );
};

export default observer(EventTimeLine);
