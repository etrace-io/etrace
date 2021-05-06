import React, {ReactNode} from "react";
import {Popover, Spin, Tooltip, Tree} from "antd";
import {autobind} from "core-decorators";
import {get, isEmpty, uniq} from "lodash";
import {StatListItem, StatListTree, Target} from "../../models/ChartModel";
import {DataFormatter} from "../../utils/DataFormatter";
import {URLParamStore} from "../../store/URLParamStore";
import StoreManager from "../../store/StoreManager";
import {MapToObject} from "../../utils/Util";
import {StatListStore} from "../../store/StatListStore";
import {reaction} from "mobx";
import {buildTargetWithoutOrderBy} from "../../services/LinDBService";
import {ExclamationOutlined} from "@ant-design/icons/lib";
import IconFont from "$components/Base/IconFont";

const R = require("ramda");
// const TreeNode = Tree.TreeNode;
const { TreeNode } = Tree;

export interface StatListProps {
    target?: Target;
    // only support 1 or 2 level.
    levelKeys: Array<string>;

    hasLink?: (item: StatListTree) => boolean;
    popover?: (item: StatListTree) => string | ReactNode;

    keepStatList?: boolean;

    searchValue?: string;
    sortOrder?: boolean;
}

export interface StatListState {
    expandedKeys?: string[];
    listsTagFilter?: any;
    listExtraParams?: any;
    treeData: Array<StatListTree>;
    loading?: boolean;
}

/**
 * based on Tree Component: https://ant.design/components/tree-cn/
 * 可搜索的树。
 */
export class StatList extends React.Component<StatListProps, StatListState> {
    private urlParamStore: URLParamStore;
    private statListStore: StatListStore;

    private isOneLevel: boolean;
    private allNodeKeys: string[] = [];
    private treeData: Array<StatListTree> = [];
    private statListData: Array<StatListItem> = [];
    private searchValue: string;
    private sortOrder?: boolean;
    private target: Target;
    private targetTemplate: Target;

    private readonly disposer;
    private readonly disposer1;

    constructor(props: StatListProps) {
        super(props);
        this.state = {
            treeData: [],
            loading: true,
        };
        this.targetTemplate = props.target;
        this.target = this.getTarget();
        this.sortOrder = props.sortOrder;

        this.urlParamStore = StoreManager.urlParamStore;
        this.statListStore = StoreManager.statListStore;
        this.searchValue = props.searchValue;

        this.treeData = [];

        this.disposer = reaction(
            () => this.urlParamStore.changed,
            () => {
                const target = this.getTarget();
                if (!R.equals(target, this.target)) {
                    this.loadData(target);
                }
            }
        );

        // 强制刷新，直接刷新数据
        this.disposer1 = reaction(
            () => this.urlParamStore.forceChanged,
            () => {
                this.loadData(this.getTarget());
            }
        );
    }

    componentWillUnmount(): void {
        if (this.disposer) {
            this.disposer();
        }
        if (this.disposer1) {
            this.disposer1();
        }
        if (this.props.keepStatList) {
            StoreManager.stateLinkStore.statListData = [];
        }
    }

    componentWillReceiveProps(nextProps: StatListProps) {
        if (!R.equals(this.targetTemplate, nextProps.target)) {
            this.targetTemplate = nextProps.target;
            const target = this.getTarget();
            this.loadData(target);
        } else if (!R.equals(this.searchValue, nextProps.searchValue)) {
            this.searchValue = nextProps.searchValue;
            this.onTreeSearchChange(this.searchValue, this.treeData);
            this.setState({treeData: this.treeData, loading: false});
        } else if (nextProps.sortOrder != this.sortOrder) {
            this.sortOrder = nextProps.sortOrder;
            this.buildTreeData(nextProps);
            this.setState({treeData: this.treeData, loading: false});
        }
    }

    @autobind
    getTarget(): Target {
        return buildTargetWithoutOrderBy(this.targetTemplate);
    }

    @autobind
    buildTreeData(props: StatListProps) {
        const statList = this.statListData;
        const {levelKeys} = props;
        const displayField = this.target.groupByKeys;
        if (levelKeys.length == 0 || levelKeys.length > 2) {
            console.warn("StatList Only support one level to two level!");
        }
        let level1 = displayField ? displayField[0] : levelKeys[0];
        let level2 = displayField ? displayField[1] : levelKeys[1];
        if (statList) {
            if (level1 && level2) {
                this.treeData = this.buildTwoLevel(statList, level1, level2);
            } else if (level1) {
                this.isOneLevel = true;
                this.treeData = this.buildOneLevel(statList, level1);
            }
        }
    }

