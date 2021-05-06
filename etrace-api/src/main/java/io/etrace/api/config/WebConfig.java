package io.etrace.api.config;

import io.etrace.api.filter.GlobalFilter;
import io.etrace.api.service.UserService;
import io.etrace.plugins.interceptors.TraceFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserService userService;

    @Autowired
    private ApiTokenService apiTokenService;

    @Autowired
    @Qualifier(CustomRateLimitConfig.OPEN_API_RATE_LIMIT)
    private BaseRateLimitService baseRateLimitService;

    @Bean
    public FilterRegistrationBean<Filter> globalFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new GlobalFilter());
        registration.addUrlPatterns("/*");
        registration.setName("GlobalFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<Filter> registerTraceFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        // add trace filter
        registration.setFilter(new TraceFilter());
        Map<String, String> filterConfig = new HashMap<>(1);
        String pattern
            = "metric;chart;app;dashboard;dashboardApp;config;datasource;department;esm;host;entity;feedback;"
            + "user-action;user;user_role;api/query;api/suggest;policy;label;record";
        filterConfig.put("trace-url", pattern);
        registration.setInitParameters(filterConfig);
        registration.addUrlPatterns("/*");
        registration.setName("traceFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<Filter> registerApiTokenFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        ApiTokenFilter apiTokenFilter = new ApiTokenFilter(apiTokenService, userService, baseRateLimitService);
        String pattern = "metric;watchdog;sampling;order;callstack;rpcId;queue";
        apiTokenFilter.initPathPattern(pattern);
        registration.setFilter(apiTokenFilter);

        registration.addUrlPatterns("/api/v1/*");
        registration.setName("apiTokenFilter");
        return registration;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowCredentials(true)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //for swagger resource handlers
        registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
