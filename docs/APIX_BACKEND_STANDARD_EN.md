<!-- Before doing this task, read and follow the backend standard file:

docs/APIX_BACKEND_STANDARD.md

Project context:
- Spring Boot 3.5.16
- Java 21
- Maven
- PostgreSQL
- Flyway
- Spring Data JPA
- Spring Security
- Lombok
- Validation

Base package:
com.apixenglish.center

Rules:
- Do not delete existing code.
- Do not rewrite unrelated files.
- Only add or modify files required for this task.
- Follow the existing package structure.
- Follow the naming conventions in the backend standard.
- Use clean code and production-ready Spring Boot best practices.
- Explain which files are created or changed.

Task:
[PASTE TASK HERE] -->


# APIX English Center Management System — Backend Engineering Standard

> Version: 2.0  
> Language: English  
> Target reader: Backend engineers, frontend engineers, QA, DevOps, and AI coding assistants.  
> Product: APIX English Center Management System for a private English center teaching children and teenagers.  
> Architecture goal: production-ready, secure, scalable, maintainable, and practical for a medium-sized English center.

---

## 0. AI Reading Rules

This document is intentionally written in English because most technical naming, code conventions, database objects, APIs, framework documentation, and AI coding models work more consistently with English technical specifications.

When generating code from this file:

- Keep all database tables, columns, Java classes, DTOs, API routes, TypeScript types, and folder names in English.
- Do not translate domain names into Vietnamese in code.
- Preserve the business rules exactly.
- Do not hardcode roles such as `TEACHER`, `OWNER`, or `OFFICE_STAFF` inside business logic.
- Always check permissions by permission code, not by role name.
- Use PostgreSQL as the system of record.
- Treat `students.user_id` and `parents.user_id` as optional because young students and some parents may not have login accounts yet.

---

## 1. Product Overview

APIX English Center Management System is an internal ERP-style web application for a private English center.

The system manages:

- Employees
- Positions
- Users
- Roles and permissions
- Students
- Parents
- Campus and rooms
- Courses, levels, curriculum, lessons
- Classes and schedules
- Enrollments, transfers, freezes, trials, make-up classes, graduation
- Attendance
- Homework
- Scores
- Learning reports
- Tuition, invoices, payments, refunds, installments, discounts, vouchers
- Payroll, teaching hours, bonuses, penalties, leave requests
- Notifications
- Contact logs between office staff and parents
- AI-assisted report generation, homework generation, lesson plan generation, and student progress analysis

This is not a SaaS system. There is only one organization. The center may have multiple campuses/buildings inside the same residential area.

Use this location model:

```text
Campus
  └── Room
```

Do not use this model:

```text
Organization
  └── Branch
```

---

## 2. Core Business Rules

### 2.1 User, employee, parent, and student separation

A `user` is only a login account. It is not the same as an employee, parent, or student profile.

Correct model:

```text
users      = authentication account
employees  = employee profile
parents    = parent profile
students   = student profile
```

Important rule:

```text
A student profile can exist without a user account.
A parent profile can exist without a user account.
An employee should normally have a user account.
```

This supports young students such as kindergarten children who do not have phones or login accounts.

### 2.2 Student account modes

Use `students.access_mode`:

| Access mode | Meaning |
|---|---|
| `NO_ACCOUNT` | Student has no login account. The center only stores a student profile. |
| `PARENT_MANAGED` | Parent logs in and manages/views the student's information. Common for young children. |
| `OWN_ACCOUNT` | Student has a personal login account. Common for teenagers. |

Use `students.student_type`:

| Student type | Meaning |
|---|---|
| `KINDERGARTEN` | Very young student. No direct login expected. |
| `CHILD` | Primary school age. Usually parent-managed. |
| `TEENAGER` | Secondary/high school age. May have own account. |
| `ADULT` | Adult learner. Usually has own account. |

### 2.3 Parent relationship and pickup permission

Young students require parent/guardian controls.

The `student_parents` table must store:

- Relationship: father, mother, guardian, sibling, other
- Primary contact flag
- Tuition notification permission
- General notification permission
- Pickup permission
- Emergency contact flag

### 2.4 RBAC rules

Do not hardcode role checks.

Wrong:

```java
if (user.getRole().equals("TEACHER")) {
    // allow action
}
```

Correct:

```java
permissionChecker.require(userId, "attendance:mark");
```

A user can have multiple roles.  
A role can have multiple permissions.  
A permission belongs to a module and an action.  
A role assignment may optionally be scoped to a campus.

### 2.5 Employee positions

One employee can have multiple positions.

Example:

```text
Employee A
  - Teacher
  - Office Staff
```

Therefore, positions must not be stored as a single string in the `employees` table.

Use:

```text
positions
employee_positions
```

### 2.6 Teacher and parent communication rule

Teachers must never communicate directly with parents through the system.

Teachers can create:

- Attendance records
- Homework assignments
- Scores
- Learning report drafts
- Lesson material notes

Office Staff can deliver:

- Homework notifications
- Learning reports
- Score notifications
- Tuition notifications
- Parent communication

Every communication between Office Staff and Parent must be stored in `contact_logs`.

### 2.7 Scheduling conflict rules

The system must detect:

- Room conflict
- Teacher conflict
- Teaching Assistant conflict

Conflict examples:

```text
Same room, same date/time, two active classes.
Same teacher, same date/time, two active sessions.
Same assistant, same date/time, two active sessions.
```

### 2.8 Financial safety rules

- Never update invoice totals without a database transaction.
- Never delete financial records physically.
- Use soft delete only for correction workflows where allowed.
- Payments must be append-only whenever possible.
- Refunds must reference the original payment and invoice.
- Use optimistic locking for invoices and payments.

---

## 3. Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Security | Spring Security 6 |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Database migration | Flyway |
| Cache | Redis |
| Queue | RabbitMQ |
| Scheduler | Quartz Scheduler |
| Mapping | MapStruct |
| Validation | Jakarta Bean Validation |
| API documentation | OpenAPI / Swagger |
| Testing | JUnit 5, Mockito, Testcontainers |
| Container | Docker, Docker Compose |
| Monitoring | Spring Actuator, Prometheus, Grafana |
| Logging | SLF4J, Logback, structured JSON logs |
| File storage | MinIO for local, Cloudflare R2 or AWS S3 for production |

### 3.1 Initial development stack

For the first production-ready version, start with:

```text
Spring Boot + PostgreSQL + Flyway + Redis + Docker Compose
```

Add RabbitMQ when background tasks become real:

- Tuition reminders
- Email/SMS/Zalo notifications
- AI report generation
- Salary calculation
- Large export jobs

---

## 4. Backend Architecture

Use a modular monolith first.

Do not start with microservices. The domain is large, but the team is likely small. A modular monolith gives clear boundaries without distributed system complexity.

### 4.1 Recommended architecture style

Use a practical combination of:

- Clean Architecture
- Domain-driven module boundaries
- Hexagonal thinking for external integrations
- Service layer
- Repository pattern
- Specification pattern for filtering/searching
- Event-driven pattern for notifications and background workflows
- Strategy pattern for payment, notification, and AI providers

### 4.2 Project structure

```text
src/main/java/com/apix/englishcenter
│
├── ApixEnglishCenterApplication.java
│
├── common
│   ├── domain
│   │   ├── BaseEntity.java
│   │   ├── AuditableEntity.java
│   │   └── SoftDeletableEntity.java
│   ├── dto
│   │   ├── ApiResponse.java
│   │   ├── PageResponse.java
│   │   └── ErrorResponse.java
│   ├── exception
│   ├── pagination
│   ├── specification
│   ├── validation
│   └── util
│
├── config
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   ├── RabbitMqConfig.java
│   ├── QuartzConfig.java
│   ├── OpenApiConfig.java
│   ├── CorsConfig.java
│   └── JpaConfig.java
│
├── security
│   ├── jwt
│   ├── rbac
│   ├── filter
│   ├── userdetails
│   └── annotation
│
├── modules
│   ├── auth
│   ├── user
│   ├── role
│   ├── permission
│   ├── employee
│   ├── position
│   ├── parent
│   ├── student
│   ├── campus
│   ├── room
│   ├── course
│   ├── curriculum
│   ├── classmanagement
│   ├── schedule
│   ├── enrollment
│   ├── attendance
│   ├── homework
│   ├── score
│   ├── learningreport
│   ├── tuition
│   ├── payroll
│   ├── notification
│   ├── contactlog
│   ├── filestorage
│   ├── dashboard
│   ├── ai
│   └── settings
│
└── jobs
    ├── tuition
    ├── attendance
    ├── payroll
    ├── report
    └── backup
```

