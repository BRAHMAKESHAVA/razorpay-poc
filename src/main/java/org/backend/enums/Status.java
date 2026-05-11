package org.backend.enums;

public enum Status {
    SUCCESS("success"),
    ERROR("error");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}