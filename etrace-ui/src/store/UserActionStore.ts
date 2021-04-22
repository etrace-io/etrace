import {observable, runInAction, toJS} from "mobx";
import * as BoardService from "../services/BoardService";

export class UserActionStore {

    @observable public favoriteBoards: Array<any> = [];
    @observable public viewBoards: Array<any> = [];
    @observable public allFavoriteBoards: Array<any> = [];
    @observable public allViewBoards: Array<any> = [];

    public async loadUserAction() {
        const boards: any = await BoardService.getFavoriteBoards(99);
        if (boards) {
            const favorites: Array<any> = boards.favoriteBoards;
            const views: any = boards.viewBoards;

            runInAction(() => {
                this.favoriteBoards = favorites && favorites.length > 0 ? favorites : [];
                this.viewBoards = views && views.length > 0 ? views.reverse().slice(0, 15) : []; // 最近浏览保存 15 个
            });
        }
    }

    public async loadAllUserAction() {
        const boards: any = await BoardService.getFavoriteBoards();
        if (boards) {
            const favorites: Array<any> = boards.favoriteBoards;
            const views: any = boards.viewBoards;

            runInAction(() => {
                if (favorites && favorites.length > 0) {
                    this.allFavoriteBoards = favorites;
                }
                if (views && views.length > 0) {
                    this.allViewBoards = views;
                }
            });
        }
    }

    public getAllFavoriteBoards() {
        return toJS(this.allFavoriteBoards);
    }

    public getAllViewBoards() {
        return toJS(this.allViewBoards);
    }
}
