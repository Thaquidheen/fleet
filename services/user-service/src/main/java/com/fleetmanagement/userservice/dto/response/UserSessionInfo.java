package com.fleetmanagement.userservice.dto.response;

import com.fleetmanagement.userservice.domain.enums.UserRole;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionInfo {

    private String sessionId;
    private UUID userId;
    private String username;
    private String email;
    private UserRole role;
    private UUID companyId;
    private String deviceInfo;
    private String ipAddress;
    private String jwtToken;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
    private boolean isActive;
    private String userAgent;
    private String location;
}
