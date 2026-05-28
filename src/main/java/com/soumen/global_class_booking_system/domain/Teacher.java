package com.soumen.global_class_booking_system.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teachers")
public class Teacher {

	@Id
	private Long id;

	@Column(nullable = false)
	private String name;

	protected Teacher() {
	}

	public Teacher(Long id) {
		this.id = id;
		this.name = "Teacher " + id;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
