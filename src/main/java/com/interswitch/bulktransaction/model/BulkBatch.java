package com.interswitch.bulktransaction.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
@Table("bulk_batch")
@Data
public class BulkBatch {
    @Id
    private Long id;
    private String batchId;
    private String status; // PENDING, PROCESSING, COMPLETE
    private String submitterId;
    private LocalDateTime dateCreated;
}


