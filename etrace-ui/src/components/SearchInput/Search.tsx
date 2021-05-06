import React from "react";
import {AutoComplete, Input} from "antd";

import HolmesStore from "../../store/HolmesStore";
import {SearchOutlined} from "@ant-design/icons/lib";

const Option = AutoComplete.Option;

export default class SearchInput extends React.Component<any, any> {
    private options: any;
    private selectedDomain: string = null;

    constructor(props: any) {
        super(props);
        this.state = {
            width: props.width,
            input: "",
            dataSource: []
        };
    }

    renderTitle = (title) => {
        return (
            <span>{title}
                <a
                    style={{float: "right"}}
                    href="https://www.google.com/search?q=antd"
                    target="_blank"
                    rel="noopener noreferrer"
                >更多
                </a>
            </span>
        );
    }

    componentWillUpdate(nextProps: any, nextStat: any) {
        this.selectedDomain = nextStat.selectedDomain;
    }

    render() {
        if (this.props.dataSource) {
            this.options = this.props.dataSource.map(group => {
                let options = group.children.filter(x => {
                        return x.indexOf(this.state.input) >= 0;
                    }).map(opt => (
                        <Option key={opt} value={opt}>
                            {opt}
                            {/*<span className="certain-search-item-count">{opt.count} 人 关注</span>*/}
                        </Option>
                    ));
                return {
                    label: this.renderTitle(group.title),
                    options:  options
                };
                // return group.title;
                // return (
                //     <OptGroup
                //         key={group.title}
                //         label={this.renderTitle(group.title)}
                //     >
                //         {group.children.filter(x => {
                //             return x.indexOf(this.state.input) >= 0;
                //         }).map(opt => (
                //             <Option key={opt} value={opt}>
                //                 {opt}
                //                 {/*<span className="certain-search-item-count">{opt.count} 人 关注</span>*/}
                //             </Option>
                //         ))}
                //     </OptGroup>);
            });
        }

        return (
            <div className="certain-category-search-wrapper" style={{width: this.state.width}}>
                <AutoComplete
                    dataSource={this.options}
                    className="certain-category-search"
                    dropdownClassName="certain-category-search-dropdown"
                    dropdownStyle={{width: 300}}
                    style={{width: "100%"}}
                    placeholder="search"
                    // optionLabelProp="value"
                    onChange={(e) => this.props.onChange(e)}
                    onSelect={(e) => this.select(e)}
                >
                    <Input suffix={<SearchOutlined className="certain-category-icon"/>}/>
                </AutoComplete>
            </div>
        );
    }

    select(e: any) {
        HolmesStore.setSelectedDomain(e);
    }
}
