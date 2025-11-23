package com.example.currencyparser.service;

import com.example.currencyparser.concurrency.LogDaemonStarter;
import com.example.currencyparser.entity.CurrencyRate;
import com.example.currencyparser.parser.RatesParser;
import com.example.currencyparser.repository.CurrencyRateRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RatesCollectorServiceTest {

    @Test
    void collectRates_savesParsedData() {
        RatesParser parser = Mockito.mock(RatesParser.class);
        CurrencyRate rate = new CurrencyRate(
                "USD",
                "Доллар США",
                LocalDate.now(),
                new BigDecimal("95.50"),
                null,
                "test"
        );
        Mockito.when(parser.parseRates()).thenReturn(List.of(rate));

        CurrencyRateRepository repo = Mockito.mock(CurrencyRateRepository.class);
        ExecutorService executor = Executors.newFixedThreadPool(1);

        LogDaemonStarter logDaemon = new LogDaemonStarter();

        RatesCollectorService service = new RatesCollectorService(
                List.of(parser),
                executor,
                repo,
                logDaemon
        );

        service.collectRates();

        verify(repo, times(1)).saveAll(Mockito.anyList());
    }
}
