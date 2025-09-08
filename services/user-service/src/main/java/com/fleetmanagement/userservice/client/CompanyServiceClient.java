package com.fleetmanagement.userservice.client;

import com.fleetmanagement.userservice.dto.response.CompanyValidationResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(
        name = "company-service",
        path = "/api/companies"
)
public interface CompanyServiceClient {

    @GetMapping("/{companyId}/validation/user-limit")
    @CircuitBreaker(name = "company-service", fallbackMethod = "validateUserLimitFallback")
    ResponseEntity<CompanyValidationResponse> validateUserLimit(@PathVariable UUID companyId);

    @PostMapping("/{companyId}/users/increment")
    @CircuitBreaker(name = "company-service", fallbackMethod = "incrementUserCountFallback")
    ResponseEntity<Void> incrementUserCount(@PathVariable UUID companyId);

    @PostMapping("/{companyId}/users/decrement")
    @CircuitBreaker(name = "company-service", fallbackMethod = "decrementUserCountFallback")
    ResponseEntity<Void> decrementUserCount(@PathVariable UUID companyId);

    // Fallback methods
    default ResponseEntity<CompanyValidationResponse> validateUserLimitFallback(UUID companyId, Exception ex) {
        CompanyValidationResponse fallback = CompanyValidationResponse.builder()
                .canAddUser(true) // Allow user creation when service is down
                .currentUsers(0)
                .maxUsers(1000)
                .message("Service unavailable - using fallback validation")
                .build();
        return ResponseEntity.ok(fallback);
    }

    default ResponseEntity<Void> incrementUserCountFallback(UUID companyId, Exception ex) {
        // Log the failure but don't prevent user creation
        return ResponseEntity.ok().build();
    }

    default ResponseEntity<Void> decrementUserCountFallback(UUID companyId, Exception ex) {
        // Log the failure but don't prevent user deletion
        return ResponseEntity.ok().build();
    }
}