package io.etrace.api.config;

import com.google.common.collect.Lists;
import io.etrace.api.consts.RoleType;
import io.etrace.api.filter.UserAuthenticationFilter;
import io.etrace.api.service.UserRoleService;
import io.etrace.api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Web Security Configuration
 */
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//@ConditionalOnProperty(value = "junit.model.flag", havingValue = "false")
// todo： 这里有 Security 1.x 2.x 兼容的问题，先去掉
@Deprecated
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String MOCK_PASSWORD = "123456";
    public static final String[] AUTH_WHITELIST = {
        // -- swagger ui
        "/v2/api-docs",
        "/swagger-resources/configuration/ui",
        "/swagger-resources",
        "/swagger-resources/configuration/security",
        "/swagger-ui.html",
        "/webjars/**",
        /*
        2019.11.13 下面这个API，风控查出无鉴权，因此移除
        */
        //"/app/appId",
        // --health check
        "/health/**",
        //internal notice
        "/datasource/internalNotice",
        "/api/v1/**",

        /*
        2019.11.13 下面这个API，风控查出无鉴权，因此移除
        */
        //"/user/**",

        //"/user/info",

        "/user/checkIncumbent",
        "/user/noticeSSODowngrade",
        "/user/ssodowngrade",
        "/user/getMoziToken",
        "/ding",
        /*
       为迁移api-getaway中的配置接口，以下3个接口不需要验证
        */
        "/api/config/query/cache",
        "/api/config/query/db",
        /*
        2019.11.13 下面这个API，风控查出无鉴权，因此移除
         */
        //"/api/legacy/**",
        /*
       为迁移支持钉钉机器人，提供出去suggest api
        */
        "/api/search-api/**",
        /*
        esm related api
        */
        "/esm/callback/**",
        "/esm/runtime/**",
        "/esm/cross/ezone/**",
        "/esm/plugin/config/**",
        "/esm/agent/statistics/**",
        "/pattern",
        /*
        trace monitor
         */
        "/monitor/**",
        "/limitsql/reloadCache",
        "/task/**",
        "/mail/**",
        "/user/testRedisRateLimit",
        "/user/testRateLimit",
        "/yellowpage/search/suggest",
        "/yellowpage/search/data",
        "/yellowpage/search/home",

        /**
         * watchdog api
        */
        "/api/system/setting/**",
        "/api/bolt/**",
        "/api/policyV2/**",
        "/api/alert/**",
        "/api/data/**",
        "/api/alert/strategy/pause/**",
        "/workflow/callback/**"

    };
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);
    private final UserService userService;
    private final UserRoleService userRoleService;
    @Autowired
    Environment env;

    @Autowired
    public WebSecurityConfig(UserService userService, UserRoleService userRoleService) {
        this.userService = userService;
        this.userRoleService = userRoleService;
    }

    @Bean
    @SuppressWarnings("deprecation")
    public static NoOpPasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder)NoOpPasswordEncoder.getInstance();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //todo: 这里抽离到其他地方
        String ssoCoffeeTokenName = "YOUR_TOKEN_IN_COOKIE";

        List<String> whitePathList = Lists.newArrayList(Arrays.asList(AUTH_WHITELIST));

        //user initialize filter based on sso token
        http.csrf().disable();
        http.addFilterBefore(new UserAuthenticationFilter(userService, userRoleService, ssoCoffeeTokenName),
            SecurityContextPersistenceFilter.class);
        String admin = RoleType.ADMIN.name();
        String user = RoleType.USER.name();
        String[] real_white_list = {};
        http.authorizeRequests()
            // whitelist
            .antMatchers(whitePathList.toArray(real_white_list)).permitAll()
            .mvcMatchers("/datasource/**").hasRole(admin)
            //                .mvcMatchers("/actuator/**").hasRole(admin)
            .mvcMatchers(HttpMethod.POST, "/entity/**").hasRole(admin)
            .mvcMatchers(HttpMethod.PUT, "/entity/**").hasRole(admin)
            .mvcMatchers("/user_role/**").hasRole(admin)
            .mvcMatchers("/proxyConfig/**").hasRole(admin)
            //设置需要有user权限的
            .mvcMatchers(HttpMethod.POST, "/chart/**").hasRole(user)
            .mvcMatchers(HttpMethod.PUT, "/chart/**").hasRole(user)
            .mvcMatchers(HttpMethod.DELETE, "/chart/**").hasRole(user)

            .mvcMatchers(HttpMethod.POST, "/dashboard/**").hasRole(user)
            .mvcMatchers(HttpMethod.PUT, "/dashboard/**").hasRole(user)
            .mvcMatchers(HttpMethod.DELETE, "/dashboard/**").hasRole(user)

            .mvcMatchers(HttpMethod.POST, "/yellowpage/**").hasRole(user)
            .mvcMatchers(HttpMethod.PUT, "/yellowpage/**").hasRole(user)
            .mvcMatchers(HttpMethod.DELETE, "/yellowpage/**").hasRole(user)

            .mvcMatchers(HttpMethod.POST, "/file/**").hasRole(user)
            //                // 数据查询的path
            //                .mvcMatchers(HttpMethod.PUT, "/metric/**").hasRole(user)

            .mvcMatchers("/feedback/**").hasRole(user)

            .mvcMatchers("/token/user/**").hasRole(user)
            .mvcMatchers("/token/admin/**").hasRole(admin)
            .mvcMatchers(HttpMethod.PUT, "/limitsql").hasRole(admin)
            .mvcMatchers(HttpMethod.POST, "/limitsql").hasRole(admin)
            .mvcMatchers(HttpMethod.DELETE, "/limitsql").hasRole(admin)

            .mvcMatchers(HttpMethod.POST, "/pattern/**").hasRole(admin)
            .mvcMatchers(HttpMethod.PUT, "/pattern/**").hasRole(admin)
            .mvcMatchers(HttpMethod.DELETE, "/pattern/**").hasRole(admin)

            .mvcMatchers("/esm/plugin/project/releaseAll").hasRole(admin)
            .anyRequest().authenticated();
    }
}
