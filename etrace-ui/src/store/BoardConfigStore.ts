import {action, observable, runInAction, toJS} from "mobx";
import * as DepartmentService from "../services/DepartmentService";
import * as ChartService from "../services/ChartService";
import {isEmpty} from "$utils/Util";
import {UserStore} from "./UserStore";
import * as BoardService from "../services/BoardService";

export class BoardConfigStore {
    userStore: UserStore;
    @observable public department: any;
    @observable public productLine: any;
    @observable public departments: Array<any> = [];
    @observable public productLines: Map<number, Array<any>> = new Map<number, Array<any>>();
    @observable public boardConfig: Map<number, Array<any>> = new Map<number, Array<any>>();
    @observable public loading: boolean = false;
    @observable public loadingBoardConfig: boolean = false;
    @observable public selectedBoardConfig: Array<any> = [];

    constructor(userStore: UserStore) {
        this.userStore = userStore;
    }

    reload() {
        this.department = null;
        this.productLine = null;
        this.departments = [];
        this.productLines = new Map();
        this.boardConfig = new Map();
        this.selectedBoardConfig = [];
    }

    @action
    public init(type: string) {
        if (isEmpty(this.departments)) {
            this.loading = true;
            DepartmentService.getTreeData({type: type}).then(departments => {
                runInAction(() => {
                    let user = this.userStore.user;
                    let departmentId, productLineId;
                    if (user && user.userConfig && user.userConfig.config) {
                        departmentId = user.userConfig.config.departmentId;
                        productLineId = user.userConfig.config.productLineId;
                    }
                    departments.forEach(department => {
                        if (department.count > 0) {
                            this.departments.push(department);
                            let productLines = [];
                            department.productLines.forEach(value => {
                                if (value.count > 0) {
                                    productLines.push(value);
                                    if (productLineId && value.id == productLineId) {
                                        this.productLine = value;
                                    }
                                }
                            });
                            this.productLines.set(department.id, productLines);
                        }
                        if (departmentId && department.id == departmentId) {
                            this.department = department;
                        }
                    });
                    this.loading = false;
                });
            }).catch(err => {
                this.loading = false;
            });
        }
    }

    @action
    async loadConfigByType(productLineId: number) {
        if (!this.boardConfig.get(productLineId)) {
            this.loadingBoardConfig = true;
            let charts = await ChartService.search({productLineId: productLineId, pageSize: 10000});
            if (charts) {
                this.boardConfig.set(productLineId, charts.results);
            }
            this.loadingBoardConfig = false;
        }
    }

    getDepartments() {
        this.selectedBoardConfig = this.departments;
    }

    @action
    setDepartment(department: any) {
        this.department = department;
    }

    @action
    setProductLine(value: any) {
        this.productLine = value;
    }

    getProductLines(departmentId: number) {
        if (this.productLines) {
            this.selectedBoardConfig = this.productLines.get(departmentId);
        }
    }

    async getBoardConfig(productLineId: number, type: string) {
        if (!this.boardConfig.get(productLineId)) {
            this.loadingBoardConfig = true;
            if (type == "chart") {
                let charts = await ChartService.search({productLineId: productLineId, pageSize: 10000});
                if (charts) {
                    this.boardConfig.set(productLineId, charts.results);
                }
            } else if (type == "dashboard") {
                let dashboards = await BoardService.search({productLineId: productLineId, pageSize: 10000});
                if (dashboards) {
                    this.boardConfig.set(productLineId, dashboards.results);
                }
            }

            this.loadingBoardConfig = false;
        }
        this.selectedBoardConfig = this.boardConfig.get(productLineId);
    }

    @action
    getSelectedBoardConfig() {
        return toJS(this.selectedBoardConfig);
    }

    @action
    async getDataTree(type: string) {
        if (this.productLine) {
            await this.getBoardConfig(this.productLine.id, type);
        } else {
            if (this.department) {
                this.getProductLines(this.department.id);
            } else {
                this.getDepartments();
            }
        }
    }
}