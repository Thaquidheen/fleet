



/**
 * Mobile Device Updated Event
 */
@Data
@Builder
public class MobileDeviceUpdatedEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID driverId;
}