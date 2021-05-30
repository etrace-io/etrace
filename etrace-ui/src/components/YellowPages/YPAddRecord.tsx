import React from "react";
import {debounce, get} from "lodash";
import {messageHandler} from "../../utils/message";
import {UploadChangeParam} from "antd/lib/upload/interface";
import {KeywordItem, RecordForm, RecordItem} from "../../models/YellowPagesModel";
import {
    createRecord,
    editRecord,
    getAssociateKeywords,
    getKeywordSuggest,
    getRecordById,
    PIC_UPLOAD_URL
} from "../../services/YellowPagesService";
import {AutoComplete, Button, Divider, Form, Input, Modal, Popover, Select, Spin, Tag, Tooltip, Upload} from "antd";
import {
    CloudUploadOutlined,
    InfoCircleOutlined,
    LoadingOutlined,
    PlusCircleOutlined,
    QuestionCircleOutlined,
    TagOutlined
} from "@ant-design/icons/lib";

const ICON_ALLOWED_TYPE = ["image/jpg", "image/jpeg", "image/png", "image/svg", "image/svg+xml", "image/gif", "image/x-icon"];

function beforeUpload(file: any) {
    // console.log(file.type, ICON_ALLOWED_TYPE, file);
    const typeAllowed = ICON_ALLOWED_TYPE.indexOf(file.type) > -1;
    if (!typeAllowed) {
        messageHandler("error", "请上传符合格式的 Icon");
    }
    const isLt2M = file.size / 1024 / 1024 <= 1;
    if (!isLt2M) {
        messageHandler("error", "上传的图片不能大于 1M！");
    }
    return typeAllowed && isLt2M;
}

function getBase64(img: Blob, callback: (url: string) => void) {
    const reader = new FileReader();
    reader.addEventListener("load", () => callback(reader.result as string));
    reader.readAsDataURL(img);
}

interface YPAddRecordProps {
    visible: boolean;
    id?: number; // 编辑状态
    onOk?: (visible: boolean) => void;
    onCancel?: () => void;
}

interface YPAddRecordStatus {
    modalLoading: boolean; // 创建 or 更新 Record 的 Loading
    recordLoading: boolean; // 根据 id 获取 Record 的 Loading
    dataSource: RecordForm;
    shouldDestroyOnClose: boolean;
}

export default class YPAddRecord extends React.Component<YPAddRecordProps, YPAddRecordStatus> {
    isEdit: boolean = false;
    record: RecordForm = null; // 用户填写的信息

    constructor(props: YPAddRecordProps) {
        super(props);
        this.isEdit = !!this.props.id;

        this.state = {
            modalLoading: false,
            recordLoading: this.isEdit,
            dataSource: null,
            shouldDestroyOnClose: this.isEdit,
        };
    }

    componentWillReceiveProps(nextProps: Readonly<YPAddRecordProps>, nextContext: any): void {
        if (nextProps.visible) {
            const {id} = nextProps;
            id && this.getRecordInfoById(id);
        }
    }

    getRecordInfoById = (id: number) => {
        this.setState({recordLoading: true});
        // Get By Id
        getRecordById(id)
            .then(dataSource => {
                this.setState({
                    dataSource,
                    recordLoading: false
                });

                this.record = Object.assign({}, dataSource);
            })
            .catch(err => {
                messageHandler("error", "获取收录配置失败");
                this.setState({
                    recordLoading: false
                });
            });
    };

    validateRecordForm = (record: RecordForm) => {
        if (!record.name) {
            messageHandler("warning", "请填写收录名称");
            return false;
        }
        if (!record.url) {
            messageHandler("warning", "请填写收录网址");
            return false;
        }
        // if (!record.fileRecordId) {
        //     messageHandler("warning", "请上传收录图标");
        //     return false;
        // }
        return true;
    };

