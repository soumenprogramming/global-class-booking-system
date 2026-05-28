package com.soumen.global_class_booking_system.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "courses", uniqueConstraints = {
		@UniqueConstraint(name = "uk_courses_normalized_name", columnNames = "normalized_name")
})
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "normalized_name", nullable = false)
	private String normalizedName;

	@Column(nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	protected Course() {
	}

	public Course(String name) {
		this.name = name;
		this.normalizedName = normalize(name);
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNormalizedName() {
		return normalizedName;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public static String normalize(String name) {
		return name.trim().replaceAll("\\s+", " ").toLowerCase();
	}
}
