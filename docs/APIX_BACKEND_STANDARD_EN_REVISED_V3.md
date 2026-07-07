<!-- Before doing this task, read and follow this backend standard file.

Project context:
- Spring Boot 3.5.x
- Java 21
- Maven
- PostgreSQL
- Flyway
- Spring Data JPA
- Spring Security
- Lombok
- Jakarta Validation
- Redis
- Quartz Scheduler
- RabbitMQ when async workload becomes real

Base package:
com.apixenglish.center

Rules:
- Do not delete existing code unless the task explicitly says a deprecated table/module must be removed.
- Do not rewrite unrelated files.
- Only add or modify files required for the task.
- Follow the existing package structure.
- Use English for code names, database names, routes, DTOs, and modules.
- Do not hardcode role names inside business logic.
- Always check permissions by permission code.
- Enforce all permissions on backend even if frontend hides actions.
- Explain which files are created or changed.

Task:
[PASTE TASK HERE] -->

# APIX English Center Management System — Backend Engineering Standard

> Version: 3.0  
> Language: English  
> Product: APIX English Center Management System  
> Architecture goal: practical ERP for one private English center, with strong auditability, permission safety, and workflows that match real center operations.

---

## 0. Executive Domain Update

This version updates the backend domain to match the real operating logic of the center.

Major corrections from the previous design:

1. Replace student-submitted homework with weekly class updates for parents.
2. Simplify score management: define a score item, enter the numeric score, and optionally add a note.
3. Make payroll flexible for hourly and monthly employees.
4. Connect approved leave requests to payroll deductions.
5. Add fixed schedule pattern support for MWF, TTS, and Weekend classes.
6. Allow Office Staff to pre-mark or mark attendance for a full class.
7. Add attendance deadline reminders: teacher must complete attendance within 30 minutes after class starts.
8. Add Office Staff review workflow for attendance, weekly updates, and learning reports.
9. Make tuition flexible with monthly fees, extra fees such as books, refund policies, and attendance-based credits.
10. Replace fixed skill-based learning reports with flexible two-month narrative reports.
11. Add video/media delivery metadata while storing large video files in object storage.
12. Make audit logging mandatory for all create, update, delete, approval, cancellation, payroll, finance, and delivery actions.
13. Keep backend and frontend synchronized through explicit API contracts and permission codes.

---

## 1. Non-Negotiable Architecture Rules

### 1.1 Modular monolith first

Use a modular monolith. Do not split into microservices at this stage.

Recommended package shape:

```text
src/main/java/com/apixenglish/center
├── common
├── config
├── security
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
│   ├── weeklyupdate
│   ├── score
│   ├── learningreport
│   ├── tuition
│   ├── payroll
│   ├── leave
│   ├── notification
│   ├── contactlog
│   ├── filestorage
│   ├── media
│   ├── dashboard
│   ├── audit
│   ├── ai
│   └── settings
└── jobs
    ├── schedule
    ├── attendance
    ├── tuition
    ├── payroll
    ├── report
    ├── notification
    └── backup
```

Each module must follow:

