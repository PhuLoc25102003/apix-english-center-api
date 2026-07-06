package com.apixenglish.center.modules.media.service;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.modules.media.entity.VideoType;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MediaVideoServiceTest {
    private final MediaVideoService service = new MediaVideoService(null,null,null,null,null,null,null,null,null,null,null,null,null,null,null);

    @Test void monthlyPersonalVideoRequiresStudentAndMonth() {
        BusinessException error=assertThrows(BusinessException.class,()->service.validateTarget(VideoType.MONTHLY_PERSONAL_VIDEO,null,UUID.randomUUID(),null));
        assertEquals("MONTHLY_VIDEO_TARGET_REQUIRED",error.getErrorCode());
    }

    @Test void classActivityRequiresClass() {
        BusinessException error=assertThrows(BusinessException.class,()->service.validateTarget(VideoType.CLASS_ACTIVITY_VIDEO,null,UUID.randomUUID(),null));
        assertEquals("CLASS_VIDEO_TARGET_REQUIRED",error.getErrorCode());
    }

    @Test void targetMonthMustBeFirstDay() {
        BusinessException error=assertThrows(BusinessException.class,()->service.validateTarget(VideoType.MONTHLY_PERSONAL_VIDEO,UUID.randomUUID(),UUID.randomUUID(),LocalDate.of(2026,7,2)));
        assertEquals("INVALID_TARGET_MONTH",error.getErrorCode());
    }

    @Test void validMonthlyTargetPasses() {
        assertDoesNotThrow(()->service.validateTarget(VideoType.MONTHLY_PERSONAL_VIDEO,UUID.randomUUID(),UUID.randomUUID(),LocalDate.of(2026,7,1)));
    }
}
