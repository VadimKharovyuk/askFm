package com.example.askfm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // Минимальное количество потоков
        executor.setMaxPoolSize(50);   // Максимальное количество потоков
        executor.setQueueCapacity(2000); // Очередь на 2000 задач
        executor.setThreadNamePrefix("NotificationExecutor-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "likeExecutor")
    public Executor likeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("LikeExecutor-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "commentExecutor")
    public Executor commentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(15);
        executor.setMaxPoolSize(75);
        executor.setQueueCapacity(3000);
        executor.setThreadNamePrefix("CommentExecutor-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "eventExecutor")
    public Executor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix("EventExecutor-");
        executor.initialize();
        return executor;
    }
}
