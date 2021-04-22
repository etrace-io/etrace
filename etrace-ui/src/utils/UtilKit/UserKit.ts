import {User} from "$models/User";

export default {
    isAdmin,
    isDeveloper,
    isVisitor,
};

/**
 * 判断用户是否为 admin
 * @param user 当前用户
 */
function isAdmin(user: User): boolean {
    return user && user.roles && user.roles.indexOf("ADMIN") >= 0;
}

/**
 * 判断用户是否为 admin
 * @param user 当前用户
 */
function isDeveloper(user: User): boolean {
    return user && (user.deptname === "监控框架组" || user.deptname === "稳定性研发组");
}

/**
 * 判断用户是否为 visitor
 * @param user 当前用户
 */
function isVisitor(user: User): boolean {
    return user && user.roles && user.roles.indexOf("VISITOR") >= 0;
}
