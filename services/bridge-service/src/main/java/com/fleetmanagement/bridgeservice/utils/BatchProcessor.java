// BatchProcessor.java
package com.fleetmanagement.bridgeservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BatchProcessor {

    public <T> List<List<T>> createBatches(List<T> items, int batchSize) {
        List<List<T>> batches = new ArrayList<>();

        if (items.isEmpty()) {
            return batches;
        }

        for (int i = 0; i < items.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, items.size());
            batches.add(new ArrayList<>(items.subList(i, endIndex)));
        }

        log.debug("Created {} batches from {} items with batch size {}",
                batches.size(), items.size(), batchSize);

        return batches;
    }

    public <T> void processBatchSafely(List<T> batch, BatchOperation<T> operation) {
        for (T item : batch) {
            try {
                operation.process(item);
            } catch (Exception e) {
                log.error("Error processing batch item", e);
                // Continue processing other items
            }
        }
    }

    @FunctionalInterface
    public interface BatchOperation<T> {
        void process(T item) throws Exception;
    }
}