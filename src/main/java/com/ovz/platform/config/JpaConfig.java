package com.ovz.platform.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.ovz.platform.models")
@EnableJpaRepositories(basePackages = "com.ovz.platform.repositories")
public class JpaConfig {
    // Конфигурация JPA будет подхвачена автоматически из application.properties
}