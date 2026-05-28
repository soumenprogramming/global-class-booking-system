package com.soumen.global_class_booking_system.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soumen.global_class_booking_system.domain.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	boolean existsByParentIdAndOfferingId(Long parentId, Long offeringId);

	boolean existsByOfferingId(Long offeringId);

	@Query("""
			select count(b) > 0
			from Booking b
			join b.offering o
			join o.sessions s
			where b.parent.id = :parentId
			  and s.startAt < :endAt
			  and s.endAt > :startAt
			""")
	boolean existsOverlappingBooking(@Param("parentId") Long parentId,
			@Param("startAt") Instant startAt,
			@Param("endAt") Instant endAt);

	@EntityGraph(attributePaths = {"offering", "offering.course", "offering.teacher", "offering.sessions"})
	List<Booking> findByParentIdOrderByBookedAtDesc(Long parentId);
}
