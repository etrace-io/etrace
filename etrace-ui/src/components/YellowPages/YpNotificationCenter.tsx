import React from "react";

interface YpNotificationCenterProps {
    className?: string;
}

interface YpNotificationCenterStatus {
}

export default class YpNotificationCenter extends React.Component<YpNotificationCenterProps, YpNotificationCenterStatus> {
    render() {
        const {className} = this.props;
        return (
            <div className={className}>
                {/*<div className="e-monitor-yellow-pages__title">通知中心</div>*/}

                {/*<div className="notification-center-list">*/}
                    {/*<a className="notification-center__item link" href="https://www.baidu.com">*/}
                        {/*<div className="item-content">18</div>*/}
                        {/*<div className="item-title">未处理的报警</div>*/}
                    {/*</a>*/}

                    {/*<a className="notification-center__item link" href="https://www.baidu.com">*/}
                        {/*<div className="item-content">15</div>*/}
                        {/*<div className="item-title">今日应用变更</div>*/}
                    {/*</a>*/}
                {/*</div>*/}
            </div>
        );
    }
}
