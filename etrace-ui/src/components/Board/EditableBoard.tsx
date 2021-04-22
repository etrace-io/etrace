import * as BoardService from "$services/BoardService";
import StoreManager from "$store/StoreManager";
import {Alert} from "antd";
import {handleError} from "$utils/notification";
import {UserKit} from "$utils/Util";
import BoardViewPage from "$containers/Board/BoardViewPage";
import React, {useEffect, useState} from "react";
import EMonitorLoading from "$components/EMonitorLoading/EMonitorLoading";
import {EMonitorSection} from "$components/EMonitorLayout";
import useUser from "$hooks/useUser";

const EditableBoard: React.FC<{
    globalId: string;
    metricType?: string;
    prefixKey?: string;
    /* options */
    hideStarButton?: boolean;
    customFunctionItem?: React.ReactNode;
    hideTimePicker?: boolean;
    hideToolsBar?: boolean;
    noTimeRefresh?: boolean;
}> = props => {
    const {hideStarButton, noTimeRefresh, hideTimePicker, hideToolsBar, customFunctionItem} = props;
    const {globalId, metricType, prefixKey} = props;

    const [loading, setLoading] = useState<boolean>(true);
    const [isExist, setIsExist] = useState<boolean>(false);

    useEffect(() => {
        if (globalId) {
            loadChartConfig(globalId);
        }
    }, [globalId]);

    const loadChartConfig = id => {
        setLoading(true);
        BoardService.search({globalId: id, status: "Active"}).then((results: any) => {
            return results.results.length === 0
                ? Promise.resolve()
                : BoardService.get(results.results[0].id).then(boardConfig => {

                    boardConfig.charts.forEach(chart => {
                        chart.targets.forEach(target => {
                            if (metricType) {
                                target.metricType = metricType;
                            }
                            if (prefixKey) {
                                target.prefixVariate = prefixKey;
                            }
                        });
                    });

                    StoreManager.boardStore.setBoard(boardConfig);

                    setIsExist(true);
                }).catch(err => {
                    handleError(err, "获取面板");
                });

        }).finally(() => {
            setLoading(false);
        });
    };

    const user = useUser();
    const hideEditButton = !UserKit.isDeveloper(user);

    if (loading) {
        return <EMonitorLoading/>;
    }

    return isExist
        ? (
            <BoardViewPage
                hideStarButton={hideStarButton}
                hideEditButton={hideEditButton}
                hideBoardChoose={true}
                noTimeRefresh={noTimeRefresh}
                hideTimePicker={hideTimePicker}
                hideToolsBar={hideToolsBar}
                customFunctionItem={customFunctionItem}
            />
        )
        : (
            <EMonitorSection.Item>
                <Alert
                    message={<span>未找到名为<code>"{globalId}"</code>的看板，请先新增该看板</span>}
                    type="warning"
                    showIcon={true}
                />
            </EMonitorSection.Item>
        );
};

export default EditableBoard;
