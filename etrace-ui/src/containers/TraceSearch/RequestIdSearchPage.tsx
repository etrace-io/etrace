import moment from "moment";
import {reaction} from "mobx";
import {observer} from "mobx-react";
import {DatePicker, Input, Space} from "antd";
import StoreManager from "$store/StoreManager";
import {SPACE_BETWEEN} from "$constants/index";
import React, {useEffect, useMemo, useState} from "react";
import {EMonitorSection} from "$components/EMonitorLayout";
import TraceSampling from "$components/search/TraceSampling";

const REQUEST_ID_URL_KEY = "requestId";
const DATE_URL_KEY = "date";

const RequestIdSearchPage: React.FC = props => {
    const {urlParamStore} = StoreManager;


    const [urlDate, setURLDate] = useState<string>(() => urlParamStore.getValue(DATE_URL_KEY));
    const [requestId, setRequestId] = useState<string>(() => urlParamStore.getValue(REQUEST_ID_URL_KEY));

    useEffect(() => {
        const disposer = reaction(
            () => urlParamStore.getValue(DATE_URL_KEY),
            result => setURLDate(result),
        );

        return () => disposer();
    }, []);

    useEffect(() => {
        const disposer = reaction(
            () => urlParamStore.getValue(REQUEST_ID_URL_KEY),
            result => setRequestId(result),
        );

        return () => disposer();
    }, []);


    // 获取目标时间
    const currDate = useMemo(() => {
        if (urlDate) {
            return new Date(urlDate);
        }

        if (requestId) {
            const idx = +requestId.split("|")[1];
            if (idx) {
                return new Date(idx);
            }
        }

        return new Date();
    }, [urlDate, requestId]);

    const handleDateChange = (date: any, dateString: string) => {
        urlParamStore.changeURLParams({[DATE_URL_KEY]: dateString});
    };

    const handleSearchRequestId = id => {
        urlParamStore.changeURLParams({[REQUEST_ID_URL_KEY]: id});
    };

    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item type="card">
                <Space size={SPACE_BETWEEN}>
                    <DatePicker
                        placeholder="请选择查询日期"
                        defaultValue={moment(currDate)}
                        onChange={handleDateChange}
                        disabledDate={date => date && ((new Date()).valueOf() - date.valueOf()) > 7 * 24 * 60 * 60 * 1000}
                    />

                    <Input.Search
                        enterButton={true}
                        addonBefore="RequestID"
                        style={{width: 600}}
                        placeholder="Input RequestID..."
                        defaultValue={requestId}
                        onSearch={handleSearchRequestId}
                    />
                </Space>
            </EMonitorSection.Item>

            <TraceSampling requestId={requestId && requestId.trim()} timestamp={+currDate}/>
        </EMonitorSection>
    );
};

export default observer(RequestIdSearchPage);
