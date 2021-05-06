import {reaction} from "mobx";
import {StatList} from "./StatList";
import {Card, Input, Radio} from "antd";
import StoreManager from "$store/StoreManager";
import React, {ReactNode, useEffect, useRef, useState} from "react";
import {StatListTree, Target, Targets} from "$models/ChartModel";
import {ArrowDownOutlined, ArrowUpOutlined} from "@ant-design/icons/lib";

const TAB_SEARCH_PARAMS_KEY = "t";

/**
 * based on Tree Component: https://ant.design/components/tree-cn/
 * 可搜索的树。
 */
const MetricStatList: React.FC<{
    target?: Target; // target 和 targets 必须其中配置一个
    targets?: Targets; // 设置多个 target 需要切换显示；可以和 target 并存，但是该字段会被忽略
    inputPlaceholder: string;

    keepStatList?: boolean;

    hasLink?: (item: StatListTree) => boolean;
    popover?: (item: StatListTree) => string | ReactNode;
}> = props => {
    const {inputPlaceholder, target, targets, keepStatList, hasLink, popover} = props;
    const {urlParamStore} = StoreManager;

    const isMount = useRef<boolean>(false);
    const [ascOrder, setAscOrder] = useState<boolean>(); // 是否升序
    const [targetType, setTargetType] = useState<string>();
    const [currTarget, setCurrTarget] = useState<Target>(); // 当前展示 target
    const [levelKeys, setLevelKeys] = useState<string[]>();

    const [searchValue, setSearchValue] = useState<string>("");

    useEffect(() => {
        if (!levelKeys || isMount.current) {
            return;
        }
        isMount.current = true;
        setSearchValue(levelKeys[1] === "redis" || levelKeys[0] === "redis"
            ? urlParamStore.getValue("command")
            || urlParamStore.getValue("redisName")
            || ""
            : urlParamStore.getValue(levelKeys[1])
            || urlParamStore.getValue(levelKeys[0])
            || ""
        );
    }, [levelKeys]);

    // 获取 url 中 type
    useEffect(() => {
        const disposer = reaction(
            () => urlParamStore.getValue(TAB_SEARCH_PARAMS_KEY),
            type => {
                if (targets) {
                    const targetKeys = Object.keys(targets);
                    setTargetType(type || targetKeys[0]);
                }
            },
            {fireImmediately: true}
        );

        return () => disposer();
    }, []);

    const handleStatListTypeChange = e => {
        const type = e.target.value;
        urlParamStore.changeURLParams({[TAB_SEARCH_PARAMS_KEY]: type});
    };

    useEffect(() => {
        // 设置默认 target
        const _currTarget = targets
            ? (targets[targetType] || targets[Object.keys(targets)[0]]).target
            : target;

        console.log("currTarget: useEffect", currTarget);

        setLevelKeys(_currTarget.groupBy || []);
        setCurrTarget(_currTarget);
    }, [target, targets, targetType]);

    console.log("currTarget: ", currTarget);

    const sortBtn = ascOrder
        ? <ArrowUpOutlined onClick={() => setAscOrder(v => !v)} style={{cursor: "pointer"}}/>
        : <ArrowDownOutlined onClick={() => setAscOrder(v => !v)} style={{cursor: "pointer"}}/>;

    const radioGroup = targets && Object.keys(targets).map(key => ({type: key, label: targets[key].label}));

    return (
        <div style={{height: "100%"}}>
            <Card className="side-stat-list" size="small">
                <div className="side-stat-list-search-box">
                    <Input
                        allowClear={true}
                        placeholder={`${inputPlaceholder}(最多显示前200个)`}
                        onChange={e => setSearchValue(e.target.value)}
                        value={searchValue}
                        addonAfter={sortBtn}
                    />

                    {/* Target 切换 */}
                    {radioGroup && (
                        <Radio.Group
                            className="side-stat-list-select-btn-group"
                            onChange={handleStatListTypeChange}
                            value={targetType}
                        >
                            {radioGroup.map(item => (
                                <Radio.Button key={item.type} value={item.type}>{item.label}</Radio.Button>
                            ))}
                        </Radio.Group>
                    )}
                </div>

                {currTarget && <StatList
                    target={currTarget}
                    keepStatList={keepStatList}
                    levelKeys={levelKeys}
                    hasLink={(item) => hasLink ? hasLink(item) : false}
                    popover={popover}
                    searchValue={searchValue}
                    sortOrder={ascOrder}
                />}
            </Card>
        </div>
    );
};

export default MetricStatList;
