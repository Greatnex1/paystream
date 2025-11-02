package com.interswitch.bulktransaction.service;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
import com.interswitch.bulktransaction.dto.response.BulkResponse;
import com.interswitch.bulktransaction.model.BulkBatch;
import com.interswitch.bulktransaction.model.BulkTransaction;
import com.interswitch.bulktransaction.model.WorkItem;
import com.interswitch.bulktransaction.repository.BulkBatchRepository;
import com.interswitch.bulktransaction.repository.BulkTransactionRepository;
import com.interswitch.bulktransaction.service.implementation.BulkProcessingServiceServiceImplementation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BulkProcessingServiceServiceImplementationTest {

    @Mock
    private BulkBatchRepository batchRepo;

    @Mock
    private BulkTransactionRepository txRepo;

    @Mock
    private BackgroundWorker worker;

    @InjectMocks
    private BulkProcessingServiceServiceImplementation service;

//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//    }

    @Test
    void shouldProcessBulkSuccessfully() {
        // Arrange
        BulkRequestDto.TransactionRequest tx1 = new BulkRequestDto.TransactionRequest("TX001", "A1", "B1", BigDecimal.valueOf(100));
        BulkRequestDto.TransactionRequest tx2 = new BulkRequestDto.TransactionRequest("TX002", "A2", "B2", BigDecimal.valueOf(200));

        BulkRequestDto request = new BulkRequestDto("BATCH1234", List.of(tx1, tx2));

        BulkBatch savedBatch = new BulkBatch();
        savedBatch.setBatchId("BATCH1234");

        BulkTransaction firstBulkTransaction = new BulkTransaction();
        firstBulkTransaction.setBatchId("BATCH1234");
        firstBulkTransaction.setTransactionId("TX001");
        BulkTransaction secondBulkTransaction = new BulkTransaction();
        secondBulkTransaction.setBatchId("BATCH001");
        secondBulkTransaction.setTransactionId("TX002");

        when(batchRepo.save(any(BulkBatch.class))).thenReturn(Mono.just(savedBatch));
        when(txRepo.saveAll(anyList())).thenReturn(Flux.just(firstBulkTransaction, secondBulkTransaction));

        // Act
        Mono<String> result = service.processBulk(request);

        // Assert
        StepVerifier.create(result)
                .expectNext("BATCH1234")
                .verifyComplete();

        verify(batchRepo, times(1)).save(any(BulkBatch.class));
        verify(txRepo, times(1)).saveAll(anyList());
        verify(worker, times(2)).submit(any(WorkItem.class));
    }

    @Test
    void shouldHandleBatchRepoFailure() {
        // Arrange
        BulkRequestDto.TransactionRequest tx = new BulkRequestDto.TransactionRequest("TX001", "A1", "B1", BigDecimal.valueOf(100));
        BulkRequestDto request = new BulkRequestDto("BATCH_FAIL", List.of(tx));

        when(batchRepo.save(any(BulkBatch.class))).thenReturn(Mono.error(new RuntimeException("DB unavailable")));

        // Act
        Mono<String> result = service.processBulk(request);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(err -> err instanceof RuntimeException && err.getMessage().contains("DB unavailable"))
                .verify();

        verify(batchRepo, times(1)).save(any(BulkBatch.class));
        verifyNoInteractions(txRepo);
        verifyNoInteractions(worker);
    }

    @Test
    void shouldHandleTxRepoFailure() {
        // Arrange
        BulkRequestDto.TransactionRequest tx = new BulkRequestDto.TransactionRequest("TX_FAIL", "A1", "B1", BigDecimal.valueOf(50));
        BulkRequestDto request = new BulkRequestDto("BATCH_ERR", List.of(tx));

        BulkBatch batch = new BulkBatch();
        batch.setBatchId("BATCH_ERR");

        when(batchRepo.save(any(BulkBatch.class))).thenReturn(Mono.just(batch));
        when(txRepo.saveAll(anyList())).thenReturn(Flux.error(new RuntimeException("SaveAll failed")));

        // Act
        Mono<String> result = service.processBulk(request);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(err -> err instanceof RuntimeException && err.getMessage().contains("SaveAll failed"))
                .verify();

        verify(batchRepo, times(1)).save(any(BulkBatch.class));
        verify(txRepo, times(1)).saveAll(anyList());
        verifyNoInteractions(worker);
    }

    @Test
    void shouldReturnBatchStatusSuccessfully() {
        // Arrange
        String batchId = "BATCH123";
        BulkTransaction t1 = new BulkTransaction();
        t1.setTransactionId("TX001");
        t1.setStatus("SUCCESS");
        t1.setFailureReason(null);

        BulkTransaction t2 = new BulkTransaction();
        t2.setTransactionId("TX002");
        t2.setStatus("FAILED");
        t2.setFailureReason("Insufficient funds");

        when(txRepo.findAllByBatchId(batchId)).thenReturn(Flux.just(t1, t2));

        // Act
        Mono<List<BulkResponse.TransactionResult>> result = service.getBatchStatus(batchId);

        // Assert
        StepVerifier.create(result)
                .assertNext(list -> {
                    assert list.size() == 2;
                    assert list.get(0).getTransactionId().equals("TX001");
                    assert list.get(1).getStatus().equals("FAILED");
                })
                .verifyComplete();

        verify(txRepo, times(1)).findAllByBatchId(batchId);
    }

    @Test
    void shouldHandleErrorWhenFetchingBatchStatus() {
        // Arrange
        String batchId = "BATCH_ERR";
        when(txRepo.findAllByBatchId(batchId)).thenReturn(Flux.error(new RuntimeException("DB error")));

        // Act
        Mono<List<BulkResponse.TransactionResult>> result = service.getBatchStatus(batchId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(err -> err instanceof RuntimeException && err.getMessage().contains("DB error"))
                .verify();

        verify(txRepo, times(1)).findAllByBatchId(batchId);
    }
}
