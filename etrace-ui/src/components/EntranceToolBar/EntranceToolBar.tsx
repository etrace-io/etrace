import {debounce} from "lodash";
import {Card, Row, Space} from "antd";
import StoreManager from "$store/StoreManager";
import {EventStatusEnum} from "$models/HolmesModel";
import SearchInput from "$components/Base/SearchInput";
import {LocalStorageUtil} from "$utils/LocalStorageUtil";
import React, {useCallback, useEffect, useMemo, useState} from "react";
import {buildAppIdNotFoundTooltipTitle} from "$containers/app/EMonitorApp";
import TimePicker, {TimePickerProps} from "$components/TimePicker/TimePicker";
import {APP_ID} from "$constants/index";

interface EntranceToolBarProps extends TimePickerProps {
    dataSource: (value: string) => Promise<any>;
    urlKey: string;
    title?: string;
    storageKey?: string;
    placeholder?: string;
    isAppId?: boolean; // 针对 AppID 做差异化处理，比如 notFoundTip 等
    /* handle */
    onSelect?: (value?: string) => void;
}

const EntranceToolBar: React.FC<EntranceToolBarProps> = props => {
    const {title, urlKey, storageKey, dataSource, placeholder, children, isAppId, onSelect, ...otherProps} = props;
    const {urlParamStore, eventStore} = StoreManager;

    const [searchDataSource, setSearchDataSource] = useState([]);
    const [notFound, setNotFound] = useState<string>();

    const notFoundTooltip = useMemo(() => {
        return notFound ? buildAppIdNotFoundTooltipTitle(notFound) : null;
    }, [notFound]);

    const historyOfKey: string[] = storageKey ? LocalStorageUtil.getStringValues(storageKey) : [];

    useEffect(() => {
        if (
            !urlParamStore.getValue(urlKey) &&
            historyOfKey &&
            historyOfKey.length > 0
        ) {
            urlParamStore.changeURLParams({[urlKey]: historyOfKey[0]}, [], false, "replace");
        }
    }, []);

    const handleSearchChange = value => {
        if (!value) {
            return;
        }
        dataSource && dataSource(value).then(items => {
            setSearchDataSource(items || []);
            if (isAppId || urlKey === APP_ID) {
                setNotFound(items.length === 0 ? value : null);
            }
        });
    };

    const handleSearchBlur = value => {
        if (isAppId || urlKey === APP_ID) {
            setNotFound(null);
        }
        urlParamStore.changeURLParams({[urlKey]: value});
    };

    const handleSearchResultSelect = value => {
        urlParamStore.changeURLParams({[urlKey]: value}, [], true);

        onSelect && onSelect(value);

        if (isAppId || urlKey === APP_ID) {
            eventStore.clearAll();
            eventStore.register(value, {status: EventStatusEnum.Init});
        }
    };

    const debounceSearch = useCallback(debounce(handleSearchChange, 500), []);

    return (
        <Card size="small">
            <Row justify="space-between">
                <Space>
                    <SearchInput
                        title={title}
                        dataSource={searchDataSource}
                        placeholder={placeholder}
                        defaultValue={urlParamStore.getValue(urlKey) || historyOfKey[0]}
                        storageKey={storageKey}
                        onSelect={handleSearchResultSelect}
                        onBlur={handleSearchBlur}
                        onChange={debounceSearch}
                        notFoundTooltip={notFoundTooltip}
                    />
                    {children}
                </Space>

                <TimePicker
                    showCurrTimeShift={props.hasTimeShift === undefined ? true : props.hasTimeShift}
                    hasTimeShift={true}
                    hasTimeZoom={true}
                    noTimeRefresh={false}
                    {...otherProps}
                />
            </Row>
        </Card>
    );
};

export default EntranceToolBar;
