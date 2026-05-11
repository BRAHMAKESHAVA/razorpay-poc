package org.backend.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class OtpException extends RuntimeException {
    private final String code;
    private final HttpStatus statusCode;

    public OtpException(String code, String message, HttpStatus statusCode) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }
}
