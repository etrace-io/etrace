import React from "react";
import {Button, Input, Popover, Tooltip} from "antd";
import * as AllIcon from "@ant-design/icons";

interface IconSelectorProps {
    value?: string;
    defaultValue?: string;
    onChange?: (icon: string) => void;
}

interface IconSelectorStatus {
    currIcon: string;
    searchValue: string;
}

export default class IconSelector extends React.Component<IconSelectorProps, IconSelectorStatus> {
    public static DATA_ICON = ["AreaChartOutlined", "PieChartOutlined", "BarChartOutlined", "DotChartOutlined", "LineChartOutlined", "RadarChartOutlined", "HeatMapOutlined", "FallOutlined", "RiseOutlined", "StockOutlined", "BoxPlotOutlined", "FundOutlined", "SlidersOutlined"];
    public static COMMON_ICON = ["AccountBookOutlined", "AimOutlined", "AlertOutlined", "ApartmentOutlined", "ApiOutlined", "AppstoreAddOutlined", "AppstoreOutlined", "AudioOutlined", "AudioMutedOutlined", "AuditOutlined", "BankOutlined", "BarcodeOutlined", "BarsOutlined", "BellOutlined", "BlockOutlined", "BookOutlined", "BorderOutlined", "BorderlessTableOutlined", "BranchesOutlined", "BugOutlined", "BuildOutlined", "BulbOutlined", "CalculatorOutlined", "CalendarOutlined", "CameraOutlined", "CarOutlined", "CarryOutOutlined", "CiCircleOutlined", "CiOutlined", "ClearOutlined", "CloudDownloadOutlined", "CloudOutlined", "CloudServerOutlined", "CloudSyncOutlined", "CloudUploadOutlined", "ClusterOutlined", "CodeOutlined", "CoffeeOutlined", "CommentOutlined", "CompassOutlined", "CompressOutlined", "ConsoleSqlOutlined", "ContactsOutlined", "ContainerOutlined", "ControlOutlined", "CopyrightCircleOutlined", "CopyrightOutlined", "CreditCardOutlined", "CrownOutlined", "CustomerServiceOutlined", "DashboardOutlined", "DatabaseOutlined", "DeleteColumnOutlined", "DeleteRowOutlined", "DeliveredProcedureOutlined", "DeploymentUnitOutlined", "DesktopOutlined", "DingtalkOutlined", "DisconnectOutlined", "DislikeOutlined", "DollarCircleOutlined", "DollarOutlined", "DownloadOutlined", "EllipsisOutlined", "EnvironmentOutlined", "EuroCircleOutlined", "EuroOutlined", "ExceptionOutlined", "ExpandAltOutlined", "ExpandOutlined", "ExperimentOutlined", "ExportOutlined", "EyeOutlined", "EyeInvisibleOutlined", "FieldBinaryOutlined", "FieldNumberOutlined", "FieldStringOutlined", "FieldTimeOutlined", "FileAddOutlined", "FileDoneOutlined", "FileExcelOutlined", "FileExclamationOutlined", "FileOutlined", "FileGifOutlined", "FileImageOutlined", "FileJpgOutlined", "FileMarkdownOutlined", "FilePdfOutlined", "FilePptOutlined", "FileProtectOutlined", "FileSearchOutlined", "FileSyncOutlined", "FileTextOutlined", "FileUnknownOutlined", "FileWordOutlined", "FileZipOutlined", "FilterOutlined", "FireOutlined", "FlagOutlined", "FolderAddOutlined", "FolderOutlined", "FolderOpenOutlined", "FolderViewOutlined", "ForkOutlined", "FormatPainterOutlined", "FrownOutlined", "FunctionOutlined", "FundProjectionScreenOutlined", "FundViewOutlined", "FunnelPlotOutlined", "GatewayOutlined", "GifOutlined", "GiftOutlined", "GlobalOutlined", "GoldOutlined", "GroupOutlined", "HddOutlined", "HeartOutlined", "HistoryOutlined", "HomeOutlined", "HourglassOutlined", "IdcardOutlined", "ImportOutlined", "InboxOutlined", "InsertRowAboveOutlined", "InsertRowBelowOutlined", "InsertRowLeftOutlined", "InsertRowRightOutlined", "InsuranceOutlined", "InteractionOutlined", "KeyOutlined", "LaptopOutlined", "LayoutOutlined", "LikeOutlined", "LineOutlined", "LinkOutlined", "Loading3QuartersOutlined", "LoadingOutlined", "LockOutlined", "MacCommandOutlined", "MailOutlined", "ManOutlined", "MedicineBoxOutlined", "MehOutlined", "MenuOutlined", "MergeCellsOutlined", "MessageOutlined", "MobileOutlined", "MoneyCollectOutlined", "MonitorOutlined", "MoreOutlined", "NodeCollapseOutlined", "NodeExpandOutlined", "NodeIndexOutlined", "NotificationOutlined", "NumberOutlined", "OneToOneOutlined", "PaperClipOutlined", "PartitionOutlined", "PayCircleOutlined", "PercentageOutlined", "PhoneOutlined", "PictureOutlined", "PlaySquareOutlined", "PoundCircleOutlined", "PoundOutlined", "PoweroffOutlined", "PrinterOutlined", "ProfileOutlined", "ProjectOutlined", "PropertySafetyOutlined", "PullRequestOutlined", "PushpinOutlined", "QrcodeOutlined", "ReadOutlined", "ReconciliationOutlined", "RedEnvelopeOutlined", "ReloadOutlined", "RestOutlined", "RobotOutlined", "RocketOutlined", "RotateLeftOutlined", "RotateRightOutlined", "SafetyCertificateOutlined", "SafetyOutlined", "SaveOutlined", "ScanOutlined", "ScheduleOutlined", "SearchOutlined", "SecurityScanOutlined", "SelectOutlined", "SendOutlined", "SettingOutlined", "ShakeOutlined", "ShareAltOutlined", "ShopOutlined", "ShoppingCartOutlined", "ShoppingOutlined", "SisternodeOutlined", "SkinOutlined", "SmileOutlined", "SolutionOutlined", "SoundOutlined", "SplitCellsOutlined", "StarOutlined", "SubnodeOutlined", "SwitcherOutlined", "SyncOutlined", "TableOutlined", "TabletOutlined", "TagOutlined", "TagsOutlined", "TeamOutlined", "ThunderboltOutlined", "ToTopOutlined", "ToolOutlined", "TrademarkCircleOutlined", "TrademarkOutlined", "TransactionOutlined", "TranslationOutlined", "TrophyOutlined", "UngroupOutlined", "UnlockOutlined", "UploadOutlined", "UsbOutlined", "UserAddOutlined", "UserDeleteOutlined", "UserOutlined", "UserSwitchOutlined", "UsergroupAddOutlined", "UsergroupDeleteOutlined", "VerifiedOutlined", "VideoCameraAddOutlined", "VideoCameraOutlined", "WalletOutlined", "WhatsAppOutlined", "WifiOutlined", "WomanOutlined"];
    public static BRAND_ICON = ["AndroidOutlined", "AppleOutlined", "WindowsOutlined", "IeOutlined", "ChromeOutlined", "GithubOutlined", "AliwangwangOutlined", "DingdingOutlined", "WeiboSquareOutlined", "WeiboCircleOutlined", "TaobaoCircleOutlined", "Html5Outlined", "WeiboOutlined", "TwitterOutlined", "WechatOutlined", "YoutubeOutlined", "AlipayCircleOutlined", "TaobaoOutlined", "SkypeOutlined", "QqOutlined", "MediumWorkmarkOutlined", "GitlabOutlined", "MediumOutlined", "LinkedinOutlined", "GooglePlusOutlined", "DropboxOutlined", "FacebookOutlined", "CodepenOutlined", "CodeSandboxOutlined", "AmazonOutlined", "GoogleOutlined", "CodepenCircleOutlined", "AlipayOutlined", "AntDesignOutlined", "AntCloudOutlined", "AliyunOutlined", "ZhihuOutlined", "SlackOutlined", "SlackSquareOutlined", "BehanceOutlined", "BehanceSquareOutlined", "DribbbleOutlined", "DribbbleSquareOutlined", "InstagramOutlined", "YuqueOutlined", "AlibabaOutlined", "YahooOutlined", "RedditOutlined", "SketchOutlined"];

