const mix = require("@antv/util/lib/mix");
const clone = require("@antv/util/lib/clone");
const isString = require("@antv/util/lib/type/is-string");

class Command {
    private _cfgs: any;
    private list: any[];
    private queue: any[];
    private _events: null;
    private destroyed;

    getDefaultCfg() {
        return {_command: {zoomDelta: .1, queue: [], current: 0, clipboard: []}};
    }

    get(key: string) {
        return this._cfgs[key];
    }

    set(key: string, val: any) {
        this._cfgs[key] = val;
    }

    initPlugin(graph: any) {
        this._cfgs = this.getDefaultCfg();
        this.list = [];
        this.queue = [];
        this.initCommands();
        graph.getCommands = () => {
            return this.get("_command").queue;
        };
        graph.getCurrentCommand = () => {
            const c = this.get("_command");
            return c.queue[c.current - 1];
        };
        graph.executeCommand = (name, cfg) => {
            this.execute(name, graph, cfg);
        };
        graph.commandEnable = (name) => {
            return this.enable(name, graph);
        };
    }

    registerCommand(name: string, cfg: any) {
        if (this[name]) {
            mix(this[name], cfg);
        } else {
            const cmd = mix({}, {
                name: name,
                shortcutCodes: [],
                queue: true,
                executeTimes: 1,
                init() {
                    // ...
                },
                enable() {
                    return true;
                },
                execute(graph: any) {
                    this.snapShot = graph.save();
                    this.selectedItems = graph.get("selectedItems");
                    this.method && (isString(this.method) ? graph[this.method]() : this.method(graph));
                },
                back(graph: any) {
                    graph.read(this.snapShot);
                    graph.set("selectedItems", this.selectedItems);
                }
            }, cfg);
            this[name] = cmd;
            this.list.push(cmd);
        }
    }

    execute(name: string, graph: any, cfg: any) {
        const cmd = mix({}, this[name], cfg);
        const manager = this.get("_command");
        if (cmd.enable(graph)) {
            cmd.init();
            if (cmd.queue) {
                manager.queue.splice(manager.current, manager.queue.length - manager.current, cmd);
                manager.current++;
            }
        }
        graph.emit("beforecommandexecute", {command: cmd});
        cmd.execute(graph);
        graph.emit("aftercommandexecute", {command: cmd});
        return cmd;
    }

    enable(name: string, graph: any) {
        return this[name].enable(graph);
    }

    destroyPlugin() {
        this._events = null;
        this._cfgs = null;
        this.list = [];
        this.queue = [];
        this.destroyed = true;
    }