Each business module should follow this shape:

```text
modules/student
│
├── controller
├── service
│   └── impl
├── repository
├── entity
├── dto
│   ├── request
│   └── response
├── mapper
├── validator
├── specification
├── event
├── listener
└── exception
```

---

## 5. Database Standard

### 5.1 Database choice

Use PostgreSQL as the primary database.

Reasons:

- Strong relational modeling
- Foreign keys and transactions
- Good indexing
- JSONB support for flexible metadata
- Excellent reporting capability
- Suitable for attendance, finance, payroll, and audit records

### 5.2 Common columns for almost every table

Every main table must contain:

| Column | Type | Rule |
|---|---|---|
| `id` | `UUID` | Primary key |
| `created_at` | `TIMESTAMPTZ` | Not null |
| `updated_at` | `TIMESTAMPTZ` | Not null |
| `created_by` | `UUID` | Nullable FK to `users.id` |
| `updated_by` | `UUID` | Nullable FK to `users.id` |
| `deleted_at` | `TIMESTAMPTZ` | Nullable soft delete marker |
| `version` | `INTEGER` | Not null, optimistic locking |

Do not physically delete important business records. Use `deleted_at`.

### 5.3 PostgreSQL naming rules

| Item | Rule | Example |
|---|---|---|
| Table name | plural snake_case | `student_attendance` |
| Column name | snake_case | `created_at` |
| Primary key | `id` | `id UUID PRIMARY KEY` |
| Foreign key | singular table name + `_id` | `student_id` |
| Index | `idx_table_columns` | `idx_students_status` |
| Unique constraint | `uk_table_columns` | `uk_users_email` |
| Check constraint | `chk_table_rule` | `chk_rooms_capacity_positive` |

---

## 6. Database Design

### 6.1 Identity and access control

#### `users`

