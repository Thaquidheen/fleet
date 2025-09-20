

package com.fleetmanagement.deviceservice.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;



/**
 * Device Service Specific Configuration
 */
@Configuration
public class DeviceServiceConfig {

    @Value("${device-service.tcp-server.enabled:true}")
    private Boolean tcpServerEnabled;

    @Value("${device-service.tcp-server.port:8090}")
    private Integer tcpServerPort;

    @Value("${device-service.tcp-server.max-connections:10000}")
    private Integer maxConnections;

    @Value("${device-service.tcp-server.idle-timeout:300}")
    private Integer idleTimeout;

    @Value("${device-service.protocols.teltonika.enabled:true}")
    private Boolean teltonikaEnabled;

    @Value("${device-service.protocols.teltonika.port:8091}")
    private Integer teltonikaPort;

    @Value("${device-service.protocols.queclink.enabled:true}")
    private Boolean queclinkEnabled;

    @Value("${device-service.protocols.queclink.port:8092}")
    private Integer queclinkPort;

    @Value("${device-service.protocols.concox.enabled:true}")
    private Boolean concoxEnabled;

    @Value("${device-service.protocols.concox.port:8093}")
    private Integer concoxPort;

    @Value("${device-service.billing.sensor-pricing-enabled:true}")
    private Boolean sensorPricingEnabled;

    @Value("${device-service.billing.billing-cycle-days:30}")
    private Integer billingCycleDays;

    // Getters
    public Boolean getTcpServerEnabled() { return tcpServerEnabled; }
    public Integer getTcpServerPort() { return tcpServerPort; }
    public Integer getMaxConnections() { return maxConnections; }
    public Integer getIdleTimeout() { return idleTimeout; }
    public Boolean getTeltonikaEnabled() { return teltonikaEnabled; }
    public Integer getTeltonikaPort() { return teltonikaPort; }
    public Boolean getQueclinkEnabled() { return queclinkEnabled; }
    public Integer getQueclinkPort() { return queclinkPort; }
    public Boolean getConcoxEnabled() { return concoxEnabled; }
    public Integer getConcoxPort() { return concoxPort; }
    public Boolean getSensorPricingEnabled() { return sensorPricingEnabled; }
    public Integer getBillingCycleDays() { return billingCycleDays; }
}