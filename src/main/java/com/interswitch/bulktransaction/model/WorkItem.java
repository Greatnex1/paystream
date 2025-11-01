package com.interswitch.bulktransaction.model;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
import lombok.Data;

@Data
public class WorkItem {
    private String batchId;
    private BulkRequestDto.TransactionRequest transaction;
}
