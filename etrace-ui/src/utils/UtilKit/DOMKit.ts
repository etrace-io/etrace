export default {
    setPosition,
    findScrollParent,
    isParentOf,
    isInViewPort,
};

/**
 * 设置目标元素 Pos
 * @param target
 * @param position
 */
function setPosition (
    target: HTMLElement,
    position: {
        top?: number | string,
        right?: number | string,
        bottom?: number | string,
        left?: number | string
    },
) {
    if (!target) { return; }

    const {top, right, bottom, left} = position;
    const topValue =
        top === undefined
            ? "auto"
            : typeof top === "number"
            ? top + "px"
            : top;

    const rightValue =
        right === undefined
            ? "auto"
            : typeof right === "number"
            ? right + "px"
            : right;

    const bottomValue =
        bottom === undefined
            ? "auto"
            : typeof bottom === "number"
            ? bottom + "px"
            : bottom;

    const leftValue =
        left === undefined
            ? "auto"
            : typeof left === "number"
            ? left + "px"
            : left;

    target.style.top = topValue;
    target.style.right = rightValue;
    target.style.bottom = bottomValue;
    target.style.left = leftValue;
}

/**
 * 不断向上递归寻找父级可滚动元素
 * @param node 当前节点
 */
function findScrollParent (node: HTMLElement) {
    if (!(node instanceof HTMLElement)) {
        return document.documentElement;
    }

    const excludeStaticParent = node.style.position === "absolute";
    const overflowRegex = /(scroll|auto)/;
    let parent = node;

    while (parent) {
        if (!parent.parentNode) {
            return node.ownerDocument || document.documentElement;
        }

        const style = window.getComputedStyle(parent);
        const position = style.position;
        const overflow = style.overflow;
        const overflowX = style["overflow-x"];
        const overflowY = style["overflow-y"];

        if (position === "static" && excludeStaticParent) {
            parent = parent.parentNode as HTMLElement;
            continue;
        }

        if (overflowRegex.test(overflow) && overflowRegex.test(overflowX) && overflowRegex.test(overflowY)) {
            return parent;
        }

        parent = parent.parentNode as HTMLElement;
    }

    return node.ownerDocument || document.documentElement;
}

/**
 * 判断 parent 是否为 child 的父节点
 */
function isParentOf(child: HTMLElement | Node, parent: HTMLElement | Node) {
    while (child && child.parentElement !== parent) {
        child = child.parentElement;
    }
    return child !== null;
}

/**
 * 判断元素是否在可视范围之内
 */
function isInViewPort(el: HTMLElement): boolean {
    if (!el) {
        return false;
    }

    const viewPortWidth =
        window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
    const viewPortHeight =
        window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
    const rect = el.getBoundingClientRect();

    if (rect) {
        const { top, bottom, left, right } = rect;
        return bottom > 0 && top <= viewPortHeight && left <= viewPortWidth && right > 0;
    }

    return false;
}
