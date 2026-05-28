package com.soumen.global_class_booking_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soumen.global_class_booking_system.domain.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
}
