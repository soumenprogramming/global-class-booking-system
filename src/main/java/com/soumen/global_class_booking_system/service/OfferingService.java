package com.soumen.global_class_booking_system.service;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soumen.global_class_booking_system.domain.Course;
import com.soumen.global_class_booking_system.domain.Offering;
import com.soumen.global_class_booking_system.domain.Session;
import com.soumen.global_class_booking_system.domain.Teacher;
import com.soumen.global_class_booking_system.exception.ApiException;
import com.soumen.global_class_booking_system.repository.BookingRepository;
import com.soumen.global_class_booking_system.repository.CourseRepository;
import com.soumen.global_class_booking_system.repository.OfferingRepository;
import com.soumen.global_class_booking_system.repository.TeacherRepository;
import com.soumen.global_class_booking_system.web.dto.AddSessionRequest;
import com.soumen.global_class_booking_system.web.dto.CreateOfferingRequest;

@Service
public class OfferingService {

	private final TeacherRepository teacherRepository;
	private final CourseRepository courseRepository;
	private final OfferingRepository offeringRepository;
	private final BookingRepository bookingRepository;
	private final TimeZoneConverter timeZoneConverter;

	public OfferingService(TeacherRepository teacherRepository, CourseRepository courseRepository,
			OfferingRepository offeringRepository, BookingRepository bookingRepository, TimeZoneConverter timeZoneConverter) {
		this.teacherRepository = teacherRepository;
		this.courseRepository = courseRepository;
		this.offeringRepository = offeringRepository;
		this.bookingRepository = bookingRepository;
		this.timeZoneConverter = timeZoneConverter;
	}

	@Transactional
	public Offering createOffering(CreateOfferingRequest request) {
		timeZoneConverter.zone(request.teacherTimeZone());
		Teacher teacher = teacherRepository.findById(request.teacherId())
				.orElseGet(() -> teacherRepository.save(new Teacher(request.teacherId())));
		Course course = courseRepository.findByNormalizedName(Course.normalize(request.courseName()))
				.orElseGet(() -> courseRepository.save(new Course(request.courseName().trim())));
		return offeringRepository.save(new Offering(
				teacher,
				course,
				request.title().trim(),
				request.teacherTimeZone().trim()
		));
	}

	@Transactional
	public Offering addSession(Long offeringId, AddSessionRequest request) {
		Offering offering = offeringRepository.findWithSessionsById(offeringId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Offering not found"));
		if (!offering.getTeacher().getId().equals(request.teacherId())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Teacher does not own this offering");
		}
		if (bookingRepository.existsByOfferingId(offeringId)) {
			throw new ApiException(HttpStatus.CONFLICT, "Cannot add sessions after an offering has bookings");
		}

		String sessionTimeZone = request.timeZone() == null || request.timeZone().isBlank()
				? offering.getTeacherTimeZone()
				: request.timeZone().trim();
		Instant startAt = timeZoneConverter.toInstant(request.startLocalDateTime(), sessionTimeZone);
		Instant endAt = timeZoneConverter.toInstant(request.endLocalDateTime(), sessionTimeZone);
		if (!startAt.isBefore(endAt)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Session start time must be before end time");
		}
		boolean overlapsExistingSession = offering.getSessions().stream()
				.anyMatch(session -> session.getStartAt().isBefore(endAt) && session.getEndAt().isAfter(startAt));
		if (overlapsExistingSession) {
			throw new ApiException(HttpStatus.CONFLICT, "Session overlaps another session in the same offering");
		}

		offering.addSession(new Session(request.teacherId(), startAt, endAt));
		return offeringRepository.save(offering);
	}

	@Transactional(readOnly = true)
	public List<Offering> getTeacherOfferings(Long teacherId) {
		return offeringRepository.findUpcomingByTeacher(teacherId, Instant.now());
	}

	@Transactional(readOnly = true)
	public List<Offering> getAvailableOfferings() {
		return offeringRepository.findAvailable(Instant.now());
	}
}
