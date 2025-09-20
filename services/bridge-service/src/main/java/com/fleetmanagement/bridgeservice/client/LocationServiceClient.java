package com.fleetmanagement.bridgeservice.client;

import com.fleetmanagement.bridgeservice.model.domain.LocationData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "location-service", path = "/locations")
public interface LocationServiceClient {

    @PostMapping("/process")
    void processLocationData(@RequestBody LocationData locationData);

    @PostMapping("/validate")
    boolean validateLocationData(@RequestBody LocationData locationData);
}