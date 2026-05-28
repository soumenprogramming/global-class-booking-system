package com.soumen.global_class_booking_system.web.dto;

public record SessionResponse(
		Long id,
		Long offeringId,
		Long teacherId,
		String startTime,
		String endTime,
		String timeZone
) {
}
