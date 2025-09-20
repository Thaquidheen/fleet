// MetricsController.java
package com.fleetmanagement.bridgeservice.controller;

import com.fleetmanagement.bridgeservice.utils.ErrorAnalyzer;
import com.fleetmanagement.bridgeservice.utils.PerformanceCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/metrics")
@Slf4j
public class MetricsController {

    private final PerformanceCalculator performanceCalculator;
    private final ErrorAnalyzer errorAnalyzer;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public MetricsController(PerformanceCalculator performanceCalculator,
                             ErrorAnalyzer errorAnalyzer,
                             RedisTemplate<String, Object> redisTemplate) {
        this.performanceCalculator = performanceCalculator;
        this.errorAnalyzer = errorAnalyzer;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            metrics.put("positionsProcessed", performanceCalculator.getCounter("positions"));
            metrics.put("eventsProcessed", performanceCalculator.getCounter("events"));
            metrics.put("errorsCount", errorAnalyzer.getErrorCount("total"));
            metrics.put("timestamp", Instant.now());

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting performance metrics", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getErrorMetrics() {
        try {
            Map<String, Object> errorSummary = errorAnalyzer.getErrorSummary();
            return ResponseEntity.ok(errorSummary);
        } catch (Exception e) {
            log.error("Error getting error metrics", e);
            return ResponseEntity.status(500).build();
        }
    }
}