```text
modules/<module>
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

### 1.2 Database standard

Use PostgreSQL as the source of truth.

Every main business table must include:

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | Primary key |
| `created_at` | TIMESTAMPTZ | Not null |
| `updated_at` | TIMESTAMPTZ | Not null |
| `created_by` | UUID | Nullable FK to `users.id` |
| `updated_by` | UUID | Nullable FK to `users.id` |
| `deleted_at` | TIMESTAMPTZ | Nullable soft delete marker |
| `version` | INTEGER | Not null optimistic lock |

Never physically delete important business records. Use soft delete and audit logs.

### 1.3 Permission rule

Never do this:

```java
if (role.equals("TEACHER")) { ... }
```

Always do this:

```java
permissionChecker.require(userId, "attendance:mark");
```

Role names can exist as seed data, but business logic must depend on permission codes.

---

## 2. Roles and Responsibility Model

Roles are dynamic records in `roles`, but the first production seed should include these operational role templates.

### 2.1 Super Admin

Purpose: system owner and technical administrator.

Default permission scope:

```text
*
```

Can manage users, roles, permissions, settings, audit logs, all campuses, finance, payroll, all reports, all corrections.

### 2.2 Center Manager / Owner

Purpose: business owner/manager.

Can see almost everything and approve high-impact workflows, but should not be the default person for small daily approval tasks.

Can:

- View all dashboards.
- Manage employees, classes, students, parents, schedules, tuition, payroll.
- Approve payroll.
- Approve leave requests.
- Override Office Staff decisions if needed.
- View audit logs.

Should not be assigned as the default approver for teacher weekly updates or routine report deliveries unless configured.

### 2.3 Office Staff

Purpose: daily operation team.

Can:

- Manage student and parent profiles.
- Manage enrollments and class transfers.
- See parent contact information.
- Create contact logs.
- Mark student attendance, including full-class quick marking.
- Pre-mark attendance when a parent calls before class.
- Review attendance after teacher submits.
- Approve and publish weekly class updates.
- Send score notifications, weekly updates, learning reports, tuition notices.
- Follow up absent students.
- Manage invoice creation and payments if granted finance permissions.

Cannot:

- Approve payroll unless explicitly granted.
- Change role permissions unless explicitly granted.

### 2.4 HR / Accountant

Purpose: payroll, employee records, finance support.

Can:

- Manage salary profiles.
- Manage allowance types.
- Run payroll calculation.
- Review payroll lines.
- Mark payroll as paid if granted.
- Manage employee leave records if granted.
- View finance reports if granted.

### 2.5 Academic Manager

Purpose: academic quality and curriculum.

Can:

- Manage courses, levels, curriculums, lessons.
- View all class academic information.
- Review learning reports if granted.
- View teacher performance metrics.

### 2.6 Teacher

Purpose: teach assigned classes.

Can only access classes assigned through `class_staff`.

Can:

- View own classes.
- View class student list.
- View student academic profile and learning notes.
- Mark attendance for own sessions.
- Enter scores for own classes.
- Create weekly class update drafts and submit them to Office Staff.
- Create two-month learning report drafts.
- Add handover notes about student learning behavior.
- Upload class learning images if granted.

Cannot:

- View parent private contact information.
- Contact parents directly through the system.
- See other teachers' classes unless granted.
- Approve own weekly updates or reports.
- Manage tuition or payroll.

### 2.7 Teaching Assistant

Can support assigned classes. Default permissions should be smaller than Teacher.

Can:

- View assigned classes.
- Assist attendance if granted.
- Add internal notes if granted.

### 2.8 Parent

Can:

- View own children only.
- View weekly updates, scores, attendance summaries, tuition invoices, learning reports, timetable, media delivered to their children.
- Receive notifications.

### 2.9 Student

Only students with `access_mode = OWN_ACCOUNT` can log in.

Can:

- View own class timetable.
- View own weekly updates, scores, reports, and media.

Young students with `NO_ACCOUNT` or `PARENT_MANAGED` must not have student portal access.

---

## 3. Permission Code Matrix

Use these permission codes as the backend/frontend contract.

| Module | Permission examples |
|---|---|
| Dashboard | `dashboard:owner-read`, `dashboard:office-read`, `dashboard:teacher-read`, `dashboard:parent-read`, `dashboard:student-read`, `dashboard:employee-read` |
| Users/RBAC | `user:read`, `user:create`, `user:update`, `role:read`, `role:manage`, `permission:read`, `permission:assign` |
| Employee | `employee:read`, `employee:create`, `employee:update`, `employee:delete`, `employee:salary-read` |
| Student | `student:read`, `student:create`, `student:update`, `student:delete`, `student:academic-note-create` |
| Parent | `parent:read`, `parent:create`, `parent:update`, `parent:delete`, `parent:contact-read` |
| Class | `class:read`, `class:create`, `class:update`, `class:assign-staff`, `class:read-assigned` |
| Schedule | `schedule:read`, `schedule:create`, `schedule:update`, `schedule:generate-session`, `schedule:resolve-conflict` |
| Attendance | `attendance:read`, `attendance:mark`, `attendance:mark-office`, `attendance:submit`, `attendance:review`, `attendance:override` |
| Weekly Update | `weekly-update:read`, `weekly-update:create`, `weekly-update:submit`, `weekly-update:approve`, `weekly-update:publish`, `weekly-update:reject` |
| Score | `score:read`, `score:create`, `score:update`, `score:publish`, `score:notify` |
| Learning Report | `learning-report:read`, `learning-report:create`, `learning-report:submit`, `learning-report:approve`, `learning-report:publish`, `learning-report:remind` |
| Tuition | `tuition:read`, `invoice:create`, `invoice:update`, `payment:create`, `refund:create`, `refund:approve`, `tuition-adjustment:create` |
| Payroll | `payroll:read`, `salary-profile:manage`, `payroll:calculate`, `payroll:approve`, `payroll:mark-paid` |
| Leave | `leave:request`, `leave:read`, `leave:approve`, `leave:cancel`, `leave:cancel-approve` |
| Notification | `notification:read`, `notification:create`, `notification:send`, `notification-template:manage` |
| Contact Log | `contactlog:read`, `contactlog:create` |
| Video Delivery | `video-delivery:read`, `video-delivery:create-batch`, `video-delivery:update-batch`, `video-delivery:cancel-batch`, `video-delivery:prepare-message`, `video-delivery:mark-copied`, `video-delivery:open-zalo`, `video-delivery:mark-sent`, `video-delivery:mark-failed`, `video-delivery:mark-skipped`, `video-delivery:reopen`, `video-delivery:assign` |
| Media | `media:read`, `media:upload`, `media:publish`, `media:deliver` |
| Audit | `audit:read`, `audit:export` |
| Settings | `settings:read`, `settings:update` |

---

## 4. Core Data Model to Keep

Keep these foundation modules from the existing standard, with normal audit/common columns:

- `users`, `refresh_tokens`
- `roles`, `permissions`, `role_permissions`, `user_roles`
- `employees`, `positions`, `employee_positions`, `employee_contracts`, `employee_certificates`
- `parents`, `students`, `student_parents`
- `campuses`, `rooms`, `room_facilities`
- `levels`, `courses`, `curriculums`, `lessons`, `vocabulary_items`, `grammar_items`
- `classes`, `class_staff`, `class_enrollments`, `enrollment_transfers`, `enrollment_freezes`

Important student/account rules:

```text
A student profile can exist without a login account.
A parent profile can exist without a login account.
A teacher must normally have an employee profile and login account.
Teachers can view student academic information but must not see parent private contact information by default.
```

---

## 5. Schedule Design for APIX Fixed Class Time Rules

The center has three fixed schedule patterns:

| Pattern | Days | Slots |
|---|---|---|
| `MWF` | Monday, Wednesday, Friday | Slot 1: 18:00-19:30, Slot 2: 19:30-21:00 |
| `TTS` | Tuesday, Thursday, Saturday | Slot 1: 18:00-19:30, Slot 2: 19:30-21:00 |
| `WEEKEND` | Saturday, Sunday | Morning: 09:00-11:00, Afternoon: 15:00-17:00 |

Weekend rule:

```text
If a class uses WEEKEND_MORNING, both Saturday and Sunday must be morning.
If a class uses WEEKEND_AFTERNOON, both Saturday and Sunday must be afternoon.
Do not allow Saturday afternoon + Sunday morning for the same weekend class plan.
```

### 5.1 `schedule_patterns`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `code` | VARCHAR(50) | Unique: MWF, TTS, WEEKEND |
| `name` | VARCHAR(150) | Not null |
| `description` | TEXT | Nullable |
| `is_active` | BOOLEAN | Default true |
| common columns | | Required |

### 5.2 `schedule_pattern_days`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `schedule_pattern_id` | UUID | FK schedule_patterns.id |
| `day_of_week` | SMALLINT | 1=Monday, 7=Sunday |
| common columns | | Required |

Unique:

```sql
UNIQUE (schedule_pattern_id, day_of_week)
CHECK (day_of_week BETWEEN 1 AND 7)
```

### 5.3 `time_slots`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `code` | VARCHAR(50) | Unique |
| `name` | VARCHAR(150) | Not null |
| `start_time` | TIME | Not null |
| `end_time` | TIME | Not null |
| `slot_group` | VARCHAR(50) | EVENING, WEEKEND_MORNING, WEEKEND_AFTERNOON |
| `is_active` | BOOLEAN | Default true |
| common columns | | Required |

Constraint:

```sql
CHECK (start_time < end_time)
```

Seed examples:

```text
EVENING_SLOT_1: 18:00-19:30
EVENING_SLOT_2: 19:30-21:00
WEEKEND_MORNING: 09:00-11:00
WEEKEND_AFTERNOON: 15:00-17:00
```

### 5.4 `class_schedule_plans`

Stores the schedule plan selected for a class.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `class_id` | UUID | FK classes.id |
| `schedule_pattern_id` | UUID | FK schedule_patterns.id |
| `time_slot_id` | UUID | FK time_slots.id |
| `room_id` | UUID | FK rooms.id |
| `effective_from` | DATE | Not null |
| `effective_to` | DATE | Nullable |
| `status` | VARCHAR(30) | ACTIVE, INACTIVE |
| common columns | | Required |

Business rules:

- A class can have one active schedule plan at a time.
- Creating a schedule plan generates `class_schedules` rows based on pattern days.
- Backend must reject teacher, assistant, and room conflicts.
- `class_schedules` remains the normalized weekly schedule table used by session generation.

---

## 6. Class Visibility and Student Handover Notes

### 6.1 Class assignment rule

Teachers see only classes where they are assigned in `class_staff` and the assignment is active.

Backend list APIs must enforce this using permissions:

```text
class:read          -> can read all classes within granted scope
class:read-assigned -> can read assigned classes only
```

Do not rely on frontend filters for this rule.

### 6.2 `student_learning_notes`

Stores teacher notes used for handover when a student transfers or moves to a new class.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `student_id` | UUID | FK students.id |
| `class_id` | UUID | Nullable FK classes.id |
| `enrollment_id` | UUID | Nullable FK class_enrollments.id |
| `note_type` | VARCHAR(50) | GENERAL, BEHAVIOR, STRENGTH, WEAKNESS, HANDOVER, WARNING |
| `content` | TEXT | Not null |
| `visibility` | VARCHAR(50) | INTERNAL_ACADEMIC, CLASS_STAFF_ONLY |
| `created_by_employee_id` | UUID | FK employees.id |
| common columns | | Required |

Rules:

- Teacher can create notes for assigned students.
- New assigned teacher can view previous academic handover notes.
- Parent and student accounts cannot view internal teacher notes.
- Notes are audit logged.

---

## 7. Attendance Workflow

Attendance must support both teacher and Office Staff workflows.

### 7.1 Business rules

1. Attendance due time = `class_sessions.start_time + 30 minutes`.
2. Teacher receives reminder if attendance is not submitted by due time.
3. Office Staff dashboard must show classes with attendance status:
   - Not started
   - In progress
   - Submitted by teacher
   - Reviewed by office
   - Overdue
4. Office Staff can pre-mark an individual student as absent/excused before the teacher marks attendance.
5. Office Staff can bulk mark an entire class.
6. If Office Staff pre-marks a record and locks it, teacher can see it but cannot edit it unless they have `attendance:override`.
7. Attendance can affect tuition refund/credit only if policy allows and the attendance record is eligible.

### 7.2 `class_session_attendance_status`

Stores session-level attendance progress.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `session_id` | UUID | Unique FK class_sessions.id |
| `status` | VARCHAR(30) | NOT_STARTED, IN_PROGRESS, SUBMITTED, REVIEWED, OVERDUE |
| `due_at` | TIMESTAMPTZ | session start + configured minutes |
| `submitted_by` | UUID | Nullable FK employees.id |
| `submitted_at` | TIMESTAMPTZ | Nullable |
| `reviewed_by` | UUID | Nullable FK employees.id |
| `reviewed_at` | TIMESTAMPTZ | Nullable |
| `overdue_notified_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

