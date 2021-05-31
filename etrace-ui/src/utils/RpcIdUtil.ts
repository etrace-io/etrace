import {Stack} from "immutable";

export default class RpcIdUtil {
    private static pat: RegExp = /([.^]+)/g;

    /**
     * 可能的CASE 有： 1.2.3^4  => 1.2.4, 1^2 => 2
     * 详见 Callstack.test.tsx
     * @param rpcId
     */
    public static parseFatherRpcId(rpcId: string): string {
        let dot = rpcId.lastIndexOf(".");
        let upper = rpcId.lastIndexOf("^");

        let index = dot > upper ? dot : upper;

        if (upper > 0 && upper > dot) {
            // 这是最后为^的 rpcId
            let lastPart = rpcId.substring(upper + 1);
            if (dot > 0) {
                let firstPart = rpcId.substring(0, dot);
                return firstPart + "." + lastPart;
            } else {
                return lastPart;
            }
        } else {
            return rpcId.substr(0, index);
        }
    }

    /*
     depth first sort
     return negative if the first item is smaller; positive if it it's larger, or zero if they're equal.
     */
    public static dfs(first: string, second: string): number {
        if (first == second) {
            return 0;
        } else {
            let parts1: Array<string> = this.parseToParts2(first);
            let parts2: Array<string> = this.parseToParts2(second);
            // first, compare same part
            let sameLength: number = Math.min(parts1.length, parts2.length);

            for (let i = 0; i < sameLength; i++) {
                if (parts1[i] != (parts2[i])) {
                    try {
                        let i1 = parseInt(parts1[i]);
                        let i2 = parseInt(parts2[i]);

                        // 让 NONE.1 == 1.1
                        if (!isNaN(i1) && !isNaN(i2)) {
                            return i1 - i2;
                        }
                    } catch (e) {
                        console.warn(first, second, e);
                        // ignore
                    }
                }
            }
            // then, compare the remaining length: (parts1.length - sameLength) - (parts2.length - sameLength);
            return parts1.length - parts2.length;
        }
    }

    /*
    breadth first sort
    return negative if the first item is smaller; positive if it it's larger, or zero if they're equal.
    */
    public static bfs(first: string, second: string): number {
        if (first == second) {
            return 0;
        } else {
            let parts1: Array<string> = this.parseToParts2(first);
            let parts2: Array<string> = this.parseToParts2(second);

            if (parts1.length != parts2.length ) {
                return parts1.length - parts2.length;
            } else {
                // first, compare same part
                let sameLength: number = parts1.length;

                for (let i = 0; i < sameLength; i++) {
                    if (parts1[i] != (parts2[i])) {
                        try {
                            let i1 = parseInt(parts1[i]);
                            let i2 = parseInt(parts2[i]);

                            if (!isNaN(i1) && !isNaN(i2)) {
                                return i1 - i2;
                            }
                        } catch (e) {
                            console.warn(first, second, e);
                            // ignore
                        }
                    }
                }
                return 0;
            }
        }
    }

    public static parseToParts2(rpcId: string): Array<string> {
        let parts: Array<string> = rpcId.split(/[.^]+/);
        if (parts.length == 1) {
            return parts;
        } else {
            let result: Array<string> = [];
            let regexParts: Array<string> = rpcId.match(RpcIdUtil.pat);

            let index: number = 0;
            let meetRemoveCall: boolean = false;
            // 默认是 immutable的，需要加上asMutable()
            let stack: Stack<{}> = Stack.of().asMutable();

            while (index < parts.length) {
                if (index < regexParts.length && regexParts[index] == "^") {
                    stack.push(parts[index]);
                    meetRemoveCall = true;
                } else {
                    if (meetRemoveCall) {
                        stack.push(parts[index]);
                        result.push(...this.popAndClear(stack));
                        meetRemoveCall = false;
                    } else {
                        result.push(parts[index]);
                    }
                }
                index++;
            }
            return result;
        }
    }

    private static popAndClear(stack: Stack<{}>): Array<string> {
        let popped = [];

        while (stack.size != 0) {
            // 使用的这个 immutable package中的stack api比较奇怪
            popped.push(stack.peek());
            stack = stack.shift();
        }
        return popped;
    }
}
