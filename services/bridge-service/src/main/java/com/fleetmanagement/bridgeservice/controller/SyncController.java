// SyncController.java
package com.fleetmanagement.bridgeservice.controller;

import com.fleetmanagement.bridgeservice.service.TraccarBridgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sync")
@Slf4j
public class SyncController {

    private final TraccarBridgeService bridgeService;

    @Autowired
    public SyncController(TraccarBridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerManualSync() {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Manual sync triggered via API");
            bridgeService.triggerManualSync();

            response.put("status", "success");
            response.put("message", "Manual sync completed successfully");
            response.put("timestamp", Instant.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Manual sync failed", e);
            response.put("status", "error");
            response.put("message", "Manual sync failed: " + e.getMessage());
            response.put("timestamp", Instant.now());

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<TraccarBridgeService.SyncStatistics> getSyncStatistics() {
        try {
            TraccarBridgeService.SyncStatistics stats = bridgeService.getSyncStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting sync statistics", e);
            return ResponseEntity.status(500).build();
        }
    }
}
