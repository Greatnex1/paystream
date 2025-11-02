package com.interswitch.bulktransaction.service;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
import com.interswitch.bulktransaction.dto.response.BulkResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BulkProcessingService {
     Mono<String> processBulk(BulkRequestDto request);

    Mono<List<BulkResponse.TransactionResult>> getBatchStatus(String batchId);
}
