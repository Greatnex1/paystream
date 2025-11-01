package com.interswitch.bulktransaction.service.implementation;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
import com.interswitch.bulktransaction.dto.response.BulkResponse;

import com.interswitch.bulktransaction.dto.response.TransactionResponse;
import com.interswitch.bulktransaction.model.BulkBatch;
import com.interswitch.bulktransaction.model.BulkTransaction;
import com.interswitch.bulktransaction.repository.BulkBatchRepository;
import com.interswitch.bulktransaction.repository.BulkTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class BulkTransactionServiceImplementation {

    private final BulkBatchRepository batchRepo;
    private final BulkTransactionRepository txRepo;
    private final TransactionServiceImplementation integrationService;

//   @Value("${app.processing.concurrency}")
//    private int concurrency;


    private int concurrency = 20;

    public Mono<BulkResponse> processBulk(BulkRequestDto request, String submitterId) {
        return batchRepo.findByBatchId(request.getBatchId())
                .flatMap(existing -> {
// idempotent: fetch already processed transactions for this batch
                    return txRepo.findAllByBatchId(existing.getBatchId())
                            .map(t -> new BulkResponse.TransactionResult(t.getTransactionId(), t.getStatus(), t.getFailureReason()))
                            .collectList()
                            .map(results -> new BulkResponse(existing.getBatchId(), results));
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Batch not found")));
    }


    public Mono<BulkResponse> persistAndProcess(BulkRequestDto request, String submitterId) {
        BulkBatch batch = new BulkBatch();
        batch.setBatchId(request.getBatchId());
        batch.setStatus("PROCESSING");
        batch.setSubmitterId(submitterId);


        return batchRepo.save(batch)
                .flatMap(saved -> {
                    List<BulkTransaction> txEntities = request.getTransactions().stream().map(t -> {
                        BulkTransaction e = new BulkTransaction();
                        e.setBatchId(request.getBatchId());
                        e.setTransactionId(t.getTransactionId());
                        e.setFromAccount(t.getFromAccount());
                        e.setToAccount(t.getToAccount());
                        e.setAmount(t.getAmount());
                        e.setStatus("PENDING");
                        return e;
                    }).collect(Collectors.toList());

                    return txRepo.saveAll(txEntities).collectList().then(processTransactions(request));
                });
    }

    private Mono<BulkResponse> processTransactions(BulkRequestDto request) {
        return Flux.fromIterable(request.getTransactions())
                .flatMap(txReq -> integrationService.callDownstream(txReq)
                        .flatMap(resp -> updateTxResult(request.getBatchId(), resp)), concurrency)
                .collectList()
                .map(results -> new BulkResponse(request.getBatchId(), results));
    }


 //ASYNC
//    public Mono<BulkResponse> processTransactions(BulkRequestDto request) {
//        return Flux.fromIterable(request.getTransactions())
//                .flatMap(tx -> integrationService.callDownstream(tx)
//                        .onErrorResume(ex -> {
//                            log.error("Transaction {} failed: {}", tx.getTransactionId(), ex.getMessage());
//                            TransactionResponse failureResponse = new TransactionResponse(
//                                    tx.getTransactionId(),
//                                    "FAILED",
//                                    ex.getMessage()
//                            );
//                            // ✅ Call recordFailure here
//                            return recordFailure(request.getBatchId(), failureResponse)
//                                    .thenReturn(failureResponse); // continue pipeline
//                        }))
//                .collectList()
//                .map(results -> new BulkResponse(request.getBatchId(), results));
//    }


    private Mono<BulkResponse.TransactionResult> updateTxResult(String batchId, TransactionResponse resp) {
        return txRepo.findByBatchIdAndTransactionId(batchId, resp.getTransactionId())
                .flatMap(entity -> {
                    entity.setAttempts(entity.getAttempts() == null ? 1 : entity.getAttempts() + 1);
                    entity.setStatus(resp.getStatus());
                    entity.setFailureReason(resp.getReason());
                    return txRepo.save(entity);
                })
                .map(e -> new BulkResponse.TransactionResult(resp.getTransactionId(), resp.getStatus(), resp.getReason()))
                .switchIfEmpty(Mono.just(new BulkResponse.TransactionResult(resp.getTransactionId(), resp.getStatus(), resp.getReason())));
    }
//ASYNC
//    public Mono<Void> recordFailure(String batchId, TransactionResponse response) {
//        BulkTransaction tx = new BulkTransaction();
//        tx.setBatchId(batchId);
//        tx.setTransactionId(response.getTransactionId());
//        tx.setStatus(response.getStatus());
//        tx.setFailureReason(response.getReason());
//        return txRepo.save(tx).then();
//    }
}
