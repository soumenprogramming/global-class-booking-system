package com.soumen.global_class_booking_system.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class TimeZoneConverter {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	public ZoneId zone(String timeZone) {
		return ZoneId.of(timeZone);
	}

	public Instant toInstant(LocalDateTime localDateTime, String timeZone) {
		return localDateTime.atZone(zone(timeZone)).toInstant();
	}

	public String format(Instant instant, String timeZone) {
		return FORMATTER.format(instant.atZone(zone(timeZone)));
	}
}
