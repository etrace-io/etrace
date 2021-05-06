export default function (G6: any) {
    G6.registerBehavior("clickSelected", {
        getDefaultCfg() {
            return {
                multiple: false,
            };
        },
        getEvents() {
            return {
                "node:click": "onClick",
                "edge:click": "onClick",
                "edge:mouseover": "onEdgeMouseOver",
                "edge:mouseleave": "onEdgeMouseLeave",
                "canvas:click": "onCanvasClick",
                "node:mouseover": "onNodeMouseOver",
            };
        },
        onClick(e: any) {
            this._clearSelected();
            this.graph.setItemState(e.item, "selected", true);
            let selectedItems = this.graph.get("selectedItems");
            if (!selectedItems) {
                selectedItems = [];
            }
            selectedItems = [e.item.get("id")];

            // 激活 Edge
            if (this.graph.getCurrentMode() === "view") {
                selectedItems.forEach(id => {
                    const item = this.graph.findById(id);
                    if (item.get("type") === "node") {
                        const edges = item.getEdges();
                        edges.forEach(edge => {
                            this.graph.setItemState(edge, "selected", true);
                        });
                    }
                });
            }

            // console.log("selectedItems", selectedItems);
            this.graph.set("selectedItems", selectedItems);
            this.graph.emit("afteritemselected", selectedItems);
        },
        onNodeMouseOver(e: any) {
            this.graph.setItemState(e.item, "hover", this.graph.getCurrentMode() === "edit");
        },
        onEdgeMouseOver(e: any) {
            if (!e.item.hasState("selected")) {
                this.graph.setItemState(e.item, "hover", true);
            }
        },
        onEdgeMouseLeave(e: any) {
            if (!e.item.hasState("selected")) {
                this.graph.setItemState(e.item, "hover", false);
            }
        },
        onCanvasClick() {
            this._clearSelected();
        },
        _clearSelected() {
            let selected = this.graph.findAllByState("node", "selected");
            selected.forEach(node => {
                this.graph.setItemState(node, "selected", false);
            });
            selected = this.graph.findAllByState("edge", "selected");
            selected.forEach(edge => {
                this.graph.setItemState(edge, "selected", false);
            });
            this.graph.set("selectedItems", []);
            this.graph.emit("afteritemselected", []);
        }
    });
}