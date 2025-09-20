package com.fleetmanagement.deviceservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test for Device Service Application
 * 
 * Tests that the Spring Boot application context loads successfully
 * with all required beans and configurations.
 */
@SpringBootTest
@ActiveProfiles("test")
class DeviceServiceApplicationTests {

    @Test
    void contextLoads() {
        // This test will pass if the application context loads successfully
        // which means all configurations, beans, and dependencies are properly set up
    }
}