Stores login accounts only. A user can be linked to an employee, parent, or student profile.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `email` | VARCHAR(255) | Unique, nullable for phone-only accounts |
| `phone` | VARCHAR(30) | Unique, nullable for email-only accounts |
| `username` | VARCHAR(100) | Unique, nullable |
| `password_hash` | VARCHAR(255) | Not null |
| `full_name` | VARCHAR(255) | Not null |
| `avatar_url` | TEXT | Nullable |
| `status` | VARCHAR(30) | ACTIVE, INACTIVE, LOCKED, PENDING |
| `email_verified` | BOOLEAN | Default false |
| `phone_verified` | BOOLEAN | Default false |
| `mfa_enabled` | BOOLEAN | Default false |
| `last_login_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

Constraints:

```sql
CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED', 'PENDING'))
CHECK (email IS NOT NULL OR phone IS NOT NULL OR username IS NOT NULL)
```

#### `refresh_tokens`

Stores hashed refresh tokens for rotation and revocation.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `user_id` | UUID | FK users.id |
| `token_hash` | VARCHAR(255) | Unique, not null |
| `expires_at` | TIMESTAMPTZ | Not null |
| `revoked_at` | TIMESTAMPTZ | Nullable |
| `replaced_by_token_id` | UUID | Nullable FK refresh_tokens.id |
| `device_info` | TEXT | Nullable |
| `ip_address` | VARCHAR(100) | Nullable |
| common columns | | Required |

#### `roles`

Stores dynamic roles. Do not hardcode role logic.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `code` | VARCHAR(100) | Unique, uppercase-like code |
| `name` | VARCHAR(150) | Not null |
| `description` | TEXT | Nullable |
| `is_system` | BOOLEAN | Default false |
| `is_active` | BOOLEAN | Default true |
| common columns | | Required |

#### `permissions`

Stores atomic permissions.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `code` | VARCHAR(150) | Unique, example `student:create` |
| `module` | VARCHAR(100) | Not null |
| `action` | VARCHAR(100) | Not null |
| `description` | TEXT | Nullable |
| `is_active` | BOOLEAN | Default true |
| common columns | | Required |

#### `role_permissions`

Many-to-many table between roles and permissions.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `role_id` | UUID | FK roles.id |
| `permission_id` | UUID | FK permissions.id |
| common columns | | Required |

Unique:

```sql
UNIQUE (role_id, permission_id)
```

#### `user_roles`

Assigns roles to users. Can be scoped by campus.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `user_id` | UUID | FK users.id |
| `role_id` | UUID | FK roles.id |
| `campus_id` | UUID | Nullable FK campuses.id |
| `assigned_at` | TIMESTAMPTZ | Not null |
| `expired_at` | TIMESTAMPTZ | Nullable |
| `is_active` | BOOLEAN | Default true |
| common columns | | Required |

Unique:

```sql
UNIQUE (user_id, role_id, campus_id)
```

---

### 6.2 Employee and position management

#### `employees`

Stores employee profiles.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `user_id` | UUID | Unique FK users.id, normally not null |
| `employee_code` | VARCHAR(50) | Unique, not null |
| `date_of_birth` | DATE | Nullable |
| `gender` | VARCHAR(20) | Nullable |
| `address` | TEXT | Nullable |
| `emergency_contact_name` | VARCHAR(255) | Nullable |
| `emergency_contact_phone` | VARCHAR(30) | Nullable |
| `hired_date` | DATE | Not null |
| `resigned_date` | DATE | Nullable |
| `working_status` | VARCHAR(30) | WORKING, PROBATION, SUSPENDED, RESIGNED |
| `note` | TEXT | Nullable |
| common columns | | Required |

#### `positions`

Stores job positions such as Teacher, Office Staff, Teaching Assistant, Accountant, Academic Manager.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `code` | VARCHAR(100) | Unique |
| `name` | VARCHAR(150) | Not null |
| `description` | TEXT | Nullable |
| `is_teaching_position` | BOOLEAN | Default false |
| `is_active` | BOOLEAN | Default true |
| common columns | | Required |

#### `employee_positions`

Stores employee-position assignments.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `employee_id` | UUID | FK employees.id |
| `position_id` | UUID | FK positions.id |
| `campus_id` | UUID | Nullable FK campuses.id |
| `start_date` | DATE | Not null |
| `end_date` | DATE | Nullable |
| `is_primary` | BOOLEAN | Default false |
| `status` | VARCHAR(30) | ACTIVE, INACTIVE |
| common columns | | Required |

#### `employee_contracts`

Stores employee contracts.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `employee_id` | UUID | FK employees.id |
| `contract_number` | VARCHAR(100) | Unique |
| `contract_type` | VARCHAR(50) | FULL_TIME, PART_TIME, FREELANCE, PROBATION |
| `start_date` | DATE | Not null |
| `end_date` | DATE | Nullable |
| `base_salary` | NUMERIC(14,2) | Default 0 |
| `terms` | TEXT | Nullable |
| `status` | VARCHAR(30) | DRAFT, ACTIVE, EXPIRED, TERMINATED |
| common columns | | Required |

#### `employee_certificates`

Stores certificates and qualifications.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `employee_id` | UUID | FK employees.id |
| `certificate_name` | VARCHAR(255) | Not null |
| `issuer` | VARCHAR(255) | Nullable |
| `issued_date` | DATE | Nullable |
| `expired_date` | DATE | Nullable |
| `file_id` | UUID | Nullable FK file_objects.id |
| common columns | | Required |

---

### 6.3 Parent and student management

#### `parents`

Stores parent or guardian profiles. `user_id` is nullable because the center may only store contact information first.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `user_id` | UUID | Nullable unique FK users.id |
| `parent_code` | VARCHAR(50) | Unique, not null |
| `full_name` | VARCHAR(255) | Not null |
| `phone` | VARCHAR(30) | Not null |
| `email` | VARCHAR(255) | Nullable |
| `address` | TEXT | Nullable |
| `job_title` | VARCHAR(255) | Nullable |
| `note` | TEXT | Nullable |
| common columns | | Required |

#### `students`

Stores student profiles. `user_id` is nullable because young students may not have login accounts.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `user_id` | UUID | Nullable unique FK users.id |
| `student_code` | VARCHAR(50) | Unique, not null |
| `full_name` | VARCHAR(255) | Not null |
| `date_of_birth` | DATE | Not null |
| `gender` | VARCHAR(20) | Nullable |
| `school_name` | VARCHAR(255) | Nullable |
| `grade` | VARCHAR(50) | Nullable |
| `avatar_url` | TEXT | Nullable |
| `medical_notes` | TEXT | Nullable |
| `learning_notes` | TEXT | Nullable |
| `student_type` | VARCHAR(30) | KINDERGARTEN, CHILD, TEENAGER, ADULT |
| `access_mode` | VARCHAR(30) | NO_ACCOUNT, PARENT_MANAGED, OWN_ACCOUNT |
| `status` | VARCHAR(30) | ACTIVE, INACTIVE, GRADUATED, PAUSED |
| common columns | | Required |

Constraints:

```sql
CHECK (student_type IN ('KINDERGARTEN', 'CHILD', 'TEENAGER', 'ADULT'))
CHECK (access_mode IN ('NO_ACCOUNT', 'PARENT_MANAGED', 'OWN_ACCOUNT'))
CHECK (status IN ('ACTIVE', 'INACTIVE', 'GRADUATED', 'PAUSED'))
CHECK (
  (access_mode = 'OWN_ACCOUNT' AND user_id IS NOT NULL)
  OR
  (access_mode IN ('NO_ACCOUNT', 'PARENT_MANAGED'))
)
```

#### `student_parents`

Links students and parents. Supports multiple children per parent and multiple guardians per child.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `student_id` | UUID | FK students.id |
| `parent_id` | UUID | FK parents.id |
| `relationship` | VARCHAR(50) | FATHER, MOTHER, GUARDIAN, SIBLING, OTHER |
| `is_primary_contact` | BOOLEAN | Default false |
| `can_receive_notification` | BOOLEAN | Default true |
| `can_receive_tuition` | BOOLEAN | Default true |
| `can_pickup_student` | BOOLEAN | Default false |
| `is_emergency_contact` | BOOLEAN | Default false |
| common columns | | Required |

Unique:

```sql
UNIQUE (student_id, parent_id)
```

---

### 6.4 Campus and room management

#### `campuses`

Stores buildings/campuses in the same residential area.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `code` | VARCHAR(50) | Unique |
| `name` | VARCHAR(255) | Not null |
| `address` | TEXT | Nullable |
| `phone` | VARCHAR(30) | Nullable |
| `description` | TEXT | Nullable |
| `is_active` | BOOLEAN | Default true |
| common columns | | Required |

#### `rooms`

Stores classrooms.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `campus_id` | UUID | FK campuses.id |
| `code` | VARCHAR(50) | Not null |
| `name` | VARCHAR(255) | Not null |
| `capacity` | INTEGER | Must be greater than 0 |
| `room_type` | VARCHAR(50) | CLASSROOM, OFFICE, TESTING, OTHER |
| `facilities_note` | TEXT | Nullable |
| `is_active` | BOOLEAN | Default true |
| common columns | | Required |

Unique:

```sql
UNIQUE (campus_id, code)
```

#### `room_facilities`

Stores room equipment.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `room_id` | UUID | FK rooms.id |
| `facility_name` | VARCHAR(255) | Not null |
| `quantity` | INTEGER | Default 1 |
| `condition_status` | VARCHAR(50) | GOOD, NEED_REPAIR, BROKEN |
| `note` | TEXT | Nullable |
| common columns | | Required |

---

### 6.5 Course and curriculum management

#### `levels`

Stores course levels.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `code` | VARCHAR(50) | Unique |
| `name` | VARCHAR(150) | Not null |
| `order_index` | INTEGER | For display order |
| `description` | TEXT | Nullable |
| `is_active` | BOOLEAN | Default true |
| common columns | | Required |

#### `courses`

Stores sellable/teachable courses.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `level_id` | UUID | FK levels.id |
| `code` | VARCHAR(50) | Unique |
| `name` | VARCHAR(255) | Not null |
| `description` | TEXT | Nullable |
| `total_lessons` | INTEGER | Must be greater than 0 |
| `duration_minutes` | INTEGER | Default lesson duration |
| `default_tuition_fee` | NUMERIC(14,2) | Default 0 |
| `status` | VARCHAR(30) | DRAFT, ACTIVE, INACTIVE, ARCHIVED |
| common columns | | Required |

#### `curriculums`

Stores curriculum versions for a course.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `course_id` | UUID | FK courses.id |
| `name` | VARCHAR(255) | Not null |
| `version_name` | VARCHAR(100) | Not null |
| `description` | TEXT | Nullable |
| `is_active` | BOOLEAN | Default true |
| common columns | | Required |

#### `lessons`

Stores lessons inside a curriculum.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `curriculum_id` | UUID | FK curriculums.id |
| `lesson_no` | INTEGER | Not null |
| `title` | VARCHAR(255) | Not null |
| `objective` | TEXT | Nullable |
| `content_summary` | TEXT | Nullable |
| `duration_minutes` | INTEGER | Nullable |
| common columns | | Required |

Unique:

```sql
UNIQUE (curriculum_id, lesson_no)
```

#### `vocabulary_items`

Stores vocabulary items for lessons.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `lesson_id` | UUID | FK lessons.id |
| `word` | VARCHAR(255) | Not null |
| `ipa` | VARCHAR(255) | Nullable |
| `meaning_vi` | TEXT | Nullable |
| `example_sentence` | TEXT | Nullable |
| common columns | | Required |

#### `grammar_items`

Stores grammar points for lessons.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `lesson_id` | UUID | FK lessons.id |
| `title` | VARCHAR(255) | Not null |
| `explanation` | TEXT | Nullable |
| `formula` | TEXT | Nullable |
| `examples` | TEXT | Nullable |
| common columns | | Required |

---

### 6.6 Class, enrollment, and schedule management

#### `classes`

Stores opened classes.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `course_id` | UUID | FK courses.id |
| `campus_id` | UUID | FK campuses.id |
| `class_code` | VARCHAR(50) | Unique |
| `name` | VARCHAR(255) | Not null |
| `capacity` | INTEGER | Must be greater than 0 |
| `start_date` | DATE | Not null |
| `expected_end_date` | DATE | Nullable |
| `status` | VARCHAR(30) | PLANNED, ACTIVE, PAUSED, COMPLETED, CANCELLED |
| `note` | TEXT | Nullable |
| common columns | | Required |

#### `class_staff`

Assigns teachers and teaching assistants to classes.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `class_id` | UUID | FK classes.id |
| `employee_id` | UUID | FK employees.id |
| `staff_type` | VARCHAR(30) | TEACHER, TEACHING_ASSISTANT |
| `start_date` | DATE | Not null |
| `end_date` | DATE | Nullable |
| `is_primary` | BOOLEAN | Default false |
| common columns | | Required |

#### `class_enrollments`

Stores student enrollment in classes.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `class_id` | UUID | FK classes.id |
| `student_id` | UUID | FK students.id |
| `enrollment_code` | VARCHAR(50) | Unique |
| `enrolled_date` | DATE | Not null |
| `start_date` | DATE | Not null |
| `end_date` | DATE | Nullable |
| `status` | VARCHAR(30) | TRIAL, ACTIVE, FROZEN, TRANSFERRED, COMPLETED, CANCELLED |
| `source` | VARCHAR(50) | WALK_IN, REFERRAL, ONLINE, OTHER |
| `note` | TEXT | Nullable |
| common columns | | Required |

#### `enrollment_transfers`

Stores class transfer history.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `student_id` | UUID | FK students.id |
| `from_enrollment_id` | UUID | FK class_enrollments.id |
| `to_enrollment_id` | UUID | FK class_enrollments.id |
| `transfer_date` | DATE | Not null |
| `reason` | TEXT | Nullable |
| `approved_by` | UUID | FK employees.id |
| common columns | | Required |

#### `enrollment_freezes`

Stores temporary freeze/pause periods.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `enrollment_id` | UUID | FK class_enrollments.id |
| `start_date` | DATE | Not null |
| `end_date` | DATE | Not null |
| `reason` | TEXT | Not null |
| `status` | VARCHAR(30) | REQUESTED, APPROVED, REJECTED, CANCELLED |
| `approved_by` | UUID | Nullable FK employees.id |
| `approved_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

