import {observable} from "mobx";

class ChangeEventStore {
    @observable public selectedDomain: string;

    constructor() {
        this.selectedDomain = null;
    }

    public setSelectedDomain(name: string) {
        this.selectedDomain = name;
    }
}

export default new ChangeEventStore();
