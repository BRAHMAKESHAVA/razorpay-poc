package org.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.backend.exception.BadRequestException;

public enum AddressType {
    HOME,
    WORK,
    OTHER;

    // Ignoring case sensitivity.
    @JsonCreator
    public static AddressType fromValue(String value) {
        try{
            return AddressType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException  ex) {
            throw new BadRequestException("Invalid address type. Allowed values are: HOME, WORK, OTHER");
        }
    }
}