    componentDidMount() {
        this.loadData(this.target);
    }

    @autobind
    loadData(target: Target) {
        this.target = target;
        this.setState({loading: true});
        this.statListStore.loadStatListData(target).then((statList: Array<StatListItem>) => {
            this.statListData = statList;
            if (this.props.keepStatList) {
                StoreManager.stateLinkStore.statListData = R.clone(this.statListData);
            }
            this.buildTreeData(this.props);
            this.onTreeSearchChange(this.searchValue, this.treeData);
            this.setState({treeData: this.treeData, loading: false});
        });
    }

    /**
     * 获取搜索内容, 调用 findExpandedNode 寻找需要打开的节点
     * @param {*} value list 搜索框
     * @param {Array<StatListTree>} treeData 当前树数据
     */
    @autobind
    onTreeSearchChange(value: string, treeData: Array<StatListTree>) {
        let expandedKeys;
        if (isEmpty(value)) {
            expandedKeys = this.allNodeKeys;
        } else {
            expandedKeys = this.findExpandedNode(treeData, value);
            // 打开需要打开的节点

        }
        this.setState({expandedKeys: expandedKeys});
    }

    /**
     * 递归寻找需要符合搜索目标, 需要打开的节点
     * @param {*} tree 节点树
     * @param {*} value 搜索的目标值
     * @returns 一个数组, 存放需要打开的节点的 key 值
     */
    @autobind
    findExpandedNode(tree: Array<StatListTree>, value: string): string[] {
        if (isEmpty(value)) {
            return [];
        }
        let expandedKeys = [];
        tree.forEach((item, index) => {
            const tags: Map<string, string> = get(item, "item.tags", new Map());
            tags.forEach((tagValue: string, tagKey: string) => {
                if (tagValue.toLowerCase().indexOf(value.toLowerCase()) > -1) {
                    expandedKeys.push(item.key);
                }
            });

            if (item.children && item.children.length > 0) {
                expandedKeys.push(...this.findExpandedNode(item.children, value));
            }
        });
        // return expandedKeys.length > 0 ? expandedKeys : this.allNodeKeys;
        return expandedKeys;
    }

    /**
     * 设置展开节点
     * @param {*} expandedKeys 需要展开的节点
     */
    @autobind
    onListExpand(expandedKeys: string[], info: any) {
        const selectedKey = info.node.dataRef.key;
        let keys = [];
        if (info.expanded) {
            keys = [...expandedKeys, selectedKey];
        } else {
            keys = expandedKeys.filter(item => item.indexOf(selectedKey) === -1 && item.indexOf("\",\"") === -1); // 不需要子元素包含 `/` 或者包含选中 key 的 item
        }
        this.setState({
            expandedKeys: keys
        });
    }

    /**
     * 获取点击的节点, 生成 listsTagFilter
     * 调用 refreshMetric 插入 charts
     * @param {*} keys 选择的节点的 key 值
     */
    @autobind
    listNodeSelect(keys: Array<string>, antTreeNodeEvent: any) {
        const tags: Map<string, any> = new Map();
        const displayField = this.target.groupByKeys;
        const needDeleteParams = this.props.levelKeys.slice(0);
        if (antTreeNodeEvent.selected) {
            let selectedNode = antTreeNodeEvent.selectedNodes[0].dataRef as StatListTree;
            if (this.isOneLevel) {
                const tagKey = this.props.levelKeys[0];
                tags.set(tagKey, selectedNode.item.tags.get(tagKey));
            } else {
                const tagKeyOne = this.props.levelKeys[0];
                const tagKeyTwo = this.props.levelKeys[1];
                if (selectedNode.children && selectedNode.children.length > 0) {
                    //  current select level one
                    tags.set(tagKeyOne, displayField ? selectedNode.item.tags.get(tagKeyOne) : selectedNode.title);
                } else {
                    //  current select level two
                    // 取正确的 tag放到URL中
                    tags.set(tagKeyOne, displayField ? selectedNode.item.tags.get(tagKeyOne) : selectedNode.fatherValue);
                    tags.set(tagKeyTwo, displayField ? selectedNode.item.tags.get(tagKeyTwo) : selectedNode.title);
                }
            }

            // 针对 redis 进行的特殊处理
            if (tags.has("redis")) {
                const allRedis = tags.get("redis") || "";
                tags.set("redis", uniq(allRedis.split(",")));
                tags.set("redisName", selectedNode.item.tags.get("redisName"));
            }
        }
        if (needDeleteParams.indexOf("redis") > -1) {
            needDeleteParams.push("redisName");
        }
        this.urlParamStore.changeURLParams(MapToObject(tags), needDeleteParams);
    }

