import {getGraphWithId} from "../../services/DashboardService";
import {DashboardGraphRelation, DashboardNode, DashboardNodeQueryResult} from "../../models/DashboardModel";
import {processGroupNode, queryNodeInfoWithId} from "./DashboardNode";

/**
 * 请求 Graph 信息
 */
export async function queryGraphInfoWithId(id: number) {
    return getGraphWithId(id);
    // return queryGraphNodesByNodeIds(graph, graph.nodeIds);
}

/**
 * 根据 Node Id List 请求所有 Node 信息
 * 需要 relations 来编辑 GroupNode 下的 Node relation
 * 如果是 isView 则需要解析其子 Node
 */
export async function queryGraphNodesByNodeIds(nodeIds: number[], relations?: DashboardGraphRelation[], isView?: boolean): Promise<{nodes: DashboardNodeQueryResult[][] | DashboardNode[], relations: DashboardGraphRelation[]}> {
    // 遍历请求 Node 信息并返回
    const query = nodeIds.map((node: number) => queryNodeInfoWithId(node, isView));
    const nodes = await Promise.all(query);

    if (isView) {
        return processGroupNode(
            (nodes as DashboardNodeQueryResult[][]),
            relations
        );
    } else {
        return {
            nodes: nodes as DashboardNode[],
            relations
        };
    }
}