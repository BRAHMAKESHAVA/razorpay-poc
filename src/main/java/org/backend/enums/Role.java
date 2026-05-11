package org.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing user roles in the system.
 */
public enum Role {
    CUSTOMER,
    ADMIN,
    CAPTAIN,
    PARTNER;

    // Accept captain / Captain / CAPTAIN
    @JsonCreator
    public static Role fromValue(String value) {
        return Role.valueOf(value.toUpperCase());
    }
}
