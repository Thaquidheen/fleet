


/**
 * Device Assigned Event
 */
@Data
@Builder
public class DeviceAssignedEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID vehicleId;
    private UUID companyId;
    private UUID assignedBy;
}