### 7.3 `student_attendance`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `session_id` | UUID | FK class_sessions.id |
| `student_id` | UUID | FK students.id |
| `status` | VARCHAR(30) | PRESENT, LATE, ABSENT, EXCUSED |
| `absence_reason_type` | VARCHAR(50) | NONE, PARENT_NOTICE, SICK, FAMILY_REASON, UNKNOWN, OTHER |
| `check_in_time` | TIME | Nullable |
| `check_out_time` | TIME | Nullable |
| `note` | TEXT | Nullable |
| `marked_by` | UUID | FK employees.id |
| `marked_at` | TIMESTAMPTZ | Not null |
| `source` | VARCHAR(30) | TEACHER, OFFICE_STAFF, SYSTEM |
| `is_locked` | BOOLEAN | Default false |
| `locked_by` | UUID | Nullable FK employees.id |
| `locked_at` | TIMESTAMPTZ | Nullable |
| `contact_log_id` | UUID | Nullable FK contact_logs.id |
| `tuition_credit_eligible` | BOOLEAN | Default false |
| common columns | | Required |

Unique:

```sql
UNIQUE (session_id, student_id)
```

### 7.4 `attendance_follow_ups`

Stores office follow-up status for absent students.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `attendance_id` | UUID | FK student_attendance.id |
| `student_id` | UUID | FK students.id |
| `parent_id` | UUID | Nullable FK parents.id |
| `assigned_to_employee_id` | UUID | Nullable FK employees.id |
| `follow_up_status` | VARCHAR(30) | TODO, CONTACTED, NO_ANSWER, RESOLVED, CANCELLED |
| `reason_summary` | TEXT | Nullable |
| `contact_log_id` | UUID | Nullable FK contact_logs.id |
| common columns | | Required |

---

## 8. Weekly Class Update Workflow

This replaces the old homework submission model.

Students do not need to submit assignments in the system. The teacher writes what the class learned each week and what homework was assigned. Office Staff approves and delivers it to parents through web notifications now and Zalo later.

### 8.1 Tables to remove or deprecate

Deprecated for this center workflow:

```text
homework_assignments
homework_submissions
```

If these tables already exist, do not physically drop them in production without a migration plan. Mark the module deprecated and introduce the new weekly update module.

### 8.2 `weekly_class_updates`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `class_id` | UUID | FK classes.id |
| `week_start_date` | DATE | Monday or configured week start |
| `week_end_date` | DATE | Sunday or configured week end |
| `title` | VARCHAR(255) | Not null |
| `overall_summary` | TEXT | Nullable |
| `status` | VARCHAR(30) | DRAFT, SUBMITTED, APPROVED, REJECTED, PUBLISHED |
| `submitted_by` | UUID | Nullable FK employees.id |
| `submitted_at` | TIMESTAMPTZ | Nullable |
| `approved_by` | UUID | Nullable FK employees.id |
| `approved_at` | TIMESTAMPTZ | Nullable |
| `rejection_reason` | TEXT | Nullable |
| `published_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

Unique active draft/published rule:

```sql
UNIQUE (class_id, week_start_date, week_end_date) WHERE deleted_at IS NULL
```

### 8.3 `weekly_update_session_items`

Stores the content for each lesson/session inside the weekly update.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `weekly_update_id` | UUID | FK weekly_class_updates.id |
| `session_id` | UUID | Nullable FK class_sessions.id |
| `session_date` | DATE | Not null |
| `lesson_no` | INTEGER | Nullable |
| `learning_content` | TEXT | Not null |
| `homework_content` | TEXT | Nullable |
| `note` | TEXT | Nullable |
| common columns | | Required |

### 8.4 `weekly_update_files`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `weekly_update_id` | UUID | FK weekly_class_updates.id |
| `file_id` | UUID | FK file_objects.id |
| `caption` | TEXT | Nullable |
| `sort_order` | INTEGER | Default 0 |
| common columns | | Required |

### 8.5 `weekly_update_deliveries`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `weekly_update_id` | UUID | FK weekly_class_updates.id |
| `parent_id` | UUID | Nullable FK parents.id |
| `student_id` | UUID | Nullable FK students.id |
| `delivered_by` | UUID | FK employees.id |
| `channel` | VARCHAR(30) | IN_APP, ZALO, EMAIL, SMS, MANUAL |
| `status` | VARCHAR(30) | PENDING, SENT, FAILED, READ |
| `delivered_at` | TIMESTAMPTZ | Nullable |
| `read_at` | TIMESTAMPTZ | Nullable |
| `note` | TEXT | Nullable |
| common columns | | Required |

---

## 9. Simple Score Management

The score module should be intentionally simple.

The teacher or staff defines what the score is, enters the numeric score, and optionally writes a note.

### 9.1 Tables to remove or simplify

Deprecated for this center workflow:

```text
score_components
assessments
student_scores as complex assessment model
```

Use the following simpler model.

### 9.2 `score_items`

Defines a score entry for a class.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `class_id` | UUID | FK classes.id |
| `title` | VARCHAR(255) | Example: Unit 1 Test, Speaking Check, Final Review |
| `score_date` | DATE | Nullable |
| `max_score` | NUMERIC(6,2) | Nullable, default 10 |
| `description` | TEXT | Nullable |
| `status` | VARCHAR(30) | DRAFT, PUBLISHED, LOCKED |
| `created_by_employee_id` | UUID | FK employees.id |
| `published_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

