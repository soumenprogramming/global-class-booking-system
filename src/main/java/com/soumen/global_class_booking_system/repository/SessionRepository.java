package com.soumen.global_class_booking_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soumen.global_class_booking_system.domain.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {
}
