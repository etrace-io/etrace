package io.etrace.api.consts;

import io.etrace.api.model.po.user.ETraceUser;

/**
 * the user info context will cread when pass {@link UserAuthenticationFilter} , but if the path do not need
 * authenticate ,the current context is empty
 */
@Deprecated
public class UserContext {
    private static final ThreadLocal<ETraceUser> USERCONTEXT = new ThreadLocal<>();

    /**
     * 待搞定 Filters 移除这个类
     */
    public static void setUser(ETraceUser ETraceUser) {
        USERCONTEXT.set(ETraceUser);
    }
}
