


/**
 * Device Status Changed Event
 */
@Data
@Builder
public class DeviceStatusChangedEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private String previousStatus;
    private String newStatus;
    private String changedBy;
}