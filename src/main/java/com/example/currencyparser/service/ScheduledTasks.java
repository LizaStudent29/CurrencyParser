package com.example.currencyparser.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private final RatesCollectorService collectorService;

    public ScheduledTasks(RatesCollectorService collectorService) {
        this.collectorService = collectorService;
    }

    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void scheduledCollect() {
        collectorService.collectRates();
    }
}
