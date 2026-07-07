package com.apixenglish.center.modules.videodelivery.service;
import com.apixenglish.center.common.exception.BusinessException; import com.apixenglish.center.modules.media.entity.VideoType;
import org.junit.jupiter.api.Test; import java.time.LocalDate; import static org.junit.jupiter.api.Assertions.*;
class VideoDeliveryServiceTest {
 private final VideoDeliveryService service=new VideoDeliveryService(null,null,null,null,null,null,null,null,null,null);
 @Test void monthlyVideoRequiresMonth(){BusinessException e=assertThrows(BusinessException.class,()->service.validateMonth(VideoType.MONTHLY_PERSONAL_VIDEO,null));assertEquals("VIDEO_DELIVERY_MONTH_REQUIRED",e.getErrorCode());}
 @Test void targetMonthMustBeFirstDay(){BusinessException e=assertThrows(BusinessException.class,()->service.validateMonth(VideoType.CUSTOM,LocalDate.of(2026,7,2)));assertEquals("INVALID_TARGET_MONTH",e.getErrorCode());}
 @Test void monthlyMessageDescribesDirectZaloAttachmentWithoutLink(){String value=service.message(VideoType.MONTHLY_PERSONAL_VIDEO,LocalDate.of(2026,7,1),"Nguyễn An");assertTrue(value.contains("07/2026"));assertTrue(value.contains("Nguyễn An"));assertTrue(value.contains("trực tiếp trong tin nhắn Zalo"));assertFalse(value.contains("http"));}
 @Test void finalCourseMessageUsesFinalTemplate(){String value=service.message(VideoType.FINAL_COURSE_VIDEO,null,"Trần Bình");assertTrue(value.contains("video cuối khóa"));assertTrue(value.endsWith("APIX English"));}
}
