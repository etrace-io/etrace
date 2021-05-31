export class HardWareRule {
    id?: number;
    name?: string;
    regexPattern?: string;
    status?: string;
    createdBy?: string;
    updatedBy?: string;
    createdAt?: number;
    updatedAt?: number;
    parentId?: number;
    children?: Array<HardWareRule>;
}