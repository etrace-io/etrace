import {TokenService} from "./TokenService";
import React from "react";
import {Input, notification} from "antd";
import {autobind} from "core-decorators";
import {observer} from "mobx-react";

const Search = Input.Search;

interface TokenApplyPageProps {
}

interface TokenApplyPageStates {
}

@observer
export default class TokenManualCreate extends React.Component<TokenApplyPageProps, TokenApplyPageStates> {
    render() {
        return (
            <div className="e-monitor-tab-content">
                <div className="e-monitor-content-sections">
                    <Search
                        style={{width: 600}}
                        placeholder="输入带'E'的员工号"
                        enterButton="创建"
                        size="middle"
                        onSearch={value => this.create(value)}
                    />
                </div>
            </div>
        );
    }

    @autobind
    private create(psncode: string) {
        TokenService.adminManualCreate(psncode).then(ok => {
            notification.info({message: psncode + "创建成功", description: "去token页面查看", duration: 5});
        }).catch(err => {
            notification.error({message: psncode + "创建失败", description: err.message, duration: 10});
        });
    }

}
