package org.saphka.locationtracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@Configuration
public class DatabaseConfiguration {

    @Bean
    public Scheduler jdbcScheduler(@Value("${spring.datasource.hikari.maximum-pool-size}") Integer poolSize) {
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(poolSize));
    }

}
