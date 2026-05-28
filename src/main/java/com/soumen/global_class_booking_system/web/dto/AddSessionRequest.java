package com.soumen.global_class_booking_system.web.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record AddSessionRequest(
		@NotNull Long teacherId,
		@NotNull LocalDateTime startLocalDateTime,
		@NotNull LocalDateTime endLocalDateTime,
		String timeZone
) {
}
