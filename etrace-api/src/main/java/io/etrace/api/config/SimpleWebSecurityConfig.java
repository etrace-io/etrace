/*
 * Copyright 2020 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.api.config;

import io.etrace.api.consts.RoleType;
import io.etrace.api.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
// https://docs.spring.io/spring-security/site/docs/5.1.10.RELEASE/reference/htmlsingle/#enableglobalmethodsecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SimpleWebSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String MOCK_PASSWORD = "123456";

    public static final String[] AUTH_WHITELIST = {
        "/actuator",
        // -- swagger ui
        "/swagger-resources/**",
        "/swagger-ui.html",
        "/v2/api-docs",
        "/api/v2/api-docs",
        "/webjars/**",
    };

    @Autowired
    MyUserDetailsService myUserDetailsService;

    //@Override
    //protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    //    auth.inMemoryAuthentication()
    //        .withUser("user").password("password").roles("USER").and()
    //        .withUser("admin").password("password").roles("ADMIN");
    //}

    /**
     * NoOpPasswordEncoder is not a secure solution.
     * <p>
     * https://docs.spring.io/spring-security/site/docs/5.1.10.RELEASE/reference/htmlsingle/#troubleshooting
     */
    //@Bean
    public static PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService).passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String admin = RoleType.ADMIN.name();
        String user = RoleType.USER.name();

        http.authorizeRequests()
            // whitelist
            .antMatchers(AUTH_WHITELIST).permitAll()
            .mvcMatchers("/datasource/**").hasRole(admin)
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
        ;

        //.anyRequest().anonymous();
        http
            .formLogin()
            .permitAll();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/swagger-ui.html",
            "/swagger-ui.html/**",
            "/webjars/**");
    }
}