#### `class_schedules`

Stores recurring weekly schedule definitions.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `class_id` | UUID | FK classes.id |
| `room_id` | UUID | FK rooms.id |
| `day_of_week` | SMALLINT | 1 to 7 |
| `start_time` | TIME | Not null |
| `end_time` | TIME | Not null |
| `effective_from` | DATE | Not null |
| `effective_to` | DATE | Nullable |
| `status` | VARCHAR(30) | ACTIVE, INACTIVE |
| common columns | | Required |

Constraints:

```sql
CHECK (day_of_week BETWEEN 1 AND 7)
CHECK (start_time < end_time)
```

#### `class_sessions`

Stores actual generated class sessions.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `class_id` | UUID | FK classes.id |
| `schedule_id` | UUID | Nullable FK class_schedules.id |
| `room_id` | UUID | FK rooms.id |
| `session_date` | DATE | Not null |
| `start_time` | TIME | Not null |
| `end_time` | TIME | Not null |
| `lesson_no` | INTEGER | Nullable |
| `status` | VARCHAR(30) | PLANNED, COMPLETED, CANCELLED, RESCHEDULED |
| `note` | TEXT | Nullable |
| common columns | | Required |

#### `schedule_conflicts`

Stores detected conflicts.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `class_schedule_id` | UUID | Nullable FK class_schedules.id |
| `conflict_type` | VARCHAR(50) | ROOM, TEACHER, TEACHING_ASSISTANT |
| `related_class_id` | UUID | Nullable FK classes.id |
| `related_employee_id` | UUID | Nullable FK employees.id |
| `related_room_id` | UUID | Nullable FK rooms.id |
| `message` | TEXT | Not null |
| `status` | VARCHAR(30) | OPEN, RESOLVED, IGNORED |
| `detected_at` | TIMESTAMPTZ | Not null |
| `resolved_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

---

### 6.7 Attendance management

#### `student_attendance`

Stores attendance for students per session.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `session_id` | UUID | FK class_sessions.id |
| `student_id` | UUID | FK students.id |
| `status` | VARCHAR(30) | PRESENT, LATE, ABSENT, EXCUSED |
| `check_in_time` | TIME | Nullable |
| `check_out_time` | TIME | Nullable |
| `note` | TEXT | Nullable |
| `marked_by` | UUID | FK employees.id |
| `marked_at` | TIMESTAMPTZ | Not null |
| common columns | | Required |

Unique:

```sql
UNIQUE (session_id, student_id)
```

#### `employee_attendance`

Stores teacher/assistant attendance per session.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `session_id` | UUID | FK class_sessions.id |
| `employee_id` | UUID | FK employees.id |
| `status` | VARCHAR(30) | PRESENT, LATE, ABSENT, EXCUSED |
| `check_in_time` | TIME | Nullable |
| `check_out_time` | TIME | Nullable |
| `note` | TEXT | Nullable |
| `marked_by` | UUID | FK employees.id |
| `marked_at` | TIMESTAMPTZ | Not null |
| common columns | | Required |

---

### 6.8 Homework, score, and learning report

#### `homework_assignments`

Stores homework assigned to a class.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `class_id` | UUID | FK classes.id |
| `lesson_id` | UUID | Nullable FK lessons.id |
| `assigned_by` | UUID | FK employees.id |
| `title` | VARCHAR(255) | Not null |
| `description` | TEXT | Nullable |
| `due_at` | TIMESTAMPTZ | Nullable |
| `status` | VARCHAR(30) | DRAFT, PUBLISHED, CLOSED |
| common columns | | Required |

#### `homework_submissions`

Stores student submissions.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `homework_id` | UUID | FK homework_assignments.id |
| `student_id` | UUID | FK students.id |
| `answer_text` | TEXT | Nullable |
| `file_id` | UUID | Nullable FK file_objects.id |
| `submitted_at` | TIMESTAMPTZ | Nullable |
| `status` | VARCHAR(30) | NOT_SUBMITTED, SUBMITTED, LATE, GRADED |
| `teacher_feedback` | TEXT | Nullable |
| `score` | NUMERIC(6,2) | Nullable |
| `graded_at` | TIMESTAMPTZ | Nullable |
| `graded_by` | UUID | Nullable FK employees.id |
| common columns | | Required |

#### `score_components`

Stores reusable score components.

Examples: Listening, Speaking, Reading, Writing, Grammar, Midterm, Final.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `code` | VARCHAR(100) | Unique |
| `name` | VARCHAR(150) | Not null |
| `skill_type` | VARCHAR(50) | LISTENING, SPEAKING, READING, WRITING, GRAMMAR, EXAM, OTHER |
| `default_weight` | NUMERIC(5,2) | Default 0 |
| `is_active` | BOOLEAN | Default true |
| common columns | | Required |

#### `assessments`

Stores tests or assessments for a class.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `class_id` | UUID | FK classes.id |
| `score_component_id` | UUID | FK score_components.id |
| `title` | VARCHAR(255) | Not null |
| `assessment_date` | DATE | Nullable |
| `max_score` | NUMERIC(6,2) | Not null |
| `weight` | NUMERIC(5,2) | Default 0 |
| `status` | VARCHAR(30) | DRAFT, PUBLISHED, LOCKED |
| common columns | | Required |

#### `student_scores`

Stores each student's score for an assessment.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `assessment_id` | UUID | FK assessments.id |
| `student_id` | UUID | FK students.id |
| `score` | NUMERIC(6,2) | Not null |
| `comment` | TEXT | Nullable |
| `graded_by` | UUID | FK employees.id |
| `graded_at` | TIMESTAMPTZ | Not null |
| common columns | | Required |

Unique:

```sql
UNIQUE (assessment_id, student_id)
```

#### `learning_reports`

Stores weekly, monthly, and final learning reports.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `student_id` | UUID | FK students.id |
| `class_id` | UUID | FK classes.id |
| `report_type` | VARCHAR(30) | WEEKLY, MONTHLY, FINAL |
| `period_start` | DATE | Not null |
| `period_end` | DATE | Not null |
| `listening_comment` | TEXT | Nullable |
| `speaking_comment` | TEXT | Nullable |
| `reading_comment` | TEXT | Nullable |
| `writing_comment` | TEXT | Nullable |
| `grammar_comment` | TEXT | Nullable |
| `attitude_comment` | TEXT | Nullable |
| `overall_comment` | TEXT | Nullable |
| `status` | VARCHAR(30) | DRAFT, SUBMITTED, APPROVED, DELIVERED |
| `prepared_by` | UUID | FK employees.id |
| `approved_by` | UUID | Nullable FK employees.id |
| `approved_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

#### `report_deliveries`

Stores delivery records to parents. Only Office Staff should deliver reports.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `report_id` | UUID | FK learning_reports.id |
| `parent_id` | UUID | FK parents.id |
| `delivered_by` | UUID | FK employees.id |
| `channel` | VARCHAR(30) | IN_APP, EMAIL, SMS, ZALO, MANUAL |
| `status` | VARCHAR(30) | PENDING, SENT, FAILED, READ |
| `delivered_at` | TIMESTAMPTZ | Nullable |
| `note` | TEXT | Nullable |
| common columns | | Required |

