# APIX V3 backend alignment

## Migration strategy

V11 is forward-only. It preserves every existing table and record, introduces the V3 operational structures, and extends attendance rather than replacing it. There was no homework/assignment table in the repository, so no student-submission data required conversion. `weekly_class_updates` is the supported replacement contract.

V12 removes default Teacher/Teaching Assistant access to parent profiles, seeds notification templates, and makes `invoice_payments` append-only at the database boundary. Correct a payment with an adjustment or refund; never update or delete the original payment.

No manual SQL step is required. Deploy normally and let Flyway apply V11 and V12. Existing users who act as employees must receive an `employees` record before they can be assigned through `class_staff` or use employee workflows.

## Implemented API contracts

All routes require a JWT plus the permission shown. List endpoints use zero-based `page` and `size` and return the existing `ApiResponse` envelope.

### Weekly updates

| Route | Permission |
|---|---|
| `GET /api/v1/weekly-updates?classId=&page=&size=` | `weekly_update:read` |
| `GET /api/v1/weekly-updates/{id}` | `weekly_update:read` |
| `POST /api/v1/weekly-updates` | `weekly_update:create` |
| `PUT /api/v1/weekly-updates/{id}` | `weekly_update:update` |
| `POST /api/v1/weekly-updates/{id}/submit` | `weekly_update:submit` |
| `POST /api/v1/weekly-updates/{id}/approve` | `weekly_update:approve` |
| `POST /api/v1/weekly-updates/{id}/reject` | `weekly_update:reject` |
| `POST /api/v1/weekly-updates/{id}/deliver` (`publish` alias retained) | `weekly_update:deliver` |

The write body contains `classId`, `weekStartDate`, `weekEndDate`, optional `homeworkText`, and a non-empty `sessions[]` collection of `sessionDate`, `title`, and `content`. A teacher must be actively assigned through `class_staff`. Session dates must fall inside the week. Status transitions are `DRAFT -> SUBMITTED -> APPROVED -> DELIVERED`; rejection returns to editable state through `REJECTED -> DRAFT`.

### Scores

| Route | Permission |
|---|---|
| `GET /api/v1/score-items?classId=&page=&size=` | `score:read` |
| `GET /api/v1/score-items/{id}` | `score:read` |
| `POST /api/v1/score-items` | `score:create` |
| `PUT /api/v1/score-items/{id}` | `score:update` |
| `PUT /api/v1/score-items/{id}/records` | `score:update` |
| `POST /api/v1/score-items/{id}/publish` | `score:update` |
| `POST /api/v1/score-items/{id}/deliver` | `score:deliver` |

Score items use only `title`, `maxScore`, `scoreDate`, and optional `note`. Records use `studentId`, numeric `score`, and optional `comment`. The backend rejects duplicate students, scores outside `0..maxScore`, and students without an active enrollment. Fixed skill columns are not part of the contract.

### Learning reports

| Route | Permission |
|---|---|
| `GET /api/v1/learning-reports?classId=&studentId=&page=&size=` | `learning_report:read` |
| `GET /api/v1/learning-reports/{id}` | `learning_report:read` |
| `POST /api/v1/learning-reports` | `learning_report:create` |
| `PUT /api/v1/learning-reports/{id}` | `learning_report:update` |
| submit / approve / reject / deliver action routes | corresponding `learning_report:*` permission |

The body uses `reportType` (`PERIODIC`, `FINAL`, `CUSTOM`), period dates, `learningSummary`, `attitudeSummary`, `improvementNotes`, `recommendation`, and `internalNote`. There are no fixed listening/speaking/reading/writing columns. Only assigned teachers may prepare or submit; an independently permissioned Office Staff employee approves, rejects, and delivers.

### Tuition

`POST /api/v1/invoices` remains backward-compatible and now accepts optional `items[]` with `feeType` (`BOOK`, `MATERIAL`, `UNIFORM`, `OTHER`), `description`, `quantity`, and `unitPrice`. A `MONTHLY_TUITION` item is generated automatically. Invoice responses now include item lines. Payments remain on the existing route and are append-only.

## Database-backed V3 foundations

V11 also creates the normalized structures and constraints for class staff and handover notes, fixed schedule plans, attendance workflow state, file metadata, report cycles, tuition credits/refunds, salary profiles, configurable allowances, work logs, leave cancellation, payroll periods/item lines, media delivery, notifications, and audit logs. Their service/controller layers should be delivered module-by-module; exposing placeholder CRUD endpoints before scope and workflow security exist would be unsafe.

The five-minute attendance job creates missing session workflow rows, marks incomplete attendance overdue 30 minutes after start, and creates idempotent in-app reminders for assigned teachers. Dashboard queries can use the supplied V3 indexes.

## Frontend changes

- Replace assignment/submission screens with weekly update sessions plus homework text and workflow actions.
- Replace skill-specific score inputs with a score item and a student score grid.
- Replace skill-specific report forms with narrative fields and the new workflow statuses.
- Render invoice item lines and treat payments as immutable.
- Use the exact permission codes from V11; do not infer access from role names.
- Treat `version` in new responses as the optimistic-lock token for future edit contracts.