    handleAddRecordFormChange = (allField: any) => {
        // const needFields = ["name", "description", "dingtalkNumber", "fileRecordId", "ownerDingtalkNumber"];
        const form: any = {};
        Object.keys(allField).forEach(key => {
            const {value} = allField[key];
            form[key] = value;
            // return {name, value};
        });

        // console.log(form);

        const iconFileId = get(form.iconFile, "[0].response");

        this.record = {
            name: form.name,
            // url: form.url && [form.urlProtocol, form.url, form.urlTLD].join(""),
            url: form.url,
            description: form.description,
            dingtalkNumber: form.dingtalkNumber,
            ownerDingtalkNumber: [form.ownerDingtalkNumber, form.ownerNickname].join("|"),
            fileRecordId: Number.isInteger(iconFileId) ? iconFileId : form.fileRecordId,
            keywordList: form.keywords,
            status: "Active",
        };
    };

    handleModalCancel = () => {
        const {onCancel} = this.props;
        onCancel && onCancel();
    };

    handleModalOk = () => {
        const {onOk} = this.props;
        const {dataSource} = this.state;

        const recordNeedPost = Object.assign({}, dataSource, this.record);
        // console.log(recordNeedPost);

        // 校验 & Post Request
        if (this.validateRecordForm(recordNeedPost)) {
            this.setState({modalLoading: true, shouldDestroyOnClose: true});

            if (this.isEdit) {
                // 编辑模式
                editRecord(recordNeedPost)
                    .then(() => {
                        this.setState({modalLoading: false});
                        messageHandler("success", "更新成功");
                        onOk && onOk(false);
                    })
                    .catch((err) => {
                        this.setState({modalLoading: false});
                        messageHandler("error", "修改收录失败，请联系管理员或稍后重试~");
                        onOk && onOk(true);
                    });
            } else {
                // 创建模式
                createRecord(recordNeedPost)
                    .then(() => {
                        this.setState({modalLoading: false});
                        messageHandler("success", "添加成功");
                        onOk && onOk(false);
                    })
                    .catch((err) => {
                        this.setState({modalLoading: false});
                        messageHandler("error", "创建收录失败，请联系管理员或稍后重试~");
                        onOk && onOk(true);
                    });
            }
        }
    };

    render() {
        const {visible} = this.props;
        const {modalLoading, dataSource, recordLoading, shouldDestroyOnClose} = this.state;

        const modalFooter = [
            <Button key="back" className="add-record-modal__cancel-btn" onClick={this.handleModalCancel}>取消</Button>,
            <Button key="submit" className="add-record-modal__ok-btn" loading={modalLoading} onClick={this.handleModalOk}>{this.isEdit ? "提交修改" : "提交创建"}</Button>,
        ];

        return (
            <Modal
                destroyOnClose={shouldDestroyOnClose}
                className="e-monitor-yellow-pages__add-record-modal"
                title="添加收录"
                visible={visible}
                onOk={this.handleModalOk}
                onCancel={this.handleModalCancel}
                footer={modalFooter}
            >
                <Spin tip="配置加载中..." delay={0} spinning={recordLoading}>
                    <WrappedAddRecordForm
                        dataSource={dataSource || {}}
                        onChange={this.handleAddRecordFormChange}
                    />
                </Spin>
                <div className="add-record-modal__tips">
                    <p className="add-record-modal__tip-item"><InfoCircleOutlined />该收录将会公开给所有用户</p>
                </div>
            </Modal>
        );
    }
}

interface AddRecordProps {
    form?: any;
    onChange?: (allFields: any) => void;
    dataSource?: RecordItem;
}

interface AddRecordFormStatus {
    iconUploading: boolean;
    iconURL: string;
    keywordDataSource: KeywordItem[]; // 根据输入获取的搜索建议
    // keywordList: any[];
    associateKeywords: KeywordItem[];
    keywordsInputVisible: boolean;
    newKeywordsInputValue: string;
    keywordsIsLoading: boolean;
}

class AddRecordForm extends React.Component<AddRecordProps, AddRecordFormStatus> {
    private static FORM_LAYOUT = {
        labelCol: {span: 4},
        wrapperCol: {span: 18},
    };

    keywordInput = React.createRef();
    debounceSearch: any; // search 的 debounce 事件
    debounceTime: number = 400; // search 的 debounce 时长