### 9.3 `score_records`

Stores the score value per student.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `score_item_id` | UUID | FK score_items.id |
| `student_id` | UUID | FK students.id |
| `score_value` | NUMERIC(6,2) | Not null |
| `note` | TEXT | Nullable |
| `graded_by` | UUID | FK employees.id |
| `graded_at` | TIMESTAMPTZ | Not null |
| common columns | | Required |

Unique:

```sql
UNIQUE (score_item_id, student_id)
```

Rules:

- `score_value` must be between 0 and `max_score` when `max_score` is set.
- Parent and student can see only published scores.
- Teacher can edit draft scores for assigned classes.
- Locked scores require `score:update-locked` if this permission is added later.

---

## 10. Two-Month Learning Report Workflow

Learning reports should be flexible narrative reports, not fixed skill-by-skill forms.

### 10.1 `learning_report_cycles`

Stores reporting period and deadline.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `class_id` | UUID | FK classes.id |
| `report_type` | VARCHAR(30) | TWO_MONTH, FINAL, CUSTOM |
| `period_start` | DATE | Not null |
| `period_end` | DATE | Not null |
| `teacher_deadline_at` | TIMESTAMPTZ | Not null |
| `office_publish_deadline_at` | TIMESTAMPTZ | Nullable |
| `status` | VARCHAR(30) | OPEN, SUBMITTED, APPROVED, PUBLISHED, CLOSED |
| common columns | | Required |

### 10.2 `student_learning_reports`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `report_cycle_id` | UUID | FK learning_report_cycles.id |
| `student_id` | UUID | FK students.id |
| `class_id` | UUID | FK classes.id |
| `content` | TEXT | Not null |
| `internal_note` | TEXT | Nullable, staff-only |
| `status` | VARCHAR(30) | DRAFT, SUBMITTED, APPROVED, REJECTED, PUBLISHED |
| `prepared_by` | UUID | FK employees.id |
| `submitted_at` | TIMESTAMPTZ | Nullable |
| `approved_by` | UUID | Nullable FK employees.id |
| `approved_at` | TIMESTAMPTZ | Nullable |
| `rejection_reason` | TEXT | Nullable |
| `published_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

Unique:

```sql
UNIQUE (report_cycle_id, student_id)
```

### 10.3 `learning_report_deliveries`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `report_id` | UUID | FK student_learning_reports.id |
| `parent_id` | UUID | Nullable FK parents.id |
| `student_id` | UUID | Nullable FK students.id |
| `delivered_by` | UUID | FK employees.id |
| `channel` | VARCHAR(30) | IN_APP, ZALO, EMAIL, SMS, MANUAL |
| `status` | VARCHAR(30) | PENDING, SENT, FAILED, READ |
| `delivered_at` | TIMESTAMPTZ | Nullable |
| `read_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

### 10.4 Deadline reminders

Use Quartz jobs to remind teachers before and after deadlines.

Rules:

- Office Staff can manually send a reminder.
- System automatically sends overdue reminders.
- Overdue counts should be included in dashboard/teacher performance statistics.

---

## 11. Tuition, Extra Fees, Refunds, and Credits

Tuition is monthly, but invoices must support extra costs such as books.

### 11.1 `tuition_packages`

Keep the existing package concept for one-month, three-month, or configurable payment plans.

### 11.2 `invoices`

Use invoices as billing snapshots.

Required fields:

```text
student_id, class_id, enrollment_id, invoice_no,
billing_start_month, billing_end_month, number_of_months,
monthly_fee, subtotal_amount, discount_amount, adjustment_amount,
total_amount, paid_amount, remaining_amount, due_date, status
```

Status:

```text
DRAFT, UNPAID, PARTIALLY_PAID, PAID, OVERDUE, CANCELLED, REFUNDED
```

### 11.3 `invoice_items`

Supports tuition and non-tuition fees.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `invoice_id` | UUID | FK invoices.id |
| `item_type` | VARCHAR(50) | TUITION, BOOK, MATERIAL, TEST, UNIFORM, OTHER |
| `description` | TEXT | Not null |
| `quantity` | INTEGER | Default 1 |
| `unit_price` | NUMERIC(14,2) | Not null |
| `amount` | NUMERIC(14,2) | Not null |
| common columns | | Required |

### 11.4 `tuition_refund_policies`

Defines flexible refund/credit rules.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `code` | VARCHAR(100) | Unique |
| `name` | VARCHAR(150) | Not null |
| `policy_type` | VARCHAR(50) | ATTENDANCE_BASED, MANUAL, PERCENTAGE, FIXED_AMOUNT |
| `min_absent_sessions` | INTEGER | Nullable |
| `refund_type` | VARCHAR(30) | CASH_REFUND, CREDIT_NEXT_INVOICE, NONE |
| `value_type` | VARCHAR(30) | PER_SESSION, PERCENTAGE, FIXED_AMOUNT |
| `value` | NUMERIC(14,2) | Non-negative |
| `requires_approval` | BOOLEAN | Default true |
| `is_active` | BOOLEAN | Default true |
| `description` | TEXT | Nullable |
| common columns | | Required |

### 11.5 `tuition_adjustments`

Stores credits, deductions, or manual adjustments applied to invoices.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `student_id` | UUID | FK students.id |
| `class_id` | UUID | Nullable FK classes.id |
| `invoice_id` | UUID | Nullable FK invoices.id |
| `source_type` | VARCHAR(50) | ATTENDANCE_REFUND, MANUAL_CREDIT, MANUAL_DEBIT, DISCOUNT |
| `source_id` | UUID | Nullable |
| `amount` | NUMERIC(14,2) | Positive amount |
| `direction` | VARCHAR(20) | CREDIT, DEBIT |
| `reason` | TEXT | Not null |
| `status` | VARCHAR(30) | DRAFT, APPROVED, APPLIED, CANCELLED |
| `approved_by` | UUID | Nullable FK employees.id |
| `approved_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

### 11.6 `refund_requests`

Use only when money is returned or a formal refund workflow is needed.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `student_id` | UUID | FK students.id |
| `invoice_id` | UUID | FK invoices.id |
| `payment_id` | UUID | Nullable FK payments.id |
| `amount` | NUMERIC(14,2) | Greater than 0 |
| `refund_method` | VARCHAR(50) | CASH, BANK_TRANSFER, CREDIT_NEXT_INVOICE, OTHER |
| `reason` | TEXT | Not null |
| `status` | VARCHAR(30) | REQUESTED, APPROVED, REJECTED, REFUNDED, CANCELLED |
| `approved_by` | UUID | Nullable FK employees.id |
| `approved_at` | TIMESTAMPTZ | Nullable |
| `refunded_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

---

## 12. Payroll and Leave Management

The center has two main payroll modes:

