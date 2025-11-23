package com.example.currencyparser.concurrency;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class LogDaemonStarter {

    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void start() {
        Thread daemon = new Thread(this::processLogs);
        daemon.setDaemon(true);
        daemon.setName("log-daemon-thread");
        daemon.start();

        log("Log daemon started");
    }

    private void processLogs() {
        while (true) {
            try {
                String msg = logQueue.take();
                System.out.printf("[%s] %s%n", LocalDateTime.now(), msg);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void log(String message) {
        logQueue.offer(message);
    }
}
