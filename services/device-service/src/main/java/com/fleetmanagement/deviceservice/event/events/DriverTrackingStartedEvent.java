


/**
 * Driver Tracking Started Event
 */
@Data
@Builder
public class DriverTrackingStartedEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private UUID driverId;
    private String deviceId;
    private String shiftId;
}