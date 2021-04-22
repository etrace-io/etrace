import {action, observable} from "mobx";

export class OrderByStore {
    @observable public sort: string = "DESC";
    @observable public limit: number = 50;
    @observable public type: string = "t_max";

    public setSort(value: string) {
        this.sort = value;
    }

    public setLimit(value: number) {
        this.limit = value;
    }

    @action
    public getOrderBy(value: string): string {
        return value + " " + this.sort + " limit " + this.limit;
    }
}
