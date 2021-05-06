import {action, observable, toJS} from "mobx";
import * as MonitorEntityService from "../services/MonitorEntityService";
import {MonitorEntity} from "$services/MonitorEntityService";
import {isEmpty} from "$utils/Util";

const R = require("ramda");

export class MonitorEntityStore {
    @observable entities: Array<MonitorEntity> = [];
    @observable entityTree: Array<MonitorEntity> = [];
    loading = false;

    @action
    public async loadEntities() {
        if (isEmpty(this.entities) && !this.loading) {
            this.loading = true;
            this.entities = await MonitorEntityService.queryEntityByType("Entity", true);
            // MonitorApp.buildEntities(entityTree);
            this.entityTree = await MonitorEntityService.fecth(undefined, true);
            this.loading = false;
        }
    }

    public init() {
        this.entities = [];
        this.entityTree = [];
    }

    /**
     * 用于寻找对应 database 的 name 数组（用于展示）
     * @param target 目标 databaseName
     * @param entities 所有 entity
     */
    findEntity(target: string, entities: MonitorEntity[]) {
        if (!entities) { return []; }

        for (let entity of entities) {
            if (entity.type === "Entity") {
                if (entity.code === target) { return [entity.name]; }
            } else {
                const result = this.findEntity(target, entity.children);
                if (result.length > 0) { return [entity.name].concat(result); }
            }
        }

        return [];
    }

    public findEntityByCode(code: string) {
        const entity: MonitorEntity = R.find(R.propEq("code", code))(this.entities);
        return toJS(entity);
    }

    public getEntityTree(): MonitorEntity[] {
        return this.entityTree;
    }
}
