import Key from "./key";
import * as ReactDOM from "react-dom";

// 监听项
export interface ShortcutItem {
    keys: Key[];                    // 监听的按键
    target?: HTMLElement | null;    // 触发的元素（目前只支持元素被 Hover 后触发）
    allowDefault?: boolean;         // 是否阻止原生响应

    onMatch(matched: { keys: Key[], native: Event }): void; // 匹配到按键后的 handler
}

interface MousePosition {
    x: number;
    y: number;
}

class ShortcutManager {
    // private static instance = new ShortcutManager();

    private shortcuts: ShortcutItem[] = [];         // 需要监听的快捷键
    private keysPressed: Key[] = [];                // 当前已经 press 的 key
    private shortcutsMatched: ShortcutItem[] = [];  // Match 到的快捷键（组合）
    private currMousePos: MousePosition;             // 记录当前鼠标位置

    // 单例模式，将 constructor 设为私有属性，防止 new 调用
    constructor() {
        // 初始化监听
        this.init();
    }

    /**
     * 订阅监听快捷键
     * @param {ShortcutItem} data 订阅信息
     * @return {{unsubscribe(): void}} 移除订阅
     */
    subscribe(data: ShortcutItem) {
        const {shortcuts} = this;

        // 添加监听
        shortcuts.push(data);

        // 返回取消订阅
        return {
            unsubscribe() {
                const unsubscribeIndex = shortcuts.findIndex(
                    shortcut => shortcut === data,
                );
                shortcuts.splice(unsubscribeIndex, 1);
            },
        };
    }

    /**
     * 添加全局 KeyDown 监听
     */
    private init() {
        document.addEventListener("keydown", this.handleKeyDown.bind(this));
        document.addEventListener("mousemove", this.handleMouseMove.bind(this));
    }

    /**
     * 响应全局 KeyDown 事件，判断并响应 onMatch
     * @param {KeyboardEvent} event
     */
    private handleKeyDown(event: KeyboardEvent) {
        // 如果当前聚焦在可编辑元素上则返回
        const target = document.activeElement;
        if (
            target.tagName === "INPUT" ||
            target.tagName === "SELECT" ||
            target.tagName === "TEXTAREA" ||
            target.hasAttribute("contenteditable")
        ) {
            return;
        }

        const {key} = event;

        // 添加当前按键
        this.keysPressed.push(key as Key);
        // 判断匹配的快捷键，并更新到 shortcutsMatched
        this.updateMatchingShortcuts();

        // 判断 shortcutsMatched 中匹配到的信息
        switch (this.shortcutsMatched.length) {
            case 0:
                this.resetKeys();
                break;
            // 暂不考虑快捷键重叠（如 `v` 和 `vs`）
            // case 1:
            //     this.callMatchedShortcut(event);
            //     break;
            default:
                this.callMatchedOnShortcut(event);
        }
    }

    private handleMouseMove(event: MouseEvent) {
        this.currMousePos = {x: event.clientX, y: event.clientY};
    }

    /**
     * 根据当前按键（keysPressed）更新匹配内容（shortcutsMatched）
     */
    private updateMatchingShortcuts() {
        // 如果当前无匹配内容，从所有监听中去匹配，否则继续上一次匹配内容进行搜索
        const shortcuts = this.shortcutsMatched.length > 0 ? this.shortcutsMatched : this.shortcuts;

        // 遍历并取出 `当前按键` 与 `监听项中的快捷键` 匹配或部分匹配的监听项
        this.shortcutsMatched = shortcuts.filter(
            ({keys, target}) => {
                // 判断是否和当前监听项中的快捷键匹配或部分匹配
                const partiallyMatching = arraysMatch(
                    this.keysPressed,
                    keys.slice(0, this.keysPressed.length),
                );

                // 如果存在 target，判断是否需要激活快捷键
                if (target) {
                    const isActive = isMouseOnTarget(target, this.currMousePos);
                    return partiallyMatching && isActive;
                }

                return partiallyMatching;
            }
        );
    }

    /**
     * 调用对应监听项的 onMatch 方法
     * @param {Event} event 原件 KeyBoard 事件
     */
    private callMatchedOnShortcut(event: Event) {
        // 寻找第一个匹配快捷键的监听项
        const matchingShortcuts = this.shortcutsMatched.filter(
            ({keys}) =>
                arraysMatch(keys, this.keysPressed)
        );

        if (!matchingShortcuts) {
            return;
        }

        // 遍历所有匹配到的监听项
        matchingShortcuts.forEach((shortcut: ShortcutItem) => {
            // 是否阻止原生响应（默认阻止）
            if (!shortcut.allowDefault) {
                event.preventDefault();
            }

            // 调用 onMatch
            shortcut.onMatch({
                keys: shortcut.keys,
                native: event,
            });
        });

        // 重置当前状态，进行新的一轮监听
        this.resetKeys();
    }

    private resetKeys() {
        this.keysPressed = [];
        this.shortcutsMatched = [];
    }
}

/**
 * 判断数组是否相等
 * @param {T[]} first 数组一
 * @param {T[]} second 数组二
 * @return {boolean} 是否相等
 */
function arraysMatch<T>(first: T[], second: T[]) {
    if (first.length !== second.length) {
        return false;
    }

    return first.every((value, index) => second[index] === value);
}

/**
 * 判断鼠标是否在元素上
 * @param {HTMLElement} target 目标元素
 * @param {MousePosition} pos 当前鼠标位置
 * @return {boolean} 是否在元素上
 */
function isMouseOnTarget(target: HTMLElement, pos: MousePosition) {
    const dom = getRealDom(target);
    if (!dom || !pos) {
        return false;
    }
    const {left, right, top, bottom} = dom.getBoundingClientRect();

    return pos.x >= left && pos.x <= right && pos.y >= top && pos.y <= bottom;
}

/**
 * 根据 Ref 获取真实 DOM 节点
 * @param {HTMLElement} target 目标 Ref
 * @return {HTMLElement} 真实 DOM 节点
 */
function getRealDom(target: HTMLElement): HTMLElement {
    return (ReactDOM.findDOMNode(target) as HTMLElement);
}

export default new ShortcutManager();
