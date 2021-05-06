import React, {ReactNode} from "react";
import EXCEPTION_CONFIG from "./typeConfig";
import "./Exception.css";

interface ExceptionProps {
    style?: React.CSSProperties;
    img?: string;
    type?: string;
    title?: string;
    desc?: string;
    actions?: ReactNode;
}

interface ExceptionState {

}

export default class Exception extends React.Component<ExceptionProps, ExceptionState> {

    render() {
        const {img, type, style, title, desc} = this.props;
        const pageType = type ? type : "404";
        return (
            <div className="exception" style={style}>
                <div className="imgBlock">
                    <div
                        className="imgEle"
                        style={{backgroundImage: `url(${img || (EXCEPTION_CONFIG[pageType] && EXCEPTION_CONFIG[pageType].img) || EXCEPTION_CONFIG["404"].img})`}}
                    />
                </div>
                <div className="content">
                    <h1>{title || (EXCEPTION_CONFIG[pageType] && EXCEPTION_CONFIG[pageType].title) || pageType}</h1>
                    <div className="desc">{desc || EXCEPTION_CONFIG[pageType].desc}</div>
                    {
                        this.props.actions && (this.props.actions)
                    }
                </div>
            </div>
        );
    }
}
