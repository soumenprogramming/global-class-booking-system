package com.soumen.global_class_booking_system.service;

import java.util.Comparator;

import org.springframework.stereotype.Component;

import com.soumen.global_class_booking_system.domain.Booking;
import com.soumen.global_class_booking_system.domain.Offering;
import com.soumen.global_class_booking_system.domain.Session;
import com.soumen.global_class_booking_system.web.dto.BookingResponse;
import com.soumen.global_class_booking_system.web.dto.OfferingResponse;
import com.soumen.global_class_booking_system.web.dto.SessionResponse;

@Component
public class ResponseMapper {

	private final TimeZoneConverter timeZoneConverter;

	public ResponseMapper(TimeZoneConverter timeZoneConverter) {
		this.timeZoneConverter = timeZoneConverter;
	}

	public OfferingResponse toOfferingResponse(Offering offering, String viewTimeZone) {
		return new OfferingResponse(
				offering.getId(),
				offering.getCourse().getId(),
				offering.getTeacher().getId(),
				offering.getCourse().getName(),
				offering.getTitle(),
				offering.getTeacherTimeZone(),
				offering.getSessions().stream()
						.sorted(Comparator.comparing(Session::getStartAt))
						.map(session -> toSessionResponse(session, viewTimeZone))
						.toList()
		);
	}

	public BookingResponse toBookingResponse(Booking booking, String viewTimeZone) {
		return new BookingResponse(
				booking.getId(),
				booking.getParent().getId(),
				booking.getBookedAt(),
				toOfferingResponse(booking.getOffering(), viewTimeZone)
		);
	}

	private SessionResponse toSessionResponse(Session session, String viewTimeZone) {
		return new SessionResponse(
				session.getId(),
				session.getOffering().getId(),
				session.getTeacherId(),
				timeZoneConverter.format(session.getStartAt(), viewTimeZone),
				timeZoneConverter.format(session.getEndAt(), viewTimeZone),
				viewTimeZone
		);
	}
}
