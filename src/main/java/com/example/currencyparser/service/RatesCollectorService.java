package com.example.currencyparser.service;

import com.example.currencyparser.concurrency.LogDaemonStarter;
import com.example.currencyparser.entity.CurrencyRate;
import com.example.currencyparser.parser.RatesParser;
import com.example.currencyparser.repository.CurrencyRateRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class RatesCollectorService {

    private final List<RatesParser> parsers;
    private final ExecutorService executorService;
    private final CurrencyRateRepository repository;
    private final LogDaemonStarter logDaemon;
    private final Counter parsingSuccessCounter;
    private final Counter parsingErrorCounter;
    private final Counter savedRecordsCounter;


    // Micrometer
    private final MeterRegistry meterRegistry;
    private final Timer parsingTimer;

    public RatesCollectorService(List<RatesParser> parsers,
                                 @Qualifier("ratesExecutorService") ExecutorService executorService,
                                 CurrencyRateRepository repository,
                                 LogDaemonStarter logDaemon,
                                 MeterRegistry meterRegistry) {
        this.parsers = parsers;
        this.executorService = executorService;
        this.repository = repository;
        this.logDaemon = logDaemon;
        this.meterRegistry = meterRegistry;
        this.parsingSuccessCounter = meterRegistry.counter(
            "currency_parser_parsing_success_total"
        );

        this.parsingErrorCounter = meterRegistry.counter(
             "currency_parser_parsing_error_total"
        );


        this.parsingTimer = meterRegistry.timer(
                "currency_parser_parsing_duration_seconds",
                "operation", "collectRates"
        );

        this.savedRecordsCounter = meterRegistry.counter(
            "currency_parser_saved_records_total"
        );
    }

    public void collectRates() {
        logDaemon.log("Запуск сбора курсов. Парсеров: " + parsers.size());

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            List<Future<List<CurrencyRate>>> futures = new ArrayList<>();
            for (RatesParser parser : parsers) {
                futures.add(executorService.submit(parser::parseRates));
            }

            List<CurrencyRate> allRates = new ArrayList<>();
            for (Future<List<CurrencyRate>> future : futures) {
                try {
                    List<CurrencyRate> parsed = future.get(60, TimeUnit.SECONDS);
                    allRates.addAll(parsed);

                    parsingSuccessCounter.increment();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    parsingErrorCounter.increment();
                } catch (ExecutionException | TimeoutException e) {
                    logDaemon.log("Ошибка при выполнении парсера: " + e.getMessage());
                    parsingErrorCounter.increment();
                }
            }

            repository.saveAll(allRates);
            savedRecordsCounter.increment(allRates.size());
            logDaemon.log("Сохранено курсов: " + allRates.size());

        } finally {
            sample.stop(parsingTimer);
        }
    }
}
