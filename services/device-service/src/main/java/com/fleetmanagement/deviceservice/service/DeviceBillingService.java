
package com.fleetmanagement.deviceservice.service;

import com.fleetmanagement.deviceservice.domain.enums.SensorType;
import com.fleetmanagement.deviceservice.dto.response.BillingReportResponse;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Device Billing Service Interface
 * Device and sensor billing operations
 */
public interface DeviceBillingService {

    /**
     * Calculate device monthly cost
     */
    Double calculateDeviceMonthlyCost(String deviceId);

    /**
     * Calculate company monthly cost
     */
    Double calculateCompanyMonthlyCost(UUID companyId);

    /**
     * Generate billing report
     */
    BillingReportResponse generateBillingReport(UUID companyId, LocalDateTime from, LocalDateTime to);

    /**
     * Process monthly billing
     */
    void processMonthlyBilling();

    /**
     * Update sensor pricing
     */
    void updateSensorPricing(SensorType sensorType, Double basePrice, Double maxPrice);

    /**
     * Get billing summary
     */
    BillingSummaryResponse getBillingSummary(UUID companyId);
}

