package com.soumen.global_class_booking_system.web.dto;

import java.util.List;

public record OfferingResponse(
		Long id,
		Long courseId,
		Long teacherId,
		String courseName,
		String title,
		String teacherTimeZone,
		List<SessionResponse> sessions
) {
}
