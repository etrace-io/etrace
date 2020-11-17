package io.etrace.api.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.etrace.agent.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    public static final String THREAD_POOL_TASK_EXECUTOR = "threadPoolTaskExecutor";
    //todo: this 3 executor not used?
    public static final String PROXY_THREAD_POOL_TASK_EXECUTOR = "proxyThreadPoolTaskExecutor";
    public static final String REDIS_RATE_LIMIT_TASK_EXECUTOR = "redisRateLimitTaskExcutor";
    public static final String AsyncRateLimitThreadPoolExecutor = "AsyncRateLimitThreadPoolExecutor";
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(AsyncRateLimitThreadPoolExecutor)
    public ThreadPoolExecutor AsyncRateLimitThreadPoolExecutor() {
        return new ThreadPoolExecutor(
            40,
            40,
            30,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(500),
            new ThreadFactoryBuilder().setNameFormat("async-rateLimit-thread-%d").build(),
            (r, executor) -> Trace.newCounter("AsyncRateLimitReject").once());
    }

    @Bean
    @Primary
    public Executor mainThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(100);
        executor.setThreadNamePrefix("main-thread-pool-");
        executor.initialize();
        return executor;
    }

    @Bean(name = THREAD_POOL_TASK_EXECUTOR)
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(40);
        executor.setMaxPoolSize(40);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("lindb-query-thread-pool-");
        executor.initialize();
        return executor;
    }

    @Bean(name = PROXY_THREAD_POOL_TASK_EXECUTOR)
    public Executor proxyThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(40);
        executor.setMaxPoolSize(40);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("proxy-thread-pool-");
        executor.initialize();
        return executor;
    }

    @Bean(name = REDIS_RATE_LIMIT_TASK_EXECUTOR)
    public Executor redisRateLimitTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(40);
        executor.setMaxPoolSize(40);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("redis-ratelimit-thread-pool-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (arg0, arg1, arg2) -> {
            logger.error(arg0.getMessage(), arg0, ", exception method:" + arg1.getName());
        };
    }

}
