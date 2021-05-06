// import {getGraphStyle} from "../defaultStyle";

// todo:
// const Item = require("@antv/g6/src/item/item");

const createAnchor = (index, style, group) => {
    // const graphStyle = getGraphStyle();
    // const anchorContainer = group.addGroup();
    // todo:
    // const anchor = new Item({
    //     type: "anchor",
    //     group: anchorContainer,
    //     capture: false,
    //     index,
    //     isActived: false,
    //     model: {
    //         style: {
    //             ...style,
    //             ...graphStyle.anchorPointStyle,
    //             cursor: graphStyle.cursor.hoverEffectiveAnchor,
    //         }
    //     },
    // });
    // anchor.isAnchor = true;
    // anchor.toFront();
    //
    // let hotpot;
    // anchor.showHotpot = function () {
    //     hotpot = anchorContainer.addShape("marker", {
    //         attrs: {
    //             ...style,
    //             ...graphStyle.anchorHotsoptStyle
    //         }
    //     });
    //     hotpot.toFront();
    //     anchor.getKeyShape().toFront();
    // };
    // anchor.setActived = function () {
    //     anchor.update({style: {...graphStyle.anchorPointHoverStyle}});
    // };
    // anchor.clearActived = function () {
    //     anchor.update({style: {...graphStyle.anchorPointStyle}});
    // };
    // anchor.setHotspotActived = function (act: any) {
    //     if (hotpot) {
    //         act ? hotpot.attr(graphStyle.anchorHotsoptActivedStyle)
    //             : hotpot.attr(graphStyle.anchorHotsoptStyle);
    //     }
    // };
    // return anchorContainer;
};

export default createAnchor;
