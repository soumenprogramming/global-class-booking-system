package com.soumen.global_class_booking_system.exception;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
		return build(exception.getStatus(), exception.getMessage(), List.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		List<String> details = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList();
		return build(HttpStatus.BAD_REQUEST, "Validation failed", details);
	}

	@ExceptionHandler(DateTimeException.class)
	ResponseEntity<ErrorResponse> handleDateTime(DateTimeException exception) {
		return build(HttpStatus.BAD_REQUEST, exception.getMessage(), List.of());
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException exception) {
		return build(HttpStatus.CONFLICT, "Request conflicts with existing data", List.of());
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", List.of());
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, List<String> details) {
		return ResponseEntity.status(status).body(new ErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				details
		));
	}
}
