/**
 * Device Communication Error Event
 */
@Data
@Builder
public class DeviceCommunicationErrorEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private String errorType;
    private String errorMessage;
    private Integer errorCount;
}
