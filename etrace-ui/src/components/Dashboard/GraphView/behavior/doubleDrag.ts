export default function (G6: any) {
    G6.registerBehavior("doubleDrag", {
        getEvents: function getEvents() {
            return {
                wheel: "onWheel"
            };
        },
        onWheel: function onWheel(ev: any) {
            if (ev.ctrlKey) {
                const canvas = this.graph.get("canvas");
                const pixelRatio = canvas.get("pixelRatio");
                const point = canvas.getPointByClient(ev.clientX, ev.clientY);
                let ratio = this.graph.getZoom();
                if (ev.wheelDelta > 0) {
                    ratio = ratio + ratio * 0.05;
                } else {
                    ratio = ratio - ratio * 0.05;
                }
                this.graph.zoomTo(ratio, {
                    x: point.x / pixelRatio,
                    y: point.y / pixelRatio
                });
            } else {
                const x = ev.deltaX || (ev.axis === 1 ? ev.detail : 0);
                const y = ev.deltaY || (ev.axis === 2 ? ev.detail : 0);
                this._translate(x, y);
            }
            ev.preventDefault();
        },
        _translate(x: any, y: any) {
            const containerWidth = this.graph.get("width");
            const containerHeight = this.graph.get("height");

            const CANVAS_WIDTH = containerWidth - 100;
            const CANVAS_HEIGHT = containerHeight + 100;

            const LIMIT_OVERFLOW_WIDTH = CANVAS_WIDTH - 100;
            const LIMIT_OVERFLOW_HEIGHT = CANVAS_HEIGHT - 100;
            let moveX = x;
            let moveY = y;
            // 获得当前偏移量
            const group = this.graph.get("group");
            const bbox = group.getBBox();
            const leftTopPoint = this.graph.getCanvasByPoint(bbox.minX, bbox.minY);
            const rightBottomPoint = this.graph.getCanvasByPoint(bbox.maxX, bbox.maxY);
            /* 如果 x 轴在区域内，不允许左右超过100 */
            if (x < 0 && leftTopPoint.x - x > LIMIT_OVERFLOW_WIDTH) {
                moveX = 0;
            }
            if (x > 0 && rightBottomPoint.x - x < containerWidth - LIMIT_OVERFLOW_WIDTH) {
                moveX = 0;
            }

            if (y < 0 && leftTopPoint.y - y > LIMIT_OVERFLOW_HEIGHT) {
                moveY = 0;
            }
            if (y > 0 && rightBottomPoint.y - y < containerHeight - LIMIT_OVERFLOW_HEIGHT) {
                moveY = 0;
            }
            this.graph.translate(-moveX, -moveY);
        }
    });
}