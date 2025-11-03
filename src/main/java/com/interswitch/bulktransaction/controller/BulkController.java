package com.interswitch.bulktransaction.controller;


import com.interswitch.bulktransaction.controller.constants.ApplicationUrl;
import com.interswitch.bulktransaction.dto.BulkRequestDto;
import com.interswitch.bulktransaction.dto.response.BulkResponse;
import com.interswitch.bulktransaction.service.implementation.BulkProcessingServiceImplementation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
/**

 * This controller exposes endpoints for submitting and tracking the status of bulk transaction batches.
 * It operates in a non-blocking (reactive) manner using {@link Mono} responses provided by Spring WebFlux.
 *
 * <h3>Features</h3>
 * <ul>
 *     <li>Accepts bulk transaction requests and delegates processing to {@link BulkProcessingServiceImplementation}.</li>
 *     <li>Returns an HTTP 202 (Accepted) response with a {@code Location} header to check batch status asynchronously.</li>
 *     <li>Provides a status endpoint to query the processing results of a batch using its ID.</li>
 * </ul>
 *
 */
@RestController
@RequestMapping(ApplicationUrl.BaseUrl)
@RequiredArgsConstructor
@Slf4j
public class BulkController {

    /**
     * Service that handles business logic for processing and retrieving bulk transaction batches.
     */
    private final BulkProcessingServiceImplementation bulkService;

    /**
     * Submits a new bulk transaction batch for asynchronous processing.
     * <p>
     * Upon successful submission, the endpoint returns an HTTP 202 (Accepted) response along with a
     * {@code Location} header pointing to the batch status endpoint.
     * In case of failure, an HTTP 500 (Internal Server Error) is returned.
     * </p>
     *
     * @param request the {@link BulkRequestDto} containing the list of transactions to be processed
     * @return a {@link Mono} emitting a {@link ResponseEntity} with HTTP 202 on success, or 500 on error
     *
     *
     */
    @PostMapping(ApplicationUrl.SubmitBulkUrl)
    public Mono<ResponseEntity<Void>> submitBulk(@Valid @RequestBody BulkRequestDto request) {
        return bulkService.processBulk(request)
                .<ResponseEntity<Void>>map(batchId -> ResponseEntity.accepted()
                        .header("Location", ApplicationUrl.ViewBulkStatusUrl)
                        .build())
                .doOnSuccess(resp -> log.info("Bulk batch {} accepted", request.getBatchId()))
                .onErrorResume(err -> {
                    log.error("Failed to process bulk batch {}: {}", request.getBatchId(), err.getMessage(), err);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Void>build());
                });
    }

    /**
     * Retrieves the processing status and results of a previously submitted bulk transaction batch.
     * <p>
     * This endpoint returns the list of transaction results associated with the specified batch ID.
     * If no results are found, an HTTP 404 (Not Found) is returned.
     * </p>
     *
     * @param batchId the unique identifier of the bulk batch
     * @return a {@link Mono} emitting:
     * <ul>
     *     <li>HTTP 200 (OK) with a list of {@link BulkResponse.TransactionResult} if found</li>
     *     <li>HTTP 404 (Not Found) if no batch matches the provided ID</li>
     * </ul>
     */
    @GetMapping(ApplicationUrl.ViewBulkStatusUrl)
    public Mono<ResponseEntity<List<BulkResponse.TransactionResult>>> viewStatus(@RequestParam String batchId) {
        return bulkService.getBatchStatus(batchId)
                .map(results -> results.isEmpty()
                        ? ResponseEntity.notFound().<List<BulkResponse.TransactionResult>>build()
                        : ResponseEntity.ok(results))
                .doOnError(err -> log.error("Error fetching status for batch {}: {}", batchId, err.getMessage(), err));
    }

}