    constructor(props: AddRecordProps) {
        super(props);

        const {dataSource} = props;

        this.state = {
            // Form 状态
            iconUploading: false,
            iconURL: get(dataSource, "icon"),
            keywordDataSource: [],
            // keywords: [],
            // keywordList: get(dataSource, "keywordList", []).map(keyword => keyword.name),
            associateKeywords: [],
            keywordsInputVisible: false,
            keywordsIsLoading: false,
            newKeywordsInputValue: "",
            // Form 内容
        };
        this.debounceSearch = debounce(this.handleSearchSuggestKeyword, this.debounceTime);
    }

    componentWillReceiveProps(nextProps: Readonly<AddRecordProps>, nextContext: any): void {
        const {dataSource} = nextProps;
        this.setState({
            iconURL: this.state.iconURL || get(dataSource, "icon"),
            // keywordList: get(dataSource, "keywordList", []).map(keyword => keyword.name),
        });
    }

    handleUploadStatusChange = (info: UploadChangeParam) => {
        if (info.file.status === "uploading") {
            this.setState({iconUploading: true});
            return;
        }
        if (info.file.status === "done") {
            // Get this url from response in real world.
            getBase64(info.file.originFileObj, iconURL =>
                this.setState({
                    iconURL,
                    iconUploading: false,
                }),
            );
        }
        if (info.file.status === "error") {
            this.setState({
                iconUploading: false,
            });
            messageHandler("error", "icon 上传失败");
            // console.log(info);
        }
    };

    handleKeywordsDeleted = (keyword: KeywordItem) => {
        this.deleteKeyword(keyword);
    };

    handleShowKeywordInput = () => {
        this.setState({
            keywordsInputVisible: true
        }, () => {
            const input: any = this.keywordInput.current;
            input.focus();
        });
    };

    handleKeywordInputChange = (value) => {
        this.setState({newKeywordsInputValue: value});
    };

    // 点击下拉框选择现有 keyword
    handleSuggestKeywordSelected = (keyword) => {
        this.addKeyword(JSON.parse(keyword));
    };

    // 回车确认添加新的 keyword
    handleAddKeyword = () => {
        const {newKeywordsInputValue, keywordDataSource} = this.state;
        if (!newKeywordsInputValue) {
            return;
        }
        const index = keywordDataSource.findIndex(item => item.name === newKeywordsInputValue);
        const target = index > -1
            ? keywordDataSource[index]
            : {name: newKeywordsInputValue, status: "Active"};

        this.addKeyword(target);
    };

    handleAddAssociateKeywords = (keyword: KeywordItem) => {
        this.addKeyword(keyword);
    };

    handleSearchSuggestKeyword = (value: string) => {
        if (value) {
            this.setState({keywordsIsLoading: true});
            getKeywordSuggest(value)
                .then(keywordDataSource => {
                    this.setState({keywordDataSource, keywordsIsLoading: false});
                })
                .catch(err => {
                    messageHandler("error", "关键词搜索失败");
                    this.setState({keywordsIsLoading: false});
                });
        } else {
            this.setState({keywordDataSource: []});
        }
    };

    handleQuickIconChooserSelect = (icon) => {
        const {setFieldsValue} = this.props.form;
        this.setState({
            iconURL: icon
        }, () => {
            setFieldsValue({
                iconFile: [{
                    uid: "-1",
                    status: "done",
                    url: icon,
                }],
            });
        });
    };

    addKeyword = (keyword: KeywordItem) => {
        const [form] = Form.useForm();
        // const {keywordList} = this.state;
        const {getFieldValue, setFieldsValue} = form;
        const keywordList = getFieldValue("keywords") || [];
        const index = keywordList.findIndex(item => item.name === keyword.name);

        if (index === -1) {
            const result = keywordList.concat(keyword);
            setFieldsValue({keywords: result});
            this.setState({
                keywordsInputVisible: false,
                newKeywordsInputValue: "",
            });
            // 添加完获取关联关键字
            this.queryAssociateKeywords(result.map(i => i.id).filter(Boolean));
        }
    };

