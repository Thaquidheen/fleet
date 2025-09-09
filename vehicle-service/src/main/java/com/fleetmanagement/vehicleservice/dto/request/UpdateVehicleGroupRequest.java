
/**
 * Update Vehicle Group Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateVehicleGroupRequest {

    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private UUID parentGroupId;

    @Min(value = 1, message = "Max vehicles must be at least 1")
    private Integer maxVehicles;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    private UUID managerId;

    private Boolean isActive;

    private Map<String, Object> customFields;

    private Integer sortOrder;
}