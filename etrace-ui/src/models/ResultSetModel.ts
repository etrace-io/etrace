import {ResultModel} from "./ResultModel";
import {ConvertFunctionModel} from "../utils/ConvertFunction";
import {TagFilter} from "./ChartModel";

export class ResultSetModel {
    errorMsg?: string;
    queryType?: string;
    functions?: Array<ConvertFunctionModel>;
    results: ResultModel;
    metricType?: string;
    name?: string;
    tagFilters?: Array<TagFilter>;
}