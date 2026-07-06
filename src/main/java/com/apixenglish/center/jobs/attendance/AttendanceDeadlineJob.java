package com.apixenglish.center.jobs.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Idempotent five-minute attendance SLA monitor. */
@Component @RequiredArgsConstructor
public class AttendanceDeadlineJob {
    private final JdbcTemplate jdbc;

    @Scheduled(cron = "0 */5 17-22 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void markAndNotifyOverdueAttendance() {
        jdbc.update("""
            INSERT INTO class_session_attendance_status(id,session_id,status,due_at,created_at,updated_at,version)
            SELECT gen_random_uuid(),s.id,'NOT_STARTED',(s.session_date+s.start_time)+INTERVAL '30 minutes',NOW(),NOW(),0
            FROM class_sessions s
            WHERE s.deleted_at IS NULL AND s.session_date BETWEEN CURRENT_DATE-1 AND CURRENT_DATE
              AND NOT EXISTS (SELECT 1 FROM class_session_attendance_status x WHERE x.session_id=s.id)
            ON CONFLICT (session_id) DO NOTHING
            """);
        jdbc.update("""
            UPDATE class_session_attendance_status
            SET status='OVERDUE',updated_at=NOW(),version=version+1
            WHERE status IN ('NOT_STARTED','IN_PROGRESS') AND due_at<NOW()
            """);
        jdbc.update("""
            INSERT INTO notifications(id,template_id,title,body,target_type,target_user_id,channel,status,metadata,created_at,updated_at,version)
            SELECT gen_random_uuid(),t.id,'Attendance overdue',
                   'Attendance for class session '||s.id||' is overdue.','USER',e.user_id,'IN_APP','PENDING',
                   jsonb_build_object('type','ATTENDANCE_OVERDUE','sessionId',s.id,'classId',s.class_id),NOW(),NOW(),0
            FROM class_session_attendance_status a
            JOIN class_sessions s ON s.id=a.session_id
            JOIN class_staff cs ON cs.class_id=s.class_id AND cs.staff_role='TEACHER' AND cs.deleted_at IS NULL
            JOIN employees e ON e.id=cs.employee_id AND e.user_id IS NOT NULL
            JOIN notification_templates t ON t.code='ATTENDANCE_OVERDUE'
            WHERE a.status='OVERDUE' AND a.overdue_notified_at IS NULL
            """);
        jdbc.update("UPDATE class_session_attendance_status SET overdue_notified_at=NOW(),updated_at=NOW() WHERE status='OVERDUE' AND overdue_notified_at IS NULL");
    }
}
