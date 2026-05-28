package com.soumen.global_class_booking_system.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String message,
		List<String> details
) {
}