    initCommands() {
        const cmdPlugin = this;
        cmdPlugin.registerCommand("add", {
            enable: function () {
                return this.type && this.addModel;
            },
            execute: function (graph: any) {
                const item = graph.add(this.type, this.addModel);
                if (this.executeTimes === 1) {
                    this.addId = item.get("id");
                }
            },
            back: function (graph: any) {
                graph.remove(this.addId);
            },
        });
        cmdPlugin.registerCommand("update", {
            enable: function () {
                return this.itemId && this.updateModel;
            },
            execute: function (graph: any) {
                const item = graph.findById(this.itemId);
                if (item) {
                    if (this.executeTimes === 1) {
                        this.originModel = mix({}, this.startModel || item.getModel());
                    }
                    graph.update(item, this.updateModel);
                }
            },
            back: function (graph: any) {
                const item = graph.findById(this.itemId);
                graph.update(item, this.originModel);
            },
        });
        cmdPlugin.registerCommand("delete", {
            enable: function (graph: any) {
                const mode = graph.getCurrentMode();
                const selectedItems = graph.get("selectedItems");
                return mode === "edit" && selectedItems && selectedItems.length > 0;
            },
            method: function (graph: any) {
                const selectedItems = graph.get("selectedItems");
                graph.emit("beforedelete", {items: selectedItems});
                const deleteEdges = [];
                if (selectedItems && selectedItems.length > 0) {
                    selectedItems.forEach(item => {
                        const targetItem = graph.findById(item);
                        const targetClass = targetItem.get("model").class;
                        const groupNode = graph.findAll("node", node => {
                            return node.get("model").class === targetClass && node.get("model").id !== item;
                        });

                        const targetEdges = targetItem.getEdges();
                        targetEdges.forEach(e => {
                            deleteEdges.push(e.getModel());
                        });

                        graph.remove(targetItem);

                        groupNode.forEach(node => {
                            const edges = node.getEdges();
                            edges.forEach(e => {
                                deleteEdges.push(e.getModel());
                            });

                            graph.remove(node);
                        });
                    });
                }

                graph.emit("afterdelete", {items: selectedItems, edges: deleteEdges});
            },
            shortcutCodes: ["Delete", "Backspace"],
        });
        cmdPlugin.registerCommand("redo", {
            queue: false,
            enable: function (graph: any) {
                const mode = graph.getCurrentMode();
                const manager = cmdPlugin.get("_command");
                return mode === "edit" && manager.current < manager.queue.length;
            },
            execute: function (graph: any) {
                const manager = cmdPlugin.get("_command");
                const cmd = manager.queue[manager.current];
                cmd && cmd.execute(graph);
                manager.current++;
            },
            shortcutCodes: [["metaKey", "shiftKey", "z"], ["ctrlKey", "shiftKey", "z"]],
        });
        cmdPlugin.registerCommand("undo", {
            queue: false,
            enable: function (graph: any) {
                const mode = graph.getCurrentMode();
                return mode === "edit" && cmdPlugin.get("_command").current > 0;
            },
            execute: function (graph: any) {
                const manager = cmdPlugin.get("_command");
                const cmd = manager.queue[manager.current - 1];
                if (cmd) {
                    cmd.executeTimes++;
                    cmd.back(graph);
                }
                manager.current--;
            },
            shortcutCodes: [["metaKey", "z"], ["ctrlKey", "z"]],
        });
        cmdPlugin.registerCommand("copy", {
            queue: false,
            enable: function (graph: any) {
                const mode = graph.getCurrentMode();
                const items = graph.get("selectedItems");
                return mode === "edit" && items && items.length > 0;
            },
            method: function (graph: any) {
                const manager = cmdPlugin.get("_command");
                manager.clipboard = [];
                const items = graph.get("selectedItems");
                if (items && items.length > 0) {
                    const item = graph.findById(items[0]);
                    if (item) {
                        manager.clipboard.push({type: item.get("type"), model: item.getModel()});
                        graph.executeCommand("paste");
                    }
                }
            },
        });
        cmdPlugin.registerCommand("paste", {
            enable: function (graph: any) {
                const mode = graph.getCurrentMode();
                return mode === "edit" && cmdPlugin.get("_command").clipboard.length > 0;
            },
            method: function (graph: any) {
                const manager = cmdPlugin.get("_command");
                this.pasteData = clone(manager.clipboard[0]);
                const addModel = this.pasteData.model;
                addModel.x && (addModel.x += 10);
                addModel.y && (addModel.y += 10);
                delete addModel.id;
                const item = graph.add(this.pasteData.type, addModel);
                item.toFront();
            },
        });
        cmdPlugin.registerCommand("zoomIn", {
            queue: false,
            enable: function (graph: any) {
                const zoom = graph.getZoom();
                const maxZoom = graph.get("maxZoom");
                const minZoom = graph.get("minZoom");
                return zoom <= maxZoom && zoom >= minZoom;
            },
            execute: function (graph: any) {
                const manager = cmdPlugin.get("_command");
                const maxZoom = graph.get("maxZoom");
                const zoom = graph.getZoom();
                this.originZoom = zoom;
                let currentZoom = zoom + manager.zoomDelta;
                if (currentZoom > maxZoom) {
                    currentZoom = maxZoom;
                }
                graph.zoomTo(currentZoom);
            },
            back: function (graph: any) {
                graph.zoomTo(this.originZoom);
            },
            shortcutCodes: [["metaKey", "="], ["ctrlKey", "="]],
        });
        cmdPlugin.registerCommand("zoomOut", {
            queue: false,
            enable: function (graph: any) {
                const zoom = graph.getZoom();
                const maxZoom = graph.get("maxZoom");
                const minZoom = graph.get("minZoom");
                return zoom <= maxZoom && zoom >= minZoom;
            },
            execute: function (graph: any) {
                const manager = cmdPlugin.get("_command");
                const minZoom = graph.get("minZoom");
                const zoom = graph.getZoom();
                this.originZoom = zoom;
                let currentZoom = zoom - manager.zoomDelta;
                if (currentZoom < minZoom) {
                    currentZoom = minZoom;
                }
                graph.zoomTo(currentZoom);
            },
            back: function (graph: any) {
                graph.zoomTo(this.originZoom);
            },
            shortcutCodes: [["metaKey", "-"], ["ctrlKey", "-"]],
        });
        cmdPlugin.registerCommand("alignCenter", {
            queue: false,
            execute: function (graph: any) {
                const zoom = graph.getZoom();
                graph.fitView(5);
                const width = graph.get("width");
                const height = graph.get("height");
                graph.zoomTo(zoom, {x: width / 2, y: height / 2});
            },
        });
        cmdPlugin.registerCommand("resetZoom", {
            queue: false,
            execute: function (graph: any) {
                const zoom = graph.getZoom();
                this.originZoom = zoom;
                const width = graph.get("width");
                const height = graph.get("height");
                graph.zoomTo(1, {x: width / 2, y: height / 2});
            },
            back: function (graph: any) {
                graph.zoomTo(this.originZoom);
            },
        });
        cmdPlugin.registerCommand("autoFit", {
            queue: false,
            execute: function (graph: any) {
                const zoom = graph.getZoom();
                this.originZoom = zoom;
                graph.fitView(5);
            },
            back: function (graph: any) {
                graph.zoomTo(this.originZoom);
            },
        });
        cmdPlugin.registerCommand("toFront", {
            queue: false,
            enable: function (graph: any) {
                const items = graph.get("selectedItems");
                return items && items.length > 0;
            },
            execute: function (graph: any) {
                const items = graph.get("selectedItems");
                if (items && items.length > 0) {
                    const item = graph.findById(items[0]);
                    item.toFront();
                    graph.paint();
                }
            },
            back: function (graph: any) {
                // ...
            },
        });
        cmdPlugin.registerCommand("toBack", {
            queue: false,
            enable: function (graph: any) {
                const items = graph.get("selectedItems");
                return items && items.length > 0;
            },
            execute: function (graph: any) {
                const items = graph.get("selectedItems");
                if (items && items.length > 0) {
                    const item = graph.findById(items[0]);
                    item.toBack();
                    graph.paint();
                }
            },
            back: function (graph: any) {
                // ...
            },
        });
        cmdPlugin.registerCommand("layout", {
            queue: false,
            enable: function (graph: any) {
                return true;
            },
            execute: function (graph: any) {
                // const snapShot = graph.save();
                // graph.read(snapShot);
                graph.updateLayout({needLayout: true});
                // graph.updateLayout({needLayout: false});
                // graph.layout();
                // currGraph.changeLayout({});
                // currGraph.refreshLayout(false);
            },
        });
    }
}

export default Command;