    deleteKeyword = (keyword: KeywordItem) => {
        const [form] = Form.useForm();
        const {getFieldValue, setFieldsValue} = form;
        const keywordList = getFieldValue("keywords") || [];
        const index = keywordList.findIndex(item => item.name === keyword.name);

        if (index > -1) {
            keywordList.splice(index, 1);
            setFieldsValue({keywords: keywordList});
        }
    };

    queryAssociateKeywords = (ids: number[]) => {
        if (!ids || ids.length === 0) {
            return;
        }
        getAssociateKeywords(ids)
            .then(associateKeywords => {
                this.setState({associateKeywords});
            })
            .catch(err => {
                messageHandler("error", "相关关键词搜索失败");
            });
    };

    render() {
        const {iconUploading, iconURL, associateKeywords, keywordsInputVisible, newKeywordsInputValue, keywordDataSource, keywordsIsLoading} = this.state;


        const {dataSource} = this.props;

        const dingTalkNumberTips = this.getDingTalkNumberTips();
        // const finalUrl = this.getFinalUrl();

        const parsedUrl = this.parseUrl(dataSource.url);

        // const urlProtocol = getFieldDecorator("urlProtocol", {
        //     initialValue: parsedUrl.protocol || AddRecordForm.WEBSITE_PROTOCOL_DEFAULT
        // })(AddRecordForm.WEBSITE_PROTOCOL);
        //
        // const urlTLD = getFieldDecorator("urlTLD", {
        //     initialValue: dataSource.url
        //         ? parsedUrl.tld
        //         : AddRecordForm.WEBSITE_TLD_DEFAULT,
        // })(AddRecordForm.WEBSITE_TLD);

        const iconFile = dataSource.icon ? [{
            uid: "-1",
            status: "done",
            url: dataSource.icon,
        }] : null;

        // 注册 Keywords
        // getFieldDecorator("keywords", {initialValue: get(dataSource, "keywordList", [])});
        // getFieldDecorator("fileRecordId", {initialValue: get(dataSource, "fileRecordId")});

        // 获取 Keywords
        // const keywordList: KeywordItem[] = getFieldValue("keywords");
        // const keywordAutoCompleteDataSource = keywordsIsLoading
        //     ? [(
        //         <AutoComplete.Option disabled={true} key="loading">
        //             <div style={{textAlign: "center"}}>加载中...</div>
        //         </AutoComplete.Option>
        //     )]
        //     : keywordDataSource.map(item => ({
        //         value: JSON.stringify(item),
        //         text: item.name
        //     }));
        // TODO: 支持在创建 Record 的过程中往 List 中添加

        return (
            <Form onFieldsChange={this.props.onChange}>
                {/* 类型 */}
                <Form.Item {...AddRecordForm.FORM_LAYOUT} label="类型" initialValue={dataSource.type || "app"} rules={[{required: true, message: "请填写 App 名称"}]} >
                    // todo: https://ant.design/components/radio/#header
                {/*<Radio.Group buttonStyle="solid">*/}
                {/*<Radio.Button value="app">应用</Radio.Button>*/}
                {/*<Radio.Button value="link">链接</Radio.Button>*/}
                {/*</Radio.Group>*/}
                </Form.Item>

                {/* 名称 */}
                <Form.Item {...AddRecordForm.FORM_LAYOUT} label="名称" initialValue={dataSource.name} rules={[{required: true, message: "请填写 App 名称"}]}>
                        <Input placeholder="请输入收录名称"/>
                </Form.Item>

                {/* 网址 */}
                <Form.Item {...AddRecordForm.FORM_LAYOUT} label="网址" initialValue={parsedUrl.url} rules={[{required: true, message: "请填写 App 网址"}]}>
                        <Input placeholder="请填写收录网址"/>
                    {/*<Input addonBefore={urlProtocol} addonAfter={urlTLD}/>*/}
                </Form.Item>

                {/* 最终链接 */}
                {/*<Form.Item wrapperCol={Object.assign({}, AddRecordForm.FORM_LAYOUT.wrapperCol, {offset: AddRecordForm.FORM_LAYOUT.labelCol.span})}>*/}
                {/*{finalUrl && <div className="final-url">{finalUrl}</div>}*/}
                {/*</Form.Item>*/}

                {/* 名称 */}
                <Form.Item {...AddRecordForm.FORM_LAYOUT} label="简介" initialValue={dataSource.description}>
                        <Input placeholder="好让用户快速了解"/>
                </Form.Item>

                {/* 钉钉群 */}
                <Form.Item {...AddRecordForm.FORM_LAYOUT} label="钉钉群" initialValue={dataSource.dingtalkNumber}>
                        <Input placeholder="钉钉群号码，方便团队答疑支持"/>
                </Form.Item>

                {/* 负责人 */}
                <Form.Item {...AddRecordForm.FORM_LAYOUT} label="负责人">
                    <Input.Group>
                        {/*{getFieldDecorator("ownerDingtalkNumber", {*/}
                        {/*    initialValue: dataSource.ownerDingtalkNumber && dataSource.ownerDingtalkNumber.split("|")[0],*/}
                        {/*})(*/}
                            <Input style={{width: "75%"}} placeholder="负责人钉钉号，方便用户发起咨询" suffix={dingTalkNumberTips}/>
                        {/*)}*/}
                        {/*{getFieldDecorator("ownerNickname", {*/}
                        {/*    initialValue: dataSource.ownerDingtalkNumber && dataSource.ownerDingtalkNumber.split("|")[1],*/}
                        {/*})(*/}
                            <Input style={{width: "25%"}} placeholder="名字 / 花名"/>
                        {/*)}*/}
                    </Input.Group>
                </Form.Item>

                {/* 图标 */}
                <Form.Item
                    {...AddRecordForm.FORM_LAYOUT}
                    label="图标"
                    className="record-choose-icon"
                    initialValue={iconFile}
                    valuePropName={"fileList"}
                    getValueFromEvent={(e) => {
                        if (Array.isArray(e)) {
                            return e;
                        }
                        return e && [e.file];
                    }
                    }
                >
                        <Upload
                            withCredentials={true}
                            listType="picture-card"
                            className="record-icon-uploader"
                            showUploadList={false}
                            action={PIC_UPLOAD_URL}
                            beforeUpload={beforeUpload}
                            onChange={this.handleUploadStatusChange}
                            accept={ICON_ALLOWED_TYPE.join(", ")}
                        >
                            {iconURL
                                ? <img src={iconURL} alt="加载失败" className="record-icon__img"/>
                                : <div>
                                    {iconUploading ? <LoadingOutlined /> : <CloudUploadOutlined />}
                                    <div className="ant-upload-text">{iconUploading ? "上传中..." : "点击上传"}</div>
                                </div>
                            }
                        </Upload>

                    {/*<QuickIconChooser onSelect={this.handleQuickIconChooserSelect}/>*/}
                </Form.Item>

                {/* 关键字 */}
                <Form.Item {...AddRecordForm.FORM_LAYOUT} label="关键字">
                    {({getFieldValue}) => {
                        return getFieldValue("keywords").map(keyword => {
                            const isLongTag = keyword.name.length > 20;
                            const tagElem = (
                                <Tag key={keyword.name} className="keyword-tag" closable={true} onClose={() => this.handleKeywordsDeleted(keyword)}>
                                    <TagOutlined /> {isLongTag ? `${keyword.name.slice(0, 20)}...` : keyword.name}
                                </Tag>
                            );
                            return isLongTag ? (
                                <Tooltip title={keyword.name} key={keyword.name}>
                                    {tagElem}
                                </Tooltip>
                            ) : (tagElem);
                        });
                    }}
                    {keywordsInputVisible
                        ? (
                            <AutoComplete
                                style={{width: 120}}
                                // todo: 这里没有ref
                                // ref={this.keywordInput}
                                dataSource={keywordDataSource.map(item => ({
                                    value: JSON.stringify(item),
                                    text: item.name
                                }))}
                                // dataSource={keywordAutoCompleteDataSource}

                                onSelect={this.handleSuggestKeywordSelected}
                                onChange={this.handleKeywordInputChange}
                                onSearch={this.debounceSearch}
                                placeholder="搜索关键词"
                                // optionLabelProp="text"
                            >
                                <Input
                                    type="text"
                                    className="keyword-input"
                                    value={newKeywordsInputValue}
                                    onBlur={this.handleAddKeyword}
                                    // onBlur={this.handleInputConfirm}
                                    onPressEnter={this.handleAddKeyword}
                                />
                            </AutoComplete>
                        )
                        : (
                            <Tag className="keyword-tag add-tag-btn" onClick={this.handleShowKeywordInput}>
                                <PlusCircleOutlined /> 新增关键字
                            </Tag>
                        )
                    }
                </Form.Item>

                {/* 联想关键词 */}
                {associateKeywords && associateKeywords.length > 0 && (
                    <Form.Item
                        wrapperCol={Object.assign({}, AddRecordForm.FORM_LAYOUT.wrapperCol, {offset: AddRecordForm.FORM_LAYOUT.labelCol.span})}
                    >
                        <Divider className="associative-keyword__divider" dashed={true}>推荐关键词</Divider>

                        {associateKeywords.map(keyword => {
                            const isLongTag = keyword.name.length > 20;
                            const tagElem = (
                                <Tag key={keyword.name} className="associative-keyword" onClick={() => this.handleAddAssociateKeywords(keyword)}>
                                    <TagOutlined /> {isLongTag ? `${keyword.name.slice(0, 20)}...` : keyword.name}
                                </Tag>
                            );
                            return isLongTag ? (
                                <Tooltip title={keyword.name} key={keyword.name}>
                                    {tagElem}
                                </Tooltip>
                            ) : (
                                tagElem
                            );
                        })}
                    </Form.Item>
                )}
            </Form>
        );
    }

