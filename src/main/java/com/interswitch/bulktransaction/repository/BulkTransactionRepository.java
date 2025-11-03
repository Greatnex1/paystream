package com.interswitch.bulktransaction.repository;

import com.interswitch.bulktransaction.model.BulkTransaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BulkTransactionRepository extends ReactiveCrudRepository<BulkTransaction,Long> {
    Flux<BulkTransaction> findAllByBatchId(String batchId);
    Flux<BulkTransaction> findAllByStatus(String status);
    Mono<BulkTransaction> findByBatchIdAndTransactionId(String batchId, String transactionId);
}
