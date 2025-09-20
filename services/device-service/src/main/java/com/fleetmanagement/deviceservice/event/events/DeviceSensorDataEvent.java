


/**
 * Device Sensor Data Event
 */
@Data
@Builder
public class DeviceSensorDataEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private String sensorType;
    private String sensorValue;
    private String unit;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime readingTime;
}