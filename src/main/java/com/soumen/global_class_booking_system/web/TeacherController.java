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

import com.soumen.global_class_booking_system.domain.Offering;
import com.soumen.global_class_booking_system.service.OfferingService;
import com.soumen.global_class_booking_system.service.ResponseMapper;
import com.soumen.global_class_booking_system.service.TimeZoneConverter;
import com.soumen.global_class_booking_system.web.dto.AddSessionRequest;
import com.soumen.global_class_booking_system.web.dto.CreateOfferingRequest;
import com.soumen.global_class_booking_system.web.dto.OfferingResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

	private final OfferingService offeringService;
	private final ResponseMapper responseMapper;
	private final TimeZoneConverter timeZoneConverter;

	public TeacherController(OfferingService offeringService, ResponseMapper responseMapper,
			TimeZoneConverter timeZoneConverter) {
		this.offeringService = offeringService;
		this.responseMapper = responseMapper;
		this.timeZoneConverter = timeZoneConverter;
	}

	@PostMapping("/offerings")
	@ResponseStatus(HttpStatus.CREATED)
	public OfferingResponse createOffering(@Valid @RequestBody CreateOfferingRequest request) {
		return responseMapper.toOfferingResponse(
				offeringService.createOffering(request),
				request.teacherTimeZone()
		);
	}

	@PostMapping("/offerings/{offeringId}/sessions")
	@ResponseStatus(HttpStatus.CREATED)
	public OfferingResponse addSession(@PathVariable Long offeringId, @Valid @RequestBody AddSessionRequest request) {
		Offering offering = offeringService.addSession(offeringId, request);
		String viewTimeZone = request.timeZone() == null || request.timeZone().isBlank()
				? offering.getTeacherTimeZone()
				: request.timeZone();
		return responseMapper.toOfferingResponse(offering, viewTimeZone);
	}

	@GetMapping("/{teacherId}/offerings")
	public List<OfferingResponse> getTeacherOfferings(@PathVariable Long teacherId,
			@RequestParam(defaultValue = "UTC") String timeZone) {
		timeZoneConverter.zone(timeZone);
		return offeringService.getTeacherOfferings(teacherId).stream()
				.map(offering -> responseMapper.toOfferingResponse(offering, timeZone))
				.toList();
	}
}
