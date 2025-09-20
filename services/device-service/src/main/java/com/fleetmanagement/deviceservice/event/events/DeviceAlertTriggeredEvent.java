

/**
 * Device Alert Triggered Event
 */
@Data
@Builder
public class DeviceAlertTriggeredEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private String alertType;
    private String alertLevel; // INFO, WARNING, CRITICAL
    private String message;
    private String details;
}