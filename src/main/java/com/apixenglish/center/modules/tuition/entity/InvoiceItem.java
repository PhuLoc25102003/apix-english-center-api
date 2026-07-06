package com.apixenglish.center.modules.tuition.entity;
import com.apixenglish.center.common.domain.SoftDeleteEntity;import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;
@Entity @Table(name="invoice_items") @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InvoiceItem extends SoftDeleteEntity{
 @ManyToOne(fetch=FetchType.LAZY)@JoinColumn(name="invoice_id",nullable=false)private Invoice invoice;
 @Column(name="fee_type",nullable=false)private String feeType;@Column(nullable=false)private String description;
 @Column(nullable=false)private BigDecimal quantity;@Column(name="unit_price",nullable=false)private BigDecimal unitPrice;@Column(nullable=false)private BigDecimal amount;
}
