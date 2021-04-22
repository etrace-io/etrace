import Moment from "react-moment";
import {Button, Input} from "antd";
import React from "react";
import {SearchOutlined} from "@ant-design/icons";
import IconFont from "$components/Base/IconFont";
import {TooltipSort, TooltipSortBy, TooltipSortMethod} from "$models/ChartModel";

const OPPOSITE_SORT = {
    [TooltipSort.DECREASING]: TooltipSort.INCREASING,
    [TooltipSort.INCREASING]: TooltipSort.DECREASING,
};

const ToolTipToolBar: React.FC<{
    search?: string;
    time?: string | number;
    sort?: TooltipSortMethod;
    searchVisible?: boolean;
    onSearch?(value: string): void;
    onSearchVisibleChange?(visible: boolean): void;
    onSort?(type: TooltipSortBy, order: TooltipSort): void;
}> = props => {
    const {sort, onSearch, onSort, time, searchVisible, onSearchVisibleChange, search} = props;

    const handleShowSearchVisibleChange = () => {
        onSearchVisibleChange && onSearchVisibleChange(!searchVisible);
    };

    const handleNameSortClick = () => {
        onSort && onSort(
            TooltipSortBy.NAME,
            sort.by === TooltipSortBy.NAME ? OPPOSITE_SORT[sort.order] : TooltipSort.DECREASING,
        );
    };

    const handleValueSortClick = () => {
        onSort && onSort(
            TooltipSortBy.VALUE,
            sort.by === TooltipSortBy.VALUE ? OPPOSITE_SORT[sort.order] : TooltipSort.DECREASING,
        );
    };

    const alphaSortBtn = sort.by === TooltipSortBy.NAME && sort.order === TooltipSort.DECREASING
        ? "icon-sort-alpha-desc"
        : "icon-sort-alpha-asc";

    const valueSortBtn = sort.by === TooltipSortBy.VALUE && sort.order === TooltipSort.DECREASING
        ? "icon-sortnumericdesc"
        : "icon-sortnumericasc";

    return (
        <div className="tooltip-toolbar">
            <div className="toolbar-header">
                <Moment className="tooltip-curr-time" format="YYYY-MM-DD HH:mm:ss">{time}</Moment>
                <Button.Group className="tooltip-btn-group" style={{paddingLeft: 10}} size="small">
                    {/* 搜索框显示 */}
                    <Button
                        className="tooltip-toolbar-btn"
                        icon={<SearchOutlined />}
                        type={searchVisible ? "primary" : "default"}
                        onClick={handleShowSearchVisibleChange}
                    />

                    {/* 字典排序按钮 */}
                    <Button
                        className="tooltip-toolbar-btn"
                        icon={<IconFont type={alphaSortBtn}/>}
                        onClick={handleNameSortClick}
                        type={sort.by === TooltipSortBy.NAME ? "primary" : "default"}
                    />

                    {/* 值排序按钮 */}
                    <Button
                        className="tooltip-toolbar-btn"
                        icon={<IconFont type={valueSortBtn}/>}
                        onClick={handleValueSortClick}
                        type={sort.by === TooltipSortBy.VALUE ? "primary" : "default"}
                    />
                </Button.Group>
            </div>

            {searchVisible && (
                <Input
                    className="tooltip-toolbar__search-input"
                    size="small"
                    value={search}
                    onChange={e => onSearch && onSearch(e.target.value)}
                    placeholder="输入名称以过滤"
                />
            )}
        </div>
    );
};

export default React.memo(ToolTipToolBar);
