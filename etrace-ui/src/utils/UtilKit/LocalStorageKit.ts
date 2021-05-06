export default {
    getArray,
    getValue,
    setValue,
};

/**
 * 从 LocalStorage 取数组
 * @param key
 */
function getArray(key: string) {
    try {
        return JSON.parse(localStorage.getItem(key)) || [];
    } catch (e) {
        // delete wrong key
        localStorage.removeItem(key);
        return [];
    }
}

function getValue(key: string) {
    return localStorage.getItem(key);
}

function setValue(key: string, value: string) {
    return localStorage.setItem(key, value);
}
