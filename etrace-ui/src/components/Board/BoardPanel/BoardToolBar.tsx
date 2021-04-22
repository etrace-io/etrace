import {get} from "lodash";
import {useFullscreen} from "ahooks";
import {Link} from "react-router-dom";
import {Board} from "$models/BoardModel";
import StoreManager from "$store/StoreManager";
import React, {useEffect, useState} from "react";
import {FULL_SCREEN_CHART} from "$constants/Route";
import * as BoardService from "$services/BoardService";
import TimePicker from "$components/TimePicker/TimePicker";
import ShareBoardBtn from "$components/Board/ShareBoardBtn";
import {ListBoardTree} from "$containers/Board/ListBoardTree";
import {Button, Card, Dropdown, Menu, Row, Space, Tooltip} from "antd";
import MultiVariateSelect from "$components/VariateSelector/MultiVariateSelect";
import {
    AppstoreOutlined,
    DownOutlined,
    EditOutlined,
    FullscreenExitOutlined,
    FullscreenOutlined,
    LinkOutlined,
    StarFilled,
    StarOutlined
} from "@ant-design/icons";

export interface BoardToolBarProps {
    board: Board;

    /* 显示配置 */
    hideBoardChoose?: boolean;              // 隐藏便捷切换面板
    showBoardName?: boolean;                // 展示看板名称
    hideStarButton?: boolean;               // 隐藏收藏按钮
    hideEditButton?: boolean;               // 隐藏编辑按钮
    hideTimePicker?: boolean;               // 隐藏时间控件
    hideShareButton?: boolean;              // 隐藏分享按钮
    hideFullScreenButton?: boolean;         // 隐藏全屏按钮

    /* 额外配置 */
    noTimeRefresh?: boolean;
    fullScreenTarget?: React.MutableRefObject<any>; // 全屏目标
    customFunctionItem?: React.ReactNode;           // 额外功能栏
}

/**
 * 看板工具栏
 */
