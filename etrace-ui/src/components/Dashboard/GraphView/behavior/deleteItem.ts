export default function (G6: any) {
    G6.registerBehavior("deleteItem", {
        getEvents() {
            return {
                "keydown": "onKeydown",
                // "canvas:mouseleave": "onCanvasLeave",
                // "canvas:mouseenter": "onCanvasFocus",
            };
        },
        onKeydown(e: any) {
            const items = this.graph.get("selectedItems");
            // const focus = this.graph.get("focusGraphWrapper");
            const isDeleteKey = e.keyCode === 8;

            if (isDeleteKey && items && items.length > 0) {
                if (this.graph.executeCommand) {
                    this.graph.executeCommand("delete", {});
                } else {
                    const targetNode = this.graph.findById(items[0]);
                    const targetClass = targetNode.get("model").class;
                    const groupNode = this.graph.findAll("node", node => {
                        return node.get("model").class === targetClass;
                    });

                    groupNode.forEach(node => {
                        this.graph.remove(node);
                    });
                }
                this.graph.set("selectedItems", []);
                this.graph.emit("afteritemselected", []);
                e.preventDefault();
            }
        },
        // onCanvasLeave(e: any) {
        //     this.graph.set("focusGraphWrapper", false);
        // },
        // onCanvasFocus() {
        //     this.graph.set("focusGraphWrapper", true);
        // }
    });
}