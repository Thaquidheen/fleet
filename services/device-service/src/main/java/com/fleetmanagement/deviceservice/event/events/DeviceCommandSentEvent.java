



/**
 * Device Command Sent Event
 */
@Data
@Builder
public class DeviceCommandSentEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID commandId;
    private String commandType;
    private UUID initiatedBy;
}
