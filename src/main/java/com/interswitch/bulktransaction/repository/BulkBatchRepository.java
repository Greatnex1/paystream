package com.interswitch.bulktransaction.repository;

import com.interswitch.bulktransaction.model.BulkBatch;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BulkBatchRepository extends ReactiveCrudRepository<BulkBatch, Long> {
    Mono<BulkBatch> findByBatchId(String batchId);
}
