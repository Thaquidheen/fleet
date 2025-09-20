


/**
 * Device Health Update Event
 */
@Data
@Builder
public class DeviceHealthUpdateEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private String healthLevel;
    private Integer healthScore;
}