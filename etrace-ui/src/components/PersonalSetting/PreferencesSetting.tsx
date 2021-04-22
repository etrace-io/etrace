import React from "react";
import {get} from "lodash";
import {observer} from "mobx-react";
import {ToolKit} from "$utils/Util";
import {Form, Select, Switch} from "antd";
import {changeThemeTo} from "$utils/Theme";
import StoreManager from "$store/StoreManager";
import TitleWithTooltip from "$components/Base/TitleWithTooltip";
import {SUPPORT_THEME} from "$constants/Theme";

const PreferencesSetting: React.FC = props => {
    const {userStore} = StoreManager;

    const handleAlertEnable = (checked: boolean) => {
        userStore.saveUserConfig({showAlert: checked});
    };

    const handlePublishEnable = (checked: boolean) => {
        userStore.saveUserConfig({showPublish: checked});
    };

    const selectOption = (value: any) => {
        userStore.saveUserConfig({theme: value});
        changeThemeTo(value);
    };

    return (
        <Form {...ToolKit.getFormLayout(10)}>
            <Form.Item label={<TitleWithTooltip title="显示报警事件" tooltip="设置图表上是否显示报警线"/>}>
                <Switch
                    checkedChildren="显示"
                    unCheckedChildren="隐藏"
                    checked={get(userStore.user, "userConfig.config.showAlert", false)}
                    onChange={handleAlertEnable}
                />
            </Form.Item>

            <Form.Item label={<TitleWithTooltip title="显示发布事件" tooltip="设置图表上是否显示发布线"/>}>
                <Switch
                    checkedChildren="显示"
                    unCheckedChildren="隐藏"
                    checked={get(userStore.user, "userConfig.config.showPublish", false)}
                    onChange={handlePublishEnable}
                />
            </Form.Item>

            <Form.Item label={<TitleWithTooltip title="选择主题" tooltip="非常抱歉，黑色主题暂时下线，请等待后续更新"/>}>
                <Select defaultValue={userStore.getTheme()} onChange={selectOption} disabled={true}>
                    {SUPPORT_THEME.map(theme => (
                        <Select.Option value={theme} key={theme}>{theme}</Select.Option>
                    ))}
                </Select>
            </Form.Item>
        </Form>
    );
};

export default observer(PreferencesSetting);