1. Hourly salary.
2. Monthly salary.

### 12.1 Payroll business rules

Hourly employee:

```text
Salary = approved work hours × hourly rate + approved allowances - deductions
If the employee does not work on a day, no salary is generated for that day.
Most hourly employees have no allowance, but the system must support allowance lines.
```

Monthly employee:

```text
Monthly gross = monthly base salary + approved allowances
Daily rate = monthly gross / standard work days
Default standard work days = 26
Leave deduction = approved deductible leave days × daily rate
Net salary = monthly gross - leave deduction + other additions - other deductions
```

Leave rule:

```text
Approved leave requests automatically generate payroll deduction lines when the salary profile says the leave is deductible.
If the leave date has not arrived, employee can request cancellation.
Cancellation requires manager approval.
```

### 12.2 `salary_profiles`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `employee_id` | UUID | FK employees.id |
| `salary_type` | VARCHAR(30) | HOURLY, MONTHLY |
| `hourly_rate` | NUMERIC(14,2) | Default 0 |
| `monthly_base_salary` | NUMERIC(14,2) | Default 0 |
| `standard_work_days` | INTEGER | Default 26 |
| `deduct_approved_leave` | BOOLEAN | Default true |
| `effective_from` | DATE | Not null |
| `effective_to` | DATE | Nullable |
| `status` | VARCHAR(30) | ACTIVE, INACTIVE |
| common columns | | Required |

### 12.3 `allowance_types`

Flexible allowance definitions.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `code` | VARCHAR(100) | Unique |
| `name` | VARCHAR(150) | Not null |
| `allowance_category` | VARCHAR(50) | RESPONSIBILITY, CLASS, STUDENT_COUNT, CONSULTATION_SUCCESS, TRANSPORT, MEAL, OTHER |
| `calculation_type` | VARCHAR(50) | FIXED_AMOUNT, PER_CLASS, PER_STUDENT, PER_SUCCESS, MANUAL |
| `default_amount` | NUMERIC(14,2) | Default 0 |
| `is_taxable` | BOOLEAN | Default true |
| `is_active` | BOOLEAN | Default true |
| `description` | TEXT | Nullable |
| common columns | | Required |

### 12.4 `employee_allowances`

Configured recurring allowance or one-time allowance for an employee.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `employee_id` | UUID | FK employees.id |
| `allowance_type_id` | UUID | FK allowance_types.id |
| `amount` | NUMERIC(14,2) | Not null |
| `start_date` | DATE | Not null |
| `end_date` | DATE | Nullable |
| `status` | VARCHAR(30) | ACTIVE, INACTIVE |
| `note` | TEXT | Nullable |
| common columns | | Required |

### 12.5 `employee_work_logs`

Stores approved work hours for hourly calculation. Teaching sessions can generate work logs automatically.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `employee_id` | UUID | FK employees.id |
| `work_date` | DATE | Not null |
| `source_type` | VARCHAR(50) | CLASS_SESSION, MANUAL, IMPORT |
| `source_id` | UUID | Nullable |
| `start_time` | TIME | Nullable |
| `end_time` | TIME | Nullable |
| `work_hours` | NUMERIC(6,2) | Not null |
| `status` | VARCHAR(30) | DRAFT, APPROVED, REJECTED, PAID |
| `approved_by` | UUID | Nullable FK employees.id |
| `approved_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

### 12.6 `leave_requests`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `employee_id` | UUID | FK employees.id |
| `leave_type` | VARCHAR(50) | SICK, PERSONAL, ANNUAL, UNPAID, OTHER |
| `start_date` | DATE | Not null |
| `end_date` | DATE | Not null |
| `total_leave_days` | NUMERIC(6,2) | Not null |
| `reason` | TEXT | Not null |
| `status` | VARCHAR(30) | REQUESTED, APPROVED, REJECTED, CANCEL_REQUESTED, CANCELLED |
| `approved_by` | UUID | Nullable FK employees.id |
| `approved_at` | TIMESTAMPTZ | Nullable |
| `rejection_reason` | TEXT | Nullable |
| `cancel_requested_at` | TIMESTAMPTZ | Nullable |
| `cancel_approved_by` | UUID | Nullable FK employees.id |
| `cancel_approved_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

Rules:

- Employee can cancel only if the leave start date is in the future.
- If the leave is already approved, cancellation requires approval.
- Leave updates must invalidate payroll calculation for affected period.

### 12.7 `payroll_periods`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `period_code` | VARCHAR(50) | Unique, example 2026-07 |
| `start_date` | DATE | Not null |
| `end_date` | DATE | Not null |
| `status` | VARCHAR(30) | OPEN, CALCULATED, APPROVED, PAID, CLOSED |
| `calculated_at` | TIMESTAMPTZ | Nullable |
| `approved_by` | UUID | Nullable FK employees.id |
| `approved_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

### 12.8 `payroll_items`

One payroll result per employee per period.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `payroll_period_id` | UUID | FK payroll_periods.id |
| `employee_id` | UUID | FK employees.id |
| `salary_type` | VARCHAR(30) | HOURLY, MONTHLY |
| `base_amount` | NUMERIC(14,2) | Default 0 |
| `allowance_amount` | NUMERIC(14,2) | Default 0 |
| `work_amount` | NUMERIC(14,2) | Default 0 |
| `leave_deduction_amount` | NUMERIC(14,2) | Default 0 |
| `other_deduction_amount` | NUMERIC(14,2) | Default 0 |
| `gross_amount` | NUMERIC(14,2) | Default 0 |
| `net_amount` | NUMERIC(14,2) | Not null |
| `status` | VARCHAR(30) | DRAFT, APPROVED, PAID |
| `paid_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

Unique:

```sql
UNIQUE (payroll_period_id, employee_id)
```

### 12.9 `payroll_item_lines`

Stores calculation details for audit and explanation.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `payroll_item_id` | UUID | FK payroll_items.id |
| `line_type` | VARCHAR(50) | BASE_SALARY, HOURLY_WORK, ALLOWANCE, LEAVE_DEDUCTION, OTHER_DEDUCTION, ADJUSTMENT |
| `source_type` | VARCHAR(50) | SALARY_PROFILE, WORK_LOG, LEAVE_REQUEST, ALLOWANCE, MANUAL |
| `source_id` | UUID | Nullable |
| `description` | TEXT | Not null |
| `quantity` | NUMERIC(10,2) | Nullable |
| `unit_amount` | NUMERIC(14,2) | Nullable |
| `amount` | NUMERIC(14,2) | Not null, positive or negative depending line |
| common columns | | Required |

---

## 13. Media and Video Delivery

Video can be integrated into the system, but the application must not store heavy files in PostgreSQL or on the app server.

### 13.0 Video Delivery Tracking (MVP default)

The default video feature is **Video Delivery Tracking**, not media file storage. Teachers record videos on mobile phones and send them to the center's internal Zalo group as they do today. Office Staff uses the desktop system to see which students require a monthly, final-course, foreign-teacher, class-activity, or custom video; opens the parent's Zalo conversation; forwards the actual video directly; returns to the web; and marks the delivery as sent. The system records responsibility, recipient, student, class, period, status, timestamps, prepared message, failure/skip reason, and detailed action history.

