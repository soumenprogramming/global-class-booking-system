create table if not exists courses (
	id bigserial primary key,
	name varchar(255) not null,
	normalized_name varchar(255) not null,
	created_at timestamp(6) with time zone not null
)@@

create unique index if not exists uk_courses_normalized_name on courses (normalized_name)@@

alter table offerings add column if not exists course_id bigint@@

alter table offerings alter column course_name drop not null@@

insert into courses (name, normalized_name, created_at)
select distinct
	trim(course_name),
	lower(regexp_replace(trim(course_name), '\s+', ' ', 'g')),
	now()
from offerings
where course_name is not null
  and trim(course_name) <> ''
  and not exists (
	  select 1
	  from courses
	  where courses.normalized_name = lower(regexp_replace(trim(offerings.course_name), '\s+', ' ', 'g'))
  )@@

insert into courses (name, normalized_name, created_at)
select 'Legacy Course', 'legacy course', now()
where not exists (
	select 1
	from courses
	where normalized_name = 'legacy course'
)@@

update offerings
set course_id = courses.id
from courses
where offerings.course_id is null
  and offerings.course_name is not null
  and trim(offerings.course_name) <> ''
  and courses.normalized_name = lower(regexp_replace(trim(offerings.course_name), '\s+', ' ', 'g'))@@

update offerings
set course_id = (
	select id
	from courses
	where normalized_name = 'legacy course'
	limit 1
)
where course_id is null@@

alter table offerings alter column course_id set not null@@
