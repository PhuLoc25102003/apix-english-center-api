-- Teachers and teaching assistants must never receive parent private-data access by default.
DELETE FROM role_permissions rp
USING roles r, permissions p
WHERE rp.role_id=r.id AND rp.permission_id=p.id
  AND r.code IN ('TEACHER','TEACHING_ASSISTANT') AND p.code='parent:read';

INSERT INTO notification_templates(code,channel,title_template,body_template,is_active)
VALUES
 ('ATTENDANCE_OVERDUE','IN_APP','Attendance overdue','Attendance for class session {{sessionId}} is overdue.',TRUE),
 ('WEEKLY_UPDATE_PENDING','IN_APP','Weekly update pending approval','A weekly class update is waiting for Office Staff review.',TRUE),
 ('LEARNING_REPORT_DUE','IN_APP','Learning report due','A learning report is due soon.',TRUE),
 ('SCORE_DELIVERED','IN_APP','New score available','A score notification has been delivered.',TRUE),
 ('TUITION_OVERDUE','IN_APP','Tuition overdue','A tuition invoice is overdue.',TRUE),
 ('PAYROLL_READY','IN_APP','Payroll ready','Payroll is ready for review.',TRUE),
 ('LEAVE_STATUS_CHANGED','IN_APP','Leave status changed','Your leave request status changed.',TRUE)
ON CONFLICT (code) DO NOTHING;

-- Finance records are append-only at the database boundary. Corrections must use refund/adjustment records.
CREATE OR REPLACE FUNCTION reject_payment_mutation() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'invoice_payments are append-only; create an adjustment or refund instead';
END $$;
CREATE TRIGGER trg_invoice_payments_no_update BEFORE UPDATE OR DELETE ON invoice_payments
FOR EACH ROW EXECUTE FUNCTION reject_payment_mutation();
