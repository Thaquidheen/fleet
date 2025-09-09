package com.fleetmanagement.userservice.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationStatus {

    private boolean isVerified;
    private LocalDateTime verifiedAt;
    private String email;
    private boolean canResend;
    private boolean hasPendingToken;
    private LocalDateTime tokenExpiryDate;
    private String message;
}