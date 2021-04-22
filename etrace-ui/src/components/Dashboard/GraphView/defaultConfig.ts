// const G6 = require("@antv/g6");
// import "@antv/g6/build/plugin.tool.minimap";
// const minimap = new G6.Plugins["tool.minimap"]();

// export {default as CustomNodeConfig} from "../DashboardNode";

export const GraphConfig = {
    modes: {
        view: [
            // "zoom-canvas",
            "dragNode",
            "doubleDrag",
            "clickSelected",
            "drag-canvas",
        ],
        edit: [
            "drag-canvas",
            "doubleDrag",
            // "zoom-canvas",
            "clickSelected",
            "deleteItem",
            "dragEdge",
            "hoverNodeActived",
            "hoverAnchorActived",
            "dragNode",
        ]
    },
    layout: {
        type: "DashboardLayout",
        // type: "dagre",
        // rankdir: "LR",           // 可选，默认为图的中心
        // align: "DL",             // 可选
        // nodesep: 20,             // 可选
        // ranksep: 50,             // 可选
    },
    defaultNode: {
        shape: "DashboardNode",
        // shape: "react",
    },
    defaultEdge: {
        // shape: "polyline",
        // shape: "flow-polyline-round",
        shape: "ant-edge-emergency",
    },
};