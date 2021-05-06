import React from "react";
import {Popover} from "antd";
import {TooltipPlacement} from "antd/lib/tooltip";

interface ActionBtnProps {
    text?: any;
    onClick?: () => void;
    popoverContent?: any;
    popoverPlacement?: TooltipPlacement;
    className?: string;
    style?: React.CSSProperties;
    icon?: React.ReactNode;
}
const ActionBtn: React.FC<ActionBtnProps> = (props: ActionBtnProps) => {
    const {icon} = props; // Icon Props
    const {style, onClick, text, popoverContent, popoverPlacement} = props;

    let btn = (
        <span onClick={onClick} style={style} className="list-item__action-btn">
            {icon} {text !== undefined && <span className="list-item__text">{text}</span>}
        </span>
    );

    if (popoverContent) {
        btn = <Popover content={popoverContent} placement={popoverPlacement}>{btn}</Popover>;
    }

    return btn;
};

export default ActionBtn;