---

### 6.9 Tuition and payment

#### `invoices`

Stores tuition invoices.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `student_id` | UUID | FK students.id |
| `enrollment_id` | UUID | Nullable FK class_enrollments.id |
| `invoice_no` | VARCHAR(100) | Unique |
| `issue_date` | DATE | Not null |
| `due_date` | DATE | Not null |
| `subtotal_amount` | NUMERIC(14,2) | Not null |
| `discount_amount` | NUMERIC(14,2) | Default 0 |
| `total_amount` | NUMERIC(14,2) | Not null |
| `paid_amount` | NUMERIC(14,2) | Default 0 |
| `remaining_amount` | NUMERIC(14,2) | Not null |
| `status` | VARCHAR(30) | DRAFT, UNPAID, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED, REFUNDED |
| common columns | | Required |

#### `invoice_items`

Stores invoice lines.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `invoice_id` | UUID | FK invoices.id |
| `item_type` | VARCHAR(50) | TUITION, MATERIAL, TEST, OTHER |
| `description` | TEXT | Not null |
| `quantity` | INTEGER | Default 1 |
| `unit_price` | NUMERIC(14,2) | Not null |
| `amount` | NUMERIC(14,2) | Not null |
| common columns | | Required |

#### `payments`

Stores payments. Payments should be append-only.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `invoice_id` | UUID | FK invoices.id |
| `payment_no` | VARCHAR(100) | Unique |
| `amount` | NUMERIC(14,2) | Must be greater than 0 |
| `payment_method` | VARCHAR(50) | CASH, BANK_TRANSFER, CARD, MOMO, VNPAY, OTHER |
| `paid_at` | TIMESTAMPTZ | Not null |
| `status` | VARCHAR(30) | PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED |
| `transaction_ref` | VARCHAR(255) | Nullable |
| `collected_by` | UUID | FK employees.id |
| `note` | TEXT | Nullable |
| common columns | | Required |

#### `refunds`

Stores refund records.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `payment_id` | UUID | FK payments.id |
| `invoice_id` | UUID | FK invoices.id |
| `amount` | NUMERIC(14,2) | Must be greater than 0 |
| `reason` | TEXT | Not null |
| `status` | VARCHAR(30) | REQUESTED, APPROVED, REJECTED, REFUNDED |
| `approved_by` | UUID | Nullable FK employees.id |
| `refunded_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

#### `discounts`, `vouchers`, `student_vouchers`

Use these tables to support tuition promotions and voucher usage.

Required fields:

```text
discounts: code, name, discount_type, value, start_date, end_date, is_active
vouchers: code, name, voucher_type, value, usage_limit, used_count, start_date, end_date, is_active
student_vouchers: student_id, voucher_id, invoice_id, status, used_at
```

#### `installment_plans`, `installments`

Use these tables for installment payments.

Required fields:

```text
installment_plans: invoice_id, total_installments, total_amount, status
installments: installment_plan_id, installment_no, due_date, amount, paid_amount, status
```

---

### 6.10 Payroll and leave management

#### `salary_profiles`

Stores salary configuration per employee.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `employee_id` | UUID | FK employees.id |
| `hourly_rate` | NUMERIC(14,2) | Default 0 |
| `teaching_hour_rate` | NUMERIC(14,2) | Default 0 |
| `monthly_base_salary` | NUMERIC(14,2) | Default 0 |
| `allowance_amount` | NUMERIC(14,2) | Default 0 |
| `effective_from` | DATE | Not null |
| `effective_to` | DATE | Nullable |
| `status` | VARCHAR(30) | ACTIVE, INACTIVE |
| common columns | | Required |

#### `payroll_periods`

Stores payroll calculation periods.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `period_code` | VARCHAR(50) | Unique |
| `start_date` | DATE | Not null |
| `end_date` | DATE | Not null |
| `status` | VARCHAR(30) | OPEN, CALCULATED, APPROVED, PAID, CLOSED |
| common columns | | Required |

#### `teaching_hour_logs`

Stores teaching hours generated from completed sessions.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `employee_id` | UUID | FK employees.id |
| `session_id` | UUID | FK class_sessions.id |
| `teaching_hours` | NUMERIC(6,2) | Not null |
| `rate` | NUMERIC(14,2) | Not null |
| `amount` | NUMERIC(14,2) | Not null |
| `status` | VARCHAR(30) | DRAFT, APPROVED, PAID |
| `approved_at` | TIMESTAMPTZ | Nullable |
| `approved_by` | UUID | Nullable FK employees.id |
| common columns | | Required |

#### `payroll_items`

Stores payroll result per employee per period.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `payroll_period_id` | UUID | FK payroll_periods.id |
| `employee_id` | UUID | FK employees.id |
| `base_salary` | NUMERIC(14,2) | Default 0 |
| `teaching_amount` | NUMERIC(14,2) | Default 0 |
| `bonus_amount` | NUMERIC(14,2) | Default 0 |
| `penalty_amount` | NUMERIC(14,2) | Default 0 |
| `total_amount` | NUMERIC(14,2) | Not null |
| `status` | VARCHAR(30) | DRAFT, APPROVED, PAID |
| `paid_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

#### `leave_requests`

Stores employee leave requests.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `employee_id` | UUID | FK employees.id |
| `leave_type` | VARCHAR(50) | SICK, PERSONAL, ANNUAL, UNPAID, OTHER |
| `start_date` | DATE | Not null |
| `end_date` | DATE | Not null |
| `reason` | TEXT | Not null |
| `status` | VARCHAR(30) | REQUESTED, APPROVED, REJECTED, CANCELLED |
| `approved_by` | UUID | Nullable FK employees.id |
| `approved_at` | TIMESTAMPTZ | Nullable |
| `rejection_reason` | TEXT | Nullable |
| common columns | | Required |

---

### 6.11 Notification, contact log, files, audit, AI, settings

#### `notification_templates`

Stores reusable message templates.

Required fields:

```text
code, channel, title_template, body_template, is_active
```

#### `notifications`

Stores notification jobs and delivery status.

Required fields:

```text
template_id, title, body, target_type, target_user_id, channel, status, scheduled_at, sent_at, metadata JSONB
```

#### `contact_logs`

Stores every communication between Office Staff and Parents.

Required fields:

```text
parent_id, student_id, employee_id, channel, contact_type, content, direction, contacted_at, metadata JSONB
```

Rule:

```text
Only users with permission `contactlog:create` can create contact logs.
Teacher roles should not receive this permission.
```

#### `file_objects`

Stores file metadata. Binary files must be stored in S3/R2/MinIO, not inside PostgreSQL.

Required fields:

```text
bucket, object_key, original_filename, content_type, size_bytes, storage_provider, public_url, is_public
```

#### `audit_logs`

Stores important data changes.

Required fields:

```text
actor_user_id, action, entity_name, entity_id, old_value JSONB, new_value JSONB, ip_address, user_agent
```

#### `ai_generation_requests`

Stores AI generation request metadata.

Required fields:

```text
requested_by, feature_type, prompt_type, input_payload JSONB, status, requested_at, completed_at
```

#### `ai_generated_contents`

Stores AI output.

Required fields:

```text
request_id, content_type, generated_text, output_payload JSONB, status
```

#### `ai_student_insights`

Stores AI-generated student progress analysis.

Required fields:

```text
student_id, class_id, insight_type, summary, metrics JSONB, period_start, period_end
```

#### `system_settings`

Stores configurable system values.

Required fields:

```text
setting_key, setting_value JSONB, description, is_active
```

---

## 7. Mermaid ER Diagram

