

/**
 * Device Registered Event
 */
@Data
@Builder
public class DeviceRegisteredEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private String deviceName;
    private DeviceType deviceType;
    private DeviceBrand deviceBrand;
    private UUID companyId;
    private Long traccarId;
    private DeviceStatus status;
}