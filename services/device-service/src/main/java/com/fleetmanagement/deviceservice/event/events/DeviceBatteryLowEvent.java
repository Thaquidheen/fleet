


/**
 * Device Battery Low Event
 */
@Data
@Builder
public class DeviceBatteryLowEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private Integer batteryLevel;
    private String alertLevel; // WARNING, CRITICAL
}