    showRatio(content: any, titleWithPopover: any, items: any) {
        let item: StatListItem = items.item;
        let historyValue = item.historyValue;
        let value = item.value;
        if (item.showRatio) {
            let title = "";
            let icon;
            let timeShift = Math.abs(item.timeShift);
            if (!historyValue || historyValue == 0) {
                title = "无历史数据";
                icon = <IconFont type="icon-infinite" style={{color: "#da003d", fontSize: "12px", paddingLeft: "3px"}}/>;
            } else {
                let ratio = (value - historyValue) * 100 / historyValue;
                if (ratio < 0) {
                    let percentAbs = DataFormatter.transformPercent(-ratio, 3);
                    if (value == 0) {
                        title = "当前时间端无数据，" + timeShift + "天前数据：" + historyValue;
                        icon = (
                            <IconFont
                                type="icon-infinite"
                                style={{color: "#00ab69", fontSize: "12px", paddingLeft: "3px"}}
                            />
                        );
                    } else {
                        title = "环比" + timeShift + "天前,下降" + percentAbs;
                        icon = (
                            <IconFont
                                type="icon-circle"
                                style={{color: "#00ab69", fontSize: "12px", paddingLeft: "3px"}}
                            />
                        );
                    }
                } else if (ratio > 0) {
                    let percent = DataFormatter.transformPercent(ratio, 3);
                    title = "环比" + timeShift + "天前,增加" + percent;
                    icon =
                        <IconFont type="icon-circle" style={{color: "#da003d", fontSize: "12px", paddingLeft: "3px"}}/>;
                } else {
                    title = "环比无变化";
                    icon = <IconFont type="icon-circle" style={{fontSize: "12px", paddingLeft: "3px"}}/>;
                }
            }
            return (
                <span className="stat-list-title">
                    {content}
                    {item &&
                    <Tooltip title={title}>
                        <span className={"stat-list-title-value " + (titleWithPopover ? "arrow-right arrow_box" : "")}>
                            {DataFormatter.formatterByUnit(this.target.statListUnit, item.value, 2)}
                            {icon}
                            {titleWithPopover && <div className="background-triangle"/>}
                        </span>
                    </Tooltip>
                    }
                    </span>
            );
        }
        return (
            <span className="stat-list-title">
                        {content}
                {item &&
                <span className={"stat-list-title-value " + (titleWithPopover ? "arrow-right arrow_box" : "")}>
                        {DataFormatter.formatterByUnit(this.target.statListUnit, item.value, 2)}
                    {titleWithPopover && <div className="background-triangle"/>}
                        </span>}
                    </span>
        );
    }

    renderTreeNodes(data: Array<StatListTree>): Array<{ visible: boolean, node: any }> {
        /* add this filter, to fix the bug: single-layer haven't filter input.
         */
        if (this.state.expandedKeys != this.allNodeKeys && this.isOneLevel) {
            data = data.filter(item => !this.state.expandedKeys || this.state.expandedKeys.indexOf(item.key) > -1);
        }
        const displayField = this.target.groupByKeys || [];
        const nodes = [];
        let i = 0;
        for (const item of data) {
            if (i > 200) {
                break;
            }
            i++;
            const searchValue = this.searchValue;
            let titleStr = item.title;
            if (!isEmpty(searchValue)) {
                const tags: Map<string, string> = get(item, "item.tags", new Map());
                tags.forEach((value: string, key: string) => {
                    if (displayField.length > 0 &&
                        displayField.indexOf(key) === -1 &&
                        this.props.levelKeys.indexOf(key) >= 0 &&
                        value.toLowerCase().indexOf(searchValue.toLowerCase()) > -1) {
                        titleStr += ` (${value})`;
                    }
                });
            }

            // 标题高亮部分
            // Ignore case search
            const searchIndex = titleStr.toLowerCase().indexOf(searchValue.toLowerCase());

            if (item.children && item.children.length > 0) {
                if (this.allNodeKeys.indexOf(item.key) === -1) {
                    this.allNodeKeys.push(item.key); // 收集所有一级 key
                }
                const children = this.renderTreeNodes(item.children);
                if (!(searchValue.length > 0 && searchIndex === -1) || !isEmpty(children)) {
                    nodes.push(
                        <TreeNode
                            title={this.getTitle(titleStr, searchIndex, searchValue, item)}
                            key={item.key}
                            // @ts-ignore
                            dataRef={item}
                        >
                            {children}
                        </TreeNode>
                    );
                }
            } else {
                // 搜索是否符合
                if (!(searchValue.length > 0 && searchIndex === -1)) {
                    nodes.push(
                        <TreeNode
                            title={this.getTitle(titleStr, searchIndex, searchValue, item)}
                            key={item.key}
                            // @ts-ignore
                            dataRef={item}
                        />
                    );
                }
            }
        }
        return nodes;
    }

