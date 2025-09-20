

/**
 * Device Geofence Violation Event
 */
@Data
@Builder
public class DeviceGeofenceViolationEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private UUID geofenceId;
    private String violationType; // ENTER, EXIT
    private Double latitude;
    private Double longitude;
    private String address;
}