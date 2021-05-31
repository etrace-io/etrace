import React from "react";
import {Tabs} from "antd";
import TokenViewPage from "./TokenViewPage";
import TokenApplyLogPage from "./TokenApplyLogPage";
import TokenManualCreate from "./TokenManualCreate";
import {EMonitorSection} from "$components/EMonitorLayout";
import {SPACE_BETWEEN} from "$constants/index";

const TokenManagePage: React.FC = props => {
    return (
        <EMonitorSection fullscreen={true}>
            <EMonitorSection.Item type="tabs" fullscreen={true} scroll={true}>
                <Tabs defaultActiveKey="1">
                    <Tabs.TabPane tab="申请管理" key="1"  style={{padding: SPACE_BETWEEN}}>
                        <TokenApplyLogPage/>
                    </Tabs.TabPane>
                    <Tabs.TabPane tab="所有Token" key="2"  style={{padding: SPACE_BETWEEN}}>
                        <TokenViewPage/>
                    </Tabs.TabPane>
                    <Tabs.TabPane tab="手动创建" key="3"  style={{padding: SPACE_BETWEEN}}>
                        <TokenManualCreate/>
                    </Tabs.TabPane>
                </Tabs>
            </EMonitorSection.Item>
        </EMonitorSection>
    );
};

export default TokenManagePage;
