package com.interswitch.bulktransaction.service;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
//import com.interswitch.bulktransaction.dto.request.TransactionRequest;

import com.interswitch.bulktransaction.dto.response.BulkResponse;
import com.interswitch.bulktransaction.dto.response.TransactionResponse;
//import com.interswitch.bulktransaction.dto.response.TransactionResult;
import reactor.core.publisher.Mono;
public interface TransactionService {
//    Mono<TransactionResponse> callDownstream(BulkRequestDto.TransactionRequest tx);

    Mono<TransactionResponse> callDownstream(BulkRequestDto.TransactionRequest tx);

}
