package com.apixenglish.center.modules.schedule.validator;

import com.apixenglish.center.common.exception.BusinessException;
import org.springframework.stereotype.Component;
import java.time.*;
import java.util.*;

@Component
public class SchedulePatternValidator {
    public enum Pattern { MWF, TTS, WEEKEND }
    public enum Slot { EVENING_1, EVENING_2, MORNING, AFTERNOON }
    public record Meeting(DayOfWeek day, LocalTime start, LocalTime end) {}

    public List<Meeting> validateAndResolve(Pattern pattern, Slot slot) {
        if (pattern == null || slot == null) throw invalid();
        return switch (pattern) {
            case MWF -> evening(slot, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
            case TTS -> evening(slot, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY);
            case WEEKEND -> weekend(slot);
        };
    }

    public void validateMeetings(Pattern pattern, Slot slot, Collection<Meeting> submitted) {
        List<Meeting> expected = validateAndResolve(pattern, slot);
        if (submitted == null || submitted.size() != expected.size() || !new HashSet<>(submitted).equals(new HashSet<>(expected))) {
            throw new BusinessException("Meetings must exactly match the selected fixed schedule pattern and slot", "INVALID_SCHEDULE_PATTERN");
        }
    }

    private List<Meeting> evening(Slot slot, DayOfWeek... days) {
        LocalTime start;
        LocalTime end;
        if (slot == Slot.EVENING_1) { start = LocalTime.of(18,0); end = LocalTime.of(19,30); }
        else if (slot == Slot.EVENING_2) { start = LocalTime.of(19,30); end = LocalTime.of(21,0); }
        else throw invalid();
        return Arrays.stream(days).map(day -> new Meeting(day,start,end)).toList();
    }

    private List<Meeting> weekend(Slot slot) {
        LocalTime start;
        LocalTime end;
        if (slot == Slot.MORNING) { start = LocalTime.of(9,0); end = LocalTime.of(11,0); }
        else if (slot == Slot.AFTERNOON) { start = LocalTime.of(15,0); end = LocalTime.of(17,0); }
        else throw invalid();
        return List.of(new Meeting(DayOfWeek.SATURDAY,start,end), new Meeting(DayOfWeek.SUNDAY,start,end));
    }

    private BusinessException invalid() {
        return new BusinessException("Slot is not valid for the selected schedule pattern", "INVALID_SCHEDULE_SLOT");
    }
}
