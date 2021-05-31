import {action, observable, runInAction} from "mobx";
import * as DataAppService from "../services/DataAppService";
import {browserHistory} from "$utils/UtilKit/SystemKit";

export class DataAppStore {

    @observable public dataAppId: number;
    @observable public dataApp: any = {};
    @observable public dashboards: Array<any> = [];
    @observable public selectedBoard: any;
    @observable public initialize: boolean;
    @observable public current: string;

    @action
    public register(dataAppId: number, history: any) {
        this.dataAppId = dataAppId;
        this.loadDataAppData(history);
    }

    public init() {
        this.dataApp = {};
        this.dashboards = [];
        this.current = null;
        this.selectedBoard = null;
        this.dataAppId = null;
    }

    public loadDataAppData(history: any) {
        this.initialize = true;
        const {search} = browserHistory.location;
        const oldSearchParams = new URLSearchParams(search);
        DataAppService.get(this.dataAppId).then(data => {
            runInAction(() => {
                this.dataApp = data;
                if (this.dataApp && this.dataApp.dashboards) {
                    this.dashboards = this.dataApp.dashboards;
                    let dashboard = this.dataApp.dashboards[0];
                    let dataApp = "/app/" + this.dataAppId + "/board/";
                    if (this.current.indexOf(dataApp) >= 0) {
                        let dashboardId = Number.parseInt(this.current.substring(dataApp.length));
                        let dashboards = this.dataApp.dashboards.filter(value => value.id == dashboardId);
                        if (dashboards) {
                            this.selectedBoard = dashboards[0];
                            let pathname = "/app/" + this.dataAppId + "/board/" + this.selectedBoard.id;
                            history.replace({pathname: pathname, search: oldSearchParams.toString()});
                        }
                    } else {
                        let pathname = "/app/" + this.dataAppId + "/board/" + dashboard.id;
                        this.selectedBoard = dashboard;
                        this.current = pathname;
                        history.replace({pathname: pathname, search: oldSearchParams.toString()});
                    }

                    // }
                }
                this.initialize = false;
            });
        }).catch(err => {
            this.initialize = false;
        });
    }
}
