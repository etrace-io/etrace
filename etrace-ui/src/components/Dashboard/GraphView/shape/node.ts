// NodeGroup
import createAnchor from "../item/anchor";
import {getGraphStyle} from "../defaultStyle";
import {getColor} from "../../../../utils/ChartDataConvert";
import {ModelConfig} from "@antv/g6/lib/types";
import {ToolKit} from "$utils/Util";

const CONTENT_TEXT_CONFIG = {
    spacing: 25,
    titleMargin: 8,
    valueTitleHeight: 15,
    badgeMargin: 8,
    // contentMargin: 6,
    badgePadding: [5, 5, 3, 5],
    nodePadding: [15, 15, 15, 15],
    contentColumnSpacing: 10,
    groupIndicatorPosX: -20,
    groupIndicatorExtend: 5,
    groupResultPadding: [20, 0, 0, 0],
    groupFieldPadding: [0, 0, 0, 0],
    valueTitlePadding: [5, 0, 5, 0],
    metricInfoPadding: [0, 0, 0, 0],
};

// Color
const dashArray = [
    [0, 1],
    [0, 2],
    [1, 2],
    [0, 1, 1, 2],
    [0, 2, 1, 2],
    [1, 2, 1, 2],
    [2, 2, 1, 2],
    [3, 2, 1, 2],
    [4, 2, 1, 2],
];
const interval = 9;
const lineDash = [4, 2, 1, 2];

