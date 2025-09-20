


/**
 * Device Connection Status Event
 */
@Data
@Builder
public class DeviceConnectionStatusEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private String previousStatus;
    private String newStatus;
}