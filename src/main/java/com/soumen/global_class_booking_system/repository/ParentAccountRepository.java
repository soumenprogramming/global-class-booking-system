package com.soumen.global_class_booking_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.soumen.global_class_booking_system.domain.ParentAccount;

import jakarta.persistence.LockModeType;

public interface ParentAccountRepository extends JpaRepository<ParentAccount, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from ParentAccount p where p.id = :id")
	Optional<ParentAccount> findByIdForUpdate(@Param("id") Long id);
}
