package io.etrace.collector.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableScheduling
public class ScheduleConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(2);
        taskScheduler.initialize();
        taskScheduler.setThreadFactory(new ThreadFactory() {
            private AtomicInteger id = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "schedule-task-" + id.incrementAndGet());
            }
        });
        taskRegistrar.setTaskScheduler(taskScheduler);
    }
}
