import React from "react";
import {Link} from "react-router-dom";
import {Card, Popconfirm, Popover, Tooltip} from "antd";
import {
    DeleteOutlined,
    EditOutlined,
    EyeOutlined,
    RollbackOutlined,
    StarFilled,
    StarOutlined,
} from "@ant-design/icons/lib";

import "./MetaCard.less";

interface MetaCardProps {
    id: number;
    title: React.ReactNode;
    link?: string | ((id: number) => string);
    onClick?: (id: number) => void;
    description?: string;
    department?: string[];
    actions?: React.ReactNode[];
    isDeleted?: boolean;
    deleteTitle?: React.ReactNode;
    recoverTitle?: React.ReactNode;
    onDelete?: (id: number, isDelete: boolean) => void;
    isFavorite?: boolean;
    fans?: React.ReactText;
    onFavorite?: (id: number) => void;
    viewer?: React.ReactText;
    onEdit?: (id: number) => void;
    editLink?: string | ((id: number) => string);
    editInfo?: string[];
    extraActions?: ExtraAction[];
}

interface ExtraAction extends IconWithTextProps {
    popover?: React.ReactNode;
    link?: string | ((id: number) => string);
}

const MetaCard: React.FC<MetaCardProps> = props => {
    const {id, title, onClick, link, description, department, extraActions = []} = props;
    const {isDeleted, deleteTitle, recoverTitle, onDelete} = props;
    const {isFavorite, fans, onFavorite} = props;
    const {viewer} = props;
    const {editInfo, editLink, onEdit} = props;

    // 删除操作
    const deleteAction = <Popconfirm
        key="delete"
        onConfirm={() => onDelete && onDelete(id, !isDeleted)}
        okText="确定"
        cancelText="取消"
        placement="topLeft"
        arrowPointAtCenter={true}
        title={isDeleted ? (recoverTitle || "确定恢复吗") : (deleteTitle || "确定删除吗？")}
    >
        <IconWithText icon={isDeleted ? <RollbackOutlined /> : <DeleteOutlined />}/>
    </Popconfirm>;

    // 收藏
    const favoriteAction = <IconWithText
        key="favorite"
        icon={isFavorite ? <StarFilled /> : <StarOutlined />}
        text={fans}
        onClick={() => onFavorite && onFavorite(id)}
    />;

    // 查看
    const viewAction = <IconWithText key="view" icon={<EyeOutlined />} text={viewer} />;

    // 编辑
    const editItem = editLink
        ? <Link to={typeof editLink === "function" ? editLink(id) : editLink}>
            <IconWithText icon={<EditOutlined />}/>
        </Link>
        : <IconWithText onClick={() => onEdit(id)} icon={<EditOutlined />}/>;

    const editAction = editInfo
        ? <Popover
            placement="top"
            arrowPointAtCenter={true}
            content={editInfo.map(i => <div key={i}>{i}</div>)}
        >{editItem}
        </Popover>
        : editItem;

    const actions = [
        onDelete && deleteAction,
        onFavorite && favoriteAction,
        viewer !== undefined && viewAction,
        (editLink || onEdit) && editAction,
    ].filter(Boolean).concat(extraActions.map((action, idx) => {
        const {icon, text, popover} = action;
        const content = <IconWithText icon={icon} text={text} onClick={action.onClick}/>;
        const container = action.link
            ? <Link to={typeof action.link === "function" ? action.link(id) : action.link}>{content}</Link>
            : content;

        return popover
            ? <Popover placement="top" content={popover}><div>{container}</div></Popover>
            : container;
    }));

    const clickableTitle = link
            ? <Link to={typeof link === "function" ? link(id) : link}>{title}</Link>
            : <a rel="noopener noreferrer" onClick={() => onClick(id)}>{title}</a>;

    const cardTitle = typeof title === "string"
        ? <Tooltip
            placement="topLeft"
            color="blue"
            title={(description || "").trim() ? description.trim() : title}
        >{clickableTitle}
        </Tooltip>
        : clickableTitle;

    return (
        <Card actions={actions} className="emonitor-meta-card">
            <Card.Meta
                className="emonitor-meta-card__meta"
                title={cardTitle}
                description={(department || []).join(" / ")}
            />
        </Card>
    );
};

interface IconWithTextProps {
    text?: React.ReactText;
    icon?: React.ReactNode;
    onClick?: () => void;
}

const IconWithText: React.FC<IconWithTextProps> = ({text, icon, onClick}) => (
    <div onClick={() => onClick && onClick()} className="meta-card-action">
        {icon}{text !== undefined && <span className="action__text">{text}</span>}
    </div>
);

export default MetaCard;