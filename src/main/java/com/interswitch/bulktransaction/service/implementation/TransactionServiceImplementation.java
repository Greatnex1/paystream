package com.interswitch.bulktransaction.service.implementation;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
import com.interswitch.bulktransaction.dto.response.TransactionResponse;
import com.interswitch.bulktransaction.service.TransactionService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImplementation implements TransactionService {

    private final WebClient webClient;


    @Retry(name = "transactionServiceRetry", fallbackMethod = "fallbackTransaction")
    @CircuitBreaker(name = "transactionServiceCircuitBreaker", fallbackMethod = "fallbackTransaction")

    public Mono<TransactionResponse> callDownstream(BulkRequestDto.TransactionRequest tx) {

        return webClient.post()
                .uri("/api/v1/transactions")
                .bodyValue(tx)
                .retrieve()
                .bodyToMono(TransactionResponse.class)
                .doOnNext(resp -> log.info("Processed TX {} -> {}", tx.getTransactionId(), resp.getStatus()))
                .onErrorResume(ex -> {
                    log.error("Error calling downstream for TX {}: {}", tx.getTransactionId(), ex.getMessage());
                    return Mono.just(new TransactionResponse(tx.getTransactionId(), "FAILED", "Downstream error: " + ex.getMessage()));
                });
    }




    public Mono<TransactionResponse> fallbackTransaction(BulkRequestDto.TransactionRequest tx, Throwable ex) {
        log.warn("Fallback for TX {} due to {}", tx.getTransactionId(), ex.toString());
        return Mono.just(new TransactionResponse(tx.getTransactionId(), "FAILED", "Transaction-Service unavailable"));
    }
}

//    public Mono<TransactionResponse> callDownstream(BulkRequestDto.TransactionRequest tx) {
//        return webClient.post()
//                .uri("/transactions")
//                .header("Idempotency-Key", tx.getTransactionId())
//                .bodyValue(tx)
//                .retrieve()
//                .bodyToMono(TransactionResponse.class)
//                .timeout(Duration.ofSeconds(5))
//                .onErrorResume(e -> {
//                    String reason = (e instanceof WebClientResponseException w) ? w.getMessage() : e.getMessage();
//                    log.warn("Downstream call failed for {}: {}", tx.getTransactionId(), reason);
//                    return Mono.just(new TransactionResponse(tx.getTransactionId(), "FAILED", reason));
//                });
//    }
//}