```mermaid
erDiagram

    USERS {
        UUID id PK
        VARCHAR email UK
        VARCHAR phone UK
        VARCHAR username UK
        VARCHAR password_hash
        VARCHAR full_name
        TEXT avatar_url
        VARCHAR status
        BOOLEAN email_verified
        BOOLEAN phone_verified
        BOOLEAN mfa_enabled
        TIMESTAMPTZ last_login_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    ROLES {
        UUID id PK
        VARCHAR code UK
        VARCHAR name
        TEXT description
        BOOLEAN is_system
        BOOLEAN is_active
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    PERMISSIONS {
        UUID id PK
        VARCHAR code UK
        VARCHAR module
        VARCHAR action
        TEXT description
        BOOLEAN is_active
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    ROLE_PERMISSIONS {
        UUID id PK
        UUID role_id FK
        UUID permission_id FK
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    USER_ROLES {
        UUID id PK
        UUID user_id FK
        UUID role_id FK
        UUID campus_id FK
        TIMESTAMPTZ assigned_at
        TIMESTAMPTZ expired_at
        BOOLEAN is_active
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    REFRESH_TOKENS {
        UUID id PK
        UUID user_id FK
        VARCHAR token_hash UK
        TIMESTAMPTZ expires_at
        TIMESTAMPTZ revoked_at
        UUID replaced_by_token_id FK
        TEXT device_info
        VARCHAR ip_address
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    CAMPUSES {
        UUID id PK
        VARCHAR code UK
        VARCHAR name
        TEXT address
        VARCHAR phone
        TEXT description
        BOOLEAN is_active
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    ROOMS {
        UUID id PK
        UUID campus_id FK
        VARCHAR code
        VARCHAR name
        INTEGER capacity
        VARCHAR room_type
        TEXT facilities_note
        BOOLEAN is_active
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    EMPLOYEES {
        UUID id PK
        UUID user_id FK
        VARCHAR employee_code UK
        DATE date_of_birth
        VARCHAR gender
        TEXT address
        VARCHAR emergency_contact_name
        VARCHAR emergency_contact_phone
        DATE hired_date
        DATE resigned_date
        VARCHAR working_status
        TEXT note
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    POSITIONS {
        UUID id PK
        VARCHAR code UK
        VARCHAR name
        TEXT description
        BOOLEAN is_teaching_position
        BOOLEAN is_active
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    EMPLOYEE_POSITIONS {
        UUID id PK
        UUID employee_id FK
        UUID position_id FK
        UUID campus_id FK
        DATE start_date
        DATE end_date
        BOOLEAN is_primary
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    PARENTS {
        UUID id PK
        UUID user_id FK
        VARCHAR parent_code UK
        VARCHAR full_name
        VARCHAR phone
        VARCHAR email
        TEXT address
        VARCHAR job_title
        TEXT note
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    STUDENTS {
        UUID id PK
        UUID user_id FK
        VARCHAR student_code UK
        VARCHAR full_name
        DATE date_of_birth
        VARCHAR gender
        VARCHAR school_name
        VARCHAR grade
        TEXT avatar_url
        TEXT medical_notes
        TEXT learning_notes
        VARCHAR student_type
        VARCHAR access_mode
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    STUDENT_PARENTS {
        UUID id PK
        UUID student_id FK
        UUID parent_id FK
        VARCHAR relationship
        BOOLEAN is_primary_contact
        BOOLEAN can_receive_notification
        BOOLEAN can_receive_tuition
        BOOLEAN can_pickup_student
        BOOLEAN is_emergency_contact
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    LEVELS {
        UUID id PK
        VARCHAR code UK
        VARCHAR name
        INTEGER order_index
        TEXT description
        BOOLEAN is_active
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    COURSES {
        UUID id PK
        UUID level_id FK
        VARCHAR code UK
        VARCHAR name
        TEXT description
        INTEGER total_lessons
        INTEGER duration_minutes
        NUMERIC default_tuition_fee
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    CURRICULUMS {
        UUID id PK
        UUID course_id FK
        VARCHAR name
        VARCHAR version_name
        TEXT description
        BOOLEAN is_active
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    LESSONS {
        UUID id PK
        UUID curriculum_id FK
        INTEGER lesson_no
        VARCHAR title
        TEXT objective
        TEXT content_summary
        INTEGER duration_minutes
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    CLASSES {
        UUID id PK
        UUID course_id FK
        UUID campus_id FK
        VARCHAR class_code UK
        VARCHAR name
        INTEGER capacity
        DATE start_date
        DATE expected_end_date
        VARCHAR status
        TEXT note
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    CLASS_STAFF {
        UUID id PK
        UUID class_id FK
        UUID employee_id FK
        VARCHAR staff_type
        DATE start_date
        DATE end_date
        BOOLEAN is_primary
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    CLASS_ENROLLMENTS {
        UUID id PK
        UUID class_id FK
        UUID student_id FK
        VARCHAR enrollment_code UK
        DATE enrolled_date
        DATE start_date
        DATE end_date
        VARCHAR status
        VARCHAR source
        TEXT note
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    CLASS_SCHEDULES {
        UUID id PK
        UUID class_id FK
        UUID room_id FK
        SMALLINT day_of_week
        TIME start_time
        TIME end_time
        DATE effective_from
        DATE effective_to
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    CLASS_SESSIONS {
        UUID id PK
        UUID class_id FK
        UUID schedule_id FK
        UUID room_id FK
        DATE session_date
        TIME start_time
        TIME end_time
        INTEGER lesson_no
        VARCHAR status
        TEXT note
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    STUDENT_ATTENDANCE {
        UUID id PK
        UUID session_id FK
        UUID student_id FK
        VARCHAR status
        TIME check_in_time
        TIME check_out_time
        TEXT note
        UUID marked_by FK
        TIMESTAMPTZ marked_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    EMPLOYEE_ATTENDANCE {
        UUID id PK
        UUID session_id FK
        UUID employee_id FK
        VARCHAR status
        TIME check_in_time
        TIME check_out_time
        TEXT note
        UUID marked_by FK
        TIMESTAMPTZ marked_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    HOMEWORK_ASSIGNMENTS {
        UUID id PK
        UUID class_id FK
        UUID lesson_id FK
        UUID assigned_by FK
        VARCHAR title
        TEXT description
        TIMESTAMPTZ due_at
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    HOMEWORK_SUBMISSIONS {
        UUID id PK
        UUID homework_id FK
        UUID student_id FK
        TEXT answer_text
        UUID file_id FK
        TIMESTAMPTZ submitted_at
        VARCHAR status
        TEXT teacher_feedback
        NUMERIC score
        TIMESTAMPTZ graded_at
        UUID graded_by FK
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    SCORE_COMPONENTS {
        UUID id PK
        VARCHAR code UK
        VARCHAR name
        VARCHAR skill_type
        NUMERIC default_weight
        BOOLEAN is_active
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    ASSESSMENTS {
        UUID id PK
        UUID class_id FK
        UUID score_component_id FK
        VARCHAR title
        DATE assessment_date
        NUMERIC max_score
        NUMERIC weight
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    STUDENT_SCORES {
        UUID id PK
        UUID assessment_id FK
        UUID student_id FK
        NUMERIC score
        TEXT comment
        UUID graded_by FK
        TIMESTAMPTZ graded_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    LEARNING_REPORTS {
        UUID id PK
        UUID student_id FK
        UUID class_id FK
        VARCHAR report_type
        DATE period_start
        DATE period_end
        TEXT listening_comment
        TEXT speaking_comment
        TEXT reading_comment
        TEXT writing_comment
        TEXT grammar_comment
        TEXT attitude_comment
        TEXT overall_comment
        VARCHAR status
        UUID prepared_by FK
        UUID approved_by FK
        TIMESTAMPTZ approved_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    INVOICES {
        UUID id PK
        UUID student_id FK
        UUID enrollment_id FK
        VARCHAR invoice_no UK
        DATE issue_date
        DATE due_date
        NUMERIC subtotal_amount
        NUMERIC discount_amount
        NUMERIC total_amount
        NUMERIC paid_amount
        NUMERIC remaining_amount
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    INVOICE_ITEMS {
        UUID id PK
        UUID invoice_id FK
        VARCHAR item_type
        TEXT description
        INTEGER quantity
        NUMERIC unit_price
        NUMERIC amount
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    PAYMENTS {
        UUID id PK
        UUID invoice_id FK
        VARCHAR payment_no UK
        NUMERIC amount
        VARCHAR payment_method
        TIMESTAMPTZ paid_at
        VARCHAR status
        VARCHAR transaction_ref
        UUID collected_by FK
        TEXT note
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    NOTIFICATIONS {
        UUID id PK
        UUID template_id FK
        VARCHAR title
        TEXT body
        VARCHAR target_type
        UUID target_user_id FK
        VARCHAR channel
        VARCHAR status
        TIMESTAMPTZ scheduled_at
        TIMESTAMPTZ sent_at
        JSONB metadata
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    CONTACT_LOGS {
        UUID id PK
        UUID parent_id FK
        UUID student_id FK
        UUID employee_id FK
        VARCHAR channel
        VARCHAR contact_type
        TEXT content
        VARCHAR direction
        TIMESTAMPTZ contacted_at
        JSONB metadata
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    FILE_OBJECTS {
        UUID id PK
        VARCHAR bucket
        VARCHAR object_key UK
        VARCHAR original_filename
        VARCHAR content_type
        BIGINT size_bytes
        VARCHAR storage_provider
        TEXT public_url
        BOOLEAN is_public
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        UUID created_by FK
        UUID updated_by FK
        TIMESTAMPTZ deleted_at
        INTEGER version
    }

    USERS ||--o{ REFRESH_TOKENS : owns
    USERS ||--o{ USER_ROLES : has
    ROLES ||--o{ USER_ROLES : assigned
    ROLES ||--o{ ROLE_PERMISSIONS : includes
    PERMISSIONS ||--o{ ROLE_PERMISSIONS : granted
    CAMPUSES ||--o{ USER_ROLES : scopes

    USERS ||--o| EMPLOYEES : optional_profile
    USERS ||--o| PARENTS : optional_profile
    USERS ||--o| STUDENTS : optional_profile

    CAMPUSES ||--o{ ROOMS : contains
    CAMPUSES ||--o{ CLASSES : hosts

    EMPLOYEES ||--o{ EMPLOYEE_POSITIONS : has
    POSITIONS ||--o{ EMPLOYEE_POSITIONS : assigned
    CAMPUSES ||--o{ EMPLOYEE_POSITIONS : scoped_to

    STUDENTS ||--o{ STUDENT_PARENTS : has
    PARENTS ||--o{ STUDENT_PARENTS : has

    LEVELS ||--o{ COURSES : contains
    COURSES ||--o{ CURRICULUMS : has
    CURRICULUMS ||--o{ LESSONS : contains
    COURSES ||--o{ CLASSES : opened_as

    CLASSES ||--o{ CLASS_STAFF : staffed_by
    EMPLOYEES ||--o{ CLASS_STAFF : assigned_to
    CLASSES ||--o{ CLASS_ENROLLMENTS : has
    STUDENTS ||--o{ CLASS_ENROLLMENTS : enrolls

    CLASSES ||--o{ CLASS_SCHEDULES : has
    ROOMS ||--o{ CLASS_SCHEDULES : used_by
    CLASS_SCHEDULES ||--o{ CLASS_SESSIONS : generates
    CLASSES ||--o{ CLASS_SESSIONS : has
    ROOMS ||--o{ CLASS_SESSIONS : held_in

    CLASS_SESSIONS ||--o{ STUDENT_ATTENDANCE : records
    STUDENTS ||--o{ STUDENT_ATTENDANCE : attends
    CLASS_SESSIONS ||--o{ EMPLOYEE_ATTENDANCE : records
    EMPLOYEES ||--o{ EMPLOYEE_ATTENDANCE : attends

    CLASSES ||--o{ HOMEWORK_ASSIGNMENTS : receives
    LESSONS ||--o{ HOMEWORK_ASSIGNMENTS : based_on
    HOMEWORK_ASSIGNMENTS ||--o{ HOMEWORK_SUBMISSIONS : receives
    STUDENTS ||--o{ HOMEWORK_SUBMISSIONS : submits
    FILE_OBJECTS ||--o{ HOMEWORK_SUBMISSIONS : attached_to

    SCORE_COMPONENTS ||--o{ ASSESSMENTS : categorizes
    CLASSES ||--o{ ASSESSMENTS : has
    ASSESSMENTS ||--o{ STUDENT_SCORES : contains
    STUDENTS ||--o{ STUDENT_SCORES : receives

    STUDENTS ||--o{ LEARNING_REPORTS : receives
    CLASSES ||--o{ LEARNING_REPORTS : has

    STUDENTS ||--o{ INVOICES : billed
    CLASS_ENROLLMENTS ||--o{ INVOICES : generates
    INVOICES ||--o{ INVOICE_ITEMS : contains
    INVOICES ||--o{ PAYMENTS : paid_by

    USERS ||--o{ NOTIFICATIONS : receives
    PARENTS ||--o{ CONTACT_LOGS : contacted
    STUDENTS ||--o{ CONTACT_LOGS : about
    EMPLOYEES ||--o{ CONTACT_LOGS : handled_by
```

