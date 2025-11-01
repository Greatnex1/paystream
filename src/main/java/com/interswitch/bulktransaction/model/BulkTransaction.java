package com.interswitch.bulktransaction.model;

import lombok.Data;
import lombok.ToString;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Table("bulk_trx")
public class BulkTransaction {
    @Id
    private Long id;
    private String batchId;
    private String transactionId;
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String status; // PENDING, SUCCESS, FAILED
    private String failureReason;
    private Integer attempts = 0;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated ;
}
