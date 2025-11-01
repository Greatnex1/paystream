package com.interswitch.bulktransaction.controller;


import com.interswitch.bulktransaction.dto.BulkRequestDto;
import com.interswitch.bulktransaction.dto.response.BulkResponse;
import com.interswitch.bulktransaction.service.BulkTransactionService;
import com.interswitch.bulktransaction.service.implementation.BulkTransactionServiceImplementation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/bulk-transactions")
@RequiredArgsConstructor
public class BulkController {

    private final BulkTransactionServiceImplementation bulkService;

    @PostMapping("/submit")
    public Mono<ResponseEntity<BulkResponse>> submitBulk(@Valid @RequestBody BulkRequestDto request) {
// In a real app, extract submitterId from auth token
        String submitterId = "system-client";
        return bulkService.processBulk(request, submitterId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.internalServerError().build()));
    }
}
