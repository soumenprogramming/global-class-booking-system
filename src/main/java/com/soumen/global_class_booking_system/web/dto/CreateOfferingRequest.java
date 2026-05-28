package com.soumen.global_class_booking_system.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOfferingRequest(
		@NotNull Long teacherId,
		@NotBlank String courseName,
		@NotBlank String title,
		@NotBlank String teacherTimeZone
) {
}
