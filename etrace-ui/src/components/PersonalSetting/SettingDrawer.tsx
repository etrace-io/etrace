import React from "react";
import {Drawer} from "antd";
import OrderBySetting from "$components/PersonalSetting/OrderBySetting";
import PreferencesSetting from "$components/PersonalSetting/PreferencesSetting";

const SettingDrawer: React.FC<{
    collapse: boolean;
    onClose: () => void;
}> = props => {
    const {collapse, onClose} = props;

    return (
        <Drawer
            title="个人配置"
            width={350}
            visible={collapse}
            onClose={onClose}
            placement="right"
            zIndex={1000}
        >
            <OrderBySetting/>
            <PreferencesSetting/>
        </Drawer>
    );
};

export default SettingDrawer;
