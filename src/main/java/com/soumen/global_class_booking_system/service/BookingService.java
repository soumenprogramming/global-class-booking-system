package com.soumen.global_class_booking_system.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soumen.global_class_booking_system.domain.Booking;
import com.soumen.global_class_booking_system.domain.Offering;
import com.soumen.global_class_booking_system.domain.ParentAccount;
import com.soumen.global_class_booking_system.domain.Session;
import com.soumen.global_class_booking_system.exception.ApiException;
import com.soumen.global_class_booking_system.repository.BookingRepository;
import com.soumen.global_class_booking_system.repository.OfferingRepository;
import com.soumen.global_class_booking_system.repository.ParentAccountRepository;
import com.soumen.global_class_booking_system.web.dto.BookOfferingRequest;

@Service
public class BookingService {

	private final ParentAccountRepository parentAccountRepository;
	private final OfferingRepository offeringRepository;
	private final BookingRepository bookingRepository;

	public BookingService(ParentAccountRepository parentAccountRepository, OfferingRepository offeringRepository,
			BookingRepository bookingRepository) {
		this.parentAccountRepository = parentAccountRepository;
		this.offeringRepository = offeringRepository;
		this.bookingRepository = bookingRepository;
	}

	@Transactional
	public Booking bookOffering(BookOfferingRequest request) {
		ParentAccount parent = parentAccountRepository.findByIdForUpdate(request.parentId())
				.orElseGet(() -> {
					parentAccountRepository.saveAndFlush(new ParentAccount(request.parentId()));
					return parentAccountRepository.findByIdForUpdate(request.parentId())
							.orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Parent lock failed"));
				});

		Offering offering = offeringRepository.findWithSessionsById(request.offeringId())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Offering not found"));
		if (offering.getSessions().isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot book an offering without sessions");
		}
		if (bookingRepository.existsByParentIdAndOfferingId(parent.getId(), offering.getId())) {
			throw new ApiException(HttpStatus.CONFLICT, "Parent has already booked this offering");
		}

		for (Session session : offering.getSessions()) {
			boolean overlaps = bookingRepository.existsOverlappingBooking(
					parent.getId(),
					session.getStartAt(),
					session.getEndAt()
			);
			if (overlaps) {
				throw new ApiException(HttpStatus.CONFLICT, "Offering conflicts with an existing parent booking");
			}
		}

		return bookingRepository.save(new Booking(parent, offering));
	}

	@Transactional(readOnly = true)
	public List<Booking> getBookings(Long parentId) {
		return bookingRepository.findByParentIdOrderByBookedAtDesc(parentId);
	}
}
