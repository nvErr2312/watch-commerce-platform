package com.fullstack.orderservice.config;

import org.axonframework.config.ConfigurerModule;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AxonDeadlineConfig {
    @Bean
    ConfigurerModule deadlineManagerConfigurer(PlatformTransactionManager transactionManager) {
        return configurer -> configurer.configureDeadlineManager(configuration -> SimpleDeadlineManager.builder()
                .scopeAwareProvider(configuration.scopeAwareProvider())
                .transactionManager(new SpringTransactionManager(transactionManager))
                .spanFactory(configuration.spanFactory())
                .build());
    }
}