    @autobind
    getTitle(titleStr: string, searchIndex: number, searchValue: string, item: StatListTree) {
        const beforeStr = titleStr.substr(0, searchIndex);
        const afterStr = titleStr.substr(searchIndex + searchValue.length);

        const titleWithPopover = this.props.hasLink(item);

        const content = searchIndex > -1
            ? (
                <span className="stat-list-title-content">{beforeStr}<span style={{color: "#f50"}}>
                    {titleStr.substr(searchIndex, searchValue.length)}</span>{afterStr}</span>
            )
            : (<span className="stat-list-title-content">{titleStr}</span>);
        const title = (this.showRatio(content, titleWithPopover, item));

        let realTitle;
        if (titleWithPopover) {
            realTitle = (
                <Popover placement="right" content={this.props.popover(item)}>
                    {title}
                </Popover>
            );
        } else {
            realTitle = title;
        }
        return realTitle;
    }

    buildOneLevel(data: Array<StatListItem>, level1: string) {
        return data.filter(item => {
            if (item.tags.get(level1)) {
                return true;
            } else {
                console.warn("StatListItem: ", item, " has no ", level1, " in its tags. WILL IGNORE it!");
                return false;
            }
        }).sort((a, b) => this.sortOrder ? a.value - b.value : b.value - a.value)
            .map(item => new StatListTree(item.tags.get(level1), item.key, item, null));
    }

    buildTwoLevel(data: Array<StatListItem>, level1: string, level2: string) {
        let levelMap: Map<string, Array<StatListItem>> = new Map();
        data.filter(item => {
            // 过滤出 tags 包含 Level1 的 item
            if (item.tags.get(level1)) {
                return true;
            } else {
                console.warn("StatListItem: ", item, " has no ", level1, " in its tags. WILL IGNORE it!");
                return false;
            }
        }).forEach((oneData) => {
            // 构建出以 Level1 分类的 Map（每个 key 包含了很多二级 item）
            const levelOneValue = oneData.tags.get(level1);
            if (levelMap.has(levelOneValue)) {
                levelMap.get(levelOneValue).push(oneData);
            } else {
                levelMap.set(levelOneValue, [oneData]);
            }
        });
        let treeData: Array<StatListTree> = [];
        let index = 0;
        levelMap.forEach((value, key) => {
            let childrenTree: Map<string, Array<StatListItem>> = new Map<string, Array<StatListItem>>();
            value.sort((a, b) => this.sortOrder ? a.value - b.value : b.value - a.value)
                .forEach(item => {
                    const levelTowValue = item.tags.get(level2);
                    // let statListTree: StatListTree = new StatListTree(levelTowValue, item.key, item, null, key);
                    if (childrenTree.get(levelTowValue)) {
                        childrenTree.get(levelTowValue).push(item);
                        // this.mergeStatItemTree(childrenItem, item);
                    } else {
                        childrenTree.set(levelTowValue, [item]);
                    }
                });
            let children: Array<StatListTree> = [];

            childrenTree.forEach((childrenItem, levelTowValue) => {
                let item = this.mergeStatItemTree(childrenItem, key);
                children.push(new StatListTree(levelTowValue, item.key, item, null, key));
            });
            if (level1 === "redisName" || level2 === "redisName") {
                const allRedis = uniq(value.map(v => v.tags.get("redis")).join(",").split(",")).join(",");
                value[0].tags.set("redis", allRedis);
            }
            // 两层中的StatList中的Item也放入child[0]作为它的item，以便能够取到真正的tag
            treeData.push(new StatListTree(key, "level-0-" + index, value[0], children));
            index++;
        });
        // sort level one
        treeData.sort((a, b) => this.sortOrder ? a.item.value - b.item.value : b.item.value - a.item.value);
        return treeData;
    }

