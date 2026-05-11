package org.backend.exception;

import io.jsonwebtoken.JwtException;
import org.backend.dto.common.ErrorResponseDTO;
import org.backend.dto.common.FieldValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ErrorResponseDTO build(HttpStatus status, String type, String message) {
        return ErrorResponseDTO.builder()
                .status_code(status.value())
                .status(type)
                .message(message)
                .build();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not allowed | Method: {} | Error: {}", ex.getMethod(), ex.getMessage());        String message = "Method " + ex.getMethod() + " is not supported for this endpoint.";
        ErrorResponseDTO error = build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", message);
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        log.warn("Unsupported media type | Error: {}", ex.getMessage());
        ErrorResponseDTO error = build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "Unsupported Media Type");
        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidJson(MethodArgumentTypeMismatchException ex) {
        log.warn("Invalid parameter type | Param: {} | Value: {} | Error: {}",
                ex.getName(), ex.getValue(), ex.getMessage());
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected a number.", ex.getValue(), ex.getName());
        return new ResponseEntity<>(
                build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message), HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
//        List<FieldValidationError> fieldErrors = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(err -> new FieldValidationError(err.getField(), err.getDefaultMessage()))
//                .toList();

        log.warn("Validation failed | Errors: {}", ex.getBindingResult().getFieldErrors());
        Map<String, String> errorMap = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> {
            errorMap.putIfAbsent(err.getField(), err.getDefaultMessage());
        });
        List<FieldValidationError> fieldErrors = errorMap.entrySet()
                .stream()
                .map(e -> new FieldValidationError(e.getKey(), e.getValue()))
                .toList();
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .status_code(HttpStatus.BAD_REQUEST.value())
                .status("VALIDATION_ERROR")
                .message("Input validation failed")
                .fieldErrors(fieldErrors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicate(DuplicateResourceException ex) {
        log.warn("Duplicate resource error | Message: {}", ex.getMessage());
        return new ResponseEntity<>(
                build(HttpStatus.BAD_REQUEST, "DUPLICATE_RESOURCE", ex.getMessage()), HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicate(ResourceNotFoundException ex) {
        log.warn("Resource not found | Message: {}", ex.getMessage());
        return new ResponseEntity<>(
                build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage()), HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(OtpException.class)
    public ResponseEntity<ErrorResponseDTO> handleOtp(OtpException ex) {
        log.warn("OTP error | Code: {} | Message: {}", ex.getCode(), ex.getMessage());
        return new ResponseEntity<>(
                build(ex.getStatusCode(), ex.getCode(), ex.getMessage()), ex.getStatusCode()
        );
    }

//    @ExceptionHandler(AuthenticationException.class)
//    public ResponseEntity<ErrorResponseDTO> handleAuthentication(AuthenticationException ex) {
//        log.warn("Authentication failed | Reason: {}", ex.getMessage());
//        return new ResponseEntity<>(
//                build(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Invalid credentials"), HttpStatus.UNAUTHORIZED
//        );
//    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponseDTO> handleJwt(JwtException ex) {
        log.warn("JWT error | Message: {}", ex.getMessage());
        return new ResponseEntity<>(
                build(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "JWT token is invalid or expired"), HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied | Message: {}", ex.getMessage());
        return new ResponseEntity<>(
                build(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You don’t have permission to access this resource"), HttpStatus.FORBIDDEN
        );
    }

//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex) {
//        log.warn("Bad credentials | Message: {}", ex.getMessage());
//        return new ResponseEntity<>(
//                build(HttpStatus.BAD_REQUEST, "BAD_CREDENTIALS", "Invalid mobile number or password"), HttpStatus.BAD_REQUEST
//        );
//    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadRequestException ex) {
        log.warn("Bad request | Message: {}", ex.getMessage());
        return new ResponseEntity<>(
                build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage()), HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponseDTO> noHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("No handler found | URL: {} | Method: {}", ex.getRequestURL(), ex.getHttpMethod());
        return new ResponseEntity<>(
                build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage()), HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidJson(HttpMessageNotReadableException ex) {
        log.warn("Invalid JSON request | Error: {}", ex.getMessage());
        log.debug("Root cause: {}", ex.getMostSpecificCause().getMessage());
        String message = "Invalid request payload";
        String status = "INVALID_JSON_DATA";
        String errorMsg = ex.getMessage();
        String rootMsg = ex.getMostSpecificCause().getMessage();
        if (errorMsg.contains("Unrecognized token") || rootMsg.contains("Unrecognized token")) {
            message = "Invalid JSON format. Check for missing quotes or incorrect values.";
            status = "MALFORMED_JSON";
        } else if (errorMsg.contains("Cannot construct instance") || rootMsg.contains("Cannot construct instance")) {
            // Check if it's really ENUM
            if (errorMsg.contains("enum") || rootMsg.contains("enum")) {
                message = "Invalid value provided. Please check enum values.";
                status = "INVALID_ENUM";
            }
            // Otherwise it's STRUCTURE issue
            else {
                message = "Invalid JSON structure. Please close the brackets properly.";
                status = "INVALID_JSON_STRUCTURE";
            }
        } else if (errorMsg.contains("Unexpected end-of-input") || rootMsg.contains("Unexpected end-of-input")) {
            message = "Invalid JSON structure. Please close the brackets properly.";
            status = "INVALID_JSON_STRUCTURE";
        } else if (errorMsg.contains("Unexpected character") || rootMsg.contains("Unexpected character")) {
            message = "Invalid JSON syntax. Check commas and structure.";
            status = "INVALID_JSON_SYNTAX";
        }
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .status_code(HttpStatus.BAD_REQUEST.value())
                .status(status)
                .message(message)
                .error(ex.getMessage())
                .build();

        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobal(Exception ex) {
        log.error("Unexpected error occurred | Exception: ", ex);
        return new ResponseEntity<>(
                build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Something went wrong. Please try again later."), HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
