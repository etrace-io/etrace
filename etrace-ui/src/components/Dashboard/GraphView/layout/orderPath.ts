import {flatten, intersection, uniq} from "lodash";

const CustomGraphLayout = {
    // 默认参数
    getDefaultCfg: function getDefaultCfg() {
        return {
            rowSep: 50, // 上下节点之间的距离
            colSep: 50,  // 左右节点节点之间的距离
            needLayout: false,
            direction: ["upper", "right", "down", "left"], // anchor 对应位置和 Index
        };
    },
    // 执行布局
    execute: function execute() {
        const {nodes, edges, needLayout} = this;

        if (!needLayout) {
            return;
        }

        // console.log(this.nodes);
        const allChains = []; // 所有链路
        const nodeInfoMap = new Map(); // id => nodeInfo Map
        const allOutRelationNode = {}; // 所有有「出」edge 的 Node
        this.nodeInfoMap = nodeInfoMap;
        this.allOutRelationNode = allOutRelationNode;

        // const posNode = {
        //   left: null,
        //   right: null,
        //   up: null,
        //   down: null,
        //   isStart: true,
        //   isSingle: true,
        //   nodeList: ["...posNode"],
        //   width: 0,
        //   height: 0,
        //   layout: "ltr",
        // }

        nodes.forEach(node => {
            node.x = null;
            node.y = null;
            nodeInfoMap.set(node.id, node);
        });

        // 整理所有 Out Relation 的 Node
        edges.forEach(edge => {
            // source 源节点 ID
            // target 目标节点 ID
            // startPoint 源节点位置信息及 Anchor 信息
            // endPoint 目标节点位置信息及 Anchor 信息
            // const {source, target, startPoint, endPoint} = edge;
            const {source, target, startPoint} = edge;

            let sourceNode = allOutRelationNode[source]; // allOutRelationNode.find(node => node.node === source);

            if (!sourceNode) {
                const {width, height} = this.getNodeGroupBBox([source]);
                sourceNode = {
                    width,
                    height,
                    node: source,
                };
                allOutRelationNode[source] = sourceNode; // 存储的都是有 Out Relation 的节点
            }

            // 获取某方位上的 Node 节点
            const sourceIndex = startPoint.index; // up: 0, right: 1, down: 2, left: 3;

            const targetDirection = this.direction[sourceIndex];
            const targetNode = sourceNode[targetDirection] || {
                layout: this.getLayout(targetDirection), // (targetDirection === "upper" || targetDirection === "down") ? "LR" : "TB",
                width: 0,
                height: 0,
                dir: targetDirection,
            };

            !targetNode.nodeList && (targetNode.nodeList = []);

            if (targetNode.nodeList.indexOf(target) === -1) {
                targetNode.nodeList.push(target);
            }

            // 每个 Node 只记录 Out Relation
            Object.assign(sourceNode, {
                [targetDirection]: targetNode,
            });
        });
        // console.log(allOutRelationNode);

        Object.keys(allOutRelationNode).forEach(currNodeId => {
            // 所有一级下游节点
            const currNode = allOutRelationNode[currNodeId];
            const nextNodeIds = this.getNextNodes(currNodeId);
            // console.log(currNode, nextNode);

            // 找出单独节点
            currNode.isSingle = nextNodeIds.length === 0;

            // 找出起始节点（该节点不是任意一个节点的下游节点）
            currNode.isStart = true; // 初始化
            Object.keys(allOutRelationNode).forEach(targetNodeId => {
                const targetNextNodeIdx = this.getNextNodes(targetNodeId);
                if (targetNextNodeIdx.indexOf(currNodeId) > -1) {
                    currNode.isStart = false;
                }
            });

            // 计算下游节点的 Size
            this.direction.map(dir => currNode[dir]).filter(Boolean).forEach(currDirAllNodes => {
                // 设置该 Node 所有一级下游节点的总高宽
                const {nodeList, layout, dir: currDir} = currDirAllNodes;
                const {width, height} = this.getNodeGroupBoundingWithLayout(nodeList, layout);
                currDirAllNodes.width = width;
                currDirAllNodes.height = height;

                if (currDir === this.direction[0] || currDir === this.direction[2]) { // upper or down
                    // currNode.width += this.colSep + width
                    currNode.width = Math.max(currNode.width, width);
                }
                if (currDir === this.direction[1] || currDir === this.direction[3]) { // right or left
                    // currNode.height += this.rowSep + height
                    currNode.height = Math.max(currNode.height, height);
                }
            });
        });

        // 构建 Chain
        Object.keys(allOutRelationNode).forEach(currNodeId => {
            const currNode = allOutRelationNode[currNodeId];
            if (currNode.isStart) {
                const chain = [];
                let currChainNodeIds = [currNodeId];
                while (currChainNodeIds) {
                    chain.push(currChainNodeIds);
                    const nextNodeIds = uniq(flatten(currChainNodeIds.map(id => {
                        return this.getNextNodes(id);
                    })));
                    currChainNodeIds = nextNodeIds.length > 0 ? nextNodeIds : undefined;
                }

                allChains.forEach(comparedChain => {
                    const intersectionNodes = intersection(flatten(chain), flatten(comparedChain)); // 判断交集
                    if (intersectionNodes.length === 0) {
                        // new chain
                        allChains.push(chain);
                    } else {
                        const firstIntersectionNode = intersectionNodes[0]; // 第一个相交的点
                        const intersectionIndexByCompared = comparedChain.findIndex(c => c.indexOf(firstIntersectionNode) > -1);
                        const intersectionIndexByOriginal = chain.findIndex(c => c.indexOf(firstIntersectionNode) > -1);
                        if (intersectionIndexByCompared < intersectionIndexByOriginal) {
                            comparedChain.unshift(...chain.slice(0, intersectionIndexByOriginal - intersectionIndexByCompared));
                        }
                        // Merge
                        for (let i = 0; i < intersectionIndexByCompared; i++) {
                            comparedChain[i].push(...chain[i]);
                        }
                    }
                });

                if (allChains.length === 0) {
                    allChains.push(chain);
                }
            }
        });

        // 根据 Chain 进行布局（从下游到上游）
        allChains.forEach(chain => {
            const startNode = chain[0];
            // 控制起始位置
            let nextNodeIds = [];

            // 不断控制下游节点的位置
            startNode.forEach(currNodeId => {
                // 遍历同层所有节点
                nextNodeIds = this.layoutNodeWithChildren(currNodeId);
            });

            while (nextNodeIds.length > 0) {
                const result = [];
                nextNodeIds.forEach(nextNodeId => {
                    const _next = this.layoutNodeWithChildren(nextNodeId);
                    _next.forEach(_nextId => {
                        if (result.indexOf(_nextId) === -1) {
                            result.push(_nextId);
                        }
                    });
                });
                nextNodeIds = result;
            }
        });

        // 处理未定位的 Node
        this.nodes.forEach(node => {
            if (!this.hasLocated(node)) {
                node.x = 0;
                node.y = 0;
            }
        });

        // console.log(this.nodeInfoMap.forEach((node, id) => {console.log(id, node.x, node.y)}));
    },
    layoutNodeWithChildren(currNodeId: string) {
        const currNode = this.nodeInfoMap.get(currNodeId); // 下文称上游节点
        const nextNodeIds = this.getNextNodes(currNodeId);
        if (!this.allOutRelationNode[currNodeId] || this.allOutRelationNode[currNodeId].located) {
            return [];
        } else {
            this.allOutRelationNode[currNodeId].located = true;
        }

        nextNodeIds.forEach(nextNodeId => {
            const isNextNodeLocated = this.hasLocated(nextNodeId);
            const currDir = this.getDirWith(nextNodeId, currNodeId); // 当前 Node 相对于下游节点的方向
            const nextNode = this.nodeInfoMap.get(nextNodeId); // 下文称下游节点

            const {width: currNodeWidth, height: currNodeHeight} = currNode;
            const {width: nextNodeWidth, height: nextNodeHeight} = nextNode;

            // 判断下游节点是否定位（用于同方向其他或其他方向的「上游」节点定位）
            if (isNextNodeLocated) {
                // 下游已定位，定位自身，判断同层同方向是否存在已定位节点
                const sameLevelAndDirNodes = this.getPrevNodes(nextNodeId, [currDir]);
                const sameLevelAndDirLocatedNodes = this.filterLocatedNodes(sameLevelAndDirNodes).filter(id => id !== currNodeId);

                // 判断是否需要定位自身
                if (sameLevelAndDirLocatedNodes.length > 0 && !this.hasLocated(currNodeId)) {
                    // 同层同方向存在其他已定位节点（过滤自己），寻找自身定位
                    // 上游节点已定位则无需定位
                    const {minX, maxX, minY, maxY} = this.getNodeGroupBBox(sameLevelAndDirLocatedNodes);
                    const currLayout = this.getLayout(currDir);
                    const centerX = currLayout === "LR"
                        ? maxX + this.colSep + currNodeWidth / 2
                        : minX + (maxX - minX) / 2;
                    const centerY = currLayout === "LR"
                        ? minY + (maxY - minY) / 2
                        : maxY + this.rowSep + currNodeHeight / 2;
                    this.setNodePosByCenter(centerX, centerY, currNodeId);
                } else {
                    // 同层同方向「无」已定位节点，直接放置
                    if (!this.hasLocated(currNodeId)) {
                        let {x: centerX, y: centerY} = this.getNodeCenterPoint(nextNodeId);
                        if (currDir === this.direction[0]) { // upper
                            centerY -= (currNodeHeight / 2 + this.rowSep);
                        }
                        if (currDir === this.direction[1]) { // right
                            centerX += (currNodeWidth / 2 + this.colSep);
                        }
                        if (currDir === this.direction[2]) { // down
                            centerY += (currNodeHeight / 2 + this.rowSep);
                        }
                        if (currDir === this.direction[3]) { // left
                            centerX -= (currNodeWidth / 2 + this.colSep);
                        }
                        this.setNodePosByCenter(centerX, centerY, currNodeId);
                    }
                }

                // 下游节点根据上游节点重新定位
                let {
                    centerX: groupCenterX,
                    centerY: groupCenterY,
                    width: groupWidth,
                    height: groupHeight,
                } = this.getNodeGroupBBox(sameLevelAndDirLocatedNodes.concat(currNodeId));

                const nextDir = this.getDirWith(currNodeId, nextNodeId);
                const nextOutNode = this.allOutRelationNode[nextNodeId];
                const _nextNodeWidth = nextOutNode ? nextOutNode.width : nextNodeWidth;
                const _nextNodeHeight = nextOutNode ? nextOutNode.height : nextNodeHeight;
                if (nextDir === this.direction[0]) { // upper
                    groupCenterY -= ((groupHeight + _nextNodeHeight) / 2 + this.rowSep);
                }
                if (nextDir === this.direction[1]) { // right
                    groupCenterX += ((groupWidth + _nextNodeWidth) / 2 + this.colSep);
                }
                if (nextDir === this.direction[2]) { // down
                    groupCenterY += ((groupHeight + _nextNodeHeight) / 2 + this.rowSep);
                }
                if (nextDir === this.direction[3]) { // left
                    groupCenterX -= ((groupWidth + _nextNodeWidth) / 2 + this.colSep);
                }
                this.setNodePosByCenter(groupCenterX, groupCenterY, nextNodeId);
            } else {
                // 下游节点未定位，定位上游节点（如果未定位）
                if (!this.hasLocated(currNodeId)) {
                    this.setNodePos(0, 0, currNodeId);
                }
                let {x: centerX, y: centerY} = this.getNodeCenterPoint(currNodeId);
                // 判断下游节点方位
                const nextDir = this.getDirWith(currNodeId, nextNodeId);
                const nextLayout = this.getLayout(nextDir);
                // 下游所有已定位节点重新排序
                const nextLocatedNodes = this.filterLocatedNodes(this.getNextNodes(currNodeId, [nextDir]));

                // 判断是否存在已经定位的下游节点
                if (nextLocatedNodes.length > 0) {
                    // 存在则重新排序所有下游节点（包括当前 nextNode）
                    // 获取已经排序好的下游节点 BBox
                    const {
                        centerX: nextLocatedNodesCenterX,
                        centerY: nextLocatedNodesCenterY,
                        width: nextLocatedNodesWidth,
                        height: nextLocatedNodesHeight,
                    } = this.getNodeGroupBBox(nextLocatedNodes);
                    // 按已定位的节点为基准
                    let needLocatedNodesCenterX = nextLocatedNodesCenterX;
                    let needLocatedNodesCenterY = nextLocatedNodesCenterY;
                    const targetNextLocatedNodes = nextLocatedNodes.concat(nextNodeId);
                    const {
                        width: unlocatedNodeWidth,
                        height: unlocatedNodeHeight
                    } = this.nodeInfoMap.get(nextNodeId);

                    if (nextLayout === "LR") {
                        needLocatedNodesCenterX += (nextLocatedNodesWidth + unlocatedNodeWidth) / 2 + this.colSep;
                    }

                    if (nextLayout === "TB") {
                        needLocatedNodesCenterY += (nextLocatedNodesHeight + unlocatedNodeHeight) / 2 + this.rowSep;
                    }

                    this.setNodePosByCenter(needLocatedNodesCenterX, needLocatedNodesCenterY, nextNodeId);

                    const {width: nextNodesGroupWidth, height: nextNodesGroupHeight} = this.getNodeGroupBBox(targetNextLocatedNodes);

                    this.translateNodes(
                        (nextLocatedNodesWidth - nextNodesGroupWidth) / 2,
                        (nextLocatedNodesHeight - nextNodesGroupHeight) / 2,
                        targetNextLocatedNodes
                    );
                } else {
                    // 不存在已经定位的下游节点，放置第一个下游节点
                    const currOutNode = this.allOutRelationNode[currNodeId];
                    const nextOutNode = this.allOutRelationNode[nextNodeId];

                    // 当前或下个节点可能存在其他子节点撑大节点宽高的 case
                    const _currNodeWidth = currOutNode ? currOutNode.width : currNodeWidth;
                    // const _currNodeHeight = currOutNode ? currOutNode.height : currNodeHeight
                    const _nextNodeWidth = nextOutNode ? nextOutNode.width : nextNodeWidth;
                    const _nextNodeHeight = nextOutNode ? nextOutNode.height : nextNodeHeight;

                    if (nextDir === this.direction[0]) { // upper
                        // upper 和 down 使用 currNodeHeight 而不是 _currNodeHeight 的原因是：
                        // 「图」以横向布局为展示原则；
                        // 即一个节点的宽度可以被 upper 和 down 方向的 nodes 宽度撑开，
                        // 而 left 和 right 不撑开 node 的高度。
                        centerY -= ((currNodeHeight + _nextNodeHeight) / 2 + this.rowSep);
                    }
                    if (nextDir === this.direction[1]) { // right
                        centerX += ((_currNodeWidth + _nextNodeWidth) / 2 + this.colSep);
                    }
                    if (nextDir === this.direction[2]) { // down
                        centerY += ((currNodeHeight + _nextNodeHeight) / 2 + this.rowSep);
                    }
                    if (nextDir === this.direction[3]) { // left
                        centerX -= ((_currNodeWidth + _nextNodeWidth) / 2 + this.colSep);
                    }
                    this.setNodePosByCenter(centerX, centerY, nextNodeId);
                }
            }
        });

        return nextNodeIds;
    },
    hasLocated(node: any) {
        if (typeof node === "string") {
            node = this.nodeInfoMap.get(node);
        }
        const {x, y} = node;
        return x !== null && y !== null;
    },
    filterLocatedNodes(nodeIds: string[]) {
        return nodeIds.map(id => ({id, located: this.hasLocated(id)})).filter(i => i.located).map(i => i.id);
    },
    getLayout(dir: string) {
        return (dir === "upper" || dir === "down") ? "LR" : "TB";
    },
    getNodeGroupBoundingWithLayout(nodeIds: string[], layout: string) {
        let height = 0, width = 0, x = Infinity, y = Infinity;
        nodeIds.forEach(id => {
            const {width: nodeWidth, height: nodeHeight, x: nodeX, y: nodeY} = this.nodeInfoMap.get(id);
            if (layout === "LR") {
                width += nodeWidth + this.colSep;
                height = Math.max(nodeHeight, height);
            } else if (layout === "TB") {
                height += nodeHeight + this.rowSep;
                width = Math.max(nodeWidth, width);
            }
            x = Math.min(x, nodeX);
            y = Math.min(y, nodeY);
        });
        return {x, y, height: Math.max(height - this.rowSep, 0), width: Math.max(width - this.colSep, 0)};
    },
    getNodeGroupBBox(nodeIds: string[]) {
        const nodes = nodeIds.map(id => this.nodeInfoMap.get(id));
        let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
        if (nodes.length > 0) {
            nodes.forEach(node => {
                const {width: nodeWidth, height: nodeHeight, x: nodeX, y: nodeY} = node;
                minX = Math.min(nodeX, minX);
                minY = Math.min(nodeY, minY);
                maxX = Math.max(nodeWidth + nodeX, maxX);
                maxY = Math.max(nodeHeight + nodeY, maxY);
            });
        } else {
            minX = 0;
            minY = 0;
            maxX = 0;
            maxY = 0;
        }

        const width = maxX - minX;
        const height = maxY - minY;
        const centerX = minX + width / 2;
        const centerY = minY + height / 2;

        return {
            minX,
            minY,
            maxX,
            maxY,
            width,
            height,
            centerX,
            centerY,
        };
    },
    getNodeCenterPoint(nodeId: string) {
        const node = this.nodeInfoMap.get(nodeId);
        const {x, y, width, height} = node;
        return {x: x + width / 2, y: y + height / 2};
    },
    setNodePosByCenter(centerX: number, centerY: number, nodeId: string) {
        const node = this.nodeInfoMap.get(nodeId);
        const {width, height} = node;
        node.x = centerX - width / 2;
        node.y = centerY - height / 2;
    },
    setNodePos(x: number, y: number, nodeId: string) {
        const node = this.nodeInfoMap.get(nodeId);
        node.x = x;
        node.y = y;
    },
    translateNodes(offsetX: string, offsetY: string, nodeIds: string[]) {
        nodeIds.forEach(id => {
            const node = this.nodeInfoMap.get(id);
            node.x += offsetX;
            node.y += offsetY;
        });
    },
    getOppositeDir(dir: any) {
        if (typeof dir === "number") {
            switch (dir) {
                case 0:
                    return 2;
                case 1:
                    return 3;
                case 2:
                    return 0;
                case 3:
                    return 1;
                default:
                    return dir;
            }
        }
        if (typeof dir === "string") {
            switch (dir) {
                case "upper":
                    return "down";
                case "right":
                    return "left";
                case "down":
                    return "upper";
                case "left":
                    return "right";
                default:
                    return dir;
            }
        }
    },
    // 获取 sourceNodeId 在 targetNodeId 的什么方位上
    getDirWith(targetNodeId: string, sourceNodeId: string) {
        let result = "";
        this.direction.forEach(dir => {
            const opposDir = this.getOppositeDir(dir);
            const prevNodes = this.getPrevNodes(targetNodeId, [opposDir]);
            const nextNodes = this.getNextNodes(targetNodeId, [dir]);
            if (prevNodes.indexOf(sourceNodeId) > -1) {
                result = opposDir;
            }
            if (nextNodes.indexOf(sourceNodeId) > -1) {
                result = dir;
            }
        });
        return result;
    },
    getPrevNodes(nodeId: string[], dirs: string[] = this.direction) {
        const result = [];
        Object.keys(this.allOutRelationNode).forEach(targetNodeId => {
            // const targetNodeId = node.node
            const targetDirs = dirs.map(dir => this.getOppositeDir(dir));
            // console.log(targetDirs, targetNodeId)
            const nextNodeIds = this.getNextNodes(targetNodeId, targetDirs);
            if (nextNodeIds.indexOf(nodeId) > -1) {
                result.push(targetNodeId);
            }
        });
        return result;
    },
    getNextNodes(nodeId: string, dirs: string[] = this.direction) {
        // TODO 可做缓存
        const node = this.allOutRelationNode[nodeId];
        if (!nodeId || !node) {
            return [];
        }
        return flatten(dirs.map(dir => node[dir] && node[dir].nodeList).filter(Boolean));
    },
};

export default function (G6: any) {
    G6.registerLayout("DashboardLayout", CustomGraphLayout);
}
