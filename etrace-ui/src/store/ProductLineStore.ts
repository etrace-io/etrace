import {action, observable, reaction, toJS} from "mobx";
import * as DepartmentService from "../services/DepartmentService";
import * as ProductLineService from "../services/ProductLineService";
import * as ChartService from "../services/ChartService";
import * as BoardService from "../services/BoardService";
import * as DataAppService from "../services/DataAppService";
import {UserStore} from "./UserStore";
import {User} from "$models/User";
import {get} from "lodash";

export class ProductLineStore {
    userStore: UserStore;
    @observable public departments: Array<any> = [];
    @observable public departmentTree: Array<any> = [];
    @observable public reversalDepartmentTree: Array<any> = [];
    @observable public productLines: Array<any> = [];
    @observable public defaultCategory: Array<number> = [];
    @observable public selectedDepartments: any;
    @observable public selectedProductLines: any;
    @observable public type: string;
    @observable public status: string = "Active";
    @observable public departmentId: number;
    @observable public params: Map<string, any> = new Map();
    @observable public currentTab: string;
    @observable public dataApps: Array<any> = [];
    @observable public appTotal: number = 0;
    @observable public boards: Array<any> = [];
    @observable public boardTotal: number = 0;
    @observable public boardLists: Array<any> = [];
    @observable public charts: Array<any> = [];
    @observable public chartTotal: number = 0;
    @observable public boardLoading: boolean = false;

    constructor(userStore: UserStore) {
        this.userStore = userStore;
        reaction(
            () => this.departmentId,
            () => {
                this.loadProductLines();
            });
        reaction(
            () => toJS(this.params),
            () => {
                this.loadDepartment();
            });
        reaction(
            () => get(this.userStore.user, "userConfig.config", null),
            () => {
                this.defaultCategory = [get(this.userStore.user, "userConfig.config.departmentId", 0), get(this.userStore.user, "userConfig.config.productLineId", 0)];
            });
    }

    public init(tab: string) {
        this.selectedDepartments = null;
        this.selectedProductLines = null;
        this.productLines = [];
        this.departmentId = null;
        this.currentTab = tab;
    }

    public reset() {
        this.selectedDepartments = null;
        this.selectedProductLines = null;
        this.productLines = [];
        this.departments = [];
        this.departmentTree = [];
        this.reversalDepartmentTree = [];
        this.defaultCategory = [];
        this.params = new Map<string, any>();
        this.dataApps = [];
        this.boards = [];
        this.boardLists = [];
        this.charts = [];
        this.departmentId = null;
    }

    public setSelectedDepartments(value: any) {
        this.selectedDepartments = value;
    }

    public setSelectedProductLines(value: any) {
        this.selectedProductLines = value;
    }

    public getDepartments() {
        return toJS(this.departments);
    }

    public getProductLines() {
        return toJS(this.productLines);
    }

    async loadDepartment() {
        if (this.params.get("type") && this.params.get("status")) {
            let departments: Array<any> = await DepartmentService.fetch({
                type: this.params.get("type"),
                status: this.params.get("status")
            });
            this.departments = departments;
        } else {
            this.departments = [];
        }
    }

    async loadDepartmentTree() {
        const userConfig: any = get(this.userStore.user, "userConfig.config", {});
        if (this.departmentTree.length == 0) {
            const departments: Array<any> = await DepartmentService.getDefaultTreeData();
            let departmentTree = [];
            let reversalDepartmentTree = [];
            departments.forEach(item => {
                let department = {value: item.id, label: item.departmentName, children: []};
                item.productLines.forEach(pl => {
                    department.children.push({value: pl.id, label: pl.productLineName});
                    reversalDepartmentTree.push({
                        value: pl.id,
                        label: pl.productLineName,
                        parent: {value: item.id, label: item.departmentName}
                    });
                });
                departmentTree.push(department);
            });
            this.departmentTree = departmentTree;
            this.reversalDepartmentTree = reversalDepartmentTree;
        }
        if (this.userStore.user && this.userStore.user.roles.indexOf("VISITOR") < 0) {
            if (this.defaultCategory.length == 0) {
                if ((userConfig && !userConfig.departmentId) || !userConfig) {
                    this.buildDefaultCategory(this.reversalDepartmentTree);
                } else {
                    this.defaultCategory = [userConfig.departmentId, userConfig.productLineId];
                }
            }
        }
    }

