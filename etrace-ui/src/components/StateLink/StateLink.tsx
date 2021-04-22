import React, {ReactElement, ReactNode} from "react";
import {observer} from "mobx-react";
import {Button, Dropdown, Menu} from "antd";
import StoreManager from "../../store/StoreManager";
import {reaction} from "mobx";
import {isEmpty} from "../../utils/Util";

const MenuItem = Menu.Item;
const ButtonGroup = Button.Group;

interface StateLinkProps {
    name: string;
    loadStatLinkSource: any;
    icon: ReactNode;
}

interface StateLinkState {
    menu: ReactElement;
}

// todo: 如此定制化的中间件功能 可移除

@observer
export default class StateLink extends React.Component<StateLinkProps, StateLinkState> {
    stateLinkStore;
    disposer;

    constructor(props: StateLinkProps) {
        super(props);
        this.stateLinkStore = StoreManager.stateLinkStore;
        this.state = {menu: (<Menu><MenuItem>无数据</MenuItem></Menu>)};
        this.disposer = reaction(
            () => this.stateLinkStore.statListData,
            () => {
                this.loadMenuData();
            }
        );
    }

    async loadMenuData() {
        const menus = await this.props.loadStatLinkSource();
        if (!isEmpty(menus)) {
            this.setState({menu: menus});
        }
    }

    componentWillUnmount() {
        if (this.disposer) {
            this.disposer();
        }
    }

    render() {
        return (
            <Dropdown overlay={this.state.menu}>
                <ButtonGroup>
                    <Button
                        htmlType="button"
                        style={{color: "rgba(218, 0, 61, 0.6)", padding: "0 7px"}}
                    >
                        {this.props.icon}
                    </Button>
                </ButtonGroup>
            </Dropdown>
        );
    }
}
