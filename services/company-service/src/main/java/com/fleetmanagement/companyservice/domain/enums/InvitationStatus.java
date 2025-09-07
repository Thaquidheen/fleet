package com.fleetmanagement.companyservice.domain.enums;

public enum InvitationStatus {
    PENDING("Pending", "Invitation sent, awaiting response"),
    ACCEPTED("Accepted", "Invitation accepted"),
    DECLINED("Declined", "Invitation declined"),
    EXPIRED("Expired", "Invitation expired");

    private final String displayName;
    private final String description;

    InvitationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}