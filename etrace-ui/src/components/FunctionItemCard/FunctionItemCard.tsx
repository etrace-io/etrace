import React from "react";
import classNames from "classnames";
import {UserKit} from "$utils/Util";
import {MenuItem} from "$models/Menu";
import {Link} from "react-router-dom";
import {Card, Dropdown, Menu} from "antd";

import "./FunctionItemCard.less";
import useUser from "$hooks/useUser";
import IconFont from "$components/Base/IconFont";

interface Content extends MenuItem {
    children?: Content[];
    onClick?: (item: Content) => void;
}

interface FunctionItemCardProps {
    title?: string;
    column?: number;
    className?: string;
    notFoundContent?: string;
    dataSource?: Content[];
    extra?: React.ReactNode;
}

const FunctionItemCard: React.FC<FunctionItemCardProps> = props => {
    const { dataSource, column, className, title, extra, notFoundContent } = props;
    const user = useUser();

    const style = { width: column && (100 / column) + "%" };
    const isAdmin = UserKit.isAdmin(user);

    const listClassString = classNames("function-item-card__list", className);

    const renderItemWithType = (item: Content) => {
        if (!item.icon && !item.url) {
            return null;
        }

        const linkIcon = <IconFont type="icon-caozuo-wailian" style={{fontSize: 12, opacity: 0.6, marginLeft: 5}}/>;

        if (item.children) {
            const dropdownMenu = <Menu>
                {item.children.map(child => <Menu.Item key={child.label}>
                    {child.isExternal
                        ? <a href={child.url} target="_blank" rel="noopener noreferrer">{child.icon && child.icon} {child.label} {linkIcon}</a>
                        : <Link to={child.url}>{child.icon && child.icon} {child.label}</Link>
                    }
                </Menu.Item>)}
            </Menu>;

            return <Dropdown overlay={dropdownMenu} arrow={true} placement="bottomCenter">
                <div className="icon-item" style={{ cursor: "pointer", marginBottom: 6 }}>
                    <div className="icon-item__icon" style={{ lineHeight: 1.5 }}>{item.icon}</div>
                    <p className="icon-item__text" style={{ lineHeight: 1.5 }}>{item.label}</p>
                </div>
            </Dropdown>;
        }

        const labelIdentity = item.isExternal ? linkIcon : null;

        const content = item.icon
            ? <>
                <div className="icon-item__icon">{item.icon}</div>
                <p className="icon-item__text">{item.label}{labelIdentity}</p>
            </>
            : <>{item.label}{labelIdentity}</>;

        if (item.isExternal) {
            return item.icon
                ? <a href={item.url} target="_blank" rel="noopener noreferrer" className="icon-item">{content}</a>
                : <a href={item.url} target="_blank" rel="noopener noreferrer" className="text-item" onClick={() => item.onClick && item.onClick(item)}>{content}</a>;
        }

        return item.icon
            ? <Link to={item.url}  className="icon-item">{content}</Link>
            : <Link to={item.url} className="text-item" onClick={() => item.onClick && item.onClick(item)}>{content}</Link>;
    };

    return (
        <div className="function-item-card">
            <Card title={title} extra={extra} size="small">
                <div className={listClassString}>
                    {dataSource && dataSource.map(item => (!item.isAdmin || (item.isAdmin && isAdmin)) && (
                        <div className="function-item-card__item" style={style} key={item.label}>
                            {renderItemWithType(item)}
                        </div>
                    )).filter(i => i)}

                    {(!dataSource || dataSource.length === 0) && (
                        <div className="list__no-content">
                            {notFoundContent || "暂无内容"}
                        </div>
                    )}
                </div>
            </Card>
        </div>
    );
};

export default FunctionItemCard;
