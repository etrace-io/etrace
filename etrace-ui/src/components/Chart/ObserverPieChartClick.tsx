import React from "react";
import {reaction} from "mobx";
import {get} from "lodash";
import StoreManager from "../../store/StoreManager";

interface ObserverPieChartClickProps {}
interface ObserverPieChartClickState {}

/**
 * Observer pie chart click, add pie groups to url params
 */
export default class ObserverPieChartClick extends React.Component<
    ObserverPieChartClickProps,
    ObserverPieChartClickState
> {
    private readonly disposer;
    constructor(props: ObserverPieChartClickProps) {
        super(props);

        this.disposer = reaction(
            () => StoreManager.chartEventStore.pieClickEvent,
            event => {
                const groups = get(event, "item.groups", null);
                if (groups) {
                    StoreManager.urlParamStore.changeURLParams(groups);
                }
            }
        );
    }

    componentWillUnmount() {
        if (this.disposer) {
            this.disposer();
        }
    }

    render() {
        return null;
    }
}
