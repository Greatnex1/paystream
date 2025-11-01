package com.interswitch.bulktransaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String status; // SUCCESS or FAILED
    private String reason;
}
