


package com.fleetmanagement.deviceservice.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;



/**
 * Redis Configuration
 */
@Configuration
public class RedisConfig {

    @Value("${device-service.caching.device-state-ttl:300}")
    private Integer deviceStateTtl;

    @Value("${device-service.caching.protocol-config-ttl:86400}")
    private Integer protocolConfigTtl;

    @Value("${device-service.caching.device-mapping-ttl:3600}")
    private Integer deviceMappingTtl;

    // Cache TTL values are configured via application properties
    // Redis configuration is handled by Spring Boot auto-configuration

    public Integer getDeviceStateTtl() {
        return deviceStateTtl;
    }

    public Integer getProtocolConfigTtl() {
        return protocolConfigTtl;
    }

    public Integer getDeviceMappingTtl() {
        return deviceMappingTtl;
    }
}
