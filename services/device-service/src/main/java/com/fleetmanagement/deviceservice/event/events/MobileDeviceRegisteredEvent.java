


/**
 * Mobile Device Registered Event
 */
@Data
@Builder
public class MobileDeviceRegisteredEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID driverId;
}