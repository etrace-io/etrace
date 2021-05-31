import {get, set} from "lodash";
import {User} from "$models/User";
import {Theme} from "$constants/Theme";
import {computed, observable} from "mobx";
import * as UserService from "../services/UserService";

const R = require("ramda");

export class UserStore {
    @observable public coffeeToken: string;
    @observable public user: User = {};

    @computed
    get ssoToken() {
        return this.coffeeToken;
    }

    public setUser(value: User) {
        this.user = value;
        if (this.user && this.user.deptname == "监控框架组") {
            this.user.buDepartments = ["CI核心基础设施部"];
        }
    }

    @computed
    get getUser(): User {
        return this.user;
    }

    public getTheme(defaultTheme?: Theme): string {
        return Theme.Light; // 暂时下线黑色主题
        // return get(this.user, "userConfig.config.theme", defaultTheme || Theme.Light);
    }

    public saveUserConfig(userConfig: object) {
        const config = R.mergeDeepRight(get(this.user, "userConfig.config", {}), userConfig || {});
        set(this.user, "userConfig.config", config);
        UserService.saveUserConfig(config);
    }
}
