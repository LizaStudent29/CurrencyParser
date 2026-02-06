package com.example.currencyparser.benchmark;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Бенчмарк "парсинга" строк в объекты тремя способами:
 *   - обычный for
 *   - stream()
 *   - parallelStream()
 */
@BenchmarkMode(Mode.AverageTime)          // измеряем среднее время
@OutputTimeUnit(TimeUnit.MILLISECONDS)    // в миллисекундах
@Warmup(iterations = 3)                   // прогревочные прогоны
@Measurement(iterations = 5)              // прогоны, которые считаются
@Fork(1)                                  // сколько раз запускать JVM
@State(Scope.Thread)                      // состояние на поток
public class ParsingBenchmark {

    private List<String> rawLines;

    /**
     * Подготовка данных: симулируем 10_000 строк "сырых" данных.
     */
    @Setup
    public void setup() {
        rawLines = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) {
            rawLines.add("USD;RUB;" + (90 + i % 10) + ";2024-01-01");
        }
    }

    /**
     * "Парсер" одной строки в объект.
     */
    private CurrencyRate parse(String line) {
        String[] parts = line.split(";");
        CurrencyRate rate = new CurrencyRate();
        rate.base = parts[0];
        rate.quote = parts[1];
        rate.value = Double.parseDouble(parts[2]);
        return rate;
    }

    /**
     * Вариант 1: классический for.
     */
    @Benchmark
    public List<CurrencyRate> forLoopParsing() {
        List<CurrencyRate> result = new ArrayList<>(rawLines.size());
        for (int i = 0; i < rawLines.size(); i++) {
            result.add(parse(rawLines.get(i)));
        }
        return result;
    }

    /**
     * Вариант 2: stream().
     */
    @Benchmark
    public List<CurrencyRate> streamParsing() {
        return rawLines.stream()
                .map(this::parse)
                .collect(Collectors.toList());
    }

    /**
     * Вариант 3: parallelStream().
     */
    @Benchmark
    public List<CurrencyRate> parallelStreamParsing() {
        return rawLines.parallelStream()
                .map(this::parse)
                .collect(Collectors.toList());
    }

    /**
     * Простейший DTO, чтобы не тянуть боевые сущности.
     */
    public static class CurrencyRate {
        String base;
        String quote;
        double value;
    }
}
