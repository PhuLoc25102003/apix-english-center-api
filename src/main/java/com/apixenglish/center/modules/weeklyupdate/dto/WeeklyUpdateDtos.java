package com.apixenglish.center.modules.weeklyupdate.dto;
import com.apixenglish.center.modules.weeklyupdate.entity.WeeklyUpdateStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;
public final class WeeklyUpdateDtos {
 private WeeklyUpdateDtos() {}
 public record Session(@NotNull LocalDate sessionDate,String title,@NotBlank String content) {}
 public record Upsert(@NotNull UUID classId,@NotNull LocalDate weekStartDate,@NotNull LocalDate weekEndDate,String homeworkText,@NotEmpty List<@Valid Session> sessions) {}
 public record Reject(@NotBlank String reason) {}
 public record Response(UUID id,UUID classId,LocalDate weekStartDate,LocalDate weekEndDate,String homeworkText,WeeklyUpdateStatus status,UUID preparedBy,UUID approvedBy,Instant approvedAt,String rejectionReason,Instant deliveredAt,List<Session> sessions,Integer version) {}
}
