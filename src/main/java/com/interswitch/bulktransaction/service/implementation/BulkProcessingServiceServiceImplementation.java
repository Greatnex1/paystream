package com.interswitch.bulktransaction.service.implementation;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
import com.interswitch.bulktransaction.dto.response.BulkResponse;
import com.interswitch.bulktransaction.model.BulkBatch;
import com.interswitch.bulktransaction.model.BulkTransaction;
import com.interswitch.bulktransaction.model.WorkItem;
import com.interswitch.bulktransaction.repository.BulkBatchRepository;
import com.interswitch.bulktransaction.repository.BulkTransactionRepository;
import com.interswitch.bulktransaction.service.BackgroundWorker;
import com.interswitch.bulktransaction.service.BulkProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
/**
 * Implementation of the {@link BulkProcessingService} interface responsible for orchestrating
 * the end-to-end lifecycle of bulk transaction requests.
 * <p>
 * This service:
 * <ul>
 *   <li>Accepts incoming bulk transaction requests.</li>
 *   <li>Persists the batch and its individual transactions in the database.</li>
 *   <li>Dispatches background workers for asynchronous transaction execution.</li>
 *   <li>Provides status tracking for all transactions in a batch.</li>
 * </ul>
 *
 * <p>Reactive programming is leveraged using <b>Project Reactor</b> to enable non-blocking,
 * asynchronous data processing and backpressure-aware persistence operations.</p>
 *
 * <p><b>Typical Flow:</b></p>
 * <ol>
 *   <li>A client submits a bulk request (list of transactions).</li>
 *   <li>The service stores the batch and transaction records as “PENDING”.</li>
 *   <li>Each transaction is dispatched to the {@link BackgroundWorker} for downstream processing.</li>
 *   <li>Clients can query the batch status using {@link #getBatchStatus(String)}.</li>
 * </ol>
 *
 * <p>Example Log Outputs:</p>
 * <pre>{@code
 * INFO  Successfully accepted bulk batch BATCH123 with 5 transactions
 * DEBUG Fetched 5 transaction statuses for batch BATCH123
 * }</pre>
 * @author noah.akoni
 * since   02/11/2025
        */
@Service
@RequiredArgsConstructor
@Slf4j
public class BulkProcessingServiceServiceImplementation implements BulkProcessingService {

    private final BulkBatchRepository batchRepo;
    private final BulkTransactionRepository txRepo;
    private final BackgroundWorker worker;

    public Mono<String> processBulk(BulkRequestDto request) {
        String submitterId = "system-client";
        BulkBatch batch = new BulkBatch();
        batch.setBatchId(request.getBatchId());
        batch.setStatus("ACCEPTED");
        batch.setSubmitterId(submitterId);

        List<BulkTransaction> bulkTnxEntities = request.getTransactions().stream()
                .map(transactionRequest -> {
                    BulkTransaction bulkTransaction = new BulkTransaction();
                    bulkTransaction.setBatchId(request.getBatchId());
                    bulkTransaction.setTransactionId(transactionRequest.getTransactionId());
                    bulkTransaction.setFromAccount(transactionRequest.getFromAccount());
                    bulkTransaction.setToAccount(transactionRequest.getToAccount());
                    bulkTransaction.setAmount(transactionRequest.getAmount());
                    bulkTransaction.setStatus("PENDING");
                    return bulkTransaction;
                })
                .collect(Collectors.toList());

        return batchRepo.save(batch)
                .flatMap(savedBatch -> txRepo.saveAll(bulkTnxEntities).collectList())
                .flatMap(savedTxs -> {
                    savedTxs.forEach(e -> {
                        var txReq = new BulkRequestDto.TransactionRequest(
                                e.getTransactionId(),
                                e.getFromAccount(),
                                e.getToAccount(),
                                e.getAmount()
                        );
                        worker.submit(new WorkItem(e.getBatchId(), txReq));
                    });
                    return Mono.just(request.getBatchId());
                })
                .doOnSuccess(id -> log.info("Successfully accepted bulk batch {} with {} transactions",
                        id, bulkTnxEntities.size()))
                .doOnError(err -> log.error("Error processing bulk batch {}: {}", request.getBatchId(), err.getMessage(), err));
    }

    public Mono<List<BulkResponse.TransactionResult>> getBatchStatus(String batchId) {
        return txRepo.findAllByBatchId(batchId)
                .map(tx -> new BulkResponse.TransactionResult(
                        tx.getTransactionId(),
                        tx.getStatus(),
                        tx.getFailureReason()
                ))
                .collectList()
                .doOnSuccess(list -> log.debug("Fetched {} transaction statuses for batch {}", list.size(), batchId))
                .doOnError(err -> log.error("Error retrieving status for batch {}: {}", batchId, err.getMessage(), err));
    }
}
