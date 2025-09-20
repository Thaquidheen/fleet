# Device Service

The Device Service is a microservice responsible for managing fleet devices, sensors, and their configurations in the Fleet Management System. It provides comprehensive device lifecycle management, real-time monitoring, and integration with external tracking systems like Traccar.

## Features

### Core Functionality
- **Device Management**: Register, update, and manage various types of fleet devices
- **Sensor Management**: Handle sensor subscriptions and data collection
- **Mobile Device Support**: Manage mobile devices and their configurations
- **Device Health Monitoring**: Real-time health status tracking and alerts
- **Command Execution**: Send commands to devices and track execution status
- **Device Assignment**: Assign devices to vehicles and users

### Integration Capabilities
- **Traccar Integration**: Seamless integration with Traccar GPS tracking system
- **External Service Communication**: Integration with Company, Vehicle, User, and Payment services
- **Event-Driven Architecture**: Publishes and consumes events for real-time updates
- **Caching**: Redis-based caching for improved performance

### Security & Monitoring
- **Security**: Role-based access control and device permission management
- **Metrics**: Comprehensive metrics collection and monitoring
- **Health Checks**: Built-in health monitoring and status reporting
- **Logging**: Structured logging for debugging and monitoring

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL with Flyway migrations
- **Cache**: Redis
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **API Documentation**: OpenAPI 3 (Swagger)
- **Monitoring**: Micrometer with Prometheus
- **Mapping**: MapStruct

### Key Components

#### Controllers
- `DeviceController`: Main device management endpoints
- `DeviceTypeController`: Device type management
- `SensorController`: Sensor management and subscriptions
- `MobileDeviceController`: Mobile device specific operations
- `DeviceHealthController`: Health monitoring endpoints
- `DeviceCommandController`: Command execution endpoints

#### Services
- `DeviceService`: Core device business logic
- `DeviceRegistrationService`: Device registration and onboarding
- `SensorSubscriptionService`: Sensor subscription management
- `MobileDeviceService`: Mobile device operations
- `DeviceHealthService`: Health monitoring and alerts
- `TraccarIntegrationService`: External Traccar integration
- `DeviceBillingService`: Billing and subscription management

#### External Clients
- `TraccarApiClient`: Traccar API integration
- `CompanyServiceClient`: Company service communication
- `VehicleServiceClient`: Vehicle service communication
- `UserServiceClient`: User service communication
- `PaymentServiceClient`: Payment service communication

## API Endpoints

### Device Management
- `GET /api/devices` - List all devices
- `GET /api/devices/{id}` - Get device details
- `POST /api/devices` - Register new device
- `PUT /api/devices/{id}` - Update device
- `DELETE /api/devices/{id}` - Delete device

### Device Types
- `GET /api/device-types` - List device types
- `POST /api/device-types` - Create device type
- `PUT /api/device-types/{id}` - Update device type

### Sensors
- `GET /api/sensors` - List sensors
- `POST /api/sensors/subscribe` - Subscribe to sensor
- `DELETE /api/sensors/{id}/unsubscribe` - Unsubscribe from sensor

### Mobile Devices
- `GET /api/mobile-devices` - List mobile devices
- `POST /api/mobile-devices` - Register mobile device
- `PUT /api/mobile-devices/{id}` - Update mobile device

### Health Monitoring
- `GET /api/health/devices` - Get device health status
- `GET /api/health/devices/{id}` - Get specific device health

### Commands
- `POST /api/commands` - Send command to device
- `GET /api/commands/{id}/status` - Get command status

## Configuration

### Application Properties
The service supports multiple profiles:
- `application.yml`: Base configuration
- `application-dev.yml`: Development environment
- `application-prod.yml`: Production environment
- `application-test.yml`: Test environment

### Key Configuration Properties
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fleet_management
    username: fleet_user
    password: fleet_password
  
  redis:
    host: localhost
    port: 6379
  
  kafka:
    bootstrap-servers: localhost:9092

traccar:
  api-url: http://localhost:8082/api
  username: admin
  password: admin
```

## Database Schema

The service uses PostgreSQL with Flyway migrations. Key tables include:
- `devices`: Main device information
- `device_types`: Device type definitions
- `device_brands`: Device brand information
- `sensor_types`: Available sensor types
- `device_sensors`: Device-sensor relationships
- `sensor_subscriptions`: Active sensor subscriptions
- `device_vehicle_assignments`: Device-vehicle relationships
- `device_user_assignments`: Device-user relationships
- `device_commands`: Command execution tracking
- `device_health`: Health monitoring data
- `mobile_devices`: Mobile device information

## Running the Service

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+
- Apache Kafka 2.8+

### Local Development
1. Clone the repository
2. Configure database and Redis connections
3. Start required services (PostgreSQL, Redis, Kafka, Eureka)
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Docker
```bash
docker-compose up -d
```

### Production
```bash
mvn clean package
java -jar target/device-service-1.0.0.jar
```

## Monitoring

### Health Checks
- Health endpoint: `GET /actuator/health`
- Metrics endpoint: `GET /actuator/metrics`
- Prometheus metrics: `GET /actuator/prometheus`

### Key Metrics
- Device registration rate
- Command execution success rate
- Health check response times
- Cache hit rates
- External service call latencies

## Events

### Published Events
- `DeviceRegisteredEvent`: When a new device is registered
- `DeviceAssignedEvent`: When a device is assigned to vehicle/user
- `SensorSubscriptionChangedEvent`: When sensor subscription changes
- `DeviceHealthUpdateEvent`: When device health status changes
- `MobileDeviceRegisteredEvent`: When mobile device is registered
- `DeviceCommandSentEvent`: When command is sent to device
- `DeviceStatusChangedEvent`: When device status changes

### Consumed Events
- Company events for access validation
- Vehicle events for assignment updates
- User events for permission changes
- Payment events for billing updates

## Security

### Authentication
- JWT-based authentication
- Role-based access control
- Device-specific permissions

### Authorization
- Company-level access control
- Device ownership validation
- Command execution permissions

## Error Handling

The service includes comprehensive error handling with custom exceptions:
- `DeviceNotFoundException`: Device not found
- `DeviceAlreadyExistsException`: Duplicate device registration
- `InvalidDeviceConfigurationException`: Invalid device configuration
- `TraccarIntegrationException`: Traccar API errors
- `SensorSubscriptionException`: Sensor subscription errors
- `DeviceAssignmentException`: Device assignment errors
- `UnauthorizedDeviceAccessException`: Access denied

## Testing

### Unit Tests
- Service layer tests
- Repository tests
- Controller tests
- Utility class tests

### Integration Tests
- Database integration tests
- External service integration tests
- Event publishing/consuming tests

### Test Containers
- PostgreSQL test container
- Redis test container
- Kafka test container

## Contributing

1. Follow the existing code structure
2. Add comprehensive tests
3. Update documentation
4. Follow naming conventions
5. Add proper logging and error handling

## License

This project is part of the Fleet Management System and follows the same licensing terms.

