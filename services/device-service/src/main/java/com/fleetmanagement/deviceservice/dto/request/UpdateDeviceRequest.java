
package com.fleetmanagement.deviceservice.dto.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fleetmanagement.deviceservice.domain.enums.DeviceStatus;
import com.fleetmanagement.deviceservice.domain.valueobjects.DeviceConfiguration;
import jakarta.validation.constraints.Size;
import lombok.*;



/**
 * Update Device Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateDeviceRequest {

    @Size(max = 100, message = "Device name must be less than 100 characters")
    private String deviceName;

    private DeviceStatus status;
    private DeviceConfiguration configuration;

    private String serialNumber;
    private String firmwareVersion;
    private String hardwareVersion;

    private String notes;
}
