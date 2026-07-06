package com.apixenglish.center.modules.schedule.validator;

import com.apixenglish.center.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class SchedulePatternValidatorTest {
    private final SchedulePatternValidator validator = new SchedulePatternValidator();

    @Test void weekendMorningAlwaysPairsSaturdayAndSundayMorning() {
        var meetings = validator.validateAndResolve(SchedulePatternValidator.Pattern.WEEKEND, SchedulePatternValidator.Slot.MORNING);
        assertThat(meetings).containsExactly(
                new SchedulePatternValidator.Meeting(DayOfWeek.SATURDAY, LocalTime.of(9,0), LocalTime.of(11,0)),
                new SchedulePatternValidator.Meeting(DayOfWeek.SUNDAY, LocalTime.of(9,0), LocalTime.of(11,0)));
    }

    @Test void mixedWeekendSlotsAreRejected() {
        var mixed = List.of(
                new SchedulePatternValidator.Meeting(DayOfWeek.SATURDAY, LocalTime.of(15,0), LocalTime.of(17,0)),
                new SchedulePatternValidator.Meeting(DayOfWeek.SUNDAY, LocalTime.of(9,0), LocalTime.of(11,0)));
        assertThatThrownBy(() -> validator.validateMeetings(SchedulePatternValidator.Pattern.WEEKEND, SchedulePatternValidator.Slot.AFTERNOON, mixed))
                .isInstanceOf(BusinessException.class).hasMessageContaining("exactly match");
    }

    @Test void ttsEveningHasFixedTimes() {
        assertThat(validator.validateAndResolve(SchedulePatternValidator.Pattern.TTS, SchedulePatternValidator.Slot.EVENING_2))
                .allMatch(m -> m.start().equals(LocalTime.of(19,30)) && m.end().equals(LocalTime.of(21,0)));
    }
}
