package com.fleetmanagement.bridgeservice.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component
public class DataFormatter {

    public Double formatCoordinate(Double coordinate) {
        if (coordinate == null) return null;
        return BigDecimal.valueOf(coordinate)
                .setScale(6, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public Double formatSpeed(Double speed) {
        if (speed == null) return null;
        // Convert knots to km/h and round to 2 decimal places
        double kmh = speed * 1.852;
        return BigDecimal.valueOf(kmh)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public String formatTimestamp(Instant timestamp) {
        if (timestamp == null) return null;
        return DateTimeFormatter.ISO_INSTANT.format(timestamp);
    }

    public Double formatSensorValue(Double value, String unit) {
        if (value == null) return null;

        int scale = switch (unit.toUpperCase()) {
            case "TEMPERATURE", "FUEL", "BATTERY" -> 1;
            case "PRESSURE", "WEIGHT" -> 2;
            default -> 2;
        };

        return BigDecimal.valueOf(value)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
