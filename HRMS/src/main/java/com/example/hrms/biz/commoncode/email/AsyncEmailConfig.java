package com.example.hrms.biz.commoncode.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncEmailConfig {

  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    // Set core pool size - the number of threads to keep in the pool
    executor.setCorePoolSize(2);
    // Set max pool size - the maximum number of threads to allow in the pool
    executor.setMaxPoolSize(5);
    // Set queue capacity
    executor.setQueueCapacity(10);
    // Set thread name prefix
    executor.setThreadNamePrefix("EmailThread-");
    // Initialize the executor
    executor.initialize();
    return executor;
  }
}