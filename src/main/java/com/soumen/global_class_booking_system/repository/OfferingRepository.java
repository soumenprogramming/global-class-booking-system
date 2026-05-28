package com.soumen.global_class_booking_system.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soumen.global_class_booking_system.domain.Offering;

public interface OfferingRepository extends JpaRepository<Offering, Long> {

	@EntityGraph(attributePaths = {"teacher", "course", "sessions"})
	@Query("select distinct o from Offering o left join o.sessions s where o.teacher.id = :teacherId and (s.endAt is null or s.endAt > :now) order by o.createdAt desc")
	List<Offering> findUpcomingByTeacher(@Param("teacherId") Long teacherId, @Param("now") Instant now);

	@EntityGraph(attributePaths = {"teacher", "course", "sessions"})
	@Query("select distinct o from Offering o join o.sessions s where s.endAt > :now order by o.createdAt desc")
	List<Offering> findAvailable(@Param("now") Instant now);

	@EntityGraph(attributePaths = {"teacher", "course", "sessions"})
	@Query("select o from Offering o where o.id = :id")
	Optional<Offering> findWithSessionsById(@Param("id") Long id);
}
