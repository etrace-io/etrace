// import {getGraphStyle} from "../defaultStyle";

// const SingleShapeMixin = require("@antv/g6/src/shape/single-shape-mixin");

export default function (G6: any) {
    // const graphStyle = getGraphStyle();
    // G6.Shape.registerFactory("anchor", {
    //     defaultShapeType: "marker",
    // });

    // todo:
    // G6.Shape.registerAnchor("single-anchor", G6.Util.mix({}, SingleShapeMixin, {
    //     itemType: "anchor",
    //
    //     drawShape(cfg: any, group: any) {
    //         const shapeType = this.shapeType;
    //         const style = this.getShapeStyle(cfg);
    //         const shape = group.addShape(shapeType, {
    //             attrs: style,
    //         });
    //         return shape;
    //     },
    //
    //     setState(name: string, value: any, item: any) {
    //         if (name === "active-anchor") {
    //             if (value) {
    //                 this.update({style: {...graphStyle.anchorPointHoverStyle}}, item);
    //             } else {
    //                 this.update({style: {...graphStyle.anchorPointStyle}}, item);
    //             }
    //         }
    //     },
    // }));

    // G6.Shape.registerAnchor("marker", {shapeType: "marker"}, "single-anchor");
}
