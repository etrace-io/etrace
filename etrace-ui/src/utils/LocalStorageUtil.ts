export class LocalStorageUtil {
    public static set(key: string, value: string) {
        localStorage[key] = value;
        return localStorage[key];
    }

    public static get(key: string, defaultValue?: any) {
        return localStorage[key] || defaultValue;
    }

    public static setObject(key: string, value: any) {
        localStorage[key] = JSON.stringify(value);
        return localStorage[key];
    }

    public static getObject(key: string) {
        if (localStorage[key]) {
            return JSON.parse(localStorage[key]);
        } else {
            return null;
        }
    }

    public static clear() {
        return localStorage.clear();
    }

    public static getStringValues(key: string): string[] {
        let historyOfKey: Array<string>;
        try {
            historyOfKey = JSON.parse(localStorage.getItem(key)) || [];
        } catch (e) {
            // delete wrong key
            localStorage.removeItem(key);
            historyOfKey = [];
        }
        return historyOfKey;
    }
}