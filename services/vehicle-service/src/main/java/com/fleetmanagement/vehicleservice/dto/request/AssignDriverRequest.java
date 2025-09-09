/**
 * Assign Driver Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignDriverRequest {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Assignment type is required")
    private AssignmentType assignmentType;

    private LocalTime shiftStartTime;

    private LocalTime shiftEndTime;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    private Map<String, Object> restrictions;
}