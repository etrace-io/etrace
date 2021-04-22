import React from "react";
import {Button, Checkbox, Input, Modal, Popconfirm, Transfer} from "antd";
import {TransferItem} from "antd/lib/transfer";
import {addRecordsToList, getListRecord, getRecordByName} from "../../services/YellowPagesService";
import {RecordItem} from "../../models/YellowPagesModel";
import {messageHandler} from "../../utils/message";
import YellowPages from "../../containers/YellowPages/YellowPages";
import {uniqBy} from "lodash";

interface YpAddRecordToListProps {
    visible: boolean; // 用于判断是否获取 List 下的内容
    list: number;
    defaultSelected?: string[]; // Record ID
    onChange?: (targets: string[]) => void;
}

interface YpAddRecordToListStatus {
    dataSource: TransferItem[];
    isLoading: boolean;
    currSelected: string[];
    totalRecord: TransferItem[];
}

export default class YpAddRecordToList extends React.Component<YpAddRecordToListProps, YpAddRecordToListStatus> {
    constructor(props: YpAddRecordToListProps) {
        super(props);
        this.state = {
            dataSource: [],
            isLoading: false,
            currSelected: [],
            totalRecord: [],
        };
    }

    componentWillMount(): void {
        const {list, visible} = this.props;
        if (visible) {
            this.handleSearchRecord("");
            this.queryListRecords(list);
        }
    }

    queryListRecords = (listId: number) => {
        this.setState({isLoading: true});

        getListRecord(listId)
            .then(result => {
                const records = result.results;
                const dataSource: TransferItem[] = records.map(record => ({
                    key: record.id.toString(),
                    ...record,
                }));
                const {totalRecord} = this.state;
                const currSelected = dataSource.map(item => item.key);
                this.setState({
                    totalRecord: uniqBy(totalRecord.concat(dataSource), "id"),
                    isLoading: false,
                });
                this.handleRecordChange(currSelected);
            })
            .catch(err => {
                this.setState({isLoading: false});
                messageHandler("error", "获取当前集合内容失败！");
            });
    };

    handleSearchRecord = (name: string) => {
        this.setState({isLoading: true});
        getRecordByName(name)
            .then(result => {
                const records = result.results;
                const dataSource: TransferItem[] = records.map(record => ({
                    key: record.id.toString(),
                    ...record,
                }));
                const {totalRecord} = this.state;
                this.setState({
                    dataSource,
                    totalRecord: uniqBy(totalRecord.concat(dataSource), "id"),
                    isLoading: false,
                });
            })
            .catch(err => {
                this.setState({isLoading: false});
                messageHandler("error", "搜索收录出错");
            });
    };

    handleRecordChange = (targetKeys) => {
        const {onChange} = this.props;
        if (onChange) {
            onChange(targetKeys);
        }
        this.setState({
            currSelected: targetKeys
        });
    };

    render() {
        const {dataSource, currSelected, totalRecord, isLoading} = this.state;

        const locale = {itemUnit: "项", itemsUnit: "项"};

        return (
            <div className="e-monitor-yellow-pages__record-chooser">
                <Transfer
                    dataSource={dataSource}
                    listStyle={{}}
                    targetKeys={currSelected}
                    onChange={this.handleRecordChange}
                    rowKey={record => record.key}
                    locale={locale}
                    showSelectAll={false}
                >
                    {({direction, disabled, selectedKeys, onItemSelect}) => {

                        const targetDataSource: any[] = direction === "left"
                            ? dataSource
                                .map(item => {
                                    const selected = currSelected.indexOf(item.key) > -1;
                                    const checked = selectedKeys.indexOf(item.key) > -1;
                                    return {
                                        ...item,
                                        disabled: selected,
                                        checked: selected || checked,
                                    };
                                })
                            : totalRecord
                                .filter(item => currSelected.indexOf(item.key) > -1)
                                .map(item => {
                                    const checked = selectedKeys.indexOf(item.key) > -1;
                                    return {
                                        ...item,
                                        checked,
                                    };
                                });

                        return (
                            <div>
                                {direction === "left" && (
                                    <Input.Search
                                        className="transfer-record-search"
                                        loading={isLoading}
                                        onSearch={this.handleSearchRecord}
                                        placeholder="搜索收录"
                                    />
                                )}
                                {targetDataSource.map(item => (
                                    <Checkbox
                                        className="transfer-record-item-container"
                                        key={item.key}
                                        checked={item.checked}
                                        disabled={item.disabled}
                                        onChange={() => onItemSelect(item.key, selectedKeys.indexOf(item.key) === -1)}
                                    >
                                        <RecordItemView record={item}/>
                                    </Checkbox>
                                ))}
                            </div>
                        );
                    }}
                </Transfer>
            </div>
        );
    }
}

