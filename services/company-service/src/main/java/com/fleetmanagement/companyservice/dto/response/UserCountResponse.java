// UserCountResponse.java
package com.fleetmanagement.companyservice.dto.response;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCountResponse {
    private int count;
    private String message;
    private java.time.LocalDateTime countedAt;
    private boolean fromCache;
}