    mergeStatItemTree(items: Array<StatListItem>, parentName: string): StatListItem {
        let length = items.length;
        let flag: boolean = this.target.fields.toString().indexOf("t_sum") >= 0;
        let item: StatListItem = items[0];
        for (let i = 1; i < length; i++) {
            let tempItem: StatListItem = items[i];
            if (tempItem.value) {
                item.value = get(item, "value", 0) + get(tempItem, "value", 0);
            }
            if (tempItem.historyValue) {
                item.historyValue = get(item, "historyValue", 0) + get(tempItem, "historyValue", 0);
            }
            if (tempItem.timeShift) {
                item.timeShift = tempItem.timeShift;
            }
        }
        if (flag) {
            if (item.value) {
                item.value = item.value / length;
            }
            if (item.historyValue) {
                item.historyValue = item.historyValue / length;
            }
        }

        if (item.value) {
            item.value = Number.parseFloat(item.value.toFixed(4));
        }
        if (item.historyValue) {
            item.historyValue = Number.parseFloat(item.historyValue.toFixed(4));
        }
        if (item.tags.get("redisName")) {
            const allRedis = items.map(i => {
                const redisName = i.tags.get("redisName");
                const redis = (i.tags.get("redis") || "") as string;

                return (redisName === parentName) ? redis : "";
            }).filter(Boolean);
            item.tags.set("redis", allRedis.join(","));
        }
        return item;
    }

    getSelectedKeys(): string[] {
        const {treeData} = this.state;
        if (isEmpty(treeData)) {
            return [];
        }
        const values = [];
        const {levelKeys} = this.props;
        for (const levelKey of levelKeys) {
            const value = this.urlParamStore.getValues(levelKey);
            const v = value.join(",");
            if (!isEmpty(v)) {
                values.push({key: levelKey, value: v});
            }
        }
        if (levelKeys.indexOf("redis") > -1) {
            values.push({key: "redisName", value: this.urlParamStore.getValue("redisName")});
        }
        if (isEmpty(values)) {
            return [];
        } else {
            const selectedKeys = [];
            treeData.forEach((value, key) => {
                const tags = value.item.tags;
                const children = value.children;
                if (!isEmpty(children)) {
                    children.forEach(child => {
                        const isRedis = values.findIndex(item => item.key === "redis") > -1;
                        if (isRedis &&
                            tags.get("redisName") === values[values.length - 1].value &&
                            // values.length > 2 是为了判断是否拥有 command 字段（也就是判断点击的时候是第二层）
                            values.length > 2 &&
                            values[1] && child.item.tags.get(values[1].key) === values[1].value
                        ) {
                            selectedKeys.push(child.item.key);
                        }
                        if (!isRedis && tags.get(values[0].key) === values[0].value && values[1] && child.item.tags.get(values[1].key) === values[1].value) {
                            selectedKeys.push(child.item.key);
                        }
                    });
                }
                if (isEmpty(selectedKeys)) {
                    if (tags.get(values[0].key) === values[0].value) {
                        selectedKeys.push(value.key);
                    }
                }
            });
            return selectedKeys;
        }
    }

    @autobind
    getLevelKeyParam(params: Array<any>, key: string): string {
        const index = R.findIndex(R.propEq("key", key))(params);
        if (index < 0) {
            return "";
        }
        return params[index].value;
    }

    render() {
        let expandedKeys = [];
        const {treeData, loading} = this.state;
        if (loading) {
            return (<Spin className="stat-list-spin-container" tip="数据加载中..." style={{display: "flex"}}/>);
        } else if (isEmpty(treeData)) {
            return (
                <p className="stat-list-no-data"><ExclamationOutlined style={{fontSize: 18}}/>暂无数据</p>
            );
        } else {
            expandedKeys = this.state.expandedKeys;
            return (
                <Tree
                    className="side-stat-list-tree"
                    autoExpandParent={true}
                    selectedKeys={this.getSelectedKeys()}
                    defaultExpandAll={true}
                    expandedKeys={expandedKeys}
                    onExpand={this.onListExpand}
                    onSelect={this.listNodeSelect}
                >
                    {this.renderTreeNodes(treeData)}
                </Tree>
            );
        }
    }
}
