package com.somshare.somshare.exception;

import com.somshare.somshare.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException e) {
        log.warn("[AUTH_INVALID_CREDENTIALS] {}", e.getMessage(), e);
        ErrorResponse error = ErrorResponse.of(e.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        log.warn("[AUTH_USER_NOT_FOUND] {}", e.getMessage(), e);
        ErrorResponse error = ErrorResponse.of(e.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException e) {
        log.warn("[SIGNUP_DUPLICATE_EMAIL] {}", e.getMessage(), e);
        ErrorResponse error = ErrorResponse.of(e.getMessage(), HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerified(EmailNotVerifiedException e) {
        log.warn("[AUTH_EMAIL_NOT_VERIFIED] {}", e.getMessage(), e);
        ErrorResponse error = ErrorResponse.of(e.getMessage(), HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationCode(InvalidVerificationCodeException e) {
        log.warn("[VERIFY_INVALID_CODE] {}", e.getMessage(), e);
        ErrorResponse error = ErrorResponse.of(e.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(EmailNotVerifiedBeforeSignupException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerifiedBeforeSignup(EmailNotVerifiedBeforeSignupException e) {
        log.warn("[SIGNUP_EMAIL_NOT_VERIFIED] {}", e.getMessage(), e);
        ErrorResponse error = ErrorResponse.of(e.getMessage(), HttpStatus.FORBIDDEN.value());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e) {
        String fields = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + "=" + fe.getRejectedValue())
                .collect(Collectors.joining(", "));
        log.warn("[VALIDATION_ERROR] fields={} message={}", fields, e.getMessage(), e);

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        if (errorMessage.isEmpty()) {
            errorMessage = "입력 값이 올바르지 않습니다.";
        }

        ErrorResponse error = ErrorResponse.of(errorMessage, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFileType(InvalidFileTypeException e) {
        log.warn("[UPLOAD_INVALID_FILE_TYPE] {}", e.getMessage(), e);
        ErrorResponse error = ErrorResponse.of(e.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileSizeExceeded(FileSizeExceededException e) {
        log.warn("[UPLOAD_FILE_TOO_LARGE] {}", e.getMessage(), e);
        ErrorResponse error = ErrorResponse.of(e.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE.value());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse> handleFileUpload(FileUploadException e) {
        log.error("[UPLOAD_FAILED] {}", e.getMessage(), e);
        ErrorResponse error = ErrorResponse.of(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("[BAD_REQUEST] {}", e.getMessage(), e);
        ErrorResponse error = ErrorResponse.of(e.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("[UNHANDLED_EXCEPTION]", e);
        ErrorResponse error = ErrorResponse.of(
                "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
