export class TreeNodeModel {
    title: string;
    key: string;
    parentTitle: string;
    parentKey: string;
    data: number;

    constructor(title: string) {
        this.title = title;
        this.key = title;
    }
}