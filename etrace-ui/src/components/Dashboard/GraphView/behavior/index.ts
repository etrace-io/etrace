import clickSelected from "./clickSelected";
import deleteItem from "./deleteItem";
import dragNode from "./dragNode";
import dragEdge from "./dragEdge";
// import dragPanelItemAddNode from "./dragPanelItemAddNode"
import hoverAnchorActived from "./hoverAnchorActived";
import hoverNodeActived from "./hoverNodeActived";
import doubleDrag from "./doubleDrag";
// import itemAlign from "./itemAlign"

export default function (G6: any) {
    clickSelected(G6);
    deleteItem(G6);
    dragNode(G6);
    dragEdge(G6);
    // dragPanelItemAddNode(G6);
    hoverAnchorActived(G6);
    hoverNodeActived(G6);
    doubleDrag(G6);
    // itemAlign(G6);
}