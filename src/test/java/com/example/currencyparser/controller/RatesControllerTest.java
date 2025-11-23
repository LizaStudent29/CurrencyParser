package com.example.currencyparser.controller;

import com.example.currencyparser.entity.CurrencyRate;
import com.example.currencyparser.parser.CbrRatesParser;
import com.example.currencyparser.repository.CurrencyRateRepository;
import com.example.currencyparser.service.RatesCollectorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RatesControllerTest {

    @Test
    void getAllRates_returnsListFromRepository() {
        CurrencyRateRepository repository = Mockito.mock(CurrencyRateRepository.class);
        RatesCollectorService collectorService = null;
        CbrRatesParser cbrRatesParser = null;

        CurrencyRate rate = new CurrencyRate(
                "USD",
                "Доллар США",
                LocalDate.of(2025, 11, 22),
                new BigDecimal("95.50"),
                null,
                "test"
        );
        Mockito.when(repository.findAll()).thenReturn(List.of(rate));

        RatesController controller = new RatesController(repository, collectorService, cbrRatesParser);

        List<CurrencyRate> result = controller.getAll("date", "asc");

        assertEquals(1, result.size());
        assertEquals("USD", result.get(0).getCode());
    }
}
