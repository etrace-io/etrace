const deepMix = require("@antv/util/lib/deep-mix");
const each = require("@antv/util/lib/each");
const createDOM = require("@antv/util/lib/dom/create-dom");

class AddItemPanel {
    private _cfgs: any;

    constructor(cfgs: any) {
        this._cfgs = deepMix(this.getDefaultCfg(), cfgs);
    }

    getDefaultCfg() {
        return {container: null};
    }

    get(key: any) {
        return this._cfgs[key];
    }

    set(key: any, val: any) {
        this._cfgs[key] = val;
    }

    initPlugin(graph: any) {
        const parentNode = this.get("container");
        const children = parentNode.querySelectorAll("div[data-item]");

        each(children, (child, i) => {
            const addModel = (new Function("return " + child.getAttribute("data-item")))();
            child.addEventListener("dragstart", e => {
                graph.set("onDragNode", true);
                graph.set("addModel", addModel);
            });
            child.addEventListener("dragend", e => {
                graph.emit("canvas:mouseup", e);
                graph.set("onDragNode", false);
                graph.set("addModel", null);
            });
        });
    }

    destroy() {
        this.get("canvas").destroy();
        const container = this.get("container");
        container.parentNode.removeChild(container);
    }
}

export default AddItemPanel;
