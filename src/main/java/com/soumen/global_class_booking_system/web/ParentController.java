package com.soumen.global_class_booking_system.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.soumen.global_class_booking_system.service.BookingService;
import com.soumen.global_class_booking_system.service.OfferingService;
import com.soumen.global_class_booking_system.service.ResponseMapper;
import com.soumen.global_class_booking_system.service.TimeZoneConverter;
import com.soumen.global_class_booking_system.web.dto.BookOfferingRequest;
import com.soumen.global_class_booking_system.web.dto.BookingResponse;
import com.soumen.global_class_booking_system.web.dto.OfferingResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/parent")
public class ParentController {

	private final OfferingService offeringService;
	private final BookingService bookingService;
	private final ResponseMapper responseMapper;
	private final TimeZoneConverter timeZoneConverter;

	public ParentController(OfferingService offeringService, BookingService bookingService, ResponseMapper responseMapper,
			TimeZoneConverter timeZoneConverter) {
		this.offeringService = offeringService;
		this.bookingService = bookingService;
		this.responseMapper = responseMapper;
		this.timeZoneConverter = timeZoneConverter;
	}

	@GetMapping("/offerings")
	public List<OfferingResponse> getAvailableOfferings(@RequestParam(defaultValue = "UTC") String timeZone) {
		timeZoneConverter.zone(timeZone);
		return offeringService.getAvailableOfferings().stream()
				.map(offering -> responseMapper.toOfferingResponse(offering, timeZone))
				.toList();
	}

	@PostMapping("/bookings")
	@ResponseStatus(HttpStatus.CREATED)
	public BookingResponse bookOffering(@Valid @RequestBody BookOfferingRequest request,
			@RequestParam(defaultValue = "UTC") String timeZone) {
		timeZoneConverter.zone(timeZone);
		return responseMapper.toBookingResponse(bookingService.bookOffering(request), timeZone);
	}

	@GetMapping("/{parentId}/bookings")
	public List<BookingResponse> getBookings(@PathVariable Long parentId,
			@RequestParam(defaultValue = "UTC") String timeZone) {
		timeZoneConverter.zone(timeZone);
		return bookingService.getBookings(parentId).stream()
				.map(booking -> responseMapper.toBookingResponse(booking, timeZone))
				.toList();
	}
}
