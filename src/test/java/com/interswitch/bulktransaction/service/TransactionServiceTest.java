package com.interswitch.bulktransaction.service;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
import com.interswitch.bulktransaction.dto.response.TransactionResponse;
import com.interswitch.bulktransaction.service.implementation.TransactionServiceImplementation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class TransactionServiceTest {
    @Mock
    private WebClient webClient;
    private WebClient.RequestBodyUriSpec uriSpec;
    private WebClient.RequestBodySpec bodySpec;
    private WebClient.RequestHeadersSpec<?> headersSpec;
    private WebClient.ResponseSpec responseSpec;

    private TransactionServiceImplementation service;

    @BeforeEach
    void setUp() {
        webClient = Mockito.mock(WebClient.class);
        uriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
        bodySpec = Mockito.mock(WebClient.RequestBodySpec.class);
        headersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
        responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);
//        when(bodySpec.bodyValue(any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);

        service = new TransactionServiceImplementation(webClient);
    }

    @Test
    void shouldReturnSuccessResponse_WhenDownstreamCallSucceeds() {
        // given
        BulkRequestDto.TransactionRequest tx =
                new BulkRequestDto.TransactionRequest("TX001", "ACC100", "ACC200", BigDecimal.valueOf(500));

        TransactionResponse expectedResponse =
                new TransactionResponse("TX001", "SUCCESS", null);

        when(responseSpec.bodyToMono(TransactionResponse.class))
                .thenReturn(Mono.just(expectedResponse));

        // when
        Mono<TransactionResponse> result = service.callDownstream(tx);

        // then
        StepVerifier.create(result)
                .expectNextMatches(resp ->
                        resp.getTransactionId().equals("TX001") &&
                                resp.getStatus().equals("SUCCESS"))
                .verifyComplete();

        verify(webClient).post();
        verify(responseSpec).bodyToMono(TransactionResponse.class);
    }

    @Test
    void shouldReturnFailedResponse_WhenDownstreamThrowsException() {
        // given
        BulkRequestDto.TransactionRequest tx =
                new BulkRequestDto.TransactionRequest("TX002", "ACC300", "ACC400", BigDecimal.valueOf(500));

        when(responseSpec.bodyToMono(TransactionResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Connection timeout")));

        // when
        Mono<TransactionResponse> result = service.callDownstream(tx);

        // then
        StepVerifier.create(result)
                .expectNextMatches(resp ->
                        resp.getTransactionId().equals("TX002") &&
                                resp.getStatus().equals("FAILED") &&
                                resp.getReason().contains("Downstream error"))
                .verifyComplete();
    }

    @Test
    void shouldReturnFallbackResponse_WhenCircuitBreakerTriggers() {
        // given
        BulkRequestDto.TransactionRequest tx =
                new BulkRequestDto.TransactionRequest("TX003", "A100", "B100", BigDecimal.valueOf(500));

        // simulate circuit breaker fallback manually
        Mono<TransactionResponse> fallbackResult = service
                .callDownstream(tx)
                .onErrorResume(ex -> service.fallbackTransaction(tx, ex));

        // when
        StepVerifier.create(fallbackResult)
                .expectNextMatches(resp ->
                        resp.getTransactionId().equals("TX003") &&
                                resp.getStatus().equals("FAILED") &&
                                resp.getReason().equals("Transaction-Service unavailable"))
                .verifyComplete();
    }
}
