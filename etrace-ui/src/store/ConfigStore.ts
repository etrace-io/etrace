import {observable, toJS} from "mobx";
import * as ConfigService from "../services/ConfigService";

export class ConfigStore {
    @observable configs: Map<string, any> = new Map<string, any>();

    loadConfig(key: string) {
        ConfigService.search({key: key}).then(value => {
            this.configs.set(key, value);
        });
    }

    getConfig(key: string) {
        return toJS(this.configs.get(key));
    }
}