    getDingTalkNumberTips = () => {
        const dingTalkNumberTipsContent = (
            <div style={{textAlign: "center"}}>
                <div className="tip-image-container">
                    <img className="tip-image" src="" alt="Tips"/>
                    <img className="tip-image overlay" src="" alt="Tips"/>
                </div>
                <div className="tip-text">PC 端：个人 Profile 页 - 钉钉号</div>
                <div className="tip-text">移动端：点击头像「我的信息」页 - 钉钉号</div>
            </div>
        );

        return (
            <Popover arrowPointAtCenter={true} placement="bottomRight" content={dingTalkNumberTipsContent} overlayClassName="e-monitor-yellow-pages__dingTalk-number-tips">
                <QuestionCircleOutlined />
            </Popover>
        );
    };

    getFinalUrl = () => {
        const {getFieldsValue} = this.props.form;
        const urlArray = ["urlProtocol", "url", "urlTLD"];
        const urlContent = getFieldsValue(urlArray);
        return urlContent.url && urlArray.map(key => urlContent[key]).join("");
    };

    parseUrl = (url: string) => {
        const result = {
            protocol: "",
            url: "",
            tld: "",
        };

        if (!url) {
            return result;
        }

        if (url.startsWith("https://")) {
            result.protocol = "https://";
        }
        if (url.startsWith("http://")) {
            result.protocol = "http://";
        }
        if (url.endsWith(".elenet.me")) {
            result.tld = ".elenet.me";
        }
        if (url.endsWith(".ele.me")) {
            result.tld = ".ele.me";
        }
        result.url = url
            .replace(result.protocol, "")
            .replace(result.tld, "");

        return result;
    };
}

const WrappedAddRecordForm = (v: AddRecordProps) => (<AddRecordForm {...v}/>);

// interface QuickIconChooserProps {
//     onSelect: (icon: string) => void;
// }
//
// const QuickIconChooser: React.SFC<QuickIconChooserProps> = props => {
//     const ICON_LIST = [
//         "https://gw.alipayobjects.com/zos/rmsportal/XuVpGqBFxXplzvLjJBZB.svg", // 语雀
//     ];
//
//     const {onSelect} = props;
//     return (
//         <div className="quick-icon-chooser">
//             <div className="quick-icon-chooser__title">快速选择</div>
//             <div className="quick-icon-chooser__list">
//                 {ICON_LIST.map(icon => (
//                     <img key={icon} className="quick-icon-chooser__item" onClick={() => onSelect(icon)} src={icon}/>
//                 ))}
//             </div>
//         </div>
//     );
// };
