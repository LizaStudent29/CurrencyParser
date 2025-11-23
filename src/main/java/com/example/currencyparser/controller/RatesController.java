package com.example.currencyparser.controller;

import com.example.currencyparser.entity.CurrencyRate;
import com.example.currencyparser.parser.CbrRatesParser;
import com.example.currencyparser.repository.CurrencyRateRepository;
import com.example.currencyparser.service.RatesCollectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/rates")
@Tag(name = "Rates", description = "Операции с курсами валют ЦБ РФ")
public class RatesController {

    private final CurrencyRateRepository repository;
    private final RatesCollectorService collectorService;
    private final CbrRatesParser cbrRatesParser;

    public RatesController(CurrencyRateRepository repository,
                           RatesCollectorService collectorService,
                           CbrRatesParser cbrRatesParser) {
        this.repository = repository;
        this.collectorService = collectorService;
        this.cbrRatesParser = cbrRatesParser;
    }

    @PostMapping("/collect")
    @Operation(summary = "Запустить сбор курсов за сегодня",
            description = "Запускает парсер ЦБ РФ и сохраняет курсы за текущую дату в БД.")
    public ResponseEntity<String> collectNow() {
        collectorService.collectRates();
        return ResponseEntity.ok("Сбор курсов запущен");
    }

    @GetMapping
    @Operation(summary = "Получить все сохранённые курсы",
            description = "Возвращает все курсы из БД с возможностью сортировки (демонстрация parallelStream).")
    public List<CurrencyRate> getAll(
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        List<CurrencyRate> all = repository.findAll();

        Comparator<CurrencyRate> comparator = switch (sortBy) {
            case "rate" -> Comparator.comparing(CurrencyRate::getRate);
            case "code" -> Comparator.comparing(CurrencyRate::getCode);
            default -> Comparator.comparing(CurrencyRate::getDate);
        };

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        return all.parallelStream()
                .sorted(comparator)
                .toList();
    }

    /**
     * Онлайн‑запрос к ЦБ РФ: всегда получает курсы непосредственно с сайта ЦБ на указанную дату,
     * не опираясь на уже сохранённые данные.
     *
     * Пример: GET /rates/2025-11-20/USD
     */
    @GetMapping("/{date}/{code}")
    @Operation(summary = "Онлайн‑курс по дате и валюте",
            description = "Делает запрос к ЦБ РФ на указанную дату и возвращает актуальный курс для данной валюты.")
    public ResponseEntity<CurrencyRate> getByDateAndCode(
            @PathVariable String date,
            @PathVariable String code
    ) {
        LocalDate parsedDate = LocalDate.parse(date);

        List<CurrencyRate> ratesForDate = cbrRatesParser.parseRatesForDate(parsedDate);
        return ratesForDate.stream()
                .filter(r -> r.getCode().equalsIgnoreCase(code))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
