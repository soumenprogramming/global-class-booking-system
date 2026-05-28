package com.soumen.global_class_booking_system.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "bookings", uniqueConstraints = {
		@UniqueConstraint(name = "uk_bookings_parent_offering", columnNames = {"parent_id", "offering_id"})
})
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "parent_id", nullable = false)
	private ParentAccount parent;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "offering_id", nullable = false)
	private Offering offering;

	@Column(nullable = false, updatable = false)
	private Instant bookedAt = Instant.now();

	protected Booking() {
	}

	public Booking(ParentAccount parent, Offering offering) {
		this.parent = parent;
		this.offering = offering;
	}

	public Long getId() {
		return id;
	}

	public ParentAccount getParent() {
		return parent;
	}

	public Offering getOffering() {
		return offering;
	}

	public Instant getBookedAt() {
		return bookedAt;
	}
}
