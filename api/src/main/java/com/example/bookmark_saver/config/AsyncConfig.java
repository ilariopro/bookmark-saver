package com.example.bookmark_saver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous task execution.
 *
 * Defines the {@code metadataExecutor} thread pool used for
 * background metadata enrichment tasks.
 */
@Configuration
public class AsyncConfig {
    /**
     * Configures the thread pool executor for metadata enrichment tasks.
     * Uses a core pool of 2 threads, up to 4 max, with a queue capacity of 50.
     *
     * @return The configured {@link Executor} bean.
     */
    @Bean(name = "metadataExecutor")
    public Executor metadataExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("metadata-");

        executor.initialize();

        return executor;
    }
}