interface RecordItemProps {
    record: RecordItem;
}

const RecordItemView: React.SFC<RecordItemProps> = props => {
    const {record} = props;
    return (
        <div className="transfer-record-item">
            <img className="record__logo" src={record.icon || YellowPages.DEFAULT_RECORD_LOGO} alt="icon"/>
            <div className="record__info">
                <div className="record__name">{record.name}</div>
                <div className="record__desc">{record.description}</div>
            </div>
        </div>
    );
};

interface YPAddRecordToListModalProps {
    list: number; // List ID
    visible: boolean;
    onOk?: (visible: boolean, needRefresh?: boolean) => void;
    onCancel?: () => void;
}

interface YPAddRecordToListModalStatus {
    isPosting?: boolean; // 创建 or 编辑请求的 Loading
    selectedRecords: number[];
    confirmVisible: boolean;
}

export class YpAddRecordToListModal extends React.Component<YPAddRecordToListModalProps, YPAddRecordToListModalStatus> {

    constructor(props: YPAddRecordToListModalProps) {
        super(props);
        this.state = {
            isPosting: false,
            confirmVisible: false,
            selectedRecords: [],
        };
    }

    handleModalOk = () => {
        const {onOk, list} = this.props;
        const {selectedRecords} = this.state;

        this.setState({
            confirmVisible: false,
            isPosting: true,
        });

        // if (!selectedRecords || selectedRecords.length === 0) {
        //     messageHandler("warning", "请添加 App~");
        //     return;
        // }

        // 校验 & Post Request
        addRecordsToList(list, selectedRecords)
            .then(() => {
                this.setState({isPosting: false});
                onOk && onOk(false, true);
            })
            .catch((err) => {
                this.setState({isPosting: false});
                messageHandler("error", "添加收录失败，请联系管理员或稍后重试~");
                onOk && onOk(true);
            });
    };

    handleModalCancel = () => {
        const {onCancel} = this.props;
        onCancel && onCancel();
    };

    handleTargetChange = (targets: string[]) => {
        this.setState({
            selectedRecords: targets.map(Number),
        });
    };

    handleConfirmVisibleChange = visible => {
        const {selectedRecords} = this.state;
        if (selectedRecords.length === 0) {
            this.setState({
                confirmVisible: visible,
            });
        } else {
            this.handleModalOk();
        }
    };

    render() {
        const {visible, list} = this.props;
        const {isPosting, confirmVisible} = this.state;

        const modalFooter = [
            <Button key="back" className="add-record-to-list-modal__cancel-btn" onClick={this.handleModalCancel}>取消</Button>,
            (
                <Popconfirm
                    key="submit"
                    title="确定不添加任何收录吗？"
                    visible={confirmVisible}
                    placement="topRight"
                    onConfirm={this.handleModalOk}
                    onVisibleChange={this.handleConfirmVisibleChange}
                    okText="确定！"
                    cancelText="我再想想"
                >
                    <Button className="add-record-to-list-modal__ok-btn" loading={isPosting}>提交</Button>
                </Popconfirm>
            ),
        ];

        return (
            <Modal
                destroyOnClose={true}
                title="添加收录"
                className="e-monitor-yellow-pages__add-record-to-list-modal"
                footer={modalFooter}
                visible={visible}
                onOk={this.handleModalOk}
                onCancel={this.handleModalCancel}
                width={750}
                // onOk={visible => this.handleToggleAddRecordToListModal(visible)}
            >
                <YpAddRecordToList
                    visible={visible}
                    list={list}
                    onChange={this.handleTargetChange}
                />
            </Modal>
        );
    }
}