    @action
    public getDepartmentTree() {
        return toJS(this.departmentTree);
    }

    public buildDefaultCategory(departments: Array<any>) {
        const user: User = this.userStore.user;
        const deptname = user.deptname;
        const length = deptname.length;
        let departmentsTemp = toJS(departments);
        for (let index = 0; index < length; index++) {
            let char = deptname.charAt(index);
            let temp = departmentsTemp.filter((value => {
                let label = value.label;
                return label.indexOf(char) >= 0;
            }));
            if (temp.length > 1) {
                departmentsTemp = toJS(temp);
            } else if (temp.length == 1) {
                const department = temp[0];
                this.defaultCategory = [department.parent.value, department.value];
                return;
            }
            if (index + 1 == length) {
                if (temp.length == 0) {
                    const department = departmentsTemp[0];
                    this.defaultCategory = [department.parent.value, department.value];
                } else {
                    const department = temp[0];
                    this.defaultCategory = [department.parent.value, department.value];
                }
            }
        }
    }

    @action
    getDefaultCategory() {
        return toJS(this.defaultCategory);
    }

    async loadProductLines() {
        if (this.departmentId && this.params.get("type")) {
            let productLines: Array<any> = await ProductLineService.getProductLineList(this.departmentId, {
                type: this.params.get("type"),
                status: this.params.get("status")
            });
            this.productLines = productLines;
        } else {
            this.productLines = [];
        }
    }

    public setStatus(value: string) {
        this.status = value;
    }

    public setType(value: string) {
        this.type = value;
    }

    public setParams(key: string, value: string) {
        this.params.set(key, value);
    }

    public setDepartmentId(value: number) {
        this.departmentId = value;
    }

    public setCurrentTab(value: string) {
        this.currentTab = value;
    }

    public async searchCharts(param: any) {
        this.boardLoading = true;
        if (this.selectedDepartments) {
            param.departmentId = this.selectedDepartments.departmentId;
        } else {
            delete param.departmentId;
        }
        if (this.selectedProductLines) {
            param.productLineId = this.selectedProductLines.productLineId;
        } else {
            delete param.productLineId;
        }
        let charts: any = await ChartService.searchByGroup(param);
        if (charts) {
            this.charts = charts.results;
            this.chartTotal = charts.total;
        } else {
            this.charts = [];
            this.chartTotal = 0;
        }
        this.boardLoading = false;
    }

    public async searchBoards(path: string, param: any) {
        this.boardLoading = true;
        if (this.selectedDepartments) {
            param.departmentId = this.selectedDepartments.departmentId;
        } else {
            delete param.departmentId;
        }
        if (this.selectedProductLines) {
            param.productLineId = this.selectedProductLines.productLineId;
        } else {
            delete param.productLineId;
        }
        let boards: any = await BoardService.searchBoards(path, param);
        if (boards) {
            this.boards = boards.results;
            this.boardTotal = boards.total;
        } else {
            this.boards = [];
            this.boardTotal = 0;
        }
        this.boardLoading = false;
    }

    public async searchBoardList(param: any) {
        let boards: any = await BoardService.search(param);
        if (boards) {
            this.boardLists = boards.results;
        } else {
            this.boardLists = [];
        }
    }

    public async searchApps(path: string, param: any) {
        this.boardLoading = true;
        if (this.selectedDepartments) {
            param.departmentId = this.selectedDepartments.departmentId;
        } else {
            delete param.departmentId;
        }
        if (this.selectedProductLines) {
            param.productLineId = this.selectedProductLines.productLineId;
        } else {
            delete param.productLineId;
        }
        let dataApps: any = await DataAppService.searchByGroup(path, param);
        if (dataApps) {
            this.dataApps = dataApps.results;
            this.appTotal = dataApps.total;
        } else {
            this.dataApps = [];
            this.appTotal = 0;
        }
        this.boardLoading = false;
    }
}