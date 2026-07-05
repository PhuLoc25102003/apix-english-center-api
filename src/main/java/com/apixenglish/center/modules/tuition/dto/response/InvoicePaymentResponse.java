package com.apixenglish.center.modules.tuition.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePaymentResponse {
    private UUID id;
    private UUID invoiceId;
    private String invoiceNo;
    private String paymentNo;
    private BigDecimal amount;
    private OffsetDateTime paymentDate;
    private String paymentMethod;
    private String note;
}
