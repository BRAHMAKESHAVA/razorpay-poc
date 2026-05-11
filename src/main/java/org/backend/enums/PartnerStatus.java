package org.backend.enums;

public enum PartnerStatus {
    CREATED("created"),
    ACTIVE("active"),
    PENDING("pending"),
    IN_ACTIVE("in_active");

    private final String displayName;

    PartnerStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}