    public static ICON_LIST: [string, string[]][] = [
        ["常用图标", IconSelector.COMMON_ICON],
        ["数据类图标", IconSelector.DATA_ICON],
        ["品牌标识", IconSelector.BRAND_ICON],
    ];

    constructor(props: IconSelectorProps) {
        super(props);
        const currIcon = typeof props.value === "undefined" ? props.defaultValue : props.value;
        this.state = {
            currIcon,
            searchValue: "",
        };
    }

    handleIconChange = (icon: string) => {
        const {onChange} = this.props;
        if (onChange) {
            onChange(icon);
        }
        this.setState({
            currIcon: icon
        });
    };

    handleSearchValueChange = (e) => {
        const searchValue = e.target.value;
        this.setState({searchValue});
    };

    renderIconSearch = (value: string) => {
        return (
            <Input
                key="icon-search-box"
                className="list-icon-chooser__search-box"
                placeholder="搜索图标"
                onChange={this.handleSearchValueChange}
                value={value}
                allowClear={true}
            />
        );
    };

    renderIconPanel = (list: [string, string[]][]) => {
        const {value, defaultValue} = this.props;
        const {currIcon, searchValue} = this.state;

        const target = AllIcon[value || currIcon || defaultValue]
            ? (value || currIcon || defaultValue)
            : "AppstoreOutlined";

        return list.map(([title, group]) => {
            const icons = searchValue ? group.filter(icon => icon.indexOf(searchValue) > -1) : group;
            return icons.length === 0 ? null : (
                <div key={title}>
                    <p className="list-icon-chooser__list-title">{title}</p>
                    <ul className="list-icon-chooser__icons-list">
                        {icons.map(icon => {
                            const Icon = AllIcon[icon];
                            const wrappedIcon = (
                                <li
                                    key={icon}
                                    className={icon === target ? "selected" : ""}
                                    onClick={() => this.handleIconChange(icon)}
                                >
                                    <Icon/>
                                </li>
                            );
                            return <Tooltip
                                key={icon}
                                mouseEnterDelay={0.5}
                                title={icon.replace("Outlined", "")}
                            >
                                {wrappedIcon}
                            </Tooltip>;
                            // return searchValue.length === 0
                            //     ? wrappedIcon
                            //     : <Tooltip key={icon} title={icon}>{wrappedIcon}</Tooltip>;
                        })}
                    </ul>
                </div>
            );
        });
    };

    render() {
        const {value, defaultValue} = this.props;
        const {currIcon, searchValue} = this.state;
        const content = [
            this.renderIconSearch(searchValue),
            this.renderIconPanel(IconSelector.ICON_LIST),
        ];

        const IconComponent = AllIcon[value || currIcon || defaultValue] || AllIcon.AppstoreOutlined;

        return (
            <Popover placement="bottom" title="选择图标" overlayClassName="e-monitor-yellow-pages__list-icon-chooser" content={content} trigger="click">
                <Button className="list-icon-chooser-btn" type="link" icon={<IconComponent/>}/>
            </Popover>
        );
    }
}
