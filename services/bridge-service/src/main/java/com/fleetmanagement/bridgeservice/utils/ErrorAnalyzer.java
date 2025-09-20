package com.fleetmanagement.bridgeservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class ErrorAnalyzer {

    private final ConcurrentHashMap<String, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> lastErrors = new ConcurrentHashMap<>();

    public void recordError(String errorType, Throwable error) {
        String key = errorType + ":" + error.getClass().getSimpleName();

        errorCounts.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        lastErrors.put(key, Instant.now());

        log.debug("Recorded error: {} (count: {})", key, errorCounts.get(key).get());
    }

    public int getErrorCount(String errorType) {
        return errorCounts.values().stream()
                .filter(count -> count.get() > 0)
                .mapToInt(AtomicInteger::get)
                .sum();
    }

    public Map<String, Object> getErrorSummary() {
        Map<String, Object> summary = new HashMap<>();

        int totalErrors = errorCounts.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();

        summary.put("totalErrors", totalErrors);
        summary.put("errorTypes", errorCounts.size());
        summary.put("lastErrorTime", getLastErrorTime());

        return summary;
    }

    public boolean hasRecentErrors(Duration duration) {
        Instant threshold = Instant.now().minus(duration);
        return lastErrors.values().stream()
                .anyMatch(errorTime -> errorTime.isAfter(threshold));
    }

    private Instant getLastErrorTime() {
        return lastErrors.values().stream()
                .max(Instant::compareTo)
                .orElse(null);
    }

    public void resetErrors() {
        errorCounts.clear();
        lastErrors.clear();
        log.info("Error counters reset");
    }
}