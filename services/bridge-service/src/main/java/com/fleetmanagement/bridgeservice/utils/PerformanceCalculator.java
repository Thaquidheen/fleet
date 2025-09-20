package com.fleetmanagement.bridgeservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class PerformanceCalculator {

    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> timers = new ConcurrentHashMap<>();

    public void incrementCounter(String metric) {
        counters.computeIfAbsent(metric, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void startTimer(String operation) {
        timers.put(operation, Instant.now());
    }

    public long stopTimer(String operation) {
        Instant start = timers.remove(operation);
        if (start == null) {
            log.warn("Timer not found for operation: {}", operation);
            return -1;
        }

        long duration = Duration.between(start, Instant.now()).toMillis();
        log.debug("Operation {} took {} ms", operation, duration);
        return duration;
    }

    public long getCounter(String metric) {
        AtomicLong counter = counters.get(metric);
        return counter != null ? counter.get() : 0;
    }

    public void resetCounters() {
        counters.clear();
        timers.clear();
        log.info("Performance counters reset");
    }

    public double calculateThroughput(String metric, Duration period) {
        long count = getCounter(metric);
        double seconds = period.toMillis() / 1000.0;
        return count / seconds;
    }
}