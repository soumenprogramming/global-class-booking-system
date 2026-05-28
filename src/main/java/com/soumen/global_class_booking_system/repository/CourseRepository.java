package com.soumen.global_class_booking_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soumen.global_class_booking_system.domain.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

	Optional<Course> findByNormalizedName(String normalizedName);
}
