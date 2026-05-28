package com.soumen.global_class_booking_system.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parent_accounts")
public class ParentAccount {

	@Id
	private Long id;

	@Column(nullable = false)
	private String name;

	protected ParentAccount() {
	}

	public ParentAccount(Long id) {
		this.id = id;
		this.name = "Parent " + id;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
