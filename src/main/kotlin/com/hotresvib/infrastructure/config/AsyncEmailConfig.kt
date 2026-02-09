package com.hotresvib.infrastructure.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.context.annotation.Bean
import java.util.concurrent.Executor

/**
 * Configuration for async email processing and scheduled jobs
 */
@Configuration
@EnableAsync
@EnableScheduling
class AsyncEmailConfig {

    /**
     * Configure thread pool for async email tasks
     */
    @Bean("emailTaskExecutor")
    fun emailTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.setCorePoolSize(5)
        executor.setMaxPoolSize(10)
        executor.setQueueCapacity(100)

        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.initialize()
        return executor
    }

    /**
     * Configure thread pool for scheduled reminder jobs
     */
    @Bean("scheduledTaskExecutor")
    fun scheduledTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.setCorePoolSize(2)
        executor.setMaxPoolSize(5)
        executor.setQueueCapacity(50)

        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.initialize()
        return executor
    }
}
