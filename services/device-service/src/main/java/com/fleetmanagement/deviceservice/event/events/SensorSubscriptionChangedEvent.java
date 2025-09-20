


/**
 * Sensor Subscription Changed Event
 */
@Data
@Builder
public class SensorSubscriptionChangedEvent {
    private String eventId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private String deviceId;
    private UUID companyId;
    private String sensorType;
    private String action; // SUBSCRIBED, UNSUBSCRIBED, UPDATED
    private Double monthlyPrice;
    private UUID subscriptionId;
}