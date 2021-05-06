export default function (G6: any) {
    G6.registerBehavior("hoverNodeActived", {
        getEvents() {
            return {
                "node:mouseover": "onNodeEnter",
                "node:mouseleave": "onNodeLeave",
                "anchor:mouseleave": "onAnchorLeave",
            };
        },
        onAnchorLeave(e: any) {
            let node = e.item.getContainer().getParent();
            if (node && !this.graph.get("onDragEdge")) {
                this.graph.setItemState(node.get("item"), "show-anchor", false);
            }
        },
        onNodeEnter(e: any) {
            const clazz = e.item.getModel().clazz;
            if (clazz !== "endEvent" && !this.graph.get("onDragEdge")) {
                this.graph.setItemState(e.item, "show-anchor", true);
            }
        },
        onNodeLeave(e: any) {
            if (e.target.type !== "marker" && !this.graph.get("onDragEdge")) {
                this.graph.setItemState(e.item, "show-anchor", false);
            }
        },
    });
}