const BoardToolBar: React.FC<BoardToolBarProps> = props => {
    const {board, fullScreenTarget, noTimeRefresh, customFunctionItem} = props;
    const {hideBoardChoose, showBoardName, hideStarButton, hideEditButton, hideShareButton, hideFullScreenButton, hideTimePicker} = props; // options
    const {dataAppStore, boardStore, urlParamStore} = StoreManager;

    const [isFullscreen, { toggleFull }] = useFullscreen(fullScreenTarget);
    const [isStar, setIsStar] = useState<boolean>(false); // 看板收藏状态

    useEffect(() => {
        setIsStar(board ? board.star : false);
    }, [board]);

    const appId = urlParamStore.getValue("appid")
        ? urlParamStore.getValue("appid")
        : urlParamStore.getValue("appId");

    const {chart} = boardStore; // 当前放大图表
    const links: any[] = get(board, "config.links", []);

    if (!board) {
        return null;
    }

    const closeFullViewChart = () => {
        boardStore.setChart(null);
        StoreManager.urlParamStore.changeURLParams({}, [FULL_SCREEN_CHART]);
    };

    const handleDataAppMenuClick = (url: string, dashboard: any) => {
        dataAppStore.selectedBoard = dashboard;
        dataAppStore.current = url;
    };

    const handleStarBoard = () => {
        const id = board.id;

        if (isStar) {
            BoardService.deleteFavorite(id).then(() => {
                setIsStar(!isStar);
                StoreManager.userActionStore.loadUserAction().then();
            });
        } else {
            BoardService.createFavorite(id).then(() => {
                setIsStar(!isStar);
                StoreManager.userActionStore.loadUserAction().then();
            });
        }
    };

    const generateLinkUrl = link => {
        const search = new URLSearchParams(link.url.split("?")[1] || "");
        if (link.time_range) {
            const selectedTime = urlParamStore.getSelectedTime();
            search.set("from", selectedTime.fromString);
            search.set("to", selectedTime.toString);
        }
        if (link.variable_value) {
            const variates = get(board, "config.variates", null);
            if (variates) {
                variates.forEach(variate => {
                    const values = urlParamStore.getValues(variate.name);
                    values.forEach(value => {
                        search.append(variate.name, value);
                    });
                });
            }
        }
        return `${link.url.split("?")[0]}?${search.toString()}`;
    };

    const boardAppListMenu = (
        <Menu>
            {(dataAppStore.dashboards || []).map(item => {
                const url = `/app/${dataAppStore.dataAppId}/board/${item.id}`;
                return (
                    <Menu.Item key={url} onClick={() => handleDataAppMenuClick(url, item)}>
                        <Link to={url}><AppstoreOutlined /> {item.title}</Link>
                    </Menu.Item>
                );
            })}
        </Menu>
    );

    const linkListDropdown = <Menu mode="vertical">
        {links.map(link => {
            if (link.title) {
                const url = generateLinkUrl(link);
                return (
                    <Menu.Item key={url}>
                        <a target={link.target || "_blank"} href={url}>{link.title} </a>
                    </Menu.Item>
                );
            } else {
                return null;
            }
        }).filter(Boolean)}
    </Menu>;

    return <Card size="small"><Row justify="space-between">
        {/* 左侧功能栏 */}
        <Space style={{marginRight: 10}}>
            {/* 便捷切换面板（看板页面） */}
            {!hideBoardChoose && dataAppStore.dashboards.length === 0 && (
                <Tooltip title="便捷切换面板">
                    <ListBoardTree board={board}/>
                </Tooltip>
            )}

            {/* 看板名称 */}
            {showBoardName && board && (
                <h3 style={{marginBottom: 0, paddingLeft: 10}}>{board.title}</h3>
            )}

            {/* 切换看板（看板应用） */}
            {dataAppStore.dashboards.length > 0 && (
                <Dropdown overlay={boardAppListMenu} placement="bottomCenter">
                    <Button type="dashed">
                        {dataAppStore.selectedBoard && dataAppStore.selectedBoard.title} <DownOutlined />
                    </Button>
                </Dropdown>
            )}

            {/* 变量列表 */}
            {get(board, "config.variates", []).length > 0 && (
                <MultiVariateSelect variates={board.config.variates}/>
            )}

            {/* 额外功能列表 */}
            {customFunctionItem}
        </Space>

        {/* 右侧功能栏 */}
        <Space style={{marginLeft: "auto"}} align="center">
            {/* 返回面板按钮 */}
            {chart && <Button type="primary" onClick={closeFullViewChart}>返回面板</Button>}

            {/* 收藏看板 */}
            {!hideStarButton && (
                <Tooltip title={`${isStar ? "取消" : ""}收藏此面板`}>
                    <Button
                        onClick={handleStarBoard}
                        icon={isStar
                            ? <StarFilled style={{color: "#f7bb12"}}/>
                            : <StarOutlined style={{color: "#f7bb12"}}/>
                        }
                    />
                </Tooltip>
            )}

            {/* 链接 */}
            {links.length > 0 && (
                <Tooltip title="链接">
                    <Dropdown trigger={["hover"]} overlay={linkListDropdown}>
                        <Button icon={<LinkOutlined/>}/>
                    </Dropdown>
                </Tooltip>
            )}

            {/* 编辑看板 */}
            {!hideEditButton && (
                <Tooltip title="编辑看板">
                    <Button href={`/board/edit/${board.id}`} target="_blank" icon={<EditOutlined/>}/>
                    {/*<Link to={`/board/edit/${board.id}`} target="_blank"><Button/></Link>*/}
                </Tooltip>)
            }

            {/* 分享看板 */}
            {!hideShareButton && <ShareBoardBtn boardId={board.id}/>}

            {/* 全屏显示 */}
            {!hideFullScreenButton && (
                <Tooltip title="全屏显示" placement="topRight">
                    <Button
                        type={isFullscreen ? "primary" : "default"}
                        icon={isFullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined/>}
                        onClick={toggleFull}
                    >
                        {isFullscreen ? "退出全屏" : ""}
                    </Button>
                </Tooltip>
            )}

            {/* 时间控件 */}
            {!hideTimePicker && (
                <TimePicker noTimeRefresh={noTimeRefresh}/>
            )}
        </Space>
    </Row></Card>;
};

export default BoardToolBar;
