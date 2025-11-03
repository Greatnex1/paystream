package com.interswitch.bulktransaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
/**
 * Represents the response returned from the downstream Transaction Service
 * after processing an individual debit or credit transaction.
 * <p>
 * This class provides the outcome of a single transaction request, including
 * its unique identifier, status, and (if applicable) the reason for failure.
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * TransactionResponse response = new TransactionResponse(
 *     "TXN001",
 *     "FAILED",
 *     "Insufficient funds"
 * );
 * }</pre>
 *
 * <p><b>Example JSON:</b></p>
 * <pre>{@code
 * {
 *   "transactionId": "TXN001",
 *   "status": "FAILED",
 *   "reason": "Insufficient funds"
 * }
 * }</pre>
 */
@Data
@AllArgsConstructor
public class TransactionResponse {
    /**
     * Unique identifier assigned to the transaction by the downstream service.
     */
    private String transactionId;
    /**
     * Indicates the processing outcome of the transaction.
     * <p>Possible values:</p>
     * <ul>
     *   <li><b>SUCCESS</b> – The transaction was processed successfully.</li>
     *   <li><b>FAILED</b> – The transaction failed during processing.</li>
     * </ul>
     */
    private String status; // SUCCESS or FAILED
    /**
     * Describes the reason for transaction failure.
     * This field is {@code null} or empty for successful transactions.
     */
    private String reason;
}
