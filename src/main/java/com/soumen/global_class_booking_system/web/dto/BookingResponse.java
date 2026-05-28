package com.soumen.global_class_booking_system.web.dto;

import java.time.Instant;

public record BookingResponse(
		Long id,
		Long parentId,
		Instant bookedAt,
		OfferingResponse offering
) {
}
