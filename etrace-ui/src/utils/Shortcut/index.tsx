import {Component} from "react";
import ShortcutManager, {ShortcutItem} from "./ShortcutManager";

interface ShortcutProps extends ShortcutItem {
}

interface ShortcutState {
}

export interface Subscription {
    unsubscribe(): void;
}

export default class Shortcut extends Component<ShortcutProps, ShortcutState> {
    public item: ShortcutItem = {
        target: this.props.target,
        keys: this.props.keys,
        allowDefault: this.props.allowDefault || false,
        onMatch: this.props.onMatch,
    };

    private subscription: Subscription;

    componentDidMount() {
        // 订阅当前快捷键
        this.subscription = ShortcutManager.subscribe(this.item);
    }

    componentWillReceiveProps(nextProps: Readonly<ShortcutProps>, nextContext: any): void {
        if (nextProps.target) {
            this.item.target = nextProps.target;
        }
    }

    componentWillUnmount() {
        if (this.subscription !== null) {
            this.subscription.unsubscribe();
        }
    }

    render() {
        return null;
    }
}