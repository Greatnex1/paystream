package com.interswitch.bulktransaction.model;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessingItem {
    private String batchId;
    private BulkRequestDto.TransactionRequest transaction;
}