Web is the source of operational tracking, responsibility, deadline, and log. Zalo is the fastest channel for sending the actual video to parents.

Zalo is manual in this phase. The backend does not call a Zalo OA/API and does not automatically send messages or videos. It only prepares direct-forwarding message content, produces the parent Zalo URL from the normalized phone number, and logs user-confirmed actions. The default message contains no video link; it tells the parent that the video is attached in the same Zalo conversation.

The canonical MVP model is:

- video_delivery_batches: a class/group campaign with type, month, due date, status, and aggregate progress;
- student_video_deliveries: one accountable student/parent delivery item;
- video_delivery_attempts: append-only preparation, copy, open, sent, failed, skipped, and reopened history.

Batch states are DRAFT, ACTIVE, COMPLETED, and CANCELLED. Delivery states are PENDING, PREPARED, OPENED_ZALO, SENT_MANUALLY, FAILED, SKIPPED, and CANCELLED. Channels are MANUAL_ZALO_DIRECT (default), MANUAL_ZALO_LINK, and OTHER.

Video upload, QR upload, temporary playback links, and object storage are optional future enhancements only. They are not required by the MVP, must not block or appear in the default frontend delivery path, and must never force Office Staff to upload a video before recording a direct Zalo delivery. Existing optional upload tables may remain for forward compatibility and must not be deleted blindly. Video binary data is never stored in PostgreSQL.

### 13.0A Optional QR direct-to-storage enhancement

If enabled later, the QR workflow is desktop-orchestrated and mobile-uploaded. Office Staff or a Teacher selects the immutable target, and the phone uploads directly to object storage through a short-lived token and presigned URL. This optional path must remain independent from the default direct-forwarding workflow.

The desktop selection is immutable from the mobile page. Staff must never have to transfer a phone video to a computer, video bytes must never be stored in PostgreSQL, and large video bytes must not pass through Spring Boot when presigned object-storage upload is available.

Required video types are `FINAL_COURSE_VIDEO`, `FOREIGN_TEACHER_ACTIVITY`, `MONTHLY_PERSONAL_VIDEO`, `CLASS_ACTIVITY_VIDEO`, and `CUSTOM`.

Required workflow states:

- upload session: `PENDING`, `UPLOADING`, `COMPLETED`, `EXPIRED`, `CANCELLED`, `FAILED`;
- media video: `UPLOADED`, `PROCESSING`, `READY`, `APPROVED`, `REJECTED`, `DELIVERED`, `ARCHIVED`;
- share link: `ACTIVE`, `EXPIRED`, `REVOKED`;
- Manual Zalo delivery: `PREPARED`, `COPIED`, `OPENED_ZALO`, `SENT_MANUALLY`, `FAILED`.

Security and validation rules:

- Upload and share tokens must be cryptographically random and unguessable. Store them separately and never reuse one token for both concerns.
- Upload sessions expire after a configurable 15–30 minutes (default 20). Presigned uploads expire after a configurable 5–15 minutes (default 10).
- A session is scoped to its original class, student, video type, session, and target month. The public mobile flow cannot change that target.
- The token-protected mobile endpoints may be unauthenticated, but authorize only that one upload session. Return no parent contact details or other sensitive student data.
- Accept only configured video MIME types and enforce the configured byte limit before signing and again against object metadata when completing.
- Persist the expected bucket, object key, name, MIME type, and size during presign. Completion must match them and verify that the object exists.
- Never expose storage credentials. Parent URLs contain only a separate revocable `share_token`, never `storage_key`.
- Public share resolution returns a short-lived signed read URL and increments access metadata.
- Permission codes, not role names, control operations. Assigned-class scoping additionally applies to teachers and other scoped staff.

Storage is S3-compatible: MinIO is used locally and Cloudflare R2 or AWS S3 in production. PostgreSQL stores only workflow and object metadata; object storage holds the video. Spring Boot creates short-lived signed upload/read URLs.

Configuration keys are `storage.provider`, `storage.endpoint`, `storage.region`, `storage.bucket`, `storage.access-key`, `storage.secret-key`, `storage.public-base-url`, `storage.presigned-upload-expiry-minutes`, `storage.presigned-read-expiry-minutes`, `media.video.max-file-size-mb`, `media.video.allowed-mime-types`, `media.upload-session-expiry-minutes`, `media.share-link-expiry-days`, `media.upload-page-base-url`, and `media.public-share-base-url`. Credentials must come from environment variables or a secret manager.

Manual Zalo delivery is explicitly human-operated in this phase. The backend prepares an approved message and share link; Office Staff uses **Copy message** and **Open Zalo**, sends it manually, and marks it `SENT_MANUALLY`. The system records the delivery and audit log; it does not call a Zalo sending API.

The optional persistence model is video_upload_sessions, media_videos, video_share_links, and notification_deliveries. The earlier generic media_items/media_deliveries model may remain for legacy data. None of these storage-oriented tables is a prerequisite for Video Delivery Tracking.

MVP decision:

```text
Send the actual video directly through the existing internal/parent Zalo workflow.
Store delivery responsibility, status, deadline, recipient, and attempt history in PostgreSQL.
```

If temporary links are enabled later, store video files in R2, S3, or MinIO and keep only metadata in PostgreSQL. This optional enhancement does not replace or gate direct Zalo forwarding.

### 13.1 Optional legacy/future `media_items`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `media_type` | VARCHAR(30) | IMAGE, VIDEO, DOCUMENT |
| `video_category` | VARCHAR(50) | FINAL_COURSE, FOREIGN_TEACHER_ACTIVITY, MONTHLY_PERSONAL, OTHER |
| `title` | VARCHAR(255) | Not null |
| `description` | TEXT | Nullable |
| `file_id` | UUID | FK file_objects.id |
| `thumbnail_file_id` | UUID | Nullable FK file_objects.id |
| `class_id` | UUID | Nullable FK classes.id |
| `student_id` | UUID | Nullable FK students.id |
| `uploaded_by` | UUID | FK employees.id |
| `status` | VARCHAR(30) | DRAFT, READY, PUBLISHED, ARCHIVED |
| common columns | | Required |

Rules:

- `MONTHLY_PERSONAL` video should usually have `student_id`.
- `FOREIGN_TEACHER_ACTIVITY` can be class-level.
- `FINAL_COURSE` can be class-level or student-level.

### 13.2 Optional legacy/future `media_deliveries`

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `media_item_id` | UUID | FK media_items.id |
| `parent_id` | UUID | Nullable FK parents.id |
| `student_id` | UUID | Nullable FK students.id |
| `delivered_by` | UUID | FK employees.id |
| `channel` | VARCHAR(30) | IN_APP, ZALO, MANUAL |
| `status` | VARCHAR(30) | PENDING, SENT, FAILED, READ |
| `delivered_at` | TIMESTAMPTZ | Nullable |
| `read_at` | TIMESTAMPTZ | Nullable |
| common columns | | Required |

---

## 14. Notification, Contact Log, File Storage, and Audit

