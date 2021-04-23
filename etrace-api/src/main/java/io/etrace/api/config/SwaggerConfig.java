package io.etrace.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    public static final String PROXY_REQUEST_TAG = "Proxy Request Api";

    public static final String FOR_ETRACE = "ETrace UI Api";

    public static final String METRIC = "Metric";
    public static final String META = "Application Metadata";
    public static final String MYSQL_DATA = "Data From Mysql";

    @Bean
    public Docket defaultApi() {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("default")
            .select()
            .apis(RequestHandlerSelectors.basePackage("io.etrace.api.controller"))
            .paths(PathSelectors.any())
            .build()
            //.host(host)
            .tags(new Tag(PROXY_REQUEST_TAG, "代理、合并请求的Api"))
            .tags(new Tag(FOR_ETRACE, "ETrace前端页面上所需的API", Integer.MIN_VALUE + 3))
            .tags(new Tag(METRIC, "ETrace前端页面使用的Metric查询API"))
            .tags(new Tag(META, "提供应用元数据相关的查询API"))
            .tags(new Tag(MYSQL_DATA, "查询数据库的API"))
            .apiInfo(metaData())
            .pathMapping("/")
            ;
    }

    private ApiInfo metaData() {
        return new ApiInfoBuilder()
            .title("ETrace-Api doc")
            .description("More info about ETrace Project, please visit https://github.com/etrace-io/etrace.")
            .version("1.0")
            .contact(new Contact("ETrace", "https://github.com/etrace-io/etrace/issues", "etraceioteam@gmail.com"))
            .build();
    }
}
