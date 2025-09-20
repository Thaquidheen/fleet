
package com.fleetmanagement.deviceservice.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignDeviceRequest {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    private UUID vehicleId;
    private UUID userId;

    @NotNull(message = "Assigned by user ID is required")
    private UUID assignedBy;

    private String installationLocation;
    private String installationTechnician;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime installationDate;

    private String notes;
}
