package com.soumen.global_class_booking_system.web.dto;

import jakarta.validation.constraints.NotNull;

public record BookOfferingRequest(
		@NotNull Long parentId,
		@NotNull Long offeringId
) {
}