### 14.1 `notification_templates`

Required fields:

```text
code, channel, title_template, body_template, is_active
```

### 14.2 `notifications`

Required fields:

```text
template_id, title, body, target_type, target_user_id, channel, status,
scheduled_at, sent_at, metadata JSONB
```

Channels:

```text
IN_APP, EMAIL, SMS, ZALO, MANUAL
```

### 14.3 `contact_logs`

Stores communication between Office Staff and parents.

Required fields:

```text
parent_id, student_id, employee_id, channel, contact_type, content,
direction, contacted_at, metadata JSONB
```

Rules:

- Teachers must not create direct parent contact logs unless explicitly granted.
- Contact logs are required when Office Staff follows up student absence.

### 14.4 `file_objects`

Binary files must be stored in S3/R2/MinIO, not in PostgreSQL.

Required fields:

```text
bucket, object_key, original_filename, content_type, size_bytes,
storage_provider, public_url, is_public, checksum, uploaded_by
```

Suggested limits:

```text
Avatar: 2MB
Weekly update image: 10MB each
Report attachment: 20MB
Video: configured separately, recommended direct-to-storage upload
```

### 14.5 `audit_logs`

Audit every meaningful create, update, delete, approval, rejection, cancellation, delivery, payment, refund, payroll calculation, and permission change.

| Column | Type | Rule |
|---|---|---|
| `id` | UUID | PK |
| `actor_user_id` | UUID | Nullable FK users.id |
| `actor_employee_id` | UUID | Nullable FK employees.id |
| `action` | VARCHAR(100) | Example: ATTENDANCE_MARKED, PAYROLL_APPROVED |
| `entity_name` | VARCHAR(150) | Not null |
| `entity_id` | UUID | Nullable |
| `old_value` | JSONB | Nullable |
| `new_value` | JSONB | Nullable |
| `reason` | TEXT | Nullable |
| `ip_address` | VARCHAR(100) | Nullable |
| `user_agent` | TEXT | Nullable |
| `request_id` | VARCHAR(100) | Nullable |
| `created_at` | TIMESTAMPTZ | Not null |

Do not soft delete audit logs.

---

## 15. Dashboard and Statistics Backend

All dashboard APIs must support date range filtering where applicable:

```text
day, week, month, year, custom range
```

### 15.1 Owner dashboard metrics

- Revenue by day/week/month/year.
- Paid invoices, unpaid invoices, overdue invoices.
- New students.
- Active students.
- Dropout/paused students.
- Attendance rate.
- Absence count.
- Classes today.
- Classes with attendance overdue.
- Weekly updates pending approval.
- Learning reports overdue.
- Payroll total by period.
- Top classes by student count.
- Teacher workload.

Charts:

```text
Revenue line chart
Invoice status donut
Attendance trend line chart
Student growth bar chart
Class occupancy bar chart
Teacher workload bar chart
Report overdue trend
```

### 15.2 Office Staff dashboard metrics

- Today's classes.
- Attendance not submitted.
- Attendance submitted but not reviewed.
- Absent students needing follow-up.
- Weekly updates pending approval.
- Learning reports pending publish.
- Tuition reminders due today.
- Parent contact follow-ups.

### 15.3 Teacher dashboard metrics

- Assigned classes today.
- Attendance tasks due now.
- Weekly updates to submit.
- Learning reports due.
- Scores draft/not published.
- Student notes recently added.

### 15.4 Parent dashboard metrics

- Tuition due by month.
- Child attendance summary.
- Weekly updates by week.
- Scores by class/date.
- Learning reports.
- Media delivered.

### 15.5 Employee dashboard metrics

- Monthly payroll summary.
- Leave request status.
- Work log summary.

---

## 16. API Contract Map

Use REST endpoints under `/api/v1`.

### 16.1 Attendance APIs

```text
GET    /api/v1/attendance/sessions?date=&status=&classId=
GET    /api/v1/sessions/{sessionId}/attendance
PUT    /api/v1/sessions/{sessionId}/attendance/mark
POST   /api/v1/sessions/{sessionId}/attendance/bulk-mark
POST   /api/v1/sessions/{sessionId}/attendance/submit
POST   /api/v1/sessions/{sessionId}/attendance/review
POST   /api/v1/attendance/{attendanceId}/follow-up
```

### 16.2 Weekly update APIs

```text
GET    /api/v1/weekly-updates?classId=&weekStart=&status=
POST   /api/v1/weekly-updates
GET    /api/v1/weekly-updates/{id}
PUT    /api/v1/weekly-updates/{id}
POST   /api/v1/weekly-updates/{id}/submit
POST   /api/v1/weekly-updates/{id}/approve
POST   /api/v1/weekly-updates/{id}/reject
POST   /api/v1/weekly-updates/{id}/publish
POST   /api/v1/weekly-updates/{id}/files
```

### 16.3 Score APIs

```text
GET    /api/v1/score-items?classId=&status=
POST   /api/v1/score-items
GET    /api/v1/score-items/{id}
PUT    /api/v1/score-items/{id}
PUT    /api/v1/score-items/{id}/records
POST   /api/v1/score-items/{id}/publish
```

### 16.4 Learning report APIs

```text
GET    /api/v1/learning-report-cycles?classId=&status=
POST   /api/v1/learning-report-cycles
GET    /api/v1/learning-reports?cycleId=&classId=&studentId=&status=
PUT    /api/v1/learning-reports/{id}
POST   /api/v1/learning-reports/{id}/submit
POST   /api/v1/learning-reports/{id}/approve
POST   /api/v1/learning-reports/{id}/reject
POST   /api/v1/learning-reports/{id}/publish
POST   /api/v1/learning-report-cycles/{id}/remind
```

### 16.5 Tuition APIs

```text
GET    /api/v1/invoices?studentId=&classId=&month=&status=
POST   /api/v1/invoices
GET    /api/v1/invoices/{id}
PUT    /api/v1/invoices/{id}
POST   /api/v1/invoices/{id}/payments
POST   /api/v1/refund-requests
POST   /api/v1/refund-requests/{id}/approve
POST   /api/v1/tuition-adjustments
POST   /api/v1/tuition-adjustments/{id}/approve
```

### 16.6 Payroll and leave APIs

```text
GET    /api/v1/salary-profiles?employeeId=
POST   /api/v1/salary-profiles
PUT    /api/v1/salary-profiles/{id}
GET    /api/v1/allowance-types
POST   /api/v1/allowance-types
GET    /api/v1/leave-requests?employeeId=&status=&from=&to=
POST   /api/v1/leave-requests
POST   /api/v1/leave-requests/{id}/approve
POST   /api/v1/leave-requests/{id}/reject
POST   /api/v1/leave-requests/{id}/request-cancel
POST   /api/v1/leave-requests/{id}/approve-cancel
GET    /api/v1/payroll-periods
POST   /api/v1/payroll-periods/{id}/calculate
GET    /api/v1/payroll-periods/{id}/items
POST   /api/v1/payroll-items/{id}/approve
POST   /api/v1/payroll-items/{id}/mark-paid
```