---

## 8. Required Indexes

Create indexes early for tables that will grow quickly.

```sql
-- Soft delete filters
CREATE INDEX idx_users_deleted_at ON users(deleted_at);
CREATE INDEX idx_students_deleted_at ON students(deleted_at);
CREATE INDEX idx_parents_deleted_at ON parents(deleted_at);
CREATE INDEX idx_employees_deleted_at ON employees(deleted_at);
CREATE INDEX idx_classes_deleted_at ON classes(deleted_at);

-- Search and lookup
CREATE INDEX idx_students_full_name ON students(full_name);
CREATE INDEX idx_students_status_type ON students(status, student_type);
CREATE INDEX idx_parents_phone ON parents(phone);
CREATE INDEX idx_employees_code_status ON employees(employee_code, working_status);

-- Class and schedule
CREATE INDEX idx_classes_course_status ON classes(course_id, status);
CREATE INDEX idx_classes_campus_status ON classes(campus_id, status);
CREATE INDEX idx_class_sessions_date ON class_sessions(session_date);
CREATE INDEX idx_class_sessions_class_date ON class_sessions(class_id, session_date);
CREATE INDEX idx_class_schedules_room_time ON class_schedules(room_id, day_of_week, start_time, end_time);

-- Attendance
CREATE INDEX idx_student_attendance_student ON student_attendance(student_id);
CREATE INDEX idx_student_attendance_session_status ON student_attendance(session_id, status);
CREATE INDEX idx_employee_attendance_employee ON employee_attendance(employee_id);

-- Finance
CREATE INDEX idx_invoices_student_status ON invoices(student_id, status);
CREATE INDEX idx_invoices_due_date_status ON invoices(due_date, status);
CREATE INDEX idx_payments_invoice_paid_at ON payments(invoice_id, paid_at);

-- Communication
CREATE INDEX idx_contact_logs_parent_date ON contact_logs(parent_id, contacted_at);
CREATE INDEX idx_contact_logs_student_date ON contact_logs(student_id, contacted_at);
CREATE INDEX idx_notifications_target_status ON notifications(target_user_id, status);
CREATE INDEX idx_notifications_scheduled_at ON notifications(scheduled_at);
```

---

## 9. API Standard

### 9.1 Base URL

```text
/api/v1
```

### 9.2 Success response

```json
{
  "success": true,
  "message": "Success",
  "data": {},
  "meta": {
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

### 9.3 Error response

```json
{
  "success": false,
  "errorCode": "STUDENT_NOT_FOUND",
  "message": "Student not found",
  "details": [
    {
      "field": "studentId",
      "message": "Student does not exist"
    }
  ],
  "timestamp": "2026-07-04T10:00:00Z",
  "path": "/api/v1/students/123"
}
```

### 9.4 Pagination, filtering, sorting

Use consistent query parameters:

```text
?page=0&size=20&search=anna&status=ACTIVE&sort=createdAt,desc
```

### 9.5 Core REST endpoints

```text
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh-token
POST   /api/v1/auth/logout
POST   /api/v1/auth/forgot-password
POST   /api/v1/auth/change-password

