package com.interswitch.bulktransaction.controller;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
import com.interswitch.bulktransaction.dto.response.BulkResponse;
import com.interswitch.bulktransaction.service.implementation.BulkProcessingServiceServiceImplementation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class BulkControllerTest {

    @Mock
    private BulkProcessingServiceServiceImplementation bulkService;

    @InjectMocks
    private BulkController controller;

    // ---------- submitBulk() ----------

    @Test
    void shouldReturnAcceptedOnSuccessfulSubmission() {
        BulkRequestDto.TransactionRequest tx =
                new BulkRequestDto.TransactionRequest("TX001", "A1", "B1", BigDecimal.valueOf(100));
        BulkRequestDto request = new BulkRequestDto("BATCH001", List.of(tx));

        when(bulkService.processBulk(any())).thenReturn(Mono.just("BATCH001"));

        StepVerifier.create(controller.submitBulk(request))
                .assertNext(resp -> {
                    assertEquals(HttpStatus.ACCEPTED, resp.getStatusCode());
                })
                .verifyComplete();

        verify(bulkService, times(1)).processBulk(any());
    }

    @Test
    void shouldReturnInternalServerErrorWhenServiceFails() {
        BulkRequestDto request =
                new BulkRequestDto("BATCH_ERR",
                        List.of(new BulkRequestDto.TransactionRequest("TX001", "A1", "B1", BigDecimal.TEN)));

        when(bulkService.processBulk(any())).thenReturn(Mono.error(new RuntimeException("Downstream failure")));

        StepVerifier.create(controller.submitBulk(request))
                .assertNext(resp -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode()))
                .verifyComplete();

        verify(bulkService, times(1)).processBulk(any());
    }

    // ---------- getStatus() ----------

    @Test
    void shouldReturnOkWhenStatusFound() {
        String batchId = "BATCH123";
        List<BulkResponse.TransactionResult> results = List.of(
                new BulkResponse.TransactionResult("TX001", "SUCCESS", null),
                new BulkResponse.TransactionResult("TX002", "FAILED", "Insufficient funds")
        );

        when(bulkService.getBatchStatus(batchId)).thenReturn(Mono.just(results));

        StepVerifier.create(controller.viewStatus(batchId))
                .assertNext(resp -> {
                    assertEquals(HttpStatus.OK, resp.getStatusCode());
                    assertEquals(2, resp.getBody().size());
                })
                .verifyComplete();

        verify(bulkService, times(1)).getBatchStatus(batchId);
    }

    @Test
    void shouldReturnNotFoundWhenStatusListEmpty() {
        String batchId = "BATCH_EMPTY";
        when(bulkService.getBatchStatus(batchId)).thenReturn(Mono.just(List.of()));

        StepVerifier.create(controller.viewStatus(batchId))
                .assertNext(resp -> assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode()))
                .verifyComplete();

        verify(bulkService, times(1)).getBatchStatus(batchId);
    }

    @Test
    void shouldPropagateErrorWhenGetStatusFails() {
        String batchId = "BATCH_ERR";
        when(bulkService.getBatchStatus(batchId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(controller.viewStatus(batchId))
                .expectErrorMatches(err -> err instanceof RuntimeException &&
                        err.getMessage().contains("DB error"))
                .verify();

        verify(bulkService, times(1)).getBatchStatus(batchId);
    }
}
