package com.interswitch.bulktransaction.service.interfaces;

import com.interswitch.bulktransaction.dto.BulkRequestDto;
import com.interswitch.bulktransaction.dto.response.TransactionResponse;
import reactor.core.publisher.Mono;
public interface TransactionService {

    Mono<TransactionResponse> callDownstream(BulkRequestDto.TransactionRequest tx);

}
