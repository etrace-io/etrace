const deepMix = require("@antv/util/lib/deep-mix");
const each = require("@antv/util/lib/each");
const wrapBehavior = require("@antv/util/lib/event/wrap-behavior");

class PluginBase {
    _cfgs: any;
    private _events: {};
    private destroyed: boolean;

    constructor(cfgs: any) {
        this._cfgs = deepMix(this.getDefaultCfgs(), cfgs);
    }

    getDefaultCfgs() {
        return {};
    }

    initPlugin(graph: any) {
        const self = this;
        self.set("graph", graph);
        const events = self.getEvents();
        const bindEvents = {};
        each(events, (v, k) => {
            const event = wrapBehavior(self, v);
            bindEvents[k] = event;
            graph.on(k, event);
        });
        this._events = bindEvents;
        this.init();
    }

    init() {
        // ...
    }

    destroy() {
        // ...
    }

    getEvents() {
        return {};
    }

    get(key: any) {
        return this._cfgs[key];
    }

    set(key: any, val: any) {
        this._cfgs[key] = val;
    }

    destroyPlugin() {
        this.destroy();
        const graph = this.get("graph");
        const events = this._events;
        each(events, (v, k) => {
            graph.off(k, v);
        });
        this._events = null;
        this._cfgs = null;
        this.destroyed = true;
    }

}

export default PluginBase;