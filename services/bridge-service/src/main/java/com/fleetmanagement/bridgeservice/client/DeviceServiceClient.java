package com.fleetmanagement.bridgeservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "device-service", path = "/devices")
public interface DeviceServiceClient {

    @GetMapping("/traccar/{traccarId}/device-id")
    UUID getDeviceByTraccarId(@PathVariable("traccarId") Long traccarId);

    @GetMapping("/{deviceId}/company-id")
    UUID getCompanyIdByDeviceId(@PathVariable("deviceId") UUID deviceId);

    @GetMapping("/{deviceId}/active")
    boolean isDeviceActive(@PathVariable("deviceId") UUID deviceId);

    @GetMapping("/traccar/{traccarId}/name")
    String getDeviceNameByTraccarId(@PathVariable("traccarId") Long traccarId);
}