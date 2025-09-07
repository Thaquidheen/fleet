package com.fleetmanagement.companyservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompanyRequest {

    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String name;

    @Size(max = 100, message = "Industry must not exceed 100 characters")
    private String industry;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    @Pattern(regexp = "^https?://.*", message = "Website must be a valid URL starting with http:// or https://")
    private String website;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 255, message = "Logo URL must not exceed 255 characters")
    private String logoUrl;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    @Size(min = 2, max = 5, message = "Language code must be between 2 and 5 characters")
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Language must be in format 'en' or 'en-US'")
    private String language;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // Contact person information (can be updated)
    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    private String contactPersonName;

    @Size(max = 100, message = "Contact person title must not exceed 100 characters")
    private String contactPersonTitle;

    @Email(message = "Invalid contact email format")
    @Size(max = 255, message = "Contact email must not exceed 255 characters")
    private String contactPersonEmail;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid contact phone number format")
    private String contactPersonPhone;
}