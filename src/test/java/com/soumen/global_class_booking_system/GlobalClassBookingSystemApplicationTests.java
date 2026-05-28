package com.soumen.global_class_booking_system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import com.soumen.global_class_booking_system.domain.Booking;
import com.soumen.global_class_booking_system.domain.Offering;
import com.soumen.global_class_booking_system.domain.ParentAccount;
import com.soumen.global_class_booking_system.exception.ApiException;
import com.soumen.global_class_booking_system.repository.BookingRepository;
import com.soumen.global_class_booking_system.repository.CourseRepository;
import com.soumen.global_class_booking_system.repository.OfferingRepository;
import com.soumen.global_class_booking_system.repository.ParentAccountRepository;
import com.soumen.global_class_booking_system.repository.TeacherRepository;
import com.soumen.global_class_booking_system.service.BookingService;
import com.soumen.global_class_booking_system.service.OfferingService;
import com.soumen.global_class_booking_system.service.ResponseMapper;
import com.soumen.global_class_booking_system.web.dto.AddSessionRequest;
import com.soumen.global_class_booking_system.web.dto.BookOfferingRequest;
import com.soumen.global_class_booking_system.web.dto.CreateOfferingRequest;

@SpringBootTest
class GlobalClassBookingSystemApplicationTests {

	private final OfferingService offeringService;
	private final BookingService bookingService;
	private final ResponseMapper responseMapper;
	private final BookingRepository bookingRepository;
	private final CourseRepository courseRepository;
	private final OfferingRepository offeringRepository;
	private final ParentAccountRepository parentAccountRepository;
	private final TeacherRepository teacherRepository;

	@Autowired
	GlobalClassBookingSystemApplicationTests(OfferingService offeringService, BookingService bookingService,
			ResponseMapper responseMapper, BookingRepository bookingRepository, CourseRepository courseRepository,
			OfferingRepository offeringRepository, ParentAccountRepository parentAccountRepository,
			TeacherRepository teacherRepository) {
		this.offeringService = offeringService;
		this.bookingService = bookingService;
		this.responseMapper = responseMapper;
		this.bookingRepository = bookingRepository;
		this.courseRepository = courseRepository;
		this.offeringRepository = offeringRepository;
		this.parentAccountRepository = parentAccountRepository;
		this.teacherRepository = teacherRepository;
	}

	@BeforeEach
	void cleanDatabase() {
		bookingRepository.deleteAll();
		offeringRepository.deleteAll();
		courseRepository.deleteAll();
		parentAccountRepository.deleteAll();
		teacherRepository.deleteAll();
	}

	@Test
	void rejectsOfferingWhenAnySessionOverlapsExistingParentBooking() {
		Offering python = createOffering(101L, "Python Coding", "Saturday Batch");
		offeringService.addSession(python.getId(), new AddSessionRequest(
				101L,
				LocalDateTime.of(2026, 6, 6, 18, 0),
				LocalDateTime.of(2026, 6, 6, 19, 0),
				null
		));
		bookingService.bookOffering(new BookOfferingRequest(501L, python.getId()));

		Offering roblox = createOffering(102L, "Roblox Game Design", "Weekend Batch");
		offeringService.addSession(roblox.getId(), new AddSessionRequest(
				102L,
				LocalDateTime.of(2026, 6, 6, 18, 30),
				LocalDateTime.of(2026, 6, 6, 19, 30),
				null
		));

		assertThatThrownBy(() -> bookingService.bookOffering(new BookOfferingRequest(501L, roblox.getId())))
				.isInstanceOf(ApiException.class)
				.extracting("status")
				.isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void rendersSessionTimesInParentsRequestedTimezone() {
		Offering offering = createOffering(101L, "Art Drawing Class", "India Evening Batch");
		Offering withSession = offeringService.addSession(offering.getId(), new AddSessionRequest(
				101L,
				LocalDateTime.of(2026, 6, 6, 18, 0),
				LocalDateTime.of(2026, 6, 6, 19, 0),
				"Asia/Kolkata"
		));

		var response = responseMapper.toOfferingResponse(withSession, "America/New_York");

		assertThat(response.courseId()).isNotNull();
		assertThat(response.courseName()).isEqualTo("Art Drawing Class");
		assertThat(response.sessions()).hasSize(1);
		assertThat(response.sessions().get(0).startTime()).isEqualTo("2026-06-06T08:30:00-04:00");
		assertThat(response.sessions().get(0).endTime()).isEqualTo("2026-06-06T09:30:00-04:00");
	}

	@Test
	void concurrentOverlappingBookingsForSameParentCannotBothSucceed() throws Exception {
		parentAccountRepository.save(new ParentAccount(777L));
		Offering first = createOffering(201L, "Public Speaking", "Batch A");
		offeringService.addSession(first.getId(), new AddSessionRequest(
				201L,
				LocalDateTime.of(2026, 7, 1, 17, 0),
				LocalDateTime.of(2026, 7, 1, 18, 0),
				null
		));
		Offering second = createOffering(202L, "Debate Club", "Batch B");
		offeringService.addSession(second.getId(), new AddSessionRequest(
				202L,
				LocalDateTime.of(2026, 7, 1, 17, 30),
				LocalDateTime.of(2026, 7, 1, 18, 30),
				null
		));

		CountDownLatch start = new CountDownLatch(1);
		Callable<Boolean> bookFirst = () -> bookAfterLatch(start, 777L, first.getId());
		Callable<Boolean> bookSecond = () -> bookAfterLatch(start, 777L, second.getId());
		var executor = Executors.newFixedThreadPool(2);
		try {
			Future<Boolean> firstResult = executor.submit(bookFirst);
			Future<Boolean> secondResult = executor.submit(bookSecond);
			start.countDown();

			List<Boolean> results = List.of(firstResult.get(), secondResult.get());

			assertThat(results).containsExactlyInAnyOrder(true, false);
			assertThat(bookingRepository.findByParentIdOrderByBookedAtDesc(777L)).hasSize(1);
		}
		finally {
			executor.shutdownNow();
		}
	}

	private Boolean bookAfterLatch(CountDownLatch start, Long parentId, Long offeringId) throws Exception {
		start.await();
		try {
			Booking ignored = bookingService.bookOffering(new BookOfferingRequest(parentId, offeringId));
			return true;
		}
		catch (ApiException exception) {
			assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
			return false;
		}
	}

	private Offering createOffering(Long teacherId, String courseName, String title) {
		return offeringService.createOffering(new CreateOfferingRequest(
				teacherId,
				courseName,
				title,
				"Asia/Kolkata"
		));
	}
}