export default function (G6: any) {
    const graphStyle = getGraphStyle();
    G6.registerNode("DashboardNode", {
        options: {
            icon: null,
            iconStyle: {
                width: 14,
                height: 14,
                left: 0,
                top: 0,
            },
            style: {
                fill: "#f9f9f9",
                stroke: "#bbb",
                cursor: "default",
            },
            stateStyles: {
                selected: {
                    fill: "#eee",
                },
                hover: {
                    cursor: graphStyle.cursor.hoverNode,
                },
            },
        },
        drawAnchor(group: any) {
            const bbox = group.get("backShape").getBBox();
            this.getAnchorPoints().forEach((p, i) => {
                const style = {
                    x: bbox.minX + bbox.width * p[0],
                    y: bbox.minY + bbox.height * p[1],
                };
                const anchor = createAnchor(i, style, group);
                group.anchorShapes.push(anchor);
                group.getAllAnchors = () => {
                    return group.anchorShapes.map(c => {
                        c.filter(a => a.isAnchor);
                    });
                };
                group.getAnchor = (idx: any) => {
                    return group.anchorShapes.filter(a => a.get("index") === idx);
                };
            });
        },
        _getBadgeData(cfg: ModelConfig) {
            if (!cfg.status) {
                return [];
            }

            const {status} = cfg;
            const result = [];
            const {alert, change, publish} = status as any;

            alert && result.push({fill: "#da003d", text: alert > 99 ? "99+" : alert});
            change && result.push({fill: "#00ab69", text: change > 99 ? "99+" : change});
            publish && result.push({fill: "#ffc857", text: publish > 99 ? "99+" : publish});

            return result;
        },
        _drawTitle(title: string, group: any) {
            const options = group.get("_options");
            const {startX, startY} = options || {startX: 0, startY: 0}; // 获取起始位置
            const {x: initX, y: initY} = graphStyle.nodeTitleStyle;
            const attrs = Object.assign({}, graphStyle.nodeTitleStyle, {
                x: initX + startX,
                y: initY + startY,
                text: title,
                // fill: getColor(0),
            });
            group.addShape("text", {attrs});
        },
        _drawBadge(badges: any[], group: any) {
            const options = group.get("_options") || {};
            const {startY} = options;
            let posX = 0;
            badges.forEach(({fill, text}, index) => {
                const badgesGroup = group.addGroup();

                badgesGroup.addShape("text", {
                    attrs: {
                        ...graphStyle.nodeBadgeTextStyle,
                        x: posX,
                        y: startY,
                        text
                    },
                }).toFront();

                badgesGroup.renderBack(CONTENT_TEXT_CONFIG.badgePadding, {
                    radius: [4],
                    fill,
                });

                posX += badgesGroup.getBBox().width + CONTENT_TEXT_CONFIG.badgeMargin;
            });
        },
        _drawGroupField(field: string, group: any) {
            group.addShape("text", {
                attrs: {
                    ...graphStyle.nodeGroupFieldStyle,
                    text: field,
                }
            });
        },
        _drawTableHeader(keys: string[], group: any) {
            const width = [];
            const name = group.addShape("text", {
                attrs: {
                    ...graphStyle.nodeContentValueTitleStyle,
                    text: "Name",
                    textAlign: "left"
                }
            });
            width.push(name.getBBox().width);
            (["Trend", ...keys]).forEach(key => {
                const value = group.addShape("text", {
                    attrs: {
                        ...graphStyle.nodeContentValueTitleStyle,
                        text: ToolKit.firstUpperCase(key),
                    }
                });
                width.push(value.getBBox().width);
            });
            return width;
        },
        _drawMetricInfo(content: any, keys: string[], group: any) {
            const width = [];
            // 指标名
            const metricName = group.addShape("text", {
                attrs: {text: content.name, ...graphStyle.nodeContentMetricNameStyle},
            });
            width.push(metricName.getBBox().width);

            // 趋势图片
            if (content.chart) {
                const chartImage = group.addShape("image", {
                    attrs: {
                        ...graphStyle.nodeContentChartStyle,
                        // img: content.chart,
                    }
                });
                content.chart.then(img => {
                    try {
                        chartImage.attr("img", img);
                    } catch (e) {
                        // ...
                    }
                });
                width.push(chartImage.getBBox().width);
                chartImage.attr("y", -chartImage.getBBox().height / 2);
            }

            // 对应数据
            keys.forEach(key => {
                const metricValue = group.addShape("text", {
                    attrs: {
                        ...graphStyle.nodeContentValueStyle,
                        text: content[key] === null ? "-" : `${content[key]}`
                    }
                });
                width.push(metricValue.getBBox().width);
            });

            // 返回该行宽度信息
            return width;
        },
        _drawGroupResultIndicator(group: any) {
            // 添加 Indicator
            group.addShape("rect", {
                attrs: {
                    ...graphStyle.nodeGroupIndicatorStyle,
                    x: 0,
                    y: group.getBBox().y - CONTENT_TEXT_CONFIG.groupIndicatorExtend,
                    height: group.getBBox().height + CONTENT_TEXT_CONFIG.groupIndicatorExtend * 2,
                    width: 6,
                }
            });
        },
        _drawContent(contents: any[], group: any) {
            // const valuesKeys = ["total", "current"]; // 需要展示的 Key
            const rowItemWidth = []; // 存储每行各 Item 的宽度信息

            const options = group.get("_options");
            const {startX, startY} = options || {startX: 0, startY: 0}; // 获取起始位置
            let {maxWidth} = options || {maxWidth: 0}; // 获取起始内容最大宽度

            // 开始绘制，遍历每个 Group
            contents.forEach(resultGroup => {
                const groupResultGroup = group.addGroup({_type: "groupResult", _field: resultGroup.group});
                const valuesKeys = resultGroup.valuesKeys; // 需要展示的 Key
                // 绘制 Group Field
                if (resultGroup.group) {
                    groupResultGroup.set("groupField", resultGroup.group);
                    const groupFieldGroup = groupResultGroup.addGroup({
                        _type: "groupField",
                        _padding: CONTENT_TEXT_CONFIG.groupFieldPadding,
                        // _attr: {fill: "gold"}
                    });
                    this._drawGroupField(resultGroup.group, groupFieldGroup);
                    maxWidth = Math.max(groupFieldGroup.getBBox().width, maxWidth);
                }

                // 绘制表头
                const tableHeaderGroup = groupResultGroup.addGroup({
                    _type: "tableHeader",
                    _padding: CONTENT_TEXT_CONFIG.valueTitlePadding
                });
                const headerWidthInfo = this._drawTableHeader(valuesKeys, tableHeaderGroup);
                rowItemWidth.push(headerWidthInfo);
                maxWidth = Math.max(tableHeaderGroup.getBBox().width, maxWidth);

                // 针对每个指标新建一个 Group
                resultGroup.metric.forEach(content => {
                    const metricGroup = groupResultGroup.addGroup({
                        _type: "metricInfo",
                        _padding: CONTENT_TEXT_CONFIG.metricInfoPadding
                    });
                    // 绘制指标内容
                    const currRowWidthInfo = this._drawMetricInfo(content, valuesKeys, metricGroup);
                    rowItemWidth.push(currRowWidthInfo);
                    maxWidth = Math.max(metricGroup.getBBox().width, maxWidth);
                });
            });

            /* 开始定位 */
            // 1. 计算格列最大宽度
            let columnMaxWidth = rowItemWidth.reduce(
                (a, b) => [...new Array(b.length)].join().split(",").map((_, idx) => Math.max(a[idx] || 0, b[idx] || 0)),
                []
            );
            const metricTableSpacing = CONTENT_TEXT_CONFIG.contentColumnSpacing * (Math.max(columnMaxWidth.length - 1, 0));
            const metricTableWidth = columnMaxWidth.reduce((a, b) => a + b, 0);
            if (maxWidth > (metricTableWidth + metricTableSpacing)) {
                columnMaxWidth = columnMaxWidth.map(width =>
                    width / metricTableWidth * (maxWidth - metricTableSpacing)
                );
            }

            // 2. 遍历 Group，进行子元素定位
            for (let groupResult, i = 0, posX = startX, posY = startY; groupResult = group.getChildByIndex(i); i++) {
                const needIndicator = !!groupResult.get("groupField"); // 是否分组
                posY += needIndicator ? CONTENT_TEXT_CONFIG.groupResultPadding[0] : 0;
                // 3. 遍历 Group 内每行内容
                for (let row, j = 0; row = groupResult.getChildByIndex(j); j++) {
                    // 4. 对每行内容进行定位
                    posX = needIndicator ? startX + -CONTENT_TEXT_CONFIG.groupIndicatorPosX : startX;
                    const currType = row.get("_type");
                    const padding = row.get("_padding") || [0, 0, 0, 0];
                    const attr = row.get("_attr") || {};

                    if (currType === "tableHeader" || currType === "metricInfo") {
                        columnMaxWidth.forEach((width, idx) => {
                            const col = row.getChildByIndex(idx);
                            const align = col.attr("textAlign");
                            if (align === "center") {
                                col.translate(width / 2, 0);
                            }
                            const initX = col.attr("x") || 0;
                            const initY = col.attr("y") || 0;
                            col.attr({x: initX + posX, y: initY + posY});
                            posX += width;
                            posX += CONTENT_TEXT_CONFIG.contentColumnSpacing; // 列间距
                        });
                    } else {
                        // 不需要 Column
                        row.translate(posX, posY);
                    }

                    if (row.get("zIndex") !== -1) {
                        row.renderBack(padding, attr);
                        row.translate(0, padding[0]);
                    }

                    posY += row.getBBox().height + 6;
                    posY += CONTENT_TEXT_CONFIG.groupResultPadding[2];
                }

                needIndicator && this._drawGroupResultIndicator(groupResult);

                groupResult.renderBack(CONTENT_TEXT_CONFIG.groupResultPadding, {});
            }
        },
        draw(cfg: ModelConfig, group: any) {
            const badges = this._getBadgeData(cfg);
            // const hasBadge = badges.length > 0;

            // Title 绘制
            const titleGroup = group.addGroup({_options: {startX: 0, startY: 0}}); // 给定起始位置
            this._drawTitle(cfg.title, titleGroup);

            // 绘制事件角标
            const badgeGroup = group.addGroup({
                _type: "badges",
                _options: {
                    startY: titleGroup.getBBox().height,
                }
            });
            this._drawBadge(badges, badgeGroup);

            const contentGroup = group.addGroup({
                _options: {
                    startX: 0,
                    startY: titleGroup.getBBox().height + badgeGroup.getBBox().height,
                    maxWidth: titleGroup.getBBox().width,
                }
            });

            const contents = cfg.contents as string[];
            if (contents && contents.length > 0) {
                this._drawContent(cfg.contents, contentGroup);
            }

            const nodeWidth = Math.max(titleGroup.getBBox().width, contentGroup.getBBox().width);
            contentGroup.translate((nodeWidth - contentGroup.getBBox().width) / 2, 0);

            // 居中
            titleGroup.translate(nodeWidth / 2, 0);
            badgeGroup.translate((nodeWidth - badgeGroup.getBBox().width) / 2 + CONTENT_TEXT_CONFIG.badgePadding[3], 10);

            group.anchorShapes = [];
            group.showAnchor = (g) => {
                this.drawAnchor(g);
            };
            group.clearAnchor = (g) => {
                g.anchorShapes && g.anchorShapes.forEach(a => a.remove());
                g.anchorShapes = [];
            };
            group.clearHotpotActived = (g) => {
                g.anchorShapes && g.anchorShapes.forEach(a => {
                    a.isAnchor && a.setHotspotActived(false);
                });
            };

            group.set("backShape", undefined);

            const keyShape = group.renderBack(CONTENT_TEXT_CONFIG.nodePadding, {
                ...graphStyle.nodeContainerStyle,
                // fill: this.options.style.fill,
                stroke: Number.isInteger(+cfg.class) ? getColor(+cfg.class) || "#909399" : "#909399",
            });

            cfg.width = keyShape.getBBox().width;
            cfg.height = keyShape.getBBox().height;

            // 底部卡片样式
            return keyShape;
        },
        runAnimate(cfg: ModelConfig, group: any) {
            if (cfg.active) {
                let totalArray = [];
                let index = 0;
                const shape = group.getFirst();
                const animation = {
                    onFrame(ratio: any) {
                        for (let i = 0; i < 9; i += interval) {
                            totalArray = totalArray.concat(lineDash);
                        }
                        const config = {
                            lineDash: dashArray[index].concat(totalArray),
                        };
                        index = (index + 1) % interval;
                        return config;
                    },
                    repeat: true,
                };
                shape.animate(animation, 5000);
            }
        },
        /**
         * 绘制后的附加操作，默认没有任何操作
         * @param  {Object} cfg 节点的配置项
         * @param  {G.Group} group 节点的容器
         */
        afterDraw(cfg: ModelConfig, group: any) {
            this.runAnimate(cfg, group);
        },
        /**
         * 设置节点的状态，主要是交互状态，业务状态请在 draw 方法中实现
         * 单图形的节点仅考虑 selected、active 状态，有其他状态需求的用户自己复写这个方法
         * @param  {String} name 状态名称
         * @param  {Object} value 状态值
         * @param  {Node} node 节点
         */
        setState(name: string, value: any, node: any) {
            const group = node.getContainer();
            if (name === "show-anchor") {
                if (value) {
                    group.showAnchor(group);
                } else {
                    group.clearAnchor(group);
                }
            } else if (name === "selected") {
                const rect = group.getChildByIndex(0);
                if (value) {
                    rect.attr("fill", graphStyle.nodeContainerSelectStyle.fill);
                } else {
                    rect.attr("fill", graphStyle.nodeContainerStyle.fill);
                }
            } else if (name === "hover") {
                const rect = group.getChildByIndex(0);
                const textGroup = group.getChildByIndex(1);

                const cursor = value ? this.options.stateStyles.hover.cursor : this.options.style.cursor;

                if (textGroup.isGroup) {
                    let child = 0;
                    let currChild;
                    while (currChild = textGroup.getChildByIndex(child++)) {
                        currChild.attr("cursor", cursor);
                    }
                }

                rect.attr("cursor", cursor);
            }
        },
        /**
         * 获取控制点
         * @param  {Object} cfg 节点、边的配置项
         * @return {Array|null} 控制点的数组,如果为 null，则没有控制点
         */
        getAnchorPoints(cfg: ModelConfig) {
            return [
                [0.5, 0], // top
                [1, 0.5], // right
                [0.5, 1], // bottom
                [0, 0.5], // left
            ];
        },
        drawStatusDot(cfg: ModelConfig) {
            if (!cfg.status) {
                return [];
            }

            const {status} = cfg;
            const result = [];
            const {alert, change, publish} = status as any;

            alert && result.push({fill: "#da003d", text: alert > 99 ? "99+" : alert});
            change && result.push({fill: "#00ab69", text: change > 99 ? "99+" : change});
            publish && result.push({fill: "#ffc857", text: publish > 99 ? "99+" : publish});
            // result.push({fill: "#da003d", text: alert > 99 ? "99+" : alert});
            // result.push({fill: "#00ab69", text: change > 99 ? "99+" : change});
            // result.push({fill: "#ffc857", text: publish > 99 ? "99+" : publish});

            return result;
        },
    }, "single-shape");
}
