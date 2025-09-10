package com.fleetmanagement.companyservice.dto.request;

import com.fleetmanagement.companyservice.domain.enums.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * FIXED: Added missing subscriptionPlan field that was causing compilation errors
 * in CompanyService.java where request.getSubscriptionPlan() was being called
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCompanyRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name cannot exceed 100 characters")
    private String name;

    @Pattern(regexp = "^[a-z0-9-]+$", message = "Subdomain must contain only lowercase letters, numbers, and hyphens")
    @Size(min = 3, max = 50, message = "Subdomain must be between 3 and 50 characters")
    private String subdomain;

    @Size(max = 100, message = "Industry cannot exceed 100 characters")
    private String industry;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 200, message = "Website cannot exceed 200 characters")
    private String website;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    @Size(max = 200, message = "Logo URL cannot exceed 200 characters")
    private String logoUrl;

    @Size(max = 50, message = "Timezone cannot exceed 50 characters")
    private String timezone;

    @Size(max = 10, message = "Language cannot exceed 10 characters")
    private String language;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    @Size(max = 100, message = "Contact person name cannot exceed 100 characters")
    private String contactPersonName;

    @Size(max = 100, message = "Contact person title cannot exceed 100 characters")
    private String contactPersonTitle;

    @Email(message = "Invalid contact person email format")
    private String contactPersonEmail;

    @Size(max = 20, message = "Contact person phone cannot exceed 20 characters")
    private String contactPersonPhone;

    // CRITICAL FIX: This field was missing causing CompanyService compilation errors
    private SubscriptionPlan subscriptionPlan;
}