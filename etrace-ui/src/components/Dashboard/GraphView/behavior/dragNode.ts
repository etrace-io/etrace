import {getGraphStyle} from "../defaultStyle";

export default function (G6: any) {
    const graphStyle = getGraphStyle();
    G6.registerBehavior("dragNode", {
        getDefaultCfg() {
            return {
                updateEdge: true,
                delegate: false,
                delegateStyle: {},
                align: true,
            };
        },
        getEvents() {
            return {
                "node:dragstart": "onDragStart",
                "node:drag": "onDrag",
                "node:dragend": "onDragEnd"
            };
        },
        onDragStart(e: any) {
            if (!this.shouldBegin.call(this, e)) {
                return;
            }
            this.target = e.item;
            this.origin = {
                x: e.x,
                y: e.y
            };
            const {x: startX, y: startY, width, height} = this.target.getBBox();
            this.startModel = {
                x: startX + width / 2,
                y: startY + height / 2
            };
        },
        onDrag(e: any) {
            if (!this.origin) {
                return;
            }
            if (!this.get("shouldUpdate").call(this, e)) {
                return;
            }
            const origin = this.origin;
            const model = this.target.get("model");
            if (!this.point) {
                this.point = {
                    x: model.x,
                    y: model.y
                };
            }
            const x = e.x - origin.x + this.point.x;
            const y = e.y - origin.y + this.point.y;
            this.origin = {x: e.x, y: e.y};
            this.point = {x, y};
            if (this.delegate) {
                this._updateDelegate(this.target, x, y);
            } else {
                this._updateItem(this.target, {x, y});
            }
        },
        onDragEnd(e: any) {
            if (!this.shouldEnd.call(this, e)) {
                return;
            }
            if (!this.origin) {
                return;
            }
            const delegateShape = e.item.get("delegateShape");
            const {x, y} = this.point;
            if (delegateShape) {
                delegateShape.remove();
                this.target.set("delegateShape", null);
                this._updateItem(this.target, {x, y});
            }

            if (this.graph.executeCommand) {
                this.graph.executeCommand("update", {
                    startModel: this.startModel,
                    itemId: this.target.get("id"),
                    updateModel: {x, y}
                });
            }
            this.point = null;
            this.origin = null;
            this.graph.emit("afternodedragend");
        },
        _updateItem(item: any, point: any) {
            if (this.get("updateEdge")) {
                this.graph.updateItem(item, point);
            } else {
                item.updatePosition(point);
                this.graph.paint();
            }
        },
        _updateDelegate(item: any, x: any, y: any) {
            const self = this;
            let shape = item.get("delegateShape");
            const bbox = item.get("keyShape").getBBox();
            if (!shape) {
                const parent = self.graph.get("group");
                const attrs = graphStyle.nodeDelegationStyle;
                // model上的x, y是相对于图形中心的，delegateShape是g实例，x,y是绝对坐标
                shape = parent.addShape("rect", {
                    attrs: {
                        width: bbox.width,
                        height: bbox.height,
                        x: x - bbox.width / 2,
                        y: y - bbox.height / 2,
                        nodeId: item.get("id"),
                        ...attrs
                    }
                });
                shape.set("capture", false);
                item.set("delegateShape", shape);
            }
            shape.attr({x: x - bbox.width / 2, y: y - bbox.height / 2});
            this.graph.paint();
            this.graph.emit("afternodedrag", shape);
        },
    });
}
