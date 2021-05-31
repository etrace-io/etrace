import React from "react";
import {Button} from "antd";
import {autobind} from "core-decorators";
import {ButtonProps} from "antd/lib/button/button";
import "./ConfirmButton.css";

const classNames = require("classnames");

type ConfirmButtonProps = ButtonProps & { confirmTime?: number };

// interface ConfirmButtonProps extends ButtonProps {
//     confirmTime?: number; // 默认 1500ms
// }

interface ConfirmButtonState {
    loading?: boolean;
}

/**
 * 使用方法如下：
 * <ConfirmButton confirmTime={3000} onClick={this.handleClick}>喵喵喵</ConfirmButton>
 * 可以使用 Button 上所有的 props
 */
export class ConfirmButton extends React.Component<ConfirmButtonProps, ConfirmButtonState> {
    private timer: any;

    constructor(props: ConfirmButtonProps) {
        super(props);
        this.state = {
            loading: false
        };
    }

    @autobind
    handleClick(e: any) {
        if (e.button !== 0) { // 只响应左键
            return;
        }
        e.persist(); // 从事件池中取出合成的事件，并允许该事件的引用
        this.setState({
            loading: true
        });
        const _timeout = this.props.confirmTime || 1500;
        const _func = this.props.onClick;
        this.timer = setTimeout(
            () => {
                if (_func) {
                    _func.call(null, e);
                }
                this.setState({
                    loading: false
                });
            },
            _timeout
        );
    }

    @autobind
    handleMouseUp() {
        this.setState({
            loading: false
        });
        clearTimeout(this.timer);
    }

    render() {
        const props = {...this.props};
        // 不污染 Button 组件
        delete props.confirmTime;
        delete props.onClick;

        const elCls = classNames(props.className, {
            "btn-confirm-mask": true,
            "loading": this.state.loading || false
        });

        return (
            <Button
                {...props}
                onMouseDown={this.handleClick}
                onMouseUp={this.handleMouseUp}
                className="confirm-btn"
            >
                <div className={elCls} style={{transitionDuration: `${this.props.confirmTime || 1500}ms`}}/>
                {this.props.children}
                <span className="confirm-hover-tip">长按</span>
            </Button>
        );
    }
}
