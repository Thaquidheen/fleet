


/**
 * Device Maintenance Event
 */
@Data
@Builder
public class DeviceMaintenanceEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private String maintenanceType; // SCHEDULED, EMERGENCY, REPAIR
    private String description;
    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED
}
