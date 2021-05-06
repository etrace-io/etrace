import {action, observable} from "mobx";

export class PageSwitchStore {
    public static readonly MSG_LEAVE_WITHOUT_SAVE = "数据未保存，确定离开？";
    @observable public promptSwitch: boolean = false;

    @action
    public setPromptSwitch(value: boolean) {
        this.promptSwitch = value;
        if (this.promptSwitch) {
            window.onbeforeunload = (e: any) => {
                return PageSwitchStore.MSG_LEAVE_WITHOUT_SAVE;
            };
            window.onunload = (e: any) => {
                return PageSwitchStore.MSG_LEAVE_WITHOUT_SAVE;
            };
        } else {
            window.onbeforeunload = null;
            window.onunload = null;
        }
    }
}