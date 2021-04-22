import {Delete, GetAndParse, GetAndParseAsArray, Post} from "../../utils/api";
import {JsonObject, JsonProperty} from "json2typescript";
import {CURR_API} from "$constants/API";

export class TokenService {

    public static async adminDeleteTokenLog(id: number): Promise<any> {
        let url = CURR_API.monitor + "/token/admin/delete?id=" + id;
        return Delete(url, null);
    }

    public static async adminUpdateTokenLog(log: ApplyTokenLog): Promise<any> {
        let url = CURR_API.monitor + "/token/admin/audit";
        return Post(url, log);
    }

    public static async adminManualCreate(psncode: string): Promise<any> {
        let url = CURR_API.monitor + "/token/admin/create-token?psncode=" + psncode;
        return Post(url, null);
    }

    public static async adminQueryAllTokenApplyLog(auditStatus: ApplyTokenAuditStatus): Promise<ApplyTokenLogSearchResult> {
        let url = CURR_API.monitor + "/token/admin/apply/search?auditStatus=" + (auditStatus ? auditStatus : "");
        return GetAndParse(url, ApplyTokenLogSearchResult, null);
    }

    public static async queryUserToken(errHandler: any): Promise<ApiToken> {
        let url = CURR_API.monitor + "/token/user/findToken";
        return GetAndParse(url, ApiToken, null, errHandler);
    }

    public static async apply(): Promise<any> {
        let url = CURR_API.monitor + "/token/user/apply";
        return Post(url, null);
    }

    public static async adminQueryAllToken(): Promise<ApiToken[]> {
        let url = CURR_API.monitor + "/token/admin/all-token";
        return GetAndParseAsArray(url, ApiToken, null);
    }

    public static async adminUpdateApiToken(log: ApiToken): Promise<any> {
        let url = CURR_API.monitor + "/token/admin/update-token";
        return Post(url, log);
    }
}

export enum ApplyTokenAuditStatus {
    NOT_AUDIT = "NOT_AUDIT",
    AGREE = "AGREE",
    REFUSED = "REFUSED"
}

export enum TokenStatus {
    Active = "Active",
    Inactive = "Inactive",
    WAIT_AUDIT = "WAIT_AUDIT",
}

@JsonObject("ApplyTokenLogSearchResult")
export class ApplyTokenLogSearchResult {
    @JsonProperty()
    total: number = undefined;

    @JsonProperty()
    results: Array<ApplyTokenLog> = undefined;
}

@JsonObject("ApplyTokenLog")
export class ApplyTokenLog {
    @JsonProperty()
    id: number = undefined;
    @JsonProperty()
    updatedAt: number = undefined;
    @JsonProperty()
    createdAt: number = undefined;
    @JsonProperty()
    userCode: string = undefined;

    @JsonProperty()
    applyReason: string = undefined;
    @JsonProperty()
    auditOpinion: string = undefined;
    @JsonProperty()
    createdBy: string = undefined;
    @JsonProperty()
    updatedBy: string = undefined;
    @JsonProperty()
    auditStatus: ApplyTokenAuditStatus = undefined;
    @JsonProperty()
    status: TokenStatus = undefined;

    @JsonProperty()
    onedeptname: string = undefined;
    @JsonProperty()
    fatdeptname: string = undefined;
    @JsonProperty()
    deptname: string = undefined;

    // for ui
    shouldUpdate: boolean = false;
}

@JsonObject("ApiToken")
export class ApiToken {
    @JsonProperty("id", Number, true)
    id: number = undefined;
    @JsonProperty("updatedAt", Number, true)
    updatedAt: number = undefined;
    @JsonProperty("createdAt", Number, true)
    createdAt: number = undefined;
    @JsonProperty("createdBy", String, true)
    createdBy: string = undefined;
    @JsonProperty("updatedBy", String, true)
    updatedBy: string = undefined;
    @JsonProperty()
    userCode: string = undefined;
    @JsonProperty()
    status: TokenStatus = undefined;

    @JsonProperty("cid", String, true)
    cid: TokenStatus = undefined;
    @JsonProperty("token", String, true)
    token: TokenStatus = undefined;
    @JsonProperty("alwaysAccess", Boolean, true)
    alwaysAccess: boolean = undefined;

    @JsonProperty("psnname", String, true)
    psnname: string = undefined;

    @JsonProperty("onedeptname", String, true)
    onedeptname: string = undefined;
    @JsonProperty("fatdeptname", String, true)
    fatdeptname: string = undefined;
    @JsonProperty("deptname", String, true)
    deptname: string = undefined;
}
