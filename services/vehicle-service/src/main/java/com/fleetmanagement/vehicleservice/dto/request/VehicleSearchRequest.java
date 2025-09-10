/**
 * Vehicle Search Request DTO
 */

import java.util.UUID;
import java.time.LocalDate;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.*;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleStatus;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleType;
import com.fleetmanagement.vehicleservice.domain.enums.VehicleCategory;
import com.fleetmanagement.vehicleservice.domain.enums.FuelType;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSearchRequest {

    private String searchTerm;

    private VehicleStatus status;

    private VehicleType vehicleType;

    private VehicleCategory vehicleCategory;

    private FuelType fuelType;

    private Boolean assignedOnly;

    private UUID groupId;

    private UUID driverId;

    @Min(value = 1900, message = "Year from must be 1900 or later")
    private Integer yearFrom;

    @Max(value = 2030, message = "Year to cannot be more than 2030")
    private Integer yearTo;

    private LocalDate purchaseDateFrom;

    private LocalDate purchaseDateTo;

    @Min(value = 0, message = "Mileage from must be non-negative")
    private Integer mileageFrom;

    private Integer mileageTo;

    private Boolean maintenanceDue;

    private Boolean insuranceExpiring;

    private Boolean registrationExpiring;

    @Min(value = 1, message = "Days threshold must be at least 1")
    private Integer daysThreshold;

    // Location-based search
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    @Min(value = 1, message = "Radius must be at least 1 meter")
    private Double radiusMeters;

    // Sorting
    private String sortBy;

    private String sortDirection; // ASC or DESC
}
