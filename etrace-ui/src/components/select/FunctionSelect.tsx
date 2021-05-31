import React from "react";
import {Input, Tooltip} from "antd";
import SeriesSelect from "./SeriesSelect";
import {Target} from "../../models/ChartModel";
import SeriesNameMention from "./SeriesNameMention";
import {CloseOutlined} from "@ant-design/icons/lib";
import {ConvertFunctionModel} from "../../utils/ConvertFunction";

import "./select.css";

const R = require("ramda");

interface FunctionSelectProps {
    index: number;
    funIndex: number;
    fun: ConvertFunctionModel;
    target: Target;
    changeFunction: any;
    removeFunction: any;
    chartUniqueId: string;
}

interface FunctionSelectState {
    options?: Array<string>;
    width?: number;
    fun: ConvertFunctionModel;
}

// const FunctionSelect: React.FC<FunctionSelectProps> = props => {
//     const {func} = props;
//
//     const {params} = func;
//     return (
//         <span className="function-select">
//             {func.name}(
//
//             {params.map((param, paramIndex) => ([
//                 param.display
//                     ? renderInput(func, paramIndex, param)
//                     : <a key={paramIndex} style={{minWidth: 20}} onClick={() => functionParamClick(paramIndex, param)}>{func.defaultParams[paramIndex]}</a>,
//                 // Params 分隔符
//                 paramIndex + 1 < params.length ? "," : ""
//             ]))}
//
//             )
//         <CloseOutlined onClick={this.removeFunction} style={{marginLeft: 3, fontSize: 12, cursor: "pointer"}}/>
//         </span>
//     );
// };
//
// const interface FunctionParamInputProps {
//
// }
//
// const FunctionParamInput: React.FC = props => {
//     if (fun.name === "aliasReplace" && paramIndex === 0) {
//         return (
//             <SeriesSelect
//                 key={paramIndex}
//                 value={fun.defaultParams[paramIndex]}
//                 chartUniqueId={this.props.chartUniqueId}
//                 fun={fun}
//                 paramIndex={paramIndex}
//                 onBlur={this.functionChange}
//             />
//         );
//     } else if (fun.name === "compute" && paramIndex === 0) {
//         return (
//             <Tooltip title={"不支持饼图和雷达图计算"}>
//                 <SeriesNameMention
//                     key={paramIndex}
//                     value={fun.defaultParams[paramIndex]}
//                     chartUniqueId={this.props.chartUniqueId}
//                     fun={fun}
//                     prefix={"$"}
//                     paramIndex={paramIndex}
//                     onBlur={this.functionChange}
//                 />
//             </Tooltip>
//         );
//     } else {
//         return (
//             <Input
//                 key={paramIndex}
//                 size="small"
//                 style={{minWidth: 80}}
//                 onBlur={() => this.functionParamBlur(paramIndex)}
//                 onChange={e => this.functionParamChange(paramIndex, param, e)}
//                 value={fun.defaultParams[paramIndex]}
//             />
//         );
//     }
// };

export class FunctionSelect extends React.Component<FunctionSelectProps, FunctionSelectState> {
    constructor(props: FunctionSelectProps) {
        super(props);
        this.state = {fun: R.clone(props.fun)};
    }

    componentWillReceiveProps(nextProps: Readonly<FunctionSelectProps>, nextContext: any): void {
        this.setState({fun: R.clone(nextProps.fun)});
    }

    functionParamClick = (paramIndex: number, param: any) => {
        const {fun} = this.state;
        const {target} = this.props;
        if (target.display != false) {
            param.display = true;
            const value = fun.defaultParams[paramIndex];
            param.width = value.length * 8 + 25;
            this.setState({fun: fun});
        }
    };

    functionParamChange = (paramIndex: number, param: any, event: any) => {
        const {fun} = this.state;
        const value = event.target.value;
        fun.defaultParams[paramIndex] = value;
        param.width = value.length * 8 + 25;
        this.setState({fun: fun});
    };

    functionParamBlur = (paramIndex: number) => {
        const {fun} = this.state;
        fun.params[paramIndex].display = false;
        this.setState({fun: fun});
        const {changeFunction, index, funIndex} = this.props;
        changeFunction(index, funIndex, R.clone(fun));
    };

    functionChange = (fun: ConvertFunctionModel) => {
        const {changeFunction, index, funIndex} = this.props;
        changeFunction(index, funIndex, R.clone(fun));
        this.setState({fun: fun});
    };

    removeFunction = () => {
        const {removeFunction, index, funIndex} = this.props;
        removeFunction(index, funIndex);
    };

    renderInput(fun: any, paramIndex: number, param: any) {
        if (fun.name === "aliasReplace" && paramIndex === 0) {
            return (
                <SeriesSelect
                    key={paramIndex}
                    value={fun.defaultParams[paramIndex]}
                    chartUniqueId={this.props.chartUniqueId}
                    fun={fun}
                    paramIndex={paramIndex}
                    onBlur={this.functionChange}
                />
            );
        } else if (fun.name === "compute" && paramIndex === 0) {
            return (
                <Tooltip title={"不支持饼图和雷达图计算"}>
                    <SeriesNameMention
                        key={paramIndex}
                        value={fun.defaultParams[paramIndex]}
                        chartUniqueId={this.props.chartUniqueId}
                        fun={fun}
                        prefix={"$"}
                        paramIndex={paramIndex}
                        onBlur={this.functionChange}
                    />
                </Tooltip>
            );
        } else {
            return (
                <Input
                    key={paramIndex}
                    size="small"
                    style={{
                        minWidth: 60,
                        width: param.width
                    }}
                    onBlur={() => this.functionParamBlur(paramIndex)}
                    onChange={e => this.functionParamChange(paramIndex, param, e)}
                    value={fun.defaultParams[paramIndex]}
                />
            );
        }
    }

    render() {
        const {fun} = this.state;
        const params = fun.params;
        const paramsLength = params.length;
        return (
            <span className="function-select">
                {[fun.name + "(", (
                    params.map((param, paramIndex) => {
                            let splits = "";
                            if (paramIndex + 1 < paramsLength) {
                                splits = ",";
                            }
                            return ([!param.display && (<a
                                key={paramIndex}
                                style={{minWidth: 20}}
                                onClick={() => this.functionParamClick(paramIndex, param)}
                            >{fun.defaultParams[paramIndex]}
                            </a>),
                                param.display && (
                                    this.renderInput(fun, paramIndex, param)
                                ), splits]);
                        }
                    )
                ), ")"]}
                <CloseOutlined onClick={this.removeFunction} style={{marginLeft: 3, fontSize: 12, cursor: "pointer"}} />
            </span>
        );
    }
}
