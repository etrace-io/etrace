import {Input} from "antd";
import React, {useState} from "react";
import StoreManager from "$store/StoreManager";
import {EMonitorSection} from "$components/EMonitorLayout";
import {OrderOrShipSearchList} from "$components/search/OrderOrShipSearchList";

const OrderSearchPage: React.FC = props => {
    const {urlParamStore} = StoreManager;

    const [orderId, setOrderId] = useState(urlParamStore.getValue("orderId"));

    const handleSearchOrderId = id => {
        setOrderId(id);
        urlParamStore.changeURLParams({orderId: id});
    };

    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item type="card">
                <Input.Search
                    addonBefore="Order ID"
                    placeholder="Input Order ID..."
                    style={{width: 600}}
                    enterButton={true}
                    defaultValue={orderId}
                    onSearch={handleSearchOrderId}
                />
            </EMonitorSection.Item>

            <EMonitorSection.Item fullscreen={true} scroll={true}>
                <OrderOrShipSearchList id={orderId} type="1"/>
            </EMonitorSection.Item>
        </EMonitorSection>
    );
};

export default OrderSearchPage;