### 16.7 Video Delivery Tracking APIs

```text
GET    /api/v1/video-delivery-batches
POST   /api/v1/video-delivery-batches
PUT    /api/v1/video-delivery-batches/{id}
PATCH  /api/v1/video-delivery-batches/{id}/cancel
GET    /api/v1/video-deliveries
GET    /api/v1/video-deliveries/stats
GET    /api/v1/video-deliveries/{id}
POST   /api/v1/video-deliveries/{id}/prepare-message
PATCH  /api/v1/video-deliveries/{id}/copied
PATCH  /api/v1/video-deliveries/{id}/opened-zalo
PATCH  /api/v1/video-deliveries/{id}/sent
PATCH  /api/v1/video-deliveries/{id}/failed
PATCH  /api/v1/video-deliveries/{id}/skipped
PATCH  /api/v1/video-deliveries/{id}/reopen
```

### 16.7A Optional media storage APIs

```text
GET    /api/v1/media-items?classId=&studentId=&category=&status=
POST   /api/v1/media-items
GET    /api/v1/media-items/{id}
PUT    /api/v1/media-items/{id}
POST   /api/v1/media-items/{id}/publish
POST   /api/v1/media-items/{id}/deliver
```

### 16.8 Dashboard APIs

```text
GET /api/v1/dashboard/owner?range=month&from=&to=
GET /api/v1/dashboard/office?date=
GET /api/v1/dashboard/teacher?date=
GET /api/v1/dashboard/parent?childId=&range=month
GET /api/v1/dashboard/student?range=month
GET /api/v1/dashboard/employee?period=
```

---

## 17. Background Jobs and Events

### 17.1 Quartz jobs

| Job | Suggested schedule | Purpose |
|---|---|---|
| `GenerateClassSessionsJob` | Daily 00:10 | Generate sessions from active schedules |
| `AttendanceDeadlineJob` | Every 5 minutes during class hours | Mark overdue attendance and notify teachers/office |
| `AttendanceSummaryJob` | Daily 22:00 | Aggregate attendance data |
| `ConsecutiveAbsenceJob` | Daily 22:30 | Detect repeated absences |
| `WeeklyUpdateReminderJob` | Weekly | Remind teachers to submit weekly updates |
| `LearningReportReminderJob` | Daily | Remind upcoming/overdue report deadlines |
| `TuitionReminderJob` | Daily 08:00 | Tuition due/overdue notifications |
| `PayrollCalculationJob` | Monthly or manual | Calculate payroll |
| `NotificationRetryJob` | Every 15 minutes | Retry failed notifications |
| `DatabaseBackupJob` | Daily | Backup trigger |

### 17.2 Event names

```text
attendance.marked
attendance.submitted
attendance.overdue
attendance.reviewed
student.absent.detected
weekly_update.submitted
weekly_update.approved
weekly_update.published
score.published
learning_report.submitted
learning_report.overdue
learning_report.published
invoice.created
payment.completed
refund.approved
leave.approved
leave.cancelled
payroll.calculated
payroll.approved
media.published
notification.requested
audit.recorded
```

Event listeners must be idempotent.

---

## 18. Required Indexes

Add indexes for large filters and dashboards.

```sql
CREATE INDEX idx_class_staff_employee_active ON class_staff(employee_id, class_id, end_date);
CREATE INDEX idx_class_schedule_plans_class_status ON class_schedule_plans(class_id, status);
CREATE INDEX idx_class_sessions_date_status ON class_sessions(session_date, status);
CREATE INDEX idx_class_sessions_class_date ON class_sessions(class_id, session_date);
CREATE INDEX idx_attendance_session_student ON student_attendance(session_id, student_id);
CREATE INDEX idx_attendance_status_source ON student_attendance(status, source);
CREATE INDEX idx_attendance_credit ON student_attendance(tuition_credit_eligible);
CREATE INDEX idx_attendance_session_status ON class_session_attendance_status(status, due_at);
CREATE INDEX idx_weekly_updates_class_week ON weekly_class_updates(class_id, week_start_date, week_end_date);
CREATE INDEX idx_weekly_updates_status ON weekly_class_updates(status);
CREATE INDEX idx_score_items_class_status ON score_items(class_id, status);
CREATE INDEX idx_score_records_student ON score_records(student_id);
CREATE INDEX idx_learning_report_cycles_class_period ON learning_report_cycles(class_id, period_start, period_end);
CREATE INDEX idx_learning_reports_status ON student_learning_reports(status);
CREATE INDEX idx_invoices_student_status ON invoices(student_id, status);
CREATE INDEX idx_invoices_due_status ON invoices(due_date, status);
CREATE INDEX idx_payroll_items_period_employee ON payroll_items(payroll_period_id, employee_id);
CREATE INDEX idx_leave_requests_employee_status ON leave_requests(employee_id, status);
CREATE INDEX idx_media_items_student_category ON media_items(student_id, video_category);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_name, entity_id);
CREATE INDEX idx_audit_logs_actor_date ON audit_logs(actor_user_id, created_at);
```

---

## 19. AI Feature Suggestions

AI should assist staff, not replace approvals.

Useful AI features:

1. Draft weekly class update from lesson/session notes.
2. Draft two-month learning report from attendance, scores, and teacher notes.
3. Suggest student progress risks from repeated absences, low scores, and teacher notes.
4. Generate parent-friendly message drafts for Office Staff.
5. Generate class activity ideas for teachers.
6. Generate speaking questions and homework suggestions by course level.
7. Summarize audit/activity history when investigating mistakes.

AI outputs must be drafts and require human review before sending to parents.

---

## 20. Transaction Rules

Use `@Transactional` for all write workflows.

Examples:

### 20.1 Mark attendance

```text
Validate session
Validate student belongs to class
Validate actor permission and class scope
Upsert attendance record
Respect locked Office Staff records
Update class_session_attendance_status
Create audit log
Publish attendance.marked after commit
```

### 20.2 Publish weekly update

```text
Validate status APPROVED
Create delivery records for eligible parents/students
Create in-app notifications
Update status PUBLISHED
Create audit log
Publish weekly_update.published after commit
```

### 20.3 Calculate payroll

```text
Lock payroll period
Load active salary profiles
Load approved work logs
Load approved leave requests
Load active allowances
Calculate payroll items and lines
Create audit log
Mark period CALCULATED
Publish payroll.calculated after commit
```

### 20.4 Apply tuition credit

```text
Validate invoice
Validate approved adjustment
Lock invoice with optimistic version
Apply adjustment
Recalculate totals
Create audit log
Commit
```

---

## 21. Definition of Done

A backend feature is done only when:

- Database migration exists.
- Entity, repository, service, controller, DTO, mapper are implemented where needed.
- Permission check is enforced in service/controller.
- Frontend-required response DTOs are flat and stable.
- Write operations are transactional.
- Audit log is created for important changes.
- List APIs have server-side pagination, filtering, and sorting.
- Validation handles business rules and bad states.
- Tests cover main service logic and security denial.
- API docs are updated.
- No business logic depends on hardcoded role names.
- Backend routes match the frontend route/API contract in this file.
