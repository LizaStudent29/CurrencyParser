package com.example.currencyparser.service;

import com.example.currencyparser.concurrency.LogDaemonStarter;
import com.example.currencyparser.entity.CurrencyRate;
import com.example.currencyparser.parser.RatesParser;
import com.example.currencyparser.repository.CurrencyRateRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class RatesCollectorService {

    private final List<RatesParser> parsers;
    private final ExecutorService executorService;
    private final CurrencyRateRepository repository;
    private final LogDaemonStarter logDaemon;

    public RatesCollectorService(List<RatesParser> parsers,
                                 @Qualifier("ratesExecutorService") ExecutorService executorService,
                                 CurrencyRateRepository repository,
                                 LogDaemonStarter logDaemon) {
        this.parsers = parsers;
        this.executorService = executorService;
        this.repository = repository;
        this.logDaemon = logDaemon;
    }

    public void collectRates() {
        logDaemon.log("Запуск сбора курсов. Парсеров: " + parsers.size());

        List<Future<List<CurrencyRate>>> futures = new ArrayList<>();
        for (RatesParser parser : parsers) {
            futures.add(executorService.submit(parser::parseRates));
        }

        List<CurrencyRate> allRates = new ArrayList<>();
        for (Future<List<CurrencyRate>> future : futures) {
            try {
                List<CurrencyRate> parsed = future.get(60, TimeUnit.SECONDS);
                allRates.addAll(parsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException | TimeoutException e) {
                logDaemon.log("Ошибка при выполнении парсера: " + e.getMessage());
            }
        }

        repository.saveAll(allRates);
        logDaemon.log("Сохранено курсов: " + allRates.size());
    }
}
