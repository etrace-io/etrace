import React, {useState} from "react";
import {Button, Modal, Radio, Tooltip} from "antd";
import {observer} from "mobx-react";
import {Callstacks} from "../CallstackPage";
import {DataFormatter} from "$utils/DataFormatter";
import StoreManager from "$store/StoreManager";
import {ArrowsAltOutlined, ShrinkOutlined} from "@ant-design/icons/lib";
import {getPrefixCls} from "$utils/Theme";

import "./SamplingModal.less";
import "../SamplingModal.css";

const SamplingModalClass = getPrefixCls("sampling-modal");

const SamplingModal: React.FC = props => {
    const {callstackStore} = StoreManager;

    const handleCancel = () => {
        callstackStore.clearSamplings();
    };

    return (
        <Modal
            className={SamplingModalClass}
            destroyOnClose={true}
            footer={null}
            width="95%"
            visible={callstackStore.isStartToQuerySampling}
            onCancel={handleCancel}
            title="调用链路"
            style={{top: 30}}
        >
            <SamplingTimeRadio/>

            <Callstacks requestId={callstackStore.selectedRequestId}/>
        </Modal>
    );
};

const SamplingTimeRadio: React.FC = observer(props => {
    const {callstackStore} = StoreManager;
    const MAX_COUNT = 10;

    const [collapse, setCollapse] = useState<boolean>(true);

    if (
        !callstackStore.callstackShowHead ||
        !callstackStore.selectedRequestId ||
        callstackStore.samplings.length === 0
    ) {
        return null;
    }

    const handleRadioChange = (e: any) => {
        callstackStore.loadCallstack(e.target.value);
    };

    const metricType = callstackStore.metricType;

    const radioButtonList = callstackStore.samplingsAsRequestIdAndTime.map((sampling, index) => {
        switch (metricType) {
            case "histogram":
            case "timer":
                return (
                    <Radio.Button key={sampling.requestId} value={sampling.requestId}>
                        <Tooltip color="blue" title={"服务最大耗时:" + DataFormatter.transformMilliseconds(sampling.time, 3)}>
                            <span>{DataFormatter.transformMilliseconds(sampling.time)}</span>
                        </Tooltip>
                    </Radio.Button>
                );
            case "payload":
                return <Radio.Button key={sampling.requestId} value={sampling.requestId}>{DataFormatter.transformBytes(sampling.time)}</Radio.Button>;
            case "counter":
            default:
                return <Radio.Button key={sampling.requestId} value={sampling.requestId}>{index}</Radio.Button>;
        }
    });

    return (
        <div className={`${SamplingModalClass}-radio-group`}>
            <Radio.Group
                value={callstackStore.selectedRequestId}
                onChange={handleRadioChange}
                size="small"
                buttonStyle="solid"
            >
                {collapse ? radioButtonList.slice(0, MAX_COUNT) : radioButtonList}
            </Radio.Group>

            <Button
                size="small"
                type="primary"
                icon={collapse ? <ArrowsAltOutlined /> : <ShrinkOutlined />}
                onClick={() => setCollapse(c => !c)}
            >
                {collapse ? "显示所有" : "收起"}
            </Button>
        </div>
    );
});

export default observer(SamplingModal);
