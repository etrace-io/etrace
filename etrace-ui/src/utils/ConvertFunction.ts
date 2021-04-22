export const INPUT_WIDTH: number = 60;

export enum ConvertFunctionEnum {
    ALIAS = "alias",
    ALIAS_PATTERN = "aliasPattern",
    ALIAS_REPLACE = "aliasReplace",
    ALIAS_PREFIX = "aliasPrefix",
    ALIAS_POSTFIX = "aliasPostfix",
    TIME_SHIFT = "timeShift",
    INTERVAL = "interval",
    COUNT_PS = "count_ps",
    COMPUTE = "compute",
    TARGET_DISPLAY = "target_display",
    LINE_FLAG = "line_flag",
}

export class ConvertFunctionModel {
    public static models: Array<ConvertFunctionModel> = [
        new ConvertFunctionModel(ConvertFunctionEnum.COUNT_PS, "count_ps", [], []),
        new ConvertFunctionModel(ConvertFunctionEnum.TARGET_DISPLAY, "target_display", [{name: "target_display", type: "boolean"}], [true]),
        new ConvertFunctionModel(ConvertFunctionEnum.LINE_FLAG, "line_flag", [{name: "line_flag", type: "string"}], ["A"]),
        new ConvertFunctionModel(ConvertFunctionEnum.ALIAS, "alias", [{name: "alias", type: "string"}], ["alias"]),
        new ConvertFunctionModel(ConvertFunctionEnum.ALIAS_PATTERN, "aliasPattern", [{name: "pattern", type: "string"}], ["pattern"]),
        new ConvertFunctionModel(ConvertFunctionEnum.ALIAS_REPLACE, "aliasReplace", [{name: "search", type: "string"}, {name: "replace", type: "string"}], ["search", "replace"]),
        new ConvertFunctionModel(ConvertFunctionEnum.ALIAS_PREFIX, "aliasPrefix", [{name: "prefix", type: "string"}], ["prefix"]),
        new ConvertFunctionModel(ConvertFunctionEnum.ALIAS_POSTFIX, "aliasPostfix", [{name: "postfix", type: "string"}], ["postfix"]),
        new ConvertFunctionModel(ConvertFunctionEnum.TIME_SHIFT, "timeShift", [{name: "amount", type: "string"}], ["-1d"]),
        new ConvertFunctionModel(ConvertFunctionEnum.INTERVAL, "interval", [{name: "interval", type: "string"}], ["10s"]),
        new ConvertFunctionModel(ConvertFunctionEnum.COMPUTE, "compute", [{name: "compute", type: "string"}, {name: "alias", type: "string"}], ["${a}", "alias"])
    ];

    modelEnum: ConvertFunctionEnum;
    name: string;
    params: Array<any>;
    defaultParams: Array<any>;
    display: boolean = false;
    width: number = INPUT_WIDTH;

    constructor(modelEnum: ConvertFunctionEnum, name: string, params: Array<any>, defaultParams: Array<any>) {
        this.modelEnum = modelEnum;
        this.name = name;
        this.params = params;
        this.defaultParams = defaultParams;
    }
}

export function findConvertFunctionModel(tp: ConvertFunctionEnum): ConvertFunctionModel {
    for (let model of ConvertFunctionModel.models) {
        if (model.modelEnum == tp) {
            return model;
        }
    }
    return null;
}

export function findConverFunctionModelByName(name: string): ConvertFunctionModel {
    for (let model of ConvertFunctionModel.models) {
        if (model.name == name) {
            return model;
        }
    }
    return null;
}

export const FUNCTIONS: any = {
    ALIAS: [
        findConvertFunctionModel(ConvertFunctionEnum.ALIAS),
        findConvertFunctionModel(ConvertFunctionEnum.ALIAS_PATTERN),
        findConvertFunctionModel(ConvertFunctionEnum.ALIAS_REPLACE),
        findConvertFunctionModel(ConvertFunctionEnum.ALIAS_PREFIX),
        findConvertFunctionModel(ConvertFunctionEnum.ALIAS_POSTFIX)
    ],
    SPECIAL: [
        findConvertFunctionModel(ConvertFunctionEnum.INTERVAL),
    ],
    TRANSFORM: [
        findConvertFunctionModel(ConvertFunctionEnum.TIME_SHIFT),
        findConvertFunctionModel(ConvertFunctionEnum.COUNT_PS)
    ]
};