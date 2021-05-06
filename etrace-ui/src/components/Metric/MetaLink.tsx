import React from "react";
import {observer} from "mobx-react";
import {Target} from "$models/ChartModel";
import {Button, Dropdown, Menu} from "antd";
import StoreManager from "$store/StoreManager";
import {MonitorEntity} from "$services/MonitorEntityService";
import {LineChartOutlined, LinkOutlined, WarningOutlined} from "@ant-design/icons/lib";

const MetaLink: React.FC<{
    targets?: Target[];
    hiddenApp?: boolean;
}> = props => {
    const {targets, hiddenApp} = props;

    const {monitorEntityStore, urlParamStore} = StoreManager;

    if (!targets) {
        return null;
    }

    const menuMap: Map<string, string> = new Map();
    const alertMenuMap: Map<string, string> = new Map();

    const appLink = targets.map((target, index) => {
        if (!target.prefix) {
            return null;
        }

        const prefix = target.prefixVariate
            ? urlParamStore.getValue(target.prefixVariate)
            : target.prefix;

        const entity: MonitorEntity = monitorEntityStore.findEntityByCode(target.entity);

        if (entity && entity.config) {
            const config = JSON.parse(entity.config);
            if (config && config.url && !menuMap.get(prefix)) {
                // hasAppLink = true;
                menuMap.set(prefix, prefix);
                return (
                    <Menu.Item key={"App 监控" + prefix + index}>
                        <a href={config.url + prefix} target="_parent"><LineChartOutlined/> {prefix}</a>
                    </Menu.Item>
                );
            }
        }

        return null;
    }).filter(Boolean);

    const alertLink = targets.map((target, index) => {
        // has prefix
        if (target.prefix) {

            const prefix = target.prefixVariate
                ? urlParamStore.getValue(target.prefixVariate)
                : target.prefix;

            const entity: MonitorEntity = monitorEntityStore.findEntityByCode(target.entity);

            if (entity && entity.config) {
                const config = JSON.parse(entity.config);
                if (config && config.url) {
                    const url = `/alert/ruleList?entity=${target.entity}&prefix=${prefix}&measurement=${target.measurement}`;
                    if (!alertMenuMap.get(url)) {
                        alertMenuMap.set(url, url);
                        return (
                            <Menu.Item key={"报警配置" + index}>
                                <a rel="noopener noreferrer" href={url} target="_blank">
                                    <WarningOutlined/>{prefix + "." + target.measurement}
                                </a>
                            </Menu.Item>
                        );
                    }
                }
            }
        } else {
            // use measurement
            const url = `/alert/ruleList?entity=${target.entity}&measurement=${target.measurement}`;
            if (!alertMenuMap.get(url)) {
                alertMenuMap.set(url, url);
                return (
                    <Menu.Item key={"配置报警" + index}>
                        <a rel="noopener noreferrer" href={url} target="_blank">
                            <WarningOutlined/>{target.measurement}
                        </a>
                    </Menu.Item>
                );
            }
        }
        return null;
    }).filter(Boolean);

    const overlayMenu = (
        <Menu>
            {!hiddenApp && appLink.length > 0 && (<>
                <Menu.SubMenu title="App 监控">{appLink}</Menu.SubMenu>
                <Menu.Divider/>
            </>)}

            <Menu.SubMenu title="报警配置">{alertLink}</Menu.SubMenu>
        </Menu>
    );

    return (
        <Dropdown overlay={overlayMenu} placement="bottomRight">
            <Button
                type="dashed"
                shape="circle"
                size="small"
                icon={<LinkOutlined />}
            />
        </Dropdown>
    );
};

export default observer(MetaLink);
