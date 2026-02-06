package com.example.currencyparser.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RestController
public class PerformanceController {

    @GetMapping("/slow-classic")
    public String slowClassic() throws InterruptedException {
        // Имитация долгой работы на обычном потоке
        Thread.sleep(5000);
        return "classic-ok";
    }

    @GetMapping("/slow-virtual")
    public String slowVirtual() throws Exception {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            Future<String> future = executor.submit(() -> {
                Thread.sleep(5000);
                return "virtual-ok";
            });
            return future.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdown();
        }
    }
}
