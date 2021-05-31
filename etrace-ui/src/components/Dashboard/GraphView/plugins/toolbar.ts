const deepMix = require("@antv/util/lib/deep-mix");
const each = require("@antv/util/lib/each");
const wrapBehavior = require("@antv/util/lib/event/wrap-behavior");

class Toolbar {

    private _cfgs: any;
    private _events: {};

    constructor(cfgs: any) {
        this._cfgs = deepMix(this.getDefaultCfg(), cfgs);
    }

    getDefaultCfg() {
        return {container: null};
    }

    get(key: string) {
        return this._cfgs[key];
    }

    set(key: string, val: any) {
        this._cfgs[key] = val;
    }

    initPlugin(graph: any) {
        const self = this;
        this.set("graph", graph);
        const events = self.getEvents();
        const bindEvents = {};
        each(events, (v, k) => {
            const event = wrapBehavior(self, v);
            bindEvents[k] = event;
            graph.on(k, event);
        });
        this._events = bindEvents;

        this.initEvents();
        this.updateToolbar();
    }

    getEvents() {
        return {afteritemselected: "updateToolbar", aftercommandexecute: "updateToolbar"};
    }

    initEvents() {
        const graph = this.get("graph");
        const parentNode = this.get("container");
        const children = parentNode.querySelectorAll("div > span[data-command]");
        each(children, (child, i) => {
            const cmdName = child.getAttribute("data-command");
            child.addEventListener("click", e => {
                graph.commandEnable(cmdName) && graph.executeCommand(cmdName);
            });
        });

        // console.log("init event", parentNode);

        // save btn
        const saveBtn = parentNode.querySelectorAll("[data-command=save]");
        saveBtn[0] && saveBtn[0].addEventListener("click", e => {
            graph.emit("save", {graph: graph.save()});
        });
    }

    updateToolbar() {
        const graph = this.get("graph");
        const parentNode = this.get("container");
        const children = parentNode.querySelectorAll("div > span[data-command]");
        each(children, (child, i) => {
            const cmdName = child.getAttribute("data-command");
            if (graph.commandEnable(cmdName)) {
                child.children[0].classList.remove("disable");
            } else {
                child.children[0].classList.add("disable");
            }
        });
    }

    destroyPlugin() {
        if (this.get) {
            const container = this.get("container");
            container.parentNode.removeChild(container);
        }
    }
}

export default Toolbar;
