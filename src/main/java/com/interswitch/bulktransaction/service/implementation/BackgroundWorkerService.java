package com.interswitch.bulktransaction.service.implementation;

import com.interswitch.bulktransaction.model.ProcessingItem;
import com.interswitch.bulktransaction.repository.BulkTransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackgroundWorkerService {
    /**
     * Reactive downstream integration service responsible for calling external transaction endpoints.
     */
    private final TransactionServiceImplementation integrationService;

    /**
     * Reactive repository for accessing and updating transaction records.
     */
    private final BulkTransactionRepository txRepo;

    private final Sinks.Many<ProcessingItem> sink = Sinks.many().unicast().onBackpressureBuffer();

    @Value("${app.processing.concurrency:10}")
    private int concurrency;

    public void submit(ProcessingItem item) {
        sink.tryEmitNext(item);
    }

    @PostConstruct
    public void start() {
        Flux<ProcessingItem> flux = sink.asFlux();

        flux
                .flatMap(item -> {
                    log.debug("Processing work item {}:{}", item.getBatchId(), item.getTransaction().getTransactionId());
                    // mark IN_PROGRESS in DB
                    return txRepo.findByBatchIdAndTransactionId(item.getBatchId(), item.getTransaction().getTransactionId())
                            .flatMap(entity -> {
                                entity.setStatus("IN_PROGRESS");
                                entity.setAttempts(entity.getAttempts() == null ? 1 : entity.getAttempts() + 1);
                                return txRepo.save(entity);
                            })
                            .then(integrationService.callDownstream(item.getTransaction()))
                            .flatMap(response -> txRepo.findByBatchIdAndTransactionId(item.getBatchId(), item.getTransaction().getTransactionId())
                                    .flatMap(e -> {
                                        e.setStatus(response.getStatus());
                                        e.setFailureReason(response.getReason());
                                        return txRepo.save(e);
                                    }))
                            .onErrorResume(e -> {
                                log.error("Worker error for {}:{} -> {}", item.getBatchId(), item.getTransaction().getTransactionId(), e.getMessage());
                                return txRepo.findByBatchIdAndTransactionId(item.getBatchId(), item.getTransaction().getTransactionId())
                                        .flatMap(bulkTnx -> {
                                            bulkTnx.setStatus("FAILED");
                                            bulkTnx.setFailureReason(bulkTnx.getFailureReason());
                                            return txRepo.save(bulkTnx);
                                        });
                            });
                }, concurrency)
                .subscribe(r -> log.debug("Work item complete"), err -> log.error("Worker stream error", err));
    }
}