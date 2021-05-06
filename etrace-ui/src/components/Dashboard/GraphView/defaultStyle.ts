import {get} from "lodash";
import {Theme} from "$constants/Theme";
import StoreManager from "../../../store/StoreManager";

export const defaultTheme = {
    nodeActivedOutterStyle: {lineWidth: 0},
    groupSelectedOutterStyle: {stroke: "#E0F0FF", lineWidth: 2},
    nodeSelectedOutterStyle: {stroke: "#E0F0FF", lineWidth: 2},
    edgeActivedStyle: {stroke: "#1890FF", strokeOpacity: .92},
    nodeActivedStyle: {fill: "#F3F9FF", stroke: "#1890FF"},
    groupActivedStyle: {stroke: "#1890FF"},
    edgeSelectedStyle: {lineWidth: 3.5, strokeOpacity: .92, stroke: "#A3B1BF"},
    nodeSelectedStyle: {fill: "#F3F9FF", stroke: "#1890FF", fillOpacity: .4},
    groupSelectedStyle: {stroke: "#1890FF", fillOpacity: .92},
    nodeStyle: {
        stroke: "#CED4D9",
        fill: "#FFFFFF",
        shadowOffsetX: 0,
        shadowOffsetY: 4,
        shadowBlur: 10,
        shadowColor: "rgba(13, 26, 38, 0.08)",
        lineWidth: 1,
        radius: 4,
        strokeOpacity: .7,
    },
    edgeStyle: {stroke: "#A3B1BF", strokeOpacity: .92, lineWidth: 1, lineAppendWidth: 8, endArrow: true, color: "#e2e2e2"},
    groupBackgroundPadding: [40, 10, 10, 10],
    groupLabelOffsetX: 10,
    groupLabelOffsetY: 10,
    edgeLabelStyle: {fill: "#666", textAlign: "center", textBaseline: "middle"},
    edgeLabelRectPadding: 4,
    edgeLabelRectStyle: {fill: "white"},
    nodeLabelStyle: {fill: "#666", textAlign: "center", textBaseline: "middle"},
    groupStyle: {stroke: "#CED4D9", radius: 4},
    groupLabelStyle: {fill: "#666", textAlign: "left", textBaseline: "top"},
    multiSelectRectStyle: {fill: "#1890FF", fillOpacity: .08, stroke: "#1890FF", opacity: .1},
    dragNodeHoverToGroupStyle: {stroke: "#1890FF", lineWidth: 2},
    dragNodeLeaveFromGroupStyle: {stroke: "#BAE7FF", lineWidth: 2},
    anchorPointStyle: {radius: 3.5, fill: "#fff", stroke: "#1890FF", lineAppendWidth: 12},
    anchorHotsoptStyle: {radius: 12, fill: "#1890FF", fillOpacity: .25},
    anchorHotsoptActivedStyle: {radius: 14},
    anchorPointHoverStyle: {radius: 4, fill: "#1890FF", fillOpacity: 1, stroke: "#1890FF"},
    nodeControlPointStyle: {radius: 4, fill: "#fff", shadowBlur: 4, shadowColor: "#666"},
    edgeControlPointStyle: {radius: 6, symbol: "square", lineAppendWidth: 6, fillOpacity: 0, strokeOpacity: 0},
    nodeSelectedBoxStyle: {stroke: "#C2C2C2"},
    cursor: {
        panningCanvas: "-webkit-grabbing",
        beforePanCanvas: "-webkit-grab",
        hoverNode: "move",
        hoverEffectiveAnchor: "crosshair",
        hoverEdge: "default",
        hoverGroup: "move",
        hoverUnEffectiveAnchor: "default",
        hoverEdgeControllPoint: "crosshair",
        multiSelect: "crosshair",
    },
    nodeDelegationStyle: {
        stroke: "#1890FF",
        fill: "#1890FF",
        fillOpacity: .08,
        lineDash: [4, 4],
        radius: 4,
        lineWidth: 1,
    },
    edgeDelegationStyle: {stroke: "#1890FF", lineDash: [4, 4], lineWidth: 1},
    nodeContainerStyle: { radius: [6], lineWidth: 5, shadowOffsetX: 0, shadowOffsetY: 4, shadowColor: "rgba(0,0,0,0.05)", shadowBlur: 20, fill: "#f9f9f9" },
    nodeContainerSelectStyle: { radius: [6], lineWidth: 5, shadowOffsetX: 0, shadowOffsetY: 4, shadowColor: "rgba(0,0,0,0.05)", shadowBlur: 20, fill: "#eee" },
    nodeTitleStyle: {x: 0, y: 0, textAlign: "center", textBaseline: "middle", fill: "#666", fontSize: 20, fontWeight: "bold"},
    nodeGroupFieldStyle: {x: 0, y: 0, textBaseline: "middle", fill: "#666", fontSize: 15, fontFamily: "monospace", fontWeight: "bold"},
    nodeGroupIndicatorStyle: {x: 0, y: 0, fill: "#DCDFE6", radius: [0], lineWidth: 3, height: 25, width: 6},
    nodeContentMetricNameStyle: {x: 0, y: 0, textAlign: "left", textBaseline: "middle", fill: "#666", fontSize: 14},
    nodeContentChartStyle: {x: 0, y: 0, width: 100, height: 30},
    nodeContentValueStyle: {x: 0, y: 0, textAlign: "center", textBaseline: "middle", fill: "#666", fontSize: 14, fontFamily: "monospace", fontWeight: "bold"},
    nodeContentValueTitleStyle: {x: 0, y: 0, textAlign: "center", textBaseline: "middle", fill: "#1890ff", fontSize: 14, fontWeight: "bold", fontFamily: "monospace"},
    nodeBadgeTextStyle: {x: 0, y: 0, textAlign: "left", textBaseline: "middle", fill: "#fff", fontSize: 12},
};

export const darkTheme = Object.assign({}, defaultTheme, {
    nodeContainerStyle: { radius: [6], lineWidth: 5, shadowOffsetX: 0, shadowOffsetY: 4, shadowColor: "rgba(0,0,0,0.05)", shadowBlur: 20, fill: "#444444" },
    nodeContainerSelectStyle: { radius: [6], lineWidth: 5, shadowOffsetX: 0, shadowOffsetY: 4, shadowColor: "rgba(0,0,0,0.05)", shadowBlur: 20, fill: "#676767" },
    nodeTitleStyle: {x: 0, y: 0, textAlign: "center", textBaseline: "middle", fill: "#ddd", fontSize: 20, fontWeight: "bold"},
    nodeGroupFieldStyle: {x: 0, y: 0, textBaseline: "middle", fill: "#ddd", fontSize: 15, fontFamily: "monospace", fontWeight: "bold"},
    nodeContentMetricNameStyle: {x: 0, y: 0, textAlign: "left", textBaseline: "middle", fill: "#ddd", fontSize: 14},
    nodeContentValueStyle: {x: 0, y: 0, textAlign: "center", textBaseline: "middle", fill: "#ddd", fontSize: 14, fontFamily: "monospace", fontWeight: "bold"},
    nodeGroupIndicatorStyle: {x: 0, y: 0, fill: "#676767", radius: [0], lineWidth: 3, height: 25, width: 6},
});

export const lightTheme = Object.assign({}, defaultTheme, {

});

export function getGraphStyle() {
    const localStorageTheme = localStorage.getItem("THEME");
    const currTheme = get(StoreManager.userStore.user, "userConfig.config.theme", localStorageTheme);
    return currTheme === Theme.Light ? lightTheme : darkTheme;
}
