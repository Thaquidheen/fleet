

/**
 * Device Firmware Update Event
 */
@Data
@Builder
public class DeviceFirmwareUpdateEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private String currentVersion;
    private String targetVersion;
    private String updateStatus; // PENDING, IN_PROGRESS, COMPLETED, FAILED
}
