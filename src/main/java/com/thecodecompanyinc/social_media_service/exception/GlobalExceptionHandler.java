package com.thecodecompanyinc.social_media_service.exception;

import static com.thecodecompanyinc.social_media_service.response.ApiResponse.createFailureResponse;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.thecodecompanyinc.social_media_service.response.ApiResponse;
import com.thecodecompanyinc.social_media_service.response.MessageResponse;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        String errorMessage = "Validation failed: " + errors;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleConstraintViolations(
            ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations()
                .forEach(
                        cv -> {
                            String path = cv.getPropertyPath() != null
                                    ? cv.getPropertyPath().toString()
                                    : "";
                            errors.put(path, cv.getMessage());
                        });
        String errorMessage = "Constraint violations: " + errors;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleMissingRequestHeader(
            MissingRequestHeaderException ex) {
        String errorMessage = "Required request header '" + ex.getHeaderName() + "' is missing";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        String errorMessage = "Invalid request body";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        String requiredTypeName = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown type";
        String errorMessage = "Method argument type mismatch: "
                + ex.getName()
                + " should be of type "
                + requiredTypeName;
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<ApiResponse<MessageResponse>> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        String errorMessage = "HTTP request method '" + ex.getMethod() + "' is not supported for this endpoint";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(createFailureResponse(errorMessage));
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleMultipartException(
            MultipartException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createFailureResponse("Multipart request error: " + ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse<MessageResponse>> handleAccessDeniedException(
            AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createFailureResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<MessageResponse>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createFailureResponse(ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<MessageResponse>> handleNoSuchElementException(
            NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createFailureResponse(ex.getMessage()));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<MessageResponse>> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createFailureResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<MessageResponse>> handleIllegalStateException(
            IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createFailureResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<MessageResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createFailureResponse(ex.getMessage()));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ApiResponse<MessageResponse>> handleServiceUnavailableException(
            ServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(createFailureResponse(ex.getMessage()));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<MessageResponse>> handleExpiredJwtException(
            ExpiredJwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createFailureResponse("Token expired: " + ex.getMessage()));
    }

    @ExceptionHandler(GeneralSecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<MessageResponse>> handleGeneralSecurityException(
            GeneralSecurityException ex) {
        log.error("Security verification failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createFailureResponse("Security verification failed"));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseEntity<ApiResponse<MessageResponse>> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(createFailureResponse("Unsupported media type: " + ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<MessageResponse>> handleNoResourceFoundException(
            NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createFailureResponse("Resource not found: " + ex.getResourcePath()));
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<MessageResponse>> handleAuthenticationException(
            AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createFailureResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<MessageResponse>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createFailureResponse("Internal server error"));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<MessageResponse>> handleDuplicateKeyException(
            DuplicateKeyException ex) {
        log.error("Duplicate key error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createFailureResponse(ex.getMessage()));
    }
}
