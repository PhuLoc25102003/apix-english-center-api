package com.apixenglish.center.modules.score.dto;
import com.apixenglish.center.modules.score.entity.ScoreItemStatus;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import java.math.BigDecimal;import java.time.*;import java.util.*;
public final class ScoreDtos {private ScoreDtos(){}
 public record Upsert(@NotNull UUID classId,@NotBlank String title,@NotNull @DecimalMin("0.01") BigDecimal maxScore,@NotNull LocalDate scoreDate,String note){}
 public record RecordInput(@NotNull UUID studentId,@NotNull @DecimalMin("0.0") BigDecimal score,String comment){}
 public record Records(@NotEmpty List<@Valid RecordInput> records){}
 public record RecordResponse(UUID studentId,BigDecimal score,String comment,UUID gradedBy,Instant gradedAt){}
 public record Response(UUID id,UUID classId,String title,BigDecimal maxScore,LocalDate scoreDate,String note,ScoreItemStatus status,List<RecordResponse> records,Instant deliveredAt,Integer version){}
}
