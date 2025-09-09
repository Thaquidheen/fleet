package com.fleetmanagement.userservice.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequiredResponse {

    private UUID userId;
    private boolean isRequired;
    private String message;
    private String reason;
}