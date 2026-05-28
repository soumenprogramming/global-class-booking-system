package com.soumen.global_class_booking_system.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sessions", indexes = {
		@Index(name = "idx_sessions_start_end", columnList = "start_at,end_at"),
		@Index(name = "idx_sessions_offering", columnList = "offering_id")
})
public class Session {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "offering_id", nullable = false)
	private Offering offering;

	@Column(name = "teacher_id", nullable = false)
	private Long teacherId;

	@Column(name = "start_at", nullable = false)
	private Instant startAt;

	@Column(name = "end_at", nullable = false)
	private Instant endAt;

	protected Session() {
	}

	public Session(Long teacherId, Instant startAt, Instant endAt) {
		this.teacherId = teacherId;
		this.startAt = startAt;
		this.endAt = endAt;
	}

	public Long getId() {
		return id;
	}

	public Offering getOffering() {
		return offering;
	}

	public Long getTeacherId() {
		return teacherId;
	}

	public Instant getStartAt() {
		return startAt;
	}

	public Instant getEndAt() {
		return endAt;
	}

	void assignOffering(Offering offering) {
		this.offering = offering;
	}
}
