package com.soumen.global_class_booking_system.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "offerings")
public class Offering {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String teacherTimeZone;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "teacher_id", nullable = false)
	private Teacher teacher;

	@OneToMany(mappedBy = "offering", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("startAt ASC")
	private List<Session> sessions = new ArrayList<>();

	@Column(nullable = false, updatable = false)
	private Instant createdAt = Instant.now();

	protected Offering() {
	}

	public Offering(Teacher teacher, Course course, String title, String teacherTimeZone) {
		this.teacher = teacher;
		this.course = course;
		this.title = title;
		this.teacherTimeZone = teacherTimeZone;
	}

	public Long getId() {
		return id;
	}

	public Course getCourse() {
		return course;
	}

	public String getTitle() {
		return title;
	}

	public String getTeacherTimeZone() {
		return teacherTimeZone;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void addSession(Session session) {
		sessions.add(session);
		session.assignOffering(this);
	}
}
