package com.interswitch.bulktransaction.service;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
import com.interswitch.bulktransaction.dto.response.TransactionResponse;
import com.interswitch.bulktransaction.model.BulkBatch;
import com.interswitch.bulktransaction.model.BulkTransaction;
import com.interswitch.bulktransaction.repository.BulkBatchRepository;
import com.interswitch.bulktransaction.repository.BulkTransactionRepository;
import com.interswitch.bulktransaction.service.implementation.BulkTransactionServiceImplementation;
import com.interswitch.bulktransaction.service.implementation.TransactionServiceImplementation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
class BulkTransactionServiceTest {

    @Mock
    private BulkBatchRepository batchRepo;

    @Mock
    private BulkTransactionRepository txRepo;

    @Mock
    private TransactionServiceImplementation integrationService;

    @InjectMocks
    private BulkTransactionServiceImplementation service;

    @Test
    void shouldProcessAllTransactionsAndReturnResults() {
        // Mock data
        BulkBatch batch = new BulkBatch();
        batch.setBatchId("BULK12345");

        BulkTransaction tx = new BulkTransaction();
        tx.setTransactionId("TX001");
        tx.setStatus("SUCCESS");

        // Mock behaviors
       when(batchRepo.findByBatchId("BULK12345")).thenReturn(Mono.just(batch));
        when(txRepo.findAllByBatchId("BULK12345")).thenReturn(Flux.just(tx));
       lenient().when(integrationService.callDownstream(any()))
                .thenReturn(Mono.just(new TransactionResponse("TX001", "SUCCESS", null)));

        // Build request
        BulkRequestDto request = new BulkRequestDto(
                "BULK12345",
                List.of(new BulkRequestDto.TransactionRequest("TX001", "111", "222", BigDecimal.TEN))
        );

        // Verify
        StepVerifier.create(service.processBulk(request, "6679"))
                .expectNextMatches(resp ->
                        resp.getBatchId().equals("BULK12345") &&
                                !resp.getResults().isEmpty() &&
                                resp.getResults().get(0).getStatus().equals("SUCCESS"))
                .verifyComplete();
        log.info("Batch process completed");

}


    @Test
    void shouldHandleDownstreamFailure() {
        // Mock repositories
        BulkBatchRepository batchRepo = Mockito.mock(BulkBatchRepository.class);
        BulkTransactionRepository txRepo = Mockito.mock(BulkTransactionRepository.class);
        TransactionServiceImplementation integrationService = Mockito.mock(TransactionServiceImplementation.class);

        // Mock downstream failure
        when(integrationService.callDownstream(any())).thenReturn(
                Mono.just(new TransactionResponse("TX002", "FAILED", "Insufficient funds"))
        );

        // Mock batch saving
        when(batchRepo.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // Mock saving transactions
        when(txRepo.saveAll((Iterable<BulkTransaction>) any())).thenAnswer(invocation -> {
            List<?> txList = invocation.getArgument(0);
            return Flux.fromIterable(txList);
        });

        // Mock findByBatchIdAndTransactionId for updateTxResult()
        when(txRepo.findByBatchIdAndTransactionId(any(), any()))
                .thenReturn(Mono.just(new BulkTransaction()));

        when(txRepo.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        BulkTransactionServiceImplementation service =
                new BulkTransactionServiceImplementation(batchRepo, txRepo, integrationService);

        BulkRequestDto request = new BulkRequestDto("BULKFAIL",
                List.of(new BulkRequestDto.TransactionRequest("TX002", "111", "333", BigDecimal.TEN)));

        StepVerifier.create(service.persistAndProcess(request, "JJ44567"))
                .expectNextMatches(resp ->
                        resp.getBatchId().equals("BULKFAIL") &&
                                resp.getResults().get(0).getStatus().equals("FAILED") &&
                                resp.getResults().get(0).getReason().equals("Insufficient funds"))
                .verifyComplete();
    }

}