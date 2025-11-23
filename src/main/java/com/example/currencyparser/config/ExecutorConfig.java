package com.example.currencyparser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean
    public ExecutorService ratesExecutorService() {
        // 4-поточный пул для парсеров
        return Executors.newFixedThreadPool(4);
    }
}
