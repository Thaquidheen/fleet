

/**
 * Device Location Updated Event
 */
@Data
@Builder
public class DeviceLocationUpdatedEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double heading;
    private String address;
    private Double accuracy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deviceTime;
}