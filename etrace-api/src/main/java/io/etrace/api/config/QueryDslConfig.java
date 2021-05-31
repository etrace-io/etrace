package io.etrace.api.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

@Configuration
public class QueryDslConfig {

    @Bean
    //    public JPAQueryFactory jpaQueryFactory(@Qualifier("entityManager") EntityManager entityManager) {
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    //    @Bean
    //    public JPAQueryFactory watchdogJpaQueryFactory(@Qualifier("watchdogEntityManager") EntityManager
    //    entityManager) {
    //        return new JPAQueryFactory(entityManager);
    //    }
}