GET    /api/v1/users
GET    /api/v1/users/{id}
POST   /api/v1/users
PUT    /api/v1/users/{id}
DELETE /api/v1/users/{id}

GET    /api/v1/roles
POST   /api/v1/roles
PUT    /api/v1/roles/{id}
POST   /api/v1/roles/{id}/permissions

GET    /api/v1/permissions

GET    /api/v1/employees
GET    /api/v1/employees/{id}
POST   /api/v1/employees
PUT    /api/v1/employees/{id}
DELETE /api/v1/employees/{id}
POST   /api/v1/employees/{id}/positions

GET    /api/v1/students
GET    /api/v1/students/{id}
POST   /api/v1/students
PUT    /api/v1/students/{id}
DELETE /api/v1/students/{id}
POST   /api/v1/students/{id}/parents
POST   /api/v1/students/{id}/create-login-account

GET    /api/v1/parents
GET    /api/v1/parents/{id}
POST   /api/v1/parents
PUT    /api/v1/parents/{id}
POST   /api/v1/parents/{id}/create-login-account

GET    /api/v1/campuses
POST   /api/v1/campuses
PUT    /api/v1/campuses/{id}
DELETE /api/v1/campuses/{id}

GET    /api/v1/rooms
POST   /api/v1/rooms
PUT    /api/v1/rooms/{id}
DELETE /api/v1/rooms/{id}

GET    /api/v1/courses
POST   /api/v1/courses
PUT    /api/v1/courses/{id}
DELETE /api/v1/courses/{id}

GET    /api/v1/classes
GET    /api/v1/classes/{id}
POST   /api/v1/classes
PUT    /api/v1/classes/{id}
POST   /api/v1/classes/{id}/staff
POST   /api/v1/classes/{id}/enrollments
POST   /api/v1/classes/{id}/schedules
POST   /api/v1/classes/{id}/generate-sessions

GET    /api/v1/attendance/sessions/{sessionId}/students
POST   /api/v1/attendance/sessions/{sessionId}/students
POST   /api/v1/attendance/sessions/{sessionId}/employees

GET    /api/v1/homework
POST   /api/v1/homework
POST   /api/v1/homework/{id}/publish
POST   /api/v1/homework/{id}/submissions
PUT    /api/v1/homework-submissions/{id}/grade

GET    /api/v1/assessments
POST   /api/v1/assessments
POST   /api/v1/assessments/{id}/scores

GET    /api/v1/learning-reports
POST   /api/v1/learning-reports
POST   /api/v1/learning-reports/{id}/submit
POST   /api/v1/learning-reports/{id}/approve
POST   /api/v1/learning-reports/{id}/deliver

GET    /api/v1/invoices
POST   /api/v1/invoices
POST   /api/v1/invoices/{id}/payments
POST   /api/v1/payments/{id}/refunds

GET    /api/v1/contact-logs
POST   /api/v1/contact-logs
```

---

## 10. Security Requirements

### 10.1 Authentication

- Use JWT access token with short expiry.
- Use refresh token rotation.
- Store refresh token hash, never raw token.
- Revoke old refresh token after rotation.
- Detect refresh token reuse and revoke the token family.

### 10.2 Password security

- Use BCrypt or Argon2.
- Never log passwords or tokens.
- Enforce minimum password policy.
- Add rate limiting for login and forgot password.

### 10.3 Authorization

- Use permission-based authorization.
- Use annotations such as `@RequirePermission("student:create")`.
- Campus-scoped permissions must filter data by campus where applicable.

### 10.4 Input and output security

- Validate all DTOs with Bean Validation.
- Never expose entity objects directly.
- Use DTO responses only.
- Prevent mass assignment by mapping request DTOs manually or with MapStruct.
- Sanitize rich text fields if they can contain HTML.

### 10.5 File upload security

- Store files in S3/R2/MinIO.
- Validate file type and size.
- Generate random object keys.
- Do not trust original file names.
- Use signed URLs for private files.

---

## 11. Performance Requirements

### 11.1 Database

- Always paginate list APIs.
- Never return unbounded lists.
- Avoid N+1 queries.
- Use indexes for common filters.
- Use projections for read-heavy list screens.
- Use batch inserts for attendance and scores.
- Use read-only transactions for query methods.

### 11.2 Redis cache

Use Redis for:

- Permission cache
- User session metadata
- Rate limiting
- Dashboard summary cache
- Frequently used master data

Do not cache financial writes or sensitive token raw values.

### 11.3 Background jobs

Use Quartz for scheduled jobs:

```text
Daily attendance summary
Tuition reminder 7 days before due
Tuition reminder 3 days before due
Tuition reminder 1 day before due
Overdue tuition reminder
Student absent 3 consecutive sessions alert
Monthly payroll calculation
Learning report reminder
Database backup trigger
Notification retry
```

Use RabbitMQ for asynchronous processing:

```text
notification.requested
invoice.created
payment.completed
student.absent.detected
learning_report.approved
ai.report.requested
payroll.calculation.requested
```

---

## 12. Transaction Rules

Use `@Transactional` for write use cases.

Examples:

### 12.1 Create student with parent

```text
Create student
Create or update parent
Link student_parent
Create audit log
Commit transaction
```

### 12.2 Collect payment

```text
Lock invoice with optimistic version
Create payment
Recalculate paid_amount and remaining_amount
Update invoice status
Create audit log
Publish payment.completed event after commit
```

### 12.3 Mark attendance

```text
Validate session exists
Validate student belongs to class
Upsert attendance record
Detect 3 consecutive absences
Publish student.absent.detected event if needed
```

---

## 13. Testing Standard

Required test layers:

```text
Unit test: service business logic
Repository test: JPA query and specification
Controller test: request validation and status codes
Integration test: full workflow with Testcontainers PostgreSQL
Security test: permission denied and allowed cases
```

Minimum coverage targets:

```text
Business services: 80%+
Security-sensitive logic: 90%+
Financial logic: 90%+
```

---

## 14. Development Roadmap

### Phase 1: Foundation

```text
Project setup
Docker Compose
PostgreSQL
Flyway
BaseEntity
Global exception handler
API response standard
Security skeleton
OpenAPI
```

### Phase 2: Auth and RBAC

```text
Login
Refresh token rotation
Logout
Users
Roles
Permissions
Role permissions
User roles
Permission guard
```

### Phase 3: Master data

```text
Campus
Room
Level
Course
Curriculum
Lesson
Vocabulary
Grammar
```

### Phase 4: People management

```text
Employees
Positions
Employee positions
Parents
Students
Student-parent links
Optional login account creation for parent/student
```

### Phase 5: Academic core

```text
Classes
Class staff
Enrollments
Transfers
Freezes
Schedules
Session generation
Conflict detection
```

### Phase 6: Operations

```text
Attendance
Homework
Scores
Learning reports
Report delivery
Contact logs
```

### Phase 7: Finance and HR

```text
Invoices
Payments
Refunds
Installments
Discounts
Vouchers
Salary profiles
Teaching hour logs
Payroll
Leave requests
```

### Phase 8: Automation and AI

```text
Notifications
Tuition reminders
Absence alerts
AI learning reports
AI homework generation
AI lesson plans
AI progress analysis
```

---

## 15. Definition of Done

A backend task is done only when:

- Database migration is created and tested.
- Entity, repository, service, mapper, DTO, controller are implemented.
- Validation rules exist.
- Permission checks exist.
- Unit tests are written.
- Integration tests are written for important workflows.
- API is documented in OpenAPI.
- Errors use standard error response.
- No entity is returned directly to frontend.
- List endpoints are paginated.
- Audit log is created for sensitive changes.
- Soft delete and optimistic locking are